package com.schoolqueue.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.schoolqueue.domain.model.ProximityRange;
import com.schoolqueue.domain.model.QueueStatus;
import com.schoolqueue.infrastructure.adapters.in.web.dto.AnnounceArrivalRequest;
import com.schoolqueue.infrastructure.adapters.in.web.dto.ClassroomResponse;
import com.schoolqueue.infrastructure.adapters.in.web.dto.ParentResponse;
import com.schoolqueue.infrastructure.adapters.in.web.dto.QueueItemResponse;
import com.schoolqueue.infrastructure.adapters.in.web.dto.RegisterClassroomRequest;
import com.schoolqueue.infrastructure.adapters.in.web.dto.RegisterParentRequest;
import com.schoolqueue.infrastructure.adapters.in.web.dto.RegisterSchoolRequest;
import com.schoolqueue.infrastructure.adapters.in.web.dto.RegisterStudentRequest;
import com.schoolqueue.infrastructure.adapters.in.web.dto.SchoolResponse;
import com.schoolqueue.infrastructure.adapters.in.web.dto.StudentResponse;
import com.schoolqueue.infrastructure.adapters.in.web.dto.UpdateStatusRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class PickupQueueFlowIT {

  private static final BigDecimal SCHOOL_LAT = new BigDecimal("-23.550520");
  private static final BigDecimal SCHOOL_LON = new BigDecimal("-46.633308");

  @Container
  static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Container
  static final RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3-management-alpine");

  @DynamicPropertySource
  static void infraProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.rabbitmq.host", rabbit::getHost);
    registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
    registry.add("spring.rabbitmq.username", rabbit::getAdminUsername);
    registry.add("spring.rabbitmq.password", rabbit::getAdminPassword);
  }

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate rest;

  @Autowired private RabbitTemplate rabbitTemplate;

  @Test
  @DisplayName("full pickup flow: school CLOSE to completed, then FAR to auto-call")
  void shouldRunFullPickupFlowEndToEnd() {
    // escola CLOSE -> turma -> responsavel -> aluno
    UUID schoolId = createSchool();
    UUID classroomId = createClassroom(schoolId);
    UUID parentId = createParent(schoolId);
    UUID studentId = createStudent(schoolId, classroomId, parentId);

    // announce CLOSE -> EN_ROUTE + CLOSE + called
    QueueItemResponse item = announce(schoolId, studentId, parentId, SCHOOL_LAT, SCHOOL_LON);
    assertThat(item.journeyStatus()).isEqualTo(QueueStatus.EN_ROUTE);
    assertThat(item.currentRange()).isEqualTo(ProximityRange.CLOSE);
    assertThat(item.called()).isTrue();
    assertThat(item.latitude()).isEqualByComparingTo(SCHOOL_LAT);

    // active contem o item
    assertThat(fetchActive(schoolId)).extracting(QueueItemResponse::id).contains(item.id());

    // ARRIVED, e repetir ARRIVED -> 409
    assertThat(updateStatus(item.id(), "MARK_AS_ARRIVED", null).journeyStatus())
        .isEqualTo(QueueStatus.ARRIVED);
    assertThat(updateStatusRaw(item.id(), "MARK_AS_ARRIVED", null).getStatusCode())
        .isEqualTo(HttpStatus.CONFLICT);

    // COMPLETED -> active vazio
    assertThat(updateStatus(item.id(), "MARK_AS_COMPLETED", null).journeyStatus())
        .isEqualTo(QueueStatus.COMPLETED);
    assertThat(fetchActive(schoolId)).isEmpty();

    // eventos publicados no broker
    int pending =
        rabbitTemplate.execute(
            channel -> channel.queueDeclarePassive("queue.notifications").getMessageCount());
    assertThat(pending).as("arrival + status events should reach the broker").isPositive();

    // announce FAR -> EN_ROUTE + FAR + !called
    UUID farStudentId = createStudent(schoolId, classroomId, parentId, "Aluno FAR E2E");
    QueueItemResponse farItem =
        announce(schoolId, farStudentId, parentId, BigDecimal.ZERO, BigDecimal.ZERO);
    assertThat(farItem.journeyStatus()).isEqualTo(QueueStatus.EN_ROUTE);
    assertThat(farItem.currentRange()).isEqualTo(ProximityRange.FAR);
    assertThat(farItem.called()).isFalse();

    // UPDATE_RANGE CLOSE -> auto-chamada
    QueueItemResponse calledItem = updateStatus(farItem.id(), "UPDATE_RANGE", ProximityRange.CLOSE);
    assertThat(calledItem.currentRange()).isEqualTo(ProximityRange.CLOSE);
    assertThat(calledItem.called()).isTrue();

    // UPDATE_RANGE sem newRange -> 400
    assertThat(updateStatusRaw(farItem.id(), "UPDATE_RANGE", null).getStatusCode())
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  private UUID createSchool() {
    ResponseEntity<SchoolResponse> res =
        rest.postForEntity(
            base("/api/v1/schools"),
            new RegisterSchoolRequest("Escola E2E", SCHOOL_LAT, SCHOOL_LON),
            SchoolResponse.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(res.getBody()).isNotNull();
    return res.getBody().id();
  }

  private UUID createClassroom(UUID schoolId) {
    ResponseEntity<ClassroomResponse> res =
        rest.postForEntity(
            base("/api/v1/classrooms"),
            new RegisterClassroomRequest(schoolId, "Turma E2E"),
            ClassroomResponse.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(res.getBody()).isNotNull();
    return res.getBody().id();
  }

  private UUID createParent(UUID schoolId) {
    ResponseEntity<ParentResponse> res =
        rest.postForEntity(
            base("/api/v1/parents"),
            new RegisterParentRequest("Mae E2E", "11999990000", "mae.e2e@mail.com", schoolId),
            ParentResponse.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(res.getBody()).isNotNull();
    return res.getBody().id();
  }

  private UUID createStudent(UUID schoolId, UUID classroomId, UUID parentId) {
    return createStudent(schoolId, classroomId, parentId, "Aluno E2E");
  }

  private UUID createStudent(UUID schoolId, UUID classroomId, UUID parentId, String name) {
    ResponseEntity<StudentResponse> res =
        rest.postForEntity(
            base("/api/v1/students"),
            new RegisterStudentRequest(schoolId, classroomId, name, List.of(parentId)),
            StudentResponse.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(res.getBody()).isNotNull();
    return res.getBody().id();
  }

  private QueueItemResponse announce(
      UUID schoolId, UUID studentId, UUID parentId, BigDecimal lat, BigDecimal lon) {
    ResponseEntity<QueueItemResponse> res =
        rest.postForEntity(
            base("/api/v1/queue/announce"),
            new AnnounceArrivalRequest(schoolId, studentId, parentId, lat, lon),
            QueueItemResponse.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(res.getBody()).isNotNull();
    return res.getBody();
  }

  private List<QueueItemResponse> fetchActive(UUID schoolId) {
    ResponseEntity<QueueItemResponse[]> res =
        rest.getForEntity(
            base("/api/v1/queue/school/" + schoolId + "/active"), QueueItemResponse[].class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(res.getBody()).isNotNull();
    return List.of(res.getBody());
  }

  private QueueItemResponse updateStatus(UUID itemId, String action, ProximityRange newRange) {
    ResponseEntity<QueueItemResponse> res = updateStatusRaw(itemId, action, newRange);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(res.getBody()).isNotNull();
    return res.getBody();
  }

  private ResponseEntity<QueueItemResponse> updateStatusRaw(
      UUID itemId, String action, ProximityRange newRange) {
    return rest.exchange(
        base("/api/v1/queue/" + itemId + "/status"),
        HttpMethod.PATCH,
        new HttpEntity<>(new UpdateStatusRequest(action, newRange)),
        QueueItemResponse.class);
  }

  private String base(String path) {
    return "http://localhost:" + port + path;
  }
}

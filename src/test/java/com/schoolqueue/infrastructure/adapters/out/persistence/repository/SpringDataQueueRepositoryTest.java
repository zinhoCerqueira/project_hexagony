package com.schoolqueue.infrastructure.adapters.out.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.schoolqueue.domain.model.ProximityRange;
import com.schoolqueue.domain.model.QueueStatus;
import com.schoolqueue.infrastructure.adapters.out.persistence.entity.PickupQueueEntity;
import com.schoolqueue.infrastructure.adapters.out.persistence.entity.SchoolEntity;
import com.schoolqueue.infrastructure.adapters.out.persistence.entity.StudentEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class SpringDataQueueRepositoryTest {

  @Container
  static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @DynamicPropertySource
  static void datasourceProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired private SpringDataQueueRepository repository;

  @PersistenceContext private EntityManager entityManager;

  private FkRefs newFks() {
    UUID schoolId = UUID.randomUUID();
    UUID classroomId = UUID.randomUUID();
    UUID studentId = UUID.randomUUID();
    UUID parentId = UUID.randomUUID();

    persistSchool(schoolId);
    persistClassroom(classroomId, schoolId);
    persistStudent(studentId, schoolId, classroomId);
    persistParent(parentId);

    return new FkRefs(schoolId, studentId, parentId);
  }

  private void persistSchool(UUID schoolId) {
    entityManager.persist(
        new SchoolEntity(
            schoolId, "Escola Central", new BigDecimal("-23.5505"), new BigDecimal("-46.6333")));
  }

  private void persistClassroom(UUID classroomId, UUID schoolId) {
    entityManager
        .createNativeQuery("INSERT INTO classrooms (id, school_id, name) VALUES (?, ?, ?)")
        .setParameter(1, classroomId)
        .setParameter(2, schoolId)
        .setParameter(3, "Turma A")
        .executeUpdate();
  }

  private void persistStudent(UUID studentId, UUID schoolId, UUID classroomId) {
    entityManager.persist(new StudentEntity(studentId, schoolId, classroomId, "João da Silva"));
  }

  private void persistParent(UUID parentId) {
    entityManager
        .createNativeQuery("INSERT INTO parents (id, name, phone) VALUES (?, ?, ?)")
        .setParameter(1, parentId)
        .setParameter(2, "Maria da Silva")
        .setParameter(3, "+55 11 99999-0000")
        .executeUpdate();
  }

  private PickupQueueEntity newEntity(
      FkRefs fks, QueueStatus status, ProximityRange range, boolean called) {
    return new PickupQueueEntity(
        UUID.randomUUID(),
        fks.schoolId(),
        fks.studentId(),
        fks.parentId(),
        status,
        called,
        range,
        new BigDecimal("-23.5505"),
        new BigDecimal("-46.6333"),
        Instant.now(),
        Instant.now());
  }

  private void flushAndClear() {
    entityManager.flush();
    entityManager.clear();
  }

  @Test
  @DisplayName("findBySchoolIdAndJourneyStatusInOrderByCreatedAtAsc filters status and orders ASC")
  @Transactional
  void shouldReturnOnlyMatchingStatusesOrderedByCreatedAtAsc() throws Exception {
    FkRefs fks = newFks();
    FkRefs otherSchoolFks = newFks();

    PickupQueueEntity first =
        repository.save(newEntity(fks, QueueStatus.EN_ROUTE, ProximityRange.FAR, false));
    flushAndClear();
    Thread.sleep(10);
    PickupQueueEntity third =
        repository.save(newEntity(fks, QueueStatus.ARRIVED, ProximityRange.CLOSE, true));
    flushAndClear();
    Thread.sleep(10);
    repository.save(newEntity(fks, QueueStatus.COMPLETED, ProximityRange.CLOSE, true));
    flushAndClear();
    Thread.sleep(10);
    repository.save(newEntity(otherSchoolFks, QueueStatus.EN_ROUTE, ProximityRange.FAR, false));
    flushAndClear();

    List<PickupQueueEntity> found =
        repository.findBySchoolIdAndJourneyStatusInOrderByCreatedAtAsc(
            fks.schoolId(), List.of(QueueStatus.EN_ROUTE, QueueStatus.ARRIVED));

    assertThat(found)
        .extracting(PickupQueueEntity::getId)
        .containsExactly(first.getId(), third.getId());
    assertThat(found)
        .allSatisfy(
            entity -> {
              assertThat(entity.getSchoolId()).isEqualTo(fks.schoolId());
              assertThat(entity.getJourneyStatus()).isIn(QueueStatus.EN_ROUTE, QueueStatus.ARRIVED);
            });
  }

  @Test
  @DisplayName("findBySchoolIdAndJourneyStatusInOrderByCreatedAtAsc returns empty when no match")
  @Transactional
  void shouldReturnEmptyWhenNoItemMatchesSchoolAndStatus() {
    FkRefs fks = newFks();
    repository.save(newEntity(fks, QueueStatus.COMPLETED, ProximityRange.CLOSE, true));
    flushAndClear();

    List<PickupQueueEntity> found =
        repository.findBySchoolIdAndJourneyStatusInOrderByCreatedAtAsc(
            fks.schoolId(), List.of(QueueStatus.EN_ROUTE));

    assertThat(found).isEmpty();
  }

  @Test
  @DisplayName("findFirstByStudentIdAndJourneyStatusInOrderByCreatedAtDesc returns the most recent")
  @Transactional
  void shouldReturnMostRecentActiveItemForStudent() throws Exception {
    FkRefs fks = newFks();

    PickupQueueEntity older =
        repository.save(newEntity(fks, QueueStatus.EN_ROUTE, ProximityRange.FAR, false));
    flushAndClear();
    Thread.sleep(10);
    PickupQueueEntity newer =
        repository.save(newEntity(fks, QueueStatus.ARRIVED, ProximityRange.CLOSE, true));
    flushAndClear();

    Optional<PickupQueueEntity> found =
        repository.findFirstByStudentIdAndJourneyStatusInOrderByCreatedAtDesc(
            fks.studentId(), QueueStatus.activeStatuses());

    assertThat(found).isPresent();
    assertThat(found.orElseThrow().getId()).isEqualTo(newer.getId());
    assertThat(found.orElseThrow().getCreatedAt()).isAfterOrEqualTo(older.getCreatedAt());
    assertThat(found.orElseThrow().getJourneyStatus()).isEqualTo(QueueStatus.ARRIVED);
  }

  @Test
  @DisplayName(
      "findFirstByStudentIdAndJourneyStatusInOrderByCreatedAtDesc returns empty when none active")
  @Transactional
  void shouldReturnEmptyWhenStudentHasNoActiveItem() {
    FkRefs fks = newFks();
    repository.save(newEntity(fks, QueueStatus.COMPLETED, ProximityRange.CLOSE, true));
    flushAndClear();

    Optional<PickupQueueEntity> found =
        repository.findFirstByStudentIdAndJourneyStatusInOrderByCreatedAtDesc(
            fks.studentId(), QueueStatus.activeStatuses());

    assertThat(found).isEmpty();
  }

  @Test
  @DisplayName(
      "findFirstByStudentIdAndJourneyStatusInOrderByCreatedAtDesc does not leak other students")
  @Transactional
  void shouldIgnoreItemsFromOtherStudentsWhenFindingByStudent() throws Exception {
    FkRefs fks = newFks();
    FkRefs otherStudentFks = newFks();

    repository.save(newEntity(fks, QueueStatus.EN_ROUTE, ProximityRange.FAR, false));
    flushAndClear();
    Thread.sleep(10);
    repository.save(newEntity(otherStudentFks, QueueStatus.ARRIVED, ProximityRange.CLOSE, true));
    flushAndClear();

    Optional<PickupQueueEntity> found =
        repository.findFirstByStudentIdAndJourneyStatusInOrderByCreatedAtDesc(
            fks.studentId(), QueueStatus.activeStatuses());

    assertThat(found).isPresent();
    assertThat(found.orElseThrow().getStudentId()).isEqualTo(fks.studentId());
  }

  private record FkRefs(UUID schoolId, UUID studentId, UUID parentId) {}
}

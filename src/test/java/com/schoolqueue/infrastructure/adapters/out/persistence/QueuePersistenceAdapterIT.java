package com.schoolqueue.infrastructure.adapters.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.schoolqueue.domain.model.PickupQueueItem;
import com.schoolqueue.domain.model.ProximityRange;
import com.schoolqueue.domain.model.QueueStatus;
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
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(QueuePersistenceAdapter.class)
@Testcontainers
class QueuePersistenceAdapterIT {

  @Container
  static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @DynamicPropertySource
  static void datasourceProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired private QueuePersistenceAdapter adapter;

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

  private PickupQueueItem newItem(FkRefs fks, QueueStatus status, ProximityRange range) {
    return PickupQueueItem.reconstitute(
        null,
        fks.schoolId(),
        fks.studentId(),
        fks.parentId(),
        status,
        range == ProximityRange.CLOSE,
        range,
        new BigDecimal("-23.5505"),
        new BigDecimal("-46.6333"),
        Instant.now(),
        Instant.now());
  }

  @Test
  @DisplayName("save persists a new item and returns it with a generated id")
  @Transactional
  void shouldPersistNewItemWithGeneratedId() {
    FkRefs fks = newFks();
    PickupQueueItem item = newItem(fks, QueueStatus.EN_ROUTE, ProximityRange.MEDIUM);

    PickupQueueItem saved = adapter.save(item);

    assertThat(saved.id()).isNotNull();
    assertThat(saved.schoolId()).isEqualTo(fks.schoolId());
    assertThat(saved.studentId()).isEqualTo(fks.studentId());
    assertThat(saved.parentId()).isEqualTo(fks.parentId());
    assertThat(saved.journeyStatus()).isEqualTo(QueueStatus.EN_ROUTE);
    assertThat(saved.currentRange()).isEqualTo(ProximityRange.MEDIUM);
  }

  @Test
  @DisplayName("findById returns the previously saved item as a domain object")
  @Transactional
  void shouldRoundTripItemThroughSaveAndFindById() {
    FkRefs fks = newFks();
    PickupQueueItem item = newItem(fks, QueueStatus.EN_ROUTE, ProximityRange.CLOSE);

    PickupQueueItem saved = adapter.save(item);
    PickupQueueItem found = adapter.findById(saved.id()).orElseThrow();

    assertThat(found.id()).isEqualTo(saved.id());
    assertThat(found.journeyStatus()).isEqualTo(QueueStatus.EN_ROUTE);
    assertThat(found.currentRange()).isEqualTo(ProximityRange.CLOSE);
    assertThat(found.called()).isTrue();
    assertThat(found.latitude()).isEqualByComparingTo("-23.5505");
    assertThat(found.longitude()).isEqualByComparingTo("-46.6333");
  }

  @Test
  @DisplayName("findById returns empty when no item exists for the given id")
  void shouldReturnEmptyWhenItemNotFound() {
    Optional<PickupQueueItem> found = adapter.findById(UUID.randomUUID());

    assertThat(found).isEmpty();
  }

  @Test
  @DisplayName(
      "findBySchoolIdAndStatusIn returns only items with requested statuses, ordered by createdAt asc")
  @Transactional
  void shouldReturnOnlyItemsWithRequestedStatusesForSchoolOrderedByCreatedAt() throws Exception {
    FkRefs fks = newFks();
    FkRefs otherSchoolFks = newFks();

    PickupQueueItem first = adapter.save(newItem(fks, QueueStatus.EN_ROUTE, ProximityRange.FAR));
    Thread.sleep(10);
    PickupQueueItem third = adapter.save(newItem(fks, QueueStatus.ARRIVED, ProximityRange.CLOSE));
    Thread.sleep(10);
    PickupQueueItem completed =
        adapter.save(newItem(fks, QueueStatus.COMPLETED, ProximityRange.CLOSE));
    Thread.sleep(10);
    adapter.save(newItem(otherSchoolFks, QueueStatus.EN_ROUTE, ProximityRange.FAR));

    List<PickupQueueItem> active =
        adapter.findBySchoolIdAndStatusIn(
            fks.schoolId(), List.of(QueueStatus.EN_ROUTE, QueueStatus.ARRIVED));

    assertThat(active).extracting(PickupQueueItem::id).containsExactly(first.id(), third.id());
    assertThat(active).noneMatch(item -> item.journeyStatus() == QueueStatus.COMPLETED);
  }

  @Test
  @DisplayName("findActiveByStudentId returns the most recent active item for the student")
  @Transactional
  void shouldReturnMostRecentActiveItemForStudent() throws Exception {
    FkRefs fks = newFks();
    PickupQueueItem older = adapter.save(newItem(fks, QueueStatus.EN_ROUTE, ProximityRange.FAR));
    Thread.sleep(10);
    PickupQueueItem newer = adapter.save(newItem(fks, QueueStatus.ARRIVED, ProximityRange.CLOSE));

    Optional<PickupQueueItem> active = adapter.findActiveByStudentId(fks.studentId());

    assertThat(active).isPresent();
    assertThat(active.orElseThrow().id()).isEqualTo(newer.id());
    assertThat(active.orElseThrow().journeyStatus()).isEqualTo(QueueStatus.ARRIVED);
    assertThat(older.id()).isNotEqualTo(newer.id());
  }

  @Test
  @DisplayName("findActiveByStudentId returns empty when the student has no active item")
  @Transactional
  void shouldReturnEmptyWhenStudentHasNoActiveItem() {
    FkRefs fks = newFks();
    adapter.save(newItem(fks, QueueStatus.COMPLETED, ProximityRange.CLOSE));

    Optional<PickupQueueItem> active = adapter.findActiveByStudentId(fks.studentId());

    assertThat(active).isEmpty();
  }

  private record FkRefs(UUID schoolId, UUID studentId, UUID parentId) {}
}

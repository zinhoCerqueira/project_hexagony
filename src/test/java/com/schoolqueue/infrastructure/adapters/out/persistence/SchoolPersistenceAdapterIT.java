package com.schoolqueue.infrastructure.adapters.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.schoolqueue.domain.model.School;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(SchoolPersistenceAdapter.class)
@Testcontainers
class SchoolPersistenceAdapterIT {

  @Container
  static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @DynamicPropertySource
  static void datasourceProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired private SchoolPersistenceAdapter adapter;

  private School newSchoolWithoutId() {
    return new School(
        null, "Escola Central", new BigDecimal("-23.5505"), new BigDecimal("-46.6333"));
  }

  @Test
  @DisplayName("saves a school and returns the persisted domain with a generated id")
  void shouldSaveSchoolAndReturnPersistedDomainWithGeneratedId() {
    School school = newSchoolWithoutId();

    School saved = adapter.save(school);

    assertThat(saved.id()).isNotNull();
    assertThat(saved.name()).isEqualTo("Escola Central");
    assertThat(saved.latitude()).isEqualByComparingTo(school.latitude());
    assertThat(saved.longitude()).isEqualByComparingTo(school.longitude());
  }

  @Test
  @DisplayName("finds a previously saved school by id")
  void shouldFindPreviouslySavedSchoolById() {
    School saved = adapter.save(newSchoolWithoutId());

    School found = adapter.findById(saved.id()).orElseThrow();

    assertThat(found.id()).isEqualTo(saved.id());
    assertThat(found.name()).isEqualTo("Escola Central");
    assertThat(found.latitude()).isEqualByComparingTo("-23.5505");
    assertThat(found.longitude()).isEqualByComparingTo("-46.6333");
  }

  @Test
  @DisplayName("returns empty when no school exists for the given id")
  void shouldReturnEmptyWhenNoSchoolExistsForGivenId() {
    assertThat(adapter.findById(UUID.randomUUID())).isEmpty();
  }
}

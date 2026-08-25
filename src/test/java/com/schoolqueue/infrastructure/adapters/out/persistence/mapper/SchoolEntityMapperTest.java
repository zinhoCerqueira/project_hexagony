package com.schoolqueue.infrastructure.adapters.out.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.schoolqueue.domain.model.School;
import com.schoolqueue.infrastructure.adapters.out.persistence.entity.SchoolEntity;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SchoolEntityMapperTest {

  private School newDomainSchool() {
    return new School(
        UUID.randomUUID(),
        "Escola Central",
        new BigDecimal("-23.550520"),
        new BigDecimal("-46.633308"));
  }

  private SchoolEntity newEntity() {
    return new SchoolEntity(
        UUID.randomUUID(),
        "Escola Municipal",
        new BigDecimal("-22.906840"),
        new BigDecimal("-43.172890"));
  }

  @Test
  @DisplayName("converts domain to entity preserving every field")
  void shouldConvertDomainToEntityPreservingEveryField() {
    School school = newDomainSchool();

    SchoolEntity entity = SchoolEntityMapper.toEntity(school);

    assertThat(entity.getId()).isEqualTo(school.id());
    assertThat(entity.getName()).isEqualTo(school.name());
    assertThat(entity.getLatitude()).isEqualByComparingTo(school.latitude());
    assertThat(entity.getLongitude()).isEqualByComparingTo(school.longitude());
  }

  @Test
  @DisplayName("converts entity to domain preserving every field")
  void shouldConvertEntityToDomainPreservingEveryField() {
    SchoolEntity entity = newEntity();

    School school = SchoolEntityMapper.toDomain(entity);

    assertThat(school.id()).isEqualTo(entity.getId());
    assertThat(school.name()).isEqualTo(entity.getName());
    assertThat(school.latitude()).isEqualByComparingTo(entity.getLatitude());
    assertThat(school.longitude()).isEqualByComparingTo(entity.getLongitude());
  }

  @Test
  @DisplayName("round-trips domain to entity and back without loss")
  void shouldRoundTripDomainToEntityAndBackWithoutLoss() {
    School school = newDomainSchool();

    School result = SchoolEntityMapper.toDomain(SchoolEntityMapper.toEntity(school));

    assertThat(result.id()).isEqualTo(school.id());
    assertThat(result.name()).isEqualTo(school.name());
    assertThat(result.latitude()).isEqualByComparingTo(school.latitude());
    assertThat(result.longitude()).isEqualByComparingTo(school.longitude());
  }
}

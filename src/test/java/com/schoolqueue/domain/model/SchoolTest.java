package com.schoolqueue.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SchoolTest {

  @Test
  @DisplayName("exposes id and name when constructed with an explicit id")
  void shouldExposeIdAndNameWhenConstructedWithExplicitId() {
    UUID id = UUID.randomUUID();

    School school =
        new School(id, "Escola Municipal", new BigDecimal("-23.5505"), new BigDecimal("-46.6333"));

    assertThat(school.id()).isEqualTo(id);
    assertThat(school.name()).isEqualTo("Escola Municipal");
  }

  @Test
  @DisplayName("generates an id when constructed with a null id")
  void shouldGenerateIdWhenConstructedWithNullId() {
    School school =
        new School(
            null, "Escola Municipal", new BigDecimal("-23.5505"), new BigDecimal("-46.6333"));

    assertThat(school.id()).isNotNull();
    assertThat(school.name()).isEqualTo("Escola Municipal");
  }

  @Test
  @DisplayName("changes name when updated via setter")
  void shouldChangeNameWhenUpdatedViaSetter() {
    School school =
        new School(
            UUID.randomUUID(),
            "Escola Municipal",
            new BigDecimal("-23.5505"),
            new BigDecimal("-46.6333"));

    school.setName("Escola Estadual");

    assertThat(school.name()).isEqualTo("Escola Estadual");
  }

  @Test
  @DisplayName("exposes latitude and longitude when constructed with coordinates")
  void shouldExposeCoordinatesWhenConstructedWithCoordinates() {
    BigDecimal latitude = new BigDecimal("-23.5505");
    BigDecimal longitude = new BigDecimal("-46.6333");
    School school = new School(UUID.randomUUID(), "Escola Municipal", latitude, longitude);

    assertThat(school.latitude()).isEqualTo(latitude);
    assertThat(school.longitude()).isEqualTo(longitude);
  }

  @Test
  @DisplayName("changes latitude when updated via setter")
  void shouldChangeLatitudeWhenUpdatedViaSetter() {
    School school =
        new School(
            UUID.randomUUID(),
            "Escola Municipal",
            new BigDecimal("-23.5505"),
            new BigDecimal("-46.6333"));

    school.setLatitude(new BigDecimal("-22.9068"));

    assertThat(school.latitude()).isEqualTo(new BigDecimal("-22.9068"));
  }

  @Test
  @DisplayName("changes longitude when updated via setter")
  void shouldChangeLongitudeWhenUpdatedViaSetter() {
    School school =
        new School(
            UUID.randomUUID(),
            "Escola Municipal",
            new BigDecimal("-23.5505"),
            new BigDecimal("-46.6333"));

    school.setLongitude(new BigDecimal("-43.1729"));

    assertThat(school.longitude()).isEqualTo(new BigDecimal("-43.1729"));
  }
}

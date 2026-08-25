package com.schoolqueue.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProximityRangeTest {

  private static final BigDecimal SCHOOL_LAT = new BigDecimal("-23.550520");
  private static final BigDecimal SCHOOL_LNG = new BigDecimal("-46.633308");

  private static BigDecimal metersSouthOfSchool(double meters) {
    BigDecimal delta = BigDecimal.valueOf(meters / 111_195.0);
    return SCHOOL_LAT.subtract(delta);
  }

  @Test
  @DisplayName("defines the proximity ranges in specification order")
  void shouldDefineAllExpectedStates() {
    assertThat(ProximityRange.values())
        .containsExactly(ProximityRange.FAR, ProximityRange.MEDIUM, ProximityRange.CLOSE);
  }

  @Test
  @DisplayName("classifies 0.5 km as CLOSE")
  void shouldClassifyHalfKmAsClose() {
    assertThat(ProximityRange.fromDistanceKm(0.5)).isEqualTo(ProximityRange.CLOSE);
  }

  @Test
  @DisplayName("classifies 0.51 km as MEDIUM")
  void shouldClassifyJustAboveHalfKmAsMedium() {
    assertThat(ProximityRange.fromDistanceKm(0.51)).isEqualTo(ProximityRange.MEDIUM);
  }

  @Test
  @DisplayName("classifies 2 km as MEDIUM")
  void shouldClassifyTwoKmAsMedium() {
    assertThat(ProximityRange.fromDistanceKm(2.0)).isEqualTo(ProximityRange.MEDIUM);
  }

  @Test
  @DisplayName("classifies 2.01 km as FAR")
  void shouldClassifyJustAboveTwoKmAsFar() {
    assertThat(ProximityRange.fromDistanceKm(2.01)).isEqualTo(ProximityRange.FAR);
  }

  @Test
  @DisplayName("computes one degree of longitude at the equator as roughly 111.19 km")
  void shouldComputeKnownHaversineDistance() {
    double distanceKm =
        ProximityRange.haversineKm(
            new BigDecimal("0"), new BigDecimal("0"), new BigDecimal("0"), new BigDecimal("1"));

    assertThat(distanceKm).isCloseTo(111.195, within(0.5));
  }

  @Test
  @DisplayName("computes zero distance between identical coordinates")
  void shouldComputeZeroDistanceForSamePoint() {
    double distanceKm = ProximityRange.haversineKm(SCHOOL_LAT, SCHOOL_LNG, SCHOOL_LAT, SCHOOL_LNG);

    assertThat(distanceKm).isCloseTo(0.0, within(0.0001));
  }

  @Test
  @DisplayName("classifies a parent 400 meters away as CLOSE")
  void shouldClassifyParent400mAwayAsClose() {
    assertThat(
            ProximityRange.fromCoordinates(
                metersSouthOfSchool(400), SCHOOL_LNG, SCHOOL_LAT, SCHOOL_LNG))
        .isEqualTo(ProximityRange.CLOSE);
  }

  @Test
  @DisplayName("classifies a parent 1 km away as MEDIUM")
  void shouldClassifyParent1KmAwayAsMedium() {
    assertThat(
            ProximityRange.fromCoordinates(
                metersSouthOfSchool(1_000), SCHOOL_LNG, SCHOOL_LAT, SCHOOL_LNG))
        .isEqualTo(ProximityRange.MEDIUM);
  }

  @Test
  @DisplayName("classifies a parent 3 km away as FAR")
  void shouldClassifyParent3KmAwayAsFar() {
    assertThat(
            ProximityRange.fromCoordinates(
                metersSouthOfSchool(3_000), SCHOOL_LNG, SCHOOL_LAT, SCHOOL_LNG))
        .isEqualTo(ProximityRange.FAR);
  }
}

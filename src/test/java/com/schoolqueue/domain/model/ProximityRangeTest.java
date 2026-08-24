package com.schoolqueue.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProximityRangeTest {

  @Test
  @DisplayName("defines the proximity ranges in specification order")
  void shouldDefineAllExpectedStates() {
    assertThat(ProximityRange.values())
        .containsExactly(ProximityRange.FAR, ProximityRange.MEDIUM, ProximityRange.CLOSE);
  }

  @Test
  @DisplayName("maps eta of 5 minutes to CLOSE")
  void shouldMapEta5ToClose() {
    assertThat(ProximityRange.fromEtaMinutes(5)).isEqualTo(ProximityRange.CLOSE);
  }

  @Test
  @DisplayName("maps eta of 6 minutes to MEDIUM")
  void shouldMapEta6ToMedium() {
    assertThat(ProximityRange.fromEtaMinutes(6)).isEqualTo(ProximityRange.MEDIUM);
  }

  @Test
  @DisplayName("maps eta of 15 minutes to MEDIUM")
  void shouldMapEta15ToMedium() {
    assertThat(ProximityRange.fromEtaMinutes(15)).isEqualTo(ProximityRange.MEDIUM);
  }

  @Test
  @DisplayName("maps eta of 16 minutes to FAR")
  void shouldMapEta16ToFar() {
    assertThat(ProximityRange.fromEtaMinutes(16)).isEqualTo(ProximityRange.FAR);
  }

  @Test
  @DisplayName("maps zero eta to CLOSE")
  void shouldMapEta0ToClose() {
    assertThat(ProximityRange.fromEtaMinutes(0)).isEqualTo(ProximityRange.CLOSE);
  }

  @Test
  @DisplayName("maps typical medium eta to MEDIUM")
  void shouldMapTypicalMediumEtaToMedium() {
    assertThat(ProximityRange.fromEtaMinutes(10)).isEqualTo(ProximityRange.MEDIUM);
  }

  @Test
  @DisplayName("throws when eta is null")
  void shouldThrowWhenEtaIsNull() {
    assertThatThrownBy(() -> ProximityRange.fromEtaMinutes(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("ETA minutes must not be null");
  }
}

package com.schoolqueue.domain.model;

public enum ProximityRange {
  FAR,
  MEDIUM,
  CLOSE;

  public static ProximityRange fromEtaMinutes(Integer eta) {
    if (eta == null) {
      throw new IllegalArgumentException("ETA minutes must not be null");
    }
    if (eta <= 5) {
      return CLOSE;
    }
    if (eta <= 15) {
      return MEDIUM;
    }
    return FAR;
  }
}

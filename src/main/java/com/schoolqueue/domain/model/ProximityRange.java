package com.schoolqueue.domain.model;

import java.math.BigDecimal;

public enum ProximityRange {
  FAR,
  MEDIUM,
  CLOSE;

  private static final double EARTH_RADIUS_KM = 6371.0;
  private static final double CLOSE_LIMIT_KM = 0.5;
  private static final double MEDIUM_LIMIT_KM = 2.0;

  public static ProximityRange fromDistanceKm(double distanceKm) {
    if (distanceKm <= CLOSE_LIMIT_KM) {
      return CLOSE;
    }
    if (distanceKm <= MEDIUM_LIMIT_KM) {
      return MEDIUM;
    }
    return FAR;
  }

  public static ProximityRange fromCoordinates(
      BigDecimal parentLat, BigDecimal parentLng, BigDecimal schoolLat, BigDecimal schoolLng) {
    return fromDistanceKm(haversineKm(parentLat, parentLng, schoolLat, schoolLng));
  }

  static double haversineKm(BigDecimal lat1, BigDecimal lng1, BigDecimal lat2, BigDecimal lng2) {
    double dLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
    double dLng = Math.toRadians(lng2.doubleValue() - lng1.doubleValue());
    double a =
        Math.pow(Math.sin(dLat / 2), 2)
            + Math.cos(Math.toRadians(lat1.doubleValue()))
                * Math.cos(Math.toRadians(lat2.doubleValue()))
                * Math.pow(Math.sin(dLng / 2), 2);
    return 2 * EARTH_RADIUS_KM * Math.asin(Math.sqrt(a));
  }
}

package com.schoolqueue.domain.model;

import java.util.UUID;

public class Parent {
  private final UUID id;
  private String name;
  private String phone;

  public Parent(UUID id, String name, String phone) {
    this.id = id != null ? id : UUID.randomUUID();
    this.name = name;
    this.phone = phone;
  }

  public UUID id() {
    return id;
  }

  public String name() {
    return name;
  }

  public String phone() {
    return phone;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }
}

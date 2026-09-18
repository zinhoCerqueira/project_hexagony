package com.schoolqueue.domain.model;

import java.util.UUID;

public class Parent {
  private final UUID id;
  private String name;
  private String phone;
  private String email;

  public Parent(UUID id, String name, String phone, String email) {
    this.id = id != null ? id : UUID.randomUUID();
    this.name = name;
    this.phone = phone;
    this.email = email;
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

  public String email() {
    return email;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public void setEmail(String email) {
    this.email = email;
  }
}

package com.schoolqueue.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "parents")
public class ParentEntity {

  @Id private UUID id;

  private String name;

  private String phone;

  private String email;

  protected ParentEntity() {}

  public ParentEntity(UUID id, String name, String phone, String email) {
    this.id = id;
    this.name = name;
    this.phone = phone;
    this.email = email;
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }
}

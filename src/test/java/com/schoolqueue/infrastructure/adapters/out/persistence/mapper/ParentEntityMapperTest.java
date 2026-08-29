package com.schoolqueue.infrastructure.adapters.out.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.schoolqueue.domain.model.Parent;
import com.schoolqueue.infrastructure.adapters.out.persistence.entity.ParentEntity;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ParentEntityMapperTest {

  @Test
  @DisplayName("roundtrip preserves all fields")
  void shouldRoundtripPreserveFields() {
    UUID id = UUID.randomUUID();
    Parent domain = new Parent(id, "Maria", "11999998888");

    ParentEntity entity = ParentEntityMapper.toEntity(domain);
    Parent back = ParentEntityMapper.toDomain(entity);

    assertThat(entity.getId()).isEqualTo(id);
    assertThat(entity.getName()).isEqualTo("Maria");
    assertThat(entity.getPhone()).isEqualTo("11999998888");
    assertThat(back.id()).isEqualTo(id);
    assertThat(back.name()).isEqualTo("Maria");
    assertThat(back.phone()).isEqualTo("11999998888");
  }
}

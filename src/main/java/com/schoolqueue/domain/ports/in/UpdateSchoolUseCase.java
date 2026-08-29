package com.schoolqueue.domain.ports.in;

import com.schoolqueue.domain.model.School;
import java.math.BigDecimal;
import java.util.UUID;

public interface UpdateSchoolUseCase {

  School execute(UpdateSchoolCommand command);

  record UpdateSchoolCommand(UUID id, String name, BigDecimal latitude, BigDecimal longitude) {}
}

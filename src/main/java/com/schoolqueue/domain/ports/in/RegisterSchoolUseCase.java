package com.schoolqueue.domain.ports.in;

import com.schoolqueue.domain.model.School;
import java.math.BigDecimal;

public interface RegisterSchoolUseCase {

  School execute(RegisterSchoolCommand command);

  record RegisterSchoolCommand(String name, BigDecimal latitude, BigDecimal longitude) {}
}

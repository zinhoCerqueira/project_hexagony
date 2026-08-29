package com.schoolqueue.domain.ports.in;

import com.schoolqueue.domain.model.School;
import java.util.List;

public interface ListSchoolsUseCase {

  List<School> execute();
}

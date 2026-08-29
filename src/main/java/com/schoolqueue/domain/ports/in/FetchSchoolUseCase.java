package com.schoolqueue.domain.ports.in;

import com.schoolqueue.domain.model.School;
import java.util.UUID;

public interface FetchSchoolUseCase {

  School execute(UUID id);
}

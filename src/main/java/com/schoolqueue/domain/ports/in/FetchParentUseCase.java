package com.schoolqueue.domain.ports.in;

import com.schoolqueue.domain.model.Parent;
import java.util.UUID;

public interface FetchParentUseCase {

  Parent execute(UUID id);
}

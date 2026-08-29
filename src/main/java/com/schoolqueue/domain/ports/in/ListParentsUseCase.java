package com.schoolqueue.domain.ports.in;

import com.schoolqueue.domain.model.Parent;
import java.util.List;

public interface ListParentsUseCase {

  List<Parent> execute();
}

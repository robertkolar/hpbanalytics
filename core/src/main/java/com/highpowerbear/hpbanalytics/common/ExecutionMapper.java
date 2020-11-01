package com.highpowerbear.hpbanalytics.common;

import com.highpowerbear.shared.ExecutionDTO;
import com.highpowerbear.hpbanalytics.database.Execution;
import org.mapstruct.Mapper;

/**
 * Created by robertk on 10/4/2020.
 */
@Mapper(componentModel = "spring")
public interface ExecutionMapper {

    Execution dtoToEntity(ExecutionDTO dto);
}

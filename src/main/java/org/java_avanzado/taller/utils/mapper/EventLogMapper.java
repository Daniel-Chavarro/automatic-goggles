package org.java_avanzado.taller.utils.mapper;

import org.java_avanzado.taller.domain.model.EventLog;
import org.java_avanzado.taller.persistence.entity.EventLogEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EventLogMapper {
    EventLog fromEventLogEntityToDomain(EventLogEntity eventLogEntity);
}

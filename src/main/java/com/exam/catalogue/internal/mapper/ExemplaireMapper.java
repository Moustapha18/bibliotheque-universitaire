package com.exam.catalogue.internal.mapper;

import com.exam.catalogue.internal.domain.Exemplaire;
import com.exam.catalogue.internal.dto.ExemplaireRequest;
import com.exam.catalogue.internal.dto.ExemplaireResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ExemplaireMapper {

    @Mapping(target = "ouvrageId", source = "ouvrage.id")
    ExemplaireResponse toResponse(Exemplaire exemplaire);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ouvrage", ignore = true)
    @Mapping(target = "statut", ignore = true)
    Exemplaire toEntity(ExemplaireRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ouvrage", ignore = true)
    @Mapping(target = "statut", ignore = true)
    void updateEntity(ExemplaireRequest request, @MappingTarget Exemplaire exemplaire);
}

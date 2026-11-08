package com.exam.catalogue.internal.mapper;

import com.exam.catalogue.internal.domain.Ouvrage;
import com.exam.catalogue.internal.dto.OuvrageRequest;
import com.exam.catalogue.internal.dto.OuvrageResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface OuvrageMapper {

    @Mapping(target = "categorieId", source = "categorie.id")
    @Mapping(target = "categorieNom", source = "categorie.nom")
    OuvrageResponse toResponse(Ouvrage ouvrage);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "categorie", ignore = true)
    Ouvrage toEntity(OuvrageRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "categorie", ignore = true)
    void updateEntity(OuvrageRequest request, @MappingTarget Ouvrage ouvrage);
}

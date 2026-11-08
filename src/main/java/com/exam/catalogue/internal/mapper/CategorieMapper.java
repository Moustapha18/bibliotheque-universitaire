package com.exam.catalogue.internal.mapper;

import com.exam.catalogue.internal.domain.Categorie;
import com.exam.catalogue.internal.dto.CategorieRequest;
import com.exam.catalogue.internal.dto.CategorieResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategorieMapper {

    CategorieResponse toResponse(Categorie categorie);

    @Mapping(target = "id", ignore = true)
    Categorie toEntity(CategorieRequest request);

    @Mapping(target = "id", ignore = true)
    void updateEntity(CategorieRequest request, @MappingTarget Categorie categorie);
}

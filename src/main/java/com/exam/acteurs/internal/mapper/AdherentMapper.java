package com.exam.acteurs.internal.mapper;

import com.exam.acteurs.api.AdherentInfo;
import com.exam.acteurs.internal.domain.Adherent;
import com.exam.acteurs.internal.dto.AdherentRequest;
import com.exam.acteurs.internal.dto.AdherentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AdherentMapper {

    @Mapping(target = "quotaMax", expression = "java(adherent.getType().quotaMax())")
    AdherentResponse toResponse(Adherent adherent);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "actif", ignore = true)
    Adherent toEntity(AdherentRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "actif", ignore = true)
    void updateEntity(AdherentRequest request, @MappingTarget Adherent adherent);

    @Mapping(target = "nomComplet", expression = "java(adherent.getPrenom() + \" \" + adherent.getNom())")
    @Mapping(target = "quotaMax", expression = "java(adherent.getType().quotaMax())")
    AdherentInfo toInfo(Adherent adherent);
}

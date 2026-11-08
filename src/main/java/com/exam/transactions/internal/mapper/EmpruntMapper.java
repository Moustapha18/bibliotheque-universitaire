package com.exam.transactions.internal.mapper;

import com.exam.transactions.internal.domain.Emprunt;
import com.exam.transactions.internal.dto.EmpruntResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EmpruntMapper {

    EmpruntResponse toResponse(Emprunt emprunt);
}

package com.exam.transactions.internal.mapper;

import com.exam.transactions.internal.domain.Reservation;
import com.exam.transactions.internal.dto.ReservationResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    ReservationResponse toResponse(Reservation reservation);
}

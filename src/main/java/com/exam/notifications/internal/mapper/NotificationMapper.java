package com.exam.notifications.internal.mapper;

import com.exam.notifications.api.NotificationDto;
import com.exam.notifications.internal.domain.Notification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationDto toDto(Notification notification);
}

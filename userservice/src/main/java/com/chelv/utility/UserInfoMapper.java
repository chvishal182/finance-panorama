package com.chelv.utility;

import com.chelv.entities.UserInfo;
import com.chelv.model.UserInfoDTO;
import com.chelv.model.UserInfoEvent;

public class UserInfoMapper {

    public static UserInfoDTO toDTO(UserInfo entity) {
        return UserInfoDTO.builder()
                .userId(entity.getUserId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .phoneNumber(entity.getPhoneNumber())
                .email(entity.getEmail())
                .profilePicture(entity.getProfilePicture())
                .build();
    }

    public static UserInfo toEntity(UserInfoDTO dto) {
        return UserInfo.builder()
                .userId(dto.getUserId())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .phoneNumber(dto.getPhoneNumber())
                .email(dto.getEmail())
                .profilePicture(dto.getProfilePicture())
                .build();
    }

    public static UserInfoDTO fromEvent(UserInfoEvent event) {
        return UserInfoDTO.builder()
                .userId(event.getUserId())
                .firstName(event.getFirstName())
                .lastName(event.getLastName())
                .email(event.getEmail())
                .phoneNumber(event.getPhoneNumber())
                .profilePicture(event.getProfilePicture())
                .build();
    }
}

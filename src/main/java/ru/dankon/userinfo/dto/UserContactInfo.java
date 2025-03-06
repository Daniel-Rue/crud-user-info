package ru.dankon.userinfo.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserContactInfo {
    private String lastName;
    private String firstName;
    private String patronymic;
    private String phoneNumber;
    private String email;
}

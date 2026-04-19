package com.deliacte.dto.response;

import com.deliacte.enums.UserRole;
import com.deliacte.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private UUID          id;
    private String        firstName;
    private String        lastName;
    private String        fullName;
    private String        email;
    private String        telephone;
    private String        gender;
    private LocalDate     dateOfBirth;
    private String        placeOfBirth;
    private String        address;
    private String        city;
    private String        country;
    private UserStatus    userStatus;
    private UserRole      role;
    private boolean       emailVerified;
    private boolean       phoneVerified;
    private boolean       enabled;
    private String        idUniqueBurkinabe;
    private String        passportNumber;
    private String        nationality;
    private String        residencePermitNumber;
    private String        refugeeCardNumber;
    private String        diplomaticCardNumber;
    private String        profilePhotoUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
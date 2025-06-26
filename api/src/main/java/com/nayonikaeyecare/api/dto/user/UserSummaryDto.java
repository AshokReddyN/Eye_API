package com.nayonikaeyecare.api.dto.user;

import com.nayonikaeyecare.api.entities.user.UserStatus;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDto {

    private String id; // User's ObjectId as String
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String city;
    private String state;
    private String language;
    private UserStatus status;
    private Date createdAt; // User's creation date
}
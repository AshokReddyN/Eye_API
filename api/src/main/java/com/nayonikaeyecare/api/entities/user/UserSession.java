package com.nayonikaeyecare.api.entities.user;

import java.util.Date;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import com.nayonikaeyecare.api.entities.application.Application;
import com.nayonikaeyecare.api.entities.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UserSession entity representing a user session in the system.
 */

@Document(collection = "user_sessions")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@TypeAlias("")
/**
 * UserSession entity representing a user session in the system.
 *
 */

public class UserSession {
    @Id
    private ObjectId id;
    private ObjectId userId;
    private String otp;
    private String applicationCode;
    private ObjectId linkedSessionId;
    private UserSessionStatus status;
    private Date createdAt;
    private Date updatedAt;
}
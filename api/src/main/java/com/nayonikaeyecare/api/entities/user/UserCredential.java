package com.nayonikaeyecare.api.entities.user;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "user_credentials")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data

/**
 * UserCredential entity representing a user credential in the system.
 * credential type can be either phone number or email it is based
 */
public class UserCredential {
    @Id
    private ObjectId id;

    private String credential;
    private String secret;
}
package com.nayonikaeyecare.api.entities.user;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User entity representing a user in the system.
 * This class is annotated with @Document to indicate that it is a MongoDB
 * document.
 * It contains fields for user details such as id, phone number, username, first
 * name,
 * last name, status, and roles.
 * The class is also annotated with Lombok annotations to generate boilerplate
 * code
 * such as getters, setters, constructors, and builder methods.
 * 
 * @author Jayasimha Prasad
 * @since 1.0
 */

@Document(collection = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@TypeAlias("")
public class User {

    @Id
    private ObjectId id;

    private String phoneNumber;

    private String email;

    private ObjectId userCredentialId; // UserCredential entity representing the user's credentials

    private String firstName;

    private String lastName;

    private String city; // Add this field

    private String state; // Add this field

    private UserStatus status;

    List<Permission> permissions; // Permission entity representing the permissions associated with this user

    private String language;
}
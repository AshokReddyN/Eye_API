package com.nayonikaeyecare.api.entities.user;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "roles")
@AllArgsConstructor
@NoArgsConstructor

/**
 * Role entity representing a role in the system.
 * This class is annotated with @Document to indicate that it is a MongoDB document.
 * It contains fields for role details such as id, name, description, and a list of permissions.    
 * @author Jayasimha Prasad
 * @since 1.0
 */

@Data
public class Role {

    @Id
    private String id;

    private String name;
    private String description;

    private List<Permission> permissions; // List of permissions associated with this role
}
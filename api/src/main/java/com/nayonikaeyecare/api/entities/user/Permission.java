package com.nayonikaeyecare.api.entities.user;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Permission entity representing a permission in the system.
 * This class is annotated with @Document to indicate that it is a MongoDB document.
 * It contains fields for permission details such as id, code, and description.     
 * @author Jayasimha Prasad
 * @since 1.0
 */

@Document(collection = "permissions")
@Data
@AllArgsConstructor
@NoArgsConstructor

public class Permission {
    
    @Id
    private String id;

    private String code ;  // CAN_ADD_PATIENT, CAN_VIEW_PATIENT, etc.
    List<Role> roles; // Role entity representing the role associated with this permission
}
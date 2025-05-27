package com.nayonikaeyecare.api.entities.application;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.nayonikaeyecare.api.entities.user.Role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Application represnts various applications that are created as part of this
 * soutino
 * Mobile App - the mobile app used for creating references
 * Web portal - The web app used for operational aspects of the solution.
 * 
 * @author Jayasimha Prasad
 * @since 1.0
 */

@Document(collection = "applications")

@NoArgsConstructor
@AllArgsConstructor
@Data

/**
 * Application entity representing an application in the system.
 * This class is annotated with @Document to indicate that it is a MongoDB
 * document.
 * It contains fields for application details such as id, name, code, and
 * description.
 */

public class Application {

    @Id
    private String id;

    private String name; // Name of the application (e.g., "Mobile App", "Web Portal")
    private String code; // Unique code for the application (e.g., "MOBILE_APP", "WEB_PORTAL")
    private String description; // Description of the application
    private List<ObjectId> permittedRoles;
    private boolean allowAutoRegistration;
}
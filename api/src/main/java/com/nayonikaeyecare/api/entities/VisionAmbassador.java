package com.nayonikaeyecare.api.entities;

/**
 * Represents a Vision Ambassador in the system.
 * This class is intended to model the attributes and behaviors
 * of a Vision Ambassador.
 * 
 * Note: This class currently does not contain any fields or methods.
 * Future implementations should define the necessary properties and
 * functionalities.
 * 
 * @author Anutator
 */

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
// import scala.math.BigInt;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

// @Document(value = "visionAmbassador")
@Document(collection = "vision_ambassadors")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data

public class VisionAmbassador {
    @Id
    private ObjectId id;
    private String userId; // Add this line
    private String name;
    // private String email;
    private String phoneNumber;
    private boolean status;
    private String city;
    private String state;
    private String language;
    private Date createdAt;
    private Date updatedAt;
}

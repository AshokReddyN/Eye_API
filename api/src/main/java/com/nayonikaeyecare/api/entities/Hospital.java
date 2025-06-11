package com.nayonikaeyecare.api.entities;

import java.util.Date; // Import Date
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate; // Import CreatedDate
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate; // Import LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
//Remove this import if it exists, as @Field is not being used for the new date fields.
//import org.springframework.data.mongodb.core.mapping.Field; 
import org.springframework.data.mongodb.core.index.Indexed;


@Document(value = "hospitals")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Hospital {
    @Id
    private ObjectId id;
    private String hospitalCode;
    private String name;
    private Address address;
    private List<String> services;
    private Boolean status;
    private String coordinator;
    private String coordinator_phonenumber;
    private String coordinator_email;
    private String googleLink;
    private String registration_date;

    @CreatedDate
    private Date createdAt;

    @LastModifiedDate
    private Date updatedAt;
}

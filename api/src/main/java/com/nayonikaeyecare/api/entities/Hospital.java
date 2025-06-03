package com.nayonikaeyecare.api.entities;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

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
}

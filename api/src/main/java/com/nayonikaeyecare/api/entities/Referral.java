package com.nayonikaeyecare.api.entities;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
import java.util.List;
import java.time.LocalDateTime;

@Document(value = "referrals")
@CompoundIndexes({
        @CompoundIndex(name = "patient_created", def = "{'patientId': 1, 'createdAt': -1}")
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Referral {

    @Id
    private ObjectId id;
    private Status status;
    private ObjectId patientId;
    private String patientName;
    private String ageRange;
    private String gender;
    private String hospitalName;
    private String city;
    private String state;
    private String guardianContact;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    
    private ObjectId hospitalId;
    private ObjectId ambassadorId;
    private List<ServiceType> services;
    private String treatment;
    private EyeDetails rightEye;
    private EyeDetails leftEye;
    private String remarks;
    private Date createdAt;
    private Date updatedAt;
    private Boolean isSpectacleRequested;
    private String spectacleRequestedOn;
    private String hospitalCode;
    
}

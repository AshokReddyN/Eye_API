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
import com.nayonikaeyecare.api.pii.annotation.EncryptedField;

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
    @EncryptedField
    private String patientName;
    @EncryptedField
    private String age;
    private String gender;
    private String hospitalName;
    @EncryptedField
    private String city;
    @EncryptedField
    private String state;
    @EncryptedField
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
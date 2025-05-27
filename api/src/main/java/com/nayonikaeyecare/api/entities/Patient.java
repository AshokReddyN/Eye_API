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

@Document(value = "patients")
@CompoundIndexes({
        @CompoundIndex(name = "ambassador_name", def = "{'ambassadorId': 1, 'name': 1}"),
        @CompoundIndex(name = "state_city", def = "{'state': 1, 'city': 1}")
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Patient {

    @Id
    private ObjectId id;
    private String name;
    private String ambassadorId;
    private Gender gender; // MALE , FEMALE
    private String ageRange;
    private String phone;
    private String email;
    private String hospitalName;
    private String status;
    private String city;
    private String state;
    private List<String> referralIds;
    private Guardian guardianContact;
    private Date createdAt;
    private Date updatedAt;
}

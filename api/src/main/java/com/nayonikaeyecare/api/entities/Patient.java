package com.nayonikaeyecare.api.entities;

import com.nayonikaeyecare.api.pii.annotation.EncryptedField;
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
import java.util.ArrayList;
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
    @EncryptedField
    private String name;
    private String ambassadorId; // Assuming ambassadorId is not directly PII, but a reference or code. If it's sensitive, it should be annotated.
    private Gender gender; // MALE , FEMALE
    @EncryptedField
    private String age;
    @EncryptedField
    private String phone;
    @EncryptedField
    private String email;
    private String hospitalName;
    private String status;
    @EncryptedField
    private String city;
    @EncryptedField
    private String state;
    @Builder.Default
    private List<String> referralIds = new ArrayList<>();
    private Guardian guardianContact; // Fields within Guardian are already annotated
    private Date createdAt;
    private Date updatedAt;
}
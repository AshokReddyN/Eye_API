package com.nayonikaeyecare.api.entities;

import com.nayonikaeyecare.api.pii.annotation.EncryptedField;
import com.nayonikaeyecare.api.pii.annotation.EncryptedField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Guardian {

    @EncryptedField
    private String name;
    private String relation;
    @EncryptedField 
    private Address address; // Fields within Address are already annotated
    @EncryptedField
    private String phone;
    @EncryptedField
    private String email;
}
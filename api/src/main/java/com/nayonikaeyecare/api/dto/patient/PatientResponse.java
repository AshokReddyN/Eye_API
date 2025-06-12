package com.nayonikaeyecare.api.dto.patient;

import java.util.Date;
import java.util.List;

import com.nayonikaeyecare.api.entities.Gender;
import com.nayonikaeyecare.api.entities.Guardian;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PatientResponse {

    private String id;
    private String name;
    private String hospitalName;
    private String ambassadorId;
    private String status;
    private Gender gender;
    private String age;
    private String phone;
    private String email;
    private String city;
    private String state;
    private Guardian guardian;
    private List<String> referralIds;
    private Date createdAt;
    private Date updatedAt;
}
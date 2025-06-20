package com.nayonikaeyecare.api.dto.visionambassador;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class VisionAmbassadorResponse {

    private String id;
    private String name;
    private String phoneNumber;
    private String userId;
    private boolean status;
    private String city;
    private String state;
    private String language;
    private Integer patientCount;
    private Date createdAt;
    private Date updatedAt;
}

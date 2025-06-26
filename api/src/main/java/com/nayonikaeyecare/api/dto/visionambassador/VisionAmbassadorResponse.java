package com.nayonikaeyecare.api.dto.visionambassador;

import com.nayonikaeyecare.api.dto.user.UserSummaryDto; // Added for userDetails
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

    private String id; // This is VisionAmbassador's own ID
    private UserSummaryDto userDetails; // Details of the linked User
    private String userId;
    private String name;
    private String phoneNumber;
    private boolean status;
    private String city;
    private String state;
    private String language;
    private Integer patientCount;
    private Date createdAt;
    private Date updatedAt;
}
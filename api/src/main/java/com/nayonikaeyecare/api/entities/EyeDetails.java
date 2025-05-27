package com.nayonikaeyecare.api.entities;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EyeDetails {
    private String sph;
    private String cyl;
    private String axis;
}
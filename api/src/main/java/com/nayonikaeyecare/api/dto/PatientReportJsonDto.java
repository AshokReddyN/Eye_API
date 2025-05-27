package com.nayonikaeyecare.api.dto;

import com.nayonikaeyecare.api.entities.EyeDetails;

import lombok.Data;

@Data
public class PatientReportJsonDto {
    private String id;
    private String childName;
    private String sex;
    private String ageRange;
    private String treatment;
    private EyeDetails rightEye;
    private EyeDetails leftEye;
    private String remarks;
}
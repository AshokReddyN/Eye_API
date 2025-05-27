package com.nayonikaeyecare.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PatientReportRequestDto {
    @JsonProperty("Id")
    private String id;

    @JsonProperty("Child Name")
    private String childName;

    @JsonProperty("Sex")
    private String sex;

    @JsonProperty("Age Range")
    private String ageRange;

    @JsonProperty("City Name")
    private String cityName;

    @JsonProperty("Phone No")
    private Long phoneNo;

    @JsonProperty("Treatment")
    private String treatment;

    @JsonProperty("R Sph")
    private String rightSph;

    @JsonProperty("R Cyl")
    private Double rightCyl;

    @JsonProperty("R Axis")
    private Integer rightAxis;

    @JsonProperty("L sph")
    private String leftSph;

    @JsonProperty("L cyl")
    private Double leftCyl;

    @JsonProperty("L Axis")
    private Integer leftAxis;

    @JsonProperty("Treatment Date")
    private String treatmentDate;

    @JsonProperty("Remarks")
    private String remarks;

    @JsonProperty("Patient name")
    private String patientName;

    @JsonProperty("Hospital name")
    private String hospitalName;

    @JsonProperty("City")
    private String city;

    @JsonProperty("State")
    private String state;
    
}
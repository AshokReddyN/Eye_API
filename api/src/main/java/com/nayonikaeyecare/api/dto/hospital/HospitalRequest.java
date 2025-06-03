package com.nayonikaeyecare.api.dto.hospital;

import java.util.List;

import com.nayonikaeyecare.api.entities.Address;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * HospitalRequest represents the incoming request payload for all of Hospital
 * APIs
 * 
 * @param id          - String - unique id of the hospital
 * @param name        - string - name of the hospital - mandatory
 * @param address     - Address - address of the hospital - mandatory
 * @param services    - String - services offered by the hospital - mandatory
 * @param status      - boolean - status of the hospital - mandatory
 * @param coordinator - String - name of the coordinator
 * @param coordinator_phonenumber - String - phonenumber of the coordinator
 * @param coordinator_email - String - email of the coordinator
 * @param googleLink  - String - link to google map
 */
@Builder

public record HospitalRequest(
    @NotBlank(message = "Hospital code is required") @Size(max = 100, message = "Hospital Id must be less than 100 characters") String hospitalCode,
    @NotBlank(message = "Hospital name is required") @Size(max = 100, message = "Hospital name must be less than 100 characters") String name,
    @NotNull(message = "Address is required") @Valid Address address,
    @Size(max = 100, message = "Coordinator name must be less than 100 characters") String coordinator,
    String coordinator_phonenumber,
    @Pattern(regexp = "^[^\s@]+@[^\s@]+\\.[^\s@]+$", message = "Email must be a valid email address") String coordinator_email,
    List<String> services,
    String googleLink,
    String registration_date,
    Boolean status
) {}

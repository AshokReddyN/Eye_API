package com.nayonikaeyecare.api.dto.hospital;

import java.util.List;
import com.nayonikaeyecare.api.entities.Address;
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
 * @param googleLink  - String - link to google map
 */
@Builder
public record HospitalResponse(
                String id,
                String hospitalCode,
                String name,
                Address address,
                List<String> services,
                Boolean status,
                String coordinator,
                String coordinator_phonenumber,
                String coordinator_email,
                String googleLink,
                String registration_date) {
}

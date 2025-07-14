package com.nayonikaeyecare.api.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Guardian {

    private String name;
    private String relation; 
    private Address address;
    private String phone;
    private String email;
}
 
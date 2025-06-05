package com.nayonikaeyecare.api.repositories.hospital;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.nayonikaeyecare.api.entities.Hospital;

public interface CustomHospitalRepository {
    Page<Hospital> filterHospitals(String state, List<String> cities, Boolean status, String searchString,List<String> serviceTypes,Pageable pageable);
}


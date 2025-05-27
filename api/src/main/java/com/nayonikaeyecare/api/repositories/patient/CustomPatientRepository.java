package com.nayonikaeyecare.api.repositories.patient;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.nayonikaeyecare.api.dto.patient.PatientResponse;

public interface CustomPatientRepository {

        Page<PatientResponse> filterPatients(String state, String city, String name, String ambassadorId,
                        Pageable pageable);
}
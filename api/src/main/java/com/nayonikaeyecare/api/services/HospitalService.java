package com.nayonikaeyecare.api.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.nayonikaeyecare.api.dto.hospital.HospitalRequest;
import com.nayonikaeyecare.api.dto.hospital.HospitalResponse;
import com.nayonikaeyecare.api.entities.Hospital;
import com.nayonikaeyecare.api.exceptions.ResourceMissingException;
import com.nayonikaeyecare.api.mappers.HospitalMapper;
import com.nayonikaeyecare.api.repositories.hospital.HospitalRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HospitalService {

    private final HospitalRepository hospitalRepository;

    public HospitalResponse createHospital(HospitalRequest hospitalRequest) {
        validateHospitalRequest(hospitalRequest);

        Hospital hospital = Hospital.builder()
                .hospitalCode(hospitalRequest.hospitalCode())
                .name(hospitalRequest.name())
                .address(hospitalRequest.address())
                .services(hospitalRequest.services())
                .status(hospitalRequest.status())
                .coordinator(hospitalRequest.coordinator())
                .coordinator_phonenumber(hospitalRequest.coordinator_phonenumber())
                .coordinator_email(hospitalRequest.coordinator_email())
                .googleLink(hospitalRequest.googleLink())
                .registration_date(hospitalRequest.registration_date())
                .build();

        Hospital savedHospital = hospitalRepository.save(hospital);
        return HospitalMapper.mapToHospitalResponse(savedHospital);
    }

    public List<HospitalResponse> getAllHospitals() {
        return hospitalRepository.findAll().stream()
                .map(HospitalMapper::mapToHospitalResponse)
                .toList();
    }

    public HospitalResponse getHospitalById(String id) {
        ObjectId objectId = convertToObjectId(id);
        return hospitalRepository.findById(objectId)
                .map(HospitalMapper::mapToHospitalResponse)
                .orElseThrow(() -> new ResourceMissingException("Hospital not found with id: " + id));
    }

    public HospitalResponse updateHospital(String id, HospitalRequest hospitalRequest) {
        ObjectId objectId = convertToObjectId(id);
        validateHospitalRequest(hospitalRequest);

        Hospital existingHospital = hospitalRepository.findById(objectId)
                .orElseThrow(() -> new ResourceMissingException("Hospital not found with id: " + id));

        existingHospital.setHospitalCode(hospitalRequest.hospitalCode());
        existingHospital.setName(hospitalRequest.name());
        existingHospital.setAddress(hospitalRequest.address());
        existingHospital.setServices(hospitalRequest.services());
        existingHospital.setStatus(hospitalRequest.status());
        existingHospital.setCoordinator(hospitalRequest.coordinator());
        existingHospital.setCoordinator_phonenumber(hospitalRequest.coordinator_phonenumber());
        existingHospital.setCoordinator_email(hospitalRequest.coordinator_email());
        existingHospital.setGoogleLink(hospitalRequest.googleLink());
        existingHospital.setRegistration_date(hospitalRequest.registration_date());

        Hospital updatedHospital = hospitalRepository.save(existingHospital);
        return HospitalMapper.mapToHospitalResponse(updatedHospital);
    }

    public void deleteHospitalById(String id) {
        ObjectId objectId = convertToObjectId(id);
        if (!hospitalRepository.existsById(objectId)) {
            throw new ResourceMissingException("Hospital not found with id: " + id);
        }
        hospitalRepository.deleteById(objectId);
    }

    private ObjectId convertToObjectId(String id) {
        try {
            return new ObjectId(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid ObjectId format: " + id);
        }
    }

    private void validateHospitalRequest(HospitalRequest request) {
        if (!StringUtils.hasText(request.name())) {
            throw new IllegalArgumentException("Hospital name cannot be empty");
        }
        if (request.address() == null) {
            throw new IllegalArgumentException("Address cannot be null");
        }
    }

    private void validateId(String id) {
        if (!StringUtils.hasText(id)) {
            throw new IllegalArgumentException("ID cannot be empty");
        }
    }

    public Page<HospitalResponse> filterHospitals(String state, List cities, Boolean status, String searchString,List<String> serviceTypes,
            Pageable pageable) {
        return hospitalRepository.filterHospitals(state, cities, status, searchString,serviceTypes,pageable);
    }

    public void saveAllHospitals(List<HospitalRequest> hospitalRequests) {
        if (hospitalRequests == null || hospitalRequests.isEmpty()) {
            throw new IllegalArgumentException("Hospital requests list cannot be null or empty");
        }

        // Validate all requests first
        hospitalRequests.forEach(this::validateHospitalRequest);

        // Convert all requests to entities
        List<Hospital> hospitals = hospitalRequests.stream()
                .map(request -> Hospital.builder()
                        .hospitalCode(request.hospitalCode())
                        .name(request.name())
                        .address(request.address())
                        .services(request.services())
                        .status(request.status())
                        .coordinator(request.coordinator())
                        .coordinator_phonenumber(request.coordinator_phonenumber())
                        .coordinator_email(request.coordinator_email())
                        .googleLink(request.googleLink())
                        .registration_date(request.registration_date())
                        .build())
                .toList();

        // Save all in batch
        hospitalRepository.saveAll(hospitals);
    }

    public int saveAllHospitalsIfNameNotExists(List<HospitalRequest> hospitalRequests) {
        int inserted = 0;
        if (hospitalRequests == null || hospitalRequests.isEmpty()) {
            throw new IllegalArgumentException("Hospital requests list cannot be null or empty");
        }
        hospitalRequests.forEach(this::validateHospitalRequest);
    
        List<Hospital> hospitalsToInsert = hospitalRequests.stream()
                .filter(request -> !hospitalRepository.existsByName(request.name()))
                .map(request -> Hospital.builder()
                        .hospitalCode(request.hospitalCode())
                        .name(request.name())
                        .address(request.address())
                        .services(request.services())
                        .status(request.status())
                        .coordinator(request.coordinator())
                        .coordinator_phonenumber(request.coordinator_phonenumber())
                        .coordinator_email(request.coordinator_email())
                        .googleLink(request.googleLink())
                        .registration_date(request.registration_date())
                        .build())
                .toList();
    
        if (hospitalsToInsert.isEmpty()) {
            throw new IllegalArgumentException("No new hospitals to save");
        } else {
            inserted = hospitalRepository.saveAll(hospitalsToInsert).size();
        }
    
        return inserted;
    }        
}

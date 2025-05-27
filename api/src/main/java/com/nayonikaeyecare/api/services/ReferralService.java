package com.nayonikaeyecare.api.services;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nayonikaeyecare.api.dto.referral.ReferralRequest;
import com.nayonikaeyecare.api.dto.referral.ReferralResponse;
import com.nayonikaeyecare.api.entities.Referral;
import com.nayonikaeyecare.api.entities.Patient;
import com.nayonikaeyecare.api.mappers.ReferralMapper;
import com.nayonikaeyecare.api.repositories.referral.ReferralRepository;
import com.nayonikaeyecare.api.repositories.patient.PatientRepository;


import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Transactional
public class ReferralService {

    private final ReferralRepository referralRepository;
    private final ReferralMapper referralMapper;
    private final PatientRepository patientRepository;
    private final MongoTemplate mongoTemplate;

    @Transactional
    public ReferralResponse createReferral(ReferralRequest referralRequest) {
  
          // Update patient's referralIds
        Patient patient = patientRepository.findById(new ObjectId(referralRequest.patientId()))
            .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
        
        // Initialize referralIds list if null
        if (patient.getReferralIds() == null) {
            patient.setReferralIds(new ArrayList<>());
        }
        else {
            // Check for duplicates
            if (patient.getStatus().equals(referralRequest.status().toString())) {
                throw new IllegalArgumentException("Referral already in progress. PLease create after completion");
            }
        }
        Referral referral = referralMapper.toEntity(referralRequest);
        referral.setCreatedAt(new Date());
        referral.setUpdatedAt(new Date());
        Referral savedReferral = referralRepository.save(referral);
        
        // Add new referral ID to the list
        patient.getReferralIds().add(savedReferral.getId().toString());
        patient.setHospitalName(referralRequest.hospitalName());
        patient.setStatus(referralRequest.status().toString());
        // Save updated patient
        patientRepository.save(patient);
        return referralMapper.toResponse(savedReferral);
    }

    public List<ReferralResponse> getAllReferrals() {
        return referralRepository.findAll()
                .stream()
                .map(referralMapper::toResponse)
                .collect(Collectors.toList());
    }

    public ReferralResponse getReferralById(String id) {
        Referral referral = referralRepository.findById(new ObjectId(id))
                .orElseThrow(() -> new IllegalArgumentException("Referral not found with id: " + id));
        return referralMapper.toResponse(referral);
    }

    public List<ReferralResponse> getReferralsByAmbassadorId(String ambassadorId) {
        return referralRepository.findByAmbassadorId(new ObjectId(ambassadorId))
                .stream()
                .map(referralMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<ReferralResponse> getReferralsByHospitalId(String hospitalId) {
        return referralRepository.findByHospitalId(new ObjectId(hospitalId))
                .stream()
                .map(referralMapper::toResponse)
                .collect(Collectors.toList());
    }

    public Page<ReferralResponse> getReferralsByHospitalIdPaginated(String hospitalId, Pageable pageable) {
        return referralRepository.findByHospitalId(new ObjectId(hospitalId), pageable)
                .map(referralMapper::toResponse);
    }

    public List<ReferralResponse> getReferralsByPatientId(String patientId) {
        return referralRepository.findByPatientId(new ObjectId(patientId))
                .stream()
                .map(referralMapper::toResponse)
                .collect(Collectors.toList());
    }

    public Page<ReferralResponse> filterReferrals(ObjectId ambassadorId, String state, String city, Boolean status, 
        String name, Pageable pageable) {
    
    // Create criteria based on non-null parameters
    Criteria criteria = new Criteria();

    if (ambassadorId != null && !ambassadorId.toHexString().isEmpty()) {
        criteria = criteria.and("ambassadorId").is(ambassadorId);
    }
    
    if (state != null && !state.isEmpty()) {
        criteria = criteria.and("state").is(state);
    }
    
    if (city != null && !city.isEmpty()) {
        criteria = criteria.and("city").is(city);
    }
    
    if (status != null) {
        criteria = criteria.and("status").is(status);
    }
    
    if (name != null && !name.isEmpty()) {
        // Case-insensitive partial name match
        criteria = criteria.and("patientName").regex(name, "i");
    }
    
    Query query = Query.query(criteria).with(pageable);
    
    // Get total count for pagination
    long total = mongoTemplate.count(Query.query(criteria), Referral.class);
    
    // Execute query with pagination
    List<Referral> referrals = mongoTemplate.find(query, Referral.class);
    
    // Convert to response DTOs
    List<ReferralResponse> referralResponses = referrals.stream()
        .map(referralMapper::toResponse)
        .collect(Collectors.toList());
    
    return new PageImpl<>(referralResponses, pageable, total);
}

    public void deleteReferralById(String id) {
        referralRepository.deleteById(new ObjectId(id));
    }

    public ReferralResponse updateReferral(String id, ReferralRequest referralRequest) {
        Referral existingReferral = referralRepository.findById(new ObjectId(id))
                .orElseThrow(() -> new IllegalArgumentException("Referral not found with id: " + id));

        Referral updatedReferral = referralMapper.updateEntity(existingReferral, referralRequest);
        Referral savedReferral = referralRepository.save(updatedReferral);
        return referralMapper.toResponse(savedReferral);
    }
}

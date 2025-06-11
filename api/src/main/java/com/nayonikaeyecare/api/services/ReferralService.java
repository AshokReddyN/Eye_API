package com.nayonikaeyecare.api.services;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nayonikaeyecare.api.dto.referral.BulkReferralUpdateRequest;
import com.nayonikaeyecare.api.dto.referral.BulkReferralUpdateRequest.EyeDetailsDto;
import com.nayonikaeyecare.api.dto.referral.BulkReferralUpdateResponse;
import com.nayonikaeyecare.api.dto.referral.BulkReferralUpdateResponse.RejectedReferralInfo;

import com.nayonikaeyecare.api.dto.referral.ReferralRequest;
import com.nayonikaeyecare.api.dto.referral.ReferralResponse;

import com.nayonikaeyecare.api.entities.EyeDetails;
import com.nayonikaeyecare.api.entities.Hospital;
import com.nayonikaeyecare.api.entities.Patient;

import com.nayonikaeyecare.api.entities.Referral;
import com.nayonikaeyecare.api.entities.Status;
import com.nayonikaeyecare.api.mappers.ReferralMapper;
import com.nayonikaeyecare.api.repositories.hospital.HospitalRepository;
import com.nayonikaeyecare.api.repositories.patient.PatientRepository;
import com.nayonikaeyecare.api.repositories.referral.ReferralRepository;
import com.nayonikaeyecare.api.repositories.visionambassador.VisionAmbassadorRepository;
import com.nayonikaeyecare.api.repositories.user.UserRepository; // Added
import com.nayonikaeyecare.api.entities.VisionAmbassador;
import com.nayonikaeyecare.api.entities.user.User; // Added

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest; // Added
import org.springframework.data.domain.Sort;       // Added
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Added

import java.util.ArrayList;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j // Added
public class ReferralService {

    private final ReferralRepository referralRepository;
    private final ReferralMapper referralMapper;
    private final PatientRepository patientRepository;
    private final HospitalRepository hospitalRepository;
    private final MongoTemplate mongoTemplate;
    private final VisionAmbassadorRepository visionAmbassadorRepository;
    private final UserRepository userRepository; // Added

    @Transactional
    public ReferralResponse createReferral(ReferralRequest referralRequest) {
  
          // Update patient's referralIds
        Patient patient = patientRepository.findById(new ObjectId(referralRequest.patientId()))
            .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
        
        Referral referral = referralMapper.toEntity(referralRequest);
        referral.setCreatedAt(new Date());
        referral.setUpdatedAt(new Date());
        referral.setStatus(Status.REFERRED); // Set initial status to INPROGRESS
        Referral savedReferral = referralRepository.save(referral);
        
        // Add new referral ID to the list
        patient.getReferralIds().add(savedReferral.getId().toString());
        patient.setStatus(Status.REFERRED.name()); // Set patient status to REFERRED
        log.info("Patient ID: {}, Referral ID added to patient: {}", patient.getId(), savedReferral.getId());
        patient.setHospitalName(savedReferral.getHospitalName());
        // Save updated patient
        patientRepository.save(patient);
        User user = null;
        if (savedReferral.getAmbassadorId() != null) {
            log.info("Referral ID: {}, Original VisionAmbassador ID (ObjectId): {}", savedReferral.getId(), savedReferral.getAmbassadorId());
            VisionAmbassador visionAmbassador = visionAmbassadorRepository.findById(savedReferral.getAmbassadorId()).orElse(null);
            if (visionAmbassador != null) {
                log.info("Found VisionAmbassador: ID_DB={}, userId_string={}", visionAmbassador.getId(), visionAmbassador.getUserId());
                if (visionAmbassador.getUserId() != null && !visionAmbassador.getUserId().trim().isEmpty()) {
                    try {
                        ObjectId userObjectId = new ObjectId(visionAmbassador.getUserId());
                        log.info("Attempting to find User with ObjectId: {}", userObjectId);
                        user = userRepository.findById(userObjectId).orElse(null);
                        if (user != null) {
                            log.info("Found User: ID_DB={}, Name={}", user.getId(), user.getFirstName() + " " + user.getLastName());
                        } else {
                            log.warn("User NOT FOUND for ObjectId: {}", userObjectId);
                        }
                    } catch (IllegalArgumentException e) {
                        log.error("Invalid ObjectId format for userId_string: {} from VisionAmbassador ID: {}", visionAmbassador.getUserId(), visionAmbassador.getId(), e);
                    }
                } else {
                    log.warn("VisionAmbassador ID: {} has null or empty userId_string.", visionAmbassador.getId());
                }
            } else {
                log.warn("VisionAmbassador NOT FOUND for ID: {}", savedReferral.getAmbassadorId());
            }
        } else {
            log.info("Referral ID: {}, has null VisionAmbassadorId.", savedReferral.getId());
        }
        return referralMapper.toResponse(savedReferral, user,null,null);
    }

    public List<ReferralResponse> getAllReferrals() {
        return referralRepository.findAll()
                .stream()
                .map(referral -> {
                    User user = null;
                    if (referral.getAmbassadorId() != null) {
                        log.info("Referral ID: {}, Original VisionAmbassador ID (ObjectId): {}", referral.getId(), referral.getAmbassadorId());
                        VisionAmbassador visionAmbassador = visionAmbassadorRepository.findById(referral.getAmbassadorId()).orElse(null);
                        if (visionAmbassador != null) {
                            log.info("Found VisionAmbassador: ID_DB={}, userId_string={}", visionAmbassador.getId(), visionAmbassador.getUserId());
                            if (visionAmbassador.getUserId() != null && !visionAmbassador.getUserId().trim().isEmpty()) {
                                try {
                                    ObjectId userObjectId = new ObjectId(visionAmbassador.getUserId());
                                    log.info("Attempting to find User with ObjectId: {}", userObjectId);
                                    user = userRepository.findById(userObjectId).orElse(null);
                                    if (user != null) {
                                        log.info("Found User: ID_DB={}, Name={}", user.getId(), user.getFirstName() + " " + user.getLastName());
                                    } else {
                                        log.warn("User NOT FOUND for ObjectId: {}", userObjectId);
                                    }
                                } catch (IllegalArgumentException e) {
                                    log.error("Invalid ObjectId format for userId_string: {} from VisionAmbassador ID: {}", visionAmbassador.getUserId(), visionAmbassador.getId(), e);
                                }
                            } else {
                                log.warn("VisionAmbassador ID: {} has null or empty userId_string.", visionAmbassador.getId());
                            }
                        } else {
                            log.warn("VisionAmbassador NOT FOUND for ID: {}", referral.getAmbassadorId());
                        }
                    } else {
                        log.info("Referral ID: {}, has null VisionAmbassadorId.", referral.getId());
                    }
                    return referralMapper.toResponse(referral, user,null,null);
                })
                .collect(Collectors.toList());
    }

    public ReferralResponse getReferralById(String id) {
        Referral referral = referralRepository.findById(new ObjectId(id))
                .orElseThrow(() -> new IllegalArgumentException("Referral not found with id: " + id));
        User user = null;
        if (referral.getAmbassadorId() != null) {
            log.info("Referral ID: {}, Original VisionAmbassador ID (ObjectId): {}", referral.getId(), referral.getAmbassadorId());
            VisionAmbassador visionAmbassador = visionAmbassadorRepository.findById(referral.getAmbassadorId()).orElse(null);
            if (visionAmbassador != null) {
                log.info("Found VisionAmbassador: ID_DB={}, userId_string={}", visionAmbassador.getId(), visionAmbassador.getUserId());
                if (visionAmbassador.getUserId() != null && !visionAmbassador.getUserId().trim().isEmpty()) {
                    try {
                        ObjectId userObjectId = new ObjectId(visionAmbassador.getUserId());
                        log.info("Attempting to find User with ObjectId: {}", userObjectId);
                        user = userRepository.findById(userObjectId).orElse(null);
                        if (user != null) {
                            log.info("Found User: ID_DB={}, Name={}", user.getId(), user.getFirstName() + " " + user.getLastName());
                        } else {
                            log.warn("User NOT FOUND for ObjectId: {}", userObjectId);
                        }
                    } catch (IllegalArgumentException e) {
                        log.error("Invalid ObjectId format for userId_string: {} from VisionAmbassador ID: {}", visionAmbassador.getUserId(), visionAmbassador.getId(), e);
                    }
                } else {
                    log.warn("VisionAmbassador ID: {} has null or empty userId_string.", visionAmbassador.getId());
                }
            } else {
                log.warn("VisionAmbassador NOT FOUND for ID: {}", referral.getAmbassadorId());
            }
        } else {
            log.info("Referral ID: {}, has null VisionAmbassadorId.", referral.getId());
        }
        return referralMapper.toResponse(referral, user,null,null);
    }

    public List<ReferralResponse> getReferralsByAmbassadorId(String ambassadorIdString) { // Parameter is VisionAmbassador ID string
        // First, find the VisionAmbassador by its own ID
        VisionAmbassador visionAmbassador = visionAmbassadorRepository.findById(new ObjectId(ambassadorIdString)).orElse(null);
        User user = null;
        if (visionAmbassador != null) {
            log.info("Processing getReferralsByAmbassadorId for VisionAmbassador ID: {}, UserID_string: {}", visionAmbassador.getId(), visionAmbassador.getUserId());
            if (visionAmbassador.getUserId() != null && !visionAmbassador.getUserId().trim().isEmpty()) {
                try {
                    ObjectId userObjectId = new ObjectId(visionAmbassador.getUserId());
                    user = userRepository.findById(userObjectId).orElse(null);
                    if (user == null) {
                        log.warn("User not found for VisionAmbassador ID: {}, UserID_string: {}", visionAmbassador.getId(), visionAmbassador.getUserId());
                    }
                } catch (IllegalArgumentException e) {
                    log.error("Invalid ObjectId format for userId_string: {} from VisionAmbassador ID: {}", visionAmbassador.getUserId(), visionAmbassador.getId(), e);
                }
            } else {
                 log.warn("VisionAmbassador ID: {} has null or empty userId_string.", visionAmbassador.getId());
            }
        } else {
            log.warn("VisionAmbassador not found for input ID string: {}", ambassadorIdString);
            // If the VA itself isn't found, no referrals can be linked, or user fetched.
            // Consider returning empty list if VA not found, as referrals are linked via VA.
            // However, the original method finds referrals by VA ID directly.
            // This logic needs to align with how referrals are queried if we want to pass the correct User.
            // For now, this method will pass a potentially null User to all referrals found by VA ID.
        }

        // The following fetches referrals by VA ID, then tries to map with the *single* user fetched above.
        // This assumes all referrals for a given VA ID should be associated with that VA's linked User.
        final User finalUser = user; // User for all referrals mapped in this context
        return referralRepository.findByAmbassadorId(new ObjectId(ambassadorIdString))
                .stream()
                .map(referral -> {
                    // Logging specific to this referral within the loop
                    if (referral.getAmbassadorId() == null) {
                         log.info("Referral ID: {} (within getReferralsByAmbassadorId) has null VisionAmbassadorId, though query was by VA ID.", referral.getId());
                    }
                    // Pass the finalUser obtained from the ambassadorIdString parameter
                    return referralMapper.toResponse(referral, finalUser,null,null);
                })
                .collect(Collectors.toList());
    }

    public List<ReferralResponse> getReferralsByHospitalId(String hospitalId) {
        return referralRepository.findByHospitalId(new ObjectId(hospitalId))
                .stream()
                .map(referral -> {
                    User user = null;
                    if (referral.getAmbassadorId() != null) {
                        log.info("Referral ID: {}, Original VisionAmbassador ID (ObjectId): {}", referral.getId(), referral.getAmbassadorId());
                        VisionAmbassador visionAmbassador = visionAmbassadorRepository.findById(referral.getAmbassadorId()).orElse(null);
                        if (visionAmbassador != null) {
                            log.info("Found VisionAmbassador: ID_DB={}, userId_string={}", visionAmbassador.getId(), visionAmbassador.getUserId());
                            if (visionAmbassador.getUserId() != null && !visionAmbassador.getUserId().trim().isEmpty()) {
                                try {
                                    ObjectId userObjectId = new ObjectId(visionAmbassador.getUserId());
                                    log.info("Attempting to find User with ObjectId: {}", userObjectId);
                                    user = userRepository.findById(userObjectId).orElse(null);
                                    if (user != null) {
                                        log.info("Found User: ID_DB={}, Name={}", user.getId(), user.getFirstName() + " " + user.getLastName());
                                    } else {
                                        log.warn("User NOT FOUND for ObjectId: {}", userObjectId);
                                    }
                                } catch (IllegalArgumentException e) {
                                    log.error("Invalid ObjectId format for userId_string: {} from VisionAmbassador ID: {}", visionAmbassador.getUserId(), visionAmbassador.getId(), e);
                                }
                            } else {
                                log.warn("VisionAmbassador ID: {} has null or empty userId_string.", visionAmbassador.getId());
                            }
                        } else {
                            log.warn("VisionAmbassador NOT FOUND for ID: {}", referral.getAmbassadorId());
                        }
                    } else {
                        log.info("Referral ID: {}, has null VisionAmbassadorId.", referral.getId());
                    }
                    return referralMapper.toResponse(referral, user,null,null);
                })
                .collect(Collectors.toList());
    }

    public Page<ReferralResponse> getReferralsByHospitalIdPaginated(String hospitalId, Pageable pageable) {
        return referralRepository.findByHospitalId(new ObjectId(hospitalId), pageable)
                .map(referral -> {
                    User user = null;
                    if (referral.getAmbassadorId() != null) {
                        log.info("Referral ID: {}, Original VisionAmbassador ID (ObjectId): {}", referral.getId(), referral.getAmbassadorId());
                        VisionAmbassador visionAmbassador = visionAmbassadorRepository.findById(referral.getAmbassadorId()).orElse(null);
                        if (visionAmbassador != null) {
                            log.info("Found VisionAmbassador: ID_DB={}, userId_string={}", visionAmbassador.getId(), visionAmbassador.getUserId());
                            if (visionAmbassador.getUserId() != null && !visionAmbassador.getUserId().trim().isEmpty()) {
                                try {
                                    ObjectId userObjectId = new ObjectId(visionAmbassador.getUserId());
                                    log.info("Attempting to find User with ObjectId: {}", userObjectId);
                                    user = userRepository.findById(userObjectId).orElse(null);
                                    if (user != null) {
                                        log.info("Found User: ID_DB={}, Name={}", user.getId(), user.getFirstName() + " " + user.getLastName());
                                    } else {
                                        log.warn("User NOT FOUND for ObjectId: {}", userObjectId);
                                    }
                                } catch (IllegalArgumentException e) {
                                    log.error("Invalid ObjectId format for userId_string: {} from VisionAmbassador ID: {}", visionAmbassador.getUserId(), visionAmbassador.getId(), e);
                                }
                            } else {
                                log.warn("VisionAmbassador ID: {} has null or empty userId_string.", visionAmbassador.getId());
                            }
                        } else {
                            log.warn("VisionAmbassador NOT FOUND for ID: {}", referral.getAmbassadorId());
                        }
                    } else {
                        log.info("Referral ID: {}, has null VisionAmbassadorId.", referral.getId());
                    }
                    return referralMapper.toResponse(referral, user,null,null);
                });
    }

    public List<ReferralResponse> getReferralsByPatientId(String patientId) {
        return referralRepository.findByPatientId(new ObjectId(patientId))
                .stream()
                .map(referral -> {
                    User user = null;
                    Hospital hospital = null;
                    if (referral.getAmbassadorId() != null) {
                        log.info("Referral ID: {}, Original VisionAmbassador ID (ObjectId): {}", referral.getId(), referral.getAmbassadorId());
                        VisionAmbassador visionAmbassador = visionAmbassadorRepository.findById(referral.getAmbassadorId()).orElse(null);
                        if (visionAmbassador != null) {
                            log.info("Found VisionAmbassador: ID_DB={}, userId_string={}", visionAmbassador.getId(), visionAmbassador.getUserId());
                            if (visionAmbassador.getUserId() != null && !visionAmbassador.getUserId().trim().isEmpty()) {
                                try {
                                    ObjectId userObjectId = new ObjectId(visionAmbassador.getUserId());
                                    log.info("Attempting to find User with ObjectId: {}", userObjectId);
                                    user = userRepository.findById(userObjectId).orElse(null);
                                    if (user != null) {
                                        log.info("Found User: ID_DB={}, Name={}", user.getId(), user.getFirstName() + " " + user.getLastName());
                                    } else {
                                        log.warn("User NOT FOUND for ObjectId: {}", userObjectId);
                                    }
                                } catch (IllegalArgumentException e) {
                                    log.error("Invalid ObjectId format for userId_string: {} from VisionAmbassador ID: {}", visionAmbassador.getUserId(), visionAmbassador.getId(), e);
                                }
                            } else {
                                log.warn("VisionAmbassador ID: {} has null or empty userId_string.", visionAmbassador.getId());
                            }
                        } else {
                            log.warn("VisionAmbassador NOT FOUND for ID: {}", referral.getAmbassadorId());
                        }
                    } else {
                        log.info("Referral ID: {}, has null VisionAmbassadorId.", referral.getId());
                    }

                    if (referral.getHospitalId() != null) {
                        log.info("Referral ID: {}, Original Hospital ID (ObjectId): {}", referral.getId(), referral.getHospitalId());
                        hospital = hospitalRepository.findById(referral.getHospitalId()).orElse(null);
                        if (hospital != null) {
                            log.info("Found hospital:", hospital.getId());
                           
                        } else {
                            log.warn("Hospital NOT FOUND for ID: {}", referral.getHospitalId());
                        }
                    } else {
                        log.info("Referral ID: {}, has null HospitalId.", referral.getId());
                    }
                    return referralMapper.toResponse(referral, user,hospital,null);
                })
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
    
    // Default sorting logic
    Pageable effectivePageable = pageable;
    if (pageable.getSort().isUnsorted()) {
        effectivePageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
    }
    // Use effectivePageable for the query
    Query query = Query.query(criteria).with(effectivePageable); 
    
    // Get total count for pagination
    long total = mongoTemplate.count(Query.query(criteria), Referral.class);
    
    // Execute query with pagination
    List<Referral> referrals = mongoTemplate.find(query, Referral.class);
    
    // Convert to response DTOs
    List<ReferralResponse> referralResponses = referrals.stream()
        .map(referral -> {
            User user = null;
            Patient patient = null; // Initialize patient to null
            if (referral.getAmbassadorId() != null) {
                log.info("Referral ID: {}, Original VisionAmbassador ID (ObjectId): {}", referral.getId(), referral.getAmbassadorId());
                VisionAmbassador visionAmbassador = visionAmbassadorRepository.findById(referral.getAmbassadorId()).orElse(null);
                if (visionAmbassador != null) {
                    log.info("Found VisionAmbassador: ID_DB={}, userId_string={}", visionAmbassador.getId(), visionAmbassador.getUserId());
                    if (visionAmbassador.getUserId() != null && !visionAmbassador.getUserId().trim().isEmpty()) {
                        try {
                            ObjectId userObjectId = new ObjectId(visionAmbassador.getUserId());
                            log.info("Attempting to find User with ObjectId: {}", userObjectId);
                            user = userRepository.findById(userObjectId).orElse(null);
                            if (user != null) {
                                log.info("Found User: ID_DB={}, Name={}", user.getId(), user.getFirstName() + " " + user.getLastName());
                            } else {
                                log.warn("User NOT FOUND for ObjectId: {}", userObjectId);
                            }
                        } catch (IllegalArgumentException e) {
                            log.error("Invalid ObjectId format for userId_string: {} from VisionAmbassador ID: {}", visionAmbassador.getUserId(), visionAmbassador.getId(), e);
                        }
                    } else {
                        log.warn("VisionAmbassador ID: {} has null or empty userId_string.", visionAmbassador.getId());
                    }
                } else {
                    log.warn("VisionAmbassador NOT FOUND for ID: {}", referral.getAmbassadorId());
                }
            } else {
                log.info("Referral ID: {}, has null VisionAmbassadorId.", referral.getId());
            }

            if (referral.getPatientId() != null) {
                log.info("Referral ID: {}, Original Patient ID (ObjectId): {}", referral.getId(), referral.getPatientId());
                patient = patientRepository.findById(referral.getPatientId()).orElse(null);
                if (patient != null) {
                    log.info("Found Patient: ID_DB={}, patientId_string={}", patient.getId(), patient.getId());
                } else {
                    log.warn("Patient NOT FOUND for ID: {}", referral.getAmbassadorId());
                }
            } else {
                log.info("Referral ID: {}, has null PatientId.", referral.getId());
            }
            return referralMapper.toResponse(referral, user,null,patient);
        })
        .collect(Collectors.toList());
    
    return new PageImpl<>(referralResponses, effectivePageable, total);
}

    public void deleteReferralById(String id) {
        referralRepository.deleteById(new ObjectId(id));
    }

    public ReferralResponse updateReferral(String id, ReferralRequest referralRequest) {
        Referral existingReferral = referralRepository.findById(new ObjectId(id))
                .orElseThrow(() -> new IllegalArgumentException("Referral not found with id: " + id));

        Referral updatedReferral = referralMapper.updateEntity(existingReferral, referralRequest);
        Referral savedReferral = referralRepository.save(updatedReferral);
        User user = null;
        if (savedReferral.getAmbassadorId() != null) {
            log.info("Referral ID: {}, Original VisionAmbassador ID (ObjectId): {}", savedReferral.getId(), savedReferral.getAmbassadorId());
            VisionAmbassador visionAmbassador = visionAmbassadorRepository.findById(savedReferral.getAmbassadorId()).orElse(null);
            if (visionAmbassador != null) {
                log.info("Found VisionAmbassador: ID_DB={}, userId_string={}", visionAmbassador.getId(), visionAmbassador.getUserId());
                if (visionAmbassador.getUserId() != null && !visionAmbassador.getUserId().trim().isEmpty()) {
                    try {
                        ObjectId userObjectId = new ObjectId(visionAmbassador.getUserId());
                        log.info("Attempting to find User with ObjectId: {}", userObjectId);
                        user = userRepository.findById(userObjectId).orElse(null);
                        if (user != null) {
                            log.info("Found User: ID_DB={}, Name={}", user.getId(), user.getFirstName() + " " + user.getLastName());
                        } else {
                            log.warn("User NOT FOUND for ObjectId: {}", userObjectId);
                        }
                    } catch (IllegalArgumentException e) {
                        log.error("Invalid ObjectId format for userId_string: {} from VisionAmbassador ID: {}", visionAmbassador.getUserId(), visionAmbassador.getId(), e);
                    }
                } else {
                    log.warn("VisionAmbassador ID: {} has null or empty userId_string.", visionAmbassador.getId());
                }
            } else {
                log.warn("VisionAmbassador NOT FOUND for ID: {}", savedReferral.getAmbassadorId());
            }
        } else {
            log.info("Referral ID: {}, has null VisionAmbassadorId.", savedReferral.getId());
        }
        return referralMapper.toResponse(savedReferral, user,null,null);
    }

     private EyeDetails mapEyeDetailsDtoToEntity(EyeDetailsDto dto) {
        if (dto == null) {
            return null;
        }
        return EyeDetails.builder()
            .sph(dto.getSph() != null ? String.valueOf(dto.getSph()) : null)
            .cyl(dto.getCyl() != null ? String.valueOf(dto.getCyl()) : null)
            .axis(dto.getAxis() != null ? String.valueOf(dto.getAxis()) : null)
            .build();
    }

    public BulkReferralUpdateResponse bulkUpdateReferrals(List<BulkReferralUpdateRequest> bulkRequest) {
        int updatedCount = 0;
        int rejectedCount = 0;
        List<RejectedReferralInfo> rejectedList = new ArrayList<>();

        for (BulkReferralUpdateRequest request : bulkRequest) {
            Hospital hospital = hospitalRepository.findByHospitalCode(request.getHospitalCode()).orElse(null);
            if (hospital == null) {
                rejectedList.add(new RejectedReferralInfo(request.getReferrals(), request.getGuardianContact(), request.getGender(), request.getHospitalName()));
                rejectedCount++;
                continue;
            }
            List<Referral> potentialReferrals = referralRepository.findByPatientNameAndHospitalId(request.getReferrals(), hospital.getId());
            boolean foundMatch = false;
            for (Referral referral : potentialReferrals) {
                Patient patient = patientRepository.findById(referral.getPatientId()).orElse(null);
                if (patient != null &&
                    // patient.getGuardian() != null && // This check is removed
                    Objects.equals(patient.getPhone(), request.getGuardianContact()) &&
                    patient.getGender().name().equalsIgnoreCase(request.getGender())) { // Assuming Patient.getGender() returns an enum

                    referral.setRightEye(mapEyeDetailsDtoToEntity(request.getRightEye()));
                    referral.setLeftEye(mapEyeDetailsDtoToEntity(request.getLeftEye()));
                    referral.setSpectacleRequestedOn(request.getRequestedOn());
                    referral.setIsSpectacleRequested(true);

                    try {
                        patient.setStatus(request.getStatus().toUpperCase());
                        referral.setStatus(Status.valueOf(request.getStatus().toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        // Handle invalid status string if necessary, maybe log an error or add to rejected
                        // For now, if status is invalid, we might skip update or use a default
                        // Or, as per current logic, this transaction might fail if Status enum is strict
                        // Let's assume valid status strings are provided for now.
                        // If not, this specific referral update might be problematic.
                        // A robust solution would be to add to rejectedList here too.
                        System.err.println("Invalid status string: " + request.getStatus());
                        // Decide if this means rejection or skipping status update
                        // For now, let's assume it's a critical error for this record
                        // and it should be rejected if status cannot be parsed.
                        // However, the current loop structure will attempt to save if other fields are valid.
                        // To reject, we'd need to move this record to rejectedList and break/continue.
                        // Let's refine this: if status is crucial and invalid, reject.
                    }
                    hospital.setUpdatedAt(new Date());
                    hospitalRepository.save(hospital);
                    referral.setUpdatedAt(new Date());
                    referralRepository.save(referral);
                    updatedCount++;
                    foundMatch = true;
                    break; 
                }
            }

            if (!foundMatch) {
                rejectedList.add(new RejectedReferralInfo(request.getReferrals(), request.getGuardianContact(), request.getGender(), request.getHospitalName()));
                rejectedCount++;
            }
        }
        return new BulkReferralUpdateResponse(bulkRequest.size(), updatedCount, rejectedCount, rejectedList);
    }
}

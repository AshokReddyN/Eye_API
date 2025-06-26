package com.nayonikaeyecare.api.services;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.nayonikaeyecare.api.entities.Gender;

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
import org.springframework.data.domain.Sort; // Added
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Added

import java.util.ArrayList;
import java.util.Arrays;
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
        if (patient.getReferralIds() == null) {
            patient.setReferralIds(new ArrayList<>());
        }

        // Add new referral ID to the list
        patient.getReferralIds().add(savedReferral.getId().toString());
        patient.setStatus(Status.REFERRED.name()); // Set patient status to REFERRED
        log.info("Patient ID: {}, Referral ID added to patient: {}", patient.getId(), savedReferral.getId());
        patient.setHospitalName(savedReferral.getHospitalName());
        // Save updated patient
        patientRepository.save(patient);
        User user = null;
        if (savedReferral.getAmbassadorId() != null) {
            log.info("Referral ID: {}, Original VisionAmbassador ID (ObjectId): {}", savedReferral.getId(),
                    savedReferral.getAmbassadorId());
            VisionAmbassador visionAmbassador = visionAmbassadorRepository.findById(savedReferral.getAmbassadorId())
                    .orElse(null);
            if (visionAmbassador != null) {
                log.info("Found VisionAmbassador: ID_DB={}, userId_string={}", visionAmbassador.getId(),
                        visionAmbassador.getUserId());
                if (visionAmbassador.getUserId() != null && !visionAmbassador.getUserId().trim().isEmpty()) {
                    try {
                        ObjectId userObjectId = new ObjectId(visionAmbassador.getUserId());
                        log.info("Attempting to find User with ObjectId: {}", userObjectId);
                        user = userRepository.findById(userObjectId).orElse(null);
                        if (user != null) {
                            log.info("Found User: ID_DB={}, Name={}", user.getId(),
                                    user.getFirstName() + " " + user.getLastName());
                        } else {
                            log.warn("User NOT FOUND for ObjectId: {}", userObjectId);
                        }
                    } catch (IllegalArgumentException e) {
                        log.error("Invalid ObjectId format for userId_string: {} from VisionAmbassador ID: {}",
                                visionAmbassador.getUserId(), visionAmbassador.getId(), e);
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
        return referralMapper.toResponse(savedReferral, user, null, null);
    }

    public List<ReferralResponse> getAllReferrals() {
        return referralRepository.findAll()
                .stream()
                .map(referral -> {
                    User user = null;
                    if (referral.getAmbassadorId() != null) {
                        log.info("Referral ID: {}, Original VisionAmbassador ID (ObjectId): {}", referral.getId(),
                                referral.getAmbassadorId());
                        VisionAmbassador visionAmbassador = visionAmbassadorRepository
                                .findById(referral.getAmbassadorId()).orElse(null);
                        if (visionAmbassador != null) {
                            log.info("Found VisionAmbassador: ID_DB={}, userId_string={}", visionAmbassador.getId(),
                                    visionAmbassador.getUserId());
                            if (visionAmbassador.getUserId() != null
                                    && !visionAmbassador.getUserId().trim().isEmpty()) {
                                try {
                                    ObjectId userObjectId = new ObjectId(visionAmbassador.getUserId());
                                    log.info("Attempting to find User with ObjectId: {}", userObjectId);
                                    user = userRepository.findById(userObjectId).orElse(null);
                                    if (user != null) {
                                        log.info("Found User: ID_DB={}, Name={}", user.getId(),
                                                user.getFirstName() + " " + user.getLastName());
                                    } else {
                                        log.warn("User NOT FOUND for ObjectId: {}", userObjectId);
                                    }
                                } catch (IllegalArgumentException e) {
                                    log.error(
                                            "Invalid ObjectId format for userId_string: {} from VisionAmbassador ID: {}",
                                            visionAmbassador.getUserId(), visionAmbassador.getId(), e);
                                }
                            } else {
                                log.warn("VisionAmbassador ID: {} has null or empty userId_string.",
                                        visionAmbassador.getId());
                            }
                        } else {
                            log.warn("VisionAmbassador NOT FOUND for ID: {}", referral.getAmbassadorId());
                        }
                    } else {
                        log.info("Referral ID: {}, has null VisionAmbassadorId.", referral.getId());
                    }
                    return referralMapper.toResponse(referral, user, null, null);
                })
                .collect(Collectors.toList());
    }

    public ReferralResponse getReferralById(String id) {
        Referral referral = referralRepository.findById(new ObjectId(id))
                .orElseThrow(() -> new IllegalArgumentException("Referral not found with id: " + id));
        User user = null;
        if (referral.getAmbassadorId() != null) {
            log.info("Referral ID: {}, Original VisionAmbassador ID (ObjectId): {}", referral.getId(),
                    referral.getAmbassadorId());
            VisionAmbassador visionAmbassador = visionAmbassadorRepository.findById(referral.getAmbassadorId())
                    .orElse(null);
            if (visionAmbassador != null) {
                log.info("Found VisionAmbassador: ID_DB={}, userId_string={}", visionAmbassador.getId(),
                        visionAmbassador.getUserId());
                if (visionAmbassador.getUserId() != null && !visionAmbassador.getUserId().trim().isEmpty()) {
                    try {
                        ObjectId userObjectId = new ObjectId(visionAmbassador.getUserId());
                        log.info("Attempting to find User with ObjectId: {}", userObjectId);
                        user = userRepository.findById(userObjectId).orElse(null);
                        if (user != null) {
                            log.info("Found User: ID_DB={}, Name={}", user.getId(),
                                    user.getFirstName() + " " + user.getLastName());
                        } else {
                            log.warn("User NOT FOUND for ObjectId: {}", userObjectId);
                        }
                    } catch (IllegalArgumentException e) {
                        log.error("Invalid ObjectId format for userId_string: {} from VisionAmbassador ID: {}",
                                visionAmbassador.getUserId(), visionAmbassador.getId(), e);
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
        return referralMapper.toResponse(referral, user, null, null);
    }

    public List<ReferralResponse> getReferralsByAmbassadorId(String ambassadorIdString) { // Parameter is
                                                                                          // VisionAmbassador ID string
        // First, find the VisionAmbassador by its own ID
        VisionAmbassador visionAmbassador = visionAmbassadorRepository.findById(new ObjectId(ambassadorIdString))
                .orElse(null);
        User user = null;
        if (visionAmbassador != null) {
            log.info("Processing getReferralsByAmbassadorId for VisionAmbassador ID: {}, UserID_string: {}",
                    visionAmbassador.getId(), visionAmbassador.getUserId());
            if (visionAmbassador.getUserId() != null && !visionAmbassador.getUserId().trim().isEmpty()) {
                try {
                    ObjectId userObjectId = new ObjectId(visionAmbassador.getUserId());
                    user = userRepository.findById(userObjectId).orElse(null);
                    if (user == null) {
                        log.warn("User not found for VisionAmbassador ID: {}, UserID_string: {}",
                                visionAmbassador.getId(), visionAmbassador.getUserId());
                    }
                } catch (IllegalArgumentException e) {
                    log.error("Invalid ObjectId format for userId_string: {} from VisionAmbassador ID: {}",
                            visionAmbassador.getUserId(), visionAmbassador.getId(), e);
                }
            } else {
                log.warn("VisionAmbassador ID: {} has null or empty userId_string.", visionAmbassador.getId());
            }
        } else {
            log.warn("VisionAmbassador not found for input ID string: {}", ambassadorIdString);
            // If the VA itself isn't found, no referrals can be linked, or user fetched.
            // Consider returning empty list if VA not found, as referrals are linked via
            // VA.
            // However, the original method finds referrals by VA ID directly.
            // This logic needs to align with how referrals are queried if we want to pass
            // the correct User.
            // For now, this method will pass a potentially null User to all referrals found
            // by VA ID.
        }

        // The following fetches referrals by VA ID, then tries to map with the *single*
        // user fetched above.
        // This assumes all referrals for a given VA ID should be associated with that
        // VA's linked User.
        final User finalUser = user; // User for all referrals mapped in this context
        Sort sort = Sort.by(Sort.Direction.DESC, "updatedAt");

        List<Referral> referrals = referralRepository.findByAmbassadorId(new ObjectId(ambassadorIdString), sort);

        return referrals.stream()
                .map(referral -> {
                    // Logging specific to this referral within the loop
                    if (referral.getAmbassadorId() == null) {
                        log.info(
                                "Referral ID: {} (within getReferralsByAmbassadorId) has null VisionAmbassadorId, though query was by VA ID.",
                                referral.getId());
                    }
                    // Pass the finalUser obtained from the ambassadorIdString parameter
                    return referralMapper.toResponse(referral, finalUser, null, null);
                })
                .collect(Collectors.toList());
    }

    public List<ReferralResponse> getReferralsByHospitalId(String hospitalId) {
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.DESC, "updatedAt"));
        return referralRepository.findByHospitalId(new ObjectId(hospitalId), pageable)
                .getContent() // Get the List<Referral> from Page<Referral>
                .stream()
                .map(referral -> {
                    User user = null;
                    if (referral.getAmbassadorId() != null) {
                        log.info("Referral ID: {}, Original VisionAmbassador ID (ObjectId): {}", referral.getId(),
                                referral.getAmbassadorId());
                        VisionAmbassador visionAmbassador = visionAmbassadorRepository
                                .findById(referral.getAmbassadorId()).orElse(null);
                        if (visionAmbassador != null) {
                            log.info("Found VisionAmbassador: ID_DB={}, userId_string={}", visionAmbassador.getId(),
                                    visionAmbassador.getUserId());
                            if (visionAmbassador.getUserId() != null
                                    && !visionAmbassador.getUserId().trim().isEmpty()) {
                                try {
                                    ObjectId userObjectId = new ObjectId(visionAmbassador.getUserId());
                                    log.info("Attempting to find User with ObjectId: {}", userObjectId);
                                    user = userRepository.findById(userObjectId).orElse(null);
                                    if (user != null) {
                                        log.info("Found User: ID_DB={}, Name={}", user.getId(),
                                                user.getFirstName() + " " + user.getLastName());
                                    } else {
                                        log.warn("User NOT FOUND for ObjectId: {}", userObjectId);
                                    }
                                } catch (IllegalArgumentException e) {
                                    log.error(
                                            "Invalid ObjectId format for userId_string: {} from VisionAmbassador ID: {}",
                                            visionAmbassador.getUserId(), visionAmbassador.getId(), e);
                                }
                            } else {
                                log.warn("VisionAmbassador ID: {} has null or empty userId_string.",
                                        visionAmbassador.getId());
                            }
                        } else {
                            log.warn("VisionAmbassador NOT FOUND for ID: {}", referral.getAmbassadorId());
                        }
                    } else {
                        log.info("Referral ID: {}, has null VisionAmbassadorId.", referral.getId());
                    }
                    return referralMapper.toResponse(referral, user, null, null);
                })
                .collect(Collectors.toList());
    }

    public Page<ReferralResponse> getReferralsByHospitalIdPaginated(String hospitalId, Pageable pageable) {
        return referralRepository.findByHospitalId(new ObjectId(hospitalId), pageable)
                .map(referral -> {
                    User user = null;
                    if (referral.getAmbassadorId() != null) {
                        log.info("Referral ID: {}, Original VisionAmbassador ID (ObjectId): {}", referral.getId(),
                                referral.getAmbassadorId());
                        VisionAmbassador visionAmbassador = visionAmbassadorRepository
                                .findById(referral.getAmbassadorId()).orElse(null);
                        if (visionAmbassador != null) {
                            log.info("Found VisionAmbassador: ID_DB={}, userId_string={}", visionAmbassador.getId(),
                                    visionAmbassador.getUserId());
                            if (visionAmbassador.getUserId() != null
                                    && !visionAmbassador.getUserId().trim().isEmpty()) {
                                try {
                                    ObjectId userObjectId = new ObjectId(visionAmbassador.getUserId());
                                    log.info("Attempting to find User with ObjectId: {}", userObjectId);
                                    user = userRepository.findById(userObjectId).orElse(null);
                                    if (user != null) {
                                        log.info("Found User: ID_DB={}, Name={}", user.getId(),
                                                user.getFirstName() + " " + user.getLastName());
                                    } else {
                                        log.warn("User NOT FOUND for ObjectId: {}", userObjectId);
                                    }
                                } catch (IllegalArgumentException e) {
                                    log.error(
                                            "Invalid ObjectId format for userId_string: {} from VisionAmbassador ID: {}",
                                            visionAmbassador.getUserId(), visionAmbassador.getId(), e);
                                }
                            } else {
                                log.warn("VisionAmbassador ID: {} has null or empty userId_string.",
                                        visionAmbassador.getId());
                            }
                        } else {
                            log.warn("VisionAmbassador NOT FOUND for ID: {}", referral.getAmbassadorId());
                        }
                    } else {
                        log.info("Referral ID: {}, has null VisionAmbassadorId.", referral.getId());
                    }
                    return referralMapper.toResponse(referral, user, null, null);
                });
    }

    public List<ReferralResponse> getReferralsByPatientId(String patientId) {
        return referralRepository.findByPatientId(new ObjectId(patientId))
                .stream()
                .map(referral -> {
                    User user = null;
                    Hospital hospital = null;
                    if (referral.getAmbassadorId() != null) {
                        log.info("Referral ID: {}, Original VisionAmbassador ID (ObjectId): {}", referral.getId(),
                                referral.getAmbassadorId());
                        VisionAmbassador visionAmbassador = visionAmbassadorRepository
                                .findById(referral.getAmbassadorId()).orElse(null);
                        if (visionAmbassador != null) {
                            log.info("Found VisionAmbassador: ID_DB={}, userId_string={}", visionAmbassador.getId(),
                                    visionAmbassador.getUserId());
                            if (visionAmbassador.getUserId() != null
                                    && !visionAmbassador.getUserId().trim().isEmpty()) {
                                try {
                                    ObjectId userObjectId = new ObjectId(visionAmbassador.getUserId());
                                    log.info("Attempting to find User with ObjectId: {}", userObjectId);
                                    user = userRepository.findById(userObjectId).orElse(null);
                                    if (user != null) {
                                        log.info("Found User: ID_DB={}, Name={}", user.getId(),
                                                user.getFirstName() + " " + user.getLastName());
                                    } else {
                                        log.warn("User NOT FOUND for ObjectId: {}", userObjectId);
                                    }
                                } catch (IllegalArgumentException e) {
                                    log.error(
                                            "Invalid ObjectId format for userId_string: {} from VisionAmbassador ID: {}",
                                            visionAmbassador.getUserId(), visionAmbassador.getId(), e);
                                }
                            } else {
                                log.warn("VisionAmbassador ID: {} has null or empty userId_string.",
                                        visionAmbassador.getId());
                            }
                        } else {
                            log.warn("VisionAmbassador NOT FOUND for ID: {}", referral.getAmbassadorId());
                        }
                    } else {
                        log.info("Referral ID: {}, has null VisionAmbassadorId.", referral.getId());
                    }

                    if (referral.getHospitalId() != null) {
                        log.info("Referral ID: {}, Original Hospital ID (ObjectId): {}", referral.getId(),
                                referral.getHospitalId());
                        hospital = hospitalRepository.findById(referral.getHospitalId()).orElse(null);
                        if (hospital != null) {
                            log.info("Found hospital:", hospital.getId());

                        } else {
                            log.warn("Hospital NOT FOUND for ID: {}", referral.getHospitalId());
                        }
                    } else {
                        log.info("Referral ID: {}, has null HospitalId.", referral.getId());
                    }
                    return referralMapper.toResponse(referral, user, hospital, null);
                })
                .collect(Collectors.toList());
    }

    public Page<ReferralResponse> filterReferrals(ObjectId ambassadorId, String state, String city,
            Boolean status, String name, String searchString, Pageable pageable) {

        // Delegate to repository implementation
        Page<Referral> referralPage = referralRepository.filterReferrals(
                ambassadorId, state, city, status, name, searchString, pageable);

        // Convert to response DTOs
        List<ReferralResponse> referralResponses = referralPage.getContent().stream()
                .map(referral -> {
                    User user = null;
                    Patient patient = null;

                    // Your existing mapping logic here...
                    if (referral.getAmbassadorId() != null) {
                        log.info("Referral ID: {}, Original VisionAmbassador ID (ObjectId): {}", referral.getId(),
                                referral.getAmbassadorId());
                        VisionAmbassador visionAmbassador = visionAmbassadorRepository
                                .findById(referral.getAmbassadorId()).orElse(null);
                        if (visionAmbassador != null) {
                            log.info("Found VisionAmbassador: ID_DB={}, userId_string={}", visionAmbassador.getId(),
                                    visionAmbassador.getUserId());
                            if (visionAmbassador.getUserId() != null
                                    && !visionAmbassador.getUserId().trim().isEmpty()) {
                                try {
                                    ObjectId userObjectId = new ObjectId(visionAmbassador.getUserId());
                                    log.info("Attempting to find User with ObjectId: {}", userObjectId);
                                    user = userRepository.findById(userObjectId).orElse(null);
                                    if (user != null) {
                                        log.info("Found User: ID_DB={}, Name={}", user.getId(),
                                                user.getFirstName() + " " + user.getLastName());
                                    } else {
                                        log.warn("User NOT FOUND for ObjectId: {}", userObjectId);
                                    }
                                } catch (IllegalArgumentException e) {
                                    log.error(
                                            "Invalid ObjectId format for userId_string: {} from VisionAmbassador ID: {}",
                                            visionAmbassador.getUserId(), visionAmbassador.getId(), e);
                                }
                            } else {
                                log.warn("VisionAmbassador ID: {} has null or empty userId_string.",
                                        visionAmbassador.getId());
                            }
                        } else {
                            log.warn("VisionAmbassador NOT FOUND for ID: {}", referral.getAmbassadorId());
                        }
                    } else {
                        log.info("Referral ID: {}, has null VisionAmbassadorId.", referral.getId());
                    }

                    if (referral.getPatientId() != null) {
                        log.info("Referral ID: {}, Original Patient ID (ObjectId): {}", referral.getId(),
                                referral.getPatientId());
                        patient = patientRepository.findById(referral.getPatientId()).orElse(null);
                        if (patient != null) {
                            log.info("Found Patient: ID_DB={}, patientId_string={}", patient.getId(), patient.getId());
                        } else {
                            log.warn("Patient NOT FOUND for ID: {}", referral.getPatientId());
                        }
                    } else {
                        log.info("Referral ID: {}, has null PatientId.", referral.getId());
                    }

                    return referralMapper.toResponse(referral, user, null, patient);
                })
                .collect(Collectors.toList());

        return new PageImpl<>(referralResponses, referralPage.getPageable(), referralPage.getTotalElements());
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
            log.info("Referral ID: {}, Original VisionAmbassador ID (ObjectId): {}", savedReferral.getId(),
                    savedReferral.getAmbassadorId());
            VisionAmbassador visionAmbassador = visionAmbassadorRepository.findById(savedReferral.getAmbassadorId())
                    .orElse(null);
            if (visionAmbassador != null) {
                log.info("Found VisionAmbassador: ID_DB={}, userId_string={}", visionAmbassador.getId(),
                        visionAmbassador.getUserId());
                if (visionAmbassador.getUserId() != null && !visionAmbassador.getUserId().trim().isEmpty()) {
                    try {
                        ObjectId userObjectId = new ObjectId(visionAmbassador.getUserId());
                        log.info("Attempting to find User with ObjectId: {}", userObjectId);
                        user = userRepository.findById(userObjectId).orElse(null);
                        if (user != null) {
                            log.info("Found User: ID_DB={}, Name={}", user.getId(),
                                    user.getFirstName() + " " + user.getLastName());
                        } else {
                            log.warn("User NOT FOUND for ObjectId: {}", userObjectId);
                        }
                    } catch (IllegalArgumentException e) {
                        log.error("Invalid ObjectId format for userId_string: {} from VisionAmbassador ID: {}",
                                visionAmbassador.getUserId(), visionAmbassador.getId(), e);
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
        return referralMapper.toResponse(savedReferral, user, null, null);
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
            Optional<Hospital> hospitalOpt = hospitalRepository.findByHospitalCode(request.getHospitalCode());
            if (!hospitalOpt.isPresent()) {
                log.warn("Hospital not found for code: {}. Rejecting referral for contact: {}",
                        request.getHospitalCode(), request.getGuardianContact());
                rejectedList.add(new RejectedReferralInfo(request.getReferrals(), request.getGuardianContact(),
                        request.getGender(), request.getHospitalName()));
                rejectedCount++;
                continue;
            }
            Hospital hospital = hospitalOpt.get();

            Gender patientGender;
            try {
                patientGender = Gender.valueOf(request.getGender().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid gender string: {}. Rejecting referral for contact: {}", request.getGender(),
                        request.getGuardianContact(), e);
                rejectedList.add(new RejectedReferralInfo(request.getReferrals(), request.getGuardianContact(),
                        request.getGender(), request.getHospitalName()));
                rejectedCount++;
                continue;
            }

            String ageString = request.getAge() != null ? String.valueOf(request.getAge()) : null;
            Optional<Patient> patientOpt = patientRepository.findByAgeAndPhoneAndGender(ageString,
                    request.getGuardianContact(), patientGender);
            if (!patientOpt.isPresent()) {
                log.warn("Patient not found for age: {}, phone: {}, gender: {}. Rejecting referral.", ageString,
                        request.getGuardianContact(), patientGender);
                rejectedList.add(new RejectedReferralInfo(request.getReferrals(), request.getGuardianContact(),
                        request.getGender(), request.getHospitalName()));
                rejectedCount++;
                continue;
            }
            Patient patient = patientOpt.get();

            Optional<Referral> referralOpt = referralRepository.findByPatientIdAndHospitalId(patient.getId(),
                    hospital.getId());
            if (!referralOpt.isPresent()) {
                log.warn("Referral not found for patient ID: {} and hospital ID: {}. Rejecting referral.",
                        patient.getId(), hospital.getId());
                rejectedList.add(new RejectedReferralInfo(request.getReferrals(), request.getGuardianContact(),
                        request.getGender(), request.getHospitalName()));
                rejectedCount++;
                continue;
            }
            Referral referral = referralOpt.get();

            // Status Handling Logic
            if (request.getStatus() == null || request.getStatus().trim().isEmpty()) {
                // Update other referral details
                referral.setRightEye(mapEyeDetailsDtoToEntity(request.getRightEye()));
                referral.setLeftEye(mapEyeDetailsDtoToEntity(request.getLeftEye()));
                referral.setSpectacleRequestedOn(request.getRequestedOn());
                referral.setIsSpectacleRequested(true); // Or based on presence of eye details/requestedOn

                patient.setUpdatedAt(new Date());
                patientRepository.save(patient);

                referral.setUpdatedAt(new Date());
                referralRepository.save(referral);

                // hospital.setUpdatedAt(new Date()); // Consider if this is always needed
                // hospitalRepository.save(hospital); // Consider if this is always needed
                updatedCount++;
            } else {
                String newStatusString = request.getStatus().toUpperCase();
                try {
                    Status newReferralStatus = Status.valueOf(newStatusString); // Validate status enum

                    patient.setStatus(newStatusString); // Patient status is String
                    referral.setStatus(newReferralStatus);

                    // Update other referral details
                    referral.setRightEye(mapEyeDetailsDtoToEntity(request.getRightEye()));
                    referral.setLeftEye(mapEyeDetailsDtoToEntity(request.getLeftEye()));
                    referral.setSpectacleRequestedOn(request.getRequestedOn());
                    referral.setIsSpectacleRequested(true); // Or based on presence of eye details/requestedOn

                    patient.setUpdatedAt(new Date());
                    patientRepository.save(patient);

                    referral.setUpdatedAt(new Date());
                    referralRepository.save(referral);

                    hospital.setUpdatedAt(new Date());
                    hospitalRepository.save(hospital);

                    updatedCount++;
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid status string: {} for patient ID: {}, hospital ID: {}. Rejecting update.",
                            request.getStatus(), patient.getId(), hospital.getId(), e);
                    rejectedList.add(new RejectedReferralInfo(request.getReferrals(), request.getGuardianContact(),
                            request.getGender(), request.getHospitalName()));
                    rejectedCount++;
                    continue; // Continue to the next request in the bulk list
                }
            }
        }
        return new BulkReferralUpdateResponse(bulkRequest.size(), updatedCount, rejectedCount, rejectedList);
    }
}

package com.nayonikaeyecare.api.services;

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.nayonikaeyecare.api.dto.patient.PatientRequest;
import com.nayonikaeyecare.api.dto.patient.PatientResponse;
import com.nayonikaeyecare.api.entities.Patient;
import com.nayonikaeyecare.api.entities.Referral;
import com.nayonikaeyecare.api.entities.Status;
import com.nayonikaeyecare.api.exceptions.ResourceMissingException;
import com.nayonikaeyecare.api.mappers.PatientMapper;
import com.nayonikaeyecare.api.repositories.patient.PatientRepository;
import com.nayonikaeyecare.api.repositories.referral.ReferralRepositoryImpl;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final ReferralRepositoryImpl referralRepository;
    private final HmacService hmacService; // Added

    public PatientResponse createPatient(PatientRequest request) {
        Patient patient = Patient.builder()
                .name(request.getName())
                .ambassadorId(request.getAmbassadorId())
                .gender(request.getGender())
                .age(request.getAge())
                .phone(request.getPhone())
                .hospitalName("")
                .status(Status.INPROGRESS.name())   
                .email(request.getEmail())
                .city(request.getCity())
                .state(request.getState())
                .createdAt(new Date())
                .updatedAt(new Date())
                // Add HMAC fields before building
                .ageSearchable(hmacService.generateHmac(request.getAge()))
                .phoneSearchable(hmacService.generateHmac(request.getPhone()))
                .build();

                if(isDuplicatePatient(request)) { // This duplicate check might need to use HMAC if phone is part of it
                    throw new ResourceMissingException("Patient already exists with the same phone, name and ambassadorId");
                }
        Patient savedPatient = patientRepository.save(patient);
        // No need to decrypt here for the response, as PII listener handles decryption on load.
        // The savedPatient object itself will have fields decrypted if accessed after save (due to listener behavior on load for findById).
        // However, mapping should ideally use the entity that was just saved and had its fields (including encrypted ones) set.
        // For safety and clarity, let's ensure the object returned from save() is used if it's complete,
        // or re-fetch if there's any doubt about the state of 'savedPatient' object vs what's in DB.
        // The current PII listener decrypts on load, so findById will return a decrypted object.
        Patient fullyLoadedPatient = patientRepository.findById(savedPatient.getId())
            .orElseThrow(() -> new ResourceMissingException("Patient not found after save and reload"));

    return PatientMapper.mapToPatientResponse(fullyLoadedPatient);
    }

    private boolean isDuplicatePatient(PatientRequest request) {
        // Use the searchable HMAC value for phone in duplicate checks
        String phoneSearchable = hmacService.generateHmac(request.getPhone());
        return patientRepository.findByNameAndAmbassadorIdAndPhoneSearchable(
                request.getName(),
                request.getAmbassadorId(),
                phoneSearchable).isPresent();
    }

    public PatientResponse getPatientById(String id) {
        ObjectId objectId = convertToObjectId(id);
        Patient patient = patientRepository.findById(objectId)

                .orElseThrow(() -> new ResourceMissingException("Patient not found with id: " + id));
        return PatientMapper.mapToPatientResponse(patient);
    }

    public List<PatientResponse> getPatientsByAmbassadorId(String ambassadorId) {
        // Implement the logic to fetch patients by ambassadorId
        // Example:
        return patientRepository.findByAmbassadorId(ambassadorId)
                .stream()
                .map(PatientMapper::mapToPatientResponse)
                .collect(Collectors.toList());
    }

    public List<PatientResponse> getAllPatients() {
        return patientRepository.findAll().stream()
                .map(PatientMapper::mapToPatientResponse)
                .collect(Collectors.toList());
    }

    public void deletePatient(String id) {
        ObjectId objectId = convertToObjectId(id);
        if (!patientRepository.existsById(objectId)) {
            throw new ResourceMissingException("Patient not found with id: " + id);
        }
        patientRepository.deleteById(objectId);
        patientRepository.deleteById(objectId);
    }

    // public void createReferalForPatient(String id, ReferralRequest request) {
    // ObjectId objectId = convertToObjectId(id);

    // // Verify patient exists
    // Patient patient = patientRepository.findById(objectId)
    // .orElseThrow(() -> new ResourceMissingException("Patient not found with id: "
    // + id));

    // // Create new referral
    // Referral referral = Referral.builder()
    // .patientId(objectId.toHexString())
    // .hospitalId(request.getHospital_id())
    // .services(request.getServices())
    // .createdAt(new Date())
    // .status(Status.PENDING)
    // .build();

    // // Save referral in the repository
    // Referral savedReferral = referralRepository.save(referral);

    // // Add referral ID to patient's referralIds list
    // List<String> existingReferralIds = patient.getReferralIds() != null
    // ? new ArrayList<>(patient.getReferralIds())
    // : new ArrayList<>();
    // existingReferralIds.add(savedReferral.getId());

    // patient.setReferralIds(existingReferralIds);
    // patient.setUpdatedAt(new Date());

    // // Save updated patient
    // patientRepository.save(patient);
    // }

    // public void createReferalForPatient(String id, ReferralRequest request) {
    // ObjectId objectId = convertToObjectId(id);

    // // Verify patient exists
    // Patient patient = patientRepository.findById(objectId)
    // .orElseThrow(() -> new ResourceMissingException("Patient not found with id: "
    // + id));

    // // Create new referral
    // Referral referral = Referral.builder()
    // .patientId(objectId.toHexString())
    // .hospitalId(request.getHospital_id())
    // .services(request.getServices())
    // .createdAt(new Date())
    // .status(Status.PENDING)
    // .build();

    // // Save referral in the repository
    // Referral savedReferral = referralRepository.save(referral);

    // // Add referral ID to patient's referralIds list
    // List<String> existingReferralIds = patient.getReferralIds() != null
    // ? new ArrayList<>(patient.getReferralIds())
    // : new ArrayList<>();
    // existingReferralIds.add(savedReferral.getId());

    // patient.setReferralIds(existingReferralIds);
    // patient.setUpdatedAt(new Date());

    // // Save updated patient
    // patientRepository.save(patient);
    // }

    public PatientResponse updatePatient(String id, PatientRequest request) {
        ObjectId objectId = convertToObjectId(id);

        Patient existingPatient = patientRepository.findById(objectId)
                .orElseThrow(() -> new ResourceMissingException("Patient not found with id: " + id));

        existingPatient.setName(request.getName());
        existingPatient.setAmbassadorId(request.getAmbassadorId());
        existingPatient.setGender(request.getGender());
        existingPatient.setAge(request.getAge());
        existingPatient.setAgeSearchable(hmacService.generateHmac(request.getAge()));
        existingPatient.setPhone(request.getPhone());
        existingPatient.setPhoneSearchable(hmacService.generateHmac(request.getPhone()));
        existingPatient.setEmail(request.getEmail());
        existingPatient.setHospitalName(request.getHospitalName());
        existingPatient.setStatus(request.getStatus());
        existingPatient.setReferralIds(request.getReferralIds());
        existingPatient.setCity(request.getCity());
        existingPatient.setState(request.getState());
        existingPatient.setUpdatedAt(new Date());

        Patient savedPatient = patientRepository.save(existingPatient);
        return PatientMapper.mapToPatientResponse(savedPatient);
    }

    public List<Referral> getPatientReports(String patientId, String hospitalId) {
        ObjectId objectId = convertToObjectId(patientId);

        return hospitalId != null
                ? referralRepository.findByPatientIdAndHospitalId(objectId.toHexString(), hospitalId)
                : referralRepository.findByPatientId(objectId.toHexString());

    }

    public void updateStatusForPatients(List<String> ids, Status newStatus) {
        // List<ObjectId> objectIds = ids.stream()
        //         .map(this::convertToObjectId)
        //         .collect(Collectors.toList());

        referralRepository.updateStatusByIds(ids, newStatus);
    }

    public Page<PatientResponse> filterPatients(String state, String city, String name, String ambassadorId,
            org.springframework.data.domain.Pageable pageable) {
        return patientRepository.filterPatients(state, city, name, ambassadorId, pageable);
    }

    private ObjectId convertToObjectId(String id) {
        try {
            return new ObjectId(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid ObjectId format: " + id);
        }
    }

    @Transactional
    public String migrateExistingPatientsToUseSearchableFields() {
        long totalPatients = patientRepository.count();
        long updatedCount = 0;
        long skippedCount = 0;
        int batchSize = 100; // Process in batches
        int page = 0;

        // Assuming HmacService is already injected and PatientRepository is available
        // Also assuming MongoPiiEncryptionListener will decrypt age and phone upon loading

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, batchSize);
        org.springframework.data.domain.Page<Patient> patientBatch;

        do {
            patientBatch = patientRepository.findAll(pageable);
            if (patientBatch.hasContent()) {
                List<Patient> patientsToUpdate = new java.util.ArrayList<>();
                for (Patient patient : patientBatch.getContent()) {
                    boolean needsUpdate = false;
                    // Patient.age and Patient.phone will be decrypted here by MongoPiiEncryptionListener
                    String currentAge = patient.getAge();
                    String currentPhone = patient.getPhone();

                    String newAgeSearchable = hmacService.generateHmac(currentAge);
                    String newPhoneSearchable = hmacService.generateHmac(currentPhone);

                    if (!java.util.Objects.equals(patient.getAgeSearchable(), newAgeSearchable)) {
                        patient.setAgeSearchable(newAgeSearchable);
                        needsUpdate = true;
                    }
                    if (!java.util.Objects.equals(patient.getPhoneSearchable(), newPhoneSearchable)) {
                        patient.setPhoneSearchable(newPhoneSearchable);
                        needsUpdate = true;
                    }

                    if (needsUpdate) {
                        patientsToUpdate.add(patient);
                    } else {
                        skippedCount++;
                    }
                }
                if (!patientsToUpdate.isEmpty()) {
                    patientRepository.saveAll(patientsToUpdate);
                    updatedCount += patientsToUpdate.size();
                }
            }
            page++;
            pageable = org.springframework.data.domain.PageRequest.of(page, batchSize);
        } while (patientBatch.hasNext());

        String summary = String.format("Patient migration completed. Total patients processed: %d. Updated: %d. Skipped (already up-to-date or no PII): %d.",
                                       totalPatients, updatedCount, skippedCount);
        // Log this summary
        // Consider using Slf4j logger if available in the class
        System.out.println(summary);
        return summary;
    }
}
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
                .build();

                if(isDuplicatePatient(request)) {
                    throw new ResourceMissingException("Patient already exists with the same phone, name and ambassadorId");
                }
        Patient savedPatient = patientRepository.save(patient);
        Patient decryptedPatient = patientRepository.findById(savedPatient.getId())
            .orElseThrow(() -> new ResourceMissingException("Patient not found after save"));

    return PatientMapper.mapToPatientResponse(decryptedPatient);
    }

    private boolean isDuplicatePatient(PatientRequest request) {
        return patientRepository.findByPhoneAndNameAndAmbassadorId(
                request.getPhone(),
                request.getName(),
                request.getAmbassadorId()).isPresent();
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
        existingPatient.setPhone(request.getPhone());
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
}
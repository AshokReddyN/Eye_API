package com.nayonikaeyecare.api.mappers;

import com.nayonikaeyecare.api.dto.referral.ReferralRequest;
import com.nayonikaeyecare.api.dto.referral.ReferralResponse;
import com.nayonikaeyecare.api.entities.EyeDetails;
import com.nayonikaeyecare.api.entities.Referral;
import com.nayonikaeyecare.api.entities.ServiceType;
import com.nayonikaeyecare.api.entities.Status;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class ReferralMapperTest {

    private ReferralMapper referralMapper;

    @BeforeEach
    void setUp() {
        referralMapper = new ReferralMapper();
    }

    @Test
    void testToEntity() {
        ReferralRequest request = new ReferralRequest(
                Status.NEW,
                new ObjectId().toString(),
                "John Doe",
                "30-40",
                "Test Hospital",
                "Test City",
                "Test State",
                new ObjectId().toString(),
                new ObjectId().toString(),
                Collections.singletonList(ServiceType.CONSULTATION),
                "Test Treatment",
                new EyeDetails(),
                new EyeDetails(),
                new Date(),
                new Date(),
                true,
                LocalDateTime.now(),
                "HOS001"
        );

        Referral entity = referralMapper.toEntity(request);

        assertNotNull(entity);
        assertEquals(request.status(), entity.getStatus());
        assertEquals(request.patientId(), entity.getPatientId().toString());
        assertEquals(request.patientName(), entity.getPatientName());
        assertEquals(request.ageRange(), entity.getAgeRange());
        assertEquals(request.hospitalName(), entity.getHospitalName());
        assertEquals(request.city(), entity.getCity());
        assertEquals(request.state(), entity.getState());
        assertEquals(request.hospitalId(), entity.getHospitalId().toString());
        assertEquals(request.ambassadorId(), entity.getAmbassadorId().toString());
        assertEquals(request.services(), entity.getServices());
        assertEquals(request.treatment(), entity.getTreatment());
        assertEquals(request.rightEye(), entity.getRightEye());
        assertEquals(request.leftEye(), entity.getLeftEye());
        assertEquals(request.createdAt(), entity.getCreatedAt());
        assertEquals(request.updatedAt(), entity.getUpdatedAt());
        assertEquals(request.isSpectacleRequested(), entity.getIsSpectacleRequested());
        assertEquals(request.spectacleRequestedOn(), entity.getSpectacleRequestedOn());
        assertEquals(request.hospitalCode(), entity.getHospitalCode());
    }

    @Test
    void testToResponse() {
        Referral entity = Referral.builder()
                .id(new ObjectId())
                .status(Status.COMPLETED)
                .patientId(new ObjectId())
                .patientName("Jane Doe")
                .ageRange("20-30")
                .hospitalName("Another Hospital")
                .city("Another City")
                .state("Another State")
                .hospitalId(new ObjectId())
                .ambassadorId(new ObjectId())
                .services(Collections.singletonList(ServiceType.SURGERY))
                .treatment("Another Treatment")
                .rightEye(new EyeDetails())
                .leftEye(new EyeDetails())
                .createdAt(new Date())
                .updatedAt(new Date())
                .isSpectacleRequested(false)
                .spectacleRequestedOn(LocalDateTime.now().minusDays(1))
                .hospitalCode("HOS002")
                .build();

        ReferralResponse response = referralMapper.toResponse(entity);

        assertNotNull(response);
        assertEquals(entity.getId().toString(), response.id());
        assertEquals(entity.getStatus(), response.status());
        assertEquals(entity.getPatientId().toString(), response.patientId());
        assertEquals(entity.getPatientName(), response.patientName());
        assertEquals(entity.getAgeRange(), response.ageRange());
        assertEquals(entity.getHospitalName(), response.hospitalName());
        assertEquals(entity.getCity(), response.city());
        assertEquals(entity.getState(), response.state());
        assertEquals(entity.getHospitalId().toString(), response.hospitalId());
        assertEquals(entity.getAmbassadorId().toString(), response.ambassadorId());
        assertEquals(entity.getServices(), response.services());
        assertEquals(entity.getTreatment(), response.treatment());
        assertEquals(entity.getRightEye(), response.rightEye());
        assertEquals(entity.getLeftEye(), response.leftEye());
        assertEquals(entity.getCreatedAt(), response.createdAt());
        assertEquals(entity.getUpdatedAt(), response.updatedAt());
        assertEquals(entity.getIsSpectacleRequested(), response.isSpectacleRequested());
        assertEquals(entity.getSpectacleRequestedOn(), response.spectacleRequestedOn());
        assertEquals(entity.getHospitalCode(), response.hospitalCode());
    }

    @Test
    void testUpdateEntity() {
        Referral existingReferral = Referral.builder()
                .id(new ObjectId())
                .status(Status.NEW)
                .patientId(new ObjectId())
                .patientName("Old Name")
                .ageRange("50-60")
                .hospitalName("Old Hospital")
                .city("Old City")
                .state("Old State")
                .hospitalId(new ObjectId())
                .ambassadorId(new ObjectId())
                .services(Collections.singletonList(ServiceType.CHECKUP))
                .treatment("Old Treatment")
                .rightEye(new EyeDetails())
                .leftEye(new EyeDetails())
                .createdAt(new Date(System.currentTimeMillis() - 100000))
                .updatedAt(new Date(System.currentTimeMillis() - 100000))
                .isSpectacleRequested(false)
                .spectacleRequestedOn(null)
                .hospitalCode("HOS003")
                .build();

        ReferralRequest request = new ReferralRequest(
                Status.IN_PROGRESS,
                existingReferral.getPatientId().toString(), // Keep patientId same for update
                "New Name",
                "60-70",
                "New Hospital",
                "New City",
                "New State",
                existingReferral.getHospitalId().toString(), // Keep hospitalId same for update
                existingReferral.getAmbassadorId().toString(), // Keep ambassadorId same for update
                Collections.singletonList(ServiceType.OTHER),
                "New Treatment",
                new EyeDetails(), // Assuming EyeDetails might change or have more specific data
                new EyeDetails(),
                existingReferral.getCreatedAt(), // createdAt should not change
                new Date(), // updatedAt should change
                true,
                LocalDateTime.now(),
                "HOS004"
        );

        Referral updatedEntity = referralMapper.updateEntity(existingReferral, request);

        assertNotNull(updatedEntity);
        assertEquals(request.status(), updatedEntity.getStatus());
        assertEquals(request.patientId(), updatedEntity.getPatientId().toString());
        assertEquals(request.patientName(), updatedEntity.getPatientName());
        assertEquals(request.ageRange(), updatedEntity.getAgeRange());
        assertEquals(request.hospitalName(), updatedEntity.getHospitalName());
        assertEquals(request.city(), updatedEntity.getCity());
        assertEquals(request.hospitalId(), updatedEntity.getHospitalId().toString());
        assertEquals(request.ambassadorId(), updatedEntity.getAmbassadorId().toString());
        assertEquals(request.services(), updatedEntity.getServices());
        assertEquals(request.treatment(), updatedEntity.getTreatment());
        assertEquals(request.rightEye(), updatedEntity.getRightEye());
        assertEquals(request.leftEye(), updatedEntity.getLeftEye());
        assertEquals(existingReferral.getCreatedAt(), updatedEntity.getCreatedAt()); // Ensure createdAt is not changed
        assertNotEquals(existingReferral.getUpdatedAt(), updatedEntity.getUpdatedAt()); // Ensure updatedAt is changed
        assertTrue(updatedEntity.getUpdatedAt().after(existingReferral.getUpdatedAt()));
        assertEquals(request.isSpectacleRequested(), updatedEntity.getIsSpectacleRequested());
        assertEquals(request.spectacleRequestedOn(), updatedEntity.getSpectacleRequestedOn());
        assertEquals(request.hospitalCode(), updatedEntity.getHospitalCode());
    }
}

package com.nayonikaeyecare.api.services;

import com.nayonikaeyecare.api.dto.visionambassador.VisionAmbassadorResponse;
import com.nayonikaeyecare.api.entities.Referral;
import com.nayonikaeyecare.api.entities.VisionAmbassador;
import com.nayonikaeyecare.api.repositories.referral.ReferralRepository;
import com.nayonikaeyecare.api.repositories.visionambassador.VisionAmbassadorRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class VisionAmbassadorServiceTest {

    @Mock
    private VisionAmbassadorRepository visionAmbassadorRepository;

    @Mock
    private ReferralRepository referralRepository;

    @Mock
    private UserService userService; // Mocked as it's a dependency, though not directly used in getAllVisionAmbassadors

    @InjectMocks
    private VisionAmbassadorService visionAmbassadorService;

    private VisionAmbassador ambassador1;
    private VisionAmbassador ambassador2;
    private ObjectId ambassador1Id;
    private ObjectId ambassador2Id;

    @BeforeEach
    void setUp() {
        ambassador1Id = new ObjectId();
        ambassador2Id = new ObjectId();

        ambassador1 = VisionAmbassador.builder()
                .id(ambassador1Id)
                .name("Ambassador One")
                .phoneNumber("1234567890")
                .city("City A")
                .state("State A")
                .status(true)
                .language("English")
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        ambassador2 = VisionAmbassador.builder()
                .id(ambassador2Id)
                .name("Ambassador Two")
                .phoneNumber("0987654321")
                .city("City B")
                .state("State B")
                .status(true)
                .language("Spanish")
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
    }

    @Test
    void testGetAllVisionAmbassadors_Scenario1_AmbassadorWithMultipleReferrals() {
        // Arrange
        Referral referral1 = Referral.builder().id(new ObjectId()).ambassadorId(ambassador1Id).build();
        Referral referral2 = Referral.builder().id(new ObjectId()).ambassadorId(ambassador1Id).build();
        List<Referral> ambassador1Referrals = Arrays.asList(referral1, referral2);

        when(visionAmbassadorRepository.findAll()).thenReturn(Collections.singletonList(ambassador1));
        when(referralRepository.findByAmbassadorId(ambassador1Id)).thenReturn(ambassador1Referrals);

        // Act
        List<VisionAmbassadorResponse> responses = visionAmbassadorService.getAllVisionAmbassadors();

        // Assert
        assertEquals(1, responses.size());
        VisionAmbassadorResponse response1 = responses.get(0);
        assertEquals(ambassador1Id.toHexString(), response1.getId());
        assertEquals("Ambassador One", response1.getName());
        assertEquals(2, response1.getPatientCount());
    }

    @Test
    void testGetAllVisionAmbassadors_Scenario2_AmbassadorWithZeroReferrals() {
        // Arrange
        when(visionAmbassadorRepository.findAll()).thenReturn(Collections.singletonList(ambassador1));
        when(referralRepository.findByAmbassadorId(ambassador1Id)).thenReturn(Collections.emptyList());

        // Act
        List<VisionAmbassadorResponse> responses = visionAmbassadorService.getAllVisionAmbassadors();

        // Assert
        assertEquals(1, responses.size());
        VisionAmbassadorResponse response1 = responses.get(0);
        assertEquals(ambassador1Id.toHexString(), response1.getId());
        assertEquals("Ambassador One", response1.getName());
        assertEquals(0, response1.getPatientCount());
    }

    @Test
    void testGetAllVisionAmbassadors_Scenario3_MultipleAmbassadorsWithVaryingReferralCounts() {
        // Arrange
        Referral referral1Amb1 = Referral.builder().id(new ObjectId()).ambassadorId(ambassador1Id).build();
        List<Referral> ambassador1Referrals = Collections.singletonList(referral1Amb1);

        Referral referral1Amb2 = Referral.builder().id(new ObjectId()).ambassadorId(ambassador2Id).build();
        Referral referral2Amb2 = Referral.builder().id(new ObjectId()).ambassadorId(ambassador2Id).build();
        Referral referral3Amb2 = Referral.builder().id(new ObjectId()).ambassadorId(ambassador2Id).build();
        List<Referral> ambassador2Referrals = Arrays.asList(referral1Amb2, referral2Amb2, referral3Amb2);


        when(visionAmbassadorRepository.findAll()).thenReturn(Arrays.asList(ambassador1, ambassador2));
        when(referralRepository.findByAmbassadorId(ambassador1Id)).thenReturn(ambassador1Referrals);
        when(referralRepository.findByAmbassadorId(ambassador2Id)).thenReturn(ambassador2Referrals);

        // Act
        List<VisionAmbassadorResponse> responses = visionAmbassadorService.getAllVisionAmbassadors();

        // Assert
        assertEquals(2, responses.size());

        VisionAmbassadorResponse response1 = responses.stream()
                .filter(r -> r.getId().equals(ambassador1Id.toHexString()))
                .findFirst()
                .orElse(null);
        
        VisionAmbassadorResponse response2 = responses.stream()
                .filter(r -> r.getId().equals(ambassador2Id.toHexString()))
                .findFirst()
                .orElse(null);

        assert response1 != null;
        assertEquals("Ambassador One", response1.getName());
        assertEquals(1, response1.getPatientCount());

        assert response2 != null;
        assertEquals("Ambassador Two", response2.getName());
        assertEquals(3, response2.getPatientCount());
    }

    @Test
    void testGetAllVisionAmbassadors_Scenario4_NoAmbassadorsFound() {
        // Arrange
        when(visionAmbassadorRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<VisionAmbassadorResponse> responses = visionAmbassadorService.getAllVisionAmbassadors();

        // Assert
        assertTrue(responses.isEmpty());
    }
}

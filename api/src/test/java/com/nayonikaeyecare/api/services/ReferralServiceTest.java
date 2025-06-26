package com.nayonikaeyecare.api.services;
 
import com.nayonikaeyecare.api.dto.referral.ReferralResponse;
import com.nayonikaeyecare.api.entities.Referral;
import com.nayonikaeyecare.api.entities.VisionAmbassador;
import com.nayonikaeyecare.api.mappers.ReferralMapper;
import com.nayonikaeyecare.api.repositories.hospital.HospitalRepository;
import com.nayonikaeyecare.api.repositories.patient.PatientRepository;
import com.nayonikaeyecare.api.repositories.referral.ReferralRepository;
import com.nayonikaeyecare.api.repositories.visionambassador.VisionAmbassadorRepository;
import com.nayonikaeyecare.api.repositories.user.UserRepository;
import com.nayonikaeyecare.api.entities.user.User;
import org.bson.Document; // Added
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
 
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; // Added
 
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
 
import com.nayonikaeyecare.api.dto.referral.BulkReferralUpdateRequest;
import com.nayonikaeyecare.api.dto.referral.BulkReferralUpdateResponse;
import com.nayonikaeyecare.api.dto.referral.ReferralRequest;
import com.nayonikaeyecare.api.entities.EyeDetails;
import com.nayonikaeyecare.api.entities.Gender;
import com.nayonikaeyecare.api.entities.Hospital;
import com.nayonikaeyecare.api.entities.Patient;
import com.nayonikaeyecare.api.entities.Status;
import java.util.ArrayList;
 
 
@ExtendWith(MockitoExtension.class)
public class ReferralServiceTest {
 
    @Mock
    private ReferralRepository referralRepository; // Not directly used by filterReferrals, but good to have for other tests
    @Mock
    private PatientRepository patientRepository; // Not directly used by filterReferrals
    @Mock
    private HospitalRepository hospitalRepository; // Not directly used by filterReferrals
    @Mock
    private VisionAmbassadorRepository visionAmbassadorRepository;
    @Mock
    private UserRepository userRepository; // Added
    @Mock
    private MongoTemplate mongoTemplate;
 
    // Use @Spy for the mapper if we want to test its actual mapping logic
    // For now, @Mock is fine if we define what toResponse returns.
    // However, since the service calls toResponse(referral, ambassador),
    // and we want to verify the correct ambassador is passed,
    // mocking the mapper is simpler.
    @Mock
    private ReferralMapper referralMapper;
 
    @InjectMocks
    private ReferralService referralService;
 
    @Captor
    private ArgumentCaptor<Query> queryCaptor;
    @Captor
    private ArgumentCaptor<User> userCaptor; // For mapper
    @Captor
    private ArgumentCaptor<Patient> patientCaptor;
    @Captor
    private ArgumentCaptor<Referral> referralCaptor;
 
 
    private Referral referral1, referral2, referralWithNullAmbassadorId, referralToVAWithInvalidUserId, referralToVAWithNoUserLink, referralToVANotFound;
    private VisionAmbassador ambassador1WithUser, ambassador2WithUser, ambassadorWithInvalidUserId, ambassadorWithNoUserLink;
    private User user1, user2;
    private ObjectId vaId1, vaId2, vaIdWithInvalidUser, vaIdWithNoUserLink, vaIdNotFound; // VisionAmbassador ObjectIds
    private String user1IdString, user2IdString, userNonExistentIdString;
    
    // Test data for bulkUpdateReferrals
    private Hospital testHospital;
    private Patient testPatient;
    private Referral testReferral;
    private ObjectId testHospitalId;
    private ObjectId testPatientId;
    private ObjectId testReferralId;
 
 
    @BeforeEach
    void setUp() {
        // Existing setup
        user1IdString = new ObjectId().toHexString();
        user2IdString = new ObjectId().toHexString();
        userNonExistentIdString = new ObjectId().toHexString(); // For VA that links to a non-existent user
 
        user1 = User.builder().id(new ObjectId(user1IdString)).firstName("User").lastName("One").phoneNumber("111222").email("user1@example.com").build();
        user2 = User.builder().id(new ObjectId(user2IdString)).firstName("User").lastName("Two").phoneNumber("333444").email("user2@example.com").build();
 
        vaId1 = new ObjectId(); // VisionAmbassador's own ID
        vaId2 = new ObjectId();
        vaIdWithInvalidUser = new ObjectId();
        vaIdWithNoUserLink = new ObjectId();
        vaIdNotFound = new ObjectId();
 
 
        ambassador1WithUser = VisionAmbassador.builder().id(vaId1).name("VA One").userId(user1IdString).build();
        ambassador2WithUser = VisionAmbassador.builder().id(vaId2).name("VA Two").userId(user2IdString).build();
        ambassadorWithInvalidUserId = VisionAmbassador.builder().id(vaIdWithInvalidUser).name("VA Invalid").userId("invalid-object-id-string").build();
        ambassadorWithNoUserLink = VisionAmbassador.builder().id(vaIdWithNoUserLink).name("VA No User Link").userId(userNonExistentIdString).build();
 
        referral1 = Referral.builder().id(new ObjectId()).patientName("Patient A").ambassadorId(vaId1).createdAt(new Date()).build(); // Links to ambassador1WithUser
        referral2 = Referral.builder().id(new ObjectId()).patientName("Patient B").ambassadorId(vaId2).createdAt(new Date()).build(); // Links to ambassador2WithUser
        referralWithNullAmbassadorId = Referral.builder().id(new ObjectId()).patientName("Patient C").ambassadorId(null).createdAt(new Date()).build();
        referralToVAWithInvalidUserId = Referral.builder().id(new ObjectId()).patientName("Patient D").ambassadorId(vaIdWithInvalidUser).createdAt(new Date()).build();
        referralToVAWithNoUserLink = Referral.builder().id(new ObjectId()).patientName("Patient E").ambassadorId(vaIdWithNoUserLink).createdAt(new Date()).build();
        referralToVANotFound = Referral.builder().id(new ObjectId()).patientName("Patient F").ambassadorId(vaIdNotFound).createdAt(new Date()).build();
 
        // Setup for bulkUpdateReferrals tests
        testHospitalId = new ObjectId();
        testPatientId = new ObjectId();
        testReferralId = new ObjectId();
 
        testHospital = Hospital.builder().id(testHospitalId).hospitalCode("HOS001").name("Test Hospital").build();
        testPatient = Patient.builder().id(testPatientId).name("Test Patient Bulk").age("30").phone("1234567890").gender(Gender.MALE).status("REFERRED").build();
        testReferral = Referral.builder().id(testReferralId).patientId(testPatientId).hospitalId(testHospitalId).status(Status.REFERRED).build();
    }
 
    private Patient createTestPatient(String id, List<String> referralIds) {
        Patient patient = Patient.builder()
                .id(new ObjectId(id))
                .name("Test Patient")
                .referralIds(referralIds)
                // Set other necessary fields if any for patient
                .build();
        // patient.setStatus("SOME_INITIAL_STATUS"); // If needed for tests not related to createReferral's new logic
        return patient;
    }
 
    private ReferralRequest createTestReferralRequest(String patientId) {
        return ReferralRequest.builder()
                .patientId(patientId)
                .hospitalName("Test Hospital") // This should not be used to set patient.hospitalName
                .status(Status.REFERRED) // This should not be used to set patient.status
                // Set other necessary fields for ReferralRequest
                .build();
    }
 
    @Test
    void createReferral_whenPatientHasExistingReferredReferral_shouldThrowException() {
        // Arrange
        String patientId = new ObjectId().toHexString();
        String existingReferralId = new ObjectId().toHexString();
        Patient patient = createTestPatient(patientId, new ArrayList<>(Arrays.asList(existingReferralId)));
        ReferralRequest request = createTestReferralRequest(patientId);
        Referral existingReferral = Referral.builder().id(new ObjectId(existingReferralId)).status(Status.REFERRED).build();
        Referral newReferral = Referral.builder().id(new ObjectId()).build(); // For mocking
 
        when(patientRepository.findById(any(ObjectId.class))).thenReturn(Optional.of(patient));
        when(referralRepository.findAllByIdIn(patient.getReferralIds().stream().map(ObjectId::new).collect(Collectors.toList()))).thenReturn(Arrays.asList(existingReferral));
        // Add mocks to prevent NPEs if the IllegalArgumentException is not thrown early enough
        when(referralMapper.toEntity(request)).thenReturn(newReferral);
        when(referralRepository.save(newReferral)).thenReturn(newReferral);
 
 
        // Act & Assert
        // The core of this test is to ensure IllegalArgumentException for active referrals.
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            referralService.createReferral(request);
        });
        assertEquals("Patient already has an active referral (REFERRED or INPROGRESS).", exception.getMessage());
        verify(referralRepository, never()).save(any(Referral.class));
        verify(patientRepository, never()).save(any(Patient.class)); // Patient should not be saved if referral creation fails early
    }
 
    @Test
    void createReferral_whenPatientHasExistingInProgressReferral_shouldThrowException() {
        // Arrange
        String patientId = new ObjectId().toHexString();
        String existingReferralId = new ObjectId().toHexString();
        Patient patient = createTestPatient(patientId, new ArrayList<>(Arrays.asList(existingReferralId)));
        ReferralRequest request = createTestReferralRequest(patientId);
        Referral existingReferral = Referral.builder().id(new ObjectId(existingReferralId)).status(Status.INPROGRESS).build();
        Referral newReferral = Referral.builder().id(new ObjectId()).build(); // For mocking
 
        when(patientRepository.findById(any(ObjectId.class))).thenReturn(Optional.of(patient));
        when(referralRepository.findAllByIdIn(patient.getReferralIds().stream().map(ObjectId::new).collect(Collectors.toList()))).thenReturn(Arrays.asList(existingReferral));
        // Add mocks to prevent NPEs if the IllegalArgumentException is not thrown early enough
        when(referralMapper.toEntity(request)).thenReturn(newReferral);
        when(referralRepository.save(newReferral)).thenReturn(newReferral);
 
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            referralService.createReferral(request);
        });
        assertEquals("Patient already has an active referral (REFERRED or INPROGRESS).", exception.getMessage());
        verify(referralRepository, never()).save(any(Referral.class));
        verify(patientRepository, never()).save(any(Patient.class));
    }
 
    @Test
    void createReferral_whenPatientHasExistingCompletedReferral_shouldCreateNewReferral() {
        // Arrange
        String patientIdStr = new ObjectId().toHexString();
        ObjectId patientObjectId = new ObjectId(patientIdStr);
        String existingReferralIdStr = new ObjectId().toHexString();
        Patient patient = createTestPatient(patientIdStr, new ArrayList<>(Arrays.asList(existingReferralIdStr)));
        // Store original patient fields to check they are not modified
        String originalPatientHospitalName = patient.getHospitalName();
        String originalPatientStatus = patient.getStatus();
 
        ReferralRequest request = createTestReferralRequest(patientIdStr);
        Referral existingReferral = Referral.builder().id(new ObjectId(existingReferralIdStr)).status(Status.COMPLETED).build();
        Referral newReferral = Referral.builder().id(new ObjectId()).status(request.status()).build(); // Mapper output
        ReferralResponse expectedResponse = ReferralResponse.builder().id(newReferral.getId().toHexString()).build(); // Mapper output
 
        when(patientRepository.findById(patientObjectId)).thenReturn(Optional.of(patient));
        // The following stubbing was marked as unnecessary.
        // If existingReferral is COMPLETED, hasActiveReferral should return false.
        // The actual list of referrals might still be fetched by hasActiveReferral.
        // Removing the explicit stub for findAllByIdIn as it was marked unnecessary by Mockito.
        // The service logic should correctly identify that a COMPLETED referral is not "active".
        when(referralMapper.toEntity(request)).thenReturn(newReferral);
        when(referralRepository.save(any(Referral.class))).thenReturn(newReferral);
        when(referralMapper.toResponse(eq(newReferral), any(),eq(null),eq(null))).thenReturn(expectedResponse);
 
 
        // Act
        ReferralResponse response = referralService.createReferral(request);
 
        // Assert
        assertNotNull(response);
        assertEquals(newReferral.getId().toHexString(), response.id());
 
        verify(referralRepository).save(referralCaptor.capture());
        Referral savedReferral = referralCaptor.getValue();
        assertNotNull(savedReferral.getCreatedAt());
        assertNotNull(savedReferral.getUpdatedAt());
 
        verify(patientRepository).save(patientCaptor.capture());
        Patient savedPatient = patientCaptor.getValue();
        assertTrue(savedPatient.getReferralIds().contains(newReferral.getId().toHexString()));
        assertEquals(2, savedPatient.getReferralIds().size()); // Existing + new
 
        // Verify patient's hospitalName is NOT changed to those from ReferralRequest, but status IS.
        assertEquals(originalPatientHospitalName, savedPatient.getHospitalName());
        assertEquals(Status.REFERRED.name(), savedPatient.getStatus());
    }
    
    @Test
    void createReferral_whenPatientHasNoExistingReferrals_shouldCreateNewReferral() {
        // Arrange
        String patientIdStr = new ObjectId().toHexString();
        ObjectId patientObjectId = new ObjectId(patientIdStr);
        Patient patient = createTestPatient(patientIdStr, new ArrayList<>()); // Empty list
        String originalPatientHospitalName = patient.getHospitalName();
        String originalPatientStatus = patient.getStatus();
 
        ReferralRequest request = createTestReferralRequest(patientIdStr);
        Referral newReferral = Referral.builder().id(new ObjectId()).status(request.status()).build();
        ReferralResponse expectedResponse = ReferralResponse.builder().id(newReferral.getId().toHexString()).build();
 
        when(patientRepository.findById(patientObjectId)).thenReturn(Optional.of(patient));
        // No call to referralRepository.findAllByIdIn when referralIds is empty
        when(referralMapper.toEntity(request)).thenReturn(newReferral);
        when(referralRepository.save(any(Referral.class))).thenReturn(newReferral);
        when(referralMapper.toResponse(eq(newReferral), any(),eq(null),eq(null))).thenReturn(expectedResponse);
 
        // Act
        ReferralResponse response = referralService.createReferral(request);
 
        // Assert
        assertNotNull(response);
        assertEquals(newReferral.getId().toHexString(), response.id());
 
        verify(referralRepository).save(referralCaptor.capture());
        Referral savedReferral = referralCaptor.getValue();
        assertNotNull(savedReferral.getCreatedAt());
        assertNotNull(savedReferral.getUpdatedAt());
 
        verify(patientRepository).save(patientCaptor.capture());
        Patient savedPatient = patientCaptor.getValue();
        assertTrue(savedPatient.getReferralIds().contains(newReferral.getId().toHexString()));
        assertEquals(1, savedPatient.getReferralIds().size());
 
        assertEquals(originalPatientHospitalName, savedPatient.getHospitalName());
        assertEquals(Status.REFERRED.name(), savedPatient.getStatus());
        
        verify(referralRepository, never()).findAllByIdIn(anyList());
    }
 
    @Test
    void createReferral_whenPatientReferralIdsListIsNull_shouldInitializeAndCreateNewReferral() {
        // Arrange
        String patientIdStr = new ObjectId().toHexString();
        ObjectId patientObjectId = new ObjectId(patientIdStr);
        Patient patient = createTestPatient(patientIdStr, null); // referralIds is null
        assertNull(patient.getReferralIds());
        String originalPatientHospitalName = patient.getHospitalName();
        String originalPatientStatus = patient.getStatus();
 
 
        ReferralRequest request = createTestReferralRequest(patientIdStr);
        Referral newReferral = Referral.builder().id(new ObjectId()).status(request.status()).build();
        ReferralResponse expectedResponse = ReferralResponse.builder().id(newReferral.getId().toHexString()).build();
 
        when(patientRepository.findById(patientObjectId)).thenReturn(Optional.of(patient));
        when(referralMapper.toEntity(request)).thenReturn(newReferral);
        when(referralRepository.save(any(Referral.class))).thenReturn(newReferral);
        when(referralMapper.toResponse(eq(newReferral), any(),eq(null),eq(null))).thenReturn(expectedResponse);
 
        // Act
        ReferralResponse response = referralService.createReferral(request);
 
        // Assert
        assertNotNull(response);
        assertEquals(newReferral.getId().toHexString(), response.id());
 
        verify(referralRepository).save(referralCaptor.capture());
        Referral savedReferral = referralCaptor.getValue();
        assertNotNull(savedReferral.getCreatedAt());
        assertNotNull(savedReferral.getUpdatedAt());
 
        verify(patientRepository).save(patientCaptor.capture());
        Patient savedPatient = patientCaptor.getValue();
        assertNotNull(savedPatient.getReferralIds()); // Should be initialized
        assertTrue(savedPatient.getReferralIds().contains(newReferral.getId().toHexString()));
        assertEquals(1, savedPatient.getReferralIds().size());
        
        assertEquals(originalPatientHospitalName, savedPatient.getHospitalName());
        assertEquals(Status.REFERRED.name(), savedPatient.getStatus());
 
        verify(referralRepository, never()).findAllByIdIn(anyList());
    }
 
 
    @Test
    void testFilterReferrals_EnrichesAmbassadorDetailsAndSortsByDefault() {
        // Arrange
        List<Referral> referralsFromDb = Arrays.asList(
                referral1, referral2, referralWithNullAmbassadorId,
                referralToVAWithInvalidUserId, referralToVAWithNoUserLink, referralToVANotFound
        );
        Pageable unsortedPageable = PageRequest.of(0, 10);
 
        when(mongoTemplate.find(queryCaptor.capture(), eq(Referral.class))).thenReturn(referralsFromDb);
        when(mongoTemplate.count(any(Query.class), eq(Referral.class))).thenReturn((long) referralsFromDb.size());
 
        // Mock VisionAmbassador repo
        when(visionAmbassadorRepository.findById(vaId1)).thenReturn(Optional.of(ambassador1WithUser));
        when(visionAmbassadorRepository.findById(vaId2)).thenReturn(Optional.of(ambassador2WithUser));
        when(visionAmbassadorRepository.findById(vaIdWithInvalidUser)).thenReturn(Optional.of(ambassadorWithInvalidUserId));
        when(visionAmbassadorRepository.findById(vaIdWithNoUserLink)).thenReturn(Optional.of(ambassadorWithNoUserLink));
        when(visionAmbassadorRepository.findById(vaIdNotFound)).thenReturn(Optional.empty());
 
 
        // Mock User repo
        when(userRepository.findById(eq(new ObjectId(user1IdString)))).thenReturn(Optional.of(user1));
        when(userRepository.findById(eq(new ObjectId(user2IdString)))).thenReturn(Optional.of(user2));
        when(userRepository.findById(eq(new ObjectId(userNonExistentIdString)))).thenReturn(Optional.empty()); // For ambassadorWithNoUserLink
 
 
        // Mock the mapper behavior for each referral, now with User
        when(referralMapper.toResponse(eq(referral1), eq(user1), eq(null),eq(null)))
            .thenReturn(ReferralResponse.builder().id(referral1.getId().toHexString()).patientName("Patient A")
                            .ambassadorName("User One").ambassadorPhoneNumber("111222").ambassadorEmail("user1@example.com").build());
        when(referralMapper.toResponse(eq(referral2), eq(user2), eq(null),eq(null)))
            .thenReturn(ReferralResponse.builder().id(referral2.getId().toHexString()).patientName("Patient B")
                            .ambassadorName("User Two").ambassadorPhoneNumber("333444").ambassadorEmail("user2@example.com").build());
        
        // For referrals where user mapping will be null
        when(referralMapper.toResponse(eq(referralWithNullAmbassadorId), eq(null), eq(null),eq(null)))
            .thenReturn(ReferralResponse.builder().id(referralWithNullAmbassadorId.getId().toHexString()).patientName("Patient C").build());
        when(referralMapper.toResponse(eq(referralToVAWithInvalidUserId), eq(null), eq(null),eq(null))) // VA's userId is invalid
            .thenReturn(ReferralResponse.builder().id(referralToVAWithInvalidUserId.getId().toHexString()).patientName("Patient D").build());
        when(referralMapper.toResponse(eq(referralToVAWithNoUserLink), eq(null), eq(null),eq(null))) // VA's userId links to non-existent User
            .thenReturn(ReferralResponse.builder().id(referralToVAWithNoUserLink.getId().toHexString()).patientName("Patient E").build());
        when(referralMapper.toResponse(eq(referralToVANotFound), eq(null), eq(null),eq(null))) // VA itself not found
            .thenReturn(ReferralResponse.builder().id(referralToVANotFound.getId().toHexString()).patientName("Patient F").build());
 
        // Act
        Page<ReferralResponse> resultPage = referralService.filterReferrals(null, null, null, null, null,null,unsortedPageable);
 
        // Assert
        assertNotNull(resultPage);
        assertEquals(referralsFromDb.size(), resultPage.getTotalElements());
        assertEquals(referralsFromDb.size(), resultPage.getContent().size());
 
        // Verify ambassador details by checking the mocked mapper outputs
        assertTrue(resultPage.getContent().stream().anyMatch(r -> "User One".equals(r.ambassadorName()) && "user1@example.com".equals(r.ambassadorEmail())));
        assertTrue(resultPage.getContent().stream().anyMatch(r -> "User Two".equals(r.ambassadorName()) && "user2@example.com".equals(r.ambassadorEmail())));
        // Count referrals that should have null ambassador details due to various reasons
        long countWithNullAmbassadorDetails = resultPage.getContent().stream()
            .filter(r -> r.ambassadorName() == null && r.ambassadorEmail() == null && r.ambassadorPhoneNumber() == null)
            .count();
        assertEquals(4, countWithNullAmbassadorDetails); // referralWithNullAmbassadorId, referralToVAWithInvalidUserId, referralToVAWithNoUserLink, referralToVANotFound
 
        // Verify VisionAmbassadorRepository findById calls
        verify(visionAmbassadorRepository).findById(vaId1);
        verify(visionAmbassadorRepository).findById(vaId2);
        verify(visionAmbassadorRepository).findById(vaIdWithInvalidUser);
        verify(visionAmbassadorRepository).findById(vaIdWithNoUserLink);
        verify(visionAmbassadorRepository).findById(vaIdNotFound);
        
        // Verify UserRepository findById calls
        verify(userRepository).findById(eq(new ObjectId(user1IdString)));
        verify(userRepository).findById(eq(new ObjectId(user2IdString)));
        verify(userRepository).findById(eq(new ObjectId(userNonExistentIdString))); // Attempted for ambassadorWithNoUserLink
        // Should NOT be called for ambassadorWithInvalidUserId because its userId is not a valid ObjectId string
        // Should NOT be called if referral.getAmbassadorId() is null
        // Should NOT be called if VA itself is not found
        verify(userRepository, times(3)).findById(any(ObjectId.class));
 
 
        // Verify default sorting
        Query capturedQuery = queryCaptor.getValue();
        Document sortDocument = capturedQuery.getSortObject();
        assertNotNull(sortDocument, "Sort document should not be null");
        assertFalse(sortDocument.isEmpty(), "Sort document should not be empty for default sort");
        assertEquals(-1, sortDocument.getInteger("createdAt"), "Should sort by createdAt descending");
    }
 
    @Test
    void testFilterReferrals_RespectsClientSort() {
        // Arrange
        Pageable clientSortedPageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "patientName"));
        when(mongoTemplate.find(queryCaptor.capture(), eq(Referral.class))).thenReturn(Collections.emptyList());
        when(mongoTemplate.count(any(Query.class), eq(Referral.class))).thenReturn(0L);
 
        // Act
        referralService.filterReferrals(null, null, null, null, null, null,clientSortedPageable);
 
        // Assert
        Query capturedQuery = queryCaptor.getValue();
        Document sortDocument = capturedQuery.getSortObject();
        assertNotNull(sortDocument, "Sort document should not be null");
        assertFalse(sortDocument.isEmpty(), "Sort document should not be empty for client-specified sort");
        assertEquals(1, sortDocument.getInteger("patientName"), "Should sort by patientName ascending");
        assertNull(sortDocument.get("createdAt"), "Default sort by createdAt should not be applied");
    }
 
    // --- Tests for bulkUpdateReferrals ---
 
    private BulkReferralUpdateRequest.EyeDetailsDto createEyeDetailsDto(Double sph, Double cyl, Integer axis) {
        return BulkReferralUpdateRequest.EyeDetailsDto.builder().sph(sph).cyl(cyl).axis(axis).build();
    }
 
    @Test
    void bulkUpdateReferrals_found_statusProvided_validStatus_shouldUpdate() {
        // Arrange
        BulkReferralUpdateRequest request = BulkReferralUpdateRequest.builder()
                .hospitalCode("HOS001")
                .age(30)
                .guardianContact("1234567890")
                .gender("MALE")
                .status("COMPLETED")
                .rightEye(createEyeDetailsDto(1.0, 0.5, 90))
                .leftEye(createEyeDetailsDto(1.25, 0.75, 80))
                .requestedOn(new Date().toString()) // Convert Date to String
                .referrals("Old Patient Name for Logging")
                .hospitalName("Old Hospital Name for Logging")
                .build();
        List<BulkReferralUpdateRequest> bulkRequest = Collections.singletonList(request);
 
        when(hospitalRepository.findByHospitalCode("HOS001")).thenReturn(Optional.of(testHospital));
        when(patientRepository.findByAgeAndPhoneAndGender("30", "1234567890", Gender.MALE)).thenReturn(Optional.of(testPatient));
        when(referralRepository.findByPatientIdAndHospitalId(testPatientId, testHospitalId)).thenReturn(Optional.of(testReferral));
        when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);
        when(referralRepository.save(any(Referral.class))).thenReturn(testReferral);
 
        // Act
        BulkReferralUpdateResponse response = referralService.bulkUpdateReferrals(bulkRequest);
 
        // Assert
        assertEquals(1, response.getUpdatedRecords());
        assertEquals(0, response.getRejectedRecords());
        assertTrue(response.getRejectedList().isEmpty());
 
        verify(patientRepository).save(patientCaptor.capture());
        assertEquals("COMPLETED", patientCaptor.getValue().getStatus());
        
        verify(referralRepository).save(referralCaptor.capture());
        Referral savedReferral = referralCaptor.getValue();
        assertEquals(Status.COMPLETED, savedReferral.getStatus());
        assertEquals("1.0", savedReferral.getRightEye().getSph());
        assertEquals("1.25", savedReferral.getLeftEye().getSph());
        assertNotNull(savedReferral.getSpectacleRequestedOn());
        assertTrue(savedReferral.getIsSpectacleRequested());
    }
 
    @Test
    void bulkUpdateReferrals_found_statusMissing_shouldUpdateDetailsNotStatus() {
        // Arrange
        BulkReferralUpdateRequest request = BulkReferralUpdateRequest.builder()
                .hospitalCode("HOS001")
                .age(30)
                .guardianContact("1234567890")
                .gender("MALE")
                .status(null) // Status missing
                .rightEye(createEyeDetailsDto(2.0, null, null))
                .build();
        List<BulkReferralUpdateRequest> bulkRequest = Collections.singletonList(request);
        
        String originalPatientStatus = testPatient.getStatus();
        Status originalReferralStatus = testReferral.getStatus();
 
        when(hospitalRepository.findByHospitalCode("HOS001")).thenReturn(Optional.of(testHospital));
        when(patientRepository.findByAgeAndPhoneAndGender("30", "1234567890", Gender.MALE)).thenReturn(Optional.of(testPatient));
        when(referralRepository.findByPatientIdAndHospitalId(testPatientId, testHospitalId)).thenReturn(Optional.of(testReferral));
        when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);
        when(referralRepository.save(any(Referral.class))).thenReturn(testReferral);
 
        // Act
        BulkReferralUpdateResponse response = referralService.bulkUpdateReferrals(bulkRequest);
 
        // Assert
        assertEquals(1, response.getUpdatedRecords());
        assertEquals(0, response.getRejectedRecords());
 
        verify(patientRepository).save(patientCaptor.capture());
        assertEquals(originalPatientStatus, patientCaptor.getValue().getStatus()); // Status should not change
 
        verify(referralRepository).save(referralCaptor.capture());
        Referral savedReferral = referralCaptor.getValue();
        assertEquals(originalReferralStatus, savedReferral.getStatus()); // Status should not change
        assertEquals("2.0", savedReferral.getRightEye().getSph());
        assertTrue(savedReferral.getIsSpectacleRequested());
    }
 
    @Test
    void bulkUpdateReferrals_hospitalNotFound_shouldReject() {
        // Arrange
        BulkReferralUpdateRequest request = BulkReferralUpdateRequest.builder().hospitalCode("INVALID_CODE").build();
        List<BulkReferralUpdateRequest> bulkRequest = Collections.singletonList(request);
 
        when(hospitalRepository.findByHospitalCode("INVALID_CODE")).thenReturn(Optional.empty());
 
        // Act
        BulkReferralUpdateResponse response = referralService.bulkUpdateReferrals(bulkRequest);
 
        // Assert
        assertEquals(0, response.getUpdatedRecords());
        assertEquals(1, response.getRejectedRecords());
        assertEquals(request.getReferrals(), response.getRejectedList().get(0).getReferrals()); // Using 'referrals' as patient name for logging
    }
 
    @Test
    void bulkUpdateReferrals_patientNotFound_shouldReject() {
        // Arrange
        BulkReferralUpdateRequest request = BulkReferralUpdateRequest.builder()
                .hospitalCode("HOS001")
                .age(30)
                .guardianContact("UNKNOWN_CONTACT")
                .gender("FEMALE")
                .build();
        List<BulkReferralUpdateRequest> bulkRequest = Collections.singletonList(request);
 
        when(hospitalRepository.findByHospitalCode("HOS001")).thenReturn(Optional.of(testHospital));
        when(patientRepository.findByAgeAndPhoneAndGender("30", "UNKNOWN_CONTACT", Gender.FEMALE)).thenReturn(Optional.empty());
 
        // Act
        BulkReferralUpdateResponse response = referralService.bulkUpdateReferrals(bulkRequest);
 
        // Assert
        assertEquals(0, response.getUpdatedRecords());
        assertEquals(1, response.getRejectedRecords());
    }
 
    @Test
    void bulkUpdateReferrals_referralEntityNotFound_shouldReject() {
        // Arrange
        BulkReferralUpdateRequest request = BulkReferralUpdateRequest.builder()
                .hospitalCode("HOS001")
                .age(30)
                .guardianContact("1234567890")
                .gender("MALE")
                .build();
        List<BulkReferralUpdateRequest> bulkRequest = Collections.singletonList(request);
 
        when(hospitalRepository.findByHospitalCode("HOS001")).thenReturn(Optional.of(testHospital));
        when(patientRepository.findByAgeAndPhoneAndGender("30", "1234567890", Gender.MALE)).thenReturn(Optional.of(testPatient));
        when(referralRepository.findByPatientIdAndHospitalId(testPatientId, testHospitalId)).thenReturn(Optional.empty());
        
        // Act
        BulkReferralUpdateResponse response = referralService.bulkUpdateReferrals(bulkRequest);
 
        // Assert
        assertEquals(0, response.getUpdatedRecords());
        assertEquals(1, response.getRejectedRecords());
    }
    
    @Test
    void bulkUpdateReferrals_invalidGenderString_shouldReject() {
        // Arrange
        BulkReferralUpdateRequest request = BulkReferralUpdateRequest.builder()
                .hospitalCode("HOS001")
                .age(30)
                .guardianContact("1234567890")
                .gender("INVALID_GENDER") // Invalid gender
                .build();
        List<BulkReferralUpdateRequest> bulkRequest = Collections.singletonList(request);
 
        when(hospitalRepository.findByHospitalCode("HOS001")).thenReturn(Optional.of(testHospital));
        // Patient repo findByAgeAndPhoneAndGender will not be called due to early exit
 
        // Act
        BulkReferralUpdateResponse response = referralService.bulkUpdateReferrals(bulkRequest);
 
        // Assert
        assertEquals(0, response.getUpdatedRecords());
        assertEquals(1, response.getRejectedRecords());
        verify(patientRepository, never()).findByAgeAndPhoneAndGender(any(), any(), any());
    }
 
    @Test
    void bulkUpdateReferrals_statusProvided_invalidStatusString_shouldReject() {
        // Arrange
        BulkReferralUpdateRequest request = BulkReferralUpdateRequest.builder()
                .hospitalCode("HOS001")
                .age(30)
                .guardianContact("1234567890")
                .gender("MALE")
                .status("INVALID_STATUS_STRING") // Invalid status
                .build();
        List<BulkReferralUpdateRequest> bulkRequest = Collections.singletonList(request);
 
        when(hospitalRepository.findByHospitalCode("HOS001")).thenReturn(Optional.of(testHospital));
        when(patientRepository.findByAgeAndPhoneAndGender("30", "1234567890", Gender.MALE)).thenReturn(Optional.of(testPatient));
        when(referralRepository.findByPatientIdAndHospitalId(testPatientId, testHospitalId)).thenReturn(Optional.of(testReferral));
        // Save methods should not be called
 
        // Act
        BulkReferralUpdateResponse response = referralService.bulkUpdateReferrals(bulkRequest);
 
        // Assert
        assertEquals(0, response.getUpdatedRecords());
        assertEquals(1, response.getRejectedRecords());
        verify(patientRepository, never()).save(any());
        verify(referralRepository, never()).save(any());
    }
 
     @Test
    void bulkUpdateReferrals_nullAgeInRequest_shouldPassNullOrStringifiedNullToRepo() {
        BulkReferralUpdateRequest request = BulkReferralUpdateRequest.builder()
                .hospitalCode("HOS001")
                .age(null) // Null age
                .guardianContact("1234567890")
                .gender("MALE")
                .status("COMPLETED")
                .build();
        List<BulkReferralUpdateRequest> bulkRequest = Collections.singletonList(request);
 
        when(hospitalRepository.findByHospitalCode("HOS001")).thenReturn(Optional.of(testHospital));
        // We want to verify that findByAgeAndPhoneAndGender is called with null or "null" for age
        // For this test, let's assume it leads to patient not found for simplicity of assertion focus.
        when(patientRepository.findByAgeAndPhoneAndGender(eq(null), eq("1234567890"), eq(Gender.MALE)))
            .thenReturn(Optional.empty());
 
 
        BulkReferralUpdateResponse response = referralService.bulkUpdateReferrals(bulkRequest);
 
        assertEquals(0, response.getUpdatedRecords());
        assertEquals(1, response.getRejectedRecords());
        // Verify that the repository method was called with null for the age parameter
        verify(patientRepository).findByAgeAndPhoneAndGender(null, "1234567890", Gender.MALE);
    }
 
}
 
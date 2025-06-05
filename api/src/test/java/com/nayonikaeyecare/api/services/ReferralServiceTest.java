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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
    // @Captor
    // private ArgumentCaptor<VisionAmbassador> ambassadorCaptor; // No longer directly capturing VA if User is main focus
    @Captor
    private ArgumentCaptor<User> userCaptor; // For mapper

    private Referral referral1, referral2, referralWithNullAmbassadorId, referralToVAWithInvalidUserId, referralToVAWithNoUserLink, referralToVANotFound;
    private VisionAmbassador ambassador1WithUser, ambassador2WithUser, ambassadorWithInvalidUserId, ambassadorWithNoUserLink;
    private User user1, user2;
    private ObjectId vaId1, vaId2, vaIdWithInvalidUser, vaIdWithNoUserLink, vaIdNotFound; // VisionAmbassador ObjectIds
    private String user1IdString, user2IdString, userNonExistentIdString;


    @BeforeEach
    void setUp() {
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
        when(referralMapper.toResponse(eq(referral1), eq(user1)))
            .thenReturn(ReferralResponse.builder().id(referral1.getId().toHexString()).patientName("Patient A")
                            .ambassadorName("User One").ambassadorPhoneNumber("111222").ambassadorEmail("user1@example.com").build());
        when(referralMapper.toResponse(eq(referral2), eq(user2)))
            .thenReturn(ReferralResponse.builder().id(referral2.getId().toHexString()).patientName("Patient B")
                            .ambassadorName("User Two").ambassadorPhoneNumber("333444").ambassadorEmail("user2@example.com").build());
        
        // For referrals where user mapping will be null
        when(referralMapper.toResponse(eq(referralWithNullAmbassadorId), eq(null)))
            .thenReturn(ReferralResponse.builder().id(referralWithNullAmbassadorId.getId().toHexString()).patientName("Patient C").build());
        when(referralMapper.toResponse(eq(referralToVAWithInvalidUserId), eq(null))) // VA's userId is invalid
            .thenReturn(ReferralResponse.builder().id(referralToVAWithInvalidUserId.getId().toHexString()).patientName("Patient D").build());
        when(referralMapper.toResponse(eq(referralToVAWithNoUserLink), eq(null))) // VA's userId links to non-existent User
            .thenReturn(ReferralResponse.builder().id(referralToVAWithNoUserLink.getId().toHexString()).patientName("Patient E").build());
        when(referralMapper.toResponse(eq(referralToVANotFound), eq(null))) // VA itself not found
            .thenReturn(ReferralResponse.builder().id(referralToVANotFound.getId().toHexString()).patientName("Patient F").build());

        // Act
        Page<ReferralResponse> resultPage = referralService.filterReferrals(null, null, null, null, null, unsortedPageable);

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
        referralService.filterReferrals(null, null, null, null, null, clientSortedPageable);

        // Assert
        Query capturedQuery = queryCaptor.getValue();
        Document sortDocument = capturedQuery.getSortObject();
        assertNotNull(sortDocument, "Sort document should not be null");
        assertFalse(sortDocument.isEmpty(), "Sort document should not be empty for client-specified sort");
        assertEquals(1, sortDocument.getInteger("patientName"), "Should sort by patientName ascending");
        assertNull(sortDocument.get("createdAt"), "Default sort by createdAt should not be applied");
    }
}

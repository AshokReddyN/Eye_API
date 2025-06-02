package com.nayonikaeyecare.api.repositories.visionambassador;

import com.nayonikaeyecare.api.dto.visionambassador.VisionAmbassadorResponse;
import com.nayonikaeyecare.api.entities.Referral;
import com.nayonikaeyecare.api.entities.VisionAmbassador;
import com.nayonikaeyecare.api.repositories.referral.ReferralRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class VisionAmbassadorRepositoryImplTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private ReferralRepository referralRepository;

    @InjectMocks
    private VisionAmbassadorRepositoryImpl visionAmbassadorRepositoryImpl;

    @Test
    void testFilterVisionAmbassador_includesPatientCount() {
        // Arrange
        String state = "TestState";
        String city = "TestCity";
        Pageable pageable = PageRequest.of(0, 10);

        ObjectId ambassadorId = new ObjectId();
        VisionAmbassador ambassador = VisionAmbassador.builder()
                .id(ambassadorId)
                .name("Test Ambassador")
                .state(state)
                .city(city)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        List<VisionAmbassador> ambassadors = Collections.singletonList(ambassador);
        List<Referral> referrals = Collections.nCopies(5, new Referral()); // 5 referrals

        // Mock MongoTemplate behavior
        when(mongoTemplate.find(any(Query.class), eq(VisionAmbassador.class)))
                .thenReturn(ambassadors);
        when(mongoTemplate.count(any(Query.class), eq(VisionAmbassador.class)))
                .thenReturn((long) ambassadors.size());

        // Mock ReferralRepository behavior
        when(referralRepository.findByAmbassadorId(ambassadorId))
                .thenReturn(referrals);

        // Act
        Page<VisionAmbassadorResponse> resultPage =
                visionAmbassadorRepositoryImpl.filterVisionAmbassador(state, city, pageable);

        // Assert
        assertNotNull(resultPage);
        assertEquals(1, resultPage.getTotalElements());
        assertEquals(1, resultPage.getContent().size());

        VisionAmbassadorResponse response = resultPage.getContent().get(0);
        assertEquals(ambassadorId.toHexString(), response.getId());
        assertEquals("Test Ambassador", response.getName());
        assertEquals(5, response.getPatientCount()); // Verify patient count
    }
}

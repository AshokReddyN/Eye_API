package com.nayonikaeyecare.api.services;

import com.nayonikaeyecare.api.dto.hospital.HospitalResponse;
import com.nayonikaeyecare.api.entities.Hospital;
import com.nayonikaeyecare.api.entities.Address; // Assuming Address is a class
import com.nayonikaeyecare.api.mappers.HospitalMapper;
import com.nayonikaeyecare.api.repositories.hospital.HospitalRepository;
import com.nayonikaeyecare.api.repositories.referral.ReferralRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class HospitalServiceTest {

    @Mock
    private HospitalRepository hospitalRepository;

    @Mock
    private ReferralRepository referralRepository;

    @InjectMocks
    private HospitalService hospitalService;

    private Hospital hospital1;
    private Hospital hospital2;
    private ObjectId hospital1Id;
    private ObjectId hospital2Id;

    @BeforeEach
    void setUp() {
        hospital1Id = new ObjectId();
        hospital2Id = new ObjectId();

        // Using builder pattern for Address instantiation
        Address address1 = Address.builder()
                .address1("123 Main St")
                .city("CityA")
                .state("StateX")
                .pincode("10001")
                .build();
        Address address2 = Address.builder()
                .address1("456 Oak Ave")
                .city("CityB")
                .state("StateY")
                .pincode("20002")
                .build();

        hospital1 = Hospital.builder()
                .id(hospital1Id)
                .name("Hospital One")
                .address(address1)
                .services(Arrays.asList("Cardiology", "Neurology"))
                .status(true)
                .build();

        hospital2 = Hospital.builder()
                .id(hospital2Id)
                .name("Hospital Two")
                .address(address2)
                .services(Arrays.asList("Pediatrics"))
                .status(true)
                .build();
    }

    @Test
    void testFilterHospitals_ReturnsHospitalsWithReferralCounts() {
        // Arrange
        List<Hospital> hospitals = Arrays.asList(hospital1, hospital2);
        Page<Hospital> hospitalPage = new PageImpl<>(hospitals, PageRequest.of(0, 10), hospitals.size());

        String state = "StateX";
        List<String> cities = Collections.singletonList("CityA");
        Boolean status = true;
        String searchString = "Hospital";
        List<String> services = Collections.emptyList();
        Pageable pageable = PageRequest.of(0, 10);

        when(hospitalRepository.filterHospitals(state, cities, status, searchString, services, pageable))
                .thenReturn(hospitalPage);

        when(referralRepository.countByHospitalId(hospital1Id)).thenReturn(5L);
        when(referralRepository.countByHospitalId(hospital2Id)).thenReturn(10L);

        // New mock for spectacle referrals
        when(referralRepository.countByHospitalIdAndIsSpectacleRequestedTrue(hospital1Id)).thenReturn(2L); // New
        when(referralRepository.countByHospitalIdAndIsSpectacleRequestedTrue(hospital2Id)).thenReturn(3L); // New


        // Act
        Page<HospitalResponse> resultPage = hospitalService.filterHospitals(state, cities, status, searchString, services, pageable);

        // Assert
        assertNotNull(resultPage);
        assertEquals(2, resultPage.getTotalElements());
        List<HospitalResponse> resultList = resultPage.getContent();

        HospitalResponse response1 = resultList.stream().filter(r -> r.id().equals(hospital1Id.toHexString())).findFirst().orElse(null);
        assertNotNull(response1);
        assertEquals("Hospital One", response1.name());
        assertEquals(5L, response1.referralCount());
        assertEquals(2L, response1.spectacleReferralCount()); // New assertion

        HospitalResponse response2 = resultList.stream().filter(r -> r.id().equals(hospital2Id.toHexString())).findFirst().orElse(null);
        assertNotNull(response2);
        assertEquals("Hospital Two", response2.name());
        assertEquals(10L, response2.referralCount());
        assertEquals(3L, response2.spectacleReferralCount()); // New assertion
    }

    @Test
    void testFilterHospitals_NoReferrals() {
        // Arrange
        List<Hospital> hospitals = Collections.singletonList(hospital1);
        Page<Hospital> hospitalPage = new PageImpl<>(hospitals, PageRequest.of(0, 10), hospitals.size());
        
        Pageable pageable = PageRequest.of(0, 10);

        when(hospitalRepository.filterHospitals(null, null, null, null, null, pageable))
                .thenReturn(hospitalPage);
        when(referralRepository.countByHospitalId(hospital1Id)).thenReturn(0L);
        when(referralRepository.countByHospitalIdAndIsSpectacleRequestedTrue(hospital1Id)).thenReturn(0L); // New, assuming no spectacle referrals if no total referrals

        // Act
        Page<HospitalResponse> resultPage = hospitalService.filterHospitals(null, null, null, null, null, pageable);

        // Assert
        assertNotNull(resultPage);
        assertEquals(1, resultPage.getTotalElements());
        HospitalResponse response1 = resultPage.getContent().get(0);
        assertNotNull(response1);
        assertEquals("Hospital One", response1.name());
        assertEquals(0L, response1.referralCount());
        assertEquals(0L, response1.spectacleReferralCount()); // New assertion
    }
    
    @Test
    void testFilterHospitals_EmptyResultFromRepository() {
        // Arrange
        Page<Hospital> emptyHospitalPage = Page.empty(PageRequest.of(0, 10));
        Pageable pageable = PageRequest.of(0, 10);

        when(hospitalRepository.filterHospitals(anyString(), any(), any(), anyString(), any(), any(Pageable.class)))
            .thenReturn(emptyHospitalPage);

        // Act
        Page<HospitalResponse> resultPage = hospitalService.filterHospitals("SomeState", Collections.emptyList(), true, "Search", Collections.emptyList(), pageable);

        // Assert
        assertNotNull(resultPage);
        assertEquals(0, resultPage.getTotalElements());
    }
}

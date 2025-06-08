package com.nayonikaeyecare.api.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.reset; // Added static import for reset

import java.util.Optional;
import java.util.HashMap;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor; // Added import
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.nayonikaeyecare.api.dto.user.AuthenticationRequest;
import com.nayonikaeyecare.api.dto.user.AuthenticationResponse;
import com.nayonikaeyecare.api.dto.user.OTPResendRequest;
import com.nayonikaeyecare.api.dto.user.OTPVerificationRequest;
import com.nayonikaeyecare.api.dto.user.UserRequest;
import com.nayonikaeyecare.api.entities.application.Application; // Added import
import com.nayonikaeyecare.api.entities.user.User;
import com.nayonikaeyecare.api.entities.user.UserCredential;
import com.nayonikaeyecare.api.entities.user.UserSession;
import com.nayonikaeyecare.api.entities.user.UserSessionStatus;
import com.nayonikaeyecare.api.repositories.application.ApplicationRepository;
import com.nayonikaeyecare.api.repositories.user.UserCredentialRepository;
import com.nayonikaeyecare.api.repositories.user.UserRepository;
import com.nayonikaeyecare.api.repositories.user.UserSessionRepository;
import com.nayonikaeyecare.api.security.JWTTokenProvider;

class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCredentialRepository userCredentialRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private UserSessionRepository userSessionRepository;

    @Mock
    private JWTTokenProvider jwtTokenProvider;

    @Mock
    private SmsService smsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // General mocks that don't conflict can stay, e.g., jwtTokenProvider if needed by all
        // Removed applicationRepository mock from here
    }

    @Test
    void testCreateUser() {
        UserRequest userRequest = new UserRequest("John", "Doe", "1234567890", "testCredential", "appCode", "");
        User user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("1234567890")
                .createdAt(new java.util.Date())
                .build();

        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.createUser(userRequest);

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testAuthenticateUser_ValidCredentials() {
        AuthenticationRequest authRequest = new AuthenticationRequest("testCredential", "appCode", "", "");
        UserCredential userCredential = UserCredential.builder().credential("testCredential").id(new ObjectId()).build(); 
        User user = User.builder().id(new ObjectId()).userCredentialId(userCredential.getId()).build(); 
        UserSession userSession = UserSession.builder().id(new ObjectId()).userId(user.getId()).build(); 
        Application application = Application.builder().code("appCode").allowAutoRegistration(true).build(); // Application setup for safety
        when(applicationRepository.findByCode("appCode")).thenReturn(application); 

        when(userCredentialRepository.findByCredential("testCredential")).thenReturn(userCredential);
        when(userRepository.findByUserCredentialId(userCredential.getId())).thenReturn(user);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user)); // For SMS path, user has no phone here
        when(userSessionRepository.save(any(UserSession.class))).thenReturn(userSession);

        AuthenticationResponse response = userService.authenticateUser(authRequest);

        assertNotNull(response);
        verify(userSessionRepository, times(1)).save(any(UserSession.class));
        verify(smsService, never()).sendOtp(anyString(), anyString()); // Expect no OTP for user without phone
    }

    @Test
    void testAuthenticateUser_ValidCredentials_WithPhoneNumber() {
        AuthenticationRequest authRequest = new AuthenticationRequest("testCredential", "appCode", "", "");
        ObjectId userCredentialId = new ObjectId();
        UserCredential mockUserCredential = UserCredential.builder()
                                                .id(userCredentialId)
                                                .credential("testCredential")
                                                .build();
        ObjectId userId = new ObjectId();
        User mockUserWithPhoneNumber = User.builder()
                                    .id(userId)
                                    .userCredentialId(userCredentialId)
                                    .phoneNumber("1234567890")
                                    .build();
        
        Application mockApplication = Application.builder().code("appCode").allowAutoRegistration(true).build();
        when(applicationRepository.findByCode("appCode")).thenReturn(mockApplication); // Safety mock

        // Core mocks for existing user with phone
        when(userCredentialRepository.findByCredential("testCredential")).thenReturn(mockUserCredential);
        when(userRepository.findByUserCredentialId(mockUserCredential.getId())).thenReturn(mockUserWithPhoneNumber);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUserWithPhoneNumber)); // For SMS path

        // Mock session saving
        when(userSessionRepository.save(any(UserSession.class))).thenAnswer(invocation -> {
            UserSession session = invocation.getArgument(0);
            session.setId(new ObjectId()); // Simulate DB assigning an ID
            assertNotNull(session.getOtp(), "OTP should be generated");
            return session;
        });

        // Call the service
        AuthenticationResponse response = userService.authenticateUser(authRequest);

        // Assertions
        assertNotNull(response);
        assertFalse(response.newUserRegistered(), "User should not be newly registered");
        assertEquals(userId.toString(), response.userId());

        // Verify OTP sending
        ArgumentCaptor<String> otpCaptor = ArgumentCaptor.forClass(String.class);
        verify(smsService, times(1)).sendOtp(eq("1234567890"), otpCaptor.capture());
        assertNotNull(otpCaptor.getValue());
        assertEquals(4, otpCaptor.getValue().length(), "OTP should be 4 digits");

        // Verify other interactions
        verify(userCredentialRepository, times(1)).findByCredential("testCredential");
        verify(userRepository, times(1)).findByUserCredentialId(mockUserCredential.getId());
        verify(userRepository, times(1)).findById(userId); // Called for phone number retrieval
        verify(userSessionRepository, times(1)).save(any(UserSession.class));
    }

    @Test
    void testAuthenticateUser_NewUser_AutoRegistration_WithPhoneNumber() {
        AuthenticationRequest authRequest = new AuthenticationRequest("testCredential", "appCode", "", "");
        ObjectId newUserId = new ObjectId();
        String phoneNumber = "9876543210";

        Application mockApplication = Application.builder().code("appCode").allowAutoRegistration(true).build();
        when(applicationRepository.findByCode("appCode")).thenReturn(mockApplication);

        UserCredential newUserCredential = UserCredential.builder().id(new ObjectId()).credential("testCredential").build();
        User newUserWithPhone = User.builder().id(newUserId).userCredentialId(newUserCredential.getId()).phoneNumber(phoneNumber).build();

        // Scenario: User credential does not exist initially, then is created
        when(userCredentialRepository.findByCredential("testCredential"))
            .thenReturn(null)                       // First call in authenticateUser
            .thenReturn(newUserCredential);         // Second call in authenticateUser (after creation)

        when(userCredentialRepository.save(any(UserCredential.class))).thenReturn(newUserCredential);
        when(userRepository.save(any(User.class))).thenReturn(newUserWithPhone); // Ensures user saved has phone

        // When user is fetched by credential ID after creation
        when(userRepository.findByUserCredentialId(newUserCredential.getId())).thenReturn(newUserWithPhone);
        // When user is fetched by ID for SMS sending
        when(userRepository.findById(newUserId)).thenReturn(Optional.of(newUserWithPhone));

        when(userSessionRepository.save(any(UserSession.class))).thenAnswer(invocation -> {
            UserSession session = invocation.getArgument(0);
            session.setId(new ObjectId());
            assertNotNull(session.getOtp());
            return session;
        });
        
        // Call the service
        AuthenticationResponse response = userService.authenticateUser(authRequest);

        // Assertions
        assertNotNull(response);
        assertTrue(response.newUserRegistered(), "User should be newly registered");
        assertEquals(newUserId.toString(), response.userId());

        // Verify OTP sending
        ArgumentCaptor<String> otpCaptor = ArgumentCaptor.forClass(String.class);
        verify(smsService, times(1)).sendOtp(eq(phoneNumber), otpCaptor.capture());
        assertNotNull(otpCaptor.getValue());
        assertEquals(4, otpCaptor.getValue().length());

        // Verify interactions
        verify(applicationRepository, times(1)).findByCode("appCode");
        verify(userCredentialRepository, times(2)).findByCredential("testCredential"); // Called twice
        verify(userCredentialRepository, times(1)).save(any(UserCredential.class));
        verify(userRepository, times(1)).save(any(User.class));
        verify(userRepository, times(1)).findByUserCredentialId(newUserCredential.getId());
        verify(userRepository, times(1)).findById(newUserId);
        verify(userSessionRepository, times(1)).save(any(UserSession.class));
    }


    @Test
    void testResendOTP_ValidSession() {
        ObjectId userId = new ObjectId();
        ObjectId sessionId = new ObjectId();
        OTPResendRequest otpResendRequest = new OTPResendRequest(sessionId.toString());
        UserSession existingSession = UserSession.builder()
                .id(sessionId)
                .userId(userId) // Ensure userId is set
                .status(UserSessionStatus.INITIATIED)
                .build();
        UserSession newSession = UserSession.builder().id(new ObjectId()).userId(userId).build();
        User userWithoutPhone = User.builder().id(userId).build();


        when(userSessionRepository.findById(sessionId)).thenReturn(Optional.of(existingSession));
        when(userSessionRepository.save(any(UserSession.class))).thenReturn(newSession);
        when(userRepository.findById(userId)).thenReturn(Optional.of(userWithoutPhone)); // User has no phone

        UserSession result = userService.resendOTP(otpResendRequest);

        assertNotNull(result);
        verify(userSessionRepository, times(2)).save(any(UserSession.class));
        verify(smsService, never()).sendOtp(anyString(), anyString());
    }

    @Test
    void testResendOTP_ValidSession_WithPhoneNumber() {
        ObjectId userId = new ObjectId();
        ObjectId sessionId = new ObjectId();
        OTPResendRequest otpResendRequest = new OTPResendRequest(sessionId.toString());
        UserSession existingSession = UserSession.builder()
                .id(sessionId)
                .userId(userId)
                .status(UserSessionStatus.INITIATIED)
                .build();
        
        UserSession newSessionTemplate = UserSession.builder().id(new ObjectId()).userId(userId).build();
        User userWithPhone = User.builder().id(userId).phoneNumber("1234567890").build();

        when(userSessionRepository.findById(sessionId)).thenReturn(Optional.of(existingSession));
        when(userSessionRepository.save(any(UserSession.class))).thenAnswer(invocation -> {
            UserSession savedSession = invocation.getArgument(0);
            if (savedSession.getStatus() == UserSessionStatus.OTP_REGENERATED) { // this is the new session
                 assertNotNull(savedSession.getOtp());
                 assertTrue(savedSession.getOtp().length() > 0);
                 // Return a copy to avoid issues if the same instance is modified and returned
                 return UserSession.builder()
                    .id(savedSession.getId())
                    .userId(savedSession.getUserId())
                    .applicationCode(savedSession.getApplicationCode())
                    .otp(savedSession.getOtp()) // Ensure OTP is present for verification
                    .status(savedSession.getStatus())
                    .linkedSessionId(savedSession.getLinkedSessionId())
                    .createdAt(savedSession.getCreatedAt())
                    .updatedAt(savedSession.getUpdatedAt())
                    .build();
            }
            return savedSession;
        });
        when(userRepository.findById(userId)).thenReturn(Optional.of(userWithPhone));


        UserSession result = userService.resendOTP(otpResendRequest);

        assertNotNull(result);
        verify(userSessionRepository, times(2)).save(any(UserSession.class));
        verify(smsService, times(1)).sendOtp(eq("1234567890"), anyString());
    }

    @Test
    void testVerifyOTP_ValidOTP() {
        ObjectId sessionId = new ObjectId();
        OTPVerificationRequest otpVerificationRequest = new OTPVerificationRequest(sessionId.toString(), "1234");
        UserSession userSession = UserSession.builder()
                .id(sessionId)
                .userId(new ObjectId()) // Ensure userId is set for JWT generation
                .otp("1234")
                .status(UserSessionStatus.INITIATIED)
                .build();

        when(userSessionRepository.findById(sessionId)).thenReturn(Optional.of(userSession));
        when(jwtTokenProvider.generateToken(any(HashMap.class), any())).thenReturn("jwtToken"); // Further refined matcher

        String token = userService.verifyOTP(otpVerificationRequest);

        assertNotNull(token);
        assertEquals("jwtToken", token);
        verify(userSessionRepository, times(1)).save(userSession);
    }

    @Test
    void testVerifyOTP_InvalidOTP() {
        ObjectId sessionId = new ObjectId();
        OTPVerificationRequest otpVerificationRequest = new OTPVerificationRequest(sessionId.toString(), "5678");
        UserSession userSession = UserSession.builder()
                .id(sessionId)
                .otp("1234")
                .status(UserSessionStatus.INITIATIED)
                .build();

        when(userSessionRepository.findById(sessionId)).thenReturn(Optional.of(userSession));

        assertThrows(IllegalArgumentException.class, () -> userService.verifyOTP(otpVerificationRequest));
    }
}

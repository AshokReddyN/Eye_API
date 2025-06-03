package com.nayonikaeyecare.api.services;

import org.springframework.stereotype.Service;

import com.nayonikaeyecare.api.dto.user.AuthenticationRequest;
import com.nayonikaeyecare.api.dto.user.AuthenticationResponse;
import com.nayonikaeyecare.api.dto.visionambassador.VisionAmbassadorRequest;
import com.nayonikaeyecare.api.dto.visionambassador.VisionAmbassadorResponse;
import com.nayonikaeyecare.api.entities.VisionAmbassador;
import com.nayonikaeyecare.api.entities.user.User;
import com.nayonikaeyecare.api.exceptions.ResourceMissingException;
import com.nayonikaeyecare.api.repositories.referral.ReferralRepository;
import com.nayonikaeyecare.api.repositories.visionambassador.VisionAmbassadorRepository;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class VisionAmbassadorService {

    private final VisionAmbassadorRepository visionAmbassadorRepository;
    private final UserService userService;
    private final ReferralRepository referralRepository;

    public void createVisionAmbassador(VisionAmbassadorRequest visionAmbassadorRequest) {
        // Here you would typically save the Vision Ambassador to the database
        // For now, we'll just return a success message
        VisionAmbassador visionAmbassador = VisionAmbassador.builder()
                // .id(visionAmbassadorRequest.getId())
                .name(visionAmbassadorRequest.getName())
                .phoneNumber(visionAmbassadorRequest.getPhoneNumber())
                .city(visionAmbassadorRequest.getCity())
                .state(visionAmbassadorRequest.getState())
                .status(visionAmbassadorRequest.isStatus())
                .language(visionAmbassadorRequest.getLanguage())
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        visionAmbassadorRepository.save(visionAmbassador);
    }

    public List<VisionAmbassadorResponse> getAllVisionAmbassadors() {
        // Here you would typically retrieve the Vision Ambassador from the database
        List<VisionAmbassador> visionAmbassadors = visionAmbassadorRepository.findAll();

        return visionAmbassadors.stream().map(visionAmbassador -> {
            int referredPatientCount = referralRepository.findByAmbassadorId(visionAmbassador.getId()).size();
            return mapToVisionAmbassadorResponse(visionAmbassador, referredPatientCount);
        }).toList();

    }

    private VisionAmbassadorResponse mapToVisionAmbassadorResponse(VisionAmbassador visionAmbassador, int referredPatientCount) {
        return VisionAmbassadorResponse.builder()
                .id(visionAmbassador.getId()!= null ? visionAmbassador.getId().toHexString() : null)
                .name(visionAmbassador.getName())
                .phoneNumber(visionAmbassador.getPhoneNumber())
                .status(visionAmbassador.isStatus())
                .city(visionAmbassador.getCity())
                .state(visionAmbassador.getState())
                .language(visionAmbassador.getLanguage())
                .patientCount(referredPatientCount)
                .createdAt(visionAmbassador.getCreatedAt())
                .updatedAt(visionAmbassador.getUpdatedAt())
                .build();
    }

    public VisionAmbassadorResponse getVisionAmbassadorById(String id) {
        VisionAmbassador visionambassador = visionAmbassadorRepository.findById(new ObjectId(id))
                .orElseThrow(() -> new ResourceMissingException("Vision Ambassador not found with id: " + id));
        return mapToVisionAmbassadorResponse(visionambassador,0);
    }

    public VisionAmbassadorResponse updateVisionAmbassador(String id, VisionAmbassadorRequest visionAmbassadorRequest) {

        VisionAmbassador existingVisionAmbassador = visionAmbassadorRepository.findById(new ObjectId(id))
                .orElseThrow(() -> new ResourceMissingException("Vision Ambassador not found with id: " + id));

        existingVisionAmbassador.setName(visionAmbassadorRequest.getName());
        existingVisionAmbassador.setPhoneNumber(visionAmbassadorRequest.getPhoneNumber());
        existingVisionAmbassador.setCity(visionAmbassadorRequest.getCity());
        existingVisionAmbassador.setState(visionAmbassadorRequest.getState());
        existingVisionAmbassador.setStatus(visionAmbassadorRequest.isStatus());
        existingVisionAmbassador.setLanguage(visionAmbassadorRequest.getLanguage());
        existingVisionAmbassador.setCreatedAt(existingVisionAmbassador.getCreatedAt());
        existingVisionAmbassador.setUpdatedAt(new Date());

        VisionAmbassador updatedVisionAmbassador = visionAmbassadorRepository.save(existingVisionAmbassador);
        // TODO: Update this to also fetch patient count if needed post-update
        return mapToVisionAmbassadorResponse(updatedVisionAmbassador, 0); // Passing 0 for now
    }

    public void deleteVisionAmbassador(String id) {
        if (!visionAmbassadorRepository.existsById(new ObjectId(id))) {
            throw new ResourceMissingException("VisionAmbassador not found with id: " + id);
        }
        visionAmbassadorRepository.deleteById(new ObjectId(id));
    }

    public Page<VisionAmbassadorResponse> filterVisionAmbassador(String searchString,
            Pageable pageable) {
        return visionAmbassadorRepository.filterVisionAmbassador(searchString,pageable);
    }

    public VisionAmbassador findByUserId(String userId) {
        return visionAmbassadorRepository.findByUserId(userId);
    }

    public AuthenticationResponse visionAmbassadorSignin(AuthenticationRequest request) {
        // write the logic to find out whether user already exists
        // in case it already exists then just send the session id
        // else create a vision ambassador and then send the session id

        AuthenticationResponse response = userService.authenticateUser(request);
        if (response.newUserRegistered()) {
            // create a vision ambassador
            User user = userService.getUserById(response.userId());
            VisionAmbassador visionAmbassador = VisionAmbassador.builder()
                    .userId(user.getId().toString())
                    .createdAt(new Date())
                    .updatedAt(new Date())
                    .build();
            visionAmbassadorRepository.save(visionAmbassador);
        }
        return response;
    }

}
 
package com.nayonikaeyecare.api.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nayonikaeyecare.api.services.VisionAmbassadorService;
import com.nayonikaeyecare.api.services.UserService;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.nayonikaeyecare.api.dto.visionambassador.VisionAmbassadorRequest;
import com.nayonikaeyecare.api.dto.visionambassador.VisionAmbassadorResponse;
import com.nayonikaeyecare.api.entities.VisionAmbassador;
import com.nayonikaeyecare.api.entities.user.User;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@CrossOrigin(origins = {"http://localhost:3000","http://nayonika-user-management-dev-1511095685.ap-south-1.elb.amazonaws.com","http://nayonika-user-management-qa-580028363.ap-south-1.elb.amazonaws.com"})
@RestController
@RequestMapping("/api/vision-ambassadors")

@RequiredArgsConstructor

public class VisionAmbassadorController {

    @Autowired
    private VisionAmbassadorService visionAmbassadorService;

    @Autowired
    private UserService userService;





    @PostMapping("/addVisionAmbassador")
    @ResponseStatus(HttpStatus.CREATED)
    public void createVisionAmbassador(@RequestBody VisionAmbassadorRequest visionAmbassadorRequest) {
        visionAmbassadorService.createVisionAmbassador(visionAmbassadorRequest);
    }

    @GetMapping("/getAllVisionAmbassadors")
    @ResponseStatus(HttpStatus.OK)
    public List<VisionAmbassadorResponse> getAllVisionAmbassadors() {
        return visionAmbassadorService.getAllVisionAmbassadors();
    }

    @GetMapping("/{id}")
    public ResponseEntity<VisionAmbassadorResponse> getVisionAmbassadorById(@PathVariable String id) {
        return ResponseEntity.ok(visionAmbassadorService.getVisionAmbassadorById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VisionAmbassadorResponse> updateVisionAmbassador(
            @PathVariable String id,
            @Valid @RequestBody VisionAmbassadorRequest visionAmbassadorRequest) {
        VisionAmbassadorResponse updatedVisionAmbassador = visionAmbassadorService.updateVisionAmbassador(id,
                visionAmbassadorRequest);
        return ResponseEntity.ok(updatedVisionAmbassador);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVisionAmbassador(@PathVariable String id) {
        visionAmbassadorService.deleteVisionAmbassador(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<VisionAmbassadorResponse>> getPaginatedPatients(
            // @RequestParam(required = false) String state,
            // @RequestParam(required = false) String city,
            @RequestParam(required = false) String searchString,
            Pageable pageable) {
        return ResponseEntity.ok(visionAmbassadorService.filterVisionAmbassador(searchString,pageable));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<VisionAmbassadorResponse> findByUserId(@PathVariable String userId) {
        VisionAmbassador visionAmbassador = visionAmbassadorService.findByUserId(userId);
        if (visionAmbassador == null) {
            return ResponseEntity.notFound().build();
        }

        String actualUserId = visionAmbassador.getUserId();
        User user = userService.getUserById(actualUserId);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        VisionAmbassadorResponse dto = new VisionAmbassadorResponse();
        dto.setId(visionAmbassador.getId().toHexString());

        String firstName = user.getFirstName();
        String lastName = user.getLastName();
        String name = "";
        if (firstName != null && !firstName.isEmpty()) {
            name += firstName;
        }
        if (lastName != null && !lastName.isEmpty()) {
            if (!name.isEmpty()) {
                name += " ";
            }
            name += lastName;
        }
        dto.setName(name);

        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setUserId(visionAmbassador.getUserId());
        dto.setStatus(visionAmbassador.isStatus());
        dto.setCity(user.getCity());
        dto.setState(user.getState());
        dto.setLanguage(user.getLanguage());
        dto.setCreatedAt(visionAmbassador.getCreatedAt());
        dto.setUpdatedAt(visionAmbassador.getUpdatedAt());
        dto.setPatientCount(0); // Or null, as per requirement

        return ResponseEntity.ok(dto);
    }
}
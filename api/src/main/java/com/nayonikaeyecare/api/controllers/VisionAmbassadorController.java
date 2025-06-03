package com.nayonikaeyecare.api.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nayonikaeyecare.api.services.VisionAmbassadorService;

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

import com.nayonikaeyecare.api.dto.user.AuthenticationRequest;
import com.nayonikaeyecare.api.dto.user.AuthenticationResponse;
import com.nayonikaeyecare.api.dto.visionambassador.VisionAmbassadorRequest;
import com.nayonikaeyecare.api.dto.visionambassador.VisionAmbassadorResponse;
import com.nayonikaeyecare.api.entities.VisionAmbassador;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@CrossOrigin(origins = {"http://localhost:3000","http://nayonika-user-management-dev-1511095685.ap-south-1.elb.amazonaws.com","http://nayonika-user-management-qa-580028363.ap-south-1.elb.amazonaws.com"})
@RestController
@RequestMapping("/api/vision-ambassadors")

@RequiredArgsConstructor

public class VisionAmbassadorController {

    @Autowired
    private VisionAmbassadorService visionAmbassadorService;


    

    
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
    public ResponseEntity<VisionAmbassador> findByUserId(@PathVariable String userId) {
        VisionAmbassador ambassador = visionAmbassadorService.findByUserId(userId);
        if (ambassador != null) {
            return ResponseEntity.ok(ambassador);
        }
        return ResponseEntity.notFound().build();
    }
}
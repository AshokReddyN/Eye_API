package com.nayonikaeyecare.api.repositories.visionambassador;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.nayonikaeyecare.api.dto.visionambassador.VisionAmbassadorResponse;

public interface CustomVisionAmbassador {

    Page<VisionAmbassadorResponse> filterVisionAmbassador(String searchString,Pageable pageable);

}

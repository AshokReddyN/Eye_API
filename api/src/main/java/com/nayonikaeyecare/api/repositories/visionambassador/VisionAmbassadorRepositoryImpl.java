package com.nayonikaeyecare.api.repositories.visionambassador;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.nayonikaeyecare.api.dto.visionambassador.VisionAmbassadorResponse;
import com.nayonikaeyecare.api.entities.VisionAmbassador;
import com.nayonikaeyecare.api.mappers.VisionAmbassadorMapper;
import com.nayonikaeyecare.api.repositories.referral.ReferralRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

public class VisionAmbassadorRepositoryImpl implements CustomVisionAmbassador {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ReferralRepository referralRepository;

    @Override
    public Page<VisionAmbassadorResponse> filterVisionAmbassador(String state, String city,
            Pageable pageable) {

        Query query = new Query();

        if (state != null && !state.isEmpty()) {
            query.addCriteria(Criteria.where("state").is(state));
        }
        if (city != null && !city.isEmpty()) {
            query.addCriteria(Criteria.where("city").is(city));
        }

        query.with(pageable);
        List<VisionAmbassador> visionAmbassadorsEntities = mongoTemplate.find(query, VisionAmbassador.class);
        List<VisionAmbassadorResponse> visionAmbassadorResponses = new ArrayList<>();

        for (VisionAmbassador entity : visionAmbassadorsEntities) {
            VisionAmbassadorResponse responseDto = VisionAmbassadorMapper.mapToVisionAmbassadorResponse(entity);
            long patientCountLong = referralRepository.findByAmbassadorId(entity.getId()).size();
            responseDto.setPatientCount((int) patientCountLong);
            visionAmbassadorResponses.add(responseDto);
        }

        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), VisionAmbassador.class);
        return new PageImpl<>(visionAmbassadorResponses, pageable, total);

    }

}

package com.nayonikaeyecare.api.repositories.patient;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.nayonikaeyecare.api.dto.patient.PatientResponse;
import com.nayonikaeyecare.api.entities.Patient;
import com.nayonikaeyecare.api.mappers.PatientMapper;

public class PatientRepositoryImpl implements CustomPatientRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Page<PatientResponse> filterPatients(String state, String city, String name, String ambassadorId, Pageable pageable) {

        Query query = new Query();

        if (state != null && !state.isEmpty()) {
            query.addCriteria(Criteria.where("state").is(state));
        }
        if (city != null && !city.isEmpty()) {
            query.addCriteria(Criteria.where("city").is(city));
        }
        if (name != null) {
            String regexPattern = ".*" + Pattern.quote(name) + ".*";
            query.addCriteria(Criteria.where("name").regex(regexPattern, "i"));
        }
        if (ambassadorId != null && !ambassadorId.isEmpty()) {
            query.addCriteria(Criteria.where("ambassadorId").is(ambassadorId));
        }
       
        query.with(pageable);
        List<PatientResponse> patients = mongoTemplate.find(query, Patient.class).stream()
                .map(PatientMapper::mapToPatientResponse)
                .collect(Collectors.toList());
        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Patient.class);
        return new PageImpl<PatientResponse>(patients, pageable, total);
    }
}
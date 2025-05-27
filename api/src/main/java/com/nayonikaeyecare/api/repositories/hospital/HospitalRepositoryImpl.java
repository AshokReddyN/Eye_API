package com.nayonikaeyecare.api.repositories.hospital;

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

import com.nayonikaeyecare.api.mappers.HospitalMapper;
import com.nayonikaeyecare.api.dto.hospital.HospitalResponse;
import com.nayonikaeyecare.api.entities.Hospital;

public class HospitalRepositoryImpl implements CustomHospitalRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Page<HospitalResponse> filterHospitals(String state, List<String> cities, Boolean status, String name,
            List<String> serviceTypes,
            Pageable pageable) {

        Query query = new Query();

        if (state != null && !state.isEmpty()) {
            String stateRegex = ".*" + Pattern.quote(state) + ".*";
            query.addCriteria(Criteria.where("address.state").regex(stateRegex, "i"));
        }
        
        if (cities != null && !cities.isEmpty()) {
            List<Criteria> cityCriteria = cities.stream()
                    .map(city -> {
                        String cityRegex = ".*" + Pattern.quote(city) + ".*";
                        return Criteria.where("address.city").regex(cityRegex, "i");
                    })
                    .collect(Collectors.toList());
            query.addCriteria(new Criteria().orOperator(cityCriteria.toArray(new Criteria[0])));
        }
        if (status != null) {
            query.addCriteria(Criteria.where("status").is(status));
        }

        if (serviceTypes != null && !serviceTypes.isEmpty()) {
            List<Criteria> serviceCriteria = serviceTypes.stream()
                    .map(service -> {
                        String serviceRegex = ".*" + Pattern.quote(service) + ".*";
                        return Criteria.where("services").regex(serviceRegex, "i");
                    })
                    .collect(Collectors.toList());
            query.addCriteria(new Criteria().orOperator(serviceCriteria.toArray(new Criteria[0])));
        }

        if (name != null && !name.isEmpty()) {
            String nameRegex = ".*" + Pattern.quote(name) + ".*";
            query.addCriteria(Criteria.where("name").regex(nameRegex, "i"));
        }

        query.with(pageable);
        List<HospitalResponse> hospitals = mongoTemplate.find(query, Hospital.class).stream()
                .map(HospitalMapper::mapToHospitalResponse)
                .collect(Collectors.toList());
        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Hospital.class);
        return new PageImpl<HospitalResponse>(hospitals, pageable, total);
    }
}

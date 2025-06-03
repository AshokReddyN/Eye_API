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
    public Page<HospitalResponse> filterHospitals(String state, List<String> cities, Boolean status, String searchString,
            List<String> serviceTypes,
            Pageable pageable) {

        Query query = new Query();
        
        // Build two Criteria objects, each with its own AND logic and then combine them with OR logic 
        // This allows us to have different conditions for "name" and "coordinator" fields
        // while still applying the same filters for state, cities, status, and serviceTypes.
        Criteria criteria1 = new Criteria();
        Criteria criteria2 = new Criteria();

        if (state != null && !state.isEmpty()) {
            String stateRegex = ".*" + Pattern.quote(state) + ".*";
            criteria1=criteria1.and("address.state").regex(stateRegex, "i");
            criteria2=criteria2.and("address.state").regex(stateRegex, "i");
        }
        
        if (cities != null && !cities.isEmpty()) {
            List<Criteria> cityCriteria = cities.stream()
                    .map(city -> {
                        String cityRegex = ".*" + Pattern.quote(city) + ".*";
                        return Criteria.where("address.city").regex(cityRegex, "i");
                    })
                    .collect(Collectors.toList());
            // Instead of criteria1.add(...), use andOperator if you want to combine with other AND conditions:
            criteria1 = new Criteria().andOperator(
            criteria1, // existing criteria1 conditions (if any)
            new Criteria().orOperator(cityCriteria.toArray(new Criteria[0]))
            );
            criteria2 = new Criteria().andOperator(
            criteria2, // existing criteria1 conditions (if any)
            new Criteria().orOperator(cityCriteria.toArray(new Criteria[0]))
            );
        }
        if (status != null) {
            // query1.addCriteria(Criteria.where("status").is(status));
            criteria1=criteria1.and("status").is(status);
            criteria2=criteria2.and("status").is(status);
        }

        if (serviceTypes != null && !serviceTypes.isEmpty()) {
            // query1.addCriteria(Criteria.where("services").in(serviceTypes));
            // query2.addCriteria(Criteria.where("services").in(serviceTypes));
            criteria1=criteria1.and("services").in(serviceTypes);
            criteria2=criteria2.and("services").in(serviceTypes);
        }

        if (searchString != null && !searchString.isEmpty()) {
            String nameRegex = ".*" + Pattern.quote(searchString) + ".*";
            String coordinatorRegex = ".*" + Pattern.quote(searchString) + ".*";
            // query.addCriteria(new Criteria().orOperator(
            //         Criteria.where("name").regex(nameRegex, "i"),
            //         Criteria.where("coordinator").regex(coordinatorRegex, "i")
            // ));
            // query1.addCriteria(Criteria.where("name").regex(nameRegex, "i"));
            // query2.addCriteria(Criteria.where("coordinator").regex(coordinatorRegex, "i"));
            criteria1=criteria1.and("name").regex(nameRegex, "i");
            criteria2=criteria2.and("coordinator").regex(coordinatorRegex, "i");
        }

        
        query.addCriteria(new Criteria().orOperator(criteria1,criteria2));

        query.with(pageable);
        List<HospitalResponse> hospitals = mongoTemplate.find(query, Hospital.class).stream()
                .map(HospitalMapper::mapToHospitalResponse)
                .collect(Collectors.toList());
        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Hospital.class);
        return new PageImpl<HospitalResponse>(hospitals, pageable, total);
    }
}

package com.nayonikaeyecare.api.repositories.hospital;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors; // Re-added as it's used for cityCriteria

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.nayonikaeyecare.api.entities.Hospital;

public class HospitalRepositoryImpl implements CustomHospitalRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Page<Hospital> filterHospitals(String state, List<String> cities, Boolean status, String searchString,
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
            // Ensure we only add city criteria if there are any
            // Also, the original logic for combining criteria1/2 with cityCriteria might need a careful review
            // as it was creating new Criteria().andOperator(criteria1, new Criteria().orOperator(...))
            // which might not be what's intended if criteria1 already had conditions.
            // For this pass, trying to keep it closer to original structure but this area is complex.
            if (!cityCriteria.isEmpty()) {
                 Criteria cityOrCriteria = new Criteria().orOperator(cityCriteria.toArray(new Criteria[0]));
                 criteria1.andOperator(cityOrCriteria);
                 criteria2.andOperator(cityOrCriteria);
            }
        }
        if (status != null) {
            criteria1=criteria1.and("status").is(status);
            criteria2=criteria2.and("status").is(status);
        }

        if (serviceTypes != null && !serviceTypes.isEmpty()) {
            criteria1=criteria1.and("services").in(serviceTypes);
            criteria2=criteria2.and("services").in(serviceTypes);
        }

        if (searchString != null && !searchString.isEmpty()) {
            String nameRegex = ".*" + Pattern.quote(searchString) + ".*";
            String coordinatorRegex = ".*" + Pattern.quote(searchString) + ".*";
            criteria1=criteria1.and("name").regex(nameRegex, "i");
            criteria2=criteria2.and("coordinator").regex(coordinatorRegex, "i");
        }
        
        // Only add criteria to the query if they have had conditions added to them.
        // An empty criteria object can cause issues.
        if (criteria1.getCriteriaObject().size() > 0 || criteria2.getCriteriaObject().size() > 0) {
            query.addCriteria(new Criteria().orOperator(criteria1,criteria2));
        }


        query.with(pageable);
        // New line to get List<Hospital>:
        List<Hospital> hospitals = mongoTemplate.find(query, Hospital.class);
        
        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Hospital.class);
        
        // New return:
        return new PageImpl<Hospital>(hospitals, pageable, total);
    }
}

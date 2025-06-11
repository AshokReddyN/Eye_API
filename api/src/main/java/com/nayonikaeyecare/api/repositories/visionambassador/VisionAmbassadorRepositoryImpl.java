package com.nayonikaeyecare.api.repositories.visionambassador;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.nayonikaeyecare.api.dto.visionambassador.VisionAmbassadorResponse;
import com.nayonikaeyecare.api.entities.VisionAmbassador;
import com.nayonikaeyecare.api.entities.user.User;
import com.nayonikaeyecare.api.mappers.VisionAmbassadorMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import java.util.regex.Pattern;
import org.bson.types.ObjectId;

public class VisionAmbassadorRepositoryImpl implements CustomVisionAmbassador {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Page<VisionAmbassadorResponse> filterVisionAmbassador(String searchString, Pageable pageable) {
        Query query = new Query();
    
       // Build criteria list
       List<Criteria> criteriaList = new java.util.ArrayList<>();
       criteriaList.add(Criteria.where("firstName").ne(null));
       criteriaList.add(Criteria.where("firstName").ne(""));
       criteriaList.add(Criteria.where("firstName").ne("null "));
    
       if (searchString != null && !searchString.isEmpty()) {
           String regex = ".*" + Pattern.quote(searchString) + ".*";
           Criteria searchCriteria = new Criteria().orOperator(
               Criteria.where("firstName").regex(regex, "i"),
               Criteria.where("lastName").regex(regex, "i"),
               Criteria.where("city").regex(regex, "i"),
               Criteria.where("state").regex(regex, "i")
           );
           criteriaList.add(searchCriteria);
       }
       // Combine all criteria with andOperator
       query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
       query.with(pageable);
       List<VisionAmbassadorResponse> visionAmbassadors = mongoTemplate.find(query, User.class)
               .stream()
               .map(VisionAmbassadorMapper::mapToVisionAmbassadorResponse)
               .collect(Collectors.toList());
       // Set patientCount for each ambassador
       for (VisionAmbassadorResponse va  : visionAmbassadors) {
           ObjectId ambassadorObjectId = new ObjectId(va.getId());
           long patientCount = mongoTemplate.count(
                   Query.query(Criteria.where("ambassadorId").is(ambassadorObjectId)),
                   "referrals"
           );
           va.setPatientCount((int) patientCount);
       }
       long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), User.class);
       return new PageImpl<>(visionAmbassadors, pageable, total);
    }
     

//     @Override
// public Page<VisionAmbassadorResponse> filterVisionAmbassador(String state, String city, String searchString, Pageable pageable) {
//     Query query = new Query();

//     if (state != null && !state.isEmpty()) {
//         query.addCriteria(Criteria.where("state").is(state));
//     }
//     if (city != null && !city.isEmpty()) {
//         query.addCriteria(Criteria.where("city").is(city));
//     }
//     if (searchString != null && !searchString.isEmpty()) {
//         query.addCriteria(new Criteria().orOperator(
//             Criteria.where("name").regex(searchString, "i"),
//             Criteria.where("email").regex(searchString, "i")
//         ));
//     }

//     query.with(pageable);

//     List<User> users = mongoTemplate.find(query, User.class);

//     // Map User to VisionAmbassadorResponse (make sure your mapper supports this)
//     List<VisionAmbassadorResponse> responses = users.stream()
//         .map(VisionAmbassadorMapper::mapToVisionAmbassadorResponse)
//         .collect(Collectors.toList());

//     long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), User.class);

//     return new PageImpl<>(responses, pageable, total);
// }

}

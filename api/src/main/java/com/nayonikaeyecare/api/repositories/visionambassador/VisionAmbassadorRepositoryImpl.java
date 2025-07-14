package com.nayonikaeyecare.api.repositories.visionambassador;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.aggregation.AddFieldsOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationSpELExpression;

import com.nayonikaeyecare.api.entities.VisionAmbassador;

import org.springframework.beans.factory.annotation.Autowired;
import org.bson.types.ObjectId;

public class VisionAmbassadorRepositoryImpl implements CustomVisionAmbassador {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Page<VisionAmbassador> filterVisionAmbassador(String searchString, Pageable pageable) {
        // Build aggregation pipeline
        AddFieldsOperation addFields = Aggregation.addFields()
            .addFieldWithValue("userIdObjectId", AggregationSpELExpression.expressionOf("toObjectId(userId)"))
            .build();
            
        LookupOperation lookupUser = LookupOperation.newLookup()
            .from("users")
            .localField("userIdObjectId")
            .foreignField("_id")
            .as("userDetails");
        
        UnwindOperation unwindUser = Aggregation.unwind("$userDetails", true);
        
        MatchOperation matchUserExists = Aggregation.match(Criteria.where("userDetails").ne(null));
        MatchOperation matchFirstNameExists = Aggregation.match(Criteria.where("userDetails.firstName").ne(null));
        
        MatchOperation searchMatch = null;
        if (searchString != null && !searchString.isEmpty()) {
            String regex = ".*" + Pattern.quote(searchString) + ".*";
            searchMatch = Aggregation.match(new Criteria().orOperator(
                Criteria.where("userDetails.firstName").regex(regex, "i"),
                Criteria.where("userDetails.lastName").regex(regex, "i"),
                Criteria.where("userDetails.city").regex(regex, "i")
            ));
        }
        
        Aggregation aggregation = Aggregation.newAggregation(
            addFields,
            lookupUser,
            unwindUser,
            matchUserExists,
            matchFirstNameExists,
            searchMatch != null ? searchMatch : Aggregation.match(new Criteria()),
            Aggregation.sort(Sort.Direction.DESC, "updatedAt"),
            Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()),
            Aggregation.limit(pageable.getPageSize())
        );
        
        AggregationResults<org.bson.Document> results = mongoTemplate.aggregate(aggregation, "vision_ambassadors", org.bson.Document.class);
        List<org.bson.Document> documents = results.getMappedResults();
        
        // Manually map Document to VisionAmbassador
        List<VisionAmbassador> visionAmbassadors = documents.stream()
            .map(doc -> {
                VisionAmbassador va = new VisionAmbassador();
                va.setId(doc.getObjectId("_id"));
                va.setUserId(doc.getString("userId"));
                
                // Get userDetails from the document
                org.bson.Document userDetails = doc.get("userDetails", org.bson.Document.class);
                if (userDetails != null) {
                    String firstName = userDetails.getString("firstName");
                    String lastName = userDetails.getString("lastName");
                    String fullName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
                    va.setName(fullName.trim());
                    va.setUserId(userDetails.getObjectId("_id").toHexString());
                    va.setCity(userDetails.getString("city"));
                    va.setState(userDetails.getString("state"));
                    va.setPhoneNumber(userDetails.getString("phoneNumber"));
                    va.setLanguage(userDetails.getString("language"));
                }
                
                va.setStatus(doc.getBoolean("status", false));
                va.setCreatedAt(doc.getDate("createdAt"));
                va.setUpdatedAt(doc.getDate("updatedAt"));
                return va;
            })
            .collect(java.util.stream.Collectors.toList());
        
        // For total count
        Aggregation countAggregation = Aggregation.newAggregation(
            addFields,
            lookupUser,
            unwindUser,
            matchUserExists,
            matchFirstNameExists,
            searchMatch != null ? searchMatch : Aggregation.match(new Criteria()),
            Aggregation.count().as("total")
        );
        
        AggregationResults<org.bson.Document> countResults = mongoTemplate.aggregate(countAggregation, "vision_ambassadors", org.bson.Document.class);
        long total = 0;
        if (countResults.getUniqueMappedResult() != null) {
            total = countResults.getUniqueMappedResult().getInteger("total", 0);
        }
        
        return new PageImpl<>(visionAmbassadors, pageable, total);
    }

    // The commented out method below can be removed if no longer relevant.
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
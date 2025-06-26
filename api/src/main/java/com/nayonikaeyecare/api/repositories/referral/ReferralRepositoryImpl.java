package com.nayonikaeyecare.api.repositories.referral;

import java.util.List;
import java.util.regex.Pattern;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;
import com.nayonikaeyecare.api.entities.Referral;
import com.nayonikaeyecare.api.entities.Status;
import com.nayonikaeyecare.api.repositories.patient.CustomReferralRepository;

@Repository
public class ReferralRepositoryImpl implements CustomReferralRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    // Implement these methods if needed, or remove them if not used.
    public List<Referral> findByPatientId(String patientId) {
        throw new UnsupportedOperationException("Unimplemented method 'findByPatientId'");
    }

    public List<Referral> findByPatientIdAndHospitalId(String patientId, String hospitalId) {
        throw new UnsupportedOperationException("Unimplemented method 'findByPatientIdAndHospitalId'");
    }

    @Override
    public Page<Referral> filterReferrals(ObjectId ambassadorId, String state, String city,
            Boolean status, String name, String searchString, Pageable pageable) {

        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        // Test with just the search string first
        if (searchString != null && !searchString.trim().isEmpty()) {
            String searchTerm = searchString.trim();

            String regex = ".*" + Pattern.quote(searchTerm) + ".*";

            Criteria searchCriteria = new Criteria().orOperator(
                    Criteria.where("patientName").regex(regex, "i"),
                    Criteria.where("hospitalName").regex(regex, "i"),
                    Criteria.where("city").regex(regex, "i"),
                    Criteria.where("state").regex(regex, "i"));

            criteriaList.add(searchCriteria);
        }

        // Add other filters
        if (ambassadorId != null && !ambassadorId.toHexString().isEmpty()) {
            criteriaList.add(Criteria.where("ambassadorId").is(ambassadorId));
        }

        // Combine criteria
        if (!criteriaList.isEmpty()) {
            Criteria finalCriteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));
            query.addCriteria(finalCriteria);
        }

        query.with(pageable);
        // Execute query
        List<Referral> referrals = mongoTemplate.find(query, Referral.class);

        // Get total count
        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Referral.class);

        // Test simple query if no results
        if (referrals.isEmpty() && searchString != null && !searchString.trim().isEmpty()) {

            // Test total documents
            long totalDocs = mongoTemplate.count(new Query(), Referral.class);

            // Test simple search
            Query simpleQuery = Query.query(Criteria.where("patientName").regex(searchString.trim(), "i"));
            List<Referral> simpleResults = mongoTemplate.find(simpleQuery, Referral.class);
        }

        return new PageImpl<>(referrals, pageable, total);
    }

    @Override
    public void updateStatusByIds(List<String> ids, Status newStatus) {
        throw new UnsupportedOperationException("Unimplemented method 'updateStatusByIds'");
    }
}

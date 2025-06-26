package com.nayonikaeyecare.api.repositories.patient;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;

import com.nayonikaeyecare.api.entities.Referral;
import com.nayonikaeyecare.api.entities.Status;

public interface CustomReferralRepository {

    @Query(value = "{ '_id': { $in: ?0 } }")
    void updateStatusByIds(List<String> ids, Status newStatus);

    Page<Referral> filterReferrals(ObjectId ambassadorId, String state, String city,
            Boolean status, String name, String searchString, Pageable pageable);
}
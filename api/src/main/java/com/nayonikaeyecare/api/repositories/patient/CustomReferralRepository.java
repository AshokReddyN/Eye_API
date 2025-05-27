package com.nayonikaeyecare.api.repositories.patient;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;

import com.nayonikaeyecare.api.entities.Status;

public interface CustomReferralRepository {
        
        @Query(value = "{ '_id': { $in: ?0 } }")
        void updateStatusByIds(List<String> ids, Status newStatus);
}
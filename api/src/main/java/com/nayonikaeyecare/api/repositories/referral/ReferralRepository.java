package com.nayonikaeyecare.api.repositories.referral;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Sort;
import com.nayonikaeyecare.api.entities.Referral;
import com.nayonikaeyecare.api.repositories.patient.CustomReferralRepository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ReferralRepository extends MongoRepository<Referral, ObjectId>, CustomReferralRepository {

    List<Referral> findByAmbassadorId(ObjectId ambassadorId);

    List<Referral> findByAmbassadorId(ObjectId ambassadorId, Sort sort);

    List<Referral> findByHospitalId(ObjectId hospitalId);

    List<Referral> findByPatientId(ObjectId patientId);

    Page<Referral> findByHospitalId(ObjectId hospitalId, Pageable pageable);

    List<Referral> findByPatientNameAndHospitalId(String patientName, ObjectId hospitalId);

    long countByHospitalId(ObjectId hospitalId);

    long countByHospitalIdAndIsSpectacleRequestedTrue(ObjectId hospitalId);

    List<Referral> findAllByIdIn(List<ObjectId> ids);

    Optional<Referral> findFirstByPatientIdAndHospitalIdOrderByCreatedAtDesc(ObjectId patientId, ObjectId hospitalId);
}

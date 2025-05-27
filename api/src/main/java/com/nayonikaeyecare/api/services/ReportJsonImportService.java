package com.nayonikaeyecare.api.services;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.WriteModel;
import com.nayonikaeyecare.api.dto.PatientReportRequestDto;
import com.nayonikaeyecare.api.entities.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportJsonImportService {

    private final MongoTemplate mongoTemplate;
    private static final int BATCH_SIZE = 1000;

    @Async
    public CompletableFuture<BulkWriteResult> importReportsFromJson(List<PatientReportRequestDto> reports) {
        try {
            return processBatch(reports);
        } catch (Exception e) {
            log.error("Failed to process JSON data", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    private CompletableFuture<BulkWriteResult> processBatch(List<PatientReportRequestDto> reports) {
        List<PatientReportRequestDto> batch = new ArrayList<>(BATCH_SIZE);
        BulkWriteResult finalResult = null;

        for (int i = 0; i < reports.size(); i++) {
            batch.add(reports.get(i));

            if (batch.size() >= BATCH_SIZE || i == reports.size() - 1) {
                finalResult = updateReportsInBatch(batch);
                batch.clear();
            }
        }

        return CompletableFuture.completedFuture(finalResult);
    }

    private BulkWriteResult updateReportsInBatch(List<PatientReportRequestDto> records) {
        List<Referral> reportsToInsert = new ArrayList<>();
        List<WriteModel<Document>> bulkOperations = new ArrayList<>();

        for (PatientReportRequestDto record : records) {
            if (record.getId() == null) {
                reportsToInsert.add(createNewReport(record));
            } else {
                bulkOperations.add(createUpdateOperation(record));
            }
        }

        return executeBulkOperations(reportsToInsert, bulkOperations);
    }

    private BulkWriteResult executeBulkOperations(List<Referral> reportsToInsert,
            List<WriteModel<Document>> bulkOperations) {
        // Insert new reports
        if (!reportsToInsert.isEmpty()) {
            mongoTemplate.insertAll(reportsToInsert);
        }

        // Execute bulk update operations
        if (!bulkOperations.isEmpty()) {
            MongoCollection<Document> collection = mongoTemplate.getCollection("report");
            return collection.bulkWrite(bulkOperations, new BulkWriteOptions().ordered(false));
        }

        try {
            // Return a dummy result if no operations were performed
            // return BulkWriteResult.unacknowledged();

            // Return acknowledged result with 0 modifications instead of unacknowledged
            return BulkWriteResult.acknowledged(0, 0, 0, 0,
                    new ArrayList<>(), new ArrayList<>());
        } catch (Exception e) {
            log.error("Failed to execute bulk operations", e);
            throw new RuntimeException("Failed to process reports: " + e.getMessage());
        }
    }

    private Referral createNewReport(PatientReportRequestDto record) {
        return Referral.builder()
                .treatment(record.getTreatment())
                .rightEye(createEyeDetails(record.getRightSph(), record.getRightCyl().toString(),
                        record.getRightAxis().toString()))
                .leftEye(createEyeDetails(record.getLeftSph(), record.getLeftCyl().toString(),
                        record.getLeftAxis().toString()))
                .patientName(record.getPatientName())
                .ageRange(record.getAgeRange())
                .hospitalName(record.getHospitalName())
                .city(record.getCity())
                .state(record.getState())
                .createdAt(new Date())
                .updatedAt(new Date())
                .remarks(record.getRemarks())
                .status(Status.COMPLETED)
                .build();
    }

    private UpdateOneModel<Document> createUpdateOperation(PatientReportRequestDto record) {
        Document query = new Document("_id", new ObjectId(record.getId()));
        Document update = new Document("$set", new Document()
                .append("treatment", record.getTreatment())
                .append("rightEye",
                        createEyeDetailsDocument(record.getRightSph(), record.getRightCyl().toString(),
                                record.getRightAxis().toString()))
                .append("leftEye",
                        createEyeDetailsDocument(record.getLeftSph(), record.getLeftCyl().toString(),
                                record.getLeftAxis().toString()))
                .append("patientName", record.getPatientName())
                .append("ageRange", record.getAgeRange())
                .append("hospitalName", record.getHospitalName())
                .append("city", record.getCity())
                .append("state", record.getState())
                .append("updatedAt", new Date())
                .append("remarks", record.getRemarks())
                .append("status", Status.COMPLETED.toString()));
        UpdateOptions options = new UpdateOptions().upsert(true);
        return new UpdateOneModel<>(query, update, options);
    }

    private Document createEyeDetailsDocument(String sph, String cyl, String axis) {
        return new Document()
                .append("sph", sph)
                .append("cyl", cyl)
                .append("axis", axis);
    }

    private EyeDetails createEyeDetails(String sph, String cyl, String axis) {
        return EyeDetails.builder()
                .sph(sph)
                .cyl(cyl)
                .axis(axis)
                .build();
    }
}
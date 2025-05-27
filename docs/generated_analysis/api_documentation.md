# API Documentation

This document provides details for the REST endpoints available in the application.

## Hospital Controller (`com.nayonikaeyecare.api.controllers.HospitalController`)

Base Path: `/api/hospitals`

---

### 1. Create Hospital
- **HTTP Method and Path:** `POST /api/hospitals`
- **Controller Method:** `createHospital`
- **Request Parameters:** None
- **Request Body:** `HospitalRequest`
    - Key fields: (Refer to `HospitalRequest` DTO definition for details - e.g., name, address, services, status)
- **Response Body:** `HospitalResponse`
    - Key fields: (Refer to `HospitalResponse` DTO definition - e.g., id, name, address, services, status)
- **Business Logic Summary:** Creates a new hospital record based on the provided request data.
- **Authorization/Security:** Public.

---

### 2. Get All Hospitals
- **HTTP Method and Path:** `GET /api/hospitals`
- **Controller Method:** `getAllHospitals`
- **Request Parameters:** None
- **Request Body:** None
- **Response Body:** `List<HospitalResponse>`
    - Each `HospitalResponse` contains details of a hospital.
- **Business Logic Summary:** Retrieves a list of all hospital records.
- **Authorization/Security:** Public.

---

### 3. Get Hospital By ID
- **HTTP Method and Path:** `GET /api/hospitals/{id}`
- **Controller Method:** `getHospitalById`
- **Request Parameters:**
    - Path Variable: `id` (String) - The MongoDB ObjectId of the hospital.
- **Request Body:** None
- **Response Body:** `ResponseEntity<HospitalResponse>`
    - Contains `HospitalResponse` on success (200 OK).
    - Returns 400 Bad Request if the ID is not a valid ObjectId.
- **Business Logic Summary:** Retrieves a specific hospital record by its unique ID.
- **Authorization/Security:** Public.

---

### 4. Delete Hospital
- **HTTP Method and Path:** `DELETE /api/hospitals/{id}`
- **Controller Method:** `deleteHospital`
- **Request Parameters:**
    - Path Variable: `id` (String) - The MongoDB ObjectId of the hospital.
- **Request Body:** None
- **Response Body:** `ResponseEntity<String>`
    - Success: "Hospital deleted successfully with id: {id}" (200 OK).
    - Error: "Invalid ObjectId: {id}" (400 Bad Request).
- **Business Logic Summary:** Deletes a specific hospital record by its unique ID.
- **Authorization/Security:** Public.

---

### 5. Update Hospital
- **HTTP Method and Path:** `PUT /api/hospitals/{id}`
- **Controller Method:** `updateHospital`
- **Request Parameters:**
    - Path Variable: `id` (String) - The MongoDB ObjectId of the hospital to update.
- **Request Body:** `HospitalRequest`
    - Contains the updated hospital data.
- **Response Body:** `ResponseEntity<HospitalResponse>`
    - Contains the updated `HospitalResponse` on success (200 OK).
    - Returns 400 Bad Request if the ID is not a valid ObjectId.
- **Business Logic Summary:** Updates an existing hospital record with the provided data.
- **Authorization/Security:** Public.

---

### 6. Get Paginated and Filtered Hospitals
- **HTTP Method and Path:** `GET /api/hospitals/filter`
- **Controller Method:** `getPaginatedHospitals`
- **Request Parameters:**
    - Query Param: `state` (String, optional) - Filter by state.
    - Query Param: `cities` (`List<String>`, optional) - Filter by a list of cities.
    - Query Param: `status` (Boolean, optional) - Filter by status.
    - Query Param: `name` (String, optional) - Filter by hospital name (likely a partial match).
    - Query Param: `services` (`List<String>`, optional) - Filter by a list of services offered.
    - Query Param: `pageable` (Spring Data `Pageable` object) - Controls pagination (e.g., `page`, `size`, `sort`).
- **Request Body:** None
- **Response Body:** `ResponseEntity<Page<HospitalResponse>>`
    - Contains a paginated list of `HospitalResponse` objects matching the filter criteria.
- **Business Logic Summary:** Retrieves hospitals with pagination and filtering capabilities based on various criteria.
- **Authorization/Security:** Public.

---

### 7. Bulk Upload Hospitals
- **HTTP Method and Path:** `POST /api/hospitals/bulk-upload`
- **Controller Method:** `bulkUploadHospitals`
- **Request Parameters:** None
- **Request Body:** `List<HospitalRequest>`
    - A list of hospital records to be created.
- **Response Body:** `ResponseEntity<String>`
    - Success: "Successfully uploaded and saved {count} hospitals" (200 OK).
    - Error: "No Hospital data provided" (400 Bad Request if list is empty).
    - Error: "Invalid data: {error_message}" (400 Bad Request for other processing errors).
- **Business Logic Summary:** Allows for creating multiple hospital records in a single request.
- **Authorization/Security:** Public.

---

## Patient Controller (`com.nayonikaeyecare.api.controllers.PatientController`)

Base Path: `/api/patient`

---

### 1. Create Patient
- **HTTP Method and Path:** `POST /api/patient/addPatient`
- **Controller Method:** `createPatient`
- **Request Parameters:** None
- **Request Body:** `PatientRequest`
    - Key fields: (Refer to `PatientRequest` DTO for details - e.g., name, age, gender, contact, address, guardian details, eye details, ambassador ID)
- **Response Body:** `ResponseEntity<PatientResponse>` (Status 201 CREATED)
    - Contains `PatientResponse` with created patient details.
- **Business Logic Summary:** Creates a new patient record.
- **Authorization/Security:** Public.

---

### 2. Get Patient By ID
- **HTTP Method and Path:** `GET /api/patient/{id}`
- **Controller Method:** `getPatientsById`
- **Request Parameters:**
    - Path Variable: `id` (String) - The MongoDB ObjectId of the patient.
- **Request Body:** None
- **Response Body:** `ResponseEntity<PatientResponse>`
    - Contains `PatientResponse` on success (200 OK).
    - Returns 400 Bad Request if ID is invalid ObjectId format.
- **Business Logic Summary:** Retrieves a specific patient by their ID.
- **Authorization/Security:** Public.

---

### 3. Get All Patients
- **HTTP Method and Path:** `GET /api/patient/getAllPatients`
- **Controller Method:** `getAllPatients`
- **Request Parameters:** None
- **Request Body:** None
- **Response Body:** `ResponseEntity<List<PatientResponse>>`
    - Contains a list of all `PatientResponse` objects.
- **Business Logic Summary:** Retrieves all patient records.
- **Authorization/Security:** Public.

---

### 4. Get Patients By Ambassador ID
- **HTTP Method and Path:** `GET /api/patient/ambassador/{id}`
- **Controller Method:** `getPatientsByAmbassadorId`
- **Request Parameters:**
    - Path Variable: `id` (String) - The ID of the Vision Ambassador.
- **Request Body:** None
- **Response Body:** `ResponseEntity<List<PatientResponse>>`
    - Contains a list of `PatientResponse` objects associated with the given ambassador ID.
- **Business Logic Summary:** Retrieves all patients associated with a specific Vision Ambassador.
- **Authorization/Security:** Public.

---

### 5. Update Patient
- **HTTP Method and Path:** `PUT /api/patient/{id}`
- **Controller Method:** `updatePatient`
- **Request Parameters:**
    - Path Variable: `id` (String) - The MongoDB ObjectId of the patient to update.
- **Request Body:** `PatientRequest`
    - Contains the updated patient data.
- **Response Body:** `ResponseEntity<PatientResponse>`
    - Contains the updated `PatientResponse` on success (200 OK).
    - Returns 400 Bad Request if ID is invalid ObjectId format.
- **Business Logic Summary:** Updates an existing patient's information.
- **Authorization/Security:** Public.

---

### 6. Delete Patient
- **HTTP Method and Path:** `DELETE /api/patient/{id}`
- **Controller Method:** `deletePatient`
- **Request Parameters:**
    - Path Variable: `id` (String) - The MongoDB ObjectId of the patient to delete.
- **Request Body:** None
- **Response Body:** `ResponseEntity<Void>`
    - Returns 204 No Content on successful deletion.
    - Returns 400 Bad Request if ID is invalid ObjectId format.
- **Business Logic Summary:** Deletes a patient record by their ID.
- **Authorization/Security:** Public.

---

### 7. Get Patient Reports (Referrals)
- **HTTP Method and Path:** `GET /api/patient/reports`
- **Controller Method:** `getPatientReports`
- **Request Parameters:**
    - Query Param: `id` (String) - The MongoDB ObjectId of the patient.
    - Query Param: `hospitalId` (String, optional) - Filter reports by a specific hospital ID.
- **Request Body:** None
- **Response Body:** `ResponseEntity<List<Referral>>`
    - Contains a list of `Referral` entities associated with the patient.
    - Returns 204 No Content if no reports are found.
    - Returns 400 Bad Request if patient ID is invalid.
    - Returns 500 Internal Server Error for other exceptions.
- **Business Logic Summary:** Retrieves referral reports for a given patient, optionally filtered by hospital.
- **Authorization/Security:** Public.

---

### 8. Get Paginated and Filtered Patients
- **HTTP Method and Path:** `GET /api/patient/filter`
- **Controller Method:** `getPaginatedPatients`
- **Request Parameters:**
    - Query Param: `state` (String, optional) - Filter by state.
    - Query Param: `city` (String, optional) - Filter by city.
    - Query Param: `name` (String, optional) - Filter by patient name.
    - Query Param: `ambassadorId` (String, optional) - Filter by associated Vision Ambassador ID.
    - Query Param: `pageable` (Spring Data `Pageable` object) - Controls pagination.
- **Request Body:** None
- **Response Body:** `ResponseEntity<Page<PatientResponse>>`
    - Contains a paginated list of `PatientResponse` objects matching criteria.
- **Business Logic Summary:** Retrieves patients with pagination and filtering based on various criteria.
- **Authorization/Security:** Public.

---

## Patient Report Controller (`com.nayonikaeyecare.api.controllers.PatientReportController`)

Base Path: `/api/reports`

---

### 1. Import Patient Reports from JSON
- **HTTP Method and Path:** `POST /api/reports/upload`
- **Controller Method:** `importReportsJson`
- **Request Parameters:** None
- **Request Body:** `List<PatientReportRequestDto>`
    - Each `PatientReportRequestDto` contains data for a single report. (Refer to DTO for specific fields).
- **Response Body:** `ResponseEntity<String>`
    - Success: "JSON import completed. Modified: {modifiedCount}, Matched: {matchedCount}" (200 OK).
    - Error: "Reports cannot be empty" (400 Bad Request).
    - Error: "Failed to import JSON: {error_message}" (500 Internal Server Error).
- **Business Logic Summary:** Imports patient reports from a JSON payload. The service likely matches and updates existing reports or creates new ones. This operation is asynchronous.
- **Authorization/Security:** Public.

---

### 2. Update Patient Report Status
- **HTTP Method and Path:** `PUT /api/reports/updateStatus/{status}`
- **Controller Method:** `updatePatientStatus`
- **Request Parameters:**
    - Path Variable: `status` (`Status` enum) - The new status to set for the reports (e.g., SCREENED, REFERRED, COMPLETED).
- **Request Body:** `List<String>`
    - A list of report IDs (presumably MongoDB ObjectIds as Strings) whose status needs to be updated.
- **Response Body:** `ResponseEntity<String>`
    - Success: "Reports added successfully" (200 OK). (Note: Message might be slightly misleading, it's updating status).
    - Error: "Report IDs cannot be empty" (400 Bad Request).
    - Error: "Failed to update status: {error_message}" (500 Internal Server Error).
- **Business Logic Summary:** Updates the status for a list of patient reports.
- **Authorization/Security:** Public.

---

## Referral Controller (`com.nayonikaeyecare.api.controllers.ReferralController`)

Base Path: `/api/referrals`

---

### 1. Create Referral
- **HTTP Method and Path:** `POST /api/referrals`
- **Controller Method:** `createReferral`
- **Request Parameters:** None
- **Request Body:** `ReferralRequest`
    - Key fields: (Refer to `ReferralRequest` DTO for details - e.g., patientId, hospitalId, ambassadorId, notes, status)
- **Response Body:** `ReferralResponse` (Status 201 CREATED)
    - Contains `ReferralResponse` with created referral details.
- **Business Logic Summary:** Creates a new referral record.
- **Authorization/Security:** Public.

---

### 2. Get All Referrals
- **HTTP Method and Path:** `GET /api/referrals`
- **Controller Method:** `getAllReferrals`
- **Request Parameters:** None
- **Request Body:** None
- **Response Body:** `List<ReferralResponse>` (Status 200 OK)
    - Contains a list of all `ReferralResponse` objects.
- **Business Logic Summary:** Retrieves all referral records.
- **Authorization/Security:** Public.

---

### 3. Get Referral By ID
- **HTTP Method and Path:** `GET /api/referrals/{id}`
- **Controller Method:** `getReferralById`
- **Request Parameters:**
    - Path Variable: `id` (String) - The MongoDB ObjectId of the referral.
- **Request Body:** None
- **Response Body:** `ResponseEntity<ReferralResponse>`
    - Contains `ReferralResponse` on success (200 OK).
    - Returns 400 Bad Request if ID is invalid ObjectId format, body will be null.
- **Business Logic Summary:** Retrieves a specific referral by its ID.
- **Authorization/Security:** Public.

---

### 4. Get Referrals By Ambassador ID
- **HTTP Method and Path:** `GET /api/referrals/ambassador/{id}`
- **Controller Method:** `getReferralsByAmbassadorId`
- **Request Parameters:**
    - Path Variable: `id` (String) - The ID of the Vision Ambassador.
- **Request Body:** None
- **Response Body:** `ResponseEntity<List<ReferralResponse>>`
    - Contains a list of `ReferralResponse` objects associated with the given ambassador ID.
- **Business Logic Summary:** Retrieves all referrals linked to a specific Vision Ambassador.
- **Authorization/Security:** Public.

---

### 5. Get Referrals By Hospital ID
- **HTTP Method and Path:** `GET /api/referrals/hospital/{id}`
- **Controller Method:** `getReferralsByHospitalId`
- **Request Parameters:**
    - Path Variable: `id` (String) - The ID of the Hospital.
- **Request Body:** None
- **Response Body:** `ResponseEntity<List<ReferralResponse>>`
    - Contains a list of `ReferralResponse` objects associated with the given hospital ID.
- **Business Logic Summary:** Retrieves all referrals linked to a specific Hospital.
- **Authorization/Security:** Public.

---

### 6. Get Referrals By Hospital ID (Paginated)
- **HTTP Method and Path:** `GET /api/referrals/hospital/{id}/paginated`
- **Controller Method:** `getReferralsByHospitalIdPaginated`
- **Request Parameters:**
    - Path Variable: `id` (String) - The ID of the Hospital.
    - Query Param: `pageable` (Spring Data `Pageable` object) - Controls pagination.
- **Request Body:** None
- **Response Body:** `ResponseEntity<Page<ReferralResponse>>`
    - Contains a paginated list of `ReferralResponse` objects associated with the given hospital ID.
- **Business Logic Summary:** Retrieves referrals linked to a specific Hospital, with pagination.
- **Authorization/Security:** Public.

---

### 7. Get Referrals By Patient ID
- **HTTP Method and Path:** `GET /api/referrals/patient/{id}`
- **Controller Method:** `getReferralsByPatientId`
- **Request Parameters:**
    - Path Variable: `id` (String) - The ID of the Patient.
- **Request Body:** None
- **Response Body:** `ResponseEntity<List<ReferralResponse>>`
    - Contains a list of `ReferralResponse` objects associated with the given patient ID.
- **Business Logic Summary:** Retrieves all referrals linked to a specific Patient.
- **Authorization/Security:** Public.

---

### 8. Get Paginated and Filtered Referrals
- **HTTP Method and Path:** `GET /api/referrals/filter`
- **Controller Method:** `getPaginatedReferrals`
- **Request Parameters:**
    - Query Param: `ambassadorId` (ObjectId, optional) - Filter by Vision Ambassador ID.
    - Query Param: `state` (String, optional) - Filter by state.
    - Query Param: `city` (String, optional) - Filter by city.
    - Query Param: `status` (Boolean, optional) - Filter by referral status.
    - Query Param: `name` (String, optional) - Filter by name (likely patient name associated with referral).
    - Query Param: `pageable` (Spring Data `Pageable` object) - Controls pagination.
- **Request Body:** None
- **Response Body:** `ResponseEntity<Page<ReferralResponse>>`
    - Contains a paginated list of `ReferralResponse` objects matching criteria.
- **Business Logic Summary:** Retrieves referrals with pagination and filtering based on various criteria.
- **Authorization/Security:** Public.

---

### 9. Delete Referral
- **HTTP Method and Path:** `DELETE /api/referrals/{id}`
- **Controller Method:** `deleteReferral`
- **Request Parameters:**
    - Path Variable: `id` (String) - The MongoDB ObjectId of the referral to delete.
- **Request Body:** None
- **Response Body:** `ResponseEntity<String>`
    - Success: "Referral deleted successfully with id: {id}" (200 OK).
    - Error: "Invalid ObjectId: {id}" (400 Bad Request).
- **Business Logic Summary:** Deletes a referral record by its ID.
- **Authorization/Security:** Public.

---

### 10. Update Referral
- **HTTP Method and Path:** `PUT /api/referrals/{id}`
- **Controller Method:** `updateReferral`
- **Request Parameters:**
    - Path Variable: `id` (String) - The MongoDB ObjectId of the referral to update.
- **Request Body:** `ReferralRequest`
    - Contains the updated referral data.
- **Response Body:** `ResponseEntity<ReferralResponse>`
    - Contains the updated `ReferralResponse` on success (200 OK).
    - Returns 400 Bad Request if ID is invalid ObjectId format, body will be null.
- **Business Logic Summary:** Updates an existing referral's information.
- **Authorization/Security:** Public.

---

## Vision Ambassador Controller (`com.nayonikaeyecare.api.controllers.VisionAmbassadorController`)

Base Path: `/api/vision-ambassadors`

---

### 1. Create Vision Ambassador
- **HTTP Method and Path:** `POST /api/vision-ambassadors/addVisionAmbassador`
- **Controller Method:** `createVisionAmbassador`
- **Request Parameters:** None
- **Request Body:** `VisionAmbassadorRequest`
    - Key fields: (Refer to `VisionAmbassadorRequest` DTO for details - e.g., name, contact, address, userId)
- **Response Body:** `void` (Status 201 CREATED)
- **Business Logic Summary:** Creates a new Vision Ambassador record.
- **Authorization/Security:** Public.

---

### 2. Get All Vision Ambassadors
- **HTTP Method and Path:** `GET /api/vision-ambassadors/getAllVisionAmbassadors`
- **Controller Method:** `getAllVisionAmbassadors`
- **Request Parameters:** None
- **Request Body:** None
- **Response Body:** `List<VisionAmbassadorResponse>` (Status 200 OK)
    - Each `VisionAmbassadorResponse` contains details of a vision ambassador.
- **Business Logic Summary:** Retrieves a list of all Vision Ambassador records.
- **Authorization/Security:** Public.

---

### 3. Get Vision Ambassador By ID
- **HTTP Method and Path:** `GET /api/vision-ambassadors/{id}`
- **Controller Method:** `getVisionAmbassadorById`
- **Request Parameters:**
    - Path Variable: `id` (String) - The ID of the Vision Ambassador.
- **Request Body:** None
- **Response Body:** `ResponseEntity<VisionAmbassadorResponse>`
    - Contains `VisionAmbassadorResponse` on success (200 OK).
- **Business Logic Summary:** Retrieves a specific Vision Ambassador by their ID. Assumes ID is valid; error handling might be in service layer.
- **Authorization/Security:** Public.

---

### 4. Update Vision Ambassador
- **HTTP Method and Path:** `PUT /api/vision-ambassadors/{id}`
- **Controller Method:** `updateVisionAmbassador`
- **Request Parameters:**
    - Path Variable: `id` (String) - The ID of the Vision Ambassador to update.
- **Request Body:** `VisionAmbassadorRequest`
    - Contains the updated vision ambassador data.
- **Response Body:** `ResponseEntity<VisionAmbassadorResponse>`
    - Contains the updated `VisionAmbassadorResponse` on success (200 OK).
- **Business Logic Summary:** Updates an existing Vision Ambassador's information.
- **Authorization/Security:** Public.

---

### 5. Delete Vision Ambassador
- **HTTP Method and Path:** `DELETE /api/vision-ambassadors/{id}`
- **Controller Method:** `deleteVisionAmbassador`
- **Request Parameters:**
    - Path Variable: `id` (String) - The ID of the Vision Ambassador to delete.
- **Request Body:** None
- **Response Body:** `ResponseEntity<Void>` (Status 204 No Content)
- **Business Logic Summary:** Deletes a Vision Ambassador record by their ID.
- **Authorization/Security:** Public.

---

### 6. Get Paginated and Filtered Vision Ambassadors
- **HTTP Method and Path:** `GET /api/vision-ambassadors/filter`
- **Controller Method:** `getPaginatedPatients` (Note: method name seems like a typo, expected `getPaginatedVisionAmbassadors`)
- **Request Parameters:**
    - Query Param: `state` (String, optional) - Filter by state.
    - Query Param: `city` (String, optional) - Filter by city.
    - Query Param: `pageable` (Spring Data `Pageable` object) - Controls pagination.
- **Request Body:** None
- **Response Body:** `ResponseEntity<Page<VisionAmbassadorResponse>>`
    - Contains a paginated list of `VisionAmbassadorResponse` objects matching criteria.
- **Business Logic Summary:** Retrieves Vision Ambassadors with pagination and filtering by state and city.
- **Authorization/Security:** Public.

---

### 7. Find Vision Ambassador By User ID
- **HTTP Method and Path:** `GET /api/vision-ambassadors/user/{userId}`
- **Controller Method:** `findByUserId`
- **Request Parameters:**
    - Path Variable: `userId` (String) - The User ID associated with the Vision Ambassador.
- **Request Body:** None
- **Response Body:** `ResponseEntity<VisionAmbassador>`
    - Contains the `VisionAmbassador` entity on success (200 OK).
    - Returns 404 Not Found if no ambassador is associated with the userId.
- **Business Logic Summary:** Finds and retrieves a Vision Ambassador record based on the associated User ID.
- **Authorization/Security:** Public.

---

## Authentication Controller (`com.nayonikaeyecare.api.controllers.user.AuthenticationController`)

Base Path: `/auth`

---

### 1. Vision Ambassador Sign-in Request OTP
- **HTTP Method and Path:** `POST /auth/vision-ambassador-rquest-otp` (Note: "rquest" might be a typo for "request")
- **Controller Method:** `visionAmbassadorSignin`
- **Request Parameters:** None
- **Request Body:** `AuthenticationRequest`
    - Key fields: (e.g., `phoneNumber`)
- **Response Body:** `ResponseEntity<?>` (contains `AuthenticationResponse`)
    - `AuthenticationResponse` key fields: (e.g., `sessionId`, `message`)
- **Business Logic Summary:** Initiates the OTP sign-in process specifically for Vision Ambassadors. Generates and sends an OTP to the ambassador's registered phone number and returns a session ID.
- **Authorization/Security:** Authentication endpoint (publicly accessible to initiate login).

---

### 2. Request OTP (Generic)
- **HTTP Method and Path:** `POST /auth/request-otp`
- **Controller Method:** `authenticateUser`
- **Request Parameters:**
    - Query Param: `visionAmbassador` (boolean, optional, default: `false`) - Flag to indicate if the OTP request is for a Vision Ambassador.
- **Request Body:** `AuthenticationRequest`
    - Key fields: (e.g., `phoneNumber`)
- **Response Body:** `ResponseEntity<?>` (contains `AuthenticationResponse`)
    - `AuthenticationResponse` key fields: (e.g., `sessionId`, `message`)
- **Business Logic Summary:** Initiates OTP sign-in for general users or Vision Ambassadors based on the flag. Generates and sends an OTP, then returns a session ID.
- **Authorization/Security:** Authentication endpoint.

---

### 3. Update User
- **HTTP Method and Path:** `PUT /auth/{userId}`
- **Controller Method:** `updateUser`
- **Request Parameters:**
    - Path Variable: `userId` (String) - The ID of the user to update.
- **Request Body:** `UserRequest`
    - Key fields: (Refer to `UserRequest` DTO for details - e.g., name, email, roles, etc.)
- **Response Body:** `ResponseEntity<String>`
    - Success: "User updated successfully" (200 OK).
    - Error: 404 Not Found if user does not exist.
- **Business Logic Summary:** Updates the details of an existing user.
- **Authorization/Security:** This endpoint modifies user data and should be protected (e.g., accessible by admins or the user themselves). Current configuration allows all, but typically this would require authentication and authorization.

---

### 4. Verify OTP
- **HTTP Method and Path:** `POST /auth/verify-otp`
- **Controller Method:** `verifyOtp`
- **Request Parameters:** None
- **Request Body:** `OTPVerificationRequest`
    - Key fields: (e.g., `sessionId`, `otp`)
- **Response Body:** `ResponseEntity<?>`
    - Success: Contains `OTPVerificationResponse` with a token (e.g., JWT) (200 OK).
    - Error: Contains an error message (e.g., "Invalid OTP") and 401 Unauthorized status.
- **Business Logic Summary:** Verifies the OTP provided by the user against the session. If valid, generates an authentication token.
- **Authorization/Security:** Authentication endpoint.

---

### 5. Resend OTP
- **HTTP Method and Path:** `POST /auth/resend-otp`
- **Controller Method:** `resendOtp`
- **Request Parameters:** None
- **Request Body:** `OTPResendRequest`
    - Key fields: (e.g., `sessionId`)
- **Response Body:** `ResponseEntity<?>`
    - Success: Contains `OTPResendResponse` with a new session ID (200 OK).
    - Error: Contains an error message and 401 Unauthorized status if resend fails.
- **Business Logic Summary:** Resends an OTP to the user associated with the given session ID.
- **Authorization/Security:** Authentication endpoint.

---

### 6. Get User By ID
- **HTTP Method and Path:** `GET /auth/{userId}`
- **Controller Method:** `getUserById`
- **Request Parameters:**
    - Path Variable: `userId` (String) - The ID of the user to retrieve.
- **Request Body:** None
- **Response Body:** `ResponseEntity<?>`
    - Success: Contains the `User` entity (200 OK).
    - Error: "User not found with id: {userId}" (404 Not Found).
    - Error: "Invalid user ID format" (400 Bad Request).
- **Business Logic Summary:** Retrieves the details of a specific user by their ID.
- **Authorization/Security:** This endpoint exposes user data and should be protected (e.g., accessible by admins or the user themselves). Current configuration allows all.

---

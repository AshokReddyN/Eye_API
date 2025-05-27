# Entity Design Review

This document reviews the structure and design of MongoDB document classes, embedded classes, and enums identified from the `api/src/main/java/com/nayonikaeyecare/api/entities/` directory.

---

## Address.java

*   **Type:** Embedded Object
*   **Purpose:** Represents a physical address. It is intended to be embedded within other documents like `Hospital` and `Guardian`.
*   **Field Analysis:**
    *   `Field Name`: `address1`
        *   `Java Type`: `String`
        *   `MongoDB Type (Implied)`: `String`
        *   `Annotations`: `@NotBlank(message = "Address line 1 is required")`, `@Size(max = 100, message = "Address line 1 must be less than 100 characters")`
        *   `Observations & Recommendations`:
            *   Appropriate type.
            *   Validation (`@NotBlank`, `@Size`) is good for ensuring essential data and length constraints.
    *   `Field Name`: `address2`
        *   `Java Type`: `String`
        *   `MongoDB Type (Implied)`: `String`
        *   `Annotations`: `@Size(max = 100, message = "Address line 2 must be less than 100 characters")`
        *   `Observations & Recommendations`:
            *   Appropriate type.
            *   `address2` is optional, which is typical. Size constraint is good.
    *   `Field Name`: `city`
        *   `Java Type`: `String`
        *   `MongoDB Type (Implied)`: `String`
        *   `Annotations`: `@NotBlank(message = "City is required")`, `@Size(max = 50, message = "City must be less than 50 characters")`
        *   `Observations & Recommendations`:
            *   Appropriate type.
            *   Validation (`@NotBlank`, `@Size`) is good.
    *   `Field Name`: `state`
        *   `Java Type`: `String`
        *   `MongoDB Type (Implied)`: `String`
        *   `Annotations`: `@NotBlank(message = "State is required")`, `@Size(max = 50, message = "State must be less than 50 characters")`
        *   `Observations & Recommendations`:
            *   Appropriate type.
            *   Validation (`@NotBlank`, `@Size`) is good.
    *   `Field Name`: `pincode`
        *   `Java Type`: `String`
        *   `MongoDB Type (Implied)`: `String`
        *   `Annotations`: `@NotBlank(message = "Pincode is required")`, `@Pattern(regexp = "^[1-9][0-9]{5}$", message = "Pincode must be 6 digits")`
        *   `Observations & Recommendations`:
            *   Appropriate type (String is often better for pincodes/zipcodes than numeric types to preserve leading zeros if applicable, though the pattern here `^[1-9][0-9]{5}$` implies no leading zero).
            *   Validation (`@NotBlank`, `@Pattern`) is good for ensuring format.

*   **General Entity Observations & Recommendations:**
    *   The structure is suitable for an embedded address.
    *   Lombok annotations `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`, `@Data` are used, which is convenient for DTOs and embedded objects. `@Data` includes `@ToString`, `@EqualsAndHashCode`, `@Getter`, `@Setter`. For a mutable embedded object, this is generally fine.
    *   Validation annotations are from `jakarta.validation.constraints`, which are standard. These validations will be triggered if this object is part of a validated request body DTO, but not automatically by Spring Data MongoDB when saving (unless explicitly triggered).
    *   No specific concerns for its role as an embedded address.

---

## EyeDetails.java

*   **Type:** Embedded Object
*   **Purpose:** Represents the SPH, CYL, and AXIS details for an eye, typically used in ophthalmology prescriptions or referrals.
*   **Field Analysis:**
    *   `Field Name`: `sph`
        *   `Java Type`: `String`
        *   `MongoDB Type (Implied)`: `String`
        *   `Annotations`: None (apart from Lombok's `@Data` and `@Builder` on the class).
        *   `Observations & Recommendations`:
            *   Type is `String`. While these values are often numeric (e.g., "+2.25", "-1.50"), String allows for flexibility in representing them (e.g., "PLANO", "DS").
            *   Consider if a more structured type (e.g., `Double` for numeric part, additional fields for "PLANO", "DS") or specific validation patterns would be beneficial if consistent formatting is required. For now, `String` is flexible.
            *   No explicit constraints. Depending on requirements, validation (e.g., pattern for typical values) could be added if this object is used in DTOs.
    *   `Field Name`: `cyl`
        *   `Java Type`: `String`
        *   `MongoDB Type (Implied)`: `String`
        *   `Annotations`: None.
        *   `Observations & Recommendations`:
            *   Similar to `sph`, `String` offers flexibility.
            *   Consider validation if specific formats are expected.
    *   `Field Name`: `axis`
        *   `Java Type`: `String`
        *   `MongoDB Type (Implied)`: `String`
        *   `Annotations`: None.
        *   `Observations & Recommendations`:
            *   Axis is typically a numerical value (degrees) or could be descriptive. `String` is flexible.
            *   If always numeric, `Integer` might be an option, but `String` is acceptable. Consider validation if numeric range is expected.

*   **General Entity Observations & Recommendations:**
    *   Suitable for embedding within entities like `Referral` to store eye prescription details.
    *   Lombok annotations `@Data` and `@Builder` are used, which is fine for an embedded data holder.
    *   The fields are simple strings without explicit validation. This provides flexibility but relies on consuming services or DTOs to ensure data integrity if specific formats are needed.

---

## Guardian.java

*   **Type:** Embedded Object
*   **Purpose:** Represents information about a patient's guardian. Designed to be embedded within the `Patient` document.
*   **Field Analysis:**
    *   `Field Name`: `name`
        *   `Java Type`: `String`
        *   `MongoDB Type (Implied)`: `String`
        *   `Annotations`: None.
        *   `Observations & Recommendations`:
            *   Appropriate type.
            *   Consider adding `@NotBlank` if the guardian's name is mandatory when a guardian object is provided. This would typically be enforced at the DTO level.
    *   `Field Name`: `relation`
        *   `Java Type`: `String`
        *   `MongoDB Type (Implied)`: `String`
        *   `Annotations`: None.
        *   `Observations & Recommendations`:
            *   Appropriate type.
            *   Could be an Enum if the set of relations is fixed and predefined (e.g., FATHER, MOTHER, SPOUSE, etc.) for consistency.
    *   `Field Name`: `address`
        *   `Java Type`: `Address`
        *   `MongoDB Type (Implied)`: Embedded Document (`Address` object)
        *   `Annotations`: None.
        *   `Observations & Recommendations`:
            *   Correctly embeds the `Address` object.
    *   `Field Name`: `phone`
        *   `Java Type`: `String`
        *   `MongoDB Type (Implied)`: `String`
        *   `Annotations`: None.
        *   `Observations & Recommendations`:
            *   Appropriate type.
            *   Consider adding phone number validation (e.g., `@Pattern`) if used in DTOs.
    *   `Field Name`: `email`
        *   `Java Type`: `String`
        *   `MongoDB Type (Implied)`: `String`
        *   `Annotations`: None.
        *   `Observations & Recommendations`:
            *   Appropriate type.
            *   Consider adding email validation (e.g., `@Email`) if used in DTOs.

*   **General Entity Observations & Recommendations:**
    *   The structure is suitable for embedding guardian details within a `Patient` document.
    *   Uses Lombok (`@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`, `@Data`), which is convenient.
    *   Lack of direct validation annotations means validation would need to be handled at a higher level (e.g., in DTOs that include `Guardian`).
    *   The choice of `String` for `relation` is flexible but could be an enum for better data integrity and standardized choices.

---

## Gender.java

*   **Type:** Enum
*   **Purpose:** Represents the gender of an individual (e.g., a patient).
*   **Values:**
    *   `MALE`
    *   `FEMALE`
*   **Observations & Recommendations:**
    *   Standard way to represent gender with fixed values.
    *   The current values (`MALE`, `FEMALE`) are common but might not be exhaustive for all use cases or demographics. Depending on the application's requirements, it might be necessary to add an `OTHER`, `PREFER_NOT_TO_SAY`, or a more inclusive set of options. This depends entirely on the client's requirements for data collection.
    *   Stored as a String in MongoDB by default when used in an entity (e.g., in `Patient.java`).

---

## Hospital.java

*   **Type:** MongoDB Document (`@Document(value = "hospitals")`)
*   **Purpose:** Represents a hospital or eye care center in the system.
*   **Field Analysis:**
    *   `Field Name`: `id`
        *   `Java Type`: `ObjectId` (from `org.bson.types`)
        *   `MongoDB Type (Implied)`: `ObjectId`
        *   `Annotations`: `@Id` (from `org.springframework.data.annotation`)
        *   `Observations & Recommendations`: Standard and appropriate for MongoDB primary key.
    *   `Field Name`: `name`
        *   `Java Type`: `String`
        *   `MongoDB Type (Implied)`: `String`
        *   `Annotations`: None (Lombok generated).
        *   `Observations & Recommendations`: Appropriate. Consider if this should be indexed if frequently searched by name.
    *   `Field Name`: `address`
        *   `Java Type`: `Address`
        *   `MongoDB Type (Implied)`: Embedded Document (`Address` object)
        *   `Annotations`: None (Lombok generated).
        *   `Observations & Recommendations`: Good use of embedding for structured address data.
    *   `Field Name`: `services`
        *   `Java Type`: `List<String>`
        *   `MongoDB Type (Implied)`: `Array of String`
        *   `Annotations`: None (Lombok generated).
        *   `Observations & Recommendations`: Stores a list of service names. If services were complex objects or needed to be managed centrally, this might reference a `Service` collection. For simple string tags, this is fine. Could also be a `List<ServiceType>` if `ServiceType.java` enum covers all possible services and is intended for this purpose.
    *   `Field Name`: `status`
        *   `Java Type`: `Boolean`
        *   `MongoDB Type (Implied)`: `Boolean`
        *   `Annotations`: None (Lombok generated).
        *   `Observations & Recommendations`: Simple boolean for status (e.g., active/inactive). Clear and appropriate.
    *   `Field Name`: `coordinator`
        *   `Java Type`: `String`
        *   `MongoDB Type (Implied)`: `String`
        *   `Annotations`: None (Lombok generated).
        *   `Observations & Recommendations`: Name of the coordinator.
    *   `Field Name`: `coordinator_phonenumber`
        *   `Java Type`: `String`
        *   `MongoDB Type (Implied)`: `String`
        *   `Annotations`: None (Lombok generated).
        *   `Observations & Recommendations`: Phone number for the coordinator. Field naming consistency: uses snake_case, while other fields use camelCase. Standardizing to camelCase (e.g., `coordinatorPhoneNumber`) is recommended.
    *   `Field Name`: `coordinator_email`
        *   `Java Type`: `String`
        *   `MongoDB Type (Implied)`: `String`
        *   `Annotations`: None (Lombok generated).
        *   `Observations & Recommendations`: Email for the coordinator. Field naming consistency: uses snake_case. Standardizing to camelCase (e.g., `coordinatorEmail`) is recommended.
    *   `Field Name`: `googleLink`
        *   `Java Type`: `String`
        *   `MongoDB Type (Implied)`: `String`
        *   `Annotations`: None (Lombok generated).
        *   `Observations & Recommendations`: Link to Google Maps or similar.

*   **General Entity Observations & Recommendations:**
    *   The entity provides a good structure for hospital information.
    *   Lombok annotations (`@AllArgsConstructor`, `@NoArgsConstructor`, `@Data`, `@Builder`) are used, which is convenient for this type of entity.
    *   Consider indexing fields that are frequently used in queries (e.g., `name`, `address.city`, `address.state`, `status`). This is not specified in the entity but can be added via `@Indexed` or programmatically.
    *   The `services` field being a `List<String>` is flexible. If the list of services becomes standardized or requires more attributes, an enum (`ServiceType.java`) or a separate collection might be more appropriate.
    *   Field naming for `coordinator_phonenumber` and `coordinator_email` should be standardized to camelCase (e.g., `coordinatorPhoneNumber`, `coordinatorEmail`) to align with Java conventions and other fields in the entity.

---

## Patient.java

*   **Type:** MongoDB Document (`@Document(value = "patients")`)
*   **Purpose:** Represents a patient registered in the system.
*   **Field Analysis:**
    *   `Field Name`: `id`
        *   `Java Type`: `ObjectId`
        *   `MongoDB Type (Implied)`: `ObjectId`
        *   `Annotations`: `@Id`
        *   `Observations & Recommendations`: Standard primary key.
    *   `Field Name`: `name`
        *   `Java Type`: `String`
        *   `MongoDB Type (Implied)`: `String`
        *   `Annotations`: None (Lombok generated).
        *   `Observations & Recommendations`: Appropriate.
    *   `Field Name`: `ambassadorId`
        *   `Java Type`: `String`
        *   `MongoDB Type (Implied)`: `String` (likely storing `ObjectId.toHexString()` of a `VisionAmbassador`)
        *   `Annotations`: None (Lombok generated).
        *   `Observations & Recommendations`: Manual reference to `VisionAmbassador`. Using `ObjectId` type directly might be slightly better for type safety if all such IDs are indeed ObjectIds, but String is also common.
    *   `Field Name`: `gender`
        *   `Java Type`: `Gender` (Enum)
        *   `MongoDB Type (Implied)`: `String` (by default for enums)
        *   `Annotations`: None (Lombok generated).
        *   `Observations & Recommendations`: Good use of the `Gender` enum.
    *   `Field Name`: `ageRange`
        *   `Java Type`: `String`
        *   `MongoDB Type (Implied)`: `String`
        *   `Annotations`: None (Lombok generated).
        *   `Observations & Recommendations`: Stores age as a range (e.g., "20-30"). This is simple but makes range queries or calculations based on age difficult. Storing Date of Birth (`LocalDate` or `Date`) or a numeric age (`Integer`) and calculating the range if needed might be more flexible for querying and data analysis, though `ageRange` is simpler for direct display if that's the only use.
    *   `Field Name`: `phone`
        *   `Java Type`: `String`
        *   `MongoDB Type (Implied)`: `String`
        *   `Annotations`: None (Lombok generated).
        *   `Observations & Recommendations`: Appropriate.
    *   `Field Name`: `email`
        *   `Java Type`: `String`
        *   `MongoDB Type (Implied)`: `String`
        *   `Annotations`: None (Lombok generated).
        *   `Observations & Recommendations`: Appropriate.
    *   `Field Name`: `hospitalName`
        *   `Java Type`: `String`
        *   `MongoDB Type (Implied)`: `String`
        *   `Annotations`: None (Lombok generated).
        *   `Observations & Recommendations`: Denormalized field. Could be useful for quick display, but ensure consistency if the hospital name changes in the `Hospital` document. If this is the name of the hospital where the patient was *last referred* or *registered*, the name might need to be updated if the hospital's name changes. Alternatively, storing `hospitalId` (ObjectId) and looking up the name when needed would ensure consistency but require an additional query. The choice depends on read/write patterns and consistency requirements.
    *   `Field Name`: `status`
        *   `Java Type`: `String`
        *   `MongoDB Type (Implied)`: `String`
        *   `Annotations`: None (Lombok generated).
        *   `Observations & Recommendations`: Could be an enum (`PatientStatus.java` or use the existing generic `Status.java` if applicable) for standardized values (e.g., "NEW", "ACTIVE", "REFERRED", "CLOSED").
    *   `Field Name`: `city`
        *   `Java Type`: `String`
        *   `MongoDB Type (Implied)`: `String`
        *   `Annotations`: None (Lombok generated).
        *   `Observations & Recommendations`: Patient's city. This might be redundant if `guardianContact.address.city` is always the patient's primary city. If the patient can have an address independent of the guardian, then it's fine.
    *   `Field Name`: `state`
        *   `Java Type`: `String`
        *   `MongoDB Type (Implied)`: `String`
        *   `Annotations`: None (Lombok generated).
        *   `Observations & Recommendations`: Patient's state. Similar redundancy concern as `city` if `guardianContact.address.state` is primary.
    *   `Field Name`: `referralIds`
        *   `Java Type`: `List<String>`
        *   `MongoDB Type (Implied)`: `Array of String` (likely storing `ObjectId.toHexString()` of `Referral` documents)
        *   `Annotations`: None (Lombok generated).
        *   `Observations & Recommendations`: Manual reference list to `Referral` documents.
    *   `Field Name`: `guardianContact`
        *   `Java Type`: `Guardian`
        *   `MongoDB Type (Implied)`: Embedded Document (`Guardian` object)
        *   `Annotations`: None (Lombok generated).
        *   `Observations & Recommendations`: Good use of embedding for guardian details.
    *   `Field Name`: `createdAt`
        *   `Java Type`: `Date`
        *   `MongoDB Type (Implied)`: `Date`
        *   `Annotations`: None (Lombok generated).
        *   `Observations & Recommendations`: Standard audit field. Consider using Spring Data's auditing features (`@CreatedDate`) for automatic population.
    *   `Field Name`: `updatedAt`
        *   `Java Type`: `Date`
        *   `MongoDB Type (Implied)`: `Date`
        *   `Annotations`: None (Lombok generated).
        *   `Observations & Recommendations`: Standard audit field. Consider using Spring Data's auditing features (`@LastModifiedDate`) for automatic population.

*   **General Entity Observations & Recommendations:**
    *   The entity uses `@CompoundIndexes` for `{'ambassadorId': 1, 'name': 1}` and `{'state': 1, 'city': 1}`, which is good for query performance on these combinations.
    *   Lombok annotations are used effectively.
    *   The `status` field should ideally be an enum for consistency (e.g., using the existing `Status.java` if its values are appropriate, or a new specific `PatientStatus` enum).
    *   Clarify the source/purpose of `city` and `state` fields on the `Patient` directly, versus those within `guardianContact.address`. If the patient's address is always the guardian's address, these are redundant. If a patient can have their own address distinct from the guardian, then the `Patient` entity might need its own `Address` field.
    *   The `hospitalName` field is denormalized. This is acceptable for performance but requires careful data management if hospital names can change.
    *   Using Spring Data MongoDB's auditing annotations (`@CreatedDate`, `@LastModifiedDate`) for `createdAt` and `updatedAt` would simplify their management.

---

## Languages.java

*   **Type:** Enum
*   **Purpose:** Represents languages that can be associated with entities (e.g., a Vision Ambassador).
*   **Values:** `HINDI`, `ENGLISH`, `BENGALI`, `GUJARATI`, `MARATHI`, `TAMIL`, `TELUGU`, `MALAYALAM`, `KANNADA`, `ODIA`, `PUNJABI`, `ASSAMESE`, `URDU`.
*   **Observations & Recommendations:**
    *   Provides a good, diverse list of common Indian languages.
    *   Comprehensive for many use cases.
    *   As with any enum, if new languages need to be supported frequently, a lookup collection in the database might offer more dynamic management. However, for a relatively stable set of languages, an enum is efficient and type-safe.

---

## Referral.java

*   **Type:** MongoDB Document (`@Document(value = "referrals")`)
*   **Purpose:** Represents a patient referral to a hospital, potentially including details of the screening.
*   **Field Analysis:**
    *   `Field Name`: `id`
        *   `Java Type`: `ObjectId`
        *   `MongoDB Type (Implied)`: `ObjectId`
        *   `Annotations`: `@Id`
        *   `Observations & Recommendations`: Standard.
    *   `Field Name`: `status`
        *   `Java Type`: `Status` (Enum)
        *   `MongoDB Type (Implied)`: `String`
        *   `Annotations`: None (Lombok generated).
        *   `Observations & Recommendations`: Good use of the `Status` enum.
    *   `Field Name`: `patientId`
        *   `Java Type`: `ObjectId`
        *   `MongoDB Type (Implied)`: `ObjectId` (Reference to `Patient` document)
        *   `Annotations`: None (Lombok generated).
        *   `Observations & Recommendations`: Strong reference to `Patient`.
    *   `Field Name`: `patientName`
        *   `Java Type`: `String`
        *   `MongoDB Type (Implied)`: `String`
        *   `Annotations`: None (Lombok generated).
        *   `Observations & Recommendations`: Denormalized patient name. Useful for quick display on referral lists. Subject to consistency issues if patient name changes and this isn't updated.
    *   `Field Name`: `ageRange`
        *   `Java Type`: `String`
        *   `MongoDB Type (Implied)`: `String`
        *   `Annotations`: None (Lombok generated).
        *   `Observations & Recommendations`: Denormalized patient age range. Similar concerns as `patientName`.
    *   `Field Name`: `hospitalName`
        *   `Java Type`: `String`
        *   `MongoDB Type (Implied)`: `String`
        *   `Annotations`: None (Lombok generated).
        *   `Observations & Recommendations`: Denormalized hospital name. Similar concerns as `patientName`.
    *   `Field Name`: `city`
        *   `Java Type`: `String`
        *   `MongoDB Type (Implied)`: `String`
        *   `Annotations`: None (Lombok generated).
        *   `Observations & Recommendations`: Denormalized city, likely of the patient or hospital. Context should be clear. If it's patient's city, same denormalization concerns.
    *   `Field Name`: `state`
        *   `Java Type`: `String`
        *   `MongoDB Type (Implied)`: `String`
        *   `Annotations`: None (Lombok generated).
        *   `Observations & Recommendations`: Denormalized state. Similar to `city`.
    *   `Field Name`: `hospitalId`
        *   `Java Type`: `ObjectId`
        *   `MongoDB Type (Implied)`: `ObjectId` (Reference to `Hospital` document)
        *   `Annotations`: None (Lombok generated).
        *   `Observations & Recommendations`: Strong reference to `Hospital`.
    *   `Field Name`: `ambassadorId`
        *   `Java Type`: `ObjectId`
        *   `MongoDB Type (Implied)`: `ObjectId` (Reference to `VisionAmbassador` document)
        *   `Annotations`: None (Lombok generated).
        *   `Observations & Recommendations`: Strong reference to `VisionAmbassador`.
    *   `Field Name`: `services`
        *   `Java Type`: `List<ServiceType>` (Enum)
        *   `MongoDB Type (Implied)`: `Array of String`
        *   `Annotations`: None (Lombok generated).
        *   `Observations & Recommendations`: Good use of `ServiceType` enum list.
    *   `Field Name`: `treatment`
        *   `Java Type`: `String`
        *   `MongoDB Type (Implied)`: `String`
        *   `Annotations`: None (Lombok generated).
        *   `Observations & Recommendations`: Details of treatment.
    *   `Field Name`: `rightEye`
        *   `Java Type`: `EyeDetails`
        *   `MongoDB Type (Implied)`: Embedded Document (`EyeDetails` object)
        *   `Annotations`: None (Lombok generated).
        *   `Observations & Recommendations`: Good use of embedding `EyeDetails`.
    *   `Field Name`: `leftEye`
        *   `Java Type`: `EyeDetails`
        *   `MongoDB Type (Implied)`: Embedded Document (`EyeDetails` object)
        *   `Annotations`: None (Lombok generated).
        *   `Observations & Recommendations`: Good use of embedding `EyeDetails`.
    *   `Field Name`: `remarks`
        *   `Java Type`: `String`
        *   `MongoDB Type (Implied)`: `String`
        *   `Annotations`: None (Lombok generated).
        *   `Observations & Recommendations`: General remarks.
    *   `Field Name`: `createdAt`, `updatedAt`
        *   `Java Type`: `Date`
        *   `MongoDB Type (Implied)`: `Date`
        *   `Annotations`: None (Lombok generated).
        *   `Observations & Recommendations`: Standard audit fields. Consider `@CreatedDate` and `@LastModifiedDate`.

*   **General Entity Observations & Recommendations:**
    *   The entity is well-structured for referral information.
    *   Uses `@CompoundIndex` for `{'patientId': 1, 'createdAt': -1}` which is good for querying referrals for a patient sorted by creation time.
    *   Extensive denormalization (`patientName`, `ageRange`, `hospitalName`, `city`, `state`). While this can improve read performance for referral listings, it introduces data redundancy and potential consistency challenges if the source data (e.g., patient's name, hospital's name) changes. A strategy for updating these denormalized fields or accepting eventual consistency would be needed.
    *   Consider if `city` and `state` refer to the patient's location or the hospital's location. If the hospital's, it's denormalized from `Hospital.address`. If the patient's, it's denormalized from `Patient` (or `Patient.guardianContact.address`).
    *   Use of `ObjectId` for references (`patientId`, `hospitalId`, `ambassadorId`) is appropriate.

---

## VisionAmbassador.java

*   **Type:** MongoDB Document (`@Document(collection = "vision_ambassadors")`)
*   **Purpose:** Represents a Vision Ambassador in the system.
*   **Field Analysis:**
    *   `Field Name`: `id`
        *   `Java Type`: `ObjectId`
        *   `MongoDB Type (Implied)`: `ObjectId`
        *   `Annotations`: `@Id`
        *   `Observations & Recommendations`: Standard.
    *   `Field Name`: `userId`
        *   `Java Type`: `String`
        *   `MongoDB Type (Implied)`: `String` (Likely `ObjectId.toHexString()` of a `User` document)
        *   `Annotations`: None.
        *   `Observations & Recommendations`: Links to the `User` entity. Consistent use of `ObjectId` type here (like `private ObjectId userId;`) might be preferable over `String` for type consistency with other ID fields, unless there's a specific reason for string.
    *   `Field Name`: `name`
        *   `Java Type`: `String`
        *   `MongoDB Type (Implied)`: `String`
        *   `Annotations`: None.
        *   `Observations & Recommendations`: Appropriate.
    *   `Field Name`: `phoneNumber`
        *   `Java Type`: `String`
        *   `MongoDB Type (Implied)`: `String`
        *   `Annotations`: None.
        *   `Observations & Recommendations`: Appropriate.
    *   `Field Name`: `status`
        *   `Java Type`: `boolean` (primitive)
        *   `MongoDB Type (Implied)`: `Boolean`
        *   `Annotations`: None.
        *   `Observations & Recommendations`: Simple active/inactive status. Using `Boolean` (wrapper) might be slightly more flexible if a "null" status (undetermined) is ever needed, but `boolean` is fine for true/false.
    *   `Field Name`: `city`, `state`
        *   `Java Type`: `String`
        *   `MongoDB Type (Implied)`: `String`
        *   `Annotations`: None.
        *   `Observations & Recommendations`: Represents the ambassador's location. Could potentially embed an `Address` object for more structured address information if needed in the future (e.g., if full address becomes a requirement).
    *   `Field Name`: `language`
        *   `Java Type`: `String`
        *   `MongoDB Type (Implied)`: `String`
        *   `Annotations`: None.
        *   `Observations & Recommendations`: Stores a single language. If multiple languages are possible, `List<String>` or `List<Languages>` (enum) would be better. Using the `Languages` enum (`private Languages language;`) would provide better type safety and consistency.
    *   `Field Name`: `createdAt`, `updatedAt`
        *   `Java Type`: `Date`
        *   `MongoDB Type (Implied)`: `Date`
        *   `Annotations`: None.
        *   `Observations & Recommendations`: Standard audit fields. Consider `@CreatedDate` and `@LastModifiedDate`.

*   **General Entity Observations & Recommendations:**
    *   The entity provides a basic structure for Vision Ambassador information.
    *   The comment `// private String email;` suggests email might have been considered or could be a future field.
    *   Consider using the `Languages` enum for the `language` field.
    *   If a more detailed address is ever required, embedding the `Address` object would be a good approach.

---

## ServiceType.java

*   **Type:** Enum
*   **Purpose:** Represents the types of services related to referrals or hospital offerings.
*   **Values:**
    *   `SCREENING`
    *   `SPECS` (presumably for spectacles/glasses)
    *   `SURGERY`
*   **Observations & Recommendations:**
    *   Clear and concise values for common eye care service categories.
    *   Sufficient for the current scope as seen in `Referral.services`.
    *   If more granularity or attributes per service type were needed (e.g., description, average cost range), these might evolve into a separate collection, but for now, the enum is appropriate.

---

## Status.java

*   **Type:** Enum
*   **Purpose:** Represents various status values that can be applied to different entities (e.g., `Referral`).
*   **Values:**
    *   `PENDING`
    *   `CANCELLED`
    *   `INPROGRESS`
    *   `COMPLETED`
    *   `ACTIVE`
    *   `INACTIVE`
*   **Observations & Recommendations:**
    *   This is a generic status enum. It seems to combine statuses that might apply to processes (PENDING, INPROGRESS, COMPLETED, CANCELLED) and statuses that might apply to records or users (ACTIVE, INACTIVE).
    *   While reusable, this can lead to ambiguity or inappropriate status values being available for entities where they don't make sense. For example, is `INPROGRESS` a valid status for a `Hospital`? Or is `ACTIVE` a valid status for a `Referral` lifecycle if it also has `PENDING`, `COMPLETED`?
    *   **Recommendation:** It's generally better to have more specific enums for different contexts if their lifecycles and valid states differ significantly. For example:
        *   `ReferralStatus { PENDING, SCREENED, REFERRED_TO_HOSPITAL, TREATMENT_COMPLETED, CANCELLED }`
        *   `RecordStatus { ACTIVE, INACTIVE }` (for entities like Hospital, User, VisionAmbassador if they only have simple active/inactive states).
        *   The `Patient` entity uses a `String status;` field. This `Status.java` enum could be a candidate for that field if the values align, but a dedicated `PatientStatus` enum might be even better.
    *   If this generic `Status` enum is kept, careful application logic is needed to ensure only relevant values are used for each entity type.

---
This covers all entities in the `api/src/main/java/com/nayonikaeyecare/api/entities/` root. The next step will cover entities in subdirectories like `user` and `application`.Okay, I've appended the analysis for `Referral.java`, `VisionAmbassador.java`, `ServiceType.java`, and `Status.java` to `entity_design_review.md`.

Next, I will analyze the entities within the `user` sub-package: `User.java`, `UserCredential.java`, `UserSession.java`, `Permission.java`, `Role.java`, and the enums `UserSessionStatus.java` and `UserStatus.java`.

First, I need to read their content.

[end of entity_design_review.md]

[end of entity_design_review.md]

[end of entity_design_review.md]

[end of entity_design_review.md]

[end of entity_design_review.md]

[end of entity_design_review.md]

[end of entity_design_review.md]

[end of entity_design_review.md]

[end of entity_design_review.md]

[end of entity_design_review.md]

[end of entity_design_review.md]

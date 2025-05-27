# MongoDB Entity Relationships

This document outlines the relationships between MongoDB documents in the application.

## Hospital (`hospitals` collection)

-   **`Hospital` and `Address`**
    -   **Nature:** `Hospital` embeds an `Address` document.
    -   **Implementation:** The `Hospital` entity has an `address` field of type `Address`.
    -   **Cardinality:** One-to-One (one Hospital has one embedded Address).

## Patient (`patients` collection)

-   **`Patient` and `VisionAmbassador`**
    -   **Nature:** `Patient` refers to a `VisionAmbassador`. This implies that a patient is associated with a specific vision ambassador.
    -   **Implementation:** The `Patient` entity stores the ID of the `VisionAmbassador` in the `ambassadorId` field (String, likely `ObjectId.toHexString()`).
    -   **Cardinality:** Many-to-One (many Patients can be associated with one VisionAmbassador).

-   **`Patient` and `Referral`**
    -   **Nature:** `Patient` refers to multiple `Referral` documents. This indicates a patient can have several referrals.
    -   **Implementation:** The `Patient` entity stores a list of `Referral` IDs in the `referralIds` field (`List<String>`, likely `ObjectId.toHexString()`).
    -   **Cardinality:** One-to-Many (one Patient can have many Referrals).

-   **`Patient` and `Guardian`**
    -   **Nature:** `Patient` embeds a `Guardian` document.
    -   **Implementation:** The `Patient` entity has a `guardianContact` field of type `Guardian`.
    -   **Cardinality:** One-to-One (one Patient has one embedded Guardian).

-   **`Patient` and `Address` (via `Guardian`)**
    -   **Nature:** The embedded `Guardian` document within `Patient` itself embeds an `Address` document.
    -   **Implementation:** The `Guardian` class (embedded in `Patient`) has an `address` field of type `Address`.
    -   **Cardinality:** One-to-One (one Guardian within a Patient has one embedded Address).

## Referral (`referrals` collection)

-   **`Referral` and `Patient`**
    -   **Nature:** `Referral` refers to a `Patient` document. Each referral is associated with a specific patient.
    -   **Implementation:** The `Referral` entity stores the `ObjectId` of the `Patient` in the `patientId` field.
    -   **Cardinality:** Many-to-One (many Referrals can belong to one Patient). This is the inverse of `Patient.referralIds`.

-   **`Referral` and `Hospital`**
    -   **Nature:** `Referral` refers to a `Hospital` document. Each referral is associated with a specific hospital.
    -   **Implementation:** The `Referral` entity stores the `ObjectId` of the `Hospital` in the `hospitalId` field.
    -   **Cardinality:** Many-to-One (many Referrals can be associated with one Hospital).

-   **`Referral` and `VisionAmbassador`**
    -   **Nature:** `Referral` refers to a `VisionAmbassador` document. Each referral can be associated with a vision ambassador.
    -   **Implementation:** The `Referral` entity stores the `ObjectId` of the `VisionAmbassador` in the `ambassadorId` field.
    -   **Cardinality:** Many-to-One (many Referrals can be associated with one VisionAmbassador).

-   **`Referral` and `EyeDetails`**
    -   **Nature:** `Referral` embeds `EyeDetails` documents for both the right and left eye.
    -   **Implementation:** The `Referral` entity has `rightEye` and `leftEye` fields, both of type `EyeDetails`.
    -   **Cardinality:** One-to-One for each eye (one Referral has specific details for the right eye and specific details for the left eye).

## VisionAmbassador (`vision_ambassadors` collection)

-   **`VisionAmbassador` and `User`**
    -   **Nature:** `VisionAmbassador` refers to a `User` document. This links a vision ambassador's profile to a system user account.
    -   **Implementation:** The `VisionAmbassador` entity stores the ID of the `User` in the `userId` field (String, likely `ObjectId.toHexString()`).
    -   **Cardinality:** One-to-One (one VisionAmbassador profile corresponds to one User account).

## User (`users` collection)

-   **`User` and `UserCredential`**
    -   **Nature:** `User` refers to a `UserCredential` document. This separates user profile information from sensitive credential data (like OTP details).
    -   **Implementation:** The `User` entity stores the `ObjectId` of the `UserCredential` in the `userCredentialId` field.
    -   **Cardinality:** One-to-One (one User has one UserCredential document).

-   **`User` and `Permission`**
    -   **Nature:** `User` embeds a list of `Permission` objects. These define the specific actions a user can perform.
    -   **Implementation:** The `User` entity has a `permissions` field of type `List<Permission>`. `Permission` is assumed to be an embedded class or enum as it's not marked as a separate `@Document`.
    -   **Cardinality:** One-to-Many (one User can have multiple Permissions).

## UserCredential (`user_credentials` collection)
-   *(No direct relationships to other top-level documents via stored IDs or embedded complex objects. Its link to `User` is defined in the `User` entity.)*

## UserSession (`user_sessions` collection)

-   **`UserSession` and `User`**
    -   **Nature:** `UserSession` refers to a `User` document. Each session is associated with a specific user.
    -   **Implementation:** The `UserSession` entity stores the `ObjectId` of the `User` in the `userId` field.
    -   **Cardinality:** Many-to-One (many UserSessions can belong to one User).

-   **`UserSession` and `Application`**
    -   **Nature:** `UserSession` refers to an `Application` document. This indicates which application the session belongs to (e.g., mobile app, web portal).
    -   **Implementation:** The `UserSession` entity stores the `code` (String) of the `Application` in the `applicationCode` field.
    -   **Cardinality:** Many-to-One (many UserSessions can be associated with one Application).

-   **`UserSession` and `UserSession` (Self-reference)**
    -   **Nature:** `UserSession` can refer to another `UserSession` document, for instance, to link an OTP verification session to the initial OTP request session.
    -   **Implementation:** The `UserSession` entity stores the `ObjectId` of the linked session in the `linkedSessionId` field.
    -   **Cardinality:** One-to-One (or Many-to-One if multiple sessions could link to a single parent, though typically one-to-one for OTP flows).

## Application (`applications` collection)

-   **`Application` and `Role`**
    -   **Nature:** `Application` refers to multiple `Role` documents. This defines which roles are permitted to access or operate within a specific application.
    -   **Implementation:** The `Application` entity stores a list of `Role` `ObjectId`s in the `permittedRoles` field. *(Assumption: `Role` is a separate document/collection, as `ObjectId` is used. If `Role.java` defined it as an enum, this would be a list of enums.)*
    -   **Cardinality:** Many-to-Many (one Application can permit many Roles, and one Role can be permitted in many Applications, managed via this list of references).

---

*Note: `Address.java`, `Guardian.java`, and `EyeDetails.java` do not have `@Document` annotations, indicating they are primarily used as embedded structures within other documents.*
*The `Permission` class (within `User.java`) is also treated as an embedded structure or enum list.*
*The relationship between `Application` and `Role` assumes `Role` is a separate collection due to the use of `List<ObjectId> permittedRoles`. If `Role` is an enum or embedded type, this interpretation would change.*

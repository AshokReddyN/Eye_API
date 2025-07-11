# Personally Identifiable Information (PII) Handling System

## 1. Overview

This document describes the system implemented for handling Personally Identifiable Information (PII) within the application. The primary goal is to ensure that PII data is stored securely (encrypted) in the MongoDB database while allowing PII to be sent and received in plaintext in API requests and responses, thus maintaining the existing API contract.

The system automatically encrypts designated PII fields before they are saved to the database and decrypts them after they are loaded from the database.

## 2. How it Works

The PII handling system consists of the following core components:

*   **`@EncryptedField` Annotation:**
    *   A custom Java annotation (`com.nayonikaeyecare.api.pii.annotation.EncryptedField`).
    *   This annotation is used to mark specific String fields within MongoDB entity classes (e.g., `Patient`, `User`, embedded objects like `Address`, `Guardian`) that should be treated as PII and undergo encryption/decryption.

*   **`EncryptionService`:**
    *   Located at `com.nayonikaeyecare.api.pii.encryption.EncryptionService`.
    *   This service encapsulates the cryptographic logic.
    *   It uses **AES/GCM/NoPadding** for encryption, which provides both confidentiality and authenticity.
    *   A 256-bit encryption key is used.
    *   A unique Initialization Vector (IV) is generated for each encryption operation and prepended to the ciphertext. The combined IV and ciphertext are then Base64 encoded for storage as a String in MongoDB.

*   **`MongoPiiEncryptionListener`:**
    *   Located at `com.nayonikaeyecare.api.pii.listener.MongoPiiEncryptionListener`.
    *   This Spring Data MongoDB event listener intercepts entities during persistence operations:
        *   **`onBeforeConvert`:** Before an entity is converted to a BSON document to be saved, this listener iterates through its fields. If a String field is annotated with `@EncryptedField` and contains non-null data, its value is encrypted using the `EncryptionService`. The listener includes basic recursive processing for embedded objects and collections containing annotated fields.
        *   **`onAfterConvert`:** After data is retrieved from MongoDB and converted into an entity POJO, this listener iterates through its fields. If a String field is annotated with `@EncryptedField`, its value (which is the Base64 encoded IV + ciphertext from the database) is decrypted using the `EncryptionService`. This ensures the application layer (services, controllers) works with plaintext PII. Basic recursive processing for embedded objects and collections is also handled here.

**Data Flow:**

*   **Write Path (API Request -> Database):**
    1.  API receives plaintext PII in a request DTO.
    2.  Application logic maps DTO to an entity object (still plaintext PII).
    3.  When the entity is saved (e.g., via a Spring Data Repository), the `MongoPiiEncryptionListener`'s `onBeforeConvert` method is triggered.
    4.  Annotated PII fields are encrypted.
    5.  The entity, now with encrypted PII, is converted to BSON and stored in MongoDB.

*   **Read Path (Database -> API Response):**
    1.  Application requests data (e.g., via a Spring Data Repository).
    2.  The entity is loaded from MongoDB (PII fields are encrypted strings).
    3.  The BSON data is converted to an entity POJO. The `MongoPiiEncryptionListener`'s `onAfterConvert` method is triggered.
    4.  Annotated PII fields are decrypted.
    5.  The application logic receives the entity with plaintext PII.
    6.  Entity is mapped to a DTO (still plaintext PII) and returned in the API response.

## 3. Marking Fields as PII

To mark a field for encryption:
1.  Ensure the field is a `String` type within your MongoDB entity class (or an embedded class).
2.  Add the `@com.nayonikaeyecare.api.pii.annotation.EncryptedField` annotation directly above the field declaration.

**Example (`Patient.java`):**

```java
import com.nayonikaeyecare.api.pii.annotation.EncryptedField;
// ... other imports

@Document(value = "patients")
public class Patient {
    // ... other fields

    @EncryptedField
    private String name;

    @EncryptedField
    private String email;

    @EncryptedField
    private String phone;

    private Guardian guardianContact; // Guardian class also has @EncryptedField on its PII strings

    // ... other fields
}
```

The listener will automatically handle fields within embedded objects (like `Guardian` or `Address`) if those fields are themselves annotated.

## 4. Key Management

The security of the encrypted PII relies heavily on the secrecy and strength of the encryption key.

*   **Key:** A 256-bit (32-byte) AES key is required.
*   **Configuration Property:** The key is provided to the application via the `pii.encryption.key` configuration property.
*   **Storage:** The value for this property **MUST** be a Base64 encoded representation of the 256-bit key.

**Production Environment:**
*   The `pii.encryption.key` property **MUST** be set using an environment variable named `PII_ENCRYPTION_KEY`.
*   The `application-prod.yml` file is configured such that the application will fail to start if this environment variable is not set. This is a security measure.
*   **Example:** `export PII_ENCRYPTION_KEY="your_generated_base64_encoded_256bit_key"`

**Development Environment (`application-dev.yml`):**
*   The configuration allows for a default fallback key if the `PII_ENCRYPTION_KEY` environment variable is not set.
    `pii.encryption.key: ${PII_ENCRYPTION_KEY:B8gmQSVarw9sWLx2Mh5gS5Cj2Qp7nC5zTjLg8fVbZ7U=}`
*   **WARNING:** The default key provided in `application-dev.yml` is for development and testing convenience **ONLY**. It **MUST NOT** be used in production or any sensitive environment.

**Generating a Secure Key:**
You can generate a cryptographically secure 256-bit key and its Base64 representation using Java. Place the following class in a temporary file, compile, and run it:

```java
import java.security.SecureRandom;
import java.util.Base64;

public class PiiKeyGenerator {
    public static void main(String[] args) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] key = new byte[32]; // 256-bit key
        secureRandom.nextBytes(key);
        String base64Key = Base64.getEncoder().encodeToString(key);
        System.out.println("Generated Base64 Encoded PII Key: " + base64Key);
        // Example output: B8gmQSVarw9sWLx2Mh5gS5Cj2Qp7nC5zTjLg8fVbZ7U=
        // Use the output as your PII_ENCRYPTION_KEY value.
    }
}
```
Store the generated key securely. If the key is compromised, all encrypted PII data is at risk. Consider key rotation policies as appropriate for your security requirements.

## 5. Configuration

The main configuration property is:
*   `pii.encryption.key`: (As described in Key Management)

The `EncryptionService` and `MongoPiiEncryptionListener` are Spring components and are automatically detected and configured if component scanning includes their packages (`com.nayonikaeyecare.api.pii.*`).

## 6. Error Handling

*   **Encryption Errors:** If encryption fails during the save process, the `EncryptionService` will throw an `EncryptionException` (a `RuntimeException`), which will typically cause the save operation to fail.
*   **Decryption Errors:** If decryption fails during data loading (e.g., due to data corruption, key mismatch, or if the field was not properly encrypted), the `EncryptionService` throws a `DecryptionException`. The `MongoPiiEncryptionListener` catches this, logs an error, and then re-throws it as a `RuntimeException`. This will cause the data loading operation to fail, preventing corrupted or unreadable PII from propagating into the application.
*   **Invalid Key:** If the application starts with an improperly formatted or wrong-length `pii.encryption.key`, the `EncryptionService` will fail to initialize, preventing the application from starting up with an insecure configuration.

## 7. Limitations and Considerations

*   **String Fields Only:** Currently, the `@EncryptedField` annotation and corresponding logic are designed to work only with `String` fields. PII in other data types is not automatically handled.
*   **Performance:**
    *   Reflection is used by the listener to identify and process annotated fields. While generally acceptable, this might introduce overhead in extremely high-throughput scenarios with very complex objects.
    *   The encryption/decryption operations themselves add computational cost. This is an inherent trade-off for security.
    *   If performance issues are suspected, profiling is recommended.
*   **Data Queries:**
    *   Because PII fields are stored as encrypted ciphertext in MongoDB, you **cannot** perform direct queries (e.g., exact matches, regex searches, range queries) on the original plaintext values of these fields in the database.
    *   If searching on PII fields is a requirement, alternative strategies like creating separate, one-way hashed fields (cryptographic hash, not encryption) for exact searches might be needed, or data must be decrypted application-side before searching (which is inefficient for large datasets). This system does not implement such search capabilities.
*   **Key Rotation:** This document does not cover key rotation procedures. If key rotation is required, a more complex strategy would be needed to manage multiple keys and re-encrypt data.
*   **Backups:** Encrypted data will be backed up in its encrypted form. Ensure your key management strategy includes secure backup and recovery of the encryption keys. Losing the key means losing access to the PII data.
```
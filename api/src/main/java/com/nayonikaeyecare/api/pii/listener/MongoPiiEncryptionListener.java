package com.nayonikaeyecare.api.pii.listener;
 
import com.nayonikaeyecare.api.pii.annotation.EncryptedField;
import com.nayonikaeyecare.api.pii.annotation.EncryptionService;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterConvertEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
 
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Base64;
 
 
@Component
public class MongoPiiEncryptionListener extends AbstractMongoEventListener<Object> {
 
    private static final Logger logger = LoggerFactory.getLogger(MongoPiiEncryptionListener.class);
    private final EncryptionService encryptionService;
 
    // To prevent re-processing the same object if events are nested or duplicated
    // N.B. This is a simple per-thread mechanism. For complex scenarios or async operations,
    // a more robust tracking mechanism might be needed.
    private final ThreadLocal<Set<Object>> processedObjects = ThreadLocal.withInitial(HashSet::new);
    private final ThreadLocal<Set<Object>> processedObjectsForDecryption = ThreadLocal.withInitial(HashSet::new);
 
 
    @Autowired
    public MongoPiiEncryptionListener(EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }
 
    @Override
    public void onBeforeConvert(BeforeConvertEvent<Object> event) {
        Object entity = event.getSource();
        if (processedObjects.get().contains(entity)) {
            return;
        }
        processedObjects.get().add(entity);
        try {
            logger.debug("onBeforeConvert: Processing entity of type {}", entity.getClass().getName());
            processFields(entity, true);
        } finally {
            processedObjects.get().remove(entity); // Clean up after top-level call for this entity
        }
    }
 
    @Override
    public void onAfterConvert(AfterConvertEvent<Object> event) {
        Object entity = event.getSource(); // This is the POJO
        Document document = event.getDocument(); // This is the BSON document from DB
 
        if (entity == null || document == null) {
            return;
        }
        
        // Use a different ThreadLocal set for decryption to avoid conflicts if an object is loaded and then immediately saved.
        if (processedObjectsForDecryption.get().contains(entity)) {
            return;
        }
        processedObjectsForDecryption.get().add(entity);
 
        try {
            logger.debug("onAfterConvert: Processing entity of type {} from document", entity.getClass().getName());
            // We need to read from the 'document' (BSON) and set on the 'entity' (POJO)
            processFieldsAfterConvert(entity, document);
        } finally {
            processedObjectsForDecryption.get().remove(entity);
        }
    }
    
   private void processFields(Object object, boolean encrypt) {
    if (object == null) {
        return;
    }

    // Avoid issues with proxies or special Spring Data types if not a domain object
    if (object.getClass().getName().startsWith("org.springframework.data")) {
        return;
    }

    // Handle collections
    if (object instanceof Collection) {
        ((Collection<?>) object).forEach(item -> processFields(item, encrypt));
        return;
    }
    // Handle arrays
    if (object.getClass().isArray()) {
        Arrays.stream((Object[]) object).forEach(item -> processFields(item, encrypt));
        return;
    }

    ReflectionUtils.doWithFields(object.getClass(), field -> {
        // --- FIX: Skip JDK fields ---
        if (field.getDeclaringClass().getPackageName().startsWith("java.")) {
            return;
        }
        // --- END FIX ---

        ReflectionUtils.makeAccessible(field);
        if (field.isAnnotationPresent(EncryptedField.class)) {
            try {
                ReflectionUtils.makeAccessible(field);
                Object value = field.get(object);

                if (value instanceof String) {
                    String stringValue = (String) value;
                    if (stringValue != null && !stringValue.isEmpty()) {
                        if (encrypt) {
                            if (!isPotentiallyEncrypted(stringValue)) {
                                field.set(object, encryptionService.encrypt(stringValue));
                                logger.trace("Encrypted field: {}.{}", object.getClass().getSimpleName(), field.getName());
                            }
                        }
                    }
                } else if (value != null && !(value instanceof String) && !isPrimitiveOrWrapperOrSpecial(value.getClass())) {
                    if (!processedObjects.get().contains(value)) {
                        processedObjects.get().add(value);
                        processFields(value, encrypt);
                        processedObjects.get().remove(value);
                    }
                }
            } catch (IllegalAccessException e) {
                logger.error("Error accessing field {} on object {}", field.getName(), object.getClass().getName(), e);
                throw new RuntimeException("Error accessing field for PII processing", e);
            } catch (EncryptionService.EncryptionException | EncryptionService.DecryptionException e) {
                logger.error("Encryption/Decryption error for field {} on object {}: {}", field.getName(), object.getClass().getName(), e.getMessage());
                throw e;
            }
        } else if (valueNeedsRecursiveProcessing(field.get(object))) {
            try {
                ReflectionUtils.makeAccessible(field);
                Object nestedObject = field.get(object);
                if (nestedObject != null && !processedObjects.get().contains(nestedObject)) {
                    processedObjects.get().add(nestedObject);
                    processFields(nestedObject, encrypt);
                    processedObjects.get().remove(nestedObject);
                }
            } catch (IllegalAccessException e) {
                logger.error("Error accessing nested field {} for recursive processing on object {}", field.getName(), object.getClass().getName(), e);
            }
        }
    });
}

private void processFieldsAfterConvert(Object entityPojo, Document bsonDocument) {
    if (entityPojo == null || bsonDocument == null) {
        return;
    }

    ReflectionUtils.doWithFields(entityPojo.getClass(), field -> {
        // --- FIX: Skip JDK fields ---
        if (field.getDeclaringClass().getPackageName().startsWith("java.")) {
            return;
        }
        // --- END FIX ---

        ReflectionUtils.makeAccessible(field);
        if (field.isAnnotationPresent(EncryptedField.class)) {
            try {
                ReflectionUtils.makeAccessible(field);
                Object bsonValue = bsonDocument.get(field.getName());

                if (bsonValue instanceof String) {
                    String encryptedString = (String) bsonValue;
                    if (encryptedString != null && !encryptedString.isEmpty()) {
                        try {
                            String decryptedValue = encryptionService.decrypt(encryptedString);
                            field.set(entityPojo, decryptedValue);
                            logger.trace("Decrypted field: {}.{}", entityPojo.getClass().getSimpleName(), field.getName());
                        } catch (EncryptionService.DecryptionException e) {
                            logger.error("Failed to decrypt field {}.{}: {}. Data may be corrupted or key mismatch.",
                                    entityPojo.getClass().getSimpleName(), field.getName(), e.getMessage(), e);
                            throw new RuntimeException("Failed to decrypt PII field: " + field.getName() + " for entity " + entityPojo.getClass().getSimpleName(), e);
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                logger.error("Error accessing field {} on object {} during onAfterConvert", field.getName(), entityPojo.getClass().getName(), e);
                throw new RuntimeException("Error accessing field for PII decryption", e);
            } catch (EncryptionService.DecryptionException e) {
                logger.error("Decryption error for field {} on object {}: {}", field.getName(), entityPojo.getClass().getName(), e.getMessage());
                throw e;
            }
        } else {
            try {
                ReflectionUtils.makeAccessible(field);
                Object nestedPojo = field.get(entityPojo);
                Object nestedBson = bsonDocument.get(field.getName());
                if (nestedPojo != null && nestedBson instanceof Document && !processedObjectsForDecryption.get().contains(nestedPojo)) {
                    processedObjectsForDecryption.get().add(nestedPojo);
                    processFieldsAfterConvert(nestedPojo, (Document) nestedBson);
                    processedObjectsForDecryption.get().remove(nestedPojo);
                } else if (nestedPojo instanceof Collection && nestedBson instanceof Collection) {
                    Collection<?> pojoCollection = (Collection<?>) nestedPojo;
                    Collection<?> bsonCollection = (Collection<?>) nestedBson;
                    if (pojoCollection.size() == bsonCollection.size()) {
                        var pojoIterator = pojoCollection.iterator();
                        var bsonIterator = bsonCollection.iterator();
                        while(pojoIterator.hasNext() && bsonIterator.hasNext()) {
                            Object pojoItem = pojoIterator.next();
                            Object bsonItem = bsonIterator.next();
                            if (pojoItem != null && bsonItem instanceof Document && !processedObjectsForDecryption.get().contains(pojoItem)) {
                                processedObjectsForDecryption.get().add(pojoItem);
                                processFieldsAfterConvert(pojoItem, (Document) bsonItem);
                                processedObjectsForDecryption.get().remove(pojoItem);
                            }
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                logger.error("Error accessing nested field {} for recursive decryption on object {}", field.getName(), entityPojo.getClass().getName(), e);
            }
        }
    });
}
    private boolean isPotentiallyEncrypted(String value) {
        // A very basic check. Assumes Base64 encoding and some length.
        // AES/GCM IV (12) + Tag (16) + at least 1 block (16) = 44 bytes.
        // Base64 encoded length will be roughly (4/3)*bytes. So > ~50-60 chars.
        // This is not foolproof.
        if (value.length() < 60) return false; // Heuristic
        try {
            Base64.getDecoder().decode(value);
            return true; // It's valid Base64, could be encrypted.
        } catch (IllegalArgumentException e) {
            return false; // Not Base64, so not encrypted by our service.
        }
    }
    
    private boolean isPrimitiveOrWrapperOrSpecial(Class<?> clazz) {
        return clazz.isPrimitive() ||
               clazz.isAssignableFrom(String.class) ||
               Number.class.isAssignableFrom(clazz) ||
               Boolean.class.isAssignableFrom(clazz) ||
               Character.class.isAssignableFrom(clazz) ||
               java.util.Date.class.isAssignableFrom(clazz) ||
               java.time.temporal.Temporal.class.isAssignableFrom(clazz) ||
               clazz.isEnum() ||
               org.bson.types.ObjectId.class.isAssignableFrom(clazz);
    }
 
    private boolean valueNeedsRecursiveProcessing(Object value) {
        if (value == null) return false;
        if (value instanceof Collection) return true; // Always check collections
        if (value.getClass().isArray()) return true; // Always check arrays
        return !isPrimitiveOrWrapperOrSpecial(value.getClass());
    }
}
package com.nayonikaeyecare.api.pii.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a field in a MongoDB entity for encryption.
 * Fields annotated with @EncryptedField will be automatically encrypted
 * before being saved to the database and decrypted after being loaded.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EncryptedField {
}
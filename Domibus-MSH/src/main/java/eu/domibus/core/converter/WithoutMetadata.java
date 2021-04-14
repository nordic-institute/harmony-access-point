package eu.domibus.core.converter;

import org.mapstruct.Mapping;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author François Gautier
 * @since 5.0
 */
@Retention(RetentionPolicy.CLASS)
@Mapping(ignore = true, target = "creationTime")
@Mapping(ignore = true, target = "modificationTime")
@Mapping(ignore = true, target = "createdBy")
@Mapping(ignore = true, target = "modifiedBy")
public @interface WithoutMetadata {
}

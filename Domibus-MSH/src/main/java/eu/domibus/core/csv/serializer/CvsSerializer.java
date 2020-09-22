package eu.domibus.core.csv.serializer;

/**
 * @author François Gautier
 * @since 4.2
 */
public interface CvsSerializer {
    boolean canHandle(Object fieldValue);
    String serialize(Object fieldValue);
}

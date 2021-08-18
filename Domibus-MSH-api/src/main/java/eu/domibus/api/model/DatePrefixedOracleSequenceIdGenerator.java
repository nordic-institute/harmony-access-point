package eu.domibus.api.model;

import org.hibernate.id.enhanced.SequenceStyleGenerator;


/**
 * New sequence format generator. The method generates a new sequence using current date and a fixed length (10 digits) increment.
 *
 * @author idragusa
 * @since 5.0
 */
public class DatePrefixedOracleSequenceIdGenerator extends SequenceStyleGenerator implements DomibusDatePrefixedSequenceIdGeneratorGenerator {

}

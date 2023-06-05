package eu.domibus.api.model;


/**
 * New sequence format generator. The method generates a new sequence using current date and a fixed length (10 digits) increment.
 *
 * @author idragusa
 * @since 5.0
 */
public class DatePrefixedMysqlSequenceIdGenerator extends org.hibernate.id.enhanced.TableGenerator implements DomibusDatePrefixedSequenceIdGeneratorGenerator {

}

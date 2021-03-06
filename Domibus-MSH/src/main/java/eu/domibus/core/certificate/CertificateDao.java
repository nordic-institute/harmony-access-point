package eu.domibus.core.certificate;

import java.util.Date;
import java.util.List;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public interface CertificateDao {

    void saveOrUpdate(Certificate certificate);

    List<Certificate> findImminentExpirationToNotifyAsAlert(Date nextNotification, Date fromDate, Date toDate);

    List<Certificate> findExpiredToNotifyAsAlert(final Date nextNotification, final Date endNotification);

    List<Certificate> getUnNotifiedSoonRevoked();

    List<Certificate> getUnNotifiedRevoked();

    void updateRevocation(Certificate certificate);

    void removeUnusedCertificates(List<Certificate> usedCertificates);
}

import eu.europa.domibus.CommonsDataLoader;
import eu.europa.domibus.FileCacheDataLoader;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.spi.client.http.DSSFileLoader;
import eu.europa.esig.dss.spi.tsl.TrustedListsCertificateSource;
import eu.europa.esig.dss.spi.x509.CertificateSource;
import eu.europa.esig.dss.spi.x509.KeyStoreCertificateSource;
import eu.europa.esig.dss.tsl.alerts.LOTLAlert;
import eu.europa.esig.dss.tsl.alerts.TLAlert;
import eu.europa.esig.dss.tsl.alerts.detections.LOTLLocationChangeDetection;
import eu.europa.esig.dss.tsl.alerts.detections.OJUrlChangeDetection;
import eu.europa.esig.dss.tsl.alerts.detections.TLExpirationDetection;
import eu.europa.esig.dss.tsl.alerts.detections.TLSignatureErrorDetection;
import eu.europa.esig.dss.tsl.alerts.handlers.log.LogLOTLLocationChangeAlertHandler;
import eu.europa.esig.dss.tsl.alerts.handlers.log.LogOJUrlChangeAlertHandler;
import eu.europa.esig.dss.tsl.alerts.handlers.log.LogTLExpirationAlertHandler;
import eu.europa.esig.dss.tsl.alerts.handlers.log.LogTLSignatureErrorAlertHandler;
import eu.europa.esig.dss.tsl.cache.CacheKey;
import eu.europa.esig.dss.tsl.cache.access.CacheAccessByKey;
import eu.europa.esig.dss.tsl.cache.access.CacheAccessFactory;
import eu.europa.esig.dss.tsl.cache.access.ReadOnlyCacheAccess;
import eu.europa.esig.dss.tsl.dto.ParsingCacheDTO;
import eu.europa.esig.dss.tsl.function.OfficialJournalSchemeInformationURI;
import eu.europa.esig.dss.tsl.job.LOTLChangeApplier;
import eu.europa.esig.dss.tsl.job.TLSourceBuilder;
import eu.europa.esig.dss.tsl.job.TLValidationJob;
import eu.europa.esig.dss.tsl.runnable.LOTLAnalysis;
import eu.europa.esig.dss.tsl.runnable.LOTLWithPivotsAnalysis;
import eu.europa.esig.dss.tsl.runnable.TLAnalysis;
import eu.europa.esig.dss.tsl.source.LOTLSource;
import eu.europa.esig.dss.tsl.source.TLSource;
import eu.europa.esig.dss.tsl.sync.AcceptAllStrategy;
import eu.europa.esig.dss.tsl.sync.TrustedListCertificateSourceSynchronizer;
import eu.europa.esig.dss.validation.CertificateValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @author Thomas Dussart
 * @since 4.2
 */
public class Test {

    private static final Logger LOG = LoggerFactory.getLogger(Test.class);
    /**
     * Provides methods to manage the asynchronous behaviour
     */
    private ExecutorService executorService = Executors.newCachedThreadPool();

    private CacheAccessFactory cacheAccessFactory = new CacheAccessFactory();


    public Test() throws IOException {
        LOTLSource lotlSource = new LOTLSource();
        lotlSource.setUrl("https://ec.europa.eu/tools/lotl/eu-lotl.xml");
        CertificateSource certificateSource = officialJournalContentKeyStore();
        lotlSource.setCertificateSource(certificateSource);
        lotlSource.setSigningCertificatesAnnouncementPredicate(new OfficialJournalSchemeInformationURI("https://ec.europa.eu/information_society/policy/esignature/trusted-list/tl.html"));
        lotlSource.setPivotSupport(true);
        List<LOTLSource> lotlSources=new ArrayList<>();
        lotlSources.add(lotlSource);
        FileCacheDataLoader dssFileLoader = new FileCacheDataLoader(new CommonsDataLoader());
        dssFileLoader.setFileCacheDirectory(new File("C:\\tmp\\test"));
        executeLOTLSourcesAnalysis(lotlSources, dssFileLoader);
        List<TLSource> tlSources = new ArrayList<>(extractTlSources(lotlSources));
        executeTLSourcesAnalysis(tlSources, dssFileLoader);
        synchronizeTLCertificateSource(tlSources.toArray(new TLSource[]{}),lotlSources.toArray(new LOTLSource[]{}),new TrustedListsCertificateSource());
    }



    public CertificateSource officialJournalContentKeyStore() throws IOException {
        return new KeyStoreCertificateSource(new File("C:\\install\\domibus-distribution-5.0-SNAPSHOT-tomcat-full\\domibus\\conf\\domibus\\keystores\\ojkeystore.p12"), "PKCS12", "dss-password");
    }

    public void executeLOTLSourcesAnalysis(List<LOTLSource> lotlSources, DSSFileLoader dssFileLoader) {
        checkNoDuplicateUrls(lotlSources);

        int nbLOTLSources = lotlSources.size();

        LOG.info("Running analysis for {} LOTLSource(s)", nbLOTLSources);

        Map<CacheKey, ParsingCacheDTO> oldParsingValues = extractParsingCache(lotlSources);

        CountDownLatch latch = new CountDownLatch(nbLOTLSources);
        for (LOTLSource lotlSource : lotlSources) {
            final CacheAccessByKey cacheAccess = cacheAccessFactory.getCacheAccess(lotlSource.getCacheKey());
            if (lotlSource.isPivotSupport()) {
                executorService.submit(new LOTLWithPivotsAnalysis(cacheAccessFactory, lotlSource, dssFileLoader, latch));
            } else {
                executorService.submit(new LOTLAnalysis(lotlSource, cacheAccess, dssFileLoader, latch));
            }
        }

        try {
            latch.await();
            LOG.info("Analysis is DONE for {} LOTLSource(s)", nbLOTLSources);
        } catch (InterruptedException e) {
            LOG.error("Interruption in the LOTLSource process", e);
            Thread.currentThread().interrupt();
        }

        Map<CacheKey, ParsingCacheDTO> newParsingValues = extractParsingCache(lotlSources);

        // Analyze introduced changes for TLs + adapt cache for TLs (EXPIRED)
        final LOTLChangeApplier lotlChangeApplier = new LOTLChangeApplier(cacheAccessFactory.getTLChangesCacheAccess(), oldParsingValues, newParsingValues);
        lotlChangeApplier.analyzeAndApply();
    }

    private List<TLSource> extractTlSources(List<LOTLSource> lotlList) {
        TLSourceBuilder tlSourceBuilder = new TLSourceBuilder(lotlList, extractParsingCache(lotlList));
        return tlSourceBuilder.build();
    }

    private void executeTLSourcesAnalysis(List<TLSource> tlSources, DSSFileLoader dssFileLoader) {
        int nbTLSources = tlSources.size();
        if (nbTLSources == 0) {
            LOG.info("No TL to be analyzed");
            return;
        }

        checkNoDuplicateUrls(tlSources);

        LOG.info("Running analysis for {} TLSource(s)", nbTLSources);

        CountDownLatch latch = new CountDownLatch(nbTLSources);
        for (TLSource tlSource : tlSources) {
            final CacheAccessByKey cacheAccess = cacheAccessFactory.getCacheAccess(tlSource.getCacheKey());
            executorService.submit(new TLAnalysis(tlSource, cacheAccess, dssFileLoader, latch));
        }

        try {
            latch.await();
            LOG.info("Analysis is DONE for {} TLSource(s)", nbTLSources);
        } catch (InterruptedException e) {
            LOG.error("Interruption in the TLAnalysis process", e);
            Thread.currentThread().interrupt();
        }
    }

    private void checkNoDuplicateUrls(List<? extends TLSource> sources) {
        List<String> allUrls = sources.stream().map(s -> s.getUrl()).collect(Collectors.toList());
        Set<String> uniqueUrls = new HashSet<>(allUrls);
        if (allUrls.size() > uniqueUrls.size()) {
            throw new DSSException(String.format("Duplicate urls found : %s", allUrls));
        }
    }

    private Map<CacheKey, ParsingCacheDTO> extractParsingCache(List<LOTLSource> lotlSources) {
        final ReadOnlyCacheAccess readOnlyCacheAccess = cacheAccessFactory.getReadOnlyCacheAccess();
        return lotlSources.stream().collect(Collectors.toMap(LOTLSource::getCacheKey, s -> readOnlyCacheAccess.getParsingCacheDTO(s.getCacheKey())));
    }

    private void synchronizeTLCertificateSource(TLSource[] tlSources, LOTLSource[] lotlSources,
                                                TrustedListsCertificateSource certificateSource) {
        LOG.info("Synchronizing certificate source ");
        if (certificateSource == null) {
            LOG.warn("No TrustedListCertificateSource to be synchronized");
            return;
        }

        TrustedListCertificateSourceSynchronizer synchronizer = new TrustedListCertificateSourceSynchronizer(tlSources, lotlSources,
                certificateSource, new AcceptAllStrategy(), cacheAccessFactory.getSynchronizerCacheAccess());
        synchronizer.sync();
        LOG.info("Synchronization done");
    }

    public static void main(String[] args) throws IOException {
        new Test();
    }

    public TLAlert tlSigningAlert() {
        TLSignatureErrorDetection signingDetection = new TLSignatureErrorDetection();
        LogTLSignatureErrorAlertHandler handler = new LogTLSignatureErrorAlertHandler();
        return new TLAlert(signingDetection, handler);
    }

    public TLAlert tlExpirationDetection() {
        TLExpirationDetection expirationDetection = new TLExpirationDetection();
        LogTLExpirationAlertHandler handler = new LogTLExpirationAlertHandler();
        return new TLAlert(expirationDetection, handler);
    }

    public LOTLAlert ojUrlAlert(LOTLSource source) {
        OJUrlChangeDetection ojUrlDetection = new OJUrlChangeDetection(source);
        LogOJUrlChangeAlertHandler handler = new LogOJUrlChangeAlertHandler();
        return new LOTLAlert(ojUrlDetection, handler);
    }

    public LOTLAlert lotlLocationAlert(LOTLSource source) {
        LOTLLocationChangeDetection lotlLocationDetection = new LOTLLocationChangeDetection(source);
        LogLOTLLocationChangeAlertHandler handler = new LogLOTLLocationChangeAlertHandler();
        return new LOTLAlert(lotlLocationDetection, handler);
    }
}

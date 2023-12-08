package eu.domibus;


import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.service.http.commons.FileCacheDataLoader;
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
import eu.europa.esig.dss.tsl.runnable.LOTLAnalysis;
import eu.europa.esig.dss.tsl.runnable.LOTLWithPivotsAnalysis;
import eu.europa.esig.dss.tsl.runnable.TLAnalysis;
import eu.europa.esig.dss.tsl.source.LOTLSource;
import eu.europa.esig.dss.tsl.source.TLSource;
import eu.europa.esig.dss.tsl.sync.AcceptAllStrategy;
import eu.europa.esig.dss.tsl.sync.ExpirationAndSignatureCheckStrategy;
import eu.europa.esig.dss.tsl.sync.SynchronizationStrategy;
import eu.europa.esig.dss.tsl.sync.TrustedListCertificateSourceSynchronizer;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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
public class TrustedListLoadTest {

    private static final Logger LOG = LoggerFactory.getLogger(TrustedListLoadTest.class);
    public static final String HTTPS_EC_EUROPA_EU_TOOLS_LOTL_EU_LOTL_XML = "https://ec.europa.eu/tools/lotl/eu-lotl.xml";
    public static final String DSS_TEST_DIRECTORY = "dss";
    public static final String HTTPS_EC_EUROPA_EU_INFORMATION_SOCIETY_POLICY_ESIGNATURE_TRUSTED_LIST_TL_HTML = "https://ec.europa.eu/information_society/policy/esignature/trusted-list/tl.html";
    public static final String CUSTOM_TRUST_LIST = "customTrustList.xml";
    /**
     * Provides methods to manage the asynchronous behaviour
     */
    private ExecutorService executorService = Executors.newCachedThreadPool();

    private CacheAccessFactory cacheAccessFactory = new CacheAccessFactory();
    private File cacheDirectory;
    FileCacheDataLoader dssFileLoader;
    CertificateSource certificateSource;


    @Before
    public void before() throws IOException {
        String tempDirectoryPath = FileUtils.getTempDirectoryPath();
        cacheDirectory = new File(tempDirectoryPath + File.separator + DSS_TEST_DIRECTORY + File.separator + "cache");
        FileUtils.forceMkdir(cacheDirectory);
        FileUtils.cleanDirectory(cacheDirectory);

        ClassLoader classLoader = getClass().getClassLoader();
        File customTrustList = new File(classLoader.getResource(CUSTOM_TRUST_LIST).getFile());
        FileUtils.copyFile(customTrustList, new File(cacheDirectory, CUSTOM_TRUST_LIST.replace(".", "_")));

        dssFileLoader = new FileCacheDataLoader(new CommonsDataLoader());
        dssFileLoader.setFileCacheDirectory(cacheDirectory);
        certificateSource = getKeyStoreCertificateSource();
    }

    @Test
    public void testTrustedListLoadTest() throws IOException {
        LOTLSource lotlSource = new LOTLSource();
        lotlSource.setUrl(HTTPS_EC_EUROPA_EU_TOOLS_LOTL_EU_LOTL_XML);
        lotlSource.setCertificateSource(certificateSource);
        lotlSource.setSigningCertificatesAnnouncementPredicate(new OfficialJournalSchemeInformationURI(HTTPS_EC_EUROPA_EU_INFORMATION_SOCIETY_POLICY_ESIGNATURE_TRUSTED_LIST_TL_HTML));
        lotlSource.setPivotSupport(true);
        List<LOTLSource> lotlSources=new ArrayList<>();
        lotlSources.add(lotlSource);

        executeLOTLSourcesAnalysis(lotlSources, dssFileLoader);
        List<TLSource> tlSources = new ArrayList<>(extractTlSources(lotlSources));
        executeTLSourcesAnalysis(tlSources, dssFileLoader);
        TrustedListsCertificateSource trustedListsCertificateSource = new TrustedListsCertificateSource();
        synchronizeTLCertificateSource(tlSources.toArray(new TLSource[]{}), lotlSources.toArray(new LOTLSource[]{}),
                trustedListsCertificateSource, new AcceptAllStrategy());

        Collection<File> files = FileUtils.listFiles(cacheDirectory, null, false);
        Assert.assertTrue(files.size() > 1);
        Assert.assertTrue(trustedListsCertificateSource.getNumberOfTrustedPublicKeys() > 0);
    }

    @Test
    public void testSignatureProblem() throws IOException {

        TrustedListsCertificateSource testCertificateSource = getTestCertificateSource(new AcceptAllStrategy());
        Assert.assertEquals(10, testCertificateSource.getNumberOfTrustedPublicKeys());

        testCertificateSource = getTestCertificateSource(new ExpirationAndSignatureCheckStrategy());
        Assert.assertEquals(0, testCertificateSource.getNumberOfTrustedPublicKeys());
    }

    private TrustedListsCertificateSource getTestCertificateSource(SynchronizationStrategy synchronizationStrategy) throws IOException {
        List<TLSource> tlSources = new ArrayList<>();

        TLSource trustedList = new TLSource();
        trustedList.setUrl(CUSTOM_TRUST_LIST);
        trustedList.setCertificateSource(certificateSource);
        tlSources.add(trustedList);

        TrustedListsCertificateSource trustedListsCertificateSource = new TrustedListsCertificateSource();

        executeTLSourcesAnalysis(tlSources, dssFileLoader);
        synchronizeTLCertificateSource(tlSources.toArray(new TLSource[]{}), new LOTLSource[]{}, trustedListsCertificateSource, synchronizationStrategy);

        return trustedListsCertificateSource;
    }

    private CertificateSource getKeyStoreCertificateSource() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("ojkeystore.p12").getFile());
        return new KeyStoreCertificateSource(file, "PKCS12", "dss-password");
    }

    private void executeLOTLSourcesAnalysis(List<LOTLSource> lotlSources, DSSFileLoader dssFileLoader) {
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
                                                TrustedListsCertificateSource certificateSource,
                                                SynchronizationStrategy synchronizationStrategy) {
        LOG.info("Synchronizing certificate source ");
        if (certificateSource == null) {
            LOG.warn("No TrustedListCertificateSource to be synchronized");
            return;
        }

        TrustedListCertificateSourceSynchronizer synchronizer = new TrustedListCertificateSourceSynchronizer(tlSources, lotlSources,
                certificateSource, synchronizationStrategy, cacheAccessFactory.getSynchronizerCacheAccess());
        synchronizer.sync();
        LOG.info("Synchronization done");
    }

    private TLAlert tlSigningAlert() {
        TLSignatureErrorDetection signingDetection = new TLSignatureErrorDetection();
        LogTLSignatureErrorAlertHandler handler = new LogTLSignatureErrorAlertHandler();
        return new TLAlert(signingDetection, handler);
    }

    private TLAlert tlExpirationDetection() {
        TLExpirationDetection expirationDetection = new TLExpirationDetection();
        LogTLExpirationAlertHandler handler = new LogTLExpirationAlertHandler();
        return new TLAlert(expirationDetection, handler);
    }

    private LOTLAlert ojUrlAlert(LOTLSource source) {
        OJUrlChangeDetection ojUrlDetection = new OJUrlChangeDetection(source);
        LogOJUrlChangeAlertHandler handler = new LogOJUrlChangeAlertHandler();
        return new LOTLAlert(ojUrlDetection, handler);
    }

    private LOTLAlert lotlLocationAlert(LOTLSource source) {
        LOTLLocationChangeDetection lotlLocationDetection = new LOTLLocationChangeDetection(source);
        LogLOTLLocationChangeAlertHandler handler = new LogLOTLLocationChangeAlertHandler();
        return new LOTLAlert(lotlLocationDetection, handler);
    }
}

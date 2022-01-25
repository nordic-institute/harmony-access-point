package eu.domibus.core.earchive.eark;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.domibus.api.model.PartInfo;
import eu.domibus.api.model.PartProperty;
import eu.domibus.api.model.RawEnvelopeDto;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.api.earchive.DomibusEArchiveException;
import eu.domibus.core.message.PartInfoService;
import eu.domibus.core.message.nonrepudiation.UserMessageRawEnvelopeDao;
import mockit.Expectations;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Tested;
import org.apache.commons.io.IOUtils;
import org.apache.tika.mime.MimeTypes;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPOutputStream;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;

/**
 * @author François Gautier
 * @since 5.0
 */
@Ignore
@SuppressWarnings({"ResultOfMethodCallIgnored", "TestMethodWithIncorrectSignature"})
public class EArchivingFileServiceTest {

    public static final String RAW_ENVELOPE_CONTENT = "rawEnvelopeDto";

    public static final String CID = "cid:";

    public static final String MESSAGE = "message";

    @Tested
    private EArchivingFileService eArchivingFileService;

    @Injectable
    private UserMessageService userMessageService;

    @Injectable
    private PartInfoService partInfoService;

    @Injectable
    private ObjectMapper objectMapper;

    @Injectable
    private UserMessageRawEnvelopeDao userMessageRawEnvelopeDao;
    private long entityId;

    @Before
    public void setUp() throws Exception {
        entityId = new Random().nextLong();
    }

    @Test
    public void getArchivingFiles_happyFlow(@Injectable RawEnvelopeDto rawEnvelopeDto,
                                            @Injectable PartInfo partInfo1,
                                            @Injectable DataHandler dataHandler,
                                            @Injectable InputStream inputStream) throws IOException {
        List<PartInfo> partInfos = Collections.singletonList(partInfo1);
        new Expectations() {{

            rawEnvelopeDto.getRawXmlMessageAsStream();
            result = new ByteArrayInputStream(RAW_ENVELOPE_CONTENT.getBytes(StandardCharsets.UTF_8));

            userMessageRawEnvelopeDao.findRawXmlByEntityId(entityId);
            result = rawEnvelopeDto;

            partInfoService.findPartInfo(entityId);
            result = partInfos;

            partInfo1.getPayloadDatahandler();
            result = dataHandler;

            dataHandler.getInputStream();
            result = inputStream;

            partInfo1.getMime();
            result = MimeTypes.XML;

            partInfo1.getHref();
            result = CID + MESSAGE;
        }};

        Map<String, InputStream> archivingFiles = eArchivingFileService.getArchivingFiles(entityId);

        new FullVerifications() {
        };

        Assert.assertThat(IOUtils.toString(archivingFiles.get(EArchivingFileService.SOAP_ENVELOPE_XML), StandardCharsets.UTF_8), is(RAW_ENVELOPE_CONTENT));
        Assert.assertThat(archivingFiles.get(MESSAGE + ".attachment.xml"), is(inputStream));
    }

    @Test
    public void getArchivingFiles_compressedAttachment(@Injectable PartInfo partInfo1,
                                            @Injectable DataHandler dataHandler,
                                            @Injectable InputStream inputStream) throws IOException {
        RawEnvelopeDto rawEnvelopeDto = new RawEnvelopeDto(1L, getCompressedBytes(RAW_ENVELOPE_CONTENT), true);
        List<PartInfo> partInfos = Collections.singletonList(partInfo1);
        new Expectations() {{

            userMessageRawEnvelopeDao.findRawXmlByEntityId(entityId);
            result = rawEnvelopeDto;

            partInfoService.findPartInfo(entityId);
            result = partInfos;

            partInfo1.getPayloadDatahandler();
            result = dataHandler;

            dataHandler.getInputStream();
            result = inputStream;

            partInfo1.getMime();
            result = MimeTypes.XML;

            partInfo1.getHref();
            result = CID + MESSAGE;
        }};

        Map<String, InputStream> archivingFiles = eArchivingFileService.getArchivingFiles(entityId);

        new FullVerifications() {
        };

        Assert.assertThat(IOUtils.toString(archivingFiles.get(EArchivingFileService.SOAP_ENVELOPE_XML), StandardCharsets.UTF_8), is(RAW_ENVELOPE_CONTENT));
        Assert.assertThat(archivingFiles.get(MESSAGE + ".attachment.xml"), is(inputStream));
    }

    @Test
    public void getArchivingFiles_partInfoWithoutDataHandler(@Injectable RawEnvelopeDto rawEnvelopeDto,
                                                             @Injectable PartInfo partInfo1,
                                                             @Injectable DataHandler dataHandler,
                                                             @Injectable InputStream inputStream) {
        List<PartInfo> partInfos = Collections.singletonList(partInfo1);
        new Expectations() {{

            rawEnvelopeDto.getRawXmlMessageAsStream();
            result = new ByteArrayInputStream(RAW_ENVELOPE_CONTENT.getBytes(StandardCharsets.UTF_8));

            userMessageRawEnvelopeDao.findRawXmlByEntityId(entityId);
            result = rawEnvelopeDto;

            partInfoService.findPartInfo(entityId);
            result = partInfos;

            partInfo1.getPayloadDatahandler();
            result = null;

            partInfo1.getHref();
            result = CID + MESSAGE;

            partInfo1.getMime();
            result = MimeTypes.XML;

        }};

        try {
            eArchivingFileService.getArchivingFiles(entityId);
            Assert.fail();
        } catch (DomibusEArchiveException e) {
            //ok
        }

        new FullVerifications() {
        };
    }

    @Test
    public void getArchivingFiles_partInfoIOException(@Injectable RawEnvelopeDto rawEnvelopeDto,
                                                      @Injectable PartInfo partInfo1,
                                                      @Injectable DataHandler dataHandler,
                                                      @Injectable InputStream inputStream) throws IOException {
        List<PartInfo> partInfos = Collections.singletonList(partInfo1);
        new Expectations() {{

            rawEnvelopeDto.getRawXmlMessageAsStream();
            result = new ByteArrayInputStream(RAW_ENVELOPE_CONTENT.getBytes(StandardCharsets.UTF_8));

            userMessageRawEnvelopeDao.findRawXmlByEntityId(entityId);
            result = rawEnvelopeDto;

            partInfoService.findPartInfo(entityId);
            result = partInfos;

            partInfo1.getPayloadDatahandler();
            result = dataHandler;

            dataHandler.getInputStream();
            result = new IOException();

            partInfo1.getMime();
            result = MimeTypes.XML;

            partInfo1.getHref();
            result = CID + MESSAGE;
        }};

        try {
            eArchivingFileService.getArchivingFiles(entityId);
            Assert.fail();
        } catch (DomibusEArchiveException e) {
            //ok
        }

        new FullVerifications() {
        };
    }

    @Test
    public void getFile(
            @Injectable PartInfo partInfo,
            @Injectable PartProperty partProperty1,
            @Injectable PartProperty partProperty2,
            @Injectable DataSource source,
            @Injectable InputStream inputStream

    ) throws IOException {
        Set<PartProperty> properties = new HashSet<>();
        properties.add(partProperty1);
        properties.add(partProperty2);


        byte[] compressedBytes = getCompressedBytes(RAW_ENVELOPE_CONTENT);

        new Expectations(){{

            partInfo.getPayloadDatahandler().getInputStream();
            result = new ByteArrayInputStream(compressedBytes);

        }};
        InputStream file = eArchivingFileService.getInputStream(1L, partInfo);

        assertNotNull("message.attachment.xml", file);

        new FullVerifications(){};
    }

    private byte[] getCompressedBytes(String rawEnvelopeContent) throws IOException {
        byte[] compressedBytes;

        // Compress it
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()){
            try(OutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
                gzipOutputStream.write(rawEnvelopeContent.getBytes(StandardCharsets.UTF_8));
            }
            compressedBytes = byteArrayOutputStream.toByteArray();
        }
        return compressedBytes;
    }
}
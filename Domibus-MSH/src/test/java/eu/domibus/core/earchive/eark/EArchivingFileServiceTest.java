package eu.domibus.core.earchive.eark;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.domibus.api.model.PartInfo;
import eu.domibus.api.model.RawEnvelopeDto;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.core.earchive.DomibusEArchiveException;
import eu.domibus.core.message.PartInfoService;
import eu.domibus.core.message.nonrepudiation.UserMessageRawEnvelopeDao;
import mockit.Expectations;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Tested;
import org.apache.commons.io.IOUtils;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeTypes;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.activation.DataHandler;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;

/**
 * @author François Gautier
 * @since 5.0
 */
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
    public void getFileName_ok(@Injectable PartInfo partInfo) {
        new Expectations() {{
            partInfo.getMime();
            result = MimeTypes.XML;

            partInfo.getHref();
            result = CID + MESSAGE;
        }};
        String fileName = eArchivingFileService.getFileName(partInfo);

        assertEquals(MESSAGE + ".attachment.xml", fileName);
    }

    @Test
    public void getFileName_noExtension(@Injectable PartInfo partInfo) {
        new Expectations() {{
            partInfo.getMime();
            result = "NoExtension";

            partInfo.getHref();
            result = CID + MESSAGE;
        }};
        String fileName = eArchivingFileService.getFileName(partInfo);

        assertEquals(MESSAGE + ".attachment", fileName);
    }

    @Test
    public void getFileName_nohref(@Injectable PartInfo partInfo) {
        new Expectations() {{
            partInfo.getMime();
            result = MimeTypes.XML;

            partInfo.getHref();
            result = null;
        }};
        String fileName = eArchivingFileService.getFileName(partInfo);

        assertEquals("bodyload.attachment.xml", fileName);
    }

    @Test
    public void getFileName_hrefNoCid(@Injectable PartInfo partInfo) {
        new Expectations() {{
            partInfo.getMime();
            result = MimeTypes.XML;

            partInfo.getHref();
            result = "NOCID";
        }};
        String fileName = eArchivingFileService.getFileName(partInfo);

        assertEquals("NOCID.attachment.xml", fileName);
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
            result = MediaType.OCTET_STREAM.getType();
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


}
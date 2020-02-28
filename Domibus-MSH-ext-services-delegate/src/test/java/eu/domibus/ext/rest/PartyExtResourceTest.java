package eu.domibus.ext.rest;

import eu.domibus.ext.domain.PartyDTO;
import eu.domibus.ext.domain.PartyFilterRequestDTO;
import eu.domibus.ext.exceptions.DomibusErrorCode;
import eu.domibus.ext.exceptions.PartyExtServiceException;
import eu.domibus.ext.services.PartyExtService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Catalin Enache
 * @since 4.2
 */
@RunWith(JMockit.class)
public class PartyExtResourceTest {

    @Tested
    PartyExtResource partyExtResource;

    @Injectable
    PartyExtService partyExtService;

    @Test
    public void test_listParties(final @Mocked PartyFilterRequestDTO partyFilterRequestDTO) {
        final String partyName = "domibus-blue";

        new Expectations() {{
            partyFilterRequestDTO.getPageStart();
            result = 1;

            partyFilterRequestDTO.getPageSize();
            result = 12;

            partyFilterRequestDTO.getName();
            result = partyName;
        }};

        //tested method
        partyExtResource.listParties(partyFilterRequestDTO);

        new FullVerifications(partyExtResource) {{
            String partyNameActual;
            int pageStartActual, pageSizeActual;
            partyExtService.getParties(partyNameActual = withCapture(),
                    anyString, anyString,
                    anyString, pageStartActual = withCapture(), pageSizeActual = withCapture());
            Assert.assertEquals(partyName, partyNameActual);
            Assert.assertEquals(1, pageStartActual);
            Assert.assertEquals(12, pageSizeActual);
        }};


    }

    @Test
    public void test_createParty(final @Mocked PartyDTO partyDTO) {
        //tested method
        partyExtResource.createParty(partyDTO);

        new FullVerifications(partyExtService) {{
            partyExtService.createParty(partyDTO);
        }};
    }

    @Test
    public void test_createParty_Exception(final @Mocked PartyDTO partyDTO) {

        new Expectations() {{
            partyExtService.createParty(partyDTO);
            result = new PartyExtServiceException(DomibusErrorCode.DOM_001, "test");

        }};

        //tested method
        try {
            partyExtResource.createParty(partyDTO);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof PartyExtServiceException);
        }
    }

    @Test
    public void test_deleteParty() {
        final String partyName = "domibus-red";

        //tested method
        partyExtResource.deleteParty(partyName);

        new FullVerifications() {{
            String partyNameActual;
            partyExtService.deleteParty(partyNameActual = withCapture());
            Assert.assertEquals(partyName, partyNameActual);
        }};
    }

    @Test
    public void test_getCertificateForParty() {
        final String partyName = "domibus-red";

        //tested method
        partyExtResource.getCertificateForParty(partyName);

        new FullVerifications() {{
            String partyNameActual;
            partyExtService.getPartyCertificateFromTruststore(partyNameActual = withCapture());
            Assert.assertEquals(partyName, partyNameActual);
        }};
    }

    @Test
    public void test_getCertificateForParty_NotFound() {
        final String partyName = "domibus-red";

        new Expectations() {{
            partyExtService.getPartyCertificateFromTruststore(partyName);
            result = null;
        }};

        //tested method
        partyExtResource.getCertificateForParty(partyName);

        new FullVerifications() {{
        }};
    }

    @Test
    public void test_listProcesses(final @Mocked PartyFilterRequestDTO partyFilterRequestDTO) {
        final String partyName = "domibus-blue";

        new Expectations() {{
            partyFilterRequestDTO.getName();
            result = partyName;
        }};

        //tested method
        partyExtResource.listParties(partyFilterRequestDTO);

        new FullVerifications(partyExtService) {{
            String partyNameActual;
            partyExtService.getParties(partyNameActual = withCapture(), anyString,
                    anyString, anyString, anyInt, anyInt);
            Assert.assertEquals(partyName, partyNameActual);
        }};
    }

    @Test
    public void test_updateParties(final @Mocked PartyDTO partyDTO) {

        //tested method
        partyExtResource.updateParty(partyDTO);

        new FullVerifications(partyExtService) {{
            PartyDTO partyDTOActual;
            partyExtService.updateParty(partyDTOActual = withCapture());
            Assert.assertNotNull(partyDTOActual);
        }};
    }
}
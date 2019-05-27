package domibus;

import domibus.ui.BaseTest;
import org.testng.annotations.Test;
import utils.soap_client.DomibusC1;

/**
 * @author Catalin Comanici
 * @description:
 * @since 4.1
 */
public class MyTest extends BaseTest {

	@Test
	public void testProp() throws Exception {
		new DomibusC1().sendMessage("admin", "QW!@qw12", "sdsd", "convas");
	}




}

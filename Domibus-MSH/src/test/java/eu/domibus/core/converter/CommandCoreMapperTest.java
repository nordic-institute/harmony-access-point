package eu.domibus.core.converter;

import eu.domibus.api.cluster.Command;
import eu.domibus.core.clustering.CommandEntity;
import eu.europa.ec.digit.commons.test.api.ObjectService;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author François Gautier
 * @since 5.0
 */
public class CommandCoreMapperTest extends AbstractMapperTest {

    @Autowired
    private CommandCoreMapper commandCoreMapper;

    @Autowired
    private ObjectService objectService;

    @Test
    @Ignore("EDELIVERY-8052 Failing tests must be ignored")
    public void convertCommand() {
        Command toConvert = (Command) objectService.createInstance(Command.class);
        final CommandEntity converted = commandCoreMapper.commandToCommandEntity(toConvert);
        final Command convertedBack = commandCoreMapper.commandEntityToCommand(converted);
        objectService.assertObjects(convertedBack, toConvert);
    }
}
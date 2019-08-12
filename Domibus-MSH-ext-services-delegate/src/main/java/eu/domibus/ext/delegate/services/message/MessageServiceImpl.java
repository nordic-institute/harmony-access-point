package eu.domibus.ext.delegate.services.message;

import eu.domibus.ext.exceptions.MessageExtException;
import eu.domibus.ext.services.MessageExtService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Sebastian-Ion TINCU
 * @since 4.1
 */
@Service
public class MessageServiceImpl implements MessageExtService {

    static String VALIDMIDPATTERN = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*";
    private static Pattern patvalidmid = Pattern.compile(VALIDMIDPATTERN);

    /**
     * {@inheritDoc}
     */
    @Override
    public String cleanMessageIdentifier(String messageId) {
        return StringUtils.trimToEmpty(messageId);
    }

    @Override
    public boolean validateMessageIdentifierRfc2822(String messageId) throws MessageExtException {
        String mid = messageId;
        if (StringUtils.countMatches(mid, "<") > 1)
            return false;
        if (StringUtils.countMatches(mid, ">") > 1)
            return false;
        if (StringUtils.containsAny(mid, "<>")) {
            mid = StringUtils.substringBetween(mid, "<", ">");
            if (StringUtils.isBlank(mid)) {
                return false;
            }
        }
        if (StringUtils.contains(mid, "..")) {
            return false;
        }
        //extract from <>
        mid = mid.trim();
        //now validate
        Matcher m = patvalidmid.matcher(mid);
        return m.matches();
    }
}

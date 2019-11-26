package eu.domibus.core.payload.temp;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
@RunWith(JMockit.class)
public class RegexIOFileFilterTest {

    @Injectable
    Pattern pattern;

    @Tested
    RegexIOFileFilter regexIOFileFilter;

    @Test
    public void acceptFile(@Injectable File file) {
        String myFile = "myFile";
        new Expectations(regexIOFileFilter) {{
            file.getName();
            result = myFile;

            regexIOFileFilter.accept(anyString);
            result = true;
        }};

        final boolean accept = regexIOFileFilter.accept(file);

        new Verifications() {{
            regexIOFileFilter.accept(myFile);
            Assert.assertTrue(accept);
        }};

    }

    @Test
    public void accept(@Injectable Matcher matcher) {
        String myFile = "myFile";

        new Expectations() {{
            pattern.matcher(myFile);
            result = matcher;

            matcher.matches();
            result = true;
        }};

        Assert.assertTrue(regexIOFileFilter.accept(myFile));
    }
}
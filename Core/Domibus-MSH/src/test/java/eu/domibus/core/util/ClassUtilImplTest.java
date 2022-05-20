package eu.domibus.core.util;

import mockit.Tested;
import org.junit.Assert;
import org.junit.Test;

public class ClassUtilImplTest {
    @Tested
    ClassUtilImpl classUtil;

    @Test
    public void isMethodDefined() {
        boolean res = classUtil.isMethodDefined(this, "isMethodDefined", new Class[]{});
        Assert.assertTrue(res);

        res = classUtil.isMethodDefined(this, "isMethodDefined2", new Class[]{});
        Assert.assertFalse(res);

        res = classUtil.isMethodDefined(this, "isMethodDefined", new Class[]{String.class});
        Assert.assertFalse(res);
    }
}
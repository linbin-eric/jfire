package cc.jfire.jfire.test.function;

import cc.jfire.jfire.core.ApplicationContext;
import cc.jfire.jfire.test.function.importtest.Node2;
import cc.jfire.jfire.test.function.importtest.Root;
import org.junit.Assert;
import org.junit.Test;

public class ImportTest
{
    @Test
    public void test()
    {
        ApplicationContext boot = ApplicationContext.boot(Root.class);
        Node2              bean = boot.getBean(Node2.class);
        Assert.assertNotNull(bean);
    }
}

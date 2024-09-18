package com.jfirer.jfire.test.function;

import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.test.function.importtest.Node2;
import com.jfirer.jfire.test.function.importtest.Root;
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

package cc.jfire.jfire.test.function;

import cc.jfire.jfire.core.ApplicationContext;
import cc.jfire.jfire.test.function.importtest.Node2;
import cc.jfire.jfire.test.function.importtest.Root;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ImportTest
{
    @Test
    public void test()
    {
        ApplicationContext boot = ApplicationContext.boot(Root.class);
        Node2              bean = boot.getBean(Node2.class);
        Assertions.assertNotNull(bean);
    }
}

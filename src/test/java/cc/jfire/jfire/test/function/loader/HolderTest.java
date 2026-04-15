package cc.jfire.jfire.test.function.loader;

import cc.jfire.jfire.core.ApplicationContext;
import cc.jfire.jfire.core.DefaultApplicationContext;
import cc.jfire.jfire.core.prepare.annotation.ComponentScan;
import cc.jfire.jfire.core.prepare.annotation.configuration.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HolderTest
{
    @Test
    public void test()
    {
        ApplicationContext context = new DefaultApplicationContext(HolderTestScan.class);
        Person             person  = context.getBean(Person.class);
        Assertions.assertEquals("name", person.getName());
        Home home = context.getBean(Home.class);
        Assertions.assertEquals(100, home.getLength());
    }

    @Configuration
    @ComponentScan("cc.jfire.jfire.test.function.loader")
    public static class HolderTestScan
    {

    }
}

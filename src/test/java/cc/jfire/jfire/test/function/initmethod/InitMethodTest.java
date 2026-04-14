package cc.jfire.jfire.test.function.initmethod;

import cc.jfire.jfire.core.ApplicationContext;
import cc.jfire.jfire.core.DefaultApplicationContext;
import cc.jfire.jfire.core.prepare.annotation.ComponentScan;
import cc.jfire.jfire.core.prepare.annotation.configuration.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InitMethodTest
{
    @Test
    public void test()
    {
        ApplicationContext context = new DefaultApplicationContext(InitMethodTestScan.class);
        Person             person  = context.getBean(Person.class);
        Assertions.assertEquals(23, person.getAge());
        Assertions.assertEquals("林斌", person.getName());
    }

    @Configuration
    @ComponentScan("cc.jfire.jfire.test.function.initmethod")
    public static class InitMethodTestScan
    {

    }
}

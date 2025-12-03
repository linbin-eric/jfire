package cc.jfire.jfire.test.function.initmethod;

import cc.jfire.jfire.core.ApplicationContext;
import cc.jfire.jfire.core.DefaultApplicationContext;
import cc.jfire.jfire.core.prepare.annotation.ComponentScan;
import cc.jfire.jfire.core.prepare.annotation.configuration.Configuration;
import org.junit.Assert;
import org.junit.Test;

public class InitMethodTest
{
    @Test
    public void test()
    {
        ApplicationContext context = new DefaultApplicationContext(InitMethodTestScan.class);
        Person             person  = context.getBean(Person.class);
        Assert.assertEquals(23, person.getAge());
        Assert.assertEquals("林斌", person.getName());
    }

    @Configuration
    @ComponentScan("com.jfirer.jfire.test.function.initmethod")
    public static class InitMethodTestScan
    {

    }
}

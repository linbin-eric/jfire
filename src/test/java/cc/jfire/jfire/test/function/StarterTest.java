package cc.jfire.jfire.test.function;

import cc.jfire.jfire.core.ApplicationContext;
import cc.jfire.jfire.core.DefaultApplicationContext;
import cc.jfire.jfire.core.prepare.annotation.EnableAutoConfiguration;
import cc.jfire.jfire.core.prepare.annotation.configuration.Bean;
import cc.jfire.jfire.core.prepare.annotation.configuration.Configuration;
import org.junit.Assert;
import org.junit.Test;

@EnableAutoConfiguration
@Configuration
public class StarterTest
{
    @Test
    public void test()
    {
        ApplicationContext context   = new DefaultApplicationContext(StarterTest.class);
        MyStarter          myStarter = context.getBean(MyStarter.class);
        Assert.assertNotNull(myStarter);
        WithStarter withStarter = context.getBean(WithStarter.class);
        Assert.assertNotNull(withStarter);
    }

    @Configuration
    public static class MyStarter
    {
        @Bean
        public WithStarter withStarter()
        {
            return new WithStarter();
        }
    }

    public static class WithStarter
    {

    }
}

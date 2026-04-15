package cc.jfire.jfire.test.function;

import cc.jfire.jfire.core.ApplicationContext;
import cc.jfire.jfire.core.DefaultApplicationContext;
import cc.jfire.jfire.core.prepare.annotation.EnableAutoConfiguration;
import cc.jfire.jfire.core.prepare.annotation.configuration.Bean;
import cc.jfire.jfire.core.prepare.annotation.configuration.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@EnableAutoConfiguration
@Configuration
public class StarterTest
{
    @Test
    public void test()
    {
        ApplicationContext context   = new DefaultApplicationContext(StarterTest.class);
        MyStarter          myStarter = context.getBean(MyStarter.class);
        Assertions.assertNotNull(myStarter);
        WithStarter withStarter = context.getBean(WithStarter.class);
        Assertions.assertNotNull(withStarter);
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

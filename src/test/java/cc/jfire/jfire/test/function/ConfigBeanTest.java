package cc.jfire.jfire.test.function;

import cc.jfire.jfire.core.ApplicationContext;
import cc.jfire.jfire.core.DefaultApplicationContext;
import cc.jfire.jfire.core.prepare.annotation.configuration.Bean;
import cc.jfire.jfire.core.prepare.annotation.configuration.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Configuration
public class ConfigBeanTest
{
    @Bean
    public Person person()
    {
        return new Person();
    }

    @Bean
    public Home home(Person person)
    {
        Home home = new Home();
        home.person = person;
        return home;
    }

    @Test
    public void test()
    {
        ApplicationContext context = new DefaultApplicationContext(ConfigBeanTest.class);
        Home               home    = context.getBean(Home.class);
        Person             person  = context.getBean(Person.class);
        Assertions.assertNotNull(person);
        Assertions.assertSame(person, home.person);
    }

    public static class Home
    {
        private Person person;
    }

    public static class Person
    {}
}

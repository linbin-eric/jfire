package cc.jfire.jfire.test.function.beanannotest;

import cc.jfire.jfire.core.ApplicationContext;
import cc.jfire.jfire.core.DefaultApplicationContext;
import cc.jfire.jfire.exception.BeanDefinitionCanNotFindException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class BeanAnnoTest
{
    @Test
    public void test()
    {
        ApplicationContext context = new DefaultApplicationContext(Data.class);
        Person             person  = context.getBean("person");
        Assertions.assertNotNull(person);
        Person person2;
        try
        {
            person2 = context.getBean("person2");
        }
        catch (Exception e)
        {
            assertTrue(e instanceof BeanDefinitionCanNotFindException);
        }
        context = new DefaultApplicationContext(Data.class);
        Properties properties = new Properties();
        properties.put("person2", "pass");
        context.getConfig().addProperty("person2","pass");
        person2 = context.getBean("person2");
        Assertions.assertNotNull(person2);
        Person person4 = context.getBean("person4");
        Assertions.assertEquals("linbin", person4.getName());
        Person person5 = context.getBean("person5");
        Assertions.assertEquals("2", person5.getName());
        Assertions.assertEquals("2", context.getBean(NeedPerson5.class).getPerson().getName());
        Person person6 = context.getBean("person6");
        Assertions.assertEquals("myimport", person6.getName());
        Person person7 = context.getBean("person7");
        Assertions.assertEquals("house2", person7.getName());
        Person person7_2 = context.getBean("person7");
        Assertions.assertEquals(person7, person7_2);
    }
}

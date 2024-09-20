package com.jfirer.jfire.test.function.condition;

import com.jfirer.baseutil.Resource;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.DefaultApplicationContext;
import com.jfirer.jfire.core.prepare.annotation.ComponentScan;
import com.jfirer.jfire.core.prepare.annotation.condition.provide.ConditionOnMissBeanType;
import com.jfirer.jfire.core.prepare.annotation.condition.provide.ConditionOnProperty;
import com.jfirer.jfire.core.prepare.annotation.configuration.Bean;
import com.jfirer.jfire.core.prepare.annotation.configuration.Configuration;
import com.jfirer.jfire.exception.BeanDefinitionCanNotFindException;
import org.junit.Assert;
import org.junit.Test;

@ComponentScan("com.jfirer.jfire.test.function.condition")
@Configuration
public class ConditionTest
{
    @Bean
    public Demo2 demo2()
    {
        return new Demo2();
    }

    @Bean
    @ConditionOnMissBeanType(Demo2.class)
    public Demo1 demo1()
    {
        return new Demo1();
    }

    @Test
    public void test()
    {
        DefaultApplicationContext applicationContext = new DefaultApplicationContext(ConditionTest.class);
        Demo1                     demo1              = null;
        try
        {
            demo1 = applicationContext.getBean(Demo1.class);
            Assert.fail();
        }
        catch (Exception e)
        {
            Assert.assertTrue(e instanceof BeanDefinitionCanNotFindException);
        }
        Demo2 demo2 = applicationContext.getBean(Demo2.class);
        Assert.assertNotNull(demo2);
    }

    static class Demo1
    {
    }

    static class Demo2
    {
    }

    @Test
    public void test2()
    {
        ApplicationContext context = ApplicationContext.boot(Test3.class);
        try
        {
            context.getBean(Demo1.class);
            Assert.fail();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Assert.assertTrue(e instanceof BeanDefinitionCanNotFindException);
        }
    }

    @Configuration
    @Resource
    @ConditionOnProperty("dev")
    public static class Test3
    {
        @Bean
        public Demo1 demo1()
        {
            return new Demo1();
        }
    }
}

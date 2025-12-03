package cc.jfire.jfire.test.function.cyclicDependenceTest;

import cc.jfire.baseutil.Resource;
import cc.jfire.jfire.core.ApplicationContext;
import cc.jfire.jfire.core.prepare.annotation.ComponentScan;
import cc.jfire.jfire.core.prepare.annotation.configuration.Bean;
import cc.jfire.jfire.core.prepare.annotation.configuration.Configuration;
import org.junit.Assert;
import org.junit.Test;

@Configuration
@Resource
@ComponentScan("com.jfirer.jfire.test.function.cyclicDependenceTest")
public class CyclicDependenceTest
{
    @Configuration
    @Resource
    public static class A
    {
        public static class FieldA
        {
        }

        @Resource
        FieldA a;

        @Bean
        B.FieldB fieldB()
        {
            return new B.FieldB();
        }
    }

    @Configuration
    @Resource
    public static class B
    {
        public static class FieldB
        {
        }

        @Resource
        FieldB b;

        @Bean
        A.FieldA fieldA()
        {
            return new A.FieldA();
        }
    }

    @Test
    public void test()
    {
        ApplicationContext applicationContext = ApplicationContext.boot(CyclicDependenceTest.class);
        try
        {
            applicationContext.getBean(A.class);
            Assert.fail();
        }
        catch (Exception e)
        {
            Assert.assertTrue(e instanceof IllegalStateException);
        }
    }
}

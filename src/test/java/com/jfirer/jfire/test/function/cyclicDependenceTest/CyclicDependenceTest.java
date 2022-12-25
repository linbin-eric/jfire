package com.jfirer.jfire.test.function.cyclicDependenceTest;

import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.prepare.annotation.ComponentScan;
import com.jfirer.jfire.core.prepare.annotation.configuration.Bean;
import com.jfirer.jfire.core.prepare.annotation.configuration.Configuration;
import org.junit.Test;

import javax.annotation.Resource;

@Configuration
@Resource
@ComponentScan("com.jfirer.jfire.test.function.cyclicDependenceTest")
public class CyclicDependenceTest
{
    @Configuration
    @Resource
    public static class A
    {
        public static class FieldA {}

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
        public static class FieldB {}

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
        applicationContext.getBean(A.class);
    }
}

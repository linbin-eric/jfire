package com.jfirer.jfire.test.function;

import com.jfirer.baseutil.Resource;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.aop.ProceedPoint;
import com.jfirer.jfire.core.aop.ProceedPointImpl;
import com.jfirer.jfire.core.aop.notated.Around;
import com.jfirer.jfire.core.aop.notated.EnhanceClass;
import com.jfirer.jfire.core.inject.BeanHolder;
import org.junit.Assert;
import org.junit.Test;

public class BeanHolderTest
{
    @Test
    public void test()
    {
        ApplicationContext applicationContext = ApplicationContext.boot();
        applicationContext.register(TestBean.class);
        applicationContext.register(Enhance.class);
        TestBean testBean = applicationContext.getBean(TestBean.class);
        testBean.say2();
        Assert.assertEquals(1, testBean.getAge());
        testBean.say3();
        Assert.assertEquals(3, testBean.getAge());
    }

    @EnhanceClass("com.jfirer.jfire.test.function.BeanHolderTest$TestBean")
    public static class Enhance
    {
        @Around("say()")
        public void after(ProceedPoint proceedPoint)
        {
            proceedPoint.invoke();
            ((ProceedPointImpl) proceedPoint).setResult(3);
        }
    }

    public static class TestBean
    {
        private int                  age;
        @Resource
        private BeanHolder<TestBean> beanHolder;

        public int say()
        {
            return 1;
        }

        public void say2()
        {
            age = say();
        }

        public void say3()
        {
            age = beanHolder.getSelf().say();
        }

        public int getAge()
        {
            return age;
        }
    }
}

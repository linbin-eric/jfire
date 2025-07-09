package com.jfirer.jfire.test.function.aop;

import com.jfirer.baseutil.Verify;
import com.jfirer.jfire.core.aop.ProceedPoint;
import com.jfirer.jfire.core.aop.ProceedPointImpl;
import com.jfirer.jfire.core.aop.notated.*;
import com.jfirer.jfire.core.aop.notated.support.MatchTargetMethod;
import org.junit.Assert;

import java.lang.reflect.Method;

@EnhanceClass("com.jfirer.jfire.*.aop.Person")
public class Enhance
{
    private int     order;
    private String  param;
    private Object  result;
    private boolean findTestForCustom = false;

    public int getOrder()
    {
        return order;
    }

    public String getParam()
    {
        return param;
    }

    @Before("sayHello(String)")
    public void sayHello(ProceedPoint point)
    {
        param = (String) point.getParams()[0];
        System.out.println("前置拦截");
    }

    @Around("testInts(int[])")
    public void test(ProceedPoint point)
    {
        System.out.println("asdasd");
        point.invoke();
        Assert.assertEquals(((int[]) point.getParams()[0]).length, ((String[]) point.getResult()).length);
        Assert.assertEquals(3,((String[]) point.getResult()).length);
        point.setResult(new String[]{"1","2"});
    }

    @Before(value = "order()")
    public void order2(ProceedPoint point)
    {
        EnhanceForOrder.result += "enhance";
    }

    public Object getResult()
    {
        return result;
    }

    @AfterReturning("order2(String,int)")
    public void order22(ProceedPoint point)
    {
        result = point.getResult();
    }

    @Around("myName(String)")
    public void testMyname(ProceedPoint point)
    {
        System.out.println("环绕增强钱");
        point.invoke();
        System.out.println("环绕增强后");
    }

    @Around("testForVoidReturn()")
    public void testForVoidReturn(ProceedPoint point)
    {
        System.out.println("环绕增强钱");
        point.invoke();
        point.invoke();
        System.out.println("环绕增强后");
    }

    @AfterThrowable("throwe(*)")
    public void throwe(ProceedPoint point)
    {
        System.out.println("dada");
        Verify.equal("aaaa", point.getE().getMessage(), "捕获到正确的异常");
    }

    @Before("getHomeName()")
    public void home1(ProceedPoint point)
    {
        System.out.println("before getHomeName");
    }

    @Before("getHome()")
    public void home2(ProceedPoint point)
    {
        System.out.println("before getHome");
    }

    public static class findTestForCustom implements MatchTargetMethod
    {
        @Override
        public boolean match(Method method)
        {
            return method.getName().equals("testForCustom");
        }
    }

    @Before(custom = findTestForCustom.class)
    public void findTestForCustom()
    {
        findTestForCustom = true;
    }

    @EnhanceClass(value = "com.jfirer.jfire.*.aop.Person", order = 2)
    public static class EnhanceForOrder
    {
        public static String result = "";

        @Before(value = "order()")
        public void order2(ProceedPoint point)
        {
            result += "EnhanceForOrder_";
        }
    }

    public boolean isFindTestForCustom()
    {
        return findTestForCustom;
    }
}

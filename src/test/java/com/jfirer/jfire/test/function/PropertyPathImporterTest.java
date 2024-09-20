package com.jfirer.jfire.test.function;

import com.jfirer.baseutil.Resource;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.inject.notated.PropertyRead;
import com.jfirer.jfire.core.prepare.annotation.PropertyPath;
import com.jfirer.jfire.core.prepare.annotation.configuration.Configuration;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

@Resource
public class PropertyPathImporterTest
{
    @PropertyRead
    private int                 age;
    @PropertyRead("person.name")
    private String              name;
    @PropertyRead("person.map")
    private Map<String, String> map;


    @Test
    public void test3()
    {
        ApplicationContext context = ApplicationContext.boot(Test3.class);
        context.register(PropertyPathImporterTest.class);
        PropertyPathImporterTest bean = context.getBean(PropertyPathImporterTest.class);
        Assert.assertEquals("test", bean.name);
        Assert.assertEquals("你好", bean.map.get("综合"));
    }



    @Configuration
    @PropertyPath("classpath:propertiestest.yml")
    public static class Test3 {}
}

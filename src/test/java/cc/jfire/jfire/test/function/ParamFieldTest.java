package cc.jfire.jfire.test.function;

import cc.jfire.baseutil.Resource;
import cc.jfire.jfire.core.ApplicationContext;
import cc.jfire.jfire.core.DefaultApplicationContext;
import cc.jfire.jfire.core.inject.notated.PropertyRead;
import cc.jfire.jfire.core.prepare.ContextPrepare;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Resource
public class ParamFieldTest
{
    @PropertyRead
    private int[]       f1;
    @PropertyRead
    private String      f2;
    @PropertyRead
    private Integer     f3;
    @PropertyRead
    private int         f4;
    @PropertyRead
    private Long        f5;
    @PropertyRead
    private long        f6;
    @PropertyRead
    private Boolean     f7;
    @PropertyRead
    private boolean     f8;
    @PropertyRead
    private float       f9;
    @PropertyRead
    private Float       f10;
    @PropertyRead
    private String[]    f11;
    @PropertyRead
    private Set<String> f12;
    @PropertyRead
    private Class<?>    f13;
    @PropertyRead
    private name        f14;

    @Test
    public void test()
    {
        ApplicationContext context = new DefaultApplicationContext();
        context.register(ForProperty.class);
        context.register(ParamFieldTest.class);
        context.register(ForProperty.class);
        ParamFieldTest data = context.getBean(ParamFieldTest.class);
        Assert.assertArrayEquals(new int[]{1, 2}, data.f1);
        Assert.assertEquals("aaa", data.f2);
        Assert.assertEquals(1, data.f3.intValue());
        Assert.assertEquals(2, data.f4);
        Assert.assertEquals(3, data.f5.intValue());
        Assert.assertEquals(4, data.f6);
        Assert.assertTrue(data.f7);
        Assert.assertFalse(data.f8);
        Assert.assertEquals(2.65, data.f9, 0.001);
        Assert.assertEquals(2.35, data.f10.floatValue(), 0.001);
        Assert.assertArrayEquals(new String[]{"ni", "sx"}, data.f11);
        Set<String> set = new HashSet<String>();
        set.add("xx");
        set.add("rr");
        Assert.assertEquals(set, data.f12);
        Assert.assertEquals(ParamFieldTest.class, data.f13);
        Assert.assertEquals(name.test1, data.f14);
    }

    public enum name
    {
        test1, test2
    }

    public static class ForProperty implements ContextPrepare
    {
        @Override
        public ApplicationContext.FoundNewContextPrepare prepare(ApplicationContext context)
        {
            Map<String,Object> environment = context.getConfig().fullPathConfig();
            environment.put("f1", "1,2");
            environment.put("f2", "aaa");
            environment.put("f3", "1");
            environment.put("f4", "2");
            environment.put("f5", "3");
            environment.put("f6", "4");
            environment.put("f7", "true");
            environment.put("f8", "false");
            environment.put("f9", "2.65");
            environment.put("f10", "2.35");
            environment.put("f11", "ni,sx");
            environment.put("f12", "xx,rr");
            environment.put("f13", ParamFieldTest.class.getName());
            environment.put("f14", "test1");
            return ApplicationContext.FoundNewContextPrepare.NO;
        }

        @Override
        public int order()
        {
            return 0;
        }
    }
}

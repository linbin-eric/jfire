package cc.jfire.jfire.test.function.cachetest;

import cc.jfire.jfire.core.ApplicationContext;
import cc.jfire.jfire.core.DefaultApplicationContext;
import cc.jfire.jfire.test.function.base.data.House;
import org.junit.Test;

public class NewCacheTest
{
    @Test
    public void test()
    {
        ApplicationContext context = new DefaultApplicationContext();
        context.register(CacheTarget.class);
        context.register(DemoCache.class);
        context.register(CacheManagerTest.class);
        CacheTarget cacheTarget = context.getBean(CacheTarget.class);
        House       house       = cacheTarget.get(5);
        System.out.println(house);
        house = cacheTarget.get(5);
        System.out.println(house);
    }
}

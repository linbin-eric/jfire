package cc.jfire.jfire.test.function.cachetest;

import cc.jfire.jfire.core.ApplicationContext;
import cc.jfire.jfire.core.DefaultApplicationContext;
import cc.jfire.jfire.core.aop.impl.support.cache.ConcurrentMapCacheManager;
import cc.jfire.jfire.core.prepare.annotation.EnableCacheManager;
import cc.jfire.jfire.core.prepare.annotation.configuration.Configuration;
import cc.jfire.jfire.test.function.base.data.House;
import cc.jfire.jfire.test.function.base.data.MutablePerson;
import org.junit.Assert;
import org.junit.Test;

@EnableCacheManager
@Configuration
public class CacheTest
{
    @Test
    public void test()
    {
        ApplicationContext context = new DefaultApplicationContext();
        context.register(CacheTarget.class);
        context.register(DemoCache.class);
        context.register(CacheManagerTest.class);
        CacheTarget cacheTarget = context.getBean(CacheTarget.class);
        House       house       = cacheTarget.get(1);
        House       second      = cacheTarget.get(1);
        Assert.assertNotSame(house, second);
        house  = cacheTarget.get(5);
        second = cacheTarget.get(5);
        Assert.assertSame(house, second);
        cacheTarget.put(5);
        second = cacheTarget.get(5);
        Assert.assertNotSame(house, second);
        house  = cacheTarget.get(5);
        second = cacheTarget.get(5);
        Assert.assertSame(house, second);
        cacheTarget.remove(5);
        second = cacheTarget.get(5);
        Assert.assertNotSame(house, second);
        String first   = cacheTarget.get();
        String seconde = cacheTarget.get();
        Assert.assertSame(first, seconde);
        cacheTarget.put();
        seconde = cacheTarget.get();
        Assert.assertNotSame(first, seconde);
        first = cacheTarget.get();
        Assert.assertSame(first, seconde);
    }

    /**
     * 测试覆盖补全
     */
    @Test
    public void test_2()
    {
        ApplicationContext context = new DefaultApplicationContext();
        context.register(CacheTarget.class);
        context.register(DemoCache.class);
        context.register(CacheManagerTest.class);
        CacheTarget cacheTarget = context.getBean(CacheTarget.class);
        House       house       = cacheTarget.get2(6);
        House       second      = cacheTarget.get2(6);
        Assert.assertSame(house, second);
    }

    /**
     * 测试覆盖补全
     */
    @Test
    public void test_3()
    {
        ApplicationContext context = new DefaultApplicationContext();
        context.register(CacheTarget.class);
        context.register(DemoCache.class);
        context.register(CacheManagerTest.class);
        CacheTarget cacheTarget = context.getBean(CacheTarget.class);
        House       house       = cacheTarget.get3(6);
        House       second      = cacheTarget.get3(6);
        Assert.assertSame(house, second);
    }

    /**
     * 测试覆盖补全
     */
    @Test
    public void test_4()
    {
        ApplicationContext context = new DefaultApplicationContext();
        context.register(CacheTarget.class);
        context.register(DemoCache.class);
        context.register(CacheManagerTest.class);
        CacheTarget   cacheTarget = context.getBean(CacheTarget.class);
        MutablePerson person      = new MutablePerson();
        person.setAge(18);
        House house  = cacheTarget.get4(person);
        House second = cacheTarget.get4(person);
        Assert.assertSame(house, second);
    }

    @Test
    public void test5()
    {
        ApplicationContext context = ApplicationContext.boot(CacheTest.class);
        context.register(ConcurrentMapCacheManager.class);
        context.register(CacheTarget.class);
        CacheTarget   cacheTarget = context.getBean(CacheTarget.class);
        MutablePerson person      = new MutablePerson();
        person.setAge(18);
        House house  = cacheTarget.get4(person);
        House second = cacheTarget.get4(person);
        Assert.assertSame(house, second);
    }
}

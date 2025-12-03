package cc.jfire.jfire.test.function;

import cc.jfire.jfire.test.function.aliastest.AliasTest;
import cc.jfire.jfire.test.function.aop.AopTest;
import cc.jfire.jfire.test.function.base.ContextTest;
import cc.jfire.jfire.test.function.beanannotest.BeanAnnoTest;
import cc.jfire.jfire.test.function.cachetest.CacheTest;
import cc.jfire.jfire.test.function.cyclicDependenceTest.CyclicDependenceTest;
import cc.jfire.jfire.test.function.initmethod.InitMethodTest;
import cc.jfire.jfire.test.function.loader.HolderTest;
import cc.jfire.jfire.test.function.map.MapTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ //
        AliasTest.class, //
        AopTest.class, //
        ContextTest.class, //
        BeanAnnoTest.class, //
        ParamFieldTest.class, //
        CacheTest.class, //
        InitMethodTest.class, //
        HolderTest.class, //
        ConfigurationOrderTest.class,//
        MapTest.class, //
        ConfigBeanTest.class, //
        DiTest.class, //
        ParamFieldTest.class, //
        PropertyPathImporterTest.class, //
        StarterTest.class, //
        CyclicDependenceTest.class,//
        BeanHolderTest.class,//
        ImportTest.class,//
        AnnodatationDatabaseTest.class,//
})
public class Coverage
{
}

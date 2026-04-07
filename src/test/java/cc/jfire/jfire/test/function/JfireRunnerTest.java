package cc.jfire.jfire.test.function;

import cc.jfire.baseutil.Resource;
import cc.jfire.jfire.helpjunit.JfireRunner;
import cc.jfire.jfire.core.prepare.annotation.Import;
import cc.jfire.jfire.core.prepare.annotation.configuration.Configuration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicInteger;

public class JfireRunnerTest
{
    @Test
    public void 应该通过容器获取测试Bean后执行每个测试方法()
    {
        RunnerCase.executedCount.set(0);
        Result result = JUnitCore.runClasses(RunnerCase.class);
        Assert.assertTrue(result.getFailures().toString(), result.wasSuccessful());
        Assert.assertEquals(2, result.getRunCount());
        Assert.assertEquals(2, RunnerCase.executedCount.get());
    }

    @RunWith(JfireRunner.class)
    @Configuration
    @Import(Dependency.class)
    @Resource
    public static class RunnerCase
    {
        private static final AtomicInteger executedCount = new AtomicInteger();
        @Resource
        private Dependency dependency;

        @Test
        public void test1()
        {
            Assert.assertNotNull(dependency);
            executedCount.incrementAndGet();
        }

        @Test
        public void test2()
        {
            Assert.assertNotNull(dependency);
            executedCount.incrementAndGet();
        }
    }

    @Resource
    public static class Dependency
    {
    }
}

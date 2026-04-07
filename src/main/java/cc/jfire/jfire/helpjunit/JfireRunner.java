package cc.jfire.jfire.helpjunit;

import cc.jfire.jfire.core.ApplicationContext;
import cc.jfire.jfire.core.DefaultApplicationContext;
import org.junit.rules.RunRules;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.util.List;

/**
 * Jfire 的 JUnit4 Runner。
 * 每个测试方法执行前，都会先从 ApplicationContext 中获取测试类对应的 Bean。
 */
public class JfireRunner extends Runner
{
    private final DelegateRunner delegateRunner;

    public JfireRunner(Class<?> klass) throws InitializationError
    {
        delegateRunner = new DelegateRunner(klass);
    }

    @Override
    public Description getDescription()
    {
        return delegateRunner.getDescription();
    }

    @Override
    public void run(RunNotifier notifier)
    {
        delegateRunner.run(notifier);
    }

    private static class DelegateRunner extends BlockJUnit4ClassRunner
    {
        private final Class<?>           klass;
        private final ApplicationContext context;

        private DelegateRunner(Class<?> klass) throws InitializationError
        {
            super(klass);
            this.klass = klass;
            context = new DefaultApplicationContext(klass);
        }

        @Override
        @SuppressWarnings("deprecation")
        protected Statement methodBlock(FrameworkMethod method)
        {
            Object    test      = createTest();
            Statement statement = methodInvoker(method, test);
            statement = possiblyExpectingExceptions(method, test, statement);
            statement = withPotentialTimeout(method, test, statement);
            statement = withBefores(method, test, statement);
            statement = withAfters(method, test, statement);
            statement = withRules(method, test, statement);
            return statement;
        }

        private Statement withRules(FrameworkMethod method, Object target, Statement statement)
        {
            List<TestRule> testRules = getTestRules(target);
            Statement      result    = statement;
            result = withMethodRules(method, testRules, target, result);
            result = withTestRules(method, testRules, result);
            return result;
        }

        private Statement withTestRules(FrameworkMethod method, List<TestRule> testRules, Statement statement)
        {
            return testRules.isEmpty() ? statement : new RunRules(statement, testRules, describeChild(method));
        }

        private Statement withMethodRules(FrameworkMethod method, List<TestRule> testRules, Object target, Statement result)
        {
            for (org.junit.rules.MethodRule each : rules(target))
            {
                if (!testRules.contains(each))
                {
                    result = each.apply(result, method, target);
                }
            }
            return result;
        }

        @Override
        protected Object createTest()
        {
            return context.getBean(klass);
        }
    }
}

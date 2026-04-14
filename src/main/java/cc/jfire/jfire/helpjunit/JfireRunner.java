package cc.jfire.jfire.helpjunit;

import cc.jfire.jfire.core.ApplicationContext;
import cc.jfire.jfire.core.DefaultApplicationContext;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestInstanceFactory;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;

/**
 * JUnit5 测试扩展，用于在测试中自动创建应用上下文
 */
public class JfireRunner implements TestInstanceFactory, ParameterResolver
{
    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(JfireRunner.class);

    @Override
    public Object createTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext extensionContext)
    {
        Class<?> testClass = factoryContext.getTestClass();
        ApplicationContext context = getOrCreateContext(extensionContext, testClass);
        return context.getBean(testClass);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
    {
        return parameterContext.getParameter().getType() == ApplicationContext.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
    {
        return getOrCreateContext(extensionContext, extensionContext.getRequiredTestClass());
    }

    private ApplicationContext getOrCreateContext(ExtensionContext extensionContext, Class<?> testClass)
    {
        return extensionContext.getStore(NAMESPACE)
                .getOrComputeIfAbsent(testClass, key -> new DefaultApplicationContext(testClass), ApplicationContext.class);
    }
}

package cc.jfire.jfire.test.function;

import cc.jfire.jfire.core.ApplicationContext;
import cc.jfire.jfire.core.prepare.annotation.configuration.Configuration;
import cc.jfire.jfire.helpjunit.JfireTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@JfireTest
@Configuration
public class JfireRunnerTest
{
    @Test
    void shouldCreateTestInstanceFromApplicationContext(ApplicationContext context)
    {
        JfireRunnerTest bean = context.getBean(JfireRunnerTest.class);
        assertNotNull(bean);
        assertSame(this, bean);
    }
}

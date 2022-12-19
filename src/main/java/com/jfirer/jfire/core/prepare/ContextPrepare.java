package com.jfirer.jfire.core.prepare;

import com.jfirer.jfire.core.ApplicationContext;

/**
 * 实现了该接口的实现类，在容器启动前会直接以反射方式被实例化
 */
public interface ContextPrepare
{
    /**
     * 执行预处理流程，返回本次是否注册了新的ContextPrepare接口的Bean
     *
     * @return
     */
    ApplicationContext.FoundNewContextPrepare prepare(ApplicationContext context);

    int order();
}

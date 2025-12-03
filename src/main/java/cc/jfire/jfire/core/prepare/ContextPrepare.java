package cc.jfire.jfire.core.prepare;

import cc.jfire.jfire.core.ApplicationContext;

/**
 * 实现了该接口的实现类，在容器启动前会直接以反射方式被实例化
 */
public interface ContextPrepare
{
    /**
     * 执行预处理流程，返回本次是否注册了新的ContextPrepare接口的Bean
     *
     * @param context 应用上下文
     * @return 是否发现了新的ContextPrepare
     */
    ApplicationContext.FoundNewContextPrepare prepare(ApplicationContext context);

    /**
     * 获取执行顺序
     *
     * @return 顺序值，值越小越先执行
     */
    int order();
}

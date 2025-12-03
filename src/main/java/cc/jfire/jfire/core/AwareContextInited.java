package cc.jfire.jfire.core;

/**
 * 容器初始化完毕后被调用
 *
 * @author 林斌
 */
public interface AwareContextInited
{
    /**
     * 当容器初始化完成后，该接口会被容器调用
     *
     * @param applicationContext 应用上下文
     */
    void aware(ApplicationContext applicationContext);

    /**
     * 获取执行顺序
     *
     * @return 顺序值，值越小越先执行
     */
    default int order()
    {
        return 0;
    }
}

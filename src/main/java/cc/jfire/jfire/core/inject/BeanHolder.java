package cc.jfire.jfire.core.inject;

/**
 * Bean持有者接口，用于获取当前Bean的代理对象
 *
 * @param <T> Bean类型
 */
public interface BeanHolder<T>
{
    /**
     * 返回当前Bean对象
     *
     * @return 当前Bean的代理对象
     */
    T getSelf();
}

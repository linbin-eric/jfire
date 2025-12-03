package cc.jfire.jfire.core.bean;

/**
 * Bean定义接口，用于获取Bean实例及其元信息
 */
public interface BeanDefinition
{
    /**
     * 获取Bean实例
     *
     * @return Bean实例
     */
    Object getBean();

    /**
     * 获取Bean名称
     *
     * @return Bean名称
     */
    String getBeanName();

    /**
     * 获取Bean类型
     *
     * @return Bean的Class类型
     */
    Class<?> getType();
}

package cc.jfire.jfire.exception;

import cc.jfire.baseutil.StringUtil;
import cc.jfire.jfire.core.bean.BeanRegisterInfo;

import java.util.Collection;

/**
 * Bean定义未找到异常
 */
public class BeanDefinitionCanNotFindException extends RuntimeException
{
    private static final long serialVersionUID = -6930943532766346915L;

    /**
     * 根据Bean名称构造异常
     *
     * @param beanName Bean名称
     */
    public BeanDefinitionCanNotFindException(String beanName)
    {
        super("无法找到bean：" + beanName);
    }

    /**
     * 根据Bean类型构造异常
     *
     * @param ckass Bean类型
     */
    public BeanDefinitionCanNotFindException(Class<?> ckass)
    {
        super("无法找到类型为:" + ckass.getName() + "的Bean");
    }

    /**
     * 根据Bean注册信息列表和类型构造异常
     *
     * @param list Bean注册信息列表
     * @param type Bean类型
     */
    public BeanDefinitionCanNotFindException(Collection<BeanRegisterInfo> list, Class<?> type)
    {
        super(StringUtil.format("无法找到合适的Bean,符合类型:{}的bean存在:{}个", type.getName(), list.size()));
    }
}

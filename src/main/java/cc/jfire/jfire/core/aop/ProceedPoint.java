package cc.jfire.jfire.core.aop;

/**
 * 方法连接点的抽象表示
 *
 * @author 林斌
 */
public interface ProceedPoint
{
    /**
     * 表示对目标方法的调用。在静态代码中作为继承方法被修改以实现对目标方法的调用
     */
    void invoke();

    /**
     * 返回目标方法的调用对象实例
     *
     * @return 目标方法的宿主对象
     */
    Object getHost();

    /**
     * 被拦截的方法
     *
     * @return 方法描述信息
     */
    MethodDescription getMethod();

    /**
     * 在异常增强中，返回原方法抛出的异常
     *
     * @return 原方法抛出的异常
     */
    Throwable getE();

    /**
     * 在后置增强中，返回原方法的执行结果。其余方法无效。
     *
     * @return 原方法的执行结果
     */
    Object getResult();

    /**
     * 环绕增强中，可以直接设定要返回的结果。需要自己保证与原方法返回的类型一致。
     *
     * @param result 要设定的返回结果
     */
    void setResult(Object result);

    /**
     * 获得目标方法的入参数组
     *
     * @return 目标方法的参数数组
     */
    Object[] getParams();

    class MethodDescription
    {
        final String methodName;
        Class<?>[] paramTypes;

        public MethodDescription(String methodName, Class<?>[] paramTypes)
        {
            this.methodName = methodName;
            this.paramTypes = paramTypes;
        }

        public String methodName()
        {
            return methodName;
        }

        public Class<?>[] getParamTypes()
        {
            return paramTypes;
        }
    }
}

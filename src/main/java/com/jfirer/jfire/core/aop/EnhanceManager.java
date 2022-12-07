package com.jfirer.jfire.core.aop;

import com.jfirer.baseutil.smc.model.ClassModel;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.bean.BeanRegisterInfo;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 不同的增强管理类实现不同的增强内容,该接口的实现类在容器中会直接以反射的形式被实例化，而不会经过容器的其他流程。
 *
 * @author linbin
 */
public interface EnhanceManager
{
    // 用来作为AOP时增加的属性命名数字后缀，保证一个类中属性名不会出现重复
    AtomicInteger FIELD_NAME_COUNTER  = new AtomicInteger(0);
    AtomicInteger CLASS_NAME_COUNTER  = new AtomicInteger(0);
    AtomicInteger METHOD_NAME_COUNTER = new AtomicInteger(0);
    AtomicInteger VAR_NAME_COUNTER    = new AtomicInteger(0);
    int           DEFAULT             = 100;
    int           TRANSACTION         = 10;
    int           CACHE               = 30;
    int           VALIDATE            = 50;

    /**
     * 扫描环境中所有的BeanDefinition，如果发现其符合增强条件，则将自身放入其AopManager集合中。 该方法仅会在环境初始化时调用一次
     */
    void scan(ApplicationContext context);

    default void interlScan(ApplicationContext context, Predicate<Method> predicate, Consumer<BeanRegisterInfo> consumer)
    {
        context.getAllBeanRegisterInfos().stream()//
               .forEach(beanRegisterInfo -> {
                   Optional<Method> any = Arrays.stream(beanRegisterInfo.getType().getMethods()).filter(predicate).findAny();
                   if (any.isPresent())
                   {
                       consumer.accept(beanRegisterInfo);
                   }
               });
    }

    /**
     * 执行增强操作
     *
     * @param classModel    为增强类创建的ClassModel
     * @param type          被增强类
     * @param context       上下文
     * @param hostFieldName 被增强类实例
     */
    void enhance(ClassModel classModel, Class<?> type, ApplicationContext context, String hostFieldName);

    /**
     * 该AOP生效顺序。数字越小生效越快
     *
     * @return
     */
    int order();
}

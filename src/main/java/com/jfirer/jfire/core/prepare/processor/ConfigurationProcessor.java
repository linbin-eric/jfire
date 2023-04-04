package com.jfirer.jfire.core.prepare.processor;

import com.jfirer.baseutil.StringUtil;
import com.jfirer.baseutil.TRACEID;
import com.jfirer.baseutil.bytecode.support.AnnotationContext;
import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.bean.BeanRegisterInfo;
import com.jfirer.jfire.core.bean.impl.register.DefaultBeanRegisterInfo;
import com.jfirer.jfire.core.beanfactory.impl.MethodBeanFactory;
import com.jfirer.jfire.core.prepare.ContextPrepare;
import com.jfirer.jfire.core.prepare.annotation.condition.Condition;
import com.jfirer.jfire.core.prepare.annotation.condition.Conditional;
import com.jfirer.jfire.core.prepare.annotation.condition.ErrorMessage;
import com.jfirer.jfire.core.prepare.annotation.configuration.Bean;
import com.jfirer.jfire.core.prepare.annotation.configuration.Configuration;
import com.jfirer.jfire.util.PrepareConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class ConfigurationProcessor implements ContextPrepare
{
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationProcessor.class);

    @Override
    public ApplicationContext.FoundNewContextPrepare prepare(ApplicationContext context)
    {
        ErrorMessage errorMessage = new ErrorMessage();
        Set<Method> methodWithBeanAnnotation = context.getAllBeanRegisterInfos().stream()//
                                                      .filter(beanRegisterInfo -> AnnotationContext.isAnnotationPresent(Configuration.class, beanRegisterInfo.getType()))//
                                                      .map(beanRegisterInfo -> beanRegisterInfo.getType())//
                                                      .flatMap(ckass -> Arrays.stream(ckass.getDeclaredMethods()))//
                                                      .filter(method -> AnnotationContext.isAnnotationPresent(Bean.class, method))//
                                                      .collect(Collectors.toSet());
        methodWithBeanAnnotation.stream()//
                                .filter(method -> AnnotationContext.isAnnotationPresent(Conditional.class, method) == false //
                                                  && AnnotationContext.isAnnotationPresent(Conditional.class, method.getDeclaringClass()) == false)//
                                .forEach(method -> registerMethodBeanDefinition(method, context, AnnotationContext.getInstanceOn(method)));
        methodWithBeanAnnotation.stream()//
                                .filter(method -> AnnotationContext.isAnnotationPresent(Conditional.class, method) ||//
                                                  AnnotationContext.isAnnotationPresent(Conditional.class, method.getDeclaringClass()))//
                                .filter(method -> {
                                    errorMessage.clear();
                                    if (AnnotationContext.isAnnotationPresent(Conditional.class, method))
                                    {
                                        if (AnnotationContext.getAnnotations(Conditional.class, method).stream().anyMatch(conditional -> matchCondition(context, conditional, method, errorMessage) == false))
                                        {
                                            return false;
                                        }
                                    }
                                    if (AnnotationContext.isAnnotationPresent(Conditional.class, method.getDeclaringClass()))
                                    {
                                        if (AnnotationContext.getAnnotations(Conditional.class, method.getDeclaringClass()).stream().anyMatch(conditional -> matchCondition(context, conditional, method.getDeclaringClass(), errorMessage) == false))
                                        {
                                            return false;
                                        }
                                    }
                                    return true;
                                })//
                                .forEach(method -> registerMethodBeanDefinition(method, context, AnnotationContext.getInstanceOn(method)));
        return ApplicationContext.FoundNewContextPrepare.NO;
    }

    @Override
    public int order()
    {
        return PrepareConstant.CONFIGURATION_ORDER;
    }

    /**
     * 判断条件注解中的条件是否被符合
     *
     * @param context
     * @param conditional
     * @param element
     * @param errorMessage
     * @return
     */
    private boolean matchCondition(ApplicationContext context, Conditional conditional, AnnotatedElement element, ErrorMessage errorMessage)
    {
        boolean match = true;
        for (Class<? extends Condition> each : conditional.value())
        {
            try
            {
                Condition instance = each.getDeclaredConstructor().newInstance();
                if (!instance.match(context, element, errorMessage))
                {
                    match = false;
                    break;
                }
            }
            catch (Exception e)
            {
                ReflectUtil.throwException(e);
            }
        }
        return match;
    }

    private void registerMethodBeanDefinition(Method method, ApplicationContext context, AnnotationContext annotationContextOnMethod)
    {
        Bean             bean             = annotationContextOnMethod.getAnnotation(Bean.class);
        String           beanName         = StringUtil.isNotBlank(bean.name()) ? bean.name() : method.getName();
        BeanRegisterInfo beanRegisterInfo = new DefaultBeanRegisterInfo(bean.prototype(), method.getReturnType(), beanName, context, new MethodBeanFactory(context, method));
        context.registerBeanRegisterInfo(beanRegisterInfo);
        logger.debug("注册方法Bean:{}", method.getDeclaringClass().getSimpleName() + "." + method.getName());
    }
}

package com.jfirer.jfire.core.prepare.processor;

import com.jfirer.baseutil.StringUtil;
import com.jfirer.baseutil.TRACEID;
import com.jfirer.baseutil.bytecode.annotation.AnnotationMetadata;
import com.jfirer.baseutil.bytecode.annotation.ValuePair;
import com.jfirer.baseutil.bytecode.support.AnnotationContext;
import com.jfirer.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.DefaultApplicationContext;
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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ConfigurationProcessor implements ContextPrepare
{
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationProcessor.class);

    @Override
    public ApplicationContext.FoundNewContextPrepare prepare(ApplicationContext context)
    {
        ErrorMessage             errorMessage             = new ErrorMessage();
        AnnotationContextFactory annotationContextFactory = DefaultApplicationContext.ANNOTATION_CONTEXT_FACTORY;
        Set<Method> methodWithBeanAnnotation = context.getAllBeanRegisterInfos().stream()//
                                                      .filter(beanRegisterInfo -> annotationContextFactory.get(beanRegisterInfo.getType()).isAnnotationPresent(Configuration.class))//
                                                      .map(beanRegisterInfo -> beanRegisterInfo.getType())//
                                                      .flatMap(ckass -> Arrays.stream(ckass.getDeclaredMethods()))//
                                                      .filter(method -> annotationContextFactory.get(method).isAnnotationPresent(Bean.class))//
                                                      .collect(Collectors.toSet());
        methodWithBeanAnnotation.stream()//
                                .filter(method -> annotationContextFactory.get(method).isAnnotationPresent(Conditional.class) == false //
                                                  && annotationContextFactory.get(method.getDeclaringClass()).isAnnotationPresent(Conditional.class) == false)//
                                .forEach(method -> registerMethodBeanDefinition(method, context, annotationContextFactory.get(method)));
        methodWithBeanAnnotation.stream()//
                                .filter(method -> annotationContextFactory.get(method).isAnnotationPresent(Conditional.class) ||//
                                                  annotationContextFactory.get(method.getDeclaringClass()).isAnnotationPresent(Conditional.class))//
                                .filter(method -> {
                                    List<AnnotationMetadata> list = new LinkedList<>();
                                    if (annotationContextFactory.get(method).isAnnotationPresent(Conditional.class))
                                    {
                                        list.addAll(annotationContextFactory.get(method).getAnnotationMetadatas(Conditional.class));
                                    }
                                    if (annotationContextFactory.get(method.getDeclaringClass()).isAnnotationPresent(Conditional.class))
                                    {
                                        list.addAll(annotationContextFactory.get(method.getDeclaringClass()).getAnnotationMetadatas(Conditional.class));
                                    }
                                    errorMessage.clear();
                                    AnnotationContext annotationContext = annotationContextFactory.get(method);
                                    for (AnnotationMetadata conditional : list)
                                    {
                                        if (matchCondition(context, conditional, annotationContext, errorMessage) == false)
                                        {
                                            return false;
                                        }
                                    }
                                    return true;
                                })//
                                .forEach(method -> registerMethodBeanDefinition(method, context, annotationContextFactory.get(method)));
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
     * @param annotationContext 在元素上的注解集合。元素可能为class也可能为member
     * @param errorMessage
     * @return
     */
    private boolean matchCondition(ApplicationContext context, AnnotationMetadata conditional, AnnotationContext annotationContext, ErrorMessage errorMessage)
    {
        boolean     match       = true;
        ValuePair[] value       = conditional.getAttribyte("value").getArray();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        for (ValuePair each : value)
        {
            try
            {
                Condition instance = (Condition) (classLoader.loadClass(each.getClassName())).newInstance();
                if (!instance.match(context, annotationContext, errorMessage))
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
        logger.debug("traceId:{} 注册方法Bean:{}", TRACEID.currentTraceId(), method.getDeclaringClass().getSimpleName() + "." + method.getName());
    }
}

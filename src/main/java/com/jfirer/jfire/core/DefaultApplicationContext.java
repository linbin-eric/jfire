package com.jfirer.jfire.core;

import com.jfirer.baseutil.StringUtil;
import com.jfirer.baseutil.TRACEID;
import com.jfirer.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfirer.baseutil.bytecode.support.SupportOverrideAttributeAnnotationContextFactory;
import com.jfirer.baseutil.smc.compiler.CompileHelper;
import com.jfirer.jfire.core.aop.EnhanceManager;
import com.jfirer.jfire.core.aop.impl.AopEnhanceManager;
import com.jfirer.jfire.core.aop.impl.CacheEnhanceManager;
import com.jfirer.jfire.core.aop.impl.TransactionEnhanceManager;
import com.jfirer.jfire.core.aop.impl.ValidateEnhanceManager;
import com.jfirer.jfire.core.bean.AwareContextComplete;
import com.jfirer.jfire.core.bean.BeanDefinition;
import com.jfirer.jfire.core.bean.BeanRegisterInfo;
import com.jfirer.jfire.core.bean.impl.register.ContextPrepareBeanRegisterInfo;
import com.jfirer.jfire.core.bean.impl.register.DefaultBeanRegisterInfo;
import com.jfirer.jfire.core.bean.impl.register.EnhanceManagerBeanRegisterInfo;
import com.jfirer.jfire.core.bean.impl.register.OutterBeanRegisterInfo;
import com.jfirer.jfire.core.beanfactory.SelectBeanFactory;
import com.jfirer.jfire.core.beanfactory.impl.ClassBeanFactory;
import com.jfirer.jfire.core.beanfactory.impl.SelectedBeanFactory;
import com.jfirer.jfire.core.prepare.ContextPrepare;
import com.jfirer.jfire.core.prepare.annotation.Import;
import com.jfirer.jfire.core.prepare.annotation.configuration.Configuration;
import com.jfirer.jfire.core.prepare.processor.ConfigurationProcessor;
import com.jfirer.jfire.exception.BeanDefinitionCanNotFindException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

public class DefaultApplicationContext implements ApplicationContext
{
    public static final  AnnotationContextFactory      ANNOTATION_CONTEXT_FACTORY = new SupportOverrideAttributeAnnotationContextFactory();
    public static final  CompileHelper                 COMPILE_HELPER             = new CompileHelper();
    private static final Logger                        LOGGER                     = LoggerFactory.getLogger(DefaultApplicationContext.class);
    /**
     * 只有Configuration注解下并且有Conditional注解的情况下，才会有Bean是否被注册的可能。
     * 如果支持每一轮刷新都根据不同的环境变量或者其他条件满足来增减Bean定义会变得较为复杂，而且实际上也没有遇到这样的场景。
     * 因为目前简化为只支持Bean定义不断增多。
     */
    protected            Map<String, BeanRegisterInfo> beanRegisterInfoMap        = new HashMap<>();
    private final        Environment                   environment                = new Environment.EnvironmentImpl();
    /**
     * 容器是否刷新过。只有刷新过的容器才能对外提供完整服务。
     */
    private              boolean                       freshed                    = false;

    public DefaultApplicationContext(Class<?> bootStarpClass)
    {
        if (!ANNOTATION_CONTEXT_FACTORY.get(bootStarpClass).isAnnotationPresent(Configuration.class))
        {
            throw new IllegalArgumentException("启动的配置类，一定要有Configuration注解");
        }
        register(bootStarpClass);
    }

    public DefaultApplicationContext()
    {
    }

    private void refreshIfNeed()
    {
        if (!freshed)
        {
            refresh();
        }
    }

    private void registerApplicationContext()
    {
        registerBeanRegisterInfo(new OutterBeanRegisterInfo(this, "applicationContext"));
    }

    /**
     * 刷新上下文，动作包含：<br/>
     * 1. 清空容器内的所有Bean实例。<br/>
     * 2. 注册默认的Bean信息到容器。<br/>
     * 2. 读取配置类信息，如果配置类注解了Import注解，则导入对应的类。并且将配置类注册为一个Bean。<br/>
     * 3. 执行当前的预处理器处理链<br/>
     */
    private void refresh()
    {
        freshed = true;
        String traceId = TRACEID.currentTraceId();
        registerInternalClass();
        processImports();
        if (processContextPrepare() == FoundNewContextPrepare.YES)
        {
            LOGGER.debug("traceId:{} 执行ContextPrepare接口，发现需要刷新容器", traceId);
            refresh();
            return;
        }
        LOGGER.debug("traceId:{} 准备获取所有的EnhanceManager，执行增强扫描", traceId);
        enhanceScan();
        beanRegisterInfoMap.values().stream().filter(beanRegisterInfo -> beanRegisterInfo instanceof AwareContextComplete).forEach(beanRegisterInfo -> ((AwareContextComplete) beanRegisterInfo).complete());
        LOGGER.debug("traceId:{} 准备获取所有的AwareContextInited接口实现，执行aware方法", traceId);
        awareContextInit();
        LOGGER.debug("traceId:{} 容器启动完毕", traceId);
    }

    private void registerInternalClass()
    {
        registerApplicationContext();
        register(AopEnhanceManager.class);
        register(TransactionEnhanceManager.class);
        register(CacheEnhanceManager.class);
        register(ValidateEnhanceManager.class);
        register(ConfigurationProcessor.class);
    }

    private FoundNewContextPrepare processContextPrepare()
    {
        long count = beanRegisterInfoMap.values().stream()//
                                        .filter(beanRegisterInfo -> ContextPrepare.class.isAssignableFrom(beanRegisterInfo.getType()))//
                                        .map(beanRegisterInfo -> ((ContextPrepare) beanRegisterInfo.get().getBean()))//
                                        .sorted(Comparator.comparingInt(ContextPrepare::order))//
                                        .map(contextPrepare -> contextPrepare.prepare(DefaultApplicationContext.this))//
                                        .filter(foundNewContextPrepare -> foundNewContextPrepare == FoundNewContextPrepare.YES)//
                                        .count();
        return count > 0 ? FoundNewContextPrepare.YES : FoundNewContextPrepare.NO;
    }

    private void processImports()
    {
        Set<Class<?>> importClasses = beanRegisterInfoMap.values().stream()//
                                                         .map(beanRegisterInfo -> beanRegisterInfo.getType())//
                                                         .filter(ckass -> ANNOTATION_CONTEXT_FACTORY.get(ckass).isAnnotationPresent(Import.class))//
                                                         .flatMap(ckass -> ANNOTATION_CONTEXT_FACTORY.get(ckass).getAnnotations(Import.class).stream())//
                                                         .flatMap(importAnnotation -> Arrays.stream(importAnnotation.value())).collect(Collectors.toSet());//
        //这里不能将两个Stream操作合并在一起，否则在遍历的过程中又添加新的元素到底层的map,会导致异常。
        importClasses.stream()//
                     .peek(ckass -> LOGGER.debug("traceId:{} 发现被导入的类:{}，准备进行导入", TRACEID.currentTraceId(), ckass))//
                     .forEach(ckass -> register(ckass));//
    }

    @Override
    public RegisterResult register(Class<?> ckass)
    {
        if (EnhanceManager.class.isAssignableFrom(ckass))
        {
            return registerBeanRegisterInfo(new EnhanceManagerBeanRegisterInfo((Class<? extends EnhanceManager>) ckass));
        }
        else if (ContextPrepare.class.isAssignableFrom(ckass))
        {
            return registerBeanRegisterInfo(new ContextPrepareBeanRegisterInfo((Class<? extends ContextPrepare>) ckass));
        }
        String  beanName;
        boolean prototype;
        if (ANNOTATION_CONTEXT_FACTORY.get(ckass).isAnnotationPresent(Resource.class))
        {
            Resource resource = ANNOTATION_CONTEXT_FACTORY.get(ckass).getAnnotation(Resource.class);
            beanName = StringUtil.isNotBlank(resource.name()) ? resource.name() : ckass.getName();
            prototype = !resource.shareable();
        }
        else
        {
            beanName = ckass.getName();
            prototype = false;
        }
        if (beanRegisterInfoMap.containsKey(beanName))
        {
            LOGGER.debug("traceId:{} beanName:{}已经存在，本次忽略", TRACEID.currentTraceId(), beanName);
            return RegisterResult.NODATA;
        }
        if (ANNOTATION_CONTEXT_FACTORY.get(ckass).isAnnotationPresent(SelectBeanFactory.class))
        {
            SelectBeanFactory selectBeanFactory = ANNOTATION_CONTEXT_FACTORY.get(ckass).getAnnotation(SelectBeanFactory.class);
            return registerBeanRegisterInfo(new DefaultBeanRegisterInfo(prototype, ckass, beanName, this, new SelectedBeanFactory(this, selectBeanFactory.value().equals("") ? null : selectBeanFactory.value(), selectBeanFactory.beanFactoryType())));
        }
        else
        {
            return registerBeanRegisterInfo(new DefaultBeanRegisterInfo(prototype, ckass, beanName, this, ClassBeanFactory.INSTANCE));
        }
    }

    @Override
    public RegisterResult registerBeanRegisterInfo(BeanRegisterInfo beanRegisterInfo)
    {
        String beanName = beanRegisterInfo.getBeanName();
        if (beanRegisterInfoMap.containsKey(beanName))
        {
            LOGGER.debug("traceId:{} beanName:{}已经存在，本次忽略", TRACEID.currentTraceId(), beanRegisterInfo.getBeanName());
            return RegisterResult.NODATA;
        }
        beanRegisterInfoMap.put(beanRegisterInfo.getBeanName(), beanRegisterInfo);
        Class<?> type = beanRegisterInfo.getType();
        if (ContextPrepare.class.isAssignableFrom(type))
        {
            LOGGER.debug("traceId:{} 注册bean:{}，其实现了ContextPrepare接口", TRACEID.currentTraceId(), beanRegisterInfo.getBeanName());
            return RegisterResult.PREPARE;
        }
        else if (ANNOTATION_CONTEXT_FACTORY.get(beanRegisterInfo.getType()).isAnnotationPresent(Configuration.class))
        {
            LOGGER.debug("traceId:{} 注册bean:{}，其标记了Configuration注解", TRACEID.currentTraceId(), beanRegisterInfo.getBeanName());
            return RegisterResult.CONFIGURATION;
        }
        else
        {
            LOGGER.debug("traceId:{} 注册bean:{}", TRACEID.currentTraceId(), beanRegisterInfo.getBeanName());
            return RegisterResult.BEAN;
        }
    }

    private void awareContextInit()
    {
        String traceId = TRACEID.currentTraceId();
        beanRegisterInfoMap.values().stream()//
                           .filter(beanRegisterInfo -> AwareContextInited.class.isAssignableFrom(beanRegisterInfo.getType()))//
                           .forEach(beanRegisterInfo -> {
                               ((AwareContextInited) beanRegisterInfo.get().getBean()).aware(DefaultApplicationContext.this);
                               LOGGER.debug("traceId:{} Bean：{}执行aware方法", traceId, beanRegisterInfo.getBeanName());
                           });
    }

    private void enhanceScan()
    {
        String traceId = TRACEID.currentTraceId();
        getBeanRegisterInfos(EnhanceManager.class).stream()//
                                                  .map(beanRegisterInfo -> ((EnhanceManager) beanRegisterInfo.get().getBean()))//
                                                  .sorted(((o1, o2) -> o1.order() > o2.order() ? 1 : o1.order() == o2.order() ? 0 : -1))//
                                                  .forEach(enhanceManager ->//
                                                           {
                                                               LOGGER.debug("traceId:{} 增强类:{}执行AOP扫描", traceId, enhanceManager.getClass().getName());
                                                               enhanceManager.scan(DefaultApplicationContext.this);
                                                           });
    }

    @Override
    public Collection<BeanRegisterInfo> getAllBeanRegisterInfos()
    {
        return beanRegisterInfoMap.values();
    }

    @Override
    public BeanRegisterInfo getBeanRegisterInfo(Class<?> ckass)
    {
        Optional<BeanRegisterInfo> any = beanRegisterInfoMap.values().stream().filter(beanRegisterInfo -> ckass == beanRegisterInfo.getType() || ckass.isAssignableFrom(beanRegisterInfo.getType())).findAny();
        return any.orElse(null);
    }

    @Override
    public BeanRegisterInfo getBeanRegisterInfo(String beanName)
    {
        return beanRegisterInfoMap.get(beanName);
    }

    @Override
    public Environment getEnv()
    {
        return environment;
    }

    @Override
    public <E> E getBean(Class<E> ckass)
    {
        refreshIfNeed();
        Optional<BeanDefinition> any = beanRegisterInfoMap.values().stream().filter(beanRegisterInfo -> ckass.isAssignableFrom(beanRegisterInfo.getType())).map(beanRegisterInfo -> beanRegisterInfo.get()).findAny();
        if (any.isPresent())
        {
            return (E) any.get().getBean();
        }
        else
        {
            throw new BeanDefinitionCanNotFindException(ckass);
        }
    }

    public Collection<BeanRegisterInfo> getBeanRegisterInfos(Class<?> ckass)
    {
        Set<BeanRegisterInfo> collect = beanRegisterInfoMap.values().stream().filter(beanRegisterInfo -> ckass == beanRegisterInfo.getType() || ckass.isAssignableFrom(beanRegisterInfo.getType())).collect(Collectors.toSet());
        return collect;
    }

    @Override
    public <E> Collection<E> getBeans(Class<E> ckass)
    {
        refreshIfNeed();
        return (Set<E>) beanRegisterInfoMap.values().stream().filter(beanRegisterInfo -> ckass.isAssignableFrom(beanRegisterInfo.getType())).map(beanRegisterInfo -> (beanRegisterInfo.get().getBean())).collect(Collectors.toSet());
    }

    @Override
    public <E> E getBean(String beanName)
    {
        refreshIfNeed();
        BeanRegisterInfo beanRegisterInfo = beanRegisterInfoMap.get(beanName);
        if (beanRegisterInfo == null)
        {
            throw new BeanDefinitionCanNotFindException(beanName);
        }
        return (E) beanRegisterInfo.get().getBean();
    }
}

package com.jfirer.jfire.core;

import com.jfirer.baseutil.StringUtil;
import com.jfirer.baseutil.TRACEID;
import com.jfirer.baseutil.bytecode.support.AnnotationContext;
import com.jfirer.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfirer.baseutil.bytecode.support.SupportOverrideAttributeAnnotationContextFactory;
import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.baseutil.smc.compiler.CompileHelper;
import com.jfirer.jfire.core.aop.EnhanceManager;
import com.jfirer.jfire.core.aop.impl.AopEnhanceManager;
import com.jfirer.jfire.core.aop.impl.CacheAopManager;
import com.jfirer.jfire.core.aop.impl.TransactionAopManager;
import com.jfirer.jfire.core.aop.impl.ValidateAopManager;
import com.jfirer.jfire.core.bean.BeanFactory;
import com.jfirer.jfire.core.bean.DefaultBeanDefinition;
import com.jfirer.jfire.core.beandescriptor.ClassReflectInstanceDescriptor;
import com.jfirer.jfire.core.beandescriptor.InstanceDescriptor;
import com.jfirer.jfire.core.beandescriptor.SelectedBeanFactoryInstanceDescriptor;
import com.jfirer.jfire.core.beanfactory.DefaultClassBeanFactory;
import com.jfirer.jfire.core.beanfactory.DefaultMethodBeanFactory;
import com.jfirer.jfire.core.beanfactory.SelectBeanFactory;
import com.jfirer.jfire.core.prepare.ContextPrepare;
import com.jfirer.jfire.core.prepare.annotation.Import;
import com.jfirer.jfire.core.prepare.annotation.configuration.Configuration;
import com.jfirer.jfire.core.prepare.processor.ConfigurationProcessor;
import com.jfirer.jfire.exception.BeanDefinitionCanNotFindException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.lang.annotation.Annotation;
import java.util.*;

public class DefaultApplicationContext implements ApplicationContext
{
    /**
     * 只有Configuration注解下并且有Conditional注解的情况下，才会有Bean是否被注册的可能。
     * 如果支持每一轮刷新都根据不同的环境变量或者其他条件满足来增减Bean定义会变得较为复杂，而且实际上也没有遇到这样的场景。
     * 因为目前简化为只支持Bean定义不断增多。
     */
    protected            Map<String, DefaultBeanDefinition> beanDefinitionMap = new HashMap<String, DefaultBeanDefinition>();
    private              Environment                        environment       = new Environment.EnvironmentImpl();
    private              AnnotationContextFactory    annotationContextFactory   = new SupportOverrideAttributeAnnotationContextFactory();
    private              CompileHelper               compileHelper;
    private              boolean                     firstRefresh               = false;
    private              boolean                     configurationClassSetBuild = false;
    private              Set<Class<?>>               configurationClassSet      = new HashSet<Class<?>>();
    private              Class<?>                    bootStarpClass;
    private static final Logger                      LOGGER                     = LoggerFactory.getLogger(DefaultApplicationContext.class);

    public DefaultApplicationContext(Class<?> bootStarpClass)
    {
        this(bootStarpClass, new CompileHelper());
    }

    public DefaultApplicationContext(Class<?> bootStarpClass, CompileHelper compileHelper)
    {
        this.bootStarpClass = bootStarpClass;
        register(bootStarpClass);
        this.compileHelper = compileHelper;
    }

    public DefaultApplicationContext()
    {
        compileHelper = new CompileHelper();
    }

    public DefaultApplicationContext(CompileHelper compileHelper)
    {
        this.compileHelper = compileHelper;
    }

    private void refreshIfNeed()
    {
        if (firstRefresh == false)
        {
            refresh();
        }
    }

    private void registerDefaultMethodBeanFatory()
    {
        InstanceDescriptor instanceDescriptor = new ClassReflectInstanceDescriptor(DefaultMethodBeanFactory.class);
        DefaultBeanDefinition beanDefinition = new DefaultBeanDefinition("defaultMethodBeanFactory", DefaultMethodBeanFactory.class, false, instanceDescriptor);
        beanDefinitionMap.put(beanDefinition.getBeanName(), beanDefinition);
    }

    private void registerAnnotationContextFactory()
    {
        DefaultBeanDefinition beanDefinition = new DefaultBeanDefinition("annotationContextFactory", SupportOverrideAttributeAnnotationContextFactory.class, annotationContextFactory);
        beanDefinitionMap.put(beanDefinition.getBeanName(), beanDefinition);
    }

    private void registerDefaultBeanFactory()
    {
        BeanFactory beanFactory = new DefaultClassBeanFactory(annotationContextFactory);
        DefaultBeanDefinition beanDefinition = new DefaultBeanDefinition(DefaultClassBeanFactory.class.getName(), DefaultClassBeanFactory.class, beanFactory);
        beanDefinitionMap.put(beanDefinition.getBeanName(), beanDefinition);
    }

    private void registerApplicationContext()
    {
        DefaultBeanDefinition beanDefinition = new DefaultBeanDefinition("jfireContext", ApplicationContext.class, this);
        registerBeanDefinition(beanDefinition);
    }

    @Override
    public void refresh()
    {
        firstRefresh = true;
        configurationClassSetBuild = false;
        String traceId = TRACEID.currentTraceId();
        registerApplicationContext();
        registerDefaultBeanFactory();
        registerAnnotationContextFactory();
        registerDefaultMethodBeanFatory();
        register(AopEnhanceManager.class);
        register(TransactionAopManager.class);
        register(CacheAopManager.class);
        register(ValidateAopManager.class);
        registerJfirePrepare(ConfigurationProcessor.class);
        if (processConfigurationImports() == ApplicationContext.NeedRefresh.YES)
        {
            LOGGER.debug("traceId:{} 在配置类上处理Import注解，发现需要刷新容器", traceId);
            refresh();
            return;
        }
        if (processContextPrepare() == ApplicationContext.NeedRefresh.YES)
        {
            LOGGER.debug("traceId:{} 执行ContextPrepare接口，发现需要刷新容器", traceId);
            refresh();
            return;
        }
        if (beanDefinitionMap.isEmpty())
        {
            return;
        }
        LOGGER.debug("traceId:{} 准备执行所有BeanDefinition的init方法", traceId);
        invokeBeanDefinitionInitMethod();
        LOGGER.debug("traceId:{} 准备获取所有的EnhanceManager，执行aopScan，并且执行BeanDefinition的initEnhance方法", traceId);
        aopScan();
        LOGGER.debug("traceId:{} 准备获取所有的AwareContextInited接口实现，执行aware方法", traceId);
        awareContextInit();
        LOGGER.debug("traceId:{} 容器启动完毕", traceId);
    }

    private NeedRefresh processContextPrepare()
    {
        List<ContextPrepare> contextPrepares = new ArrayList<ContextPrepare>();
        contextPrepares.addAll(getBeans(ContextPrepare.class));
        Collections.sort(contextPrepares, new Comparator<ContextPrepare>()
        {
            @Override
            public int compare(ContextPrepare o1, ContextPrepare o2)
            {
                return o1.order() > o2.order() ? 1 : o1.order() == o2.order() ? 0 : -1;
            }
        });
        for (ContextPrepare each : contextPrepares)
        {
            if (each.prepare(this) == ApplicationContext.NeedRefresh.YES)
            {
                return ApplicationContext.NeedRefresh.YES;
            }
        }
        return ApplicationContext.NeedRefresh.NO;
    }

    private NeedRefresh processConfigurationImports()
    {
        NeedRefresh needRefresh = ApplicationContext.NeedRefresh.NO;
        for (Class<?> each : getConfigurationClassSet())
        {
            AnnotationContext annotationContext = annotationContextFactory.get(each);
            if (annotationContext.isAnnotationPresent(Import.class))
            {
                List<Import> imports = annotationContext.getAnnotations(Import.class);
                for (Import anImport : imports)
                {
                    for (Class<?> importClass : anImport.value())
                    {
                        RegisterResult registerClass = register(importClass);
                        if (registerClass == ApplicationContext.RegisterResult.PREPARE || registerClass == ApplicationContext.RegisterResult.CONFIGURATION)
                        {
                            LOGGER.debug("traceId:{} 导入类:{},注册成功后需要刷新容器", TRACEID.currentTraceId(), importClass);
                            needRefresh = ApplicationContext.NeedRefresh.YES;
                        }
                    }
                }
            }
        }
        return needRefresh;
    }

    @Override
    public RegisterResult register(Class<?> ckass)
    {
        if (ContextPrepare.class.isAssignableFrom(ckass))
        {
            return registerJfirePrepare((Class<? extends ContextPrepare>) ckass) ? ApplicationContext.RegisterResult.PREPARE : ApplicationContext.RegisterResult.NODATA;
        }
        else if (annotationContextFactory.get(ckass).isAnnotationPresent(Configuration.class))
        {
            return registerBean(ckass) ? ApplicationContext.RegisterResult.CONFIGURATION : ApplicationContext.RegisterResult.NODATA;
        }
        else
        {
            return registerBean(ckass) ? ApplicationContext.RegisterResult.BEAN : ApplicationContext.RegisterResult.NODATA;
        }
    }

    private boolean registerBean(Class<?> ckass)
    {
        AnnotationContext annotationContext = annotationContextFactory.get(ckass);
        String beanName;
        boolean prototype;
        if (annotationContext.isAnnotationPresent(Resource.class))
        {
            Resource resource = annotationContext.getAnnotation(Resource.class);
            beanName = StringUtil.isNotBlank(resource.name()) ? resource.name() : ckass.getName();
            prototype = resource.shareable() == false;
        }
        else
        {
            beanName = ckass.getName();
            prototype = false;
        }
        if (beanDefinitionMap.containsKey(beanName))
        {
            return false;
        }
        InstanceDescriptor instanceDescriptor;
        if (annotationContext.isAnnotationPresent(SelectBeanFactory.class))
        {
            SelectBeanFactory selectBeanFactory = annotationContext.getAnnotation(SelectBeanFactory.class);
            if (StringUtil.isNotBlank(selectBeanFactory.value()))
            {
                instanceDescriptor = new SelectedBeanFactoryInstanceDescriptor(selectBeanFactory.value(), ckass);
            }
            else if (selectBeanFactory.beanFactoryType() != Object.class)
            {
                instanceDescriptor = new SelectedBeanFactoryInstanceDescriptor(selectBeanFactory.beanFactoryType(), ckass);
            }
            else
            {
                throw new IllegalArgumentException("类:" + ckass.getName() + "上的注解：SelectBeanFactory缺少正确的属性值");
            }
        }
        else
        {
            instanceDescriptor = new ClassReflectInstanceDescriptor(ckass);
        }
        DefaultBeanDefinition beanDefinition = new DefaultBeanDefinition(beanName, ckass, prototype, instanceDescriptor);
        beanDefinitionMap.put(beanDefinition.getBeanName(), beanDefinition);
        LOGGER.debug("traceId:{} 注册Bean:{}", TRACEID.currentTraceId(), ckass);
        return true;
    }

    @Override
    public boolean registerBeanDefinition(DefaultBeanDefinition beanDefinition)
    {
        return beanDefinitionMap.put(beanDefinition.getBeanName(), beanDefinition) == null;
    }

    private boolean registerJfirePrepare(Class<? extends ContextPrepare> ckass)
    {
        String beanName = ckass.getName();
        if (beanDefinitionMap.containsKey(beanName))
        {
            return false;
        }
        try
        {
            DefaultBeanDefinition beanDefinition = new DefaultBeanDefinition(beanName, ckass, ckass.newInstance());
            beanDefinitionMap.put(beanName, beanDefinition);
            LOGGER.debug("traceId:{} 注册bean:{}，其实现了ContextPrepare接口", TRACEID.currentTraceId(), ckass);
        }
        catch (Throwable e)
        {
            ReflectUtil.throwException(e);
        }
        return true;
    }

    @Override
    public Set<Class<?>> getConfigurationClassSet()
    {
        if (configurationClassSetBuild == false)
        {
            configurationClassSet.clear();
            for (DefaultBeanDefinition value : beanDefinitionMap.values())
            {
                if (annotationContextFactory.get(value.getType()).isAnnotationPresent(Configuration.class))
                {
                    configurationClassSet.add(value.getType());
                }
            }
            if (bootStarpClass != null)
            {
                configurationClassSet.add(bootStarpClass);
            }
            configurationClassSetBuild = true;
        }
        return configurationClassSet;
    }

    @Override
    public CompileHelper getCompileHelper()
    {
        if (compileHelper != null)
        {
            return compileHelper;
        }
        else
        {
            compileHelper = new CompileHelper();
            return compileHelper;
        }
    }

    @Override
    public AnnotationContextFactory getAnnotationContextFactory()
    {
        return annotationContextFactory;
    }

    private void awareContextInit()
    {
        String traceId = TRACEID.currentTraceId();
        for (DefaultBeanDefinition beanDefinition : beanDefinitionMap.values())
        {
            if (AwareContextInited.class.isAssignableFrom(beanDefinition.getType()))
            {
                ((AwareContextInited) beanDefinition.getBean()).aware(this);
                LOGGER.debug("traceId:{} Bean：{}执行aware方法", traceId, beanDefinition.getBeanName());
            }
        }
    }

    private void invokeBeanDefinitionInitMethod()
    {
        for (DefaultBeanDefinition beanDefinition : beanDefinitionMap.values())
        {
            beanDefinition.init(this);
        }
    }

    private void aopScan()
    {
        String traceId = TRACEID.currentTraceId();
        getBeans(EnhanceManager.class).stream()
                                      .sorted(((o1, o2) -> o1.order() > o2.order() ? 1 : o1.order() == o2.order() ? 0 : -1))
                                      .forEach(enhanceManager -> {
                                          LOGGER.debug("traceId:{} 增强类:{}执行AOP扫描", traceId, enhanceManager.getClass()
                                                                                                                 .getName());
                                          enhanceManager.scan(DefaultApplicationContext.this);
                                      });
        for (DefaultBeanDefinition each : beanDefinitionMap.values())
        {
            each.initEnhance();
        }
    }

    @Override
    public Collection<DefaultBeanDefinition> getAllBeanDefinitions()
    {
        return beanDefinitionMap.values();
    }

    @Override
    public DefaultBeanDefinition getBeanDefinition(Class<?> ckass)
    {
        for (DefaultBeanDefinition each : beanDefinitionMap.values())
        {
            if (ckass == each.getType() || ckass.isAssignableFrom(each.getType()))
            {
                return each;
            }
        }
        return null;
    }

    @Override
    public DefaultBeanDefinition getBeanDefinition(String beanName)
    {
        return beanDefinitionMap.get(beanName);
    }

    @Override
    public DefaultBeanDefinition getBeanFactory(InstanceDescriptor beanDescriptor)
    {
        if (StringUtil.isNotBlank(beanDescriptor.selectedBeanFactoryBeanName()))
        {
            return getBeanDefinition(beanDescriptor.selectedBeanFactoryBeanName());
        }
        if (beanDescriptor.selectedBeanFactoryBeanClass() != null)
        {
            return getBeanDefinition(beanDescriptor.selectedBeanFactoryBeanClass());
        }
        else
        {
            throw new IllegalArgumentException();
        }
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
        DefaultBeanDefinition beanDefinition = getBeanDefinition(ckass);
        if (beanDefinition == null)
        {
            throw new BeanDefinitionCanNotFindException(ckass);
        }
        return (E) beanDefinition.getBean();
    }

    public List<DefaultBeanDefinition> getBeanDefinitions(Class<?> ckass)
    {
        List<DefaultBeanDefinition> beanDefinitions = new LinkedList<DefaultBeanDefinition>();
        for (DefaultBeanDefinition each : beanDefinitionMap.values())
        {
            if (ckass == each.getType() || ckass.isAssignableFrom(each.getType()))
            {
                beanDefinitions.add(each);
            }
        }
        return beanDefinitions;
    }

    @Override
    public <E> List<E> getBeans(Class<E> ckass)
    {
        refreshIfNeed();
        List<DefaultBeanDefinition> beanDefinitions = getBeanDefinitions(ckass);
        List<E> list = new LinkedList<E>();
        for (DefaultBeanDefinition each : beanDefinitions)
        {
            list.add((E) each.getBean());
        }
        return list;
    }

    @Override
    public <E> E getBean(String beanName)
    {
        refreshIfNeed();
        DefaultBeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (beanDefinition == null)
        {
            throw new BeanDefinitionCanNotFindException(beanName);
        }
        return (E) beanDefinition.getBean();
    }

    @Override
    public List<DefaultBeanDefinition> getBeanDefinitionsByAnnotation(Class<? extends Annotation> ckass)
    {
        List<DefaultBeanDefinition> list = new ArrayList<DefaultBeanDefinition>();
        for (DefaultBeanDefinition each : beanDefinitionMap.values())
        {
            Class<?> type = each.getType();
            AnnotationContext annotationContext = annotationContextFactory.get(type);
            if (annotationContext.isAnnotationPresent(ckass))
            {
                list.add(each);
            }
        }
        return list;
    }
}

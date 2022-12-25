package com.jfirer.jfire.core.bean.impl.definition;

import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.aop.EnhanceManager;
import com.jfirer.jfire.core.aop.EnhanceWrapper;
import com.jfirer.jfire.core.bean.BeanDefinition;
import com.jfirer.jfire.core.beanfactory.BeanFactory;
import com.jfirer.jfire.core.beanfactory.impl.ClassBeanFactory;
import com.jfirer.jfire.core.inject.InjectHandler;
import com.jfirer.jfire.core.prepare.ContextPrepare;
import com.jfirer.jfire.exception.NewBeanInstanceException;
import com.jfirer.jfire.exception.PostConstructMethodException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PrototypeBeanDefinition implements BeanDefinition
{
    record CycData(String beanName, BeanFactory beanFactory) {}

    protected static final ThreadLocal<Map<String, Object>> tmpBeanInstanceMap    = ThreadLocal.withInitial(() -> new HashMap<String, Object>());
    protected static final ThreadLocal<List<CycData>>       cyclicDependenceQueue = ThreadLocal.withInitial(ArrayList::new);
    protected              BeanFactory                      beanFactory;
    protected              ApplicationContext               context;
    // 标注@PostConstruct的方法
    protected              Method                           postConstructMethod;
    protected              InjectHandler[]                  injectHandlers;
    protected              Class<?>                         enhanceType;
    protected              Class                            type;
    protected              String                           beanName;
    private static final   Logger                           LOGGER                = LoggerFactory.getLogger(PrototypeBeanDefinition.class);

    public PrototypeBeanDefinition(BeanFactory beanFactory, ApplicationContext context, Method postConstructMethod, InjectHandler[] injectHandlers, Class<?> enhanceType, Class type, String beanName)
    {
        if (ContextPrepare.class.isAssignableFrom(type) || EnhanceManager.class.isAssignableFrom(type))
        {
            throw new IllegalArgumentException("框架代码自身错误，ContextPrepare 或 EnhanceManager 类型的Bean应该要选择特定的BeanDefinition");
        }
        this.beanFactory = beanFactory;
        this.context = context;
        this.postConstructMethod = postConstructMethod;
        this.injectHandlers = injectHandlers;
        this.enhanceType = enhanceType;
        this.type = type;
        this.beanName = beanName;
    }

    protected synchronized Object buildInstance()
    {
        List<CycData>       cycDependStack = cyclicDependenceQueue.get();
        CycData             current        = new CycData(beanName, beanFactory);
        Map<String, Object> map            = tmpBeanInstanceMap.get();
        boolean             cleanMark      = map.isEmpty();
        try
        {
            detectCyc(cycDependStack, current);
            Object instance = map.get(getBeanName());
            if (instance != null)
            {
                return instance;
            }
            Object unEnhanceInstance;
            unEnhanceInstance = instance = beanFactory.getUnEnhanceyInstance(this);
            if (enhanceType != null)
            {
                try
                {
                    EnhanceWrapper newInstance = (EnhanceWrapper) enhanceType.getDeclaredConstructor().newInstance();
                    newInstance.setHost(unEnhanceInstance);
                    instance = newInstance;
                }
                catch (Throwable e)
                {
                    throw new NewBeanInstanceException(e);
                }
            }
            map.put(getBeanName(), instance);
            if (injectHandlers.length != 0)
            {
                for (InjectHandler each : injectHandlers)
                {
                    each.inject(unEnhanceInstance);
                }
            }
            //调用setEnhanceFields时候，其内部实现要求获取的Bean已经是一个实例化好了的Bean，因此需要在依赖注入之后进行，成功率最大。
            if (instance instanceof EnhanceWrapper wrapper)
            {
                //由于设置增强属性也带来依赖，因此这一步的设置必须在 map.put(getBeanName(), instance)调用之后执行
                wrapper.setEnhanceFields(context);
            }
            if (postConstructMethod != null)
            {
                try
                {
                    postConstructMethod.invoke(instance);
                }
                catch (Exception e)
                {
                    throw new PostConstructMethodException(e);
                }
            }
            return instance;
        }
        finally
        {
            if (cleanMark)
            {
                map.clear();
            }
            cycDependStack.remove(cycDependStack.size() - 1);
        }
    }

    private void detectCyc(List<CycData> cycDependStack, CycData current)
    {
        if (cycDependStack.contains(current))
        {
            //代码运行到这一步，意味着在map中的对象发生了循环引用。框架能支持的循环引用只有循环引用的Bean都是因为属性被注入，且Bean的生成方式都是反射的时候才可以。
            int index = cycDependStack.size() - 1;
            do
            {
                CycData cycData = cycDependStack.get(index);
                if (cycData.equals(current) == false)
                {
                    if (cycData.beanFactory instanceof ClassBeanFactory)
                    {
                        index--;
                    }
                    else
                    {
                        int currentIndex = cycDependStack.lastIndexOf(current);
                        cycDependStack.add(current);
                        String collect = cycDependStack.stream().skip(currentIndex).map(cyc -> context.getBeanRegisterInfo(cyc.beanName).getType().getSimpleName()).collect(Collectors.joining("\n↓\n"));
                        LOGGER.debug("发现循环依赖，具体为:\n{}\n", collect);
                        throw new IllegalStateException("发现循环依赖，具体为:\n" + collect);
                    }
                }
                else
                {
                    break;
                }
            }
            while (index >= 0);
        }
        else
        {
            cycDependStack.add(current);
        }
    }

    @Override
    public Object getBean()
    {
        return buildInstance();
    }

    @Override
    public String getBeanName()
    {
        return beanName;
    }

    @Override
    public Class<?> getType()
    {
        return type;
    }

    public BeanFactory getBeanFactory()
    {
        return beanFactory;
    }
}

package cc.jfire.jfire.core.bean.impl.definition;

import cc.jfire.jfire.core.ApplicationContext;
import cc.jfire.jfire.core.aop.EnhanceManager;
import cc.jfire.jfire.core.aop.EnhanceWrapper;
import cc.jfire.jfire.core.bean.BeanDefinition;
import cc.jfire.jfire.core.beanfactory.BeanFactory;
import cc.jfire.jfire.core.beanfactory.impl.ClassBeanFactory;
import cc.jfire.jfire.core.inject.InjectHandler;
import cc.jfire.jfire.core.prepare.ContextPrepare;
import cc.jfire.jfire.exception.NewBeanInstanceException;
import cc.jfire.jfire.exception.PostConstructMethodException;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class PrototypeBeanDefinition implements BeanDefinition
{
    record CycData(String beanName, BeanFactory beanFactory)
    {
    }

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

    public PrototypeBeanDefinition(BeanFactory beanFactory, ApplicationContext context, Method postConstructMethod, InjectHandler[] injectHandlers, Class<?> enhanceType, Class type, String beanName)
    {
        if (ContextPrepare.class.isAssignableFrom(type) || EnhanceManager.class.isAssignableFrom(type))
        {
            throw new IllegalArgumentException("框架代码自身错误，ContextPrepare 或 EnhanceManager 类型的Bean应该要选择特定的BeanDefinition");
        }
        this.beanFactory         = beanFactory;
        this.context             = context;
        this.postConstructMethod = postConstructMethod;
        this.injectHandlers      = injectHandlers;
        this.enhanceType         = enhanceType;
        this.type                = type;
        this.beanName            = beanName;
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
            else
            {
                ;
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
            /**
             * 如果是是MethodBeanfatory，并且还使用PostConstruct，可能会导致初始化方法被错误的执行多次。
             * 因此限定只有ClassBeanFactory才执行。
             */
            if (postConstructMethod != null && beanFactory instanceof ClassBeanFactory)
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
            else
            {
                ;
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
            /**
             * 代码运行到这一步，意味着在map中的对象发生了循环引用。
             * 框架能支持的循环引用只有循环引用的Bean都是因为属性被注入，且Bean的生成方式都是反射的时候才可以。
             * 因此，整体的检查逻辑就是从依赖堆栈不断往前检查，如果直到循环依赖为止，引用的Bean都是反射生成的就没有问题；如果有出现一个不是的，则进行报错
             */
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
                        log.debug("发现循环依赖，具体为:\n{}\n", collect);
                        throw new IllegalStateException("发现循环依赖，具体为:\n" + collect);
                    }
                }
                else
                {
                    cycDependStack.add(current);
                    break;
                }
            } while (index >= 0);
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

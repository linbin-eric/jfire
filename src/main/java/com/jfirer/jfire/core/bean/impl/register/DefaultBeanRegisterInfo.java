package com.jfirer.jfire.core.bean.impl.register;

import com.jfirer.baseutil.Formatter;
import com.jfirer.baseutil.bytecode.support.AnnotationContext;
import com.jfirer.baseutil.smc.SmcHelper;
import com.jfirer.baseutil.smc.model.ClassModel;
import com.jfirer.baseutil.smc.model.FieldModel;
import com.jfirer.baseutil.smc.model.MethodModel;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.DefaultApplicationContext;
import com.jfirer.jfire.core.aop.EnhanceManager;
import com.jfirer.jfire.core.aop.EnhanceWrapper;
import com.jfirer.jfire.core.aop.ProceedPoint;
import com.jfirer.jfire.core.aop.ProceedPointImpl;
import com.jfirer.jfire.core.bean.AwareContextComplete;
import com.jfirer.jfire.core.bean.BeanDefinition;
import com.jfirer.jfire.core.bean.impl.definition.PrototypeBeanDefinition;
import com.jfirer.jfire.core.bean.impl.definition.SingletonBeanDefinition;
import com.jfirer.jfire.core.beanfactory.BeanFactory;
import com.jfirer.jfire.core.inject.InjectHandler;
import com.jfirer.jfire.core.inject.impl.DefaultDependencyInjectHandler;
import com.jfirer.jfire.core.inject.impl.DefaultPropertyInjectHandler;
import com.jfirer.jfire.core.inject.notated.PropertyRead;
import com.jfirer.jfire.exception.EnhanceException;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DefaultBeanRegisterInfo extends BeanDefinitionCacheHolder implements AwareContextComplete
{
    private final boolean             prototype;
    // 该Bean的类
    private final Class<?>            type;
    private final String              beanName;
    private final BeanFactory         beanFactory;
    private final ApplicationContext  context;
    private       boolean             complete        = false;
    private final Set<EnhanceManager> enhanceManagers = new HashSet<EnhanceManager>();

    public DefaultBeanRegisterInfo(boolean prototype, Class<?> type, String beanName, ApplicationContext context, BeanFactory beanFactory)
    {
        this.prototype = prototype;
        this.type = type;
        this.beanName = beanName;
        this.context = context;
        this.beanFactory = beanFactory;
    }

    private InjectHandler[] generateInjectHandlers()
    {
        if (type.isInterface())
        {
            return new InjectHandler[0];
        }
        Collection<Field>   allFields = getAllFields(type);
        List<InjectHandler> list      = new LinkedList<InjectHandler>();
        buildCustomInjectHandlers(allFields, list);
        buildResourceInjectHandlers(allFields, list);
        buildPropertyReadInjectHandlers(allFields, list);
        return list.toArray(new InjectHandler[list.size()]);
    }

    @Override
    public void complete()
    {
        complete = true;
    }

    @Override
    public void check()
    {
        if (!complete)
        {
            throw new IllegalStateException("容器尚未初始化完成，不能调用该方法");
        }
    }

    private void buildCustomInjectHandlers(Collection<Field> allFields, List<InjectHandler> list)
    {
        list.addAll(getInjectHandlers(allFields, field -> field.isAnnotationPresent(InjectHandler.CustomInjectHanlder.class), field -> {
            try
            {
                InjectHandler injectHandler = field.getAnnotation(InjectHandler.CustomInjectHanlder.class).value().getDeclaredConstructor().newInstance();
                injectHandler.init(field, context);
                return injectHandler;
            }
            catch (Throwable e)
            {
                throw new RuntimeException(e);
            }
        }));
    }

    private void buildResourceInjectHandlers(Collection<Field> allFields, List<InjectHandler> list)
    {
        list.addAll(getInjectHandlers(allFields, field -> field.isAnnotationPresent(Resource.class), field -> {
            try
            {
                DefaultDependencyInjectHandler injectHandler = new DefaultDependencyInjectHandler();
                injectHandler.init(field, context);
                return injectHandler;
            }
            catch (Throwable e)
            {
                throw new RuntimeException(e);
            }
        }));
    }

    private void buildPropertyReadInjectHandlers(Collection<Field> allFields, List<InjectHandler> list)
    {
        list.addAll(getInjectHandlers(allFields, field -> field.isAnnotationPresent(PropertyRead.class), field -> {
            try
            {
                DefaultPropertyInjectHandler injectHandler = new DefaultPropertyInjectHandler();
                injectHandler.init(field, context);
                return injectHandler;
            }
            catch (Throwable e)
            {
                throw new RuntimeException(e);
            }
        }));
    }

    private List<InjectHandler> getInjectHandlers(Collection<Field> allFields, Predicate<Field> predicate, Function<Field, InjectHandler> function)
    {
        return allFields.stream().filter(predicate).map(function).collect(Collectors.toList());
    }

    private Method findPostConstructMethod()
    {
        if (type.isInterface())
        {
            return null;
        }
        Class ckass = type;
        while (ckass != Object.class)
        {
            Optional<Method> any = Arrays.stream(ckass.getDeclaredMethods()).filter(method -> AnnotationContext.getInstanceOn(method).isAnnotationPresent(PostConstruct.class)).findAny();
            if (any.isPresent())
            {
                Method method = any.get();
                if (Modifier.isPublic(method.getModifiers()) == false)
                {
                    throw new IllegalStateException(Formatter.format("PostConstruct标记的方法应该是public，请检查{}.{}", method.getDeclaringClass().getName(), method.getName()));
                }
                method.setAccessible(true);
                return method;
            }
            else
            {
                ckass = ckass.getSuperclass();
            }
        }
        return null;
    }

    private Collection<Field> getAllFields(Class<?> entityClass)
    {
        Map<String, Field> map = new HashMap<>();
        while (entityClass != Object.class && entityClass != null)
        {
            for (Field each : entityClass.getDeclaredFields())
            {
                if (!map.containsKey(each.getName()))
                {
                    map.put(each.getName(), each);
                }
            }
            entityClass = entityClass.getSuperclass();
        }
        return map.values();
    }

    @Override
    protected BeanDefinition internalGet()
    {
        check();
        Class           enhanceType         = buildEnhanceType();
        InjectHandler[] injectHandlers      = generateInjectHandlers();
        Method          postConstructMethod = findPostConstructMethod();
        if (prototype)
        {
            return new PrototypeBeanDefinition(beanFactory, context, postConstructMethod, injectHandlers, enhanceType, type, beanName);
        }
        else
        {
            return new SingletonBeanDefinition(beanFactory, context, postConstructMethod, injectHandlers, enhanceType, type, beanName);
        }
    }

    private Class buildEnhanceType()
    {
        if (enhanceManagers.size() == 0)
        {
            return null;
        }
        ClassModel classModel    = buildBasicsClassModel();
        String     hostFieldName = "host_" + EnhanceManager.FIELD_NAME_COUNTER.getAndIncrement();
        classModel.addField(new FieldModel(hostFieldName, type, classModel));
        addSetHostAndSetEnhanceFieldsMethod(classModel, hostFieldName);
        addInvokeHostPublicMethod(classModel, hostFieldName);
        enhanceManagers.stream().sorted(Comparator.comparingInt(EnhanceManager::order)).forEach(enhanceManager -> enhanceManager.enhance(classModel, type, context, hostFieldName));
        try
        {
            return DefaultApplicationContext.COMPILE_HELPER.compile(classModel);
        }
        catch (Throwable e)
        {
            throw new EnhanceException(e);
        }
    }

    private ClassModel buildBasicsClassModel()
    {
        ClassModel classModel = new ClassModel(type.getSimpleName() + "$AOP$" + EnhanceManager.CLASS_NAME_COUNTER.getAndIncrement());
        classModel.setPackageName(type.getPackage().getName());
        if (type.isInterface())
        {
            classModel.addInterface(type);
        }
        else
        {
            classModel.setParentClass(type);
        }
        classModel.addImport(ProceedPointImpl.class);
        classModel.addImport(ProceedPoint.class);
        classModel.addImport(Object.class);
        return classModel;
    }

    private void addSetHostAndSetEnhanceFieldsMethod(ClassModel classModel, String hostFieldName)
    {
        classModel.addInterface(EnhanceWrapper.class);
        MethodModel setAopHost = new MethodModel(classModel);
        setAopHost.setAccessLevel(MethodModel.AccessLevel.PUBLIC);
        setAopHost.setMethodName("setHost");
        setAopHost.setParamterTypes(Object.class);
        setAopHost.setReturnType(void.class);
        setAopHost.setBody(hostFieldName + " = (" + SmcHelper.getReferenceName(type, classModel) + ")$0;");
        classModel.putMethodModel(setAopHost);
        MethodModel setEnhanceFieldsMethod = new MethodModel(classModel);
        setEnhanceFieldsMethod.setAccessLevel(MethodModel.AccessLevel.PUBLIC);
        setEnhanceFieldsMethod.setMethodName("setEnhanceFields");
        setEnhanceFieldsMethod.setParamterTypes(ApplicationContext.class);
        setEnhanceFieldsMethod.setReturnType(void.class);
        setEnhanceFieldsMethod.setBody("");
        classModel.putMethodModel(setEnhanceFieldsMethod);
    }

    private void addInvokeHostPublicMethod(ClassModel classModel, String hostFieldName)
    {
        for (Method each : type.getMethods())
        {
            if (Modifier.isFinal(each.getModifiers()) || each.isBridge() || Modifier.isStatic(each.getModifiers()))
            {
                continue;
            }
            MethodModel   methodModel = new MethodModel(each, classModel);
            StringBuilder cache       = new StringBuilder();
            if (each.getReturnType() != void.class)
            {
                cache.append("return ");
            }
            cache.append(hostFieldName).append(".").append(each.getName()).append("(");
            boolean hasComma = false;
            for (int i = 0; i < methodModel.getParamterTypes().length; i++)
            {
                cache.append("$").append(i).append(',');
                hasComma = true;
            }
            if (hasComma)
            {
                cache.setLength(cache.length() - 1);
            }
            cache.append(");");
            methodModel.setBody(cache.toString());
            classModel.putMethodModel(methodModel);
        }
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

    @Override
    public void addEnhanceManager(EnhanceManager enhanceManager)
    {
        enhanceManagers.add(enhanceManager);
    }
}

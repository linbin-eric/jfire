package com.jfirer.jfire.core.inject.impl;

import com.jfirer.baseutil.Formatter;
import com.jfirer.baseutil.StringUtil;
import com.jfirer.baseutil.bytecode.support.AnnotationContext;
import com.jfirer.baseutil.reflect.ValueAccessor;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.bean.BeanRegisterInfo;
import com.jfirer.jfire.core.inject.BeanHolder;
import com.jfirer.jfire.core.inject.InjectHandler;
import com.jfirer.jfire.core.inject.notated.CanBeNull;
import com.jfirer.jfire.core.inject.notated.MapKeyBySimpleClassName;
import com.jfirer.jfire.core.inject.notated.MapKeyMethodName;
import com.jfirer.jfire.core.prepare.annotation.configuration.Primary;
import com.jfirer.jfire.exception.BeanDefinitionCanNotFindException;
import com.jfirer.jfire.exception.InjectTypeException;
import com.jfirer.jfire.exception.InjectValueException;
import com.jfirer.jfire.exception.MapKeyMethodCanNotFindException;

import javax.annotation.Resource;
import java.lang.reflect.*;
import java.util.*;

public class DefaultDependencyInjectHandler implements InjectHandler
{
    private ApplicationContext context;
    private Inject             inject;
    private ValueAccessor      valueAccessor;

    @Override
    public void init(Field field, ApplicationContext context)
    {
        if (field.getType().isPrimitive())
        {
            throw new UnsupportedOperationException("基础类型无法执行注入操作");
        }
        this.context = context;
        valueAccessor = new ValueAccessor(field);
        Class<?> fieldType = field.getType();
        /**
         *
         * 需要注意，在生成注入处理器的过程中，会发现类之间存在循环依赖。Inject的实例如果在初始化的时候就获取对应类的BeanDefinition实例，会导致死循环。
         * 因为下一个类依赖于本类，所以下一个类的注入属性也需要持有本类的BeanDefinition，但是此时本类的BeanDefinition还在生成中。
         * 解决的办法很简单，就是Inject的实例初始化时持有所需要注入属性对应的BeanRegisterInfo实例即可。
         * 在运行期在逐步创建对应的实例。
         */
        if (Map.class.isAssignableFrom(fieldType))
        {
            inject = new MapInject();
        }
        else if (Collection.class.isAssignableFrom(fieldType))
        {
            inject = new CollectionInject();
        }
        else if (BeanHolder.class.isAssignableFrom(fieldType))
        {
            inject = new BeanHolderInject();
        }
        else if (fieldType.isInterface() || Modifier.isAbstract(fieldType.getModifiers()))
        {
            inject = new AbstractInject();
        }
        else
        {
            inject = new InstacenInject();
        }
    }

    @Override
    public void inject(Object instance)
    {
        inject.inject(instance);
    }

    enum MapKeyType
    {
        BEAN_NAME,
        SIMPLE_CLASSNAME,
        METHOD
    }

    interface Inject
    {
        void inject(Object instance);
    }

    class BeanHolderImpl<T> implements BeanHolder<T>
    {
        private final T instance;

        BeanHolderImpl(T instance) {this.instance = instance;}

        @Override
        public T getSelf()
        {
            return instance;
        }
    }

    class BeanHolderInject implements Inject
    {
        private BeanRegisterInfo beanRegisterInfo;

        public BeanHolderInject()
        {
            Field    field              = valueAccessor.getField();
            Class<?> declaringClass     = field.getDeclaringClass();
            Type     actualTypeArgument = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
            if (declaringClass.equals(actualTypeArgument) == false)
            {
                throw new IllegalArgumentException(Formatter.format("请检查类:{}的字段:{}，BeanHolder字段的泛型必须要匹配该字段所在的类", declaringClass.getName(), field.getName()));
            }
            beanRegisterInfo = context.getBeanRegisterInfo(declaringClass);
            if (beanRegisterInfo == null)
            {
                //不应该出现这种情况
                throw new IllegalStateException();
            }
        }

        @Override
        public void inject(Object instance)
        {
            Object                 bean       = beanRegisterInfo.get().getBean();
            BeanHolderImpl<Object> beanHolder = new BeanHolderImpl<>(bean);
            valueAccessor.setObject(instance, beanHolder);
        }
    }

    class InstacenInject implements Inject
    {
        private BeanRegisterInfo beanRegisterInfo;

        InstacenInject()
        {
            Field             field             = valueAccessor.getField();
            AnnotationContext annotationContext = AnnotationContext.getInstanceOn(field);
            Resource          resource          = annotationContext.getAnnotation(Resource.class);
            String            beanName          = StringUtil.isNotBlank(resource.name()) ? resource.name() : field.getType().getName();
            beanRegisterInfo = context.getBeanRegisterInfo(beanName);
            if (beanRegisterInfo == null)
            {
                beanRegisterInfo = context.getBeanRegisterInfo(field.getType());
            }
            if (beanRegisterInfo == null && !annotationContext.isAnnotationPresent(CanBeNull.class))
            {
                throw new InjectValueException("无法找到属性:" + field.getDeclaringClass().getSimpleName() + "." + field.getName() + "可以注入的bean");
            }
        }

        public void inject(Object instance)
        {
            Object value = beanRegisterInfo.get().getBean();
            try
            {
                valueAccessor.setObject(instance, value);
            }
            catch (Exception e)
            {
                throw new InjectValueException(e);
            }
        }
    }

    class AbstractInject implements Inject
    {
        BeanRegisterInfo beanRegisterInfo;

        AbstractInject()
        {
            Field             field             = valueAccessor.getField();
            Class<?>          fieldType         = field.getType();
            AnnotationContext annotationContext = AnnotationContext.getInstanceOn(field);
            Resource          resource          = annotationContext.getAnnotation(Resource.class);
            // 如果定义了名称，就寻找特定名称的Bean
            if (StringUtil.isNotBlank(resource.name()))
            {
                beanRegisterInfo = context.getBeanRegisterInfo(resource.name());
                if (beanRegisterInfo == null && !annotationContext.isAnnotationPresent(CanBeNull.class))
                {
                    throw new BeanDefinitionCanNotFindException(resource.name());
                }
            }
            else
            {
                Collection<BeanRegisterInfo> beanRegisterInfos = context.getBeanRegisterInfos(fieldType);
                if (beanRegisterInfos.size() > 1)
                {
                    List<BeanRegisterInfo> primary = beanRegisterInfos.stream().filter(beanRegisterInfo -> AnnotationContext.isAnnotationPresent(Primary.class, beanRegisterInfo.getType())).toList();
                    if (primary.size() != 1)
                    {
                        throw new BeanDefinitionCanNotFindException(beanRegisterInfos, fieldType);
                    }
                    else
                    {
                        beanRegisterInfo = primary.get(0);
                    }
                }
                else if (beanRegisterInfos.size() == 1)
                {
                    beanRegisterInfo = beanRegisterInfos.iterator().next();
                }
                else if (annotationContext.isAnnotationPresent(CanBeNull.class))
                {
                    //可为空，允许
                }
                else
                {
                    throw new BeanDefinitionCanNotFindException(beanRegisterInfos, fieldType);
                }
            }
        }

        @Override
        public void inject(Object instance)
        {
            if (beanRegisterInfo != null)
            {
                Object value = beanRegisterInfo.get().getBean();
                try
                {
                    valueAccessor.setObject(instance, value);
                }
                catch (Exception e)
                {
                    throw new InjectValueException(e);
                }
            }
        }
    }

    class CollectionInject implements Inject
    {
        private static final int                LIST      = 1;
        private static final int                SET       = 2;
        private final        BeanRegisterInfo[] beanRegisterInfos;
        private              int                listOrSet = 0;

        CollectionInject()
        {
            Field field       = valueAccessor.getField();
            Type  genericType = field.getGenericType();
            if (!(genericType instanceof ParameterizedType))
            {
                throw new InjectTypeException(field.toGenericString() + "不是泛型定义，无法找到需要注入的Bean类型");
            }
            Class<?> rawType = (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0];
            beanRegisterInfos = context.getBeanRegisterInfos(rawType).stream().toArray(BeanRegisterInfo[]::new);
            if (List.class.isAssignableFrom(field.getType()))
            {
                listOrSet = LIST;
            }
            else if (Set.class.isAssignableFrom(field.getType()))
            {
                listOrSet = SET;
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public void inject(Object instance)
        {
            try
            {
                Collection<Object> value = (Collection<Object>) valueAccessor.get(instance);
                if (value == null)
                {
                    if (listOrSet == LIST)
                    {
                        value = new LinkedList<Object>();
                        valueAccessor.setObject(instance, value);
                    }
                    else if (listOrSet == SET)
                    {
                        value = new HashSet<Object>();
                        valueAccessor.setObject(instance, value);
                    }
                    else
                    {
                        throw new InjectValueException("无法识别类型:" + valueAccessor.getField().getType().getName() + "，无法生成其对应的实例");
                    }
                }
                for (BeanRegisterInfo each : beanRegisterInfos)
                {
                    value.add(each.get().getBean());
                }
            }
            catch (InjectValueException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                throw new InjectValueException(e);
            }
        }
    }

    class MapInject implements Inject
    {
        MapKeyType         mapKeyType;
        BeanRegisterInfo[] beanRegisterInfos;
        Method             method;

        MapInject()
        {
            Field field       = valueAccessor.getField();
            Type  genericType = field.getGenericType();
            if (!(genericType instanceof ParameterizedType))
            {
                throw new InjectTypeException(field.toGenericString() + "不是泛型定义，无法找到需要注入的Bean类型");
            }
            Class<?> rawType = (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[1];
            beanRegisterInfos = context.getBeanRegisterInfos(rawType).stream().toArray(BeanRegisterInfo[]::new);
            AnnotationContext annotationContext = AnnotationContext.getInstanceOn(field);
            if (annotationContext.isAnnotationPresent(MapKeyMethodName.class))
            {
                mapKeyType = MapKeyType.METHOD;
                String methodName = annotationContext.getAnnotation(MapKeyMethodName.class).value();
                try
                {
                    method = rawType.getMethod(methodName);
                }
                catch (Exception e)
                {
                    throw new MapKeyMethodCanNotFindException(methodName, rawType, e);
                }
            }
            else if (annotationContext.isAnnotationPresent(MapKeyBySimpleClassName.class))
            {
                mapKeyType = MapKeyType.SIMPLE_CLASSNAME;
            }
            else
            {
                mapKeyType = MapKeyType.BEAN_NAME;
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public void inject(Object instance)
        {
            try
            {
                Map<Object, Object> value = (Map<Object, Object>) valueAccessor.get(instance);
                if (value == null)
                {
                    value = new HashMap<>();
                    valueAccessor.setObject(instance, value);
                }
                switch (mapKeyType)
                {
                    case SIMPLE_CLASSNAME ->
                    {
                        for (BeanRegisterInfo each : beanRegisterInfos)
                        {
                            Object entryValue = each.get().getBean();
                            String entryKey   = each.getType().getSimpleName();
                            value.put(entryKey, entryValue);
                        }
                    }
                    case METHOD ->
                    {
                        for (BeanRegisterInfo each : beanRegisterInfos)
                        {
                            Object entryValue = each.get().getBean();
                            Object entryKey   = method.invoke(entryValue);
                            value.put(entryKey, entryValue);
                        }
                    }
                    case BEAN_NAME ->
                    {
                        for (BeanRegisterInfo each : beanRegisterInfos)
                        {
                            Object entryValue = each.get().getBean();
                            String entryKey   = each.getBeanName();
                            value.put(entryKey, entryValue);
                        }
                    }
                    default ->
                    {
                        ;
                    }
                }
            }
            catch (Exception e)
            {
                throw new InjectValueException(e);
            }
        }
    }
}

package cc.jfire.jfire.core.inject.impl;

import cc.jfire.baseutil.StringUtil;
import cc.jfire.baseutil.bytecode.support.AnnotationContext;
import cc.jfire.baseutil.encrypt.Base64Tool;
import cc.jfire.baseutil.reflect.valueaccessor.ValueAccessor;
import cc.jfire.jfire.core.ApplicationContext;
import cc.jfire.jfire.core.inject.InjectHandler;
import cc.jfire.jfire.core.inject.notated.PropertyRead;
import cc.jfire.jfire.exception.InjectTypeException;
import cc.jfire.jfire.exception.InjectValueException;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

public class DefaultPropertyInjectHandler implements InjectHandler
{
    private ValueAccessor valueAccessor;
    private Object        propertyValue;
    private String        propertyName;
    private Inject        inject;

    @Override
    public void init(Field field, ApplicationContext applicationContext)
    {
        valueAccessor = ValueAccessor.standard(field);
        AnnotationContext annotationContext = AnnotationContext.getInstanceOn(field);
        PropertyRead      propertyRead      = annotationContext.getAnnotation(PropertyRead.class);
        propertyName = StringUtil.isNotBlank(propertyRead.value()) ? propertyRead.value() : field.getName();
        Map<String, Object> config = applicationContext.getConfig().fullPathConfig();
        if (StringUtil.isNotBlank(System.getProperty(propertyName)))
        {
            propertyValue = System.getProperty(propertyName);
        }
        else if (config.containsKey(propertyName))
        {
            propertyValue = config.get(propertyName);
        }
        else
        {
        }
        if (propertyValue != null)
        {
            Class<?> type = field.getType();
            if (type == int.class || type == Integer.class)
            {
                inject = new IntInject();
            }
            else if (type == short.class || type == Short.class)
            {
                inject = new ShortInject();
            }
            else if (type == long.class || type == Long.class)
            {
                inject = new LongInject();
            }
            else if (type == float.class || type == Float.class)
            {
                inject = new FloatInject();
            }
            else if (type == double.class || type == Double.class)
            {
                inject = new DoubleInject();
            }
            else if (type == boolean.class || type == Boolean.class)
            {
                inject = new BooleanInject();
            }
            else if (type == byte[].class)
            {
                inject = new ByteArrayInject();
            }
            else if (type == Class.class)
            {
                inject = new ClassInject();
            }
            else if (type == File.class)
            {
                inject = new FileInject();
            }
            else if (type == int[].class)
            {
                inject = new IntArrayInject();
            }
            else if (type == String.class)
            {
                inject = new StringInject();
            }
            else if (type == String[].class)
            {
                inject = new StringArrayInject();
            }
            else if (type == Set.class)
            {
                inject = new SetStringInject();
            }
            else if (type.isEnum())
            {
                inject = new EnumInject(field);
            }
            else if (type == Map.class)
            {
                inject = new MapInject();
            }
            else
            {
                throw new InjectTypeException("无法识别的参数注入类型，请检查" + field.toGenericString());
            }
        }
    }

    @Override
    public void inject(Object instance)
    {
        if (inject == null)
        {
            return;
        }
        inject.inject(instance);
    }

    interface Inject
    {
        void inject(Object instance);
    }

    abstract class AbstractInject implements Inject
    {
        protected Object value;

        @Override
        public void inject(Object instance)
        {
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

    class BooleanInject extends AbstractInject
    {
        BooleanInject()
        {
            value = Boolean.valueOf((String) propertyValue);
        }
    }

    class IntInject extends AbstractInject
    {
        IntInject()
        {
            value = Integer.valueOf((String) propertyValue);
        }
    }

    class LongInject extends AbstractInject
    {
        LongInject()
        {
            value = Long.valueOf((String) propertyValue);
        }
    }

    class ShortInject extends AbstractInject
    {
        ShortInject()
        {
            value = Short.valueOf((String) propertyValue);
        }
    }

    class FloatInject extends AbstractInject
    {
        FloatInject()
        {
            value = Float.valueOf((String) propertyValue);
        }
    }

    class DoubleInject extends AbstractInject
    {
        DoubleInject()
        {
            value = Double.valueOf((String) propertyValue);
        }
    }

    class ByteArrayInject extends AbstractInject
    {
        ByteArrayInject()
        {
            String tmp = (String) propertyValue;
            if (tmp.startsWith("0x"))
            {
                value = StringUtil.hexStringToBytes(tmp.substring(2));
            }
            else
            {
                value = Base64Tool.decode(tmp);
            }
        }
    }

    class ClassInject extends AbstractInject
    {
        ClassInject()
        {
            try
            {
                value = this.getClass().getClassLoader().loadClass((String) propertyValue);
            }
            catch (Exception e)
            {
                throw new InjectValueException(e);
            }
        }
    }

    class FileInject extends AbstractInject
    {
        FileInject()
        {
            value = new File((String) propertyValue);
        }
    }

    class IntArrayInject extends AbstractInject
    {
        IntArrayInject()
        {
            if (propertyValue instanceof String s)
            {
                String[] tmp   = s.split(",");
                int[]    array = new int[tmp.length];
                for (int i = 0; i < array.length; i++)
                {
                    array[i] = Integer.parseInt(tmp[i]);
                }
                value = array;
            }
            else if (propertyValue instanceof List<?> list)
            {
                value = list.stream().map(s -> (String) s).mapToInt(Integer::parseInt).toArray();
            }
            else
            {
                throw new IllegalArgumentException("属性:" + propertyName + "实际值不是数字列表也不是以,隔开的数字字符串，解析失败");
            }
        }
    }

    class StringInject extends AbstractInject
    {
        StringInject()
        {
            if (propertyValue instanceof String)
            {
                value = propertyValue;
            }
            else
            {
                throw new IllegalArgumentException("属性:" + propertyName + "不是字符串，解析失败");
            }
        }
    }

    class StringArrayInject extends AbstractInject
    {
        StringArrayInject()
        {
            if (propertyValue instanceof String)
            {
                value = ((String) propertyValue).split(",");
            }
            else if (propertyValue instanceof List<?> list)
            {
                value = list.stream().map(s -> (String) s).toArray(String[]::new);
            }
            else
            {
                throw new IllegalArgumentException("属性:" + propertyName + "实际值不是字符串列表也不是以,隔开的字符串，解析失败");
            }
        }
    }

    class SetStringInject extends AbstractInject
    {
        SetStringInject()
        {
            Set<String> set = new HashSet<String>();
            if (propertyValue instanceof String s)
            {
                Collections.addAll(set, s.split(","));
            }
            else if (propertyValue instanceof List<?> list)
            {
                list.stream().map(s -> (String) s).forEach(set::add);
            }
            value = set;
        }
    }

    class EnumInject extends AbstractInject
    {
        @SuppressWarnings({"unchecked", "rawtypes"})
        EnumInject(Field field)
        {
            value = Enum.valueOf((Class<Enum>) field.getType(), (String) propertyValue);
        }
    }

    class MapInject extends AbstractInject
    {
        public MapInject()
        {
            if (propertyValue instanceof Map<?, ?>)
            {
                value = propertyValue;
            }
            else
            {
                throw new IllegalArgumentException("属性:" + propertyName + "实际值不是Map，解析失败");
            }
        }
    }
}

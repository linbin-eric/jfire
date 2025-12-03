package cc.jfire.jfire.core.prepare.annotation.condition.provide;

import cc.jfire.baseutil.StringUtil;
import cc.jfire.baseutil.bytecode.annotation.ValuePair;
import cc.jfire.baseutil.bytecode.support.AnnotationContext;
import cc.jfire.jfire.core.ApplicationContext;
import cc.jfire.jfire.core.prepare.annotation.condition.Condition;
import cc.jfire.jfire.core.prepare.annotation.condition.Conditional;
import cc.jfire.jfire.core.prepare.annotation.condition.ErrorMessage;
import cc.jfire.jfire.core.prepare.annotation.condition.provide.ConditionOnProperty.OnProperty;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;

@Retention(RetentionPolicy.RUNTIME)
@Conditional(OnProperty.class)
public @interface ConditionOnProperty
{
    /**
     * 两种写法：
     * 1、只写属性名称，此时只要求属性存在
     * 2、书写"k=v"的形式，此时要求属性存在并且值也相等
     *
     * @return
     */
    String[] value();

    class OnProperty implements Condition
    {
        @Override
        public boolean match(ApplicationContext context, AnnotatedElement element, ErrorMessage errorMessage)
        {
            return Arrays.stream(AnnotationContext.getAnnotationMetadata(ConditionOnProperty.class, element).getAttribyte("value").getArray())//
                         .map(ValuePair::getStringValue)//
                         .allMatch(value -> {
                             if (value.contains("="))
                             {
                                 String[] split            = value.split("=");
                                 String   property         = split[0];
                                 String   propertyValue    = split[1];
                                 String   envPropertyValue = (String) context.getConfig().fullPathConfig().get(property);
                                 return StringUtil.isNotBlank(envPropertyValue) && envPropertyValue.equals(propertyValue);
                             }
                             else
                             {
                                 return context.getConfig().fullPathConfig().containsKey(value);
                             }
                         });
        }
    }
}

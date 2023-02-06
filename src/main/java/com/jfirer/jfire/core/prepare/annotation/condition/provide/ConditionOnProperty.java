package com.jfirer.jfire.core.prepare.annotation.condition.provide;

import com.jfirer.baseutil.StringUtil;
import com.jfirer.baseutil.bytecode.annotation.ValuePair;
import com.jfirer.baseutil.bytecode.support.AnnotationContext;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.prepare.annotation.condition.Condition;
import com.jfirer.jfire.core.prepare.annotation.condition.Conditional;
import com.jfirer.jfire.core.prepare.annotation.condition.ErrorMessage;
import com.jfirer.jfire.core.prepare.annotation.condition.provide.ConditionOnProperty.OnProperty;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.AnnotatedElement;

@Retention(RetentionPolicy.RUNTIME)
@Conditional(OnProperty.class)
public @interface ConditionOnProperty
{
    /**
     * 需要存在的属性名称
     *
     * @return
     */
    String[] value();

    class OnProperty implements Condition
    {
        @Override
        public boolean match(ApplicationContext context, AnnotatedElement element, ErrorMessage errorMessage)
        {
            for (ValuePair each : AnnotationContext.getAnnotationMetadata(ConditionOnProperty.class, element).getAttribyte("value").getArray())
            {
                if (!StringUtil.isNotBlank(context.getEnv().getProperty(each.getStringValue())))
                {
                    errorMessage.addErrorMessage("缺少属性:" + each);
                    return false;
                }
            }
            return true;
        }
    }
}

package cc.jfire.jfire.core.prepare.annotation.condition.provide;

import cc.jfire.baseutil.bytecode.annotation.ValuePair;
import cc.jfire.baseutil.bytecode.support.AnnotationContext;
import cc.jfire.jfire.core.ApplicationContext;
import cc.jfire.jfire.core.prepare.annotation.condition.Condition;
import cc.jfire.jfire.core.prepare.annotation.condition.Conditional;
import cc.jfire.jfire.core.prepare.annotation.condition.ErrorMessage;
import cc.jfire.jfire.core.prepare.annotation.condition.provide.ConditionOnMissBeanType.OnMissBeanType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.AnnotatedElement;

@Retention(RetentionPolicy.RUNTIME)
@Conditional(OnMissBeanType.class)
public @interface ConditionOnMissBeanType
{
    Class<?>[] value();

    class OnMissBeanType implements Condition
    {
        @Override
        public boolean match(ApplicationContext context, AnnotatedElement element, ErrorMessage errorMessage)
        {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            ValuePair[] value       = AnnotationContext.getAnnotationMetadata(ConditionOnMissBeanType.class, element).getAttribyte("value").getArray();
            for (ValuePair each : value)
            {
                Class<?> aClass;
                try
                {
                    aClass = classLoader.loadClass(each.getClassName());
                }
                catch (ClassNotFoundException e)
                {
                    continue;
                }
                if (context.getBeanRegisterInfo(aClass) != null)
                {
                    errorMessage.addErrorMessage("已经存在类型:" + each + "的Bean");
                    return false;
                }
            }
            return true;
        }
    }
}

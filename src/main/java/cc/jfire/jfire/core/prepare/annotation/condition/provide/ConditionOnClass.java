package cc.jfire.jfire.core.prepare.annotation.condition.provide;

import cc.jfire.baseutil.bytecode.annotation.AnnotationMetadata;
import cc.jfire.baseutil.bytecode.annotation.ValuePair;
import cc.jfire.baseutil.bytecode.support.AnnotationContext;
import cc.jfire.jfire.core.ApplicationContext;
import cc.jfire.jfire.core.prepare.annotation.condition.Condition;
import cc.jfire.jfire.core.prepare.annotation.condition.Conditional;
import cc.jfire.jfire.core.prepare.annotation.condition.ErrorMessage;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Conditional(ConditionOnClass.ConditonOnClassProcessor.class)
public @interface ConditionOnClass
{
    Class<?>[] value();

    class ConditonOnClassProcessor implements Condition
    {
        @Override
        public boolean match(ApplicationContext context, AnnotatedElement element, ErrorMessage errorMessage)
        {
            AnnotationMetadata annotationMetadata = AnnotationContext.getAnnotationMetadata(ConditionOnClass.class, element);
            for (ValuePair each : annotationMetadata.getAttribyte("value").getArray())
            {
                try
                {
                    Thread.currentThread().getContextClassLoader().loadClass(each.getClassName());
                }
                catch (ClassNotFoundException e)
                {
                    errorMessage.addErrorMessage("classpath不存在类:" + each + "不存在");
                    return false;
                }
            }
            return true;
        }
    }
}

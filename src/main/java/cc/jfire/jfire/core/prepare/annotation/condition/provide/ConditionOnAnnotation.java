package cc.jfire.jfire.core.prepare.annotation.condition.provide;

import cc.jfire.baseutil.bytecode.annotation.ValuePair;
import cc.jfire.baseutil.bytecode.support.AnnotationContext;
import cc.jfire.jfire.core.ApplicationContext;
import cc.jfire.jfire.core.prepare.annotation.condition.Condition;
import cc.jfire.jfire.core.prepare.annotation.condition.Conditional;
import cc.jfire.jfire.core.prepare.annotation.condition.ErrorMessage;
import cc.jfire.jfire.core.prepare.annotation.configuration.Configuration;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.AnnotatedElement;

@Retention(RetentionPolicy.RUNTIME)
@Conditional(ConditionOnAnnotation.OnAnnotation.class)
public @interface ConditionOnAnnotation
{
    Class<? extends Annotation>[] value();

    class OnAnnotation implements Condition
    {
        @Override
        public boolean match(ApplicationContext context, AnnotatedElement element, ErrorMessage errorMessage)
        {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            ValuePair[] value       = AnnotationContext.getAnnotationMetadata(ConditionOnAnnotation.class, element).getAttribyte("value").getArray();
            for (ValuePair each : value)
            {
                Class<? extends Annotation> aClass;
                try
                {
                    aClass = (Class<? extends Annotation>) classLoader.loadClass(each.getClassName());
                }
                catch (ClassNotFoundException e)
                {
                    errorMessage.addErrorMessage("注解:" + each + "不存在于类路径");
                    return false;
                }
                boolean has = context.getAllBeanRegisterInfos().stream().filter(beanRegisterInfo -> AnnotationContext.isAnnotationPresent(Configuration.class, beanRegisterInfo.getType()))//
                                     .filter(beanRegisterInfo -> AnnotationContext.isAnnotationPresent(aClass, beanRegisterInfo.getType())).findAny().isPresent();
                if (!has)
                {
                    errorMessage.addErrorMessage("注解:" + each + "没有标注在配置类上");
                    return false;
                }
            }
            return true;
        }
    }
}

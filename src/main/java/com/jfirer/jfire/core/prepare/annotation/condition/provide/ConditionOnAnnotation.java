package com.jfirer.jfire.core.prepare.annotation.condition.provide;

import com.jfirer.baseutil.bytecode.annotation.AnnotationMetadata;
import com.jfirer.baseutil.bytecode.annotation.ValuePair;
import com.jfirer.baseutil.bytecode.support.AnnotationContext;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.prepare.annotation.condition.Conditional;
import com.jfirer.jfire.core.prepare.annotation.condition.ErrorMessage;
import com.jfirer.jfire.core.prepare.annotation.configuration.Configuration;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Conditional(ConditionOnAnnotation.OnAnnotation.class)
public @interface ConditionOnAnnotation
{
    Class<? extends Annotation>[] value();

    class OnAnnotation extends BaseCondition
    {
        public OnAnnotation()
        {
            super(ConditionOnAnnotation.class);
        }

        @Override
        protected boolean handleSelectAnnoType(ApplicationContext context, AnnotationMetadata metadata, ErrorMessage errorMessage)
        {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            ValuePair[] value       = metadata.getAttribyte("value").getArray();
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

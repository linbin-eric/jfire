package com.jfirer.jfire.core.prepare.annotation.condition.provide;

import com.jfirer.baseutil.bytecode.annotation.ValuePair;
import com.jfirer.baseutil.bytecode.support.AnnotationContext;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.bean.BeanRegisterInfo;
import com.jfirer.jfire.core.prepare.annotation.condition.Condition;
import com.jfirer.jfire.core.prepare.annotation.condition.Conditional;
import com.jfirer.jfire.core.prepare.annotation.condition.ErrorMessage;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.AnnotatedElement;

@Conditional(ConditionOnBean.OnBean.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConditionOnBean
{
    Class<?>[] value();

    class OnBean implements Condition
    {
        @Override
        public boolean match(ApplicationContext context, AnnotatedElement element, ErrorMessage errorMessage)
        {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            ValuePair[] beanTypes   = AnnotationContext.getAnnotationMetadata(ConditionOnBean.class, element).getAttribyte("value").getArray();
            for (ValuePair beanType : beanTypes)
            {
                Class<?> aClass;
                try
                {
                    aClass = classLoader.loadClass(beanType.getClassName());
                }
                catch (ClassNotFoundException e)
                {
                    errorMessage.addErrorMessage("类:" + beanType + "不存在classpath，对应的Bean也不会存在");
                    return false;
                }
                boolean          miss             = true;
                BeanRegisterInfo beanRegisterInfo = context.getBeanRegisterInfo(aClass);
                if (beanRegisterInfo == null)
                {
                    errorMessage.addErrorMessage("没有Bean是" + beanType + "类型或者其子类");
                    return false;
                }
            }
            return true;
        }
    }
}

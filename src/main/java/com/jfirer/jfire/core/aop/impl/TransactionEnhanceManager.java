package com.jfirer.jfire.core.aop.impl;

import com.jfirer.baseutil.bytecode.support.AnnotationContext;
import com.jfirer.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.baseutil.smc.SmcHelper;
import com.jfirer.baseutil.smc.model.ClassModel;
import com.jfirer.baseutil.smc.model.FieldModel;
import com.jfirer.baseutil.smc.model.MethodModel;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.DefaultApplicationContext;
import com.jfirer.jfire.core.aop.EnhanceManager;
import com.jfirer.jfire.core.aop.impl.support.transaction.Propagation;
import com.jfirer.jfire.core.aop.impl.support.transaction.TransactionManager;
import com.jfirer.jfire.core.aop.impl.support.transaction.TransactionState;
import com.jfirer.jfire.core.aop.notated.Transactional;
import com.jfirer.jfire.core.bean.BeanRegisterInfo;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.Predicate;

public class TransactionEnhanceManager implements EnhanceManager
{
    AnnotationContextFactory annotationContextFactory = DefaultApplicationContext.ANNOTATION_CONTEXT_FACTORY;

    @Override
    public Predicate<BeanRegisterInfo> needEnhance(ApplicationContext context)
    {
        return beanRegisterInfo -> Arrays.stream(beanRegisterInfo.getType().getDeclaredMethods()).filter(method -> annotationContextFactory.get(method).isAnnotationPresent(Transactional.class)).findAny().isPresent();
    }

    @Override
    public void enhance(ClassModel classModel, Class<?> type, ApplicationContext applicationContext, String hostFieldName)
    {
        classModel.addImport(ReflectUtil.class);
        classModel.addImport(Propagation.class);
        String transFieldName = generateTransactionManagerField(classModel);
        addBodyToSetEnhanceFields(//
                                  transFieldName + "=(" + SmcHelper.getReferenceName(TransactionManager.class, classModel) + ")$0.getBean(\"" + applicationContext.getBeanRegisterInfo(TransactionManager.class).getBeanName() + "\");",//
                                  classModel);
        for (Method method : type.getMethods())
        {
            AnnotationContext annotationContext = annotationContextFactory.get(method);
            if (Modifier.isFinal(method.getModifiers()) || !annotationContext.isAnnotationPresent(Transactional.class) || method.isBridge())
            {
                continue;
            }
            MethodModel.MethodModelKey key    = new MethodModel.MethodModelKey(method);
            MethodModel                origin = classModel.removeMethodModel(key);
            origin.setAccessLevel(MethodModel.AccessLevel.PRIVATE);
            origin.setMethodName(origin.getMethodName() + "_" + METHOD_NAME_COUNTER.getAndIncrement());
            classModel.putMethodModel(origin);
            MethodModel   newOne               = new MethodModel(method, classModel);
            StringBuilder cache                = new StringBuilder();
            Transactional transactional        = annotationContext.getAnnotation(Transactional.class);
            String        propagation          = "Propagation." + transactional.propagation().name();
            String        transactionStateName = "transactionState_" + VAR_NAME_COUNTER.getAndIncrement();
            cache.append(SmcHelper.getReferenceName(TransactionState.class, classModel)).append(" ").append(transactionStateName)//
                 .append(" = ").append(transFieldName).append(".beginTransAction(").append(propagation).append(");\r\n");
            cache.append("try\r\n{\r\n");
            if (method.getReturnType() != void.class)
            {
                cache.append(SmcHelper.getReferenceName(method.getReturnType(), classModel)).append(" result = ").append(origin.generateInvoke()).append(";\r\n");
                cache.append(transFieldName).append(".commit(").append(transactionStateName).append(");\r\n");
                cache.append("return result;\r\n");
            }
            else
            {
                cache.append(origin.generateInvoke()).append(";\r\n");
                cache.append(transFieldName).append(".commit(").append(transactionStateName).append(");\r\n");
            }
            cache.append("}\r\n");
            cache.append("catch(java.lang.Throwable e)\r\n{\r\n");
            cache.append(transFieldName).append(".rollback(").append(transactionStateName).append(",e);\r\n");
            cache.append("ReflectUtil.throwException(e);\r\n");
            if (method.getReturnType() != void.class)
            {
                if (method.getReturnType().isPrimitive())
                {
                    Class<?> returnType = method.getReturnType();
                    if (returnType == boolean.class)
                    {
                        cache.append("return false;\r\n");
                    }
                    else
                    {
                        cache.append("return 0;\r\n");
                    }
                }
                else
                {
                    cache.append("return null;\r\n");
                }
            }
            cache.append("}\r\n");
            newOne.setBody(cache.toString());
            if (method.getGenericParameterTypes().length != 0)
            {
                boolean[] flags = new boolean[method.getParameterTypes().length];
                Arrays.fill(flags, true);
                newOne.setParamterFinals(flags);
            }
            classModel.putMethodModel(newOne);
        }
    }

    private String generateTransactionManagerField(ClassModel classModel)
    {
        String     transFieldName = "transactionManager_" + FIELD_NAME_COUNTER.getAndIncrement();
        FieldModel fieldModel     = new FieldModel(transFieldName, TransactionManager.class, classModel);
        classModel.addField(fieldModel);
        return transFieldName;
    }

    @Override
    public int order()
    {
        return TRANSACTION;
    }
}

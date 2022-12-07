package com.jfirer.jfire.core.aop.impl;

import com.jfirer.baseutil.bytecode.support.AnnotationContext;
import com.jfirer.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfirer.baseutil.smc.SmcHelper;
import com.jfirer.baseutil.smc.model.ClassModel;
import com.jfirer.baseutil.smc.model.FieldModel;
import com.jfirer.baseutil.smc.model.MethodModel;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.DefaultApplicationContext;
import com.jfirer.jfire.core.aop.EnhanceManager;

import javax.validation.Constraint;
import javax.validation.Valid;
import javax.validation.executable.ValidateOnExecution;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Optional;

public class ValidateAopManager implements EnhanceManager
{
    private String validatorBeanNamen;

    @Override
    public void scan(ApplicationContext context)
    {
        AnnotationContextFactory annotationContextFactory = DefaultApplicationContext.ANNOTATION_CONTEXT_FACTORY;
        interlScan(context,//
                   method -> annotationContextFactory.get(method).isAnnotationPresent(ValidateOnExecution.class),//
                   beanRegisterInfo -> beanRegisterInfo.addEnhanceManager(ValidateAopManager.this)//
        );
        validatorBeanNamen = Optional.ofNullable(context.getBeanRegisterInfo(JfireMethodValidator.class)).map(beanRegisterInfo -> beanRegisterInfo.getBeanName()).orElse(null);
    }

    @Override
    public void enhance(ClassModel classModel, Class<?> type, ApplicationContext applicationContext, String hostFieldName)
    {
        String validateFieldName = generateValidatorField(classModel);
        setFiledValue(classModel, validateFieldName);
        AnnotationContextFactory annotationContextFactory = DefaultApplicationContext.ANNOTATION_CONTEXT_FACTORY;
        for (Method method : type.getMethods())
        {
            if (Modifier.isFinal(method.getModifiers()))
            {
                continue;
            }
            AnnotationContext annotationContext = annotationContextFactory.get(method);
            if (annotationContext.isAnnotationPresent(ValidateOnExecution.class))
            {
                if (hasConstraintBeforeMethodExecute(method, annotationContext))
                {
                    processValidateParamter(classModel, hostFieldName, validateFieldName, method);
                }
                if (hasConstraintOnReturnValue(method))
                {
                    processValidateReturnValue(classModel, hostFieldName, validateFieldName, method);
                }
            }
        }
    }

    private void setFiledValue(ClassModel classModel, String validateFieldName)
    {
        MethodModel.MethodModelKey key1 = new MethodModel.MethodModelKey();
        key1.setAccessLevel(MethodModel.AccessLevel.PUBLIC);
        key1.setMethodName("setEnhanceFields");
        key1.setParamterTypes(new Class[]{ApplicationContext.class});
        MethodModel setEnhanceFieldsMethod     = classModel.getMethodModel(key1);
        String      setEnhanceFieldsMethodBody = setEnhanceFieldsMethod.getBody();
        setEnhanceFieldsMethodBody += validateFieldName + "=(" + SmcHelper.getReferenceName(JfireMethodValidator.class, classModel) + ")$0.getBean(\"" + validatorBeanNamen + "\");\r\n";
        setEnhanceFieldsMethod.setBody(setEnhanceFieldsMethodBody);
    }

    private static boolean hasConstraintBeforeMethodExecute(Method method, AnnotationContext annotationContext)
    {
        if (annotationContext.isAnnotationPresent(Constraint.class))
        {
            return true;
        }
        for (Annotation[] parameterAnnotations : method.getParameterAnnotations())
        {
            for (Annotation annotation : parameterAnnotations)
            {
                if (annotation.annotationType() == Valid.class)
                {
                    return true;
                }
                if (annotation.annotationType().isAnnotationPresent(Constraint.class))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean hasConstraintOnReturnValue(Method method)
    {
        return method.getReturnType() != void.class && !method.getReturnType().isPrimitive() && method.isAnnotationPresent(Valid.class);
    }

    private String generateMethodMapField(ClassModel classModel)
    {
        String     fieldName  = "validator_" + FIELD_NAME_COUNTER.getAndIncrement();
        FieldModel fieldModel = new FieldModel(fieldName, Map.class, classModel);
        classModel.addField(fieldModel);
        return fieldName;
    }

    /**
     * @param classModel
     * @param hostFieldName
     * @param validateFieldName
     * @param method
     * @return
     */
    private void processValidateReturnValue(ClassModel classModel, String hostFieldName, String validateFieldName, Method method)
    {
        MethodModel.MethodModelKey key    = new MethodModel.MethodModelKey(method);
        MethodModel                origin = classModel.removeMethodModel(key);
        origin.setMethodName(origin.getMethodName() + "_" + METHOD_NAME_COUNTER.getAndIncrement());
        classModel.putMethodModel(origin);
        StringBuilder cache = new StringBuilder();
        cache.append(method.getReturnType().getSimpleName()).append(" result = ").append(origin.generateInvoke()).append(";\r\n");
        String methodParamName = "methodParam_$" + FIELD_NAME_COUNTER.getAndIncrement();
        cache.append("try{Method " + methodParamName + "=" + hostFieldName + ".getClass().getDeclaredMethod(\"" + method.getName() + "\"");
        if (method.getParameterTypes().length == 0)
        {
            cache.append(");\r\n");
        }
        else
        {
            for (Class<?> parameterType : method.getParameterTypes())
            {
                cache.append(",").append(SmcHelper.getReferenceName(parameterType, classModel) + ".class,");
            }
            cache.deleteCharAt(cache.length() - 1).append(");\r\n");
        }
        cache.append(validateFieldName).append(".validateReturnValue(").append(hostFieldName).append(",")//
             .append(methodParamName).append(",result);\r\n");
        cache.append("});\r\n");
        cache.append("}catch (NoSuchMethodException e){throw new RuntimeException(e);}\r\n");
        cache.append("return result;\r\n");
        classModel.addImport(NoSuchMethodException.class);
        classModel.addImport(RuntimeException.class);
        classModel.addImport(Method.class);
        MethodModel methodModel = new MethodModel(method, classModel);
        methodModel.setBody(cache.toString());
        classModel.putMethodModel(methodModel);
    }

    /**
     * @param classModel
     * @param hostFieldName
     * @param validateFieldName
     * @param method
     */
    private void processValidateParamter(ClassModel classModel, String hostFieldName, String validateFieldName, Method method)
    {
        StringBuilder cache           = new StringBuilder();
        String        methodParamName = "methodParam_$" + FIELD_NAME_COUNTER.getAndIncrement();
        cache.append("try{Method " + methodParamName + "=" + hostFieldName + ".getClass().getDeclaredMethod(\"" + method.getName() + "\"");
        if (method.getParameterTypes().length == 0)
        {
            cache.append(");\r\n");
        }
        else
        {
            for (Class<?> parameterType : method.getParameterTypes())
            {
                cache.append(",").append(SmcHelper.getReferenceName(parameterType, classModel) + ".class,");
            }
            cache.deleteCharAt(cache.length() - 1).append(");\r\n");
        }
        cache.append(validateFieldName).append(".validateParameters(").append(hostFieldName).append("," + methodParamName + ",")//
             .append("new Object[]{");
        int     length   = method.getParameterTypes().length;
        boolean hasComma = false;
        for (int i = 0; i < length; i++)
        {
            cache.append("$").append(i).append(',');
            hasComma = true;
        }
        if (hasComma)
        {
            cache.setLength(cache.length() - 1);
        }
        cache.append("});\r\n");
        cache.append("}catch (NoSuchMethodException e){throw new RuntimeException(e);}\r\n");
        classModel.addImport(NoSuchMethodException.class);
        classModel.addImport(RuntimeException.class);
        classModel.addImport(Method.class);
        MethodModel.MethodModelKey key         = new MethodModel.MethodModelKey(method);
        MethodModel                methodModel = classModel.getMethodModel(key);
        methodModel.setBody(cache + methodModel.getBody());
    }

    /**
     * @param classModel
     * @return
     */
    private String generateValidatorField(ClassModel classModel)
    {
        String     fieldName  = "validator_" + FIELD_NAME_COUNTER.getAndIncrement();
        FieldModel fieldModel = new FieldModel(fieldName, JfireMethodValidator.class, classModel);
        classModel.addField(fieldModel);
        return fieldName;
    }

    /**
     * @param classModel
     * @param validateFieldName
     */
    private void generateSetJfireMethodValidatorMethod(ClassModel classModel, String validateFieldName, String methodMapField)
    {
        MethodModel methodModel = new MethodModel(classModel);
        methodModel.setAccessLevel(MethodModel.AccessLevel.PUBLIC);
        methodModel.setMethodName("setJfireMethodValidator");
        methodModel.setParamterTypes(JfireMethodValidator.class, Map.class);
        methodModel.setReturnType(void.class);
        methodModel.setBody(validateFieldName + " = $0;\r\n" + methodMapField + " = $1;\r\n");
        classModel.putMethodModel(methodModel);
    }

    @Override
    public int order()
    {
        return VALIDATE;
    }

    public interface JfireMethodValidator
    {
        <T> void validateParameters(T object, Method method, Object[] parameterValues, Class<?>... groups);

        <T> void validateReturnValue(T object, Method method, Object returnValue, Class<?>... groups);
    }

    public interface SetJfireMethodValidator
    {
        void setJfireMethodValidator(JfireMethodValidator validator, Map<String, Method> methodMap);
    }
}

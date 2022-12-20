package com.jfirer.jfire.core.aop.impl;

import com.jfirer.baseutil.StringUtil;
import com.jfirer.baseutil.TRACEID;
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
import com.jfirer.jfire.core.aop.ProceedPoint;
import com.jfirer.jfire.core.aop.notated.*;
import com.jfirer.jfire.core.aop.notated.support.MatchTargetMethod;
import com.jfirer.jfire.core.aop.notated.support.impl.NameMatch;
import com.jfirer.jfire.core.bean.BeanRegisterInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AopEnhanceManager implements EnhanceManager
{
    private static final Logger logger = LoggerFactory.getLogger(AopEnhanceManager.class);
    AnnotationContextFactory annotationContextFactory = DefaultApplicationContext.ANNOTATION_CONTEXT_FACTORY;

    @Override
    public Predicate<BeanRegisterInfo> needEnhance(ApplicationContext context)
    {
        return beanRegisterInfo -> context.getAllBeanRegisterInfos().stream()//
                                          .filter(other -> annotationContextFactory.get(other.getType()).isAnnotationPresent(EnhanceClass.class))//
                                          .map(other -> annotationContextFactory.get(other.getType()).getAnnotation(EnhanceClass.class).value())//
                                          .filter(rule -> StringUtil.match(beanRegisterInfo.getType().getName(), rule))//
                                          .findAny().isPresent();
    }

    @Override
    public void enhance(ClassModel classModel, final Class<?> originType, ApplicationContext applicationContext, String hostFieldName)
    {
        AnnotationContextFactory annotationContextFactory = DefaultApplicationContext.ANNOTATION_CONTEXT_FACTORY;
        MethodModel              setEnhanceFieldsMethod   = getSetEnhanceFieldsMethod(classModel);
        List<BeanRegisterInfo>   list                     = findAspectClass(originType, applicationContext);
        for (BeanRegisterInfo each : list)
        {
            String fieldName = "enhance_" + FIELD_NAME_COUNTER.getAndIncrement();
            addFiledAndBuildSet(classModel, setEnhanceFieldsMethod, each, fieldName);
            for (Method enhanceMethod : each.getType().getMethods())
            {
                AnnotationContext annotationContextOnEnhanceMethod = annotationContextFactory.get(enhanceMethod);
                if (annotationContextOnEnhanceMethod.isAnnotationPresent(Before.class))
                {
                    processBeforeAdvice(classModel, originType, annotationContextOnEnhanceMethod, hostFieldName, fieldName, enhanceMethod);
                }
                else if (annotationContextOnEnhanceMethod.isAnnotationPresent(After.class))
                {
                    processAfterAdvice(classModel, originType, annotationContextOnEnhanceMethod, hostFieldName, fieldName, enhanceMethod);
                }
                else if (annotationContextOnEnhanceMethod.isAnnotationPresent(AfterReturning.class))
                {
                    processAfterReturningAdvice(classModel, originType, annotationContextOnEnhanceMethod, hostFieldName, fieldName, enhanceMethod);
                }
                else if (annotationContextOnEnhanceMethod.isAnnotationPresent(AfterThrowable.class))
                {
                    processAfterThrowableAdvice(classModel, originType, annotationContextOnEnhanceMethod, hostFieldName, fieldName, enhanceMethod);
                }
                else if (annotationContextOnEnhanceMethod.isAnnotationPresent(Around.class))
                {
                    processAroundAdvice(classModel, originType, annotationContextOnEnhanceMethod, hostFieldName, fieldName, enhanceMethod);
                }
            }
        }
    }

    private void addFiledAndBuildSet(ClassModel classModel, MethodModel setEnhanceFieldsMethod, BeanRegisterInfo each, String fieldName)
    {
        FieldModel fieldModel = new FieldModel(fieldName, each.getType(), classModel);
        classModel.addField(fieldModel);
        String setEnhanceFieldsMethodBody = setEnhanceFieldsMethod.getBody();
        setEnhanceFieldsMethodBody += fieldName + "=(" + SmcHelper.getReferenceName(each.getType(), classModel) + ")$0.getBean(\"" + each.getBeanName() + "\");\r\n";
        setEnhanceFieldsMethod.setBody(setEnhanceFieldsMethodBody);
    }

    private MethodModel getSetEnhanceFieldsMethod(ClassModel classModel)
    {
        MethodModel.MethodModelKey key = new MethodModel.MethodModelKey();
        key.setAccessLevel(MethodModel.AccessLevel.PUBLIC);
        key.setMethodName("setEnhanceFields");
        key.setParamterTypes(new Class[]{ApplicationContext.class});
        MethodModel setEnhanceFieldsMethod = classModel.getMethodModel(key);
        return setEnhanceFieldsMethod;
    }

    private void processBeforeAdvice(ClassModel classModel, Class<?> originType, AnnotationContext annotationContextOnEnhanceMethod, String hostFieldName, String fieldName, Method enhanceMethod)
    {
        String            traceId           = TRACEID.currentTraceId();
        Before            before            = annotationContextOnEnhanceMethod.getAnnotation(Before.class);
        MatchTargetMethod matchTargetMethod = getMatchTargetMethod(enhanceMethod, before.value(), before.custom());
        for (Method method : originType.getMethods())
        {
            if (matchTargetMethod.match(method))
            {
                logger.debug("traceId:{} 前置通知规则匹配成功，方法:{},通知方法:{}", traceId, method.getDeclaringClass().getSimpleName() + "." + method.getName(), enhanceMethod.getDeclaringClass().getSimpleName() + "." + enhanceMethod.getName());
                MethodModel.MethodModelKey key         = new MethodModel.MethodModelKey(method);
                MethodModel                methodModel = classModel.getMethodModel(key);
                String                     originBody  = methodModel.getBody();
                String                     pointName   = "point_" + FIELD_NAME_COUNTER.getAndIncrement();
                StringBuilder              cache       = new StringBuilder();
                if (enhanceMethod.getParameterTypes().length == 0)
                {
                    cache.append(fieldName).append(".").append(enhanceMethod.getName()).append("();\r\n");
                }
                else
                {
                    generateProceedPointImpl(classModel, hostFieldName, method, pointName, cache, false);
                    generateEnhanceMethodInvoke(fieldName, enhanceMethod, pointName, cache);
                }
                cache.append(originBody);
                methodModel.setBody(cache.toString());
            }
        }
    }

    private static MatchTargetMethod getMatchTargetMethod(Method enhanceMethod, String value, Class<? extends MatchTargetMethod> ckass)
    {
        if (value.equals("") && ckass.equals(MatchTargetMethod.class))
        {
            throw new IllegalArgumentException("在方法：" + enhanceMethod + "上的增强注解，value属性和custom属性必须有一个有值");
        }
        MatchTargetMethod matchTargetMethod;
        if (value.isBlank())
        {
            try
            {
                matchTargetMethod = ckass.getDeclaredConstructor().newInstance();
            }
            catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                   NoSuchMethodException e)
            {
                throw new RuntimeException(e);
            }
        }
        else
        {
            matchTargetMethod = new NameMatch(value);
        }
        return matchTargetMethod;
    }

    private String getRule(AnnotationContext annotationContext, Method enhanceMethod, Class<? extends Annotation> type)
    {
        return annotationContext.getAnnotationMetadata(type).getAttribyte("value").getStringValue();
    }

    private void generateProceedPointImplWithInvokeinternal(ClassModel classModel, String hostFieldName, Method method, String pointName, StringBuilder cache, String origin)
    {
        cache.append("ProceedPointImpl ").append(pointName).append(" = new ProceedPointImpl(){\r\n");
        if (method.getReturnType() == void.class)
        {
            cache.append("public Object invokeInternel(){ ").append(origin).append(" return null;\r\n}\r\n};\r\n");
        }
        else
        {
            cache.append("public Object invokeInternel(){ ").append(origin).append(" }\r\n};\r\n");
        }
        generateProceedPointImpl(classModel, hostFieldName, method, pointName, cache, true);
    }

    private void generateProceedPointImpl(ClassModel classModel, String hostFieldName, Method method, String pointName, StringBuilder cache, boolean alreadyCreate)
    {
        if (!alreadyCreate)
        {
            cache.append("ProceedPointImpl ").append(pointName).append(" = new ProceedPointImpl();\r\n");
        }
        cache.append(pointName).append(".setHost(").append(hostFieldName).append(");\r\n");
        String _MethodDescriptionName = "_MethodDescription_" + FIELD_NAME_COUNTER.getAndIncrement();
        classModel.addImport(ProceedPoint.MethodDescription.class);
        cache.append("MethodDescription " + _MethodDescriptionName + " = new MethodDescription(\"").append(method.getName()).append("\",new Class[]{");
        if (method.getParameterTypes().length != 0)
        {
            for (Class<?> each : method.getParameterTypes())
            {
                cache.append(SmcHelper.getReferenceName(each, classModel)).append(".class,");
            }
            cache.setLength(cache.length() - 1);
        }
        cache.append("});\r\n");
        cache.append(pointName).append(".setMethodDescription(" + _MethodDescriptionName + ");\r\n");
        if (method.getParameterTypes().length != 0)
        {
            cache.append(pointName).append(".setParams(new Object[]{");
            int length = method.getParameterTypes().length;
            for (int i = 0; i < length; i++)
            {
                cache.append("$").append(i).append(',');
            }
            cache.setLength(cache.length() - 1);
            cache.append("});\r\n");
        }
    }

    private void processAfterAdvice(ClassModel classModel, Class<?> originType, AnnotationContext annotationContextOnEnhanceMethod, String hostFieldName, String fieldName, Method enhanceMethod)
    {
        String            traceId           = TRACEID.currentTraceId();
        After             after             = annotationContextOnEnhanceMethod.getAnnotation(After.class);
        MatchTargetMethod matchTargetMethod = getMatchTargetMethod(enhanceMethod, after.value(), after.custom());
        for (Method method : originType.getMethods())
        {
            if (matchTargetMethod.match(method))
            {
                logger.debug("traceId:{} 后置通知规则匹配成功，方法:{},通知方法:{}", traceId, method.getDeclaringClass().getSimpleName() + "." + method.getName(), enhanceMethod.getDeclaringClass().getSimpleName() + "." + enhanceMethod.getName());
                MethodModel.MethodModelKey key         = new MethodModel.MethodModelKey(method);
                MethodModel                methodModel = classModel.getMethodModel(key);
                String                     originBody  = methodModel.getBody();
                String                     pointName   = "point_" + FIELD_NAME_COUNTER.getAndIncrement();
                StringBuilder              cache       = new StringBuilder();
                cache.append("try{\r\n").append(originBody).append("}\r\n").append("finally\r\n{\r\n");
                if (enhanceMethod.getParameterTypes().length == 0)
                {
                    cache.append(fieldName).append(".").append(enhanceMethod.getName()).append("();\r\n");
                }
                else
                {
                    generateProceedPointImpl(classModel, hostFieldName, method, pointName, cache, false);
                    generateEnhanceMethodInvoke(fieldName, enhanceMethod, pointName, cache);
                }
                cache.append("}");
                methodModel.setBody(cache.toString());
            }
        }
    }

    private void generateEnhanceMethodInvoke(String fieldName, Method enhanceMethod, String pointName, StringBuilder cache)
    {
        cache.append(fieldName).append(".").append(enhanceMethod.getName()).append("(").append(pointName).append(");\r\n");
    }

    private void processAfterReturningAdvice(ClassModel classModel, Class<?> originType, AnnotationContext annotationContextOnEnhanceMethod, String hostFieldName, String fieldName, Method enhanceMethod)
    {
        String            traceId           = TRACEID.currentTraceId();
        AfterReturning    afterReturning    = annotationContextOnEnhanceMethod.getAnnotation(AfterReturning.class);
        MatchTargetMethod matchTargetMethod = getMatchTargetMethod(enhanceMethod, afterReturning.value(), afterReturning.custom());
        for (Method method : originType.getMethods())
        {
            if (method.getReturnType() != void.class && matchTargetMethod.match(method))
            {
                logger.debug("traceId:{} 返回通知规则匹配成功，方法:{},通知方法:{}", traceId, method.getDeclaringClass().getSimpleName() + "." + method.getName(), enhanceMethod.getDeclaringClass().getSimpleName() + "." + enhanceMethod.getName());
                MethodModel.MethodModelKey key    = new MethodModel.MethodModelKey(method);
                MethodModel                origin = classModel.removeMethodModel(key);
                MethodModel                newOne = new MethodModel(method, classModel);
                origin.setAccessLevel(MethodModel.AccessLevel.PRIVATE);
                origin.setMethodName(origin.getMethodName() + "_" + METHOD_NAME_COUNTER.getAndIncrement());
                classModel.putMethodModel(origin);
                StringBuilder cache = new StringBuilder();
                cache.append(SmcHelper.getReferenceName(method.getReturnType(), classModel)).append(" result = ").append(origin.generateInvoke()).append(";\r\n");
                String pointName = generatePointName();
                generateProceedPointImpl(classModel, hostFieldName, method, pointName, cache, false);
                cache.append(pointName).append(".setResult(result);\r\n");
                generateEnhanceMethodInvoke(fieldName, enhanceMethod, pointName, cache);
                cache.append("return result;\r\n");
                newOne.setBody(cache.toString());
                classModel.putMethodModel(newOne);
            }
        }
    }

    private String generatePointName()
    {
        return "point_" + FIELD_NAME_COUNTER.getAndIncrement();
    }

    private void processAfterThrowableAdvice(ClassModel classModel, Class<?> originType, AnnotationContext annotationContextOnEnhanceMethod, String hostFieldName, String fieldName, Method enhanceMethod)
    {
        String            traceId           = TRACEID.currentTraceId();
        AfterThrowable    afterThrowable    = annotationContextOnEnhanceMethod.getAnnotation(AfterThrowable.class);
        MatchTargetMethod matchTargetMethod = getMatchTargetMethod(enhanceMethod, afterThrowable.value(), afterThrowable.custom());
        classModel.addImport(ReflectUtil.class);
        for (Method method : originType.getMethods())
        {
            if (matchTargetMethod.match(method))
            {
                logger.debug("traceId:{} 规则匹配成功，方法:{},通知方法:{}", traceId, method.getDeclaringClass().getSimpleName() + "." + method.getName(), enhanceMethod.getDeclaringClass().getSimpleName() + "." + enhanceMethod.getName());
                MethodModel.MethodModelKey key         = new MethodModel.MethodModelKey(method);
                MethodModel                methodModel = classModel.getMethodModel(key);
                String                     body        = methodModel.getBody();
                StringBuilder              cache       = new StringBuilder();
                cache.append("try\r\n{\r\n").append(body).append("}\r\ncatch(java.lang.Throwable e)\r\n{");
                String pointName = generatePointName();
                generateProceedPointImpl(classModel, hostFieldName, method, pointName, cache, false);
                cache.append(pointName).append(".setE(e);\r\n");
                generateEnhanceMethodInvoke(fieldName, enhanceMethod, pointName, cache);
                cache.append("ReflectUtil.throwException(e);\r\n");
                if (method.getReturnType().isPrimitive())
                {
                    if (method.getReturnType() == boolean.class)
                    {
                        cache.append("return false;\r\n");
                    }
                    else if (method.getReturnType() != void.class)
                    {
                        cache.append("return 0;\r\n");
                    }
                }
                else
                {
                    cache.append("return null;\r\n");
                }
                cache.append("}\r\n");
                methodModel.setBody(cache.toString());
                classModel.putMethodModel(methodModel);
            }
        }
    }

    private void processAroundAdvice(ClassModel classModel, Class<?> originType, AnnotationContext annotationContextOnEnhanceMethod, String hostFieldName, String fieldName, Method enhanceMethod)
    {
        String            traceId           = TRACEID.currentTraceId();
        Around            around            = annotationContextOnEnhanceMethod.getAnnotation(Around.class);
        MatchTargetMethod matchTargetMethod = getMatchTargetMethod(enhanceMethod, around.value(), around.custom());
        for (Method method : originType.getMethods())
        {
            if (matchTargetMethod.match(method))
            {
                logger.debug("traceId:{} 环绕通知规则匹配成功，方法:{},通知方法:{}", traceId, method.getDeclaringClass().getSimpleName() + "." + method.getName(), enhanceMethod.getDeclaringClass().getSimpleName() + "." + enhanceMethod.getName());
                MethodModel.MethodModelKey key         = new MethodModel.MethodModelKey(method);
                MethodModel                methodModel = classModel.getMethodModel(key);
                boolean[]                  flags       = new boolean[methodModel.getParamterTypes().length];
                Arrays.fill(flags, true);
                methodModel.setParamterFinals(flags);
                String        body      = methodModel.getBody();
                String        pointName = generatePointName();
                StringBuilder cache     = new StringBuilder();
                generateProceedPointImplWithInvokeinternal(classModel, hostFieldName, method, pointName, cache, body);
                Class<?> returnType = method.getReturnType();
                generateEnhanceMethodInvoke(fieldName, enhanceMethod, pointName, cache);
                if (returnType == void.class)
                {
                }
                else if (returnType.isPrimitive())
                {
                    if (returnType == int.class)
                    {
                        classModel.addImport(Integer.class);
                        cache.append("return (Integer)").append(pointName).append(".getResult();\r\n");
                    }
                    else if (returnType == short.class)
                    {
                        classModel.addImport(Short.class);
                        cache.append("return (Short)").append(pointName).append(".getResult();\r\n");
                    }
                    else if (returnType == long.class)
                    {
                        classModel.addImport(Long.class);
                        cache.append("return (Long)").append(pointName).append(".getResult();\r\n");
                    }
                    else if (returnType == float.class)
                    {
                        classModel.addImport(Float.class);
                        cache.append("return (Float)").append(pointName).append(".getResult();\r\n");
                    }
                    else if (returnType == double.class)
                    {
                        classModel.addImport(Double.class);
                        cache.append("return (Double)").append(pointName).append(".getResult();\r\n");
                    }
                    else if (returnType == byte.class)
                    {
                        classModel.addImport(Byte.class);
                        cache.append("return (Byte)").append(pointName).append(".getResult();\r\n");
                    }
                    else if (returnType == char.class)
                    {
                        classModel.addImport(Character.class);
                        cache.append("return (Character)").append(pointName).append(".getResult();\r\n");
                    }
                    else if (returnType == boolean.class)
                    {
                        classModel.addImport(Boolean.class);
                        cache.append("return (Boolean)").append(pointName).append(".getResult();\r\n");
                    }
                    else
                    {
                        throw new UnsupportedOperationException();
                    }
                }
                else
                {
                    cache.append("return (").append(SmcHelper.getReferenceName(returnType, classModel)).append(")").append(pointName).append(".getResult();\r\n");
                }
                methodModel.setBody(cache.toString());
                classModel.putMethodModel(methodModel);
            }
        }
    }

    private List<BeanRegisterInfo> findAspectClass(Class<?> type, ApplicationContext applicationContext)
    {
        final AnnotationContextFactory annotationContextFactory = DefaultApplicationContext.ANNOTATION_CONTEXT_FACTORY;
        List<BeanRegisterInfo> enhanceBeanRegisterList = applicationContext.getAllBeanRegisterInfos().stream().filter(beanRegisterInfo -> annotationContextFactory.get(beanRegisterInfo.getType()).isAnnotationPresent(EnhanceClass.class)).filter(beanRegisterInfo -> {
            AnnotationContext annotationContext = annotationContextFactory.get(beanRegisterInfo.getType());
            String            rule              = annotationContext.getAnnotation(EnhanceClass.class).value();
            return StringUtil.match(type.getName(), rule);
        }).sorted(Comparator.comparingInt(beanRegisterInfo -> annotationContextFactory.get(beanRegisterInfo.getType()).getAnnotation(EnhanceClass.class).order())).collect(Collectors.toList());
        return enhanceBeanRegisterList;
    }

    @Override
    public int order()
    {
        return DEFAULT;
    }
}

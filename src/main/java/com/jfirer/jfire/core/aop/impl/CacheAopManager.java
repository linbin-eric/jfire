package com.jfirer.jfire.core.aop.impl;

import com.jfirer.baseutil.StringUtil;
import com.jfirer.baseutil.bytecode.support.AnnotationContext;
import com.jfirer.baseutil.bytecode.support.AnnotationContextFactory;
import com.jfirer.baseutil.bytecode.util.BytecodeUtil;
import com.jfirer.baseutil.smc.SmcHelper;
import com.jfirer.baseutil.smc.model.ClassModel;
import com.jfirer.baseutil.smc.model.FieldModel;
import com.jfirer.baseutil.smc.model.MethodModel;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.DefaultApplicationContext;
import com.jfirer.jfire.core.aop.EnhanceManager;
import com.jfirer.jfire.core.aop.notated.cache.CacheDelete;
import com.jfirer.jfire.core.aop.notated.cache.CacheGet;
import com.jfirer.jfire.core.aop.notated.cache.CachePut;
import com.jfirer.jfireel.expression.Expression;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CacheAopManager implements EnhanceManager
{
    private String cacheManagerBeanName;

    @Override
    public void scan(ApplicationContext context)
    {
        AnnotationContextFactory annotationContextFactory = DefaultApplicationContext.ANNOTATION_CONTEXT_FACTORY;
        interlScan(context,//
                   method ->//
                   {
                       AnnotationContext annotationContext = annotationContextFactory.get(method);
                       return annotationContext.isAnnotationPresent(CacheDelete.class) || annotationContext.isAnnotationPresent(CacheGet.class) || annotationContext.isAnnotationPresent(CachePut.class);
                   },//
                   beanRegisterInfo -> beanRegisterInfo.addEnhanceManager(CacheAopManager.this));
        cacheManagerBeanName = Optional.ofNullable(context.getBeanRegisterInfo(CacheManager.class)).map(beanRegisterInfo -> beanRegisterInfo.getBeanName()).orElse(null);
    }

    @Override
    public void enhance(ClassModel classModel, Class<?> type, ApplicationContext applicationContext, String hostFieldName)
    {
        classModel.addImport(Expression.class);
        classModel.addImport(HashMap.class);
        classModel.addImport(Map.class);
        classModel.addImport(String.class);
        classModel.addImport(Boolean.class);
        classModel.addImport(Cache.class);
        String                     cacheManagerFieldName = generateCacheManagerField(classModel);
        MethodModel.MethodModelKey key                   = new MethodModel.MethodModelKey();
        key.setAccessLevel(MethodModel.AccessLevel.PUBLIC);
        key.setMethodName("setEnhanceFields");
        key.setParamterTypes(new Class[]{ApplicationContext.class});
        MethodModel setEnhanceFieldsMethod     = classModel.getMethodModel(key);
        String      setEnhanceFieldsMethodBody = setEnhanceFieldsMethod.getBody();
        setEnhanceFieldsMethodBody += cacheManagerFieldName + "=(" + SmcHelper.getReferenceName(CacheManager.class, classModel) + ")$0.getBean(\"" + cacheManagerBeanName + "\");\r\n";
        setEnhanceFieldsMethod.setBody(setEnhanceFieldsMethodBody);
        AnnotationContextFactory annotationContextFactory = DefaultApplicationContext.ANNOTATION_CONTEXT_FACTORY;
        for (Method method : type.getMethods())
        {
            if (Modifier.isFinal(method.getModifiers()))
            {
                continue;
            }
            AnnotationContext annotationContext = annotationContextFactory.get(method);
            if (!method.getReturnType().isPrimitive() //
                && method.getReturnType() != Void.class //
                && annotationContext.isAnnotationPresent(CacheGet.class))
            {
                processCacheGet(classModel, cacheManagerFieldName, annotationContext, method);
            }
            else if (annotationContext.isAnnotationPresent(CacheDelete.class))
            {
                processCacheDelete(classModel, cacheManagerFieldName, annotationContext, method);
            }
            else if (!method.getReturnType().isPrimitive() //
                     && method.getReturnType() != Void.class //
                     && annotationContext.isAnnotationPresent(CachePut.class))
            {
                processCachePut(classModel, cacheManagerFieldName, annotationContext, method);
            }
        }
    }

    private void processCachePut(ClassModel classModel, String cacheManagerFieldName, AnnotationContext annotationContext, Method method)
    {
        CachePut cachePut = annotationContext.getAnnotation(CachePut.class);
        if (StringUtil.isNotBlank(cachePut.condition()))
        {
            String        lexerConditionFieldName = generateConditionField(classModel, cachePut.condition());
            String        lexerKeyFieldName       = generateKeyField(classModel, cachePut.value());
            MethodModel   origin                  = changeOriginMethodName(classModel, method);
            StringBuilder cache                   = new StringBuilder();
            boolean       hasParams               = method.getParameterTypes().length != 0;
            if (hasParams)
            {
                generateConditionMapDeclarationPart(method, cache);
            }
            generateConditionDeclarationPart(lexerConditionFieldName, cache, hasParams);
            cache.append("if(condition==false)\r\n{\r\n");
            cache.append("return ").append(origin.generateInvoke()).append(";\r\n");
            cache.append("}\r\n");
            cache.append("else\r\n{\r\n");
            cache.append(SmcHelper.getReferenceName(method.getReturnType(), classModel)).append(" result = ").append(origin.generateInvoke()).append(";\r\n");
            generateKeyNameDeclarationPart(cache, lexerKeyFieldName, hasParams);
            generateCacheDeclarationPart(cacheManagerFieldName, lexerKeyFieldName, cache);
            cache.append("cache.put(name,result,").append(cachePut.timeToLive()).append(");\r\n");
            cache.append("return result;\r\n");
            cache.append("}\r\n");
            generateMethod(classModel, method, cache.toString());
        }
        else
        {
            String        lexerKeyFieldName = generateKeyField(classModel, cachePut.value());
            MethodModel   origin            = changeOriginMethodName(classModel, method);
            StringBuilder cache             = new StringBuilder();
            boolean       hasParams         = method.getParameterTypes().length != 0;
            if (hasParams)
            {
                generateConditionMapDeclarationPart(method, cache);
            }
            cache.append(SmcHelper.getReferenceName(method.getReturnType(), classModel)).append(" result = ").append(origin.generateInvoke()).append(";\r\n");
            generateCacheDeclarationPart(cacheManagerFieldName, lexerKeyFieldName, cache);
            generateKeyNameDeclarationPart(cache, lexerKeyFieldName, hasParams);
            cache.append("cache.put(name,result,").append(cachePut.timeToLive()).append(");\r\n");
            cache.append("return result;\r\n");
            generateMethod(classModel, method, cache.toString());
        }
    }

    private void processCacheDelete(ClassModel classModel, String cacheManagerFieldName, AnnotationContext annotationContext, Method method)
    {
        CacheDelete cacheDelete = annotationContext.getAnnotation(CacheDelete.class);
        if (StringUtil.isNotBlank(cacheDelete.condition()))
        {
            String                     lexerConditionFieldName = generateConditionField(classModel, cacheDelete.condition());
            String                     lexerKeyFieldName       = generateKeyField(classModel, cacheDelete.value());
            MethodModel.MethodModelKey key                     = new MethodModel.MethodModelKey(method);
            MethodModel                methodModel             = classModel.getMethodModel(key);
            StringBuilder              cache                   = new StringBuilder();
            boolean                    hasParams               = method.getParameterTypes().length != 0;
            if (hasParams)
            {
                generateConditionMapDeclarationPart(method, cache);
            }
            generateConditionDeclarationPart(lexerConditionFieldName, cache, hasParams);
            cache.append("if(condition)\r\n{\r\n");
            generateCacheDeclarationPart(cacheManagerFieldName, cacheDelete.cacheName(), cache);
            generateKeyNameDeclarationPart(cache, lexerKeyFieldName, hasParams);
            cache.append("cache.remove(name);\r\n");
            cache.append("}\r\n");
            methodModel.setBody(cache + methodModel.getBody());
        }
        else
        {
            String                     lexerKeyFieldName = generateKeyField(classModel, cacheDelete.value());
            MethodModel.MethodModelKey key               = new MethodModel.MethodModelKey(method);
            MethodModel                methodModel       = classModel.getMethodModel(key);
            StringBuilder              cache             = new StringBuilder();
            boolean                    hasParams         = method.getParameterTypes().length != 0;
            if (hasParams)
            {
                generateConditionMapDeclarationPart(method, cache);
            }
            generateCacheDeclarationPart(cacheManagerFieldName, cacheDelete.cacheName(), cache);
            generateKeyNameDeclarationPart(cache, lexerKeyFieldName, hasParams);
            cache.append("cache.remove(name);\r\n");
            methodModel.setBody(cache + methodModel.getBody());
        }
    }

    private void processCacheGet(ClassModel classModel, String cacheManagerFieldName, AnnotationContext annotationContext, Method method)
    {
        CacheGet cacheGet = annotationContext.getAnnotation(CacheGet.class);
        if (StringUtil.isNotBlank(cacheGet.condition()))
        {
            String        lexerConditionFieldName = generateConditionField(classModel, cacheGet.condition());
            String        lexerKeyFieldName       = generateKeyField(classModel, cacheGet.value());
            MethodModel   origin                  = changeOriginMethodName(classModel, method);
            StringBuilder cache                   = new StringBuilder();
            genetateCacheGetBodyWithCondition(cacheManagerFieldName, method, cacheGet, lexerConditionFieldName, lexerKeyFieldName, origin, cache, method.getParameterTypes().length != 0, classModel);
            generateMethod(classModel, method, cache.toString());
        }
        else
        {
            String        keyFieldName = generateKeyField(classModel, cacheGet.value());
            MethodModel   origin       = changeOriginMethodName(classModel, method);
            StringBuilder cache        = new StringBuilder();
            boolean       hasParams    = method.getParameterTypes().length != 0;
            if (hasParams)
            {
                generateConditionMapDeclarationPart(method, cache);
            }
            generateGetValuePart(method, cacheGet, origin, cache, cacheManagerFieldName, keyFieldName, hasParams, classModel);
            generateMethod(classModel, method, cache.toString());
        }
    }

    /**
     * 创建声明condition的部分代码。条件的变量为condition
     *
     * @param lexerConditionFieldName
     * @param cache
     * @param hasParams
     */
    private void generateConditionDeclarationPart(String lexerConditionFieldName, StringBuilder cache, boolean hasParams)
    {
        if (hasParams)
        {
            cache.append("Boolean condition = (Boolean)").append(lexerConditionFieldName).append(".calculate(conditionParams);\r\n");
        }
        else
        {
            cache.append("Boolean condition = (Boolean)").append(lexerConditionFieldName).append(".calculate();\r\n");
        }
    }

    private void generateMethod(ClassModel classModel, Method method, String body)
    {
        MethodModel methodModel = new MethodModel(method, classModel);
        methodModel.setBody(body);
        classModel.putMethodModel(methodModel);
    }

    /**
     * 创建了声明了Cache的代码。Cache的变量名为cache
     *
     * @param cacheManagerFieldName
     * @param cacheName
     * @param cache
     */
    private void generateCacheDeclarationPart(String cacheManagerFieldName, String cacheName, StringBuilder cache)
    {
        cache.append("Cache cache = ").append(cacheManagerFieldName).append(".get(\"").append(cacheName).append("\");\r\n");
    }

    private void generateGetValuePart(Method method, CacheGet cacheGet, MethodModel origin, StringBuilder cache, String cacheManagerFieldName, String keyFieldName, boolean hasParams, ClassModel classModel)
    {
        generateCacheDeclarationPart(cacheManagerFieldName, cacheGet.cacheName(), cache);
        generateKeyNameDeclarationPart(cache, keyFieldName, hasParams);
        cache.append(SmcHelper.getReferenceName(method.getReturnType(), classModel)).append(" result = (").append(SmcHelper.getReferenceName(method.getReturnType(), classModel)).append(")cache.get(name);\r\n");
        cache.append("if(result!=null)\r\n{");
        cache.append("return result;\r\n}\r\n");
        cache.append("else\r\n{");
        cache.append("result = (").append(SmcHelper.getReferenceName(method.getReturnType(), classModel)).append(")").append(origin.generateInvoke()).append(";\r\n");
        cache.append("cache.put(name,result,").append(cacheGet.timeToLive()).append(");\r\n");
        cache.append("return result;\r\n}\r\n");
    }

    /**
     * 创建声明了cache中key的部分代码。key的变量名为name
     *
     * @param cache
     * @param keyFieldName
     * @param hasParams
     */
    private void generateKeyNameDeclarationPart(StringBuilder cache, String keyFieldName, boolean hasParams)
    {
        if (hasParams)
        {
            cache.append("String name = (String)").append(keyFieldName).append(".calculate(conditionParams);\r\n");
        }
        else
        {
            cache.append("String name = (String)").append(keyFieldName).append(".calculate();\r\n");
        }
    }

    private void generateConditionMapDeclarationPart(Method method, StringBuilder cache)
    {
        String[] methodParamNames = BytecodeUtil.parseMethodParamNames(method);
        if (methodParamNames == null)
        {
            throw new NullPointerException("无法获取方法" + method + "的入参名称");
        }
        cache.append("Map<String,Object> conditionParams = new HashMap();\r\n");
        for (int i = 0; i < methodParamNames.length; i++)
        {
            cache.append("conditionParams.put(\"").append(methodParamNames[i]).append("\",$").append(i).append(");\r\n");
        }
    }

    private MethodModel changeOriginMethodName(ClassModel classModel, Method method)
    {
        MethodModel.MethodModelKey key    = new MethodModel.MethodModelKey(method);
        MethodModel                origin = classModel.removeMethodModel(key);
        origin.setAccessLevel(MethodModel.AccessLevel.PRIVATE);
        origin.setMethodName(origin.getMethodName() + "_" + METHOD_NAME_COUNTER.getAndIncrement());
        classModel.putMethodModel(origin);
        return origin;
    }

    private String generateKeyField(ClassModel classModel, String key)
    {
        String     lexerKeyFieldName = "expression_" + FIELD_NAME_COUNTER.getAndIncrement();
        FieldModel keyField          = new FieldModel(lexerKeyFieldName, Expression.class, "Expression.parse(\"" + key + "\")", classModel);
        classModel.addField(keyField);
        return lexerKeyFieldName;
    }

    private String generateConditionField(ClassModel classModel, String condition)
    {
        String     lexerConditionFieldName = "expression_" + FIELD_NAME_COUNTER.getAndIncrement();
        FieldModel conditionField          = new FieldModel(lexerConditionFieldName, Expression.class, "Expression.parse(\"" + condition + "\")", classModel);
        classModel.addField(conditionField);
        return lexerConditionFieldName;
    }

    private void genetateCacheGetBodyWithCondition(String cacheManagerFieldName, Method method, CacheGet cacheGet, String lexerConditionFieldName, String lexerKeyFieldName, MethodModel origin, StringBuilder cache, boolean hasParams, ClassModel classModel)
    {
        if (hasParams)
        {
            generateConditionMapDeclarationPart(method, cache);
        }
        generateConditionDeclarationPart(lexerConditionFieldName, cache, hasParams);
        cache.append("if(condition == false)\r\n{\r\n");
        cache.append("return ").append(origin.generateInvoke()).append(";\r\n}\r\n");
        cache.append("else\r\n{\r\n");
        generateGetValuePart(method, cacheGet, origin, cache, cacheManagerFieldName, lexerKeyFieldName, hasParams, classModel);
        cache.append("}\r\n");
    }

    private String generateCacheManagerField(ClassModel classModel)
    {
        String     cacheManagerFieldName = "cacheManager_" + FIELD_NAME_COUNTER.getAndIncrement();
        FieldModel fieldModel            = new FieldModel(cacheManagerFieldName, CacheManager.class, classModel);
        classModel.addField(fieldModel);
        return cacheManagerFieldName;
    }

    @Override
    public int order()
    {
        return CACHE;
    }

    public interface Cache
    {
        /**
         * 在缓存放入一个数据。
         *
         * @param key
         * @param value
         * @param timeToLive 数据过期的秒数。为-1时意味着永不过期
         */
        void put(String key, Object value, int timeToLive);

        Object get(String key);

        void remove(String key);

        void clear();
    }

    public interface CacheManager
    {
        // 如果没有找到对应名称的缓存，抛出空指针异常
        Cache get(String name);
    }

    public interface SetCacheManager
    {
        void setCacheManager(CacheManager cacheManager);
    }
}

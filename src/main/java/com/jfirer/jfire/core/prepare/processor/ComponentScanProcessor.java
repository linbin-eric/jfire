package com.jfirer.jfire.core.prepare.processor;

import com.jfirer.baseutil.PackageScan;
import com.jfirer.baseutil.TRACEID;
import com.jfirer.baseutil.bytecode.ClassFileParser;
import com.jfirer.baseutil.bytecode.support.AnnotationContext;
import com.jfirer.baseutil.bytecode.util.BytecodeUtil;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.prepare.ContextPrepare;
import com.jfirer.jfire.core.prepare.annotation.ComponentScan;
import com.jfirer.jfire.util.PrepareConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 负责在路径之下扫描具备@Resource的类
 */
public class ComponentScanProcessor implements ContextPrepare
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentScanProcessor.class);

    @Override
    public ApplicationContext.FoundNewContextPrepare prepare(ApplicationContext context)
    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        long count = context.getAllBeanRegisterInfos().stream()//
                            .filter(beanRegisterInfo -> AnnotationContext.isAnnotationPresent(ComponentScan.class, beanRegisterInfo.getType()))//
                            .map(beanRegisterInfo -> AnnotationContext.getAnnotation(ComponentScan.class, beanRegisterInfo.getType()))//
                            .flatMap(componentScan -> Arrays.stream(componentScan.value()))//
                            .flatMap(scanPath -> Arrays.stream(PackageScan.scan(scanPath)))//
                            .map(className -> className.replace('.', '/'))//
                            .filter(resourceName -> new ClassFileParser(BytecodeUtil.loadBytecode(classLoader, resourceName)).parse().isAnnotation() == false)//
                            .filter(resourceName -> AnnotationContext.get(resourceName).isAnnotationPresent(Resource.class))//
                            .map(resourceName -> resourceName.replace('/', '.'))//
                            .collect(Collectors.toSet()).stream()//下面的代码会修改这个beanRegisternfos这个集合，因此如果直接跟上stream操作会导致并发异常。所以这里需要先将原来的内容收集为一个新的集合再重新创建流
                            .map(className -> {
                                try
                                {
                                    Class<?> ckass = classLoader.loadClass(className);
                                    LOGGER.debug("扫描发现类:{}", className);
                                    return context.register(ckass);
                                }
                                catch (ClassNotFoundException e)
                                {
                                    throw new RuntimeException(e);
                                }
                            })//
                            .filter(registerResult -> registerResult == ApplicationContext.RegisterResult.PREPARE).count();
        return count > 0 ? ApplicationContext.FoundNewContextPrepare.YES : ApplicationContext.FoundNewContextPrepare.NO;
    }

    @Override
    public int order()
    {
        return PrepareConstant.DEFAULT_ORDER;
    }
}

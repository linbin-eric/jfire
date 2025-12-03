package cc.jfire.jfire.core.prepare.processor;

import cc.jfire.baseutil.PackageScan;
import cc.jfire.baseutil.Resource;
import cc.jfire.baseutil.bytecode.ClassFileParser;
import cc.jfire.baseutil.bytecode.support.AnnotationContext;
import cc.jfire.baseutil.bytecode.util.BytecodeUtil;
import cc.jfire.jfire.core.ApplicationContext;
import cc.jfire.jfire.core.prepare.ContextPrepare;
import cc.jfire.jfire.core.prepare.annotation.ComponentScan;
import cc.jfire.jfire.core.prepare.annotation.Import;
import cc.jfire.jfire.util.PrepareConstant;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 负责在路径之下扫描具备@Resource的类
 */
@Slf4j
public class ComponentScanProcessor implements ContextPrepare
{
    @Override
    public ApplicationContext.FoundNewContextPrepare prepare(ApplicationContext context)
    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        long count = context.getAllBeanRegisterInfos().stream()//
                            .filter(beanRegisterInfo -> AnnotationContext.isAnnotationPresent(ComponentScan.class, beanRegisterInfo.getType()))//
                            .flatMap(beanRegisterInfo -> {
                                ComponentScan componentScan = AnnotationContext.getAnnotation(ComponentScan.class, beanRegisterInfo.getType());
                                if (componentScan.value().length == 0)
                                {
                                    return Arrays.stream(new String[]{beanRegisterInfo.getType().getPackage().getName()});
                                }
                                else
                                {
                                    return Arrays.stream(componentScan.value());
                                }
                            })//
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
                                    log.debug("扫描发现类:{}", className);
                                    if (AnnotationContext.isAnnotationPresent(Import.class, ckass))
                                    {
                                        List<Import>   imports      = AnnotationContext.getAnnotations(Import.class, ckass);
                                        List<Class<?>> list         = imports.stream().map(Import::value).flatMap(values -> Arrays.stream(values)).toList();
                                        boolean        existPrepare = false;
                                        for (Class<?> aClass : list)
                                        {
                                            if (context.register(aClass) == ApplicationContext.RegisterResult.PREPARE)
                                            {
                                                existPrepare = true;
                                            }
                                        }
                                        if (context.register(ckass) == ApplicationContext.RegisterResult.PREPARE)
                                        {
                                            existPrepare = true;
                                        }
                                        if (existPrepare)
                                        {
                                            return ApplicationContext.RegisterResult.PREPARE;
                                        }
                                        else
                                        {
                                            /**
                                             * 因为这个集合最终是统计PREPARE结果的个数，因此其他的结果返回并不需要实际区分，返回什么都可以。
                                             */
                                            return ApplicationContext.RegisterResult.BEAN;
                                        }
                                    }
                                    else
                                    {
                                        return context.register(ckass);
                                    }
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

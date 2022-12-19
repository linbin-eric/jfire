package com.jfirer.jfire.core.prepare.processor;

import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.jfire.core.ApplicationContext;
import com.jfirer.jfire.core.prepare.ContextPrepare;
import com.jfirer.jfire.util.PrepareConstant;

import java.io.File;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class EnableAutoConfigurationProcessor implements ContextPrepare
{
    private static final String DIRECTORY_NAME = "META-INF/autoconfig/";
    private static final int    offset         = DIRECTORY_NAME.length();

    @Override
    public ApplicationContext.FoundNewContextPrepare prepare(ApplicationContext context)
    {
        try
        {
            ClassLoader        classLoader       = Thread.currentThread().getContextClassLoader();
            Enumeration<URL>   resources         = classLoader.getResources(DIRECTORY_NAME);
            Collection<String> starterClassNames = new HashSet<>();
            while (resources.hasMoreElements())
            {
                URL url = resources.nextElement();
                if (url.getProtocol().equals("jar"))
                {
                    JarURLConnection      openConnection = (JarURLConnection) url.openConnection();
                    JarFile               jarFile        = openConnection.getJarFile();
                    Enumeration<JarEntry> entries        = jarFile.entries();
                    while (entries.hasMoreElements())
                    {
                        JarEntry nextElement = entries.nextElement();
                        if (nextElement.getName().startsWith(DIRECTORY_NAME) && !nextElement.isDirectory())
                        {
                            starterClassNames.add(nextElement.getName().substring(offset));
                        }
                    }
                }
                else if (url.getProtocol().equals("file"))
                {
                    File file = new File(url.toURI());
                    if (file.isDirectory())
                    {
                        for (File each : file.listFiles())
                        {
                            if (!each.isDirectory())
                            {
                                starterClassNames.add(each.getName());
                            }
                        }
                    }
                }
            }
            long count = starterClassNames.stream().map(name -> {
                try
                {
                    return context.register(classLoader.loadClass(name));
                }
                catch (ClassNotFoundException e)
                {
                    throw new RuntimeException(e);
                }
            }).filter(registerResult -> registerResult == ApplicationContext.RegisterResult.PREPARE).count();
            return count > 0 ? ApplicationContext.FoundNewContextPrepare.YES : ApplicationContext.FoundNewContextPrepare.NO;
        }
        catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return ApplicationContext.FoundNewContextPrepare.NO;
        }
    }

    @Override
    public int order()
    {
        return PrepareConstant.ENABLE_AUTO_CONFIGURATION;
    }
}

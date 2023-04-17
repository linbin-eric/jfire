package com.jfirer.jfire.util;

import com.jfirer.baseutil.IniReader;
import com.jfirer.baseutil.SimpleYamlReader;
import com.jfirer.baseutil.StringUtil;
import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.jfire.core.ApplicationContext;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Utils
{
    public static BiConsumer<String, ApplicationContext> readPropertyFile(Class<?> rootClass)
    {
        return (String path, ApplicationContext context) -> {
            if (path.endsWith("ini") || path.endsWith("properties"))
            {
                IniReader.IniFile iniFile = Utils.readIniFile(path, rootClass);
                for (String property : iniFile.keySet())
                {
                    context.getEnv().putProperty(property, iniFile.getValue(property));
                }
            }
            else if (path.endsWith("yml") || path.endsWith("yaml"))
            {
                Map<String, Object> ymlFile = Utils.readYmlFile(path, rootClass);
                ymlFile.forEach((name, value) -> {
                    if (value instanceof String s)
                    {
                        context.getEnv().putProperty(name, s);
                    }
                    else if (value instanceof List<?> list)
                    {
                        context.getEnv().putProperty(name, list.stream().map(v -> (String) v).collect(Collectors.joining(",")));
                    }
                });
            }
        };
    }

    public static Map<String, Object> readYmlFile(String path, Class<?> rootClass)
    {
        return processPath(path, inputStream -> {
            try
            {
                return SimpleYamlReader.read(inputStream);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }, rootClass);
    }

    public static IniReader.IniFile readIniFile(String path, Class<?> rootClass)
    {
        return processPath(path, inputStream -> IniReader.read(inputStream, StandardCharsets.UTF_8), rootClass);
    }

    private static <R> R processPath(String path, Function<InputStream, R> function, Class<?> rootClass)
    {
        if (path.startsWith("classpath:"))
        {
            path = path.substring(10);
            if (Utils.class.getClassLoader().getResource(path) == null)
            {
                throw new NullPointerException(StringUtil.format("资源:{}不存在", path));
            }
            try (InputStream inputStream = Utils.class.getClassLoader().getResourceAsStream(path))
            {
                return function.apply(inputStream);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        else if (path.startsWith("file:"))
        {
            path = path.substring(5);
            File dirPath = new File(rootClass.getProtectionDomain().getCodeSource().getLocation().getPath());
            File pathFile;
            if (dirPath.isFile())
            {
                pathFile = new File(dirPath.getParentFile().getAbsolutePath() + File.separator + path);
            }
            else
            {
                pathFile = new File(dirPath + File.separator + path);
            }
            if (pathFile.exists() == false)
            {
                pathFile = new File(path);
                if (pathFile.exists() == false)
                {
                    throw new NullPointerException(StringUtil.format("资源:{}不存在", path));
                }
            }
            try (InputStream inputStream = new FileInputStream(pathFile))
            {
                return function.apply(inputStream);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        else
        {
            throw new UnsupportedOperationException("不支持的资源识别前缀:" + path);
        }
    }
}

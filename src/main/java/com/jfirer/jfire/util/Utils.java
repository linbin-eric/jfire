package com.jfirer.jfire.util;

import com.jfirer.baseutil.IniReader;
import com.jfirer.baseutil.SimpleYamlReader;
import com.jfirer.baseutil.StringUtil;
import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.jfire.core.ApplicationContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Utils
{
    public static BiConsumer<String, ApplicationContext> readPropertyFile()
    {
        return (String path, ApplicationContext context) -> {
            if (path.endsWith("ini")|| path.endsWith("properties"))
            {
                IniReader.IniFile iniFile = Utils.readIniFile(path);
                for (String property : iniFile.keySet())
                {
                    context.getEnv().putProperty(property, iniFile.getValue(property));
                }
            }
            else if (path.endsWith("yml") || path.endsWith("yaml"))
            {
                Map<String, Object> ymlFile = Utils.readYmlFile(path);
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

    public static Map<String, Object> readYmlFile(String path)
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
        });
    }

    public static IniReader.IniFile readIniFile(String path)
    {
        return processPath(path, inputStream -> IniReader.read(inputStream, StandardCharsets.UTF_8));
    }

    private static <R> R processPath(String path, Function<InputStream, R> function)
    {
        InputStream inputStream = null;
        try
        {
            if (path.startsWith("classpath:"))
            {
                path = path.substring(10);
                if (Utils.class.getClassLoader().getResource(path) == null)
                {
                    throw new NullPointerException(StringUtil.format("资源:{}不存在", path));
                }
                inputStream = Utils.class.getClassLoader().getResourceAsStream(path);
            }
            else if (path.startsWith("file:"))
            {
                path = path.substring(5);
                if (!new File(path).exists())
                {
                    throw new NullPointerException(StringUtil.format("资源:{}不存在", path));
                }
                inputStream = new FileInputStream(new File(path));
            }
            else
            {
                throw new UnsupportedOperationException("不支持的资源识别前缀:" + path);
            }
            return function.apply(inputStream);
        }
        catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return null;
        }
        finally
        {
            try
            {
                if (inputStream != null)
                {
                    inputStream.close();
                    inputStream = null;
                }
            }
            catch (IOException e)
            {
            }
        }
    }
}

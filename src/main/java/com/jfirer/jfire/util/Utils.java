package com.jfirer.jfire.util;

import com.jfirer.baseutil.IniReader;
import com.jfirer.baseutil.SimpleYamlReader;
import com.jfirer.baseutil.StringUtil;
import com.jfirer.jfire.core.ApplicationContext;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class Utils
{

    public static void readPropertyFile(Class<?> rootClass, String path, ApplicationContext context)
    {
        if (path.endsWith("ini") || path.endsWith("properties"))
        {
            processPath(path, inputStream -> IniReader.read(inputStream, StandardCharsets.UTF_8), rootClass,//
                        iniFile -> iniFile.keySet().forEach(property -> context.getEnv().putProperty(property, iniFile.getValue(property))));
        }
        else if (path.endsWith("yml") || path.endsWith("yaml"))
        {
            processPath(path, SimpleYamlReader::read, rootClass,//
                        (Map<String, Object> yaml) -> yaml.forEach((name, value) ->
                                                                   {
                                                                       if (value instanceof String s)
                                                                       {
                                                                           context.getEnv().putProperty(name, s);
                                                                       }
                                                                       else if (value instanceof List<?> list)
                                                                       {
                                                                           context.getEnv().putProperty(name, list.stream().map(v -> (String) v).collect(Collectors.joining(",")));
                                                                       }
                                                                       else if (value instanceof Map<?, ?> map)
                                                                       {
                                                                           context.getEnv().putProperty(name, map.entrySet().stream().map(entry -> ((String) entry.getKey()) + ":" + ((String) entry.getValue())).collect(Collectors.joining(",")));
                                                                       }
                                                                   }));
        }
    }

    private static <R> void processPath(String path, Function<InputStream, R> function, Class<?> rootClass, Consumer<R> consumer)
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
                consumer.accept(function.apply(inputStream));
            }
            catch (IOException e)
            {
                log.warn("路径:{}的配置文件不存在", path);
            }
        }
        else if (path.startsWith("file:"))
        {
            String filePath = path.substring(5);
            File   dirPath  = null;
            try
            {
                dirPath = new File(rootClass.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            }
            catch (URISyntaxException e)
            {
                log.warn("路径:{}的配置文件不存在", path);
                return;
            }
            //如果 dirPath 是一个文件夹路径，则意味着在编译输出目录下的 classes 文件夹下；如果 dirPath 是一个文件，则意味着他是一个jar 包
            dirPath = dirPath.isFile() ? dirPath.getParentFile() : dirPath.getParentFile().getParentFile();
            while (filePath.startsWith("../"))
            {
                dirPath  = dirPath.getParentFile();
                filePath = filePath.substring(3);
            }
            File pathFile = new File(dirPath, filePath);
            if (pathFile.exists() == false)
            {
                log.warn("路径:{}的配置文件不存在", pathFile.getAbsolutePath());
                return;
            }
            try (InputStream inputStream = new FileInputStream(pathFile))
            {
                consumer.accept(function.apply(inputStream));
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

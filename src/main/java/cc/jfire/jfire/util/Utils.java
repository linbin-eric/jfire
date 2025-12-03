package cc.jfire.jfire.util;

import cc.jfire.baseutil.IoUtil;
import cc.jfire.baseutil.StringUtil;
import cc.jfire.baseutil.YamlReader;
import cc.jfire.jfire.core.ApplicationContext;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
public class Utils
{
    public static void readYmlConfig(Class<?> rootClass, String path, ApplicationContext context)
    {
        if (path.endsWith("yml") || path.endsWith("yaml"))
        {
            processPath(path, rootClass,//
                        content -> {
                            Map<String, Object> mapWithFullPath = new YamlReader(content).getMapWithFullPath();
                            mapWithFullPath.forEach((key, value) -> context.getConfig().addProperty(key, value));
                        });
        }
    }

    private static <R> void processPath(String path, Class<?> rootClass, Consumer<String> consumer)
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
                assert inputStream != null;
                byte[] bytes = IoUtil.readAllBytes(inputStream);
                consumer.accept(new String(bytes, StandardCharsets.UTF_8));
            }
            catch (Throwable e)
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
            if (!pathFile.exists())
            {
                log.warn("路径:{}的配置文件不存在", pathFile.getAbsolutePath());
                return;
            }
            log.info("读取配置文件:{}", pathFile);
            try (InputStream inputStream = new FileInputStream(pathFile))
            {
                byte[] bytes = IoUtil.readAllBytes(inputStream);
                consumer.accept(new String(bytes, StandardCharsets.UTF_8));
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

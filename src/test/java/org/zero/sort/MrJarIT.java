package org.zero.sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.junit.jupiter.api.Test;

/**
 * MR-JAR 打包产物集成测试（failsafe 阶段，打包后）
 * <p>
 * 验证：jar 清单的 Multi-Release 声明、versions/9 版本化类的存在与字节码版本、
 * 以及 Java 9+ 运行时确实从 jar 的 versions/9 目录加载版本化类
 * （而非基础类），并锁死与 JDK 一致的异常消息契约。
 * <p>
 * 注意：URLClassLoader 的 parent 必须使用系统类加载器的 parent——
 * 若用系统类加载器本身作 parent，parent-first 委托会从 target/classes
 * 加载基础类，导致本测试静默地验证了错误的对象。
 *
 * @author Zero
 */
public class MrJarIT {

    private static final String BASE_ENTRY = "org/zero/sort/IndexChecks.class";
    private static final String VERSIONED_ENTRY = "META-INF/versions/9/" + BASE_ENTRY;

    /** 打包产物路径由 failsafe 通过系统属性（sort.jar）传入 */
    private static File jarFile() {
        return new File(System.getProperty("sort.jar"));
    }

    @Test
    void jarManifestDeclaresMultiRelease() throws Exception {
        JarFile jar = new JarFile(jarFile());
        try {
            assertEquals(
                    "true",
                    jar.getManifest().getMainAttributes().getValue("Multi-Release"),
                    "jar 清单必须声明 Multi-Release: true");
            assertEquals(
                    "org.zero.sort",
                    jar.getManifest().getMainAttributes().getValue("Automatic-Module-Name"));
        } finally {
            jar.close();
        }
    }

    @Test
    void versionedIndexChecksEntryIsJava9Bytecode() throws Exception {
        JarFile jar = new JarFile(jarFile());
        try {
            JarEntry entry = jar.getJarEntry(VERSIONED_ENTRY);
            assumeTrue(entry != null, "JDK 8 profile 不打包 java9 版本化类，跳过");

            byte[] header = new byte[8];
            InputStream in = jar.getInputStream(entry);
            try {
                int read = 0;
                while (read < header.length) {
                    int r = in.read(header, read, header.length - read);
                    if (r < 0) {
                        break;
                    }
                    read += r;
                }
            } finally {
                in.close();
            }
            assertEquals(0, header[6] & 0xFF, "minor 版本应为 0");
            assertEquals(53, header[7] & 0xFF, "versioned 类的 major 版本应为 53（Java 9）");
        } finally {
            jar.close();
        }
    }

    @Test
    void java9RuntimeLoadsVersionedIndexChecksFromJar() throws Exception {
        JarFile jar = new JarFile(jarFile());
        try {
            assumeTrue(jar.getJarEntry(VERSIONED_ENTRY) != null, "JDK 8 profile 产物无版本化类，跳过");
        } finally {
            jar.close();
        }

        URLClassLoader loader = new URLClassLoader(
                new URL[] {jarFile().toURI().toURL()},
                ClassLoader.getSystemClassLoader().getParent());
        try {
            Class<?> cls = loader.loadClass("org.zero.sort.IndexChecks");
            String location = cls.getResource("IndexChecks.class").toString();
            assertTrue(
                    location.contains("META-INF/versions/9"),
                    "Java 9+ 应从 jar 的 versions/9 目录加载版本化类，实际: " + location);

            Method method = cls.getDeclaredMethod("checkFromToIndex", int.class, int.class, int.class);
            method.setAccessible(true);
            try {
                method.invoke(null, 3, 2, 5);
                fail("非法区间应抛出 IndexOutOfBoundsException");
            } catch (InvocationTargetException e) {
                assertTrue(
                        e.getCause() instanceof IndexOutOfBoundsException,
                        "实际异常: " + e.getCause());
                // 锁死与基础层一致的 JDK 消息契约（见 IndexChecks 两版实现）
                assertEquals(
                        "Range [3, 2) out of bounds for length 5",
                        e.getCause().getMessage(),
                        "版本化类异常消息与契约不一致");
            }
        } finally {
            loader.close();
        }
    }
}

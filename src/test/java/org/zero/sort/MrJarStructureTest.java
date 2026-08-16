package org.zero.sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

/**
 * MR-JAR 编译产物结构测试（surefire 阶段，打包前）
 * <p>
 * 验证 compile-java9 execution 将 Java 9 版本化类输出到
 * target/classes/META-INF/versions/9 且字节码 major 版本为 53（Java 9）。
 * 打包后的完整验证（jar 清单、运行时加载行为）见 {@link MrJarIT}。
 *
 * @author Zero
 */
public class MrJarStructureTest {

    private static final String VERSIONED_CLASS = "target/classes/META-INF/versions/9/org/zero/sort/IndexChecks.class";

    @Test
    void versionedIndexChecksCompiledForJava9() throws Exception {
        File versioned = new File(VERSIONED_CLASS);
        assumeTrue(versioned.isFile(), "JDK 8 profile 不编译 java9 源，跳过");

        byte[] header = new byte[8];
        InputStream in = new FileInputStream(versioned);
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
        // class 文件头：magic CAFEBABE（4 字节）+ minor（2 字节）+ major（2 字节）
        assertEquals(0xCA, header[0] & 0xFF, "magic 校验失败");
        assertEquals(0xFE, header[1] & 0xFF, "magic 校验失败");
        assertEquals(0xBA, header[2] & 0xFF, "magic 校验失败");
        assertEquals(0xBE, header[3] & 0xFF, "magic 校验失败");
        assertEquals(0, header[6] & 0xFF, "minor 版本应为 0");
        assertEquals(53, header[7] & 0xFF, "versioned 类的 major 版本应为 53（Java 9）");
    }
}

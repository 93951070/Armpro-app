package armadillo.studio.common.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

import armadillo.studio.CloudApp;

/**
 * 处理 APK 文件复制，解决 Android 11+ /data/app/ 目录 EACCES 权限问题。
 * 在 MIUI/HyperOS 等定制系统上，即使有 MANAGE_EXTERNAL_STORAGE 权限，
 * 直接用 FileInputStream 读取 /data/app/ 路径也可能被 SELinux 拒绝。
 */
public class ApkCopyUtil {

    private static final int BUFFER_SIZE = 8192;

    /**
     * 将 APK 文件复制到应用缓存目录，解决 /data/app/ 权限问题。
     * 如果源文件在 /data/app/ 下且无法直接读取，会尝试多种方法复制。
     *
     * @param context 上下文
     * @param sourcePath 源 APK 路径（可能是 /data/app/ 下的路径）
     * @return 可读取的 APK 文件（可能是缓存中的副本），如果所有方法都失败则返回 null
     */
    @Nullable
    public static File copyToCacheIfNeeded(@NotNull Context context, @NotNull String sourcePath) {
        File sourceFile = new File(sourcePath);

        // 如果源文件不在 /data/app/ 下且可直接读取，直接返回
        if (!sourcePath.startsWith("/data/app/") && sourceFile.canRead()) {
            return sourceFile;
        }

        // 先尝试直接读取（在大部分 AOSP 设备上 MANAGE_EXTERNAL_STORAGE 是可以读取的）
        if (canReadFile(sourceFile)) {
            return sourceFile;
        }

        // 需要复制到缓存目录
        File cacheDir = new File(context.getExternalCacheDir(), "apk_temp");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        if (!cacheDir.canWrite()) {
            cacheDir = context.getCacheDir();
        }

        String fileName = "temp_apk_" + System.currentTimeMillis() + ".apk";
        File destFile = new File(cacheDir, fileName);

        // 方法1: 尝试 FileChannel 复制
        if (tryCopyWithChannel(sourceFile, destFile)) {
            return destFile;
        }

        // 方法2: 尝试 Stream 复制
        if (tryCopyWithStreams(sourceFile, destFile)) {
            return destFile;
        }

        // 方法3: 尝试通过 createPackageContext + AssetManager 获取
        if (tryCopyViaPackageContext(context, sourcePath, destFile)) {
            return destFile;
        }

        // 方法4: 尝试通过 shell cp 命令（部分设备上 app 进程权限不同）
        if (tryCopyWithShell(sourcePath, destFile.getAbsolutePath())) {
            return destFile;
        }

        // 清理失败的目标文件
        if (destFile.exists()) {
            destFile.delete();
        }
        return null;
    }

    /**
     * 检查文件是否真的可以被读取（canRead() 在 Android 11+ 上可能返回 true 但实际读取时 EACCES）
     */
    private static boolean canReadFile(File file) {
        if (!file.exists() || !file.canRead()) {
            return false;
        }
        // 实际尝试读取一个字节来验证
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static boolean tryCopyWithChannel(File source, File dest) {
        try (FileChannel src = new FileInputStream(source).getChannel();
             FileChannel dst = new FileOutputStream(dest).getChannel()) {
            dst.transferFrom(src, 0, src.size());
            return dest.exists() && dest.length() > 0;
        } catch (IOException e) {
            if (dest.exists()) dest.delete();
            return false;
        }
    }

    private static boolean tryCopyWithStreams(File source, File dest) {
        try (InputStream in = new FileInputStream(source);
             OutputStream out = new FileOutputStream(dest)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
            return dest.exists() && dest.length() > 0;
        } catch (IOException e) {
            if (dest.exists()) dest.delete();
            return false;
        }
    }

    /**
     * 通过 createPackageContext 获取包上下文后尝试读取 APK。
     * 这个方法在某些 OEM 系统上可以绕过直接文件访问限制。
     */
    private static boolean tryCopyViaPackageContext(Context context, String sourcePath, File dest) {
        // 从路径中提取包名：/data/app/~~xxx/package.name-yyy/base.apk
        String packageName = extractPackageName(sourcePath);
        if (packageName == null) {
            return false;
        }

        try {
            Context pkgContext = context.createPackageContext(packageName,
                    Context.CONTEXT_IGNORE_SECURITY);
            ApplicationInfo appInfo = pkgContext.getApplicationInfo();

            // 尝试从 ApplicationInfo.sourceDir 重新获取路径并读取
            String actualSourceDir = appInfo.sourceDir;
            if (actualSourceDir != null) {
                File actualSource = new File(actualSourceDir);
                if (!actualSource.getAbsolutePath().equals(sourcePath)) {
                    if (tryCopyWithChannel(actualSource, dest)) {
                        return true;
                    }
                    if (tryCopyWithStreams(actualSource, dest)) {
                        return true;
                    }
                }
            }

            // 尝试读取 split APKs
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && appInfo.splitSourceDirs != null) {
                // 对于 split APKs，需要合并多个 APK，这比较复杂，此处只处理 base.apk
                // 大多数签名场景 base.apk 就足够了
            }
        } catch (Exception e) {
            // PackageManager.NameNotFoundException or SecurityException
        }
        return false;
    }

    /**
     * 从 /data/app/ 路径中提取包名。
     * 路径格式: /data/app/~~random/package.name-suffix/base.apk
     */
    @Nullable
    private static String extractPackageName(String path) {
        try {
            // 路径格式: /data/app/[~~xxx/]package.name-xxx/base.apk
            // 或: /data/app/package.name-xxx/base.apk (旧版 Android)
            String[] parts = path.split("/");
            for (String part : parts) {
                if (part.contains(".")) {
                    // 可能是包名，去掉可能的 -suffix (如 -1, -2, -base)
                    int dashIndex = part.lastIndexOf('-');
                    if (dashIndex > 0 && dashIndex < part.length() - 1) {
                        String suffix = part.substring(dashIndex + 1);
                        // 如果后缀是数字或 "base"，去掉
                        if (suffix.matches("\\d+") || suffix.equals("base") || suffix.length() <= 3) {
                            return part.substring(0, dashIndex);
                        }
                    }
                    return part;
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    /**
     * 尝试通过 shell 命令复制（在某些设备上可能生效）
     */
    private static boolean tryCopyWithShell(String sourcePath, String destPath) {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[]{"cp", sourcePath, destPath});
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                File dest = new File(destPath);
                return dest.exists() && dest.length() > 0;
            }
        } catch (Exception e) {
            // ignore
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return false;
    }

    /**
     * 获取应用的可读 APK 路径，如果在 /data/app/ 下无法读取则复制到缓存。
     * 这是一个便捷方法，直接从包名获取可读取的 APK 路径。
     */
    @Nullable
    public static File getReadableApk(@NotNull Context context, @NotNull String packageName) {
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
            if (appInfo.sourceDir != null) {
                return copyToCacheIfNeeded(context, appInfo.sourceDir);
            }
        } catch (PackageManager.NameNotFoundException e) {
            // ignore
        }
        return null;
    }
}

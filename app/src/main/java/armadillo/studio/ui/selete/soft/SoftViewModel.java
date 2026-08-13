package armadillo.studio.ui.selete.soft;


import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import armadillo.studio.CloudApp;
import armadillo.studio.common.base.BaseViewModel;
import armadillo.studio.common.base.callback.GetAppCallBack;
import armadillo.studio.common.utils.AppUtils;
import armadillo.studio.common.utils.FileSize;
import armadillo.studio.model.apk.PackageInfos;

public class SoftViewModel extends BaseViewModel<List<PackageInfos>> {
    @Override
    public List<PackageInfos> getValue() {
        return null;
    }

    public void getAll(GetAppCallBack callBack, Activity activity) {
        CloudApp.getCachedThreadPool().execute(()->{
            List<PackageInfos> newpackages = new ArrayList<>();
            try {
                PackageManager pm = CloudApp.getContext().getPackageManager();
                List<PackageInfo> packages = pm.getInstalledPackages(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? PackageManager.MATCH_UNINSTALLED_PACKAGES : PackageManager.GET_UNINSTALLED_PACKAGES);
                for (PackageInfo packageInfo : packages) {
                    if (activity == null)
                        return;
                    if (activity.isDestroyed())
                        return;
                    if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0
                            && packageInfo.applicationInfo.sourceDir != null) {
                        PackageInfos infos = new PackageInfos();
                        infos.setPackageInfo(packageInfo);
                        infos.setName(packageInfo.applicationInfo.loadLabel(pm).toString());
                        String sourceDir = packageInfo.applicationInfo.sourceDir;
                        File sourceFile = new File(sourceDir);
                        if (sourceFile.canRead()) {
                            infos.setSize(FileSize.getAutoFileOrFileSize(sourceDir));
                            infos.setIco(AppUtils.getApkDrawable(sourceDir));
                            AnalysisJiaGu(sourceDir, infos);
                        } else {
                            // Android 11+ /data/app/ 可能无法直接读取
                            infos.setSize("");
                            infos.setIco(pm.getApplicationIcon(packageInfo.applicationInfo));
                            infos.setJiagu("点击选择后自动处理");
                            infos.setJiagu_flag(false);
                        }
                        newpackages.add(infos);
                    }
                }
                Collections.sort(newpackages, (packageInfo, t1) -> {
                    if (packageInfo.getPackageInfo().applicationInfo.sourceDir == null)
                        return -1;
                    if (t1.getPackageInfo().applicationInfo.sourceDir == null)
                        return -1;
                    File src = new File(packageInfo.getPackageInfo().applicationInfo.sourceDir);
                    File old = new File(t1.getPackageInfo().applicationInfo.sourceDir);
                    if (src.lastModified() < old.lastModified())
                        return 1;
                    else
                        return -1;
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            new Handler(Looper.getMainLooper()).post(() -> {
                callBack.Next(newpackages);
            });
        });
    }

    private void AnalysisJiaGu(String path, PackageInfos info) {
        if (path == null) {
            info.setJiagu("未检测到加固");
            info.setJiagu_flag(false);
            return;
        }
        try (ZipFile zipFile = new ZipFile(path)) {
            // 加固特征签名表：ZipEntry 名称 -> 加固厂商名称（单次遍历匹配）
            Map<String, String> signatures = new HashMap<>();
            signatures.put("assets/libjiagu.so", "360加固");
            signatures.put("assets/libjiagu_a64.so", "360加固");
            signatures.put("assets/libjiagu_x86.so", "360加固");
            signatures.put("assets/libjiagu_x64.so", "360加固");
            signatures.put("assets/ijm_lib/armeabi/libexec.so", "爱加密");
            signatures.put("assets/ijm_lib/arm64-v8a/libexec.so", "爱加密");
            signatures.put("lib/armeabi-v7a/libexec.so", "爱加密");
            signatures.put("lib/armeabi/libkdp.so", "几维加固");
            signatures.put("lib/armeabi-v7a/libkdp.so", "几维加固");
            signatures.put("lib/arm64-v8a/libkdp.so", "几维加固");
            signatures.put("lib/armeabi/libSecShell.so", "梆梆加固");
            signatures.put("lib/armeabi/libDexHelper.so", "梆梆加固");
            signatures.put("lib/arm64-v8a/libDexHelper.so", "梆梆加固");
            signatures.put("lib/armeabi/DexHelper.so", "梆梆定制版加固");
            signatures.put("lib/armeabi/mix.dex", "腾讯加固");
            signatures.put("lib/armeabi/libshellx-super.2016.so", "腾讯乐固");
            signatures.put("assets/libshell.so", "腾讯乐固");
            signatures.put("assets/libtosprotection.armeabi-v7a.so", "腾讯御安全");
            signatures.put("lib/armeabi/libx3g.so", "顶象加固");
            signatures.put("lib/armeabi-v7a/libx3g.so", "顶象加固");
            signatures.put("lib/arm64-v8a/libx3g.so", "顶象加固");
            signatures.put("assets/libzuma.so", "阿里加固");
            signatures.put("assets/dp.arm.so.dat", "dexprotect加固");
            signatures.put("lib/armeabi/libbaiduprotect.so", "百度加固");
            signatures.put("lib/armeabi-v7a/libbaiduprotect.so", "百度加固");
            signatures.put("lib/arm64-v8a/libbaiduprotect.so", "百度加固");
            signatures.put("lib/armeabi/libapktoolplus_jiagu.so", "apktoolplus加固");
            signatures.put("lib/armeabi/libitsec.so", "海云安加固");
            signatures.put("lib/armeabi/libnesec.so", "网易易盾");
            signatures.put("lib/armeabi/libtup.so", "通付盾");
            signatures.put("lib/armeabi/librsprotect.so", "瑞星");
            signatures.put("lib/armeabi/libchaoxu.so", "娜迦(Nagapt)");
            signatures.put("lib/armeabi/libnagapt.so", "娜迦(Nagapt)");
            signatures.put("assets/libkylin.so", "银河麒麟");
            signatures.put("lib/armeabi/libvmp.so", "VMProtect");
            signatures.put("lib/armeabi/libosshield.so", "Oneshield");

            // 通配签名：lib/armeabi/libshella-*.so -> 腾讯乐固
            final String shellPrefix = "lib/armeabi/libshella-";
            final String shellSuffix = ".so";

            String detected = null;
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                String match = signatures.get(name);
                if (match != null) {
                    detected = match;
                    break;
                }
                if (name.startsWith(shellPrefix) && name.endsWith(shellSuffix)) {
                    int midStart = shellPrefix.length();
                    int midEnd = name.length() - shellSuffix.length();
                    if (midEnd > midStart
                            && !name.substring(midStart, midEnd).contains("/")) {
                        detected = "腾讯乐固";
                        break;
                    }
                }
            }

            if (detected != null) {
                info.setJiagu(detected);
                info.setJiagu_flag(true);
            } else {
                info.setJiagu("未检测到加固");
                info.setJiagu_flag(false);
            }
        } catch (Exception e) {
            info.setJiagu("未检测到加固");
            info.setJiagu_flag(false);
        }
    }
}

# ==================== 基础配置 ====================
-optimizationpasses 1
-dontoptimize
-keepattributes *Annotation*,InnerClasses,Signature,EnclosingMethod
-ignorewarnings
-verbose
-dontusemixedcaseclassnames
-repackageclasses armadillo.studio
-allowaccessmodification
-mergeinterfacesaggressively

# ==================== APK签名库 ====================
-keep public class com.android.apksig.** {*;}

# ==================== 模型类（序列化） ====================
-keep public class armadillo.studio.model.** {*;}

# ==================== Android四大组件 ====================
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference

# ==================== View ====================
-keep public class * extends android.view.View
-keep public class * extends android.view.View{
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# ==================== ButterKnife ====================
-keep class butterknife.** { *; }
-keep class *$$ViewBinder { *; }
-keep class *$$ViewBinding { *; }
-keepclasseswithmembers class * {
    @butterknife.* <methods>;
}
-keepclasseswithmembers class * {
    @butterknife.* <fields>;
}

# ==================== Activity回调 ====================
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

# ==================== 序列化 ====================
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ==================== Native方法 ====================
-keepclasseswithmembernames class * {
    native <methods>;
}

# ==================== WebView ====================
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
    public void *(android.webkit.WebView, java.lang.String);
}

# ==================== Gson ====================
-keep class com.google.gson.** { *; }
-keep class sun.misc.Unsafe { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * extends com.google.gson.TypeAdapterFactory
-keep class * extends com.google.gson.JsonSerializer
-keep class * extends com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ==================== Glide ====================
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { <init>(...); }
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

# ==================== MarkdownView ====================
-keep class com.tiagohm.** { *; }

# ==================== dexlib2 ====================
-keep class org.smali.** { *; }

# ==================== 移除日志（release） ====================
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** i(...);
    public static *** d(...);
    public static *** w(...);
    public static *** e(...);
}
-assumenosideeffects class armadillo.studio.common.log.logger {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# ==================== 枚举 ====================
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ==================== R文件（减小体积） ====================
-assumenosideeffects class **.R$* {
    *** get*(...);
}

# ==================== Kotlin ====================
-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-keepclassmembers class **$$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.collections.** {
    <methods>;
}

# ==================== Jetpack Compose ====================
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.ui.** { *; }
-dontwarn org.jetbrains.annotations.**

# ==================== Kotlin Coroutines ====================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# ==================== ViewModel ====================
-keep class * extends androidx.lifecycle.ViewModel { <init>(...); }
-keep class armadillo.studio.ui.compose.** { *; }

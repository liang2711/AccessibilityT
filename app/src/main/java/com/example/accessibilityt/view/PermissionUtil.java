package com.example.accessibilityt.view;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;

import com.example.accessibilityt.MainActivity;

import java.util.List;

public class PermissionUtil {
    public static boolean isAccessibilityServiceEnabled(Context context, Class<? extends AccessibilityService> accessibilityServiceClass) {
        AccessibilityManager accessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (AccessibilityServiceInfo service : enabledServices) {
            if (service.getResolveInfo().serviceInfo.packageName.equals(context.getPackageName()) && service.getResolveInfo().serviceInfo.name.equals(accessibilityServiceClass.getName())) {
                Log.d(MainActivity.TAG,service.getId()+"  "+service.getResolveInfo().serviceInfo.name+"  "+service.getResolveInfo().resolvePackageName);
                return true;
            }
        }
        return false;
    }

    // 打开无障碍设置界面
    private static void openAccessibilitySettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    // 检查并申请无障碍服务权限
    public static void requestAccessibilityServicePermission(Context context, Class<? extends AccessibilityService> accessibilityServiceClass) {
        if (!isAccessibilityServiceEnabled(context, accessibilityServiceClass)) {
            openAccessibilitySettings(context);
        }
    }

    //用于检查应用程序是否具有修改系统设置的权限。
    public static boolean isSettingsCanWrite(Context context){
        return Settings.System.canWrite(context);
    }

    //用于检查应用程序是否具有在其他应用程序上层绘制的权限。
    private static boolean isCanDrawOverlays(Context context){
        return Settings.canDrawOverlays(context);
    }
    public static boolean requsetFloatWindowPermission(Context context){
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if (!isCanDrawOverlays(context)){
                Toast.makeText(context,"请同意悬浮窗的权限",Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                context.startActivity(intent);
                return true;
            }
        }
        return false;
    }
    public static boolean isStartFloadWindowService(Context context){
        Log.d(MainActivity.TAG+"727","isStartFloadWindowService-------");
        ActivityManager activityManager= (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningAppProcessInfos=activityManager.getRunningServices(Integer.MAX_VALUE);

        if (runningAppProcessInfos.size()<=0)
            return false;

        for (ActivityManager.RunningServiceInfo info:runningAppProcessInfos){
            Log.d(MainActivity.TAG+"727",info.service.getShortClassName()+" "+info.service.getPackageName());
        }
        return true;
    }
}

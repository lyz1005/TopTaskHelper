package com.lyz.toptaskhelper.tools;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.List;

public class AppsWithUsageAccess {

	private static boolean doesApiExist() {
		return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
	}

	/**
	 * 获得最近的应用统计数据 不需要考虑新版本问题，方法中低版本默认返回null
	 * 
	 * @return return null 持续在 duration这个时间内应用没有更新 或者新版本手机
	 */
	public static String getRecentUsagePackageName(Context context) {
		if (doesApiExist()) {
			return AppsWithUsageAccessImpl.getRecentUsagePackageName(context);
		} else {
			return null;
		}
	}

	public static boolean hasEnable(Context context) {
        return doesApiExist() && AppsWithUsageAccessImpl.hasEnable(context);
    }


	/**
	 * has Apps with usage access module
	 * @return isHas
	 */
	public static boolean hasModule(Context context) {
		try {
			PackageManager packageManager = context.getPackageManager();
			Intent intent = new Intent("android.settings.USAGE_ACCESS_SETTINGS");
			List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
			return list.size() > 0;
		} catch (SecurityException e) {
			// 在某个三星的设备上会出现这个异常
			return false;
		}
	}
//  /** 请求授权 */
//	public static void toImpower(Activity context) {
//		context.startActivity(new Intent("android.settings.USAGE_ACCESS_SETTINGS"));
//	}

	public static boolean isSupport(Context context) {
		return hasModule(context) && hasEnable(context);
	}
}

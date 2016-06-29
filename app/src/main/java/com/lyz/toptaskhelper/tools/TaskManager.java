package com.lyz.toptaskhelper.tools;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.util.List;

public class TaskManager {

	private static final Boolean LOG = true;
	private static final String TAG = "TaskManager";
	private static final int DELAY_SEC = 2;
	private static final int DELAY_MS = DELAY_SEC * 1000;

	private static TaskManager instance;

	private String currentForegroundPackageName;

	private Strategy strategy;

	private final Handler handler = new Handler();

	private TaskManager(Context context) {
		strategy = createStrategyAuto(context);
	}

	private static Strategy createStrategyAuto(Context context) {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
			if (AppsWithUsageAccess.isSupport(context)) {
				return new Strategy_Ver5(context);
			} else {
				return new Strategy_Ver4_4(context);
			}
		} else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
			return new Strategy_Ver4_4(context);
		} else {
			return new Strategy_Ver4(context);
		}
	}

	public static void init(Context context){
		if(instance==null){
			instance = new TaskManager(context);
            instance.handler.post(instance.topTaskRunnable);
		}
	}

	private final Runnable topTaskRunnable = new Runnable() {

		@Override
		public void run() {
            String topPackageName = strategy.getForegroundApp();
            if(topPackageName == null && currentForegroundPackageName == null ) {
                onChange(null);
            }else if (topPackageName != null && !topPackageName.equals(currentForegroundPackageName)) {
                onChange(topPackageName);
			}
			handler.postDelayed(this, DELAY_MS);
		}

        private void onChange(String topPackageName) {
            if (LOG) {
                Log.d(TAG, "Changed:"+topPackageName);
            }
            currentForegroundPackageName = topPackageName;
            //TODO 顶层窗口发生变化
        }
    };

	/////////////////////////////////////////////////////////////////////

	private static abstract class Strategy {

        protected final Context context;
        public Strategy(Context context){
            this.context = context;
        }

        public abstract String getForegroundApp();
	}

	private static class Strategy_Ver5 extends Strategy {

        public Strategy_Ver5(Context context){
            super(context);
        }

		@Override
		public String getForegroundApp() {
			return AppsWithUsageAccess.getRecentUsagePackageName(context);
		}

	}

	private static class Strategy_Ver4_4 extends Strategy {

        public Strategy_Ver4_4(Context context) {
            super(context);
        }

        @Override
		public String getForegroundApp() {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			List<RunningAppProcessInfo> lr = activityManager.getRunningAppProcesses();
			if (lr == null) {
				if (LOG) {
					Log.w(TAG, "getRunningAppProcesses() return null");
				}
				return null;
			}
			for (RunningAppProcessInfo ra : lr) {
				if (ra.importance == RunningAppProcessInfo.IMPORTANCE_VISIBLE
					|| ra.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
					return ra.processName;
				}
			}
			return null;
		}

	}

	private static class Strategy_Ver4 extends Strategy {

        public Strategy_Ver4(Context context) {
            super(context);
        }

        @Override
		public String getForegroundApp() {
            try {
                RunningTaskInfo rt = getTopTask(context);
                if (rt == null) {
                    if (LOG) {
                        Log.w(TAG, "runningTaskInfo is null");
                    }
                    return null;
                }
                String pn = rt.topActivity.getPackageName();
                if (pn == null) {
                    if (LOG) {
                        Log.w(TAG, "getPackageName() return null");
                    }
                    return null;
                }
                return pn;
            } catch (SecurityException se) {
                se.printStackTrace();
                return null;
            }
		}

        private RunningTaskInfo getTopTask(Context context) {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            @SuppressWarnings("deprecation")
            List<RunningTaskInfo> list = activityManager.getRunningTasks(1);
            if (list == null || list.isEmpty()) {
                if (LOG) {
                    Log.w(TAG, "ActivityManager.getRunningTasks() return null");
                }
                return null;
            }
            return list.get(0);
        }

	}

//	/**
//	 * 判断本程序是否在前台
//	 */
//	public static boolean amIForeground(Context context) {
//        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//		List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
//		if (appProcesses == null || appProcesses.size() == 0) {
//			return false;
//		}
//		ApplicationInfo ai = context.getApplicationInfo();
//		for (RunningAppProcessInfo appProcess : appProcesses) {
//			if (appProcess.uid == ai.uid) {
//				if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
//					return true;
//				} else {
//					return false;
//				}
//			}
//		}
//		return false;
//	}
}

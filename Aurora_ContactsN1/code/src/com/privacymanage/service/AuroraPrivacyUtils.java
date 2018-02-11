package com.privacymanage.service;

import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;

import com.android.contacts.ContactsApplication;
import com.android.contacts.calllog.AuroraManagePrivate;
import com.privacymanage.service.IPrivacyManageService;

public class AuroraPrivacyUtils {
	
	private static final String TAG = "Contacts_AuroraPrivacyUtils";
	public static boolean mIsPrivacyMode = false;
	public static boolean mIsServiceConnected = false;
	private static final String SERVICE_ACTION = "com.privacymanage.service.IPrivacyManageService";
	private static Intent intent = new Intent(SERVICE_ACTION);
	
	private static IPrivacyManageService mPrivacyManSer;
	public static long mCurrentAccountId = 0;
	public static String mCurrentAccountHomePath = null;
	public static int mPrivacyContactsNum = 0;
	public static int mPrivacyCallLogsNum = 0;
	
	private static void logs(String str) {
		Log.i(TAG, str);
	}
	
	public static void bindService(Context context) {
		if (!mIsServiceConnected) {
			try {
				if (!isLowVersion()) {
					intent = createExplicitFromImplicitIntent(
							context, intent);
				}
				context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE|Context.BIND_IMPORTANT);
				mIsPrivacyMode = true;
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void unbindService(Context context) {
		if (mIsServiceConnected) {
			try {
				context.unbindService(serviceConnection);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void initCurrentAccountId() {
		try {
			if (mPrivacyManSer != null) {
				mCurrentAccountId = 
						mPrivacyManSer.getCurrentAccount(
								"com.android.contacts", 
								"com.android.contacts.activity.AuroraPrivacyContactListActivity")
								.getAccountId();
				AuroraManagePrivate.getInstance().updateCallLog();
				mCurrentAccountHomePath = 
						mPrivacyManSer.getCurrentAccount(
								"com.android.contacts", 
								"com.android.contacts.activity.AuroraPrivacyContactListActivity")
								.getHomePath();
				logs("initCurrentAccountId mCurrentAccountId = " + mCurrentAccountId + "  mCurrentAccountHomePath = " + mCurrentAccountHomePath);
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException se) {
			se.printStackTrace();
		}
		
	}
	
	public static void setPrivacyNum(Context context, final String calssName, final int number, final long accountId) {
		try {
			if (mPrivacyManSer == null) {
				if (!mIsServiceConnected) {
					if (!isLowVersion()) {
						intent = createExplicitFromImplicitIntent(
								context, intent);
					}
					context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE|Context.BIND_IMPORTANT);
				}
			}
			
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					int i = 0;
					while(true) {
						if (mIsServiceConnected) {
							break;
						}
						
						try {
							Thread.sleep(10);
							i++;
						} catch (Exception e) {
							e.printStackTrace();
						}
						
						if (i > 15) {
							break;
						}
					}
					
					logs("mIsServiceConnected = " + mIsServiceConnected);
					if (mPrivacyManSer != null && mIsServiceConnected) {
						try {
							logs("calssName = " + calssName +  "  number = " + number + "  accountId = " + accountId);
							mPrivacyManSer.setPrivacyNum("com.android.contacts", calssName, number, accountId);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void resetPrivacyNumOfAllAccount(Context context, String pkgName, String className) {
	    try {
	    	if (mPrivacyManSer == null) {
				if (!mIsServiceConnected) {
					if (!isLowVersion()) {
						intent = createExplicitFromImplicitIntent(
								context, intent);
					}
					context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE|Context.BIND_IMPORTANT);
				}
			} else {
                mPrivacyManSer.resetPrivacyNumOfAllAccount(pkgName, className);
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException se) {
            se.printStackTrace();
        }
	}
	
	private static ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			logs("onServiceConnected");
			mIsPrivacyMode = true;
			mIsServiceConnected = true;
			mPrivacyManSer = IPrivacyManageService.Stub.asInterface(service);
			initCurrentAccountId();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			logs("onServiceDisconnected");
			mIsServiceConnected = false;
			mPrivacyManSer = null;
			mCurrentAccountId = 0;
			mCurrentAccountHomePath = null;
			mIsPrivacyMode = false;
			
			killPrivacyActivity();
		}
	};
	
	public static void killPrivacyActivity() {
		try {
			if (ContactsApplication.mPrivacyActivityList != null) {
				for (Activity ac : ContactsApplication.mPrivacyActivityList) {
					ac.finish();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static long getCurrentAccountId() {
		return mCurrentAccountId;
	}
	
	public static boolean isLowVersion() {
		int version = android.os.Build.VERSION.SDK_INT;
		if (version <= 20) {
			return true;
		}
		return false;
	}

	public static Intent createExplicitFromImplicitIntent(Context context,
			Intent implicitIntent) {
		// Retrieve all services that can match the given intent
		PackageManager pm = context.getPackageManager();
		List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent,
				0);

		// Make sure only one match was found
		if (resolveInfo == null || resolveInfo.size() != 1) {
			return null;
		}

		// Get component info and create ComponentName
		ResolveInfo serviceInfo = resolveInfo.get(0);
		String packageName = serviceInfo.serviceInfo.packageName;
		String className = serviceInfo.serviceInfo.name;
		ComponentName component = new ComponentName(packageName, className);

		// Create a new intent. Use the old one for extras and such reuse
		Intent explicitIntent = new Intent(implicitIntent);

		// Set the component to be explicit
		explicitIntent.setComponent(component);

		return explicitIntent;
	}
}
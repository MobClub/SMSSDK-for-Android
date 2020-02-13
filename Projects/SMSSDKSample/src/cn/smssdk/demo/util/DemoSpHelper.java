package cn.smssdk.demo.util;

import com.mob.MobSDK;
import com.mob.tools.utils.SharePrefrenceHelper;

public class DemoSpHelper {
	private static final String SP_NAME = "sms_demo_sp";
	private static final int SP_VERSION = 1;
	private static final String KEY_PRIVACY_GRANTED = "key_privacy_granted";
	private static DemoSpHelper instance;
	private SharePrefrenceHelper sp;

	public static DemoSpHelper getInstance() {
		if (instance == null) {
			synchronized (DemoSpHelper.class) {
				if (instance == null) {
					instance = new DemoSpHelper();
				}
			}
		}
		return instance;
	}

	private DemoSpHelper() {
		sp = new SharePrefrenceHelper(MobSDK.getContext());
		sp.open(SP_NAME, SP_VERSION);
	}

	public boolean isPrivacyGranted() {
		return sp.getBoolean(KEY_PRIVACY_GRANTED, false);
	}

	public void setPrivacyGranted(boolean granted) {
		sp.putBoolean(KEY_PRIVACY_GRANTED, granted);
	}
}

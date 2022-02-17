package cn.smssdk.demo.privacy;

import android.util.Log;

import com.mob.MobSDK;
import com.mob.PrivacyPolicy;

public class PrivacyHolder {
	private static final String TAG = "PrivacyHolder";
	private static PrivacyHolder instance;
	private String privacyUrl = "http://www.mob.com/about/policy";

	private PrivacyHolder() {}

	public static PrivacyHolder getInstance() {
		if (instance == null) {
			synchronized (PrivacyHolder.class) {
				if (instance == null) {
					instance = new PrivacyHolder();
				}
			}
		}
		return instance;
	}



	public String getPrivacyUrl() {
		return privacyUrl == null ? null : privacyUrl.trim();
	}
}

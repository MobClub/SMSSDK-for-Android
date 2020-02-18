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

	public void init() {
		MobSDK.getPrivacyPolicyAsync(MobSDK.POLICY_TYPE_URL, new PrivacyPolicy.OnPolicyListener() {
			@Override
			public void onComplete(PrivacyPolicy privacyPolicy) {
				if (privacyPolicy != null) {
					privacyUrl = privacyPolicy.getContent();
				}
			}

			@Override
			public void onFailure(Throwable throwable) {
				Log.e(TAG, "Get policy error", throwable);
			}
		});
	}

	public String getPrivacyUrl() {
		return privacyUrl == null ? null : privacyUrl.trim();
	}
}

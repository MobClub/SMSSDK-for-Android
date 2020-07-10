package cn.smssdk.demo;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mob.MobSDK;
import com.mob.OperationCallback;
import com.mob.tools.utils.ResHelper;

import java.util.ArrayList;

import cn.smssdk.SMSSDK;
import cn.smssdk.demo.privacy.OnDialogListener;
import cn.smssdk.demo.privacy.PrivacyDialog;
import cn.smssdk.demo.util.DemoSpHelper;

/**
 * 新的短信首页,点击按钮进入验证页
 */
public class HomeActivity extends Activity {
	private static final String TAG = "HomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImageView ivTop = new ImageView(this);
        ivTop.setScaleType(ImageView.ScaleType.CENTER_CROP);
        ivTop.setImageResource(R.drawable.smssdk_home_bg);
        ivTop.setId(R.id.ivTop);
        RelativeLayout.LayoutParams paramsTop = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);


        LinearLayout llBottom = new LinearLayout(this);
        llBottom.setGravity(Gravity.CENTER);
        TextView textView = new TextView(this);
        textView.setBackgroundResource(R.drawable.smssdk_corner_green_bg);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, ResHelper.dipToPx(this, 14));
        textView.setTextColor(Color.WHITE);
        textView.setText(R.string.smssdk_start_verify);
        llBottom.addView(textView, new LinearLayout.LayoutParams(ResHelper.dipToPx(this, 255), ResHelper.dipToPx(this, 46)));

        RelativeLayout rootView = new RelativeLayout(this);
        RelativeLayout.LayoutParams paramsBottom = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        paramsBottom.addRule(RelativeLayout.BELOW, R.id.ivTop);
        paramsBottom.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        TextView tvVersion = new TextView(this);
        tvVersion.setTextColor(getResources().getColor(R.color.smssdk_hint_textcolor));
        tvVersion.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        tvVersion.setText(getString(R.string.smssdk_version, SMSSDK.getVersion()));
        RelativeLayout.LayoutParams paramsVersion = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramsVersion.addRule(RelativeLayout.CENTER_IN_PARENT);

        rootView.addView(ivTop, paramsTop);
        rootView.addView(llBottom, paramsBottom);
        rootView.addView(tvVersion, paramsVersion);

        setContentView(rootView);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, VerifyActivity.class));
            }
        });

		if (!DemoSpHelper.getInstance().isPrivacyGranted()) {
			PrivacyDialog privacyDialog = new PrivacyDialog(HomeActivity.this, new OnDialogListener() {
				@Override
				public void onAgree() {
					uploadResult(true);
					DemoSpHelper.getInstance().setPrivacyGranted(true);
					goOn();
				}

				@Override
				public void onDisagree() {
					uploadResult(false);
					DemoSpHelper.getInstance().setPrivacyGranted(false);
					Handler handler = new Handler(new Handler.Callback() {
						@Override
						public boolean handleMessage(Message msg) {
							System.exit(0);
							return false;
						}
					});
					handler.sendEmptyMessageDelayed(0, 500);
				}
			});
			privacyDialog.show();
		} else {
			goOn();
		}
    }

	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

	}

	private void uploadResult(boolean granted) {
		MobSDK.submitPolicyGrantResult(granted, new OperationCallback<Void>() {
			@Override
			public void onComplete(Void aVoid) {
				// Nothing to do
			}

			@Override
			public void onFailure(Throwable throwable) {
				// Nothing to do
				Log.e(TAG, "Submit privacy grant result error", throwable);
			}
		});
	}

	/**
	 * 可以继续流程，一般是接受隐私条款后
	 */
	private void goOn() {
		// 动态权限申请
		if (Build.VERSION.SDK_INT >= 23) {
			int readPhone = checkSelfPermission("android.permission.READ_PHONE_STATE");
			int receiveSms = checkSelfPermission("android.permission.RECEIVE_SMS");
			int readContacts = checkSelfPermission("android.permission.READ_CONTACTS");
			int readSdcard = checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE");

			int requestCode = 0;
			final ArrayList<String> permissions = new ArrayList<String>();
			if (readPhone != PackageManager.PERMISSION_GRANTED) {
				requestCode |= 1 << 0;
				permissions.add("android.permission.READ_PHONE_STATE");
			}
			if (receiveSms != PackageManager.PERMISSION_GRANTED) {
				requestCode |= 1 << 1;
				permissions.add("android.permission.RECEIVE_SMS");
			}
			if (readContacts != PackageManager.PERMISSION_GRANTED) {
				requestCode |= 1 << 2;
				permissions.add("android.permission.READ_CONTACTS");
			}
			if (readSdcard != PackageManager.PERMISSION_GRANTED) {
				requestCode |= 1 << 3;
				permissions.add("android.permission.READ_EXTERNAL_STORAGE");
			}
			if (requestCode > 0) {
				String[] permission = new String[permissions.size()];
				this.requestPermissions(permissions.toArray(permission), requestCode);
				return;
			}
		}
	}
}

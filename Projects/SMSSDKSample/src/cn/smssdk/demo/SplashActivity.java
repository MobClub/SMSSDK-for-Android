package cn.smssdk.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.mob.MobSDK;
import com.mob.commons.dialog.entity.MobPolicyUi;

import cn.smssdk.demo.privacy.PrivacyHolder;
import cn.smssdk.demo.util.DemoSpHelper;

/**
 * 开屏页，双击进入旧的短信demo
 */
public class SplashActivity extends Activity implements View.OnTouchListener {
    private GestureDetector gestureDetector;
    private Handler handler;
    private static final long DELAY = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ImageView imageView = new ImageView(this);
        imageView.setId(R.id.ivSplash);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(R.drawable.smssdk_openpage_bg);
        setContentView(imageView);

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            //双击进入旧的入口
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
                return false;
            }
        });
        imageView.setOnTouchListener(this);
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this,HomeActivity.class));
                finish();
            }
        }, DELAY);

        init();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    private void init() {
    	if (!DemoSpHelper.getInstance().isPrivacyGranted()) {
    		// 初始化MobTech隐私协议获取
			PrivacyHolder.getInstance().init();
		}
		// 初始化隐私二次确认框相关自定义设置
		initPrivacyRecheckSettings();
	}

	private void initPrivacyRecheckSettings() {
    	MobSDK.setAllowDialog(true);
    	// 自定义UI
		MobPolicyUi mobPolicyUi = new MobPolicyUi.Builder()
				// 设置弹框背景色资源ID
//				.setBackgroundColorId(R.color.demo_common_white)
				// 设置同意按钮背景色资源ID
				.setPositiveBtnColorId(R.color.smssdk_green)
				// 设置拒绝按钮背景色资源ID
//				.setNegativeBtnColorId(R.color.demo_common_white)
				.build();
		// 需在使用SDK接口前调用，否则不生效
		MobSDK.setPolicyUi(mobPolicyUi);
	}
}

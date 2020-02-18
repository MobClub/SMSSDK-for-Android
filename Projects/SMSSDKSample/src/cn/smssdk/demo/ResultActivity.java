package cn.smssdk.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 验证结果页
 */
public class ResultActivity extends Activity implements View.OnClickListener {
    private static final String KEY_RESULT = "result";
    private static final String KEY_PHONE = "phone";

    /**
     * 跳转验证结果页，这版只显示成功页
     * @param context
     * @param success true显示成功，false显示失败
     * @param phone 验证的手机号
     */
    public static void startActivity(Context context, boolean success, String phone) {
        Intent intent = new Intent(context, ResultActivity.class);
        intent.putExtra(KEY_RESULT, success);
        intent.putExtra(KEY_PHONE, phone);
        context.startActivity(intent);
    }

	public static void startActivityForResult(Activity context, int requestCode, boolean success, String phone) {
		Intent intent = new Intent(context, ResultActivity.class);
		intent.putExtra(KEY_RESULT, success);
		intent.putExtra(KEY_PHONE, phone);
		context.startActivityForResult(intent, requestCode);
	}



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.smssdk_result_activity);
        if (getIntent() == null) {
            finish();
        }
        boolean success = getIntent().getBooleanExtra(KEY_RESULT, true);
        String phone = getIntent().getStringExtra(KEY_PHONE);
        initViews(success, phone);
    }

    private void initViews(boolean success, String phone) {
        TextView tvVerifyAgain = findViewById(R.id.tvVerifyAgain);
        if (!success) {
            findViewById(R.id.layoutTop).setBackgroundResource(R.drawable.smssdk_failure_bg);
            ((ImageView) findViewById(R.id.ivStatus)).setImageResource(R.drawable.smssdk_failure);
            ((TextView) findViewById(R.id.tvStatusHint)).setText(R.string.smssdk_login_failure);
            ((TextView) findViewById(R.id.tvHint)).setText(R.string.smssdk_failure_title);
            tvVerifyAgain.setBackgroundResource(R.drawable.smssdk_corner_red_bg);
        } else {
            ((TextView) findViewById(R.id.tvPhone)).setText(phone);
        }
        TextView tvCustomer = findViewById(R.id.tvCustomer);
        String text = getString(R.string.smssdk_customer_service);
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.smssdk_686868)), 0, 4, SpannableString.SPAN_INCLUSIVE_INCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.smssdk_green)), 4, text.length(), SpannableString.SPAN_EXCLUSIVE_INCLUSIVE);
        tvCustomer.setText(spannableString);

        tvVerifyAgain.setOnClickListener(this);
        findViewById(R.id.ivPhone).setOnClickListener(this);
        findViewById(R.id.ivQQ).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvVerifyAgain:
                finish();
                break;
            case R.id.ivPhone:
                //跳转到拨号页面，带入客服号码
                try {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    Uri data = Uri.parse("tel:" + "400-685-2216");
                    intent.setData(data);
                    startActivity(intent);
                } catch (Throwable throwable) {
                }
                break;
            case R.id.ivQQ:
                //跳转到qq客服web页，如果手机安装qq，且web识别出qq客户端，则用户可以选择调起qq客户端的客服对话页
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://url.cn/58ilGpI?_type=wpa&qidian=true"));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (Throwable throwable) {
                }
                break;
        }
    }
}

package cn.smssdk.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mob.tools.FakeActivity;
import com.mob.tools.utils.ResHelper;
import com.mob.tools.utils.SharePrefrenceHelper;

import org.json.JSONObject;

import java.util.HashMap;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.UserInterruptException;
import cn.smssdk.demo.util.DemoResHelper;
import cn.smssdk.gui.CountryPage;

/**
 * 验证页，包括短信验证和语音验证，默认使用中国区号
 */
public class VerifyActivity extends Activity implements View.OnClickListener {
	private static final String TAG = "VerifyActivity";
    private static final String[] DEFAULT_COUNTRY = new String[]{"中国", "42", "86"};
    private static final int COUNTDOWN = 60;
    private static final String TEMP_CODE = "1319972";
    private static final String KEY_START_TIME = "start_time";
    private static final int REQUEST_CODE_VERIFY = 1001;
    private TextView tvSms;
    private TextView tvAudio;
    private TextView tvCountry;
    private EditText etPhone;
    private EditText etCode;
    private TextView tvCode;
    private TextView tvVerify;
    private TextView tvToast;
    private String currentId;
    private String currentPrefix;
    private FakeActivity callback;
    private Toast toast;
    private Handler handler;
    private EventHandler eventHandler;
    private int currentSecond;
    private SharePrefrenceHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.smssdk_verify_activity);
        initViews();
        initListener();

        //默认获取短信和验证按钮不可点击，输入达到规范后，可点击
        tvVerify.setEnabled(false);
        tvCode.setEnabled(false);
        //默认使用短信验证
        tvSms.setSelected(true);
        //默认使用中国区号
        currentId = DEFAULT_COUNTRY[1];
        currentPrefix = DEFAULT_COUNTRY[2];
        tvCountry.setText(getString(R.string.smssdk_default_country) + " +" + DEFAULT_COUNTRY[2]);
        helper = new SharePrefrenceHelper(this);
        helper.open("sms_sp");
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE_VERIFY) {
			etCode.setText("");
			etPhone.setText("");
			// 重置"获取验证码"按钮
			tvCode.setText(R.string.smssdk_get_code);
			tvCode.setEnabled(true);
			if (handler != null) {
				handler.removeCallbacksAndMessages(null);
			}
		}
	}

	private void initViews() {
        tvSms = findViewById(R.id.tvSms);
        tvAudio = findViewById(R.id.tvAudio);
        tvCountry = findViewById(R.id.tvCountry);
        etPhone = findViewById(R.id.etPhone);
        etCode = findViewById(R.id.etCode);
        tvCode = findViewById(R.id.tvCode);
    }

    private void initListener() {
        findViewById(R.id.ivBack).setOnClickListener(this);
        tvSms.setOnClickListener(this);
        tvAudio.setOnClickListener(this);
        findViewById(R.id.ivSelectCountry).setOnClickListener(this);
        tvCode.setOnClickListener(this);
        tvVerify = findViewById(R.id.tvVerify);
        tvVerify.setOnClickListener(this);
        etPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            //手机号输入大于5位，获取验证码按钮可点击
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvCode.setEnabled(etPhone.getText() != null && etPhone.getText().length() > 5);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            //验证码输入6位并且手机大于5位，验证按钮可点击
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvVerify.setEnabled(etCode.getText() != null && etCode.getText().length() >= 6 && etPhone.getText() != null && etPhone.getText().length() > 5);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //回调选中的国家
        callback = new FakeActivity() {
            @Override
            public void onResult(HashMap<String, Object> data) {
                if (data != null) {
                    int page = (Integer) data.get("page");
                    if (page == 1) {
                        currentId = (String) data.get("id");
                        String[] country = SMSSDK.getCountry(currentId);
                        if (country != null) {
                            tvCountry.setText(country[0] + " " + "+" + country[1]);
                            currentPrefix = country[1];
                        }
                    }
                }
            }
        };
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (tvCode != null) {
                    if (currentSecond > 0) {
                        tvCode.setText(getString(R.string.smssdk_get_code) + " (" + currentSecond + "s)");
                        tvCode.setEnabled(false);
                        currentSecond--;
                        handler.sendEmptyMessageDelayed(0, 1000);
                    } else {
                        tvCode.setText(R.string.smssdk_get_code);
                        tvCode.setEnabled(true);
                    }
                }
            }
        };

        eventHandler = new EventHandler() {
            public void afterEvent(final int event, final int result, final Object data) {
                if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //提交验证成功，跳转成功页面，否则toast提示
                            if (result == SMSSDK.RESULT_COMPLETE) {
                                ResultActivity.startActivityForResult(
                                		VerifyActivity.this, REQUEST_CODE_VERIFY, true, "+" + currentPrefix + " " + etPhone.getText());
                            } else {
                                processError(data);
                            }
                        }
                    });
                } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE || event == SMSSDK.EVENT_GET_VOICE_VERIFICATION_CODE) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (result == SMSSDK.RESULT_COMPLETE) {
								currentSecond = COUNTDOWN;
								handler.sendEmptyMessage(0);
								helper.putLong(KEY_START_TIME, System.currentTimeMillis());
							} else {
								if (data != null && (data instanceof UserInterruptException)) {
									// 由于此处是开发者自己决定要中断发送的，因此什么都不用做
									return;
								}
								processError(data);
							}
						}
					});
				}
            }
        };
        SMSSDK.registerEventHandler(eventHandler);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivBack:
                finish();
                break;
            case R.id.tvSms:
                tvSms.setSelected(true);
                tvAudio.setSelected(false);
                break;
            case R.id.tvAudio:
                tvAudio.setSelected(true);
                tvSms.setSelected(false);
                break;
            case R.id.ivSelectCountry:
                //将当前国家带入跳转国家列表
                CountryPage countryPage = new CountryPage();
                countryPage.setCountryId(currentId);
                countryPage.showForResult(VerifyActivity.this, null, callback);
                break;
            case R.id.tvVerify:
                if (!isNetworkConnected()) {
                    Toast.makeText(VerifyActivity.this, getString(R.string.smssdk_network_error), Toast.LENGTH_SHORT).show();
                    break;
                }
                SMSSDK.submitVerificationCode(currentPrefix, etPhone.getText().toString().trim(), etCode.getText().toString());
                break;
            case R.id.tvCode:
                //获取验证码间隔时间小于1分钟，进行toast提示，在当前页面不会有这种情况，但是当点击验证码返回上级页面再进入会产生该情况
                long startTime = helper.getLong(KEY_START_TIME);
                if (System.currentTimeMillis() - startTime < COUNTDOWN * 1000) {
                    showErrorToast(getString(R.string.smssdk_busy_hint));
                    break;
                }
                if (!isNetworkConnected()) {
                    Toast.makeText(VerifyActivity.this, getString(R.string.smssdk_network_error), Toast.LENGTH_SHORT).show();
                    break;
                }
                if (tvSms.isSelected()) {
                    SMSSDK.getVerificationCode(currentPrefix, etPhone.getText().toString().trim(), TEMP_CODE, null);
                } else {
                    SMSSDK.getVoiceVerifyCode(currentPrefix, etPhone.getText().toString().trim());
                }
                currentSecond = COUNTDOWN;
                handler.sendEmptyMessage(0);
                helper.putLong(KEY_START_TIME, System.currentTimeMillis());
                break;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        SMSSDK.unregisterEventHandler(eventHandler);
    }

    private void showErrorToast(String text) {
        if (toast == null) {
            toast = new Toast(this);
            View rootView = LayoutInflater.from(this).inflate(R.layout.smssdk_error_toast_layout, null);
            tvToast = rootView.findViewById(R.id.tvToast);
            toast.setView(rootView);
            toast.setGravity(Gravity.CENTER, 0, ResHelper.dipToPx(this, -100));
        }
        tvToast.setText(text);
        toast.show();
    }


    private boolean isNetworkConnected() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    private void processError(Object data) {
		int status = 0;
		// 根据服务器返回的网络错误，给toast提示
		try {
			((Throwable) data).printStackTrace();
			Throwable throwable = (Throwable) data;

			JSONObject object = new JSONObject(
					throwable.getMessage());
			String des = object.optString("detail");
			status = object.optInt("status");
			if (!TextUtils.isEmpty(des)) {
				showErrorToast(status+":"+des);
				return;
			}
		} catch (Exception e) {
			Log.w(TAG, "", e);
		}
		// 如果木有找到资源，默认提示
		int resId = DemoResHelper.getStringRes(getApplicationContext(),
				"smsdemo_network_error");
		String netErrMsg = getApplicationContext().getResources().getString(resId);
		showErrorToast(netErrMsg);
	}
}

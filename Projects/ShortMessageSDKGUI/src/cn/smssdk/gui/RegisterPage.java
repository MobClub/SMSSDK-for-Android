//#if def{lang} == cn
/*
 * 官网地站:http://www.mob.com
 * 技术支持QQ: 4006852216
 * 官方微信:ShareSDK   （如果发布新版本的话，我们将会第一时间通过微信将版本更新内容推送给您。如果使用过程中有任何问题，
 * 也可以通过微信与我们取得联系，我们将会在24小时内给予回复）
 * 
 * Copyright (c) 2014年 mob.com. All rights reserved.
 */
//#elif def{lang} == en
/*
 * Offical Website:http://www.mob.com
 * Support QQ: 4006852216
 * Offical Wechat Account:ShareSDK   (We will inform you our updated news at the first time by Wechat, if we release a new version.
 * If you get any problem, you can also contact us with Wechat, we will reply you within 24 hours.)
 * 
 * Copyright (c) 2013 mob.com. All rights reserved.
 */
//#endif
package cn.smssdk.gui;

import android.app.Dialog;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mob.tools.FakeActivity;
import com.mob.tools.utils.ResHelper;

import org.json.JSONObject;

import java.util.HashMap;

import cn.smssdk.EventHandler;
import cn.smssdk.OnSendMessageHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.UserInterruptException;
import cn.smssdk.gui.layout.RegisterPageLayout;
import cn.smssdk.gui.layout.SendMsgDialogLayout;
import cn.smssdk.utils.SMSLog;

//#if def{lang} == cn
/** 短信注册页面*/
//#elif def{lang} == en
/** the page of get verification code */
//#endif
public class RegisterPage extends FakeActivity implements OnClickListener,
		TextWatcher {

	//#if def{lang} == cn
	// 默认使用中国区号
	//#elif def{lang} == en
	// the default country-id of china
	//#endif
	private static final String DEFAULT_COUNTRY_ID = "42";

	private EventHandler callback;

	//#if def{lang} == cn
	// 国家
	//#elif def{lang} == en
	// country
	//#endif
	private TextView tvCountry;
	//#if def{lang} == cn
	// 手机号码
	//#elif def{lang} == en
	// phone number
	//#endif
	private EditText etPhoneNum;
	//#if def{lang} == cn
	// 国家编号
	//#elif def{lang} == en
	// country code
	//#endif
	private TextView tvCountryNum;
	//#if def{lang} == cn
	// clear 号码
	//#elif def{lang} == en
	// clear number
	//#endif
	private ImageView ivClear;
	//#if def{lang} == cn
	// 下一步按钮
	//#elif def{lang} == en
	// next button
	//#endif
	private Button btnNext;

	private String currentId;
	private String currentCode;
	private EventHandler handler;
	private Dialog pd;
	private OnSendMessageHandler osmHandler;

	public void setRegisterCallback(EventHandler callback) {
		this.callback = callback;
	}

	public void setOnSendMessageHandler(OnSendMessageHandler h) {
		osmHandler = h;
	}

	public void show(Context context) {
		super.show(context, null);
	}

	public void onCreate() {
		RegisterPageLayout page = new RegisterPageLayout(activity);
		LinearLayout layout = page.getLayout();
		
		if (layout != null) {
			activity.setContentView(layout);
			currentId = DEFAULT_COUNTRY_ID;

			View llBack = activity.findViewById(ResHelper.getIdRes(activity, "ll_back"));
			TextView tv = (TextView) activity.findViewById(ResHelper.getIdRes(activity, "tv_title"));
			int resId = ResHelper.getStringRes(activity, "smssdk_regist");
			if (resId > 0) {
				tv.setText(resId);
			}
			
			View viewCountry = activity.findViewById(ResHelper.getIdRes(activity, "rl_country"));
			btnNext = (Button) activity.findViewById(ResHelper.getIdRes(activity, "btn_next"));
			tvCountry = (TextView) activity.findViewById(ResHelper.getIdRes(activity, "tv_country"));

			String[] country = getCurrentCountry();
			// String[] country = SMSSDK.getCountry(currentId);
			if (country != null) {
				currentCode = country[1];
				tvCountry.setText(country[0]);
			}
			
			tvCountryNum = (TextView) activity.findViewById(ResHelper.getIdRes(activity, "tv_country_num"));
			tvCountryNum.setText("+" + currentCode);

			etPhoneNum = (EditText) activity.findViewById(ResHelper.getIdRes(activity, "et_write_phone"));
			etPhoneNum.setText("");
			//#if def{sdk.debugable}
			etPhoneNum.setText("");
			// etPhoneNum.setText("13242858392");
			//#endif
			etPhoneNum.addTextChangedListener(this);
			etPhoneNum.requestFocus();
			if (etPhoneNum.getText().length() > 0) {
				btnNext.setEnabled(true);
				
				ivClear = (ImageView) activity.findViewById(ResHelper.getIdRes(activity, "iv_clear"));
				ivClear.setVisibility(View.VISIBLE);
				resId = ResHelper.getBitmapRes(activity, "smssdk_btn_enable");
				if (resId > 0) {
					btnNext.setBackgroundResource(resId);
				}
			}

			ivClear = (ImageView) activity.findViewById(ResHelper.getIdRes(activity, "iv_clear"));

			llBack.setOnClickListener(this);
			btnNext.setOnClickListener(this);
			ivClear.setOnClickListener(this);
			viewCountry.setOnClickListener(this);

			handler = new EventHandler() {
				public void afterEvent(final int event, final int result,
						final Object data) {
					runOnUIThread(new Runnable() {
						public void run() {
							if (pd != null && pd.isShowing()) {
								pd.dismiss();
							}
							if (result == SMSSDK.RESULT_COMPLETE) {
								if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
									//#if def{lang} == cn
									// 请求验证码后，跳转到验证码填写页面
									//#elif def{lang} == en
									// go to the IdentifyNumPage when post a
									// getting-verification-code request
									//#endif
									boolean smart = (Boolean)data;
									afterVerificationCodeRequested(smart);
								}
							} else {
								if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE
										&& data != null
										&& (data instanceof UserInterruptException)) {
									//#if def{lang} == cn
									// 由于此处是开发者自己决定要中断发送的，因此什么都不用做
									//#elif def{lang} == en
									// this exception is thown after
									// OnSendMessageHandler.onSendMessage
									// returning true
									//#endif
									return;
								}

								int status = 0;
								//#if def{lang} == cn
								// 根据服务器返回的网络错误，给toast提示
								//#elif def{lang} == en
								// show toast according to the error code
								//#endif
								try {
									((Throwable) data).printStackTrace();
									Throwable throwable = (Throwable) data;

									JSONObject object = new JSONObject(
											throwable.getMessage());
									String des = object.optString("detail");
									status = object.optInt("status");
									if (!TextUtils.isEmpty(des)) {
										Toast.makeText(activity, des, Toast.LENGTH_SHORT).show();
										return;
									}
								} catch (Exception e) {
									SMSLog.getInstance().w(e);
								}
								//#if def{lang} == cn
								// 如果木有找到资源，默认提示
								//#elif def{lang} == en
								// show default error when can't find the
								// resource of string
								//#endif
								int resId = 0;
								if(status >= 400) {
									resId = ResHelper.getStringRes(activity, "smssdk_error_desc_" + status);
								} else {
									resId = ResHelper.getStringRes(activity,
											"smssdk_network_error");
								}
								
								if (resId > 0) {
									Toast.makeText(activity, resId, Toast.LENGTH_SHORT).show();
								}
							}
						}
					});
				}
			};
		}

	}

	private String[] getCurrentCountry() {
		String mcc = getMCC();
		String[] country = null;
		if (!TextUtils.isEmpty(mcc)) {
			country = SMSSDK.getCountryByMCC(mcc);
		}

		if (country == null) {
			SMSLog.getInstance().d("no country found by MCC: " + mcc);
			country = SMSSDK.getCountry(DEFAULT_COUNTRY_ID);
		}
		return country;
	}

	private String getMCC() {
		TelephonyManager tm = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
		//#if def{lang} == cn
		// 返回当前手机注册的网络运营商所在国家的MCC+MNC. 如果没注册到网络就为空.
		//#elif def{lang} == en
		// Returns the numeric name (MCC+MNC) of current registered operator.
		// Availability: Only when user is registered to a network. Result may
		// be unreliable on CDMA networks (use getPhoneType() to determine if on
		// a CDMA network).
		//#endif
		String networkOperator = tm.getNetworkOperator();
		if (!TextUtils.isEmpty(networkOperator)) {
			return networkOperator;
		}

		//#if def{lang} == cn
		// 返回SIM卡运营商所在国家的MCC+MNC. 5位或6位. 如果没有SIM卡返回空
		//#elif def{lang} == en
		// Returns the MCC+MNC (mobile country code + mobile network code) of
		// the provider of the SIM. 5 or 6 decimal digits.
		// Availability: SIM state must be SIM_STATE_READY
		//#endif
		return tm.getSimOperator();
	}

	public void onResume() {
		SMSSDK.registerEventHandler(handler);
	}

	public void onDestroy() {
		SMSSDK.unregisterEventHandler(handler);
	}

	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {

	}

	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if (s.length() > 0) {
			btnNext.setEnabled(true);
			ivClear.setVisibility(View.VISIBLE);
			int resId = ResHelper.getBitmapRes(activity, "smssdk_btn_enable");
			if (resId > 0) {
				btnNext.setBackgroundResource(resId);
			}
		} else {
			btnNext.setEnabled(false);
			ivClear.setVisibility(View.GONE);
			int resId = ResHelper.getBitmapRes(activity, "smssdk_btn_disenable");
			if (resId > 0) {
				btnNext.setBackgroundResource(resId);
			}
		}
	}

	public void afterTextChanged(Editable s) {

	}

	public void onClick(View v) {
		int id = v.getId();
		int idLlBack = ResHelper.getIdRes(activity, "ll_back");
		int idRlCountry = ResHelper.getIdRes(activity, "rl_country");
		int idBtnNext = ResHelper.getIdRes(activity, "btn_next");
		int idIvClear = ResHelper.getIdRes(activity, "iv_clear");

		if (id == idLlBack) {
			finish();
		} else if (id == idRlCountry) {
			//#if def{lang} == cn
			// 国家列表
			//#elif def{lang} == en
			// country list
			//#endif
			CountryPage countryPage = new CountryPage();
			countryPage.setCountryId(currentId);
			countryPage.showForResult(activity, null, this);
		} else if (id == idBtnNext) {
			//#if def{lang} == cn
			// 请求发送短信验证码
			//#elif def{lang} == en
			// request to send verification code
			//#endif
			String phone = etPhoneNum.getText().toString().trim().replaceAll("\\s*", "");
			String code = tvCountryNum.getText().toString().trim();
			showDialog(phone, code);
		} else if (id == idIvClear) {
			//#if def{lang} == cn
			// 清除电话号码输入框
			//#elif def{lang} == en
			// clear phone number
			//#endif
			etPhoneNum.getText().clear();
		}
	}

	@SuppressWarnings("unchecked")
	public void onResult(HashMap<String, Object> data) {
		if (data != null) {
			int page = (Integer) data.get("page");
			if (page == 1) {
				//#if def{lang} == cn
				// 国家列表返回
				//#elif def{lang} == en
				// return country list
				//#endif
				currentId = (String) data.get("id");
				String[] country = SMSSDK.getCountry(currentId);
				if (country != null) {
					currentCode = country[1];
					tvCountryNum.setText("+" + currentCode);
					tvCountry.setText(country[0]);
				}
			} else if (page == 2) {
				//#if def{lang} == cn
				// 验证码校验返回
				//#elif def{lang} == en
				// return verification code
				//#endif
				Object res = data.get("res");
				//Object smart = data.get("smart");
				
				HashMap<String, Object> phoneMap = (HashMap<String, Object>) data.get("phone");
				if (res != null && phoneMap != null) {
					int resId = ResHelper.getStringRes(activity, "smssdk_your_ccount_is_verified");
					if (resId > 0) {
						Toast.makeText(activity, resId, Toast.LENGTH_SHORT).show();
					}

					if (callback != null) {
						callback.afterEvent(
								SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE,
								SMSSDK.RESULT_COMPLETE, phoneMap);
					}
					finish();
				}
			}
		}
	}

	//#if def{lang} == cn
	/** 分割电话号码 */
	//#elif def{lang} == en
	/** split phone number */
	//#endif
	private String splitPhoneNum(String phone) {
		StringBuilder builder = new StringBuilder(phone);
		builder.reverse();
		for (int i = 4, len = builder.length(); i < len; i += 5) {
			builder.insert(i, ' ');
		}
		builder.reverse();
		return builder.toString();
	}

	//#if def{lang} == cn
	/** 是否请求发送验证码，对话框 */
	//#elif def{lang} == en
	/** Whether the request to send verification code */
	//#endif
	public void showDialog(final String phone, final String code) {
		int resId = ResHelper.getStyleRes(activity, "CommonDialog");
		if (resId > 0) {
			final String phoneNum = code + " " + splitPhoneNum(phone);
			final Dialog dialog = new Dialog(getContext(), resId);
			
			LinearLayout layout = SendMsgDialogLayout.create(getContext());
			
			if (layout != null) {
				dialog.setContentView(layout);
				
				((TextView) dialog.findViewById(ResHelper.getIdRes(activity, "tv_phone"))).setText(phoneNum);
				TextView tv = (TextView) dialog.findViewById(ResHelper.getIdRes(activity, "tv_dialog_hint"));
				resId = ResHelper.getStringRes(activity, "smssdk_make_sure_mobile_detail");
				if (resId > 0) {
					String text = getContext().getString(resId);
					
					tv.setText(Html.fromHtml(text));
				}

				((Button) dialog.findViewById(ResHelper.getIdRes(activity, "btn_dialog_ok"))).setOnClickListener(
						new OnClickListener() {
								public void onClick(View v) {
									//#if def{lang} == cn
									// 跳转到验证码页面
									//#elif def{lang} == en
									// jump to verification code page
									//#endif
									dialog.dismiss();

									if (pd != null && pd.isShowing()) {
										pd.dismiss();
									}
									pd = CommonDialog.ProgressDialog(activity);
									if (pd != null) {
										pd.show();
									}
									SMSLog.getInstance().i("verification phone ==>>" + phone);
									SMSLog.getInstance().i("verification tempCode ==>>" + IdentifyNumPage.TEMP_CODE);
									SMSSDK.getVerificationCode(code, phone.trim(), IdentifyNumPage.TEMP_CODE, osmHandler);
								}
						});
				
				
				((Button) dialog.findViewById(ResHelper.getIdRes(activity, "btn_dialog_cancel"))).setOnClickListener(
						new OnClickListener() {
							public void onClick(View v) {
								dialog.dismiss();
							}
						});
				dialog.setCanceledOnTouchOutside(true);
				dialog.show();
			}
		}
	}

	//#if def{lang} == cn
	/** 请求验证码后，跳转到验证码填写页面 */
	//#elif def{lang} == en
	/** jump to verification code page after request verification code */
	//#endif
	private void afterVerificationCodeRequested(boolean smart) {
		String phone = etPhoneNum.getText().toString().trim().replaceAll("\\s*", "");
		String code = tvCountryNum.getText().toString().trim();
		if (code.startsWith("+")) {
			code = code.substring(1);
		}
		String formatedPhone = "+" + code + " " + splitPhoneNum(phone);
		
		//#if def{lang} == cn
		// 验证码页面
		//#elif def{lang} == en
		// verfication code page
		//#endif
		if(smart) {
			SmartVerifyPage smartPage = new SmartVerifyPage();
			smartPage.setPhone(phone, code, formatedPhone);
			smartPage.showForResult(activity, null, this);
		} else {
			IdentifyNumPage page = new IdentifyNumPage();
			page.setPhone(phone, code, formatedPhone);
			page.showForResult(activity, null, this);
		}
	}

}

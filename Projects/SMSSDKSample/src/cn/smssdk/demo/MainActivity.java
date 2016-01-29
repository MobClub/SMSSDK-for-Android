/*
 * 官网地站:http://www.mob.com
 * 技术支持QQ: 4006852216
 * 官方微信:ShareSDK   （如果发布新版本的话，我们将会第一时间通过微信将版本更新内容推送给您。如果使用过程中有任何问题，也可以通过微信与我们取得联系，我们将会在24小时内给予回复）
 *
 * Copyright (c) 2014年 mob.com. All rights reserved.
 */
package cn.smssdk.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.gui.CommonDialog;
import cn.smssdk.gui.ContactsPage;
import cn.smssdk.gui.RegisterPage;

//请注意：测试短信条数限制发送数量：20条/天，APP开发完成后请到mob.com后台提交审核，获得不限制条数的免费短信权限。
public class MainActivity extends Activity implements OnClickListener, Callback {
	// 填写从短信SDK应用后台注册得到的APPKEY
	//此APPKEY仅供测试使用，且不定期失效，请到mob.com后台申请正式APPKEY
	private static String APPKEY = "f3fc6baa9ac4";

	// 填写从短信SDK应用后台注册得到的APPSECRET
	private static String APPSECRET = "7f3dedcb36d92deebcb373af921d635a";

	// 短信注册，随机产生头像
	private static final String[] AVATARS = {
		"http://tupian.qqjay.com/u/2011/0729/e755c434c91fed9f6f73152731788cb3.jpg",
		"http://99touxiang.com/public/upload/nvsheng/125/27-011820_433.jpg",
		"http://img1.touxiang.cn/uploads/allimg/111029/2330264224-36.png",
		"http://img1.2345.com/duoteimg/qqTxImg/2012/04/09/13339485237265.jpg",
		"http://diy.qqjay.com/u/files/2012/0523/f466c38e1c6c99ee2d6cd7746207a97a.jpg",
		"http://img1.touxiang.cn/uploads/20121224/24-054837_708.jpg",
		"http://img1.touxiang.cn/uploads/20121212/12-060125_658.jpg",
		"http://img1.touxiang.cn/uploads/20130608/08-054059_703.jpg",
		"http://diy.qqjay.com/u2/2013/0422/fadc08459b1ef5fc1ea6b5b8d22e44b4.jpg",
		"http://img1.2345.com/duoteimg/qqTxImg/2012/04/09/13339510584349.jpg",
		"http://img1.touxiang.cn/uploads/20130515/15-080722_514.jpg",
		"http://diy.qqjay.com/u2/2013/0401/4355c29b30d295b26da6f242a65bcaad.jpg"
	};

	private boolean ready;
	private boolean gettingFriends;
	private Dialog pd;
	private TextView tvNum;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		Button btnRegist = (Button) findViewById(R.id.btn_bind_phone);
		View btnContact = findViewById(R.id.rl_contact);
		tvNum = (TextView) findViewById(R.id.tv_num);
		tvNum.setVisibility(View.GONE);
		btnRegist.setOnClickListener(this);
		btnContact.setOnClickListener(this);
		gettingFriends = false;

		loadSharePrefrence();
		if (Build.VERSION.SDK_INT >= 23) {
			int readPhone = checkSelfPermission(Manifest.permission.READ_PHONE_STATE);
			int readSms = checkSelfPermission(Manifest.permission.READ_SMS);
			int readContacts = checkSelfPermission(Manifest.permission.READ_CONTACTS);
			int readSdcard = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);

			int requestCode = 0;
			ArrayList<String> permissions = new ArrayList<String>();
			if (readPhone != PackageManager.PERMISSION_GRANTED) {
				requestCode |= 1 << 0;
				permissions.add(Manifest.permission.READ_PHONE_STATE);
			}
			if (readSms != PackageManager.PERMISSION_GRANTED) {
				requestCode |= 1 << 1;
				permissions.add(Manifest.permission.READ_SMS);
			}
			if (readContacts != PackageManager.PERMISSION_GRANTED) {
				requestCode |= 1 << 2;
				permissions.add(Manifest.permission.READ_CONTACTS);
			}
			if (readSdcard != PackageManager.PERMISSION_GRANTED) {
				requestCode |= 1 << 3;
				permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
			}
			if (requestCode > 0) {
				String[] permission = new String[permissions.size()];
				this.requestPermissions(permissions.toArray(permission), requestCode);
				return;
			}
		}

		showAppkeyDialog();

	}

	private void showAppkeyDialog() {
		final Dialog dialog = new Dialog(this, R.style.CommonDialog);
		dialog.setContentView(R.layout.smssdk_set_appkey_dialog);
		final EditText etAppKey = (EditText) dialog.findViewById(R.id.et_appkey);
		etAppKey.setText(APPKEY);
		final EditText etAppSecret = (EditText) dialog.findViewById(R.id.et_appsecret);
		etAppSecret.setText(APPSECRET);
		OnClickListener ocl = new OnClickListener() {
			public void onClick(View v) {
				if (v.getId() == R.id.btn_dialog_ok) {
					APPKEY = etAppKey.getText().toString().trim();
					APPSECRET = etAppSecret.getText().toString().trim();
					if (TextUtils.isEmpty(APPKEY) || TextUtils.isEmpty(APPSECRET)) {
						Toast.makeText(v.getContext(), R.string.smssdk_appkey_dialog_title,
								Toast.LENGTH_SHORT).show();
					} else {
						dialog.dismiss();
						setSharePrefrence();
						initSDK();
					}
				} else {
					dialog.dismiss();
					finish();
				}
			}
		};
		dialog.findViewById(R.id.btn_dialog_ok).setOnClickListener(ocl);
		dialog.findViewById(R.id.btn_dialog_cancel).setOnClickListener(ocl);
		dialog.setCancelable(false);
		dialog.show();
	}

	private void initSDK() {
		// 初始化短信SDK
		SMSSDK.initSDK(this, APPKEY, APPSECRET, true);
		if (APPKEY.equalsIgnoreCase("f3fc6baa9ac4") ) {
			Toast.makeText(this,"此APPKEY仅供测试使用，且不定期失效，请到mob.com后台申请正式APPKEY",Toast.LENGTH_SHORT).show();
		}
		final Handler handler = new Handler(this);
		EventHandler eventHandler = new EventHandler() {
			public void afterEvent(int event, int result, Object data) {
				Message msg = new Message();
				msg.arg1 = event;
				msg.arg2 = result;
				msg.obj = data;
				handler.sendMessage(msg);
			}
		};
		// 注册回调监听接口
		SMSSDK.registerEventHandler(eventHandler);
		ready = true;

		// 获取新好友个数
		showDialog();
		SMSSDK.getNewFriendsCount();
		gettingFriends = true;
	}

	private void loadSharePrefrence() {
		SharedPreferences p = getSharedPreferences("SMSSDK_SAMPLE", Context.MODE_PRIVATE);
		APPKEY = p.getString("APPKEY", APPKEY);
		APPSECRET = p.getString("APPSECRET", APPSECRET);
	}

	private void setSharePrefrence() {
		SharedPreferences p = getSharedPreferences("SMSSDK_SAMPLE", Context.MODE_PRIVATE);
		Editor edit = p.edit();
		edit.putString("APPKEY", APPKEY);
		edit.putString("APPSECRET", APPSECRET);
		edit.commit();
	}

	protected void onDestroy() {
		if (ready) {
			// 销毁回调监听接口
			SMSSDK.unregisterAllEventHandler();
		}
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (ready && !gettingFriends) {
			// 获取新好友个数
			showDialog();
			SMSSDK.getNewFriendsCount();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_bind_phone:
			// 打开注册页面
			RegisterPage registerPage = new RegisterPage();
			registerPage.setRegisterCallback(new EventHandler() {
				public void afterEvent(int event, int result, Object data) {
					// 解析注册结果
					if (result == SMSSDK.RESULT_COMPLETE) {
						@SuppressWarnings("unchecked")
						HashMap<String,Object> phoneMap = (HashMap<String, Object>) data;
						String country = (String) phoneMap.get("country");
						String phone = (String) phoneMap.get("phone");
						// 提交用户信息
						registerUser(country, phone);
					}
				}
			});
			registerPage.show(this);
			break;
		case R.id.rl_contact:
			tvNum.setVisibility(View.GONE);
			// 打开通信录好友列表页面
			ContactsPage contactsPage = new ContactsPage();
			contactsPage.show(this);
			break;
		}
	}

	public boolean handleMessage(Message msg) {
		if (pd != null && pd.isShowing()) {
			pd.dismiss();
		}

		int event = msg.arg1;
		int result = msg.arg2;
		Object data = msg.obj;
		if (event == SMSSDK.EVENT_SUBMIT_USER_INFO) {
			// 短信注册成功后，返回MainActivity,然后提示新好友
			if (result == SMSSDK.RESULT_COMPLETE) {
				Toast.makeText(this, R.string.smssdk_user_info_submited, Toast.LENGTH_SHORT).show();
			} else {
				((Throwable) data).printStackTrace();
			}
		} else if (event == SMSSDK.EVENT_GET_NEW_FRIENDS_COUNT){
			if (result == SMSSDK.RESULT_COMPLETE) {
				refreshViewCount(data);
				gettingFriends = false;
			} else {
				((Throwable) data).printStackTrace();
			}
		}
		return false;
	}
	// 更新，新好友个数
	private void refreshViewCount(Object data){
		int newFriendsCount = 0;
		try {
			newFriendsCount = Integer.parseInt(String.valueOf(data));
		} catch (Throwable t) {
			newFriendsCount = 0;
		}
		if(newFriendsCount > 0){
			tvNum.setVisibility(View.VISIBLE);
			tvNum.setText(String.valueOf(newFriendsCount));
		}else{
			tvNum.setVisibility(View.GONE);
		}
		if (pd != null && pd.isShowing()) {
			pd.dismiss();
		}
	}
	// 弹出加载框
	private void showDialog(){
		if (pd != null && pd.isShowing()) {
			pd.dismiss();
		}
		pd = CommonDialog.ProgressDialog(this);
		pd.show();
	}
	// 提交用户信息
	private void registerUser(String country, String phone) {
		Random rnd = new Random();
		int id = Math.abs(rnd.nextInt());
		String uid = String.valueOf(id);
		String nickName = "SmsSDK_User_" + uid;
		String avatar = AVATARS[id % 12];
		SMSSDK.submitUserInfo(uid, nickName, avatar, country, phone);
	}

	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		showAppkeyDialog();
	}
}

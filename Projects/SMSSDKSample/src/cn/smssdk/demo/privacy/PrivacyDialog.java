package cn.smssdk.demo.privacy;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Method;

import cn.smssdk.demo.util.DemoResHelper;

public class PrivacyDialog extends AlertDialog {
	private static final String TAG = "PrivacyDialog";
	private View view;
	private Context context;
	private int width;
	private TextView titleTv;
	private TextView contentTv;
	private TextView rejectTv;
	private TextView acceptTv;
	private OnDialogListener listener;

	public PrivacyDialog(Context context, OnDialogListener onDialogListener) {
		super(context, DemoResHelper.getStyleRes(context, "smsdemo_DialogStyle"));
		this.context = context;
		this.listener = onDialogListener;
		int orientation = this.context.getResources().getConfiguration().orientation;
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			double deviceHeight = getScreenHeight(this.context);
			width = (int) (deviceHeight * 0.7);
		} else {
			double deviceWidth = getScreenWidth(this.context);
			width = (int) (deviceWidth * 0.7);
		}
		setCancelable(false);
		setCanceledOnTouchOutside(false);
		LayoutInflater inflater = LayoutInflater.from(this.context);
		view = inflater.inflate(DemoResHelper.getLayoutRes(context, "smsdemo_authorize_dialog"), null);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams
				.WRAP_CONTENT, 0);
		setContentView(view, params);
		initView();
		initEvents();
	}

	private void initView() {
		this.titleTv = (TextView) view.findViewById(DemoResHelper.getIdRes(context, "smsdemo_authorize_dialog_title_tv"));
		this.contentTv = (TextView) view.findViewById(DemoResHelper.getIdRes(context, "smsdemo_authorize_dialog_content_tv"));
		this.acceptTv = (TextView) view.findViewById(DemoResHelper.getIdRes(context, "smsdemo_authorize_dialog_accept_tv"));
		this.rejectTv = (TextView) view.findViewById(DemoResHelper.getIdRes(context, "smsdemo_authorize_dialog_reject_tv"));

		if (this.contentTv != null) {
			this.contentTv.setMovementMethod(LinkMovementMethod.getInstance());
		}
		String privacyMainContent = context.getResources().getString(
				DemoResHelper.getStringRes(context, "smsdemo_authorize_dialog_content"));
		String privacyUrl = PrivacyHolder.getInstance().getPrivacyUrl();
		String privacy = privacyMainContent + " <a href=\"" + privacyUrl + "\">" + privacyUrl + "</a>";
		this.contentTv.setText(Html.fromHtml(privacy));
	}

	private void initEvents() {
		this.acceptTv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isShowing()) {
					dismiss();
				}
				if (listener != null) {
					listener.onAgree();
				}
			}
		});

		this.rejectTv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isShowing()) {
					dismiss();
				}
				if (listener != null) {
					listener.onDisagree();
				}
			}
		});
	}

	private int getScreenWidth(Context context) {
		return getScreenSize(context)[0];
	}

	private int getScreenHeight(Context context) {
		return getScreenSize(context)[1];
	}

	private int[] getScreenSize(Context context) {
		WindowManager windowManager;
		try {
			windowManager = (WindowManager)context.getSystemService("window");
		} catch (Throwable t) {
			Log.d(TAG, "get SCreenSize Exception", t);
			windowManager = null;
		}

		if(windowManager == null) {
			return new int[]{0, 0};
		} else {
			Display display = windowManager.getDefaultDisplay();
			if(Build.VERSION.SDK_INT < 13) {
				DisplayMetrics t1 = new DisplayMetrics();
				display.getMetrics(t1);
				return new int[]{t1.widthPixels, t1.heightPixels};
			} else {
				try {
					Point t = new Point();
					Method method = display.getClass().getMethod("getRealSize", new Class[]{Point.class});
					method.setAccessible(true);
					method.invoke(display, new Object[]{t});
					return new int[]{t.x, t.y};
				} catch (Throwable t) {
					Log.d(TAG, "get SCreenSize Exception", t);
					return new int[]{0, 0};
				}
			}
		}
	}
}


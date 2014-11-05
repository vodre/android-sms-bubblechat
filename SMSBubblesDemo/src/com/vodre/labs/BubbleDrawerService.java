package com.vodre.labs;


import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BubbleDrawerService extends Service {

	private WindowManager mWindowManager;
	private View mChatHead;
	private ImageView mChaContactImageView;
	private TextView mChatTextView;
	private TextView mChatContactTextView;
	private LinearLayout mLayout;
	private LinearLayout layout_text;
	private static int screenWidth;
	private static int screenHeight;
	private static String _message = "";
	private static String _senderNum = "";

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent.hasExtra("senderNum") && intent.hasExtra("message")) {
			Bundle extras = intent.getExtras();
			_message = extras.getString("message");
			_senderNum = extras.getString("senderNum");
			openWindow();
		}
		return super.onStartCommand(intent, flags, startId);

	}

	@SuppressLint("NewApi") 
	private void openWindow() {
		mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		Display display = mWindowManager.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		screenWidth = size.x;
		screenHeight = size.y;

		LayoutInflater inflater = LayoutInflater.from(this);
		mChatHead = inflater.inflate(R.layout.chatbubble_view, null);
		mChaContactImageView = (ImageView) mChatHead.findViewById(R.id.chathead_imageview);
		mChatContactTextView = (TextView) mChatHead.findViewById(R.id.contactName_textview);
		mChatTextView = (TextView) mChatHead.findViewById(R.id.text_textview);
		mLayout = (LinearLayout) mChatHead.findViewById(R.id.chathead_linearlayout);
		layout_text = (LinearLayout)mChatHead.findViewById(R.id.layout_text);
		
		
		mChatContactTextView.setText(_senderNum + ": ");
		mChatTextView.setText(_message);

		final WindowManager.LayoutParams parameters = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT, // Width
				WindowManager.LayoutParams.WRAP_CONTENT, // Height
				WindowManager.LayoutParams.TYPE_PHONE, // Type
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, // Flag
				PixelFormat.TRANSLUCENT // Format
				);

		parameters.x = 0;
		parameters.y = 50;
		parameters.gravity = Gravity.TOP | Gravity.LEFT;

		// Drag support!
		mChaContactImageView.setOnTouchListener(new OnTouchListener() {

			int initialX, initialY;
			float initialTouchX, initialTouchY;

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					initialX = parameters.x;
					initialY = parameters.y;
					initialTouchX = event.getRawX();
					initialTouchY = event.getRawY();
					return true;

				case MotionEvent.ACTION_MOVE:
					layout_text.setVisibility(View.GONE);
					parameters.x = initialX
							+ (int) (event.getRawX() - initialTouchX);
					parameters.y = initialY
							+ (int) (event.getRawY() - initialTouchY);
					mWindowManager.updateViewLayout(mChatHead, parameters);
					return true;

				case MotionEvent.ACTION_UP:

					if (parameters.y > screenHeight * 0.6) {
						layout_text.setVisibility(View.GONE);
						stopSelf();
					}

					if (parameters.x < screenWidth / 2) {
						mLayout.removeAllViews();
						mLayout.addView(mChaContactImageView);
						mLayout.addView(layout_text);
						layout_text.setVisibility(View.VISIBLE);

					} else { // Set textView to left of image
						mLayout.removeAllViews();
						mLayout.addView(layout_text);
						mLayout.addView(mChaContactImageView);
						layout_text.setVisibility(View.VISIBLE);
					}
					return true;

				default:
					return false;
				}
			}
		});

		mWindowManager.addView(mChatHead, parameters);

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mChatHead != null)
			mWindowManager.removeView(mChatHead);
	}
}

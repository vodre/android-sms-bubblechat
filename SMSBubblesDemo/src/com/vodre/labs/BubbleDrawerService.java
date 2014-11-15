package com.vodre.labs;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.telephony.PhoneNumberUtils;
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
	private static final String[] PHOTO_ID_PROJECTION = new String[] {ContactsContract.Contacts.PHOTO_ID};
	private static final String[] PHOTO_BITMAP_PROJECTION = new String[] {ContactsContract.CommonDataKinds.Photo.PHOTO};

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


		setContactView();


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

	private void setContactView() {
		_senderNum = _senderNum + " dice:";
		mChatContactTextView.setText(getContactNamebyNumber(_senderNum)); ///set Contact Name in TextView
		mChatTextView.setText(_message);//set Message in TextView

		final Integer thumbnailId = fetchThumbnailId(_senderNum);
		if (thumbnailId != null) {
			final Bitmap thumbnail = fetchThumbnail(thumbnailId);
			if (thumbnail != null) {
				mChaContactImageView.setImageBitmap(thumbnail);
			}
		}

	}



	private Integer fetchThumbnailId(String phoneNumber) {

		final Uri uri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
		final Cursor cursor = getContentResolver().query(uri, PHOTO_ID_PROJECTION, null, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC");

		try {
			Integer thumbnailId = null;
			if (cursor.moveToFirst()) {
				thumbnailId = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_ID));
			}
			return thumbnailId;
		}
		finally {
			cursor.close();
		}

	}


	final Bitmap fetchThumbnail(final int thumbnailId) {

		final Uri uri = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, thumbnailId);
		final Cursor cursor = getContentResolver().query(uri, PHOTO_BITMAP_PROJECTION, null, null, null);

		try {
			Bitmap thumbnail = null;
			if (cursor.moveToFirst()) {
				final byte[] thumbnailBytes = cursor.getBlob(0);
				if (thumbnailBytes != null) {
					thumbnail = BitmapFactory.decodeByteArray(thumbnailBytes, 0, thumbnailBytes.length);
				}
			}
			return thumbnail;
		}
		finally {
			cursor.close();

		}
	}



	private CharSequence getContactNamebyNumber(String smsNumber) {
		Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
		String contact_name = smsNumber;
		while (phones.moveToNext())
		{
			String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
			String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)); 

			if(PhoneNumberUtils.compare(phoneNumber, smsNumber)){
				contact_name = name;
			}

		}
		phones.close();
		return contact_name;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mChatHead != null)
			mWindowManager.removeView(mChatHead);
	}
}

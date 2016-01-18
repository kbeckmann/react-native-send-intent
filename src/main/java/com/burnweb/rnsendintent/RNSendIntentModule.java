package com.burnweb.rnsendintent;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Environment;

import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.lang.SecurityException;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class RNSendIntentModule extends ReactContextBaseJavaModule {

    private static final String TAG = RNSendIntentModule.class.getSimpleName();

    private static final String TEXT_PLAIN = "text/plain";
    private static final String TEXT_HTML = "text/html";

    private ReactApplicationContext reactContext;

    public RNSendIntentModule(ReactApplicationContext reactContext) {
      super(reactContext);
      this.reactContext = reactContext;
    }

    @Override
    public String getName() {
      return "SendIntentAndroid";
    }

    @Override
    public Map<String, Object> getConstants() {
      final Map<String, Object> constants = new HashMap<>();
      constants.put("TEXT_PLAIN", TEXT_PLAIN);
      constants.put("TEXT_HTML", TEXT_HTML);
      return constants;
    }

    private Intent getSendIntent(String text, String type) {
      Intent sendIntent = new Intent();
      sendIntent.setAction(Intent.ACTION_SEND);
      sendIntent.putExtra(Intent.EXTRA_TEXT, text);
      sendIntent.setType(type);
      sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

      return sendIntent;
    }

    @ReactMethod
    public void sendPhoneCall(String phoneNumberString) {
      //Needs permission "android.permission.CALL_PHONE"
      Intent sendIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumberString.trim()));
      sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

      //Check that an app exists to receive the intent
      if (sendIntent.resolveActivity(this.reactContext.getPackageManager()) != null) {
        try {
          this.reactContext.startActivity(sendIntent);
        } catch(SecurityException ex) {
          Log.d(TAG, ex.getMessage());

          this.sendPhoneDial(phoneNumberString);
        }
      }
    }

    @ReactMethod
    public void sendPhoneDial(String phoneNumberString) {
      Intent sendIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumberString.trim()));
      sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

      //Check that an app exists to receive the intent
      if (sendIntent.resolveActivity(this.reactContext.getPackageManager()) != null) {
        this.reactContext.startActivity(sendIntent);
      }
    }

    @ReactMethod
    public void sendSms(String phoneNumberString, String body) {
      Intent sendIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + phoneNumberString.trim()));
      sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

      if (body != null) {
        sendIntent.putExtra("sms_body", body);
      }

      //Check that an app exists to receive the intent
      if (sendIntent.resolveActivity(this.reactContext.getPackageManager()) != null) {
        this.reactContext.startActivity(sendIntent);
      }
    }

    @ReactMethod
    public void sendText(String text, String type) {
      Intent sendIntent = this.getSendIntent(text, type);

      //Check that an app exists to receive the intent
      if (sendIntent.resolveActivity(this.reactContext.getPackageManager()) != null) {
        this.reactContext.startActivity(sendIntent);
      }
    }

    @ReactMethod
    public void sendTextWithTitle(String title, String text, String type) {
      Intent sendIntent = this.getSendIntent(text, type);

      //Check that an app exists to receive the intent
      if (sendIntent.resolveActivity(this.reactContext.getPackageManager()) != null) {
        Intent ni = Intent.createChooser(sendIntent, title);
        ni.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        this.reactContext.startActivity(ni);
      }
    }

    @ReactMethod
    public void sendView(String pkg, String uri, String type) {
        Intent intent = new Intent();
        intent.setPackage(pkg);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setType(type);
        intent.setData(Uri.parse(uri));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(this.reactContext.getPackageManager()) != null) {
            this.reactContext.startActivity(intent);
        }
    }

    @ReactMethod
    public void canSendView(String pkg, String uri, String type, Callback callback) {
        Intent intent = new Intent();
        intent.setPackage(pkg);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setType(type);
        intent.setData(Uri.parse(uri));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        callback.invoke(intent.resolveActivity(this.reactContext.getPackageManager()) != null);
    }










    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "MD_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/tmp/");

        // Create /sdcard/tmp if it's not there
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }

        File image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",         /* suffix */
            storageDir      /* directory */
        );

        return image;
    }

    @ReactMethod
    public void startCamera(Callback callback) {
      String ret = "";
      Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
      takePictureIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      // Ensure that there's a camera activity to handle the intent
      if (takePictureIntent.resolveActivity(this.reactContext.getPackageManager()) != null) {
          // Create the File where the photo should go
          File photoFile = null;
          try {
              photoFile = createImageFile();
          } catch (IOException ex) {
              // Error occurred while creating the File

          }
          // Continue only if the File was successfully created
          if (photoFile != null) {
              takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
              this.reactContext.startActivity(takePictureIntent);

              ret = photoFile.getAbsolutePath();
          }
      }

      callback.invoke(ret);
    }
}

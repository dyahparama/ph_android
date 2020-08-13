package com.mge.pettycash;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.provider.MediaStore;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Parcelable;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.ContentValues.TAG;


public class MainActivity extends AppCompatActivity {

    int REQUEST_CHECK_SETTINGS = 100;
    WebView webView;
    WebView splash;
    public ProgressDialog progressDialog;
    private static final int INPUT_FILE_REQUEST_CODE = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    private Uri mCapturedImageURI = null;
    private String mCameraPhotoPath;
    private ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> uploadMessage;
    public static final int REQUEST_SELECT_FILE = 100;
    private final static int FILECHOOSER_RESULTCODE = 1;
    //SwipeRefreshLayout mySwipeRefreshLayout;
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.e(TAG, "Lolipop");
            Log.e(TAG, resultCode +"");
            if (requestCode != INPUT_FILE_REQUEST_CODE || uploadMessage == null) {
                Log.e(TAG, "Masuk Kosong");
                super.onActivityResult(requestCode, resultCode, intent);
                return;
            }
            Uri[] results = null;
            // Check that the response is a good one
            if (resultCode == Activity.RESULT_OK) {
                if (intent == null) {
                    // If there is not data, then we may have taken a photo
                    if (mCameraPhotoPath != null) {
                        results = new Uri[]{Uri.parse(mCameraPhotoPath)};
                        uploadMessage.onReceiveValue(results);
                    }
                    Log.e(TAG, "intent null");
                } else {
//                    String dataString = intent.getDataString();
////                    if (dataString != null) {
////                        results = new Uri[]{Uri.parse(dataString)};
////                        Log.e(TAG, "masuk data String");
////                    }

                    if(intent.getClipData() != null) {
                        int count = intent.getClipData().getItemCount();
                        int currentItem = 0;
                        Log.e(TAG, "masuk clip");
                        ClipData mClipData = intent.getClipData();

                            results = new Uri[mClipData.getItemCount()];
                            for (int i = 0; i < mClipData.getItemCount(); i++) {
                                ClipData.Item mItem = mClipData.getItemAt(i);
                                results[i] = mItem.getUri();
                                //results = new Uri[]{mItem.getUri()};
                                Log.d("this is my array", "arr: " + i + results[i]);
                            }
                        uploadMessage.onReceiveValue(results);

                    } else if(intent.getData() != null) {
                        String imagePath = intent.getDataString();
                        results = new Uri[]{Uri.parse(imagePath)};
                        Log.d("this is my array", "arr: " + Uri.parse(imagePath));
                        uploadMessage.onReceiveValue(results);
                        Log.e(TAG, "masuk biasa");
                    }

                    mUploadMessage = null;
                }

            }else{
                uploadMessage.onReceiveValue(new Uri[]{});
            }

            uploadMessage = null;
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            Log.e(TAG, "Kitkat");
            if (requestCode != FILECHOOSER_RESULTCODE || mUploadMessage == null) {
                super.onActivityResult(requestCode, resultCode, intent);
                return;
            }
            if (requestCode == FILECHOOSER_RESULTCODE) {
                if (null == this.mUploadMessage) {
                    return;
                }
                Uri result = null;
                try {
                    if (resultCode != RESULT_OK) {
                        result = null;
                    } else {
                        // retrieve from the private variable if the intent is null
                        result = intent == null ? mCapturedImageURI : intent.getData();
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "activity :" + e,
                            Toast.LENGTH_LONG).show();
                }
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            }
        }
        return;
    }
    public class JS_INTERFACE {

        Context mContext;

        /** Instantiate the interface and set the context */
        JS_INTERFACE(Context c) {
            mContext = c;
        }

        /** Show a toast from the web page */
        @JavascriptInterface
        public void check() {

        }

        /** Show a toast from the web page */
        @JavascriptInterface
        public String getVersion() {
            return "1.2.6";
        }

        @JavascriptInterface
        public void gpsDialog() {

            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                //Toast.makeText(mContext, "GPS is Enabled in your devide", Toast.LENGTH_SHORT).show();

            }else{
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
                alertDialogBuilder.setMessage("GPS tidak aktif. Aktifkan GPS?")
                        .setCancelable(false)
                        .setPositiveButton("Masuk pengaturan GPS",
                                new DialogInterface.OnClickListener(){
                                    public void onClick(DialogInterface dialog, int id){
                                        Intent callGPSSettingIntent = new Intent(
                                                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                        startActivity(callGPSSettingIntent);
                                    }
                                });
                alertDialogBuilder.setNegativeButton("Kembali",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                dialog.cancel();
                            }

                        });
                AlertDialog alert = alertDialogBuilder.create();
                alert.show();
            }
        }

        @JavascriptInterface
        public void clearCc() {
            ((Activity)mContext).runOnUiThread(new Runnable()
            {
                public void run()
                {
                    webView.reload();
                    webView.clearCache(true);
                }
            });
        }

        @JavascriptInterface
        public void runNotif(int petugasID) {
            SharedPreferences sharedPreferences
                    = getSharedPreferences("MySharedPref",
                    MODE_PRIVATE);

            SharedPreferences.Editor myEdit
                    = sharedPreferences.edit();

            myEdit.putInt(
                    "petugasID",
                    petugasID);

            myEdit.commit();
            Intent intentServive = new Intent(mContext, service.class);
            try {
                mContext.startService(intentServive);
            } catch (Exception e1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mContext.startForegroundService(intentServive);
                } else {
                    //Crashlytics.log("crash for first time, trying another.");
                    mContext.startService(intentServive);
                }
            }
        }
        @JavascriptInterface
        public void stopNotif() {
            SharedPreferences sharedPreferences
                    = getSharedPreferences("MySharedPref",
                    MODE_PRIVATE);

            SharedPreferences.Editor myEdit
                    = sharedPreferences.edit();

            myEdit.putInt(
                    "petugasID",
                    0);

            myEdit.commit();


            int petugasID = sharedPreferences.getInt("petugasID", 0);
            Log.d(TAG, "petugasLogOut: "+petugasID);

        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //mySwipeRefreshLayout = this.findViewById(R.id.swipeContainer);
        webView = findViewById(R.id.webView);
        splash = findViewById(R.id.splash);

        WebSettings mWebSettings = webView.getSettings();
        WebSettings splashSettings = splash.getSettings();


        splashSettings.setJavaScriptEnabled(true);
        splashSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        splashSettings.setGeolocationEnabled(true);
        splashSettings.setAppCacheEnabled(true);
        splashSettings.setDatabaseEnabled(true);
        splashSettings.setDomStorageEnabled(true);
        splashSettings.setSupportZoom(false);
        splashSettings.setLoadWithOverviewMode(true);
        splashSettings.setAllowFileAccess(true);
        splashSettings.setAllowContentAccess(true);
        splash.setWebViewClient(new mySplash());


        mWebSettings.setJavaScriptEnabled(true);
        mWebSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        mWebSettings.setGeolocationEnabled(true);
        mWebSettings.setAppCacheEnabled(true);
        mWebSettings.setDatabaseEnabled(true);
        mWebSettings.setDomStorageEnabled(true);
        mWebSettings.setSupportZoom(false);
        mWebSettings.setLoadWithOverviewMode(true);
        mWebSettings.setAllowFileAccess(true);
        mWebSettings.setAllowContentAccess(true);
        webView.addJavascriptInterface(new JS_INTERFACE(this), "android");
        webView.setWebViewClient(new myWebclient());
        splash.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, android.webkit.GeolocationPermissions.Callback callback) {
                Log.d("geolocation permission", "permission >>>"+origin);
                callback.invoke(origin, true, false);
            }
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> filePath, WebChromeClient.FileChooserParams fileChooserParams) {

                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                }
                uploadMessage = filePath;
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                        takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                        Log.e(TAG, "berhasil buat picture");
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                        Log.e(TAG, "Unable to create Image File", ex);
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(photoFile));
                        Log.e(TAG, "ada foto file");
                    } else {
                        takePictureIntent = null;
                        Log.e(TAG, "gak ada foto file");
                    }
                }

                //contentSelectionIntent.setType("image|application/*");
                Intent[] intentArray;
                if (takePictureIntent != null) {
                    Log.e(TAG, "Masuk take picture intent");
                    intentArray = new Intent[]{takePictureIntent};
                    Log.e(TAG, "Masuk take picture intent2");
                } else {
                    Log.e(TAG, "gak masuk takePicture");
                    intentArray = new Intent[0];
                }
                //Intent[] intentArray=new Intent[0];
                Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                contentSelectionIntent.setType("image/*|application/*");
                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                chooserIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);
                Log.e(TAG, "file open didalam pertama");
                /*String dataString = chooserIntent.getDataString();
                Log.e(TAG, "Masuk bang "+dataString);
                Uri[] results = null;
                if (dataString != null) {
                    results = new Uri[]{Uri.parse(dataString)};
                }
                Log.e(TAG, "Masuk bang");
                uploadMessage.onReceiveValue(results);
                uploadMessage = null;*/
                return true;
            }

            // openFileChooser for Android 3.0+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {


                mUploadMessage = uploadMsg;
                // Create AndroidExampleFolder at sdcard
                // Create AndroidExampleFolder at sdcard
                File imageStorageDir = new File(
                        Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES)
                        , "AndroidExampleFolder");
                if (!imageStorageDir.exists()) {
                    // Create AndroidExampleFolder at sdcard
                    imageStorageDir.mkdirs();
                }
                // Create camera captured image file path and name
                File file = new File(
                        imageStorageDir + File.separator + "IMG_"
                                + System.currentTimeMillis()
                                + ".jpg");
                mCapturedImageURI = Uri.fromFile(file);
                // Camera capture image intent
                final Intent captureIntent = new Intent(
                        android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*|application/pdf");
                // Create file chooser intent
                Intent chooserIntent = Intent.createChooser(i, "Image Chooser");
                chooserIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                // Set camera intent to file chooser
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS
                        , new Parcelable[]{captureIntent});
                // On select image call onActivityResult method of activity
                startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
            }

            // openFileChooser for Android < 3.0
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                openFileChooser(uploadMsg, "");
            }

            //openFileChooser for other Android versions
            public void openFileChooser(ValueCallback<Uri> uploadMsg,
                                        String acceptType,
                                        String capture) {
                openFileChooser(uploadMsg, acceptType);
            }
        });

        isStoragePermissionGranted();
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, android.webkit.GeolocationPermissions.Callback callback) {
                Log.d("geolocation permission", "permission >>>"+origin);
                callback.invoke(origin, true, false);
            }
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> filePath, WebChromeClient.FileChooserParams fileChooserParams) {

                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                }
                uploadMessage = filePath;
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                        takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                        Log.e(TAG, "berhasil buat picture");
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                        Log.e(TAG, "Unable to create Image File", ex);
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(photoFile));
                        Log.e(TAG, "ada foto file");
                    } else {
                        takePictureIntent = null;
                        Log.e(TAG, "gak ada foto file");
                    }
                }

                //contentSelectionIntent.setType("image|application/*");
                Intent[] intentArray;
                if (takePictureIntent != null) {
                    Log.e(TAG, "Masuk take picture intent");
                    intentArray = new Intent[]{takePictureIntent};
                } else {
                    Log.e(TAG, "gak masuk takePicture");
                    intentArray = new Intent[0];
                }
                //Intent[] intentArray=new Intent[0];
                Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                //contentSelectionIntent.setType("image/*|application/pdf");
                String [] mimeTypes = {"image/*", "application/pdf"};
                contentSelectionIntent.setType("*/*");
                contentSelectionIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                contentSelectionIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                //chooserIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);
                Log.e(TAG, "file open didalam pertama");
                /*String dataString = chooserIntent.getDataString();
                Log.e(TAG, "Masuk bang "+dataString);
                Uri[] results = null;
                if (dataString != null) {
                    results = new Uri[]{Uri.parse(dataString)};
                }
                Log.e(TAG, "Masuk bang");
                uploadMessage.onReceiveValue(results);
                uploadMessage = null;*/
                return true;
            }

            // openFileChooser for Android 3.0+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {


                mUploadMessage = uploadMsg;
                // Create AndroidExampleFolder at sdcard
                // Create AndroidExampleFolder at sdcard
                File imageStorageDir = new File(
                        Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES)
                        , "AndroidExampleFolder");
                if (!imageStorageDir.exists()) {
                    // Create AndroidExampleFolder at sdcard
                    imageStorageDir.mkdirs();
                }
                // Create camera captured image file path and name
                File file = new File(
                        imageStorageDir + File.separator + "IMG_"
                                + System.currentTimeMillis()
                                + ".jpg");
                mCapturedImageURI = Uri.fromFile(file);
                // Camera capture image intent
                final Intent captureIntent = new Intent(
                        android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                String [] mimeTypes = {"image/*", "application/pdf"};
                i.setType("*/*");
                i.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                // Create file chooser intent
                Intent chooserIntent = Intent.createChooser(i, "Image Chooser");
                chooserIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                // Set camera intent to file chooser
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS
                        , new Parcelable[]{captureIntent});
                // On select image call onActivityResult method of activity
                startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
                Log.e(TAG, "file open didalam kedua");
            }

            // openFileChooser for Android < 3.0
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                openFileChooser(uploadMsg, "");
            }

            //openFileChooser for other Android versions
            public void openFileChooser(ValueCallback<Uri> uploadMsg,
                                        String acceptType,
                                        String capture) {
                openFileChooser(uploadMsg, acceptType);
            }
        });
        if (Build.VERSION.SDK_INT >= 19) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            splash.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else if (Build.VERSION.SDK_INT >= 11 && Build.VERSION.SDK_INT < 19) {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            splash.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        if (savedInstanceState == null)
        {
            //webView.loadUrl("https://prahu-hub.com/v2/tracking-apps");
            //webView.loadUrl("https://prahu-hub.com/rc/tracking-apps");
//            webView.loadUrl("https://prahu-hub.com/tracking-apps");
//            webView.loadUrl("https://prahu-hub.com/demo/tracking-apps");
            webView.loadUrl("file:///android_asset/tracking-apps/index.html");
            splash.loadUrl("file:///android_asset/SplashScreen/index.html");
            //webView.loadUrl("https://412242b5.ap.ngrok.io");
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, 0);
        }
//        mySwipeRefreshLayout.setOnRefreshListener(
//                new SwipeRefreshLayout.OnRefreshListener() {
//                    @Override
//                    public void onRefresh() {
//                        mySwipeRefreshLayout.setRefreshing(false);
//                        webView.reload();
//                        webView.clearCache(true);
//                    }
//                }
//        );

    }
    public void reloadCc (){
        webView.reload();
        webView.clearCache(true);
    }
    @Override
    protected void onSaveInstanceState(Bundle outState )
    {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
        }

    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }
    private File createImageFile() throws IOException {

            // Create an image file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES);
            File imageFile = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
        /*Bitmap bmp = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 70, bos);
        byte[] bitmapdata = bos.toByteArray();
        FileOutputStream fos = new FileOutputStream(imageFile);
        fos.write(bitmapdata);
        fos.flush();
        fos.close();*/
            return imageFile;
        }
    private boolean isappOnline() {
        ConnectivityManager cm = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
    public class myWebclient extends WebViewClient {
        ProgressDialog progressDialog;
        @Override
        public void onPageFinished(WebView view, String url) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    findViewById(R.id.webView).setVisibility(View.VISIBLE);
                    //show webview
                    findViewById(R.id.splash).setVisibility(View.GONE);
                }

            }, 6000);
//            try {
//                // Close progressDialog
//                if (progressDialog.isShowing()) {
//                    progressDialog.dismiss();
//                    progressDialog = null;
//
//
//                }
//            } catch (Exception exception) {
//                exception.printStackTrace();
//            }
        }
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
//            if (progressDialog == null) {
//                progressDialog = new ProgressDialog(MainActivity.this);
//                progressDialog.setMessage("Loading...");
//                progressDialog.setCancelable(true);
//                progressDialog.setCanceledOnTouchOutside(false);
//                progressDialog.show();
//            }else {
//                progressDialog.show();
//            }
        }
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.contains("mailto:")||url.contains("assets/apk")) {
                // Could be cleverer and use a regex
                //Open links in new browser
                view.getContext().startActivity(
                        new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                // Here we can open new activity
                return true;

            }else if(url.contains("whatsapp:")){
                try {
                    view.getContext().startActivity(
                            new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    // Here we can open new activity
                    return true;
                }catch (Exception e){
                    Toast.makeText(view.getContext(), "WhatsApp not Installed", Toast.LENGTH_SHORT)
                            .show();
//download for example after dialog
                    Uri uri = Uri.parse("market://details?id=com.whatsapp");
                    view.getContext().startActivity(
                            new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.whatsapp")));
                    // Here we can open new activity
                    return true;

                }
            } else {
                // Stay within this webview and load url
                view.loadUrl(url);
                return true;
            }
        }
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
//            if(!isappOnline()) {
//                webView.loadUrl("file:///android_asset/error.html");
//            }else{
//                webView.loadUrl("file:///android_asset/errors.html");
//            }
        }
    }

    public class mySplash extends WebViewClient {
        ProgressDialog progressDialog;
        @Override
        public void onPageFinished(WebView view, String url) {
            try {
                // Close progressDialog
//                if (progressDialog.isShowing()) {
//                    progressDialog.dismiss();
//                    progressDialog = null;

//                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
//            if (progressDialog == null) {
//                progressDialog = new ProgressDialog(MainActivity.this);
//                progressDialog.setMessage("Loading...");
//                progressDialog.setCancelable(true);
//                progressDialog.setCanceledOnTouchOutside(false);
//                progressDialog.show();
//            }else {
//                progressDialog.show();
//            }
        }
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            if(!isappOnline()) {
                webView.loadUrl("file:///android_asset/error.html");
            }else{
                //webView.loadUrl("file:///android_asset/errors.html");
            }
        }
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        }


    @Override
    public boolean onKeyDown(int keyCode , KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

}

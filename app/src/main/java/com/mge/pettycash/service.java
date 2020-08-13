package com.mge.pettycash;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.PusherEvent;
import com.pusher.client.channel.SubscriptionEventListener;

import org.json.JSONException;

import java.util.List;

import static android.content.ContentValues.TAG;

public class service extends Service {
  private NotificationManager notifManager;
  Context context = this;
  PusherOptions options;
  Pusher pusher;
  Channel channel;
  Class mainActivity;
  Resources resources;
  String packageName;

  @Override
  public IBinder onBind(Intent intent) {

    return null;
  }

  @Override
  public void onCreate() {
    resources = context.getResources();
    if (Build.VERSION.SDK_INT >= 26) {

    }
    packageName = context.getPackageName();
    Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
    String className = launchIntent.getComponent().getClassName();

    try {
      mainActivity = Class.forName(className);
    } catch (Exception e) {
      e.printStackTrace();
    }
//    DatabaseHelper dbPusher = new DatabaseHelper(context);
//    Cursor p = dbPusher.getAllDataPusher();
//    Helper h = new Helper();

//    if (p.getCount() > 0) {
//      p.moveToFirst();
//      try {
//        String cluster = h.strToJSON(p.getString(1), "cluster");
//        String apikey = h.strToJSON(p.getString(1), "apikey");
//        String event = h.strToJSON(p.getString(1), "event");
//        String channelNm = h.strToJSON(p.getString(1), "channelNm");
        String cluster = "ap1";
        String apikey = "7942a9e6d370098c7735";
        String event = "penugasan";
        String channelNm = "V2";
        //pusher("ap1","e327dc39f0ae164632ea","my-channel","test-event");
        pusher(cluster, apikey, channelNm, event);
//      } catch (JSONException e) {
//        e.printStackTrace();
//      }
//    }


  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    return START_STICKY;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

  private boolean isAppIsInBackground(Context context) {
    boolean isInBackground = true;
    ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
      List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
      for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
        if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
          for (String activeProcess : processInfo.pkgList) {
            if (activeProcess.equals(context.getPackageName())) {
              isInBackground = false;
            }
          }
        }
      }
    } else {
      List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
      ComponentName componentInfo = taskInfo.get(0).topActivity;
      if (componentInfo.getPackageName().equals(context.getPackageName())) {
        isInBackground = false;
      }
    }

    return isInBackground;
  }

  private void pusher(String cluster, String apikey, String channelNm, String event) {
    options = new PusherOptions();
    options.setCluster(cluster);
    pusher = new Pusher(apikey, options);
    pusher.connect();
    channel = pusher.subscribe(channelNm);
    Log.d("masuk", "masuk1");
    channel.bind(event, new SubscriptionEventListener() {
      @Override
      public void onEvent(PusherEvent event) {

        SharedPreferences sh
                = getSharedPreferences("MySharedPref",
                MODE_PRIVATE);
        int petugasID = sh.getInt("petugasID", 0);
        Helper h = new Helper();
          try {
            String notifPetugasID = h.strToJSON(event.getData(), "petugasID");
            String msg = h.strToJSON(event.getData(), "message");
            Log.d(TAG, "petugasID: "+petugasID);
            Log.d(TAG, "petugasCH: "+notifPetugasID);
            if (Integer.parseInt(notifPetugasID) == petugasID){
              createNotification(msg, context, event.getData(), "new Task");
            }

          } catch (JSONException e) {
            e.printStackTrace();
          }

        }
    });


  }

  public void createNotification(String aMessage, Context context, String json, String type) {
    final int NOTIFY_ID = 0; // ID of notification
    String id = "zemmuwa"; // default_channel_id
    String title = "test"; // Default Channel
    Intent intent;
    PendingIntent pendingIntent;
    NotificationCompat.Builder builder;

    if (notifManager == null) {
      notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      int importance = NotificationManager.IMPORTANCE_HIGH;
      NotificationChannel mChannel = notifManager.getNotificationChannel(id);
      if (mChannel == null) {
        mChannel = new NotificationChannel(id, title, importance);
//        mChannel.enableVibration(true);
//        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        notifManager.createNotificationChannel(mChannel);
      }
      builder = new NotificationCompat.Builder(context, id);
      //intent = new Intent(context, MainActivity.class);
      intent = new Intent(context, Receiver.class);
      intent.setAction("com.mge.pettycash.BroadcastReceiver");
      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
      intent.putExtra("pushnotification", json);
      Log.d("isiIntent", intent.hasExtra("pushnotification") + "");
      //pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
      pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
      builder
        .setContentTitle(aMessage)                            // required
        .setSmallIcon(resources.getIdentifier("ic_launcher", "mipmap", packageName))
        .setContentText(context.getString(resources.getIdentifier("app_name", "string", packageName)))
        .setDefaults(Notification.DEFAULT_ALL)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .setTicker(aMessage)
        .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
        .setNumber(1);
    } else {
      builder = new NotificationCompat.Builder(context, id);
      intent = new Intent(context, Receiver.class);
      intent.setAction("com.mge.pettycash.BroadcastReceiver");
      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
      intent.putExtra("pushnotification", json);
      pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
      builder.setContentTitle(aMessage)                            // required
        .setSmallIcon(android.R.drawable.ic_popup_reminder)   // required
        .setContentText(context.getString(resources.getIdentifier("app_name", "string", packageName))) // required
        //.setContentText(context.getString(R.string.app_name))
        .setDefaults(Notification.DEFAULT_ALL)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .setTicker(aMessage)
        .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
        .setPriority(Notification.PRIORITY_HIGH);
    }
    Notification notification = builder.build();
    notifManager.notify((int) System.currentTimeMillis()
      , notification);
  }


}

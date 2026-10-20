package com.somnia.app;
import android.app.*;
import android.content.*;
import android.os.Build;
public class ReminderReceiver extends android.content.BroadcastReceiver {
  public void onReceive(Context context, Intent intent) {
    String channel="somnia_sleep";
    NotificationManager nm=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
    nm.createNotificationChannel(new NotificationChannel(channel,"Напоминания о сне",NotificationManager.IMPORTANCE_DEFAULT));
    Notification n=new Notification.Builder(context,channel).setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
       .setContentTitle("Somnia · Пора отдыхать").setContentText("Приближается запланированное время сна.").setAutoCancel(true).build();
    try { nm.notify(42,n); } catch(SecurityException ignored) {}
    MainActivity.scheduleReminder(context);
  }
}

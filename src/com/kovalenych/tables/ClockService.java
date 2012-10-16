package com.kovalenych.tables;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.*;
import android.util.Log;
import android.widget.RemoteViews;
import com.kovalenych.*;

import java.util.ArrayList;

/**
 * this class was made
 * by insomniac and angryded
 * for their purposes
 */
public class ClockService extends Service implements Soundable, Const {

    private int position;
    Table table;
    Vibrator v;
    PendingIntent pi;

    final String LOG_TAG = "CO2 ClockService";
    private static final int NOTIFY_ID = 1;
    private boolean vibrationEnabled;
    private ArrayList<Integer> voices;
    String name;

    public boolean showTray = false;

    SoundManager soundManager;
    private int volume;
    private SharedPreferences _preferedSettings;

    public void onCreate() {
        super.onCreate();
        v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        Log.d(LOG_TAG, "ClockService onCreate");
        soundManager = new SoundManager(this);

        _preferedSettings = getSharedPreferences("sharedSettings", MODE_PRIVATE);
        int bla = _preferedSettings.getInt("volume", 15);
        Log.d("zzzzzzbla", "" + bla);
    }

    private void showProgressInTray(int progress, int max, boolean breathing) {
        Log.d("showProgressInTray", "zzzzzzzzz");
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE); // Создаем экземпляр менеджера уведомлений
        int icon = R.drawable.tray_icon; // Иконка для уведомления, я решил воспользоваться стандартной иконкой для Email
        long when = System.currentTimeMillis(); // Выясним системное время
        Intent notificationIntent = new Intent(this, ClockActivity.class); // Создаем экземпляр Intent
//                    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        Notification notification = new Notification(icon, null, when); // Создаем экземпляр уведомления, и передаем ему наши параметры
        PendingIntent contentIntent = PendingIntent.getActivity(this, 5, notificationIntent, 0); // Подробное описание смотреть в UPD к статье
        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notif); // Создаем экземпляр RemoteViews указывая использовать разметку нашего уведомления
        contentView.setProgressBar(R.id.tray_progress, max, progress, false);
        contentView.setTextViewText(R.id.tray_text, (breathing ? "breath  " : "hold  ") + Utils.timeToString(progress)); // Привязываем текст к TextView в нашей разметке
        notification.contentIntent = contentIntent; // Присваиваем contentIntent нашему уведомлению
        notification.contentView = contentView; // Присваиваем contentView нашему уведомлению
        mNotificationManager.notify(NOTIFY_ID, notification); // Выводим уведомление в строку
    }


    public void onDestroy() {
        super.onDestroy();
        NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancel(NOTIFY_ID);
        if (task != null)
            task.cancel(true);
        Log.d(LOG_TAG, "ClockService onDestroy");
    }

    public int subscriber = SUBSCRIBER_CLOCK;


    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "ClockService onStartCommand" + flags);

        String destination = intent.getStringExtra(FLAG);
        if (destination.equals(FLAG_CREATE)) {
            Log.d(LOG_TAG, FLAG_CREATE);
            Bundle cyclesBundle = intent.getBundleExtra(ClockActivity.PARAM_CYCLES);
            pi = intent.getParcelableExtra(ClockActivity.PARAM_PINTENT);
            name = intent.getStringExtra(ClockActivity.PARAM_TABLE);
            Log.d(LOG_TAG, "tableName  " + name);
            volume = intent.getIntExtra(PARAM_VOLUME, 0);
            Log.d(LOG_TAG, "set volume " + volume);
            int size = cyclesBundle.getInt("tablesize");
            table = new Table();
            for (int i = 0; i < size; i++) {
                table.getCycles().add(
                        new Cycle(cyclesBundle.getInt("breathe" + Integer.toString(i)), cyclesBundle.getInt("hold" + Integer.toString(i)))
                );
                Log.d(LOG_TAG, "new cycle" + table.getCycles().get(i).convertToString());
            }
            position = cyclesBundle.getInt("number");
            vibrationEnabled = cyclesBundle.getBoolean("vibro");
            voices = cyclesBundle.getIntegerArrayList("voices");
            task = new ClockTask(table, true);
            task.execute(position);

        } else if (destination.equals(FLAG_SHOW_TRAY)) {
            Log.d(LOG_TAG, FLAG_SHOW_TRAY);
            showTray = true;
        } else if (destination.equals(FLAG_HIDE_TRAY)) {
            showTray = false;
            pi = intent.getParcelableExtra(PARAM_PINTENT);
            NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nMgr.cancel(NOTIFY_ID);
            subscriber = SUBSCRIBER_CLOCK;
        } else if (destination.equals(FLAG_SUBSCRIBE_TABLE)) {
            pi = intent.getParcelableExtra(PARAM_PINTENT);
            subscriber = SUBSCRIBER_TABLE;
            Intent newIntent = new Intent()
                    .putExtra(ClockActivity.PARAM_TABLE, name)
                    .putExtra(ClockActivity.PARAM_M_CYCLE, curCycle);
            try {
                pi.send(ClockService.this, 0, newIntent);
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        } else if (destination.equals(FLAG_SUBSCRIBE_CYCLES)) {
            pi = intent.getParcelableExtra(PARAM_PINTENT);
            subscriber = SUBSCRIBER_CYCLE;
            volume = intent.getIntExtra(PARAM_VOLUME, 0);
            Log.d(LOG_TAG, "set volume " + volume);
            Intent newIntent = new Intent()
                    .putExtra(ClockActivity.PARAM_TABLE, name)
                    .putExtra(ClockActivity.PARAM_M_CYCLE, curCycle);
            try {
                pi.send(ClockService.this, 0, newIntent);
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }

        } else if (destination.equals(FLAG_SETVOLUME)) {
            Log.d(LOG_TAG, "set volume " + volume);
            volume = intent.getIntExtra(PARAM_VOLUME, 0);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    ClockTask task;
    int curCycle = -1;

    private void onTic(int time, int cycle, boolean breathing) {

        //evaluate percent progress
        curCycle = cycle;
        int breathe = table.getCycles().get(cycle).breathe;
        int hold = table.getCycles().get(cycle).hold;
        int all = breathing ? breathe : hold;
        Log.d(LOG_TAG, "zzzzonTic  time: " + time);
        Intent intent = new Intent()
                .putExtra(ClockActivity.PARAM_TIME, time)
                .putExtra(ClockActivity.PARAM_PROGRESS, all)
                .putExtra(ClockActivity.PARAM_BREATHING, breathing)
                .putExtra(ClockActivity.PARAM_TABLE, name)
                .putExtra(ClockActivity.PARAM_M_CYCLE, cycle);
        try {
            int stat = breathing ? STATUS_BREATH : STATUS_HOLD;
            if (subscriber == SUBSCRIBER_CLOCK ||
                    (breathing && (time == 0) && subscriber == SUBSCRIBER_CYCLE)) {
                pi.send(ClockService.this, stat, intent);
                Log.d(LOG_TAG, "sendtotable");
            }
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
        //vibrate  or sound

        if (time == 0 && vibrationEnabled)
            v.vibrate(200);
        if (breathing) {
            if (showTray)
                showProgressInTray(time, breathe, breathing);
            //breath
            int relatTime = time - breathe;
            if (time == 0 && voices.contains(BREATHE))
                soundManager.playSound(BREATHE, volume);
            else if (voices.contains(relatTime))
                soundManager.playSound(relatTime, volume);
        } else {
            if (showTray)
                showProgressInTray(time, hold, breathing);
            if (time == 0 && voices.contains(START))                 //hold
                soundManager.playSound(START, volume);
            else if (voices.contains(time))
                soundManager.playSound(time, volume);
        }
    }

    public void onTableFinish() {

        if (voices.contains(BREATHE))
            soundManager.playSound(BREATHE, volume);
        if (vibrationEnabled)
            v.vibrate(200);

        Intent intent = new Intent()
                .putExtra(ClockActivity.PARAM_TIME, 0)
                .putExtra(ClockActivity.PARAM_PROGRESS, 0)
                .putExtra(ClockActivity.PARAM_BREATHING, false);
        try {
            pi.send(ClockService.this, STATUS_FINISH, intent);
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
        stopSelf();
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }


    class ClockTask extends AsyncTask<Integer, Integer, Void> {

        Table table;
        boolean breathing;

        public ClockTask(Table table, boolean breathing) {
            this.table = table;
            this.breathing = breathing;
        }

        @Override
        protected Void doInBackground(Integer... params) {

            long tableStart = System.currentTimeMillis();
            for (int i = params[0]; i < table.getCycles().size(); i++) {
                long cycleStart = System.currentTimeMillis();
                Cycle cycle = table.getCycles().get(i);
                if (breathing)
                    for (int t = 0; t < cycle.breathe; t++) {
                        if (isCancelled())
                            return null;
                        publishProgress(t, i);
                        try {
                            long adjust = System.currentTimeMillis() - cycleStart - 1000 * t;
                            long sleeptime = 1000 - adjust;
                            if (sleeptime > 0)
                                Thread.sleep(sleeptime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                breathing = false;
                cycleStart = System.currentTimeMillis();
                for (int t = 0; t < cycle.hold; t++) {
                    if (isCancelled())
                        return null;
                    publishProgress(t, i);
                    try {
                        long adjust = System.currentTimeMillis() - cycleStart - 1000 * t;
                        long sleeptime = 1000 - adjust;
                        if (sleeptime > 0)
                            Thread.sleep(sleeptime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
                breathing = true;
            }
            Log.d("table made in ", "" + (System.currentTimeMillis() - tableStart));
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            onTic(values[0], values[1], breathing);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            onTableFinish();
        }
    }

}

package org.eliptus.volumerecall;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

// TODO: need to save volume when killed or shutting down

public class SwitcherService extends Service
{
    // types

    private final class ServiceHandler extends Handler
    {
        // methods

        public ServiceHandler(Looper looper)
        {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg)
        {
            Intent intent = (Intent)msg.obj;
            String action = intent.getAction();
            int numStreamTypes = AudioManager.NUM_STREAMS;
            AudioManager audioService = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

            Log.v(Constants.TAG, "SwitcherService::onHandleIntent() - action = " + action);

            Resources res = getResources();
            startForeground(res.getInteger(R.integer.switcher_notify_id), mNotification);

            if (action.equals(ACTION_INIT))
            {
                // do nothing
            }
            else if (action.equals(ACTION_SWITCH))
            {
                SharedPreferences volume = getSharedPreferences(Constants.PREF_VOLUME, 0);
                SharedPreferences.Editor editor = volume.edit();
                int state = intent.getIntExtra(EXTRA_STATE, -1);
                boolean save = intent.getBooleanExtra(EXTRA_SAVE, false);

                Log.v(Constants.TAG, "SwitcherService::onHandleIntent() - state = " + state + ", save = " + save);


                for (int i = 0 ; i < numStreamTypes ; i++)
                {
                    String oldStr = String.format("%d:%d", (0 == state ? 1 : 0), i);
                    String newStr = String.format("%d:%d", state, i);
                    int oldVal = audioService.getStreamVolume(i);
                    int newVal = volume.getInt(newStr, oldVal);

                    Log.v(Constants.TAG, "SwitcherService::onHandleIntent() - i = " + i + ",  oldVal = " + oldVal + ", newVal = " + newVal);

                    audioService.setStreamVolume(i, newVal, 0);

                    if (save)
                    {
                        editor.putInt(oldStr, oldVal);
                    }
                }

                editor.commit();
            }

            stopForeground(true);
        }
    }


    // fields

    public static final String ACTION_INIT   = "INIT";
    public static final String ACTION_SWITCH = "SWITCH";

    public static final String EXTRA_STATE   = "STATE";
    public static final String EXTRA_SAVE    = "SAVE";

    private ServiceHandler mHandler;
    private GenericReceiver mReceiver;
    private Notification mNotification;


    // methods

    @Override
    public void onCreate()
    {
        Log.v(Constants.TAG, "SwitcherService::onHandleIntent() - called");

        HandlerThread thread = new HandlerThread("SwitcherThread",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        mHandler = new ServiceHandler(thread.getLooper());

        Notification.Builder builder = new Notification.Builder(this);
        Resources res = getResources();
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setContentInfo(res.getString(R.string.switcher_notify_info));
        mNotification = builder.getNotification();

        mReceiver = new GenericReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(mReceiver, filter);

    }

    @Override
    public void onDestroy()
    {
        unregisterReceiver(mReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (null != intent)
        {
            Message msg = mHandler.obtainMessage();
            msg.obj = intent;
            mHandler.sendMessage(msg);
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
}

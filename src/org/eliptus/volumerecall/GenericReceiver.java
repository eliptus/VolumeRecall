package org.eliptus.volumerecall;

import android.os.Bundle;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.util.Log;
import java.lang.Exception;

public class GenericReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        Intent service = new Intent(context, SwitcherService.class);

        Log.v(Constants.TAG, "GenericReceiver::onReceive() - action = " + action);

        if (action.equals(Intent.ACTION_BOOT_COMPLETED))
        {
            SharedPreferences config = context.getSharedPreferences(Constants.PREF_CONFIG, 0);
            boolean enabled = config.getBoolean(Constants.PREF_CONFIG__ENABLED, false);
            if (enabled)
            {
                service.setAction(SwitcherService.ACTION_INIT);
            }
        }
        else if (action.equals(Intent.ACTION_HEADSET_PLUG))
        {
            int state = intent.getIntExtra("state", -1);
            boolean save = !isInitialStickyBroadcast();
            service.setAction(SwitcherService.ACTION_SWITCH);
            service.putExtra(SwitcherService.EXTRA_STATE, state);
            service.putExtra(SwitcherService.EXTRA_SAVE, save);
        }

        if (null != service.getAction())
        {
            context.startService(service);
        }
    }
}

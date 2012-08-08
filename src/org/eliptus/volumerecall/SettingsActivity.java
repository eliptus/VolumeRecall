package org.eliptus.volumerecall;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ToggleButton;

public class SettingsActivity extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.v(Constants.TAG, "SettingsActivity::onCreate() - called");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings);

        SharedPreferences config = getSharedPreferences(Constants.PREF_CONFIG, 0);
        boolean enabled = config.getBoolean(Constants.PREF_CONFIG__ENABLED, true);

        if (enabled)
        {
            Intent service = new Intent(this, SwitcherService.class);
            service.setAction(SwitcherService.ACTION_INIT);
            startService(service);
        }

        ToggleButton togglebutton = (ToggleButton)findViewById(R.id.settings_togglebutton);
        togglebutton.setChecked(enabled);
    }

    public void onClickSettingsToggleButton(View view)
    {
        SharedPreferences config = getSharedPreferences(Constants.PREF_CONFIG, 0);
        SharedPreferences.Editor editor = config.edit();
        ToggleButton togglebutton = (ToggleButton)view;
        boolean enabled = togglebutton.isChecked();

        editor.putBoolean(Constants.PREF_CONFIG__ENABLED, enabled);
        editor.commit();

        Intent service = new Intent(this, SwitcherService.class);
        service.setAction(SwitcherService.ACTION_INIT);
        if (enabled)
        {
            startService(service);
        }
        else
        {
            stopService(service);
        }
    }
}

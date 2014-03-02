package com.abdelicious.tcpwidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.Toast;

/**
 * Created by idmad_000 on 11/12/13.
 */
public class TCPWidgetConfigure extends Activity {

    public static final String PREFS_FILE_PREFIX = "TCPWidget_";
    public static final String CONFIG_SERVER = "CONFIG_SERVER";
    public static final String CONFIG_PORT = "CONFIG_PORT";
    public static final String CONFIG_PASS = "CONFIG_PASS";
    public static final String CONFIG_CMDPRE = "CONFIG_CMDPRE";
    public static final String CONFIG_CMDPOST = "CONFIG_CMDPOST";
    public static final String CONFIG_CMDDELAY = "CONFIG_CMDDELAY";
    public static final int DEFAULT_PORT = 80;
    public static final int DEFAULT_CMDDELAY = 0;

    Button configOKButton;
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setResult(RESULT_CANCELED);

        setContentView(R.layout.config);

        configOKButton = (Button)findViewById(R.id.config_ok);
        configOKButton.setOnClickListener(configOkButtonOnClickListener);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If they gave us an intent without the widget id, just bail.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
    }

    private void saveConfig() {
        SharedPreferences.Editor prefs = getSharedPreferences(PREFS_FILE_PREFIX + mAppWidgetId, MODE_PRIVATE).edit();

        prefs.putString(CONFIG_SERVER, ((EditText)findViewById(R.id.config_server)).getText().toString());
        prefs.putInt(CONFIG_PORT, parseInt(((EditText) findViewById(R.id.config_port)).getText().toString(), DEFAULT_PORT));
        prefs.putString(CONFIG_PASS, ((EditText)findViewById(R.id.config_pass)).getText().toString());
        prefs.putString(CONFIG_CMDPRE, ((EditText)findViewById(R.id.config_cmdpre)).getText().toString());
        prefs.putString(CONFIG_CMDPOST, ((EditText)findViewById(R.id.config_cmdpost)).getText().toString());
        prefs.putInt(CONFIG_CMDDELAY, parseInt(((EditText) findViewById(R.id.config_cmddelay)).getText().toString(), DEFAULT_CMDDELAY));

        prefs.commit();
    }

    private void completeConfig() {

        final Context context = TCPWidgetConfigure.this;

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        TCPWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();

    }

    public static int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private Button.OnClickListener configOkButtonOnClickListener = new Button.OnClickListener() {

        @Override
        public void onClick(View arg0) {

            saveConfig();
            completeConfig();

        }
    };

}

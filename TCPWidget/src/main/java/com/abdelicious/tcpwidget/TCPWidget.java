package com.abdelicious.tcpwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.RemoteViews;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by idmad_000 on 11/12/13.
 */
public class TCPWidget extends AppWidgetProvider {

    public static final String LOG_TAG = "TCPWidget";
    public static final String TCP_SEND = "com.abdelicious.tcpwidget.action.TCP_SEND";
    public static final String STRING_PASS = "PASS?";
    public static final String STRING_OK = "AOK";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        final int N = appWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i<N; i++) {

            int appWidgetId = appWidgetIds[i];

            updateAppWidget(context, appWidgetManager, appWidgetId);

        }

    }

    @Override
    public void onReceive(Context context, Intent intent) {

        super.onReceive(context, intent);

        if (intent.getAction() == TCP_SEND){
            sendCommands(context, intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1));
        }

    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        // Create an Intent to launch ExampleActivity
        Intent intent = new Intent();
        intent.setAction(TCP_SEND);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get the layout for the App Widget and attach an on-click listener
        // to the button
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.main);
        views.setOnClickPendingIntent(R.id.widget_button, pendingIntent);

        // Tell the AppWidgetManager to perform an update on the current app widget
        appWidgetManager.updateAppWidget(appWidgetId, views);

    }

    public void sendCommands(Context context, int appWidgetId) {

        SharedPreferences prefs = context.getSharedPreferences(TCPWidgetConfigure.PREFS_FILE_PREFIX + appWidgetId, Context.MODE_PRIVATE);

        String config_server = prefs.getString(TCPWidgetConfigure.CONFIG_SERVER, "");
        int config_port = prefs.getInt(TCPWidgetConfigure.CONFIG_PORT, TCPWidgetConfigure.DEFAULT_PORT);
        String config_pass = prefs.getString(TCPWidgetConfigure.CONFIG_PASS, "");
        String config_cmdpre = prefs.getString(TCPWidgetConfigure.CONFIG_CMDPRE, "");
        String config_cmdpost = prefs.getString(TCPWidgetConfigure.CONFIG_CMDPOST, "");
        int config_cmddelay = prefs.getInt(TCPWidgetConfigure.CONFIG_CMDDELAY, TCPWidgetConfigure.DEFAULT_CMDDELAY);

        new SendCommandsTask().execute(config_server, config_port, config_pass, config_cmdpre, config_cmdpost, config_cmddelay);

    }

    private class SendCommandsTask extends AsyncTask<Object, Void, Void> {

        protected Void doInBackground(Object... params) {

            String config_server = (String)params[0];
            int config_port = (int)params[1];
            String config_pass = (String)params[2];
            String config_cmdpre = (String)params[3];
            String config_cmdpost = (String)params[4];
            int config_cmddelay = (int)params[5];

            try {

                // Connect
                Socket s = new Socket(config_server, config_port);
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));

                // Password
                if (!config_pass.isEmpty() && readCommand(in, STRING_PASS)) {
                    writeCommand(out, config_pass);
                    readCommand(in, STRING_OK);     //Doesn't matter what the answer is, just wait until we get something.
                }

                // Command Pre
                if (!config_cmdpre.isEmpty()) {
                    writeCommand(out, config_cmdpre);
                }

                // Delay
                if (config_cmddelay > 0) {
                    Thread.sleep(config_cmddelay);
                }

                // Command Post
                if (!config_cmdpost.isEmpty()) {
                    writeCommand(out, config_cmdpost);
                }

                // Disconnect
                out.close();
                in.close();
                s.close();

            }catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }

        private boolean readCommand(BufferedReader in, String command) {

            boolean returnValue = false;

            try {

                char[] inBuffer = new char[command.length()];
                in.read(inBuffer);

                returnValue = String.valueOf(inBuffer).equals(command);

            } catch (IOException e) {
                e.printStackTrace();
            }

            return returnValue;

        }

        private void writeCommand(BufferedWriter out, String command) {

            try {

                out.write(command);
                out.newLine();
                out.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}

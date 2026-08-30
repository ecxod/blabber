package eu.siacs.conversations.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

import eu.siacs.conversations.Config;
import eu.siacs.conversations.utils.Compatibility;

public class EventReceiver extends BroadcastReceiver {

    public static final String SETTING_ENABLED_ACCOUNTS = "enabled_accounts";
    public static final String EXTRA_NEEDS_FOREGROUND_SERVICE = "needs_foreground_service";

    @Override
    public void onReceive(final Context context, final Intent originalIntent) {
        final String action = originalIntent.getAction();
        if (!isAllowedAction(action)) {
            Log.w(Config.LOGTAG, "EventReceiver ignored unexpected action");
            return;
        }
        final Intent intentForService = new Intent(context, XmppConnectionService.class);
        intentForService.setAction(action);
        if ("ui".equals(action) || hasEnabledAccounts(context)) {
            Compatibility.startService(context, intentForService);
        } else {
            Log.d(Config.LOGTAG, "EventReceiver ignored action " + intentForService.getAction());
        }
    }

    private static boolean isAllowedAction(final String action) {
        return "ui".equals(action)
                || "ping".equals(action)
                || XmppConnectionService.ACTION_POST_CONNECTIVITY_CHANGE.equals(action)
                || XmppConnectionService.ACTION_IDLE_PING.equals(action)
                || Intent.ACTION_BOOT_COMPLETED.equals(action)
                || Intent.ACTION_SHUTDOWN.equals(action)
                || Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)
                || Intent.ACTION_PACKAGE_REPLACED.equals(action)
                || Intent.ACTION_PACKAGE_RESTARTED.equals(action)
                || "android.net.conn.CONNECTIVITY_CHANGE".equals(action)
                || "android.media.RINGER_MODE_CHANGED".equals(action);
    }

    public static boolean hasEnabledAccounts(final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SETTING_ENABLED_ACCOUNTS, true);
    }

}

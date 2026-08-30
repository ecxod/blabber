package eu.siacs.conversations.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import java.net.URI;

import eu.siacs.conversations.Config;
import io.sentry.Sentry;
import io.sentry.android.core.SentryAndroid;

public final class SentryManager {

    public static final String PREFERENCE_DSN = "sentry_dsn";

    private SentryManager() {
    }

    public static synchronized void applyConfiguration(final Context context) {
        Sentry.close();
        final SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        final String dsn = normalize(preferences.getString(PREFERENCE_DSN, ""));
        if (!isValidDsn(dsn)) {
            return;
        }
        try {
            SentryAndroid.init(context.getApplicationContext(), options -> {
                options.setDsn(dsn);
                options.setSendDefaultPii(false);
                options.setBeforeSend((event, hint) -> {
                    event.setUser(null);
                    return event;
                });
                options.setTracesSampleRate(0.0);
                options.setEnableAutoSessionTracking(false);
                options.setEnableUserInteractionTracing(false);
                options.setEnableUserInteractionBreadcrumbs(false);
                options.setEnableAutoActivityLifecycleTracing(false);
                options.setEnableActivityLifecycleBreadcrumbs(false);
                options.setEnableAppLifecycleBreadcrumbs(false);
                options.setEnableSystemEventBreadcrumbs(false);
                options.setEnableAppComponentBreadcrumbs(false);
                options.setEnableNetworkEventBreadcrumbs(false);
                options.setAttachScreenshot(false);
                options.setAttachViewHierarchy(false);
                options.setCollectAdditionalContext(false);
                options.setCollectExternalStorageContext(false);
                options.setEnableFramesTracking(false);
                options.setEnableNdk(false);
                options.setEnableScopeSync(false);
            });
        } catch (RuntimeException e) {
            Sentry.close();
            Log.e(Config.LOGTAG, "Unable to initialize the user-configured Sentry DSN", e);
        }
    }

    public static boolean isValidDsn(final String value) {
        final String dsn = normalize(value);
        if (TextUtils.isEmpty(dsn)) {
            return false;
        }
        try {
            final URI uri = new URI(dsn).normalize();
            final String userInfo = uri.getUserInfo();
            final String path = uri.getPath();
            final String projectPath = path != null && path.endsWith("/")
                    ? path.substring(0, path.length() - 1)
                    : path;
            return "https".equalsIgnoreCase(uri.getScheme())
                    && !TextUtils.isEmpty(uri.getHost())
                    && !TextUtils.isEmpty(userInfo)
                    && !TextUtils.isEmpty(userInfo.split(":", -1)[0])
                    && !TextUtils.isEmpty(projectPath)
                    && projectPath.lastIndexOf('/') >= 0
                    && projectPath.lastIndexOf('/') < projectPath.length() - 1
                    && TextUtils.isEmpty(uri.getQuery())
                    && TextUtils.isEmpty(uri.getFragment());
        } catch (Exception e) {
            return false;
        }
    }

    public static String normalize(final String value) {
        return value == null ? "" : value.trim();
    }
}

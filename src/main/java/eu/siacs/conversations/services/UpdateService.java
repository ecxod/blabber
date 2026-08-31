package eu.siacs.conversations.services;

import static eu.siacs.conversations.http.HttpConnectionManager.getProxy;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.AsyncTask;
import android.text.format.Formatter;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import eu.siacs.conversations.BuildConfig;
import eu.siacs.conversations.Config;
import eu.siacs.conversations.R;
import me.drakeet.support.toast.ToastCompat;

/**
 * Checks the official GitHub release endpoint and sends users to the release page.
 *
 * APKs are deliberately not downloaded or installed by the app. This keeps update delivery in
 * the browser and removes the need for the REQUEST_INSTALL_PACKAGES permission.
 */
public class UpdateService extends AsyncTask<String, Object, UpdateService.Wrapper> {

    private final Context context;
    private final boolean useTor;
    private final boolean useI2P;
    private final NotificationService notificationService;

    public UpdateService(
            final Context context,
            final XmppConnectionService xmppConnectionService) {
        this.context = context;
        this.useTor = xmppConnectionService.useTorToConnect();
        this.useI2P = xmppConnectionService.useI2PToConnect();
        this.notificationService = xmppConnectionService.getNotificationService();
    }

    @Override
    protected Wrapper doInBackground(final String... params) {
        final Wrapper result = new Wrapper();
        result.interactive = params.length > 0 && Boolean.parseBoolean(params[0]);
        HttpsURLConnection connection = null;
        try {
            final URL endpoint = new URL(Config.UPDATE_URL);
            if (useTor && !useI2P) {
                connection = (HttpsURLConnection) endpoint.openConnection(getProxy(false));
            } else if (useI2P) {
                connection = (HttpsURLConnection) endpoint.openConnection(getProxy(true));
            } else {
                connection = (HttpsURLConnection) endpoint.openConnection();
            }
            connection.setConnectTimeout(Config.SOCKET_TIMEOUT * 1000);
            connection.setReadTimeout(Config.SOCKET_TIMEOUT * 1000);
            connection.setRequestProperty("Accept", "application/vnd.github+json");
            connection.setRequestProperty("User-Agent", BuildConfig.APPLICATION_ID + "/" + BuildConfig.VERSION_NAME);
            connection.setInstanceFollowRedirects(false);

            if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
                throw new IllegalStateException("GitHub release endpoint returned HTTP " + connection.getResponseCode());
            }

            final StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            final JSONObject release = new JSONObject(response.toString());
            result.version = normalizeVersion(release.getString("tag_name"));
            result.releaseUrl = validateReleaseUrl(release.getString("html_url"));
            result.fileSize = findApkSize(release.optJSONArray("assets"));
            result.updateAvailable = compareVersions(result.version, BuildConfig.VERSION_NAME) > 0;
        } catch (Exception e) {
            Log.w(Config.LOGTAG, "Unable to check for updates", e);
            result.error = true;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(final Wrapper result) {
        if (result.error) {
            if (result.interactive) {
                ToastCompat.makeText(context, R.string.failed, ToastCompat.LENGTH_LONG).show();
            }
            return;
        }
        if (!result.updateAvailable) {
            if (result.interactive) {
                ToastCompat.makeText(context, R.string.no_update_available, ToastCompat.LENGTH_LONG).show();
            }
            return;
        }
        if (canShowUpdateDialog()) {
            showUpdateDialog(result);
        } else {
            showNotification(result);
        }
    }

    private boolean canShowUpdateDialog() {
        if (!(context instanceof Activity)) {
            return false;
        }
        final Activity activity = (Activity) context;
        return !activity.isFinishing()
                && (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 || !activity.isDestroyed())
                && activity.hasWindowFocus();
    }

    private void showUpdateDialog(final Wrapper result) {
        final String fileSize = formatFileSize(result.fileSize);
        new AlertDialog.Builder(context)
                .setTitle(R.string.update_available_title)
                .setMessage(context.getString(R.string.update_available, result.version, fileSize))
                .setNegativeButton(R.string.remind_later, null)
                .setPositiveButton(R.string.update, (dialog, which) -> openReleasePage(result.releaseUrl))
                .show();
    }

    private void openReleasePage(final String releaseUrl) {
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(releaseUrl));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            ToastCompat.makeText(context, R.string.failed, ToastCompat.LENGTH_LONG).show();
        }
    }

    private void showNotification(final Wrapper result) {
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(result.releaseUrl));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        final PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        final String fileSize = formatFileSize(result.fileSize);
        notificationService.AppUpdateServiceNotification(
                notificationService.AppUpdateNotification(pendingIntent, result.version, fileSize));
    }

    private String formatFileSize(final long fileSize) {
        return fileSize > 0
                ? Formatter.formatShortFileSize(context, fileSize)
                : context.getString(R.string.unknown_file_size);
    }

    private static String validateReleaseUrl(final String value) throws Exception {
        final URI uri = new URI(value).normalize();
        if (!"https".equalsIgnoreCase(uri.getScheme())
                || !"github.com".equalsIgnoreCase(uri.getHost())
                || uri.getPath() == null
                || !uri.getPath().startsWith("/ecxod/blabber/releases/")) {
            throw new IllegalArgumentException("Unexpected release URL");
        }
        return uri.toString();
    }

    private static long findApkSize(final JSONArray assets) {
        if (assets == null) {
            return -1;
        }
        for (int i = 0; i < assets.length(); i++) {
            final JSONObject asset = assets.optJSONObject(i);
            if (asset != null && asset.optString("name").toLowerCase().endsWith(".apk")) {
                return asset.optLong("size", -1);
            }
        }
        return -1;
    }

    private static String normalizeVersion(final String value) {
        final String version = value == null ? "" : value.trim();
        return version.startsWith("v") || version.startsWith("V")
                ? version.substring(1)
                : version;
    }

    static int compareVersions(final String remoteVersion, final String installedVersion) {
        final String[] remote = normalizeVersion(remoteVersion).split("[.+-]");
        final String[] installed = normalizeVersion(installedVersion).split("[.+-]");
        final int length = Math.max(remote.length, installed.length);
        for (int i = 0; i < length; i++) {
            final int remotePart = i < remote.length ? parseVersionPart(remote[i]) : 0;
            final int installedPart = i < installed.length ? parseVersionPart(installed[i]) : 0;
            if (remotePart != installedPart) {
                return Integer.compare(remotePart, installedPart);
            }
        }
        return 0;
    }

    private static int parseVersionPart(final String value) {
        try {
            return Integer.parseInt(value.replaceFirst("[^0-9].*$", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    static final class Wrapper {
        boolean error;
        boolean interactive;
        boolean updateAvailable;
        String version;
        String releaseUrl;
        long fileSize = -1;
    }
}

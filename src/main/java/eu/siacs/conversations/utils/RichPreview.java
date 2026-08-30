package eu.siacs.conversations.utils;

import android.view.View;

import java.util.regex.Pattern;

import eu.siacs.conversations.services.XmppConnectionService;

/**
 * Builds a local-only link card.
 *
 * Fetching metadata for links from incoming messages leaks the reader's IP address and can
 * access services on the reader's local network. Link cards therefore never perform network
 * requests; the destination is contacted only after the user explicitly opens it.
 */
public class RichPreview {

    public static final String RICH_LINK_METADATA = "richlink_meta_data";

    private final ResponseListener responseListener;

    public RichPreview(final ResponseListener responseListener) {
        this.responseListener = responseListener;
    }

    public void getPreview(
            final String url,
            final String filename,
            final XmppConnectionService xmppConnectionService) {
        final MetaData metaData = new MetaData();
        metaData.setUrl(url);
        metaData.setTitle(url);
        responseListener.onData(metaData);
    }

    public interface ResponseListener {
        void onData(MetaData metaData);

        void onError(Exception e);
    }

    public interface RichLinkListener {
        void onClicked(View view, MetaData meta);
    }

    public interface ViewListener {
        void onSuccess(boolean status);

        void onError(Exception e);
    }

    // Pattern for recognizing a URL, based on RFC 3986.
    public static final Pattern urlPattern = Pattern.compile(
            "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
                    + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
                    + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
}

package eu.siacs.conversations;

import androidx.multidex.MultiDexApplication;

import eu.siacs.conversations.utils.SentryManager;

public class BlabberApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        SentryManager.applyConfiguration(this);
    }
}

package codercamp.com.earningapps;

import android.os.StrictMode;

import com.facebook.ads.AudienceNetworkAds;
import com.google.android.gms.ads.MobileAds;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        //Google Ads  Initialize
        MobileAds.initialize(this);
        //Facebook Ads Initialize
        AudienceNetworkAds.initialize(this);
    }
}

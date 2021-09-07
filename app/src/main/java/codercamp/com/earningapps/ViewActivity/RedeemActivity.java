package codercamp.com.earningapps.ViewActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.facebook.ads.AudienceNetworkAds;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import codercamp.com.earningapps.Fragment.FragmentActivity;
import codercamp.com.earningapps.R;

public class RedeemActivity extends AppCompatActivity {
    private ImageView amazonImageView;
    private CardView amazonCard;
    public Toolbar toolbar;
    private InterstitialAd mInterstitialAd;
    private com.facebook.ads.InterstitialAd FbInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redeem);

        //Google Ads  Initialize
        MobileAds.initialize(this);
        //Facebook Ads Initialize
        AudienceNetworkAds.initialize(this);
        intViews();
        LoadImage();
        ClickListener();
        LoadInterstitial();
    }


    private void intViews() {
        toolbar = findViewById(R.id.Toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        amazonImageView = findViewById(R.id.amazonImage);
        amazonCard = findViewById(R.id.AmazonGiftCard);
    }

    private void LoadImage() {
        String amazonImageUrl = "https://amusecards.com/wp-content/uploads/2018/01/super-banner-amazoncards-100-serkan-656x441.jpg";

        Glide.with(RedeemActivity.this)
                .load(amazonImageUrl)
                .into(amazonImageView);
    }


    private void ClickListener() {

        amazonCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowInterstitialAd(1);
            }
        });
    }

    private void LoadInterstitial() {

        //Google ads
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.Interstitial_ads));
        mInterstitialAd.loadAd(adRequest);

        // Facebook ads
        FbInterstitialAd = new com.facebook.ads.InterstitialAd(this, getString(R.string.fb_Interstitial));
        FbInterstitialAd.loadAd();
    }

    private void ShowInterstitialAd(int i) {

        if (mInterstitialAd.isLoaded()) {

            if (mInterstitialAd != null) {
                mInterstitialAd.show();


                mInterstitialAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();

                        if (i == 1) {
                            Intent intent = new Intent(RedeemActivity.this, FragmentActivity.class);
                            intent.putExtra("position", 1);
                            startActivity(intent);
                        }

                    }

                });
                return;

            }
        }
        /*if (FbInterstitialAd.isAdLoaded()){
            FbInterstitialAd.show();
            InterstitialAdListener interstitialAdListener = new InterstitialAdListener() {
                @Override
                public void onInterstitialDisplayed(Ad ad) {


                }

                @Override
                public void onInterstitialDismissed(Ad ad) {
                    if (i == 1){
                        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                        startActivity(intent);
                    }
                    if (i==2){
                        Intent intent = new Intent(MainActivity.this, InviteActivity.class);
                        startActivity(intent);
                    }
                    if (i==3){
                        Intent intent = new Intent(MainActivity.this, RedeemActivity.class);
                        startActivity(intent);
                    }
                    if (i==4){
                        Intent intent = new Intent(MainActivity.this, FragmentActivity.class);
                        intent.putExtra("position", 2);
                        startActivity(intent);
                    }
                    if (i==5){

                        Intent intent = new Intent(MainActivity.this, FragmentActivity.class);
                        intent.putExtra("position", 3);
                        startActivity(intent);
                    }

                }

                @Override
                public void onError(Ad ad, AdError adError) {

                }

                @Override
                public void onAdLoaded(Ad ad) {

                }

                @Override
                public void onAdClicked(Ad ad) {

                }

                @Override
                public void onLoggingImpression(Ad ad) {

                }
            };

        }*/

    }

    @Override
    public void onBackPressed() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                    finish();
                }
            });
            return;
        }
        finish();

    }

}
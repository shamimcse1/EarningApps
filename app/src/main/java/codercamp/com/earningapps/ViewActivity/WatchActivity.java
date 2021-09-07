package codercamp.com.earningapps.ViewActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdSettings;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.RewardedVideoAd;
import com.facebook.ads.RewardedVideoAdListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import codercamp.com.earningapps.Model.ProfileModel;
import codercamp.com.earningapps.R;

public class WatchActivity extends AppCompatActivity {
    private InterstitialAd mInterstitialAd;
    private com.facebook.ads.InterstitialAd FbInterstitialAd;
    private Toolbar toolbar;
    private MaterialButton watchButton, watchButton2;
    private TextView coinText;
    private RewardedVideoAd rewardedVideoAd;
    private DatabaseReference reference;
    private ProgressDialog progressDialog;
    private final String TAG = WatchActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch);

        toolbar = findViewById(R.id.watch_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        AdSettings.isTestMode(this);

        watchButton = findViewById(R.id.watchVideo);
        watchButton2 = findViewById(R.id.watchVideo2);
        coinText = findViewById(R.id.coinText);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference().child("User").child(user.getUid());


        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading....");
        //Google Ads  Initialize
        MobileAds.initialize(this);
        //Facebook Ads Initialize
        AudienceNetworkAds.initialize(this);

        LoadInterstitial();
        ClickListener();
        DataLoad();
    }


    private void LoadInterstitial() {

        //Google ads
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.Interstitial_ads));
        mInterstitialAd.loadAd(adRequest);

        // Facebook ads
        rewardedVideoAd = new RewardedVideoAd(this, getString(R.string.fb_rewarded));
        rewardedVideoAd.loadAd();
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

    }

    private void ClickListener() {
        watchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRewardAds(1);
            }
        });

        watchButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRewardAds(3);
            }
        });
    }


    private void showRewardAds(int i) {
        RewardedVideoAdListener rewardedVideoAdListener = new RewardedVideoAdListener() {
            @Override
            public void onError(Ad ad, AdError error) {
                // Rewarded video ad failed to load
                Log.e(TAG, "Rewarded video ad failed to load: " + error.getErrorMessage());
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Rewarded video ad is loaded and ready to be displayed
                Log.d(TAG, "Rewarded video ad is loaded and ready to be displayed!");
                rewardedVideoAd.show();
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Rewarded video ad clicked
                Log.d(TAG, "Rewarded video ad clicked!");
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Rewarded Video ad impression - the event will fire when the
                // video starts playing
                Log.d(TAG, "Rewarded video ad impression logged!");
            }

            @Override
            public void onRewardedVideoCompleted() {
                // Rewarded Video View Complete - the video has been played to the end.
                // You can use this event to initialize your reward
                Log.d(TAG, "Rewarded video completed!");

                // Call method to give reward
                // giveReward();

                if (i == 1) {
                    watchButton.setVisibility(View.GONE);
                    watchButton2.setVisibility(View.VISIBLE);
                }
                if (i == 2) {
                    watchButton.setVisibility(View.VISIBLE);
                    watchButton2.setVisibility(View.GONE);
                }
                if (i == 3) {
                    onBackPressed();
                }

                UpdateFirebaseData();
            }

            @Override
            public void onRewardedVideoClosed() {
                // The Rewarded Video ad was closed - this can occur during the video
                // by closing the app, or closing the end card.
                Log.d(TAG, "Rewarded video ad closed!");
            }
        };
        rewardedVideoAd.loadAd(
                rewardedVideoAd.buildLoadAdConfig()
                        .withAdListener(rewardedVideoAdListener)
                        .build());
    }

    private void UpdateFirebaseData() {

        int currentCoins = Integer.parseInt(coinText.getText().toString());
        int updatedCoins = currentCoins + 5;

        HashMap<String, Object> map = new HashMap<>();
        map.put("coins", updatedCoins);

        reference.updateChildren(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(WatchActivity.this, "Coins Added Successfully ", Toast.LENGTH_SHORT).show();

                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(WatchActivity.this, "Error " + e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }


    private void DataLoad() {
        progressDialog.dismiss();
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                ProfileModel model = dataSnapshot.getValue(ProfileModel.class);
                if (model != null) {
                    coinText.setText(String.valueOf(model.getCoins()));
                    progressDialog.dismiss();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(WatchActivity.this, "Error " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (rewardedVideoAd != null) {
            rewardedVideoAd.destroy();
            rewardedVideoAd = null;
        }
        super.onDestroy();
    }
}
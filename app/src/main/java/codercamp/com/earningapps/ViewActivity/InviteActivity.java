package codercamp.com.earningapps.ViewActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.facebook.ads.AudienceNetworkAds;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import codercamp.com.earningapps.Model.ProfileModel;
import codercamp.com.earningapps.R;

public class InviteActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private String opposedUserId;
    private Button ShareButton, RedeemButton;
    private Toolbar toolbar;
    private TextView referCode;
    private DatabaseReference reference;
    private InterstitialAd mInterstitialAd;
    private com.facebook.ads.InterstitialAd FbInterstitialAd ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);

        initView();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Google Ads  Initialize
        MobileAds.initialize(this);
        //Facebook Ads Initialize
        AudienceNetworkAds.initialize(this);

        LoadInterstitial();

        LoadData();
        Share();
        Redeem();
        RedeemAvailability();

    }


    private void initView() {
        toolbar = findViewById(R.id.Toolbar);
        setSupportActionBar(toolbar);
        referCode = findViewById(R.id.ReferCodes);
        ShareButton = findViewById(R.id.ShareCode);
        RedeemButton = findViewById(R.id.RedeemCode);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference().child("User");

    }


    private void LoadData() {
        reference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String ReferCode = snapshot.child("referCode").getValue(String.class);
                referCode.setText("Your Refer Code : "+ ReferCode);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(InviteActivity.this, "Error :" + error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void Share() {

        ShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowInterstitialAd(1);
            }
        });

    }

    private void Redeem() {
        RedeemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = new EditText(InviteActivity.this);
                editText.setHint("abc123");

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                editText.setLayoutParams(layoutParams);


                AlertDialog.Builder alertDialog = new AlertDialog.Builder(InviteActivity.this);
                alertDialog.setTitle("Redeem Code");
                alertDialog.setCancelable(false);
                alertDialog.setView(editText);

                alertDialog.setPositiveButton("Redeem", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String Input = editText.getText().toString();
                        if (TextUtils.isEmpty(Input)) {
                            Toast.makeText(InviteActivity.this, "Input Valid Code", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (Input.equals(referCode.getText().toString())) {
                            Toast.makeText(InviteActivity.this, "You can not input your own code", Toast.LENGTH_SHORT).show();
                        }
                        RedeemQuery(Input, dialog);

                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                alertDialog.show();

            }
        });
    }

    private void RedeemQuery(String input, final DialogInterface dialog) {
        Query query = reference.orderByChild("referCode").equalTo(input);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    opposedUserId = dataSnapshot.getKey();
                    reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    ProfileModel model = snapshot.child(opposedUserId).getValue(ProfileModel.class);
                                    ProfileModel MyModel = snapshot.child(user.getUid()).getValue(ProfileModel.class);
                                    if (model != null) {

                                        int coins = model.getCoins();
                                        int updateCoins = coins + 100;
                                        int myCoins = MyModel.getCoins();
                                        int myUpdateCoins = myCoins + 100;

                                        HashMap<String, Object> map = new HashMap<>();
                                        map.put("coins", updateCoins);

                                        HashMap<String, Object> Mymap = new HashMap<>();
                                        Mymap.put("coins", myUpdateCoins);
                                        Mymap.put("redeemed", true);


                                        reference.child(opposedUserId).updateChildren(map);
                                        reference.child(user.getUid()).updateChildren(Mymap)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        dialog.dismiss();
                                                        Toast.makeText(InviteActivity.this, "Congrats", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(InviteActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(InviteActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void RedeemAvailability() {

        reference.child(user.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {


                        if (snapshot.exists() && snapshot.hasChild("redeemed")){
                            boolean isAvailable =snapshot.child("redeemed").getValue(boolean.class);
                            if (isAvailable){
                                RedeemButton.setVisibility(View.GONE);
                                RedeemButton.setEnabled(false);
                            }
                            else {
                                RedeemButton.setVisibility(View.VISIBLE);
                                RedeemButton.setEnabled(true);
                            }
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        Toast.makeText(InviteActivity.this, "Error "+error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void LoadInterstitial(){

        //Google ads
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.Interstitial_ads));
        mInterstitialAd.loadAd(adRequest);

        // Facebook ads
        FbInterstitialAd = new com.facebook.ads.InterstitialAd(this,getString(R.string.fb_Interstitial));
        FbInterstitialAd.loadAd();
    }


    private void ShowInterstitialAd(int i){

        if (mInterstitialAd.isLoaded()){

            if (mInterstitialAd != null){
                mInterstitialAd.show();


                mInterstitialAd.setAdListener(new AdListener(){
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();

                        if (i == 1){ // Share
                            String refer = referCode.getText().toString();
                            String ShareBodyText = "Hi!!, I'm using the best earning app. Join using my Invite code to instantly get 100" +
                                    "coins. My invite code is " + refer + "\n" +
                                    "Download from Play Store\n" + "https://play.google.com/store/apps/details?id=" +
                                    getPackageName();
                            ;
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.putExtra(Intent.EXTRA_TEXT, ShareBodyText);
                            intent.setType("text/plain");
                            startActivity(intent);
                        }
//                        if (i ==2){
//                            Intent intent = new Intent(InviteActivity.this, MainActivity.class);
//                            startActivity(intent);
//                        }
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
        if(mInterstitialAd.isLoaded()){
            mInterstitialAd.show();
            mInterstitialAd.setAdListener(new AdListener(){
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
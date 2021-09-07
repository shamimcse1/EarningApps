package codercamp.com.earningapps;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdSettings;
import com.facebook.ads.InterstitialAdListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pollfish.Pollfish;
import com.pollfish.builder.Params;
import com.pollfish.builder.Position;
import com.pollfish.callback.PollfishClosedListener;
import com.pollfish.callback.PollfishOpenedListener;
import com.pollfish.callback.PollfishSurveyCompletedListener;
import com.pollfish.callback.PollfishSurveyReceivedListener;
import com.pollfish.callback.SurveyInfo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;
import codercamp.com.earningapps.Fragment.FragmentActivity;
import codercamp.com.earningapps.Model.ProfileModel;
import codercamp.com.earningapps.ViewActivity.InviteActivity;
import codercamp.com.earningapps.ViewActivity.ProfileActivity;
import codercamp.com.earningapps.ViewActivity.RedeemActivity;
import codercamp.com.earningapps.ViewActivity.WatchActivity;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements PollfishSurveyCompletedListener, PollfishOpenedListener, PollfishSurveyReceivedListener, PollfishClosedListener {
    private CardView dailyCheckCard, luckyCard, taskCard, referCard, redeemCard, watchCard, aboutCard;
    private TextView userName, userEmail, coins;
    private CircleImageView userImage;
    private Toolbar toolbar;
    private DatabaseReference reference;
    private FirebaseUser firebaseUser;
    private Dialog dialog;
    private InternetCheck internetCheck;
    private AdView adView;
    private InterstitialAd mInterstitialAd;
    private com.facebook.ads.InterstitialAd FbInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        checkInternetConnection();
        setSupportActionBar(toolbar);

        AdSettings.isTestMode(this);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference().child("User");


        //Ads
        BannerAds();
        LoadInterstitial();

        getDataFromDatabase();

        UpdateProfile();
        RedeemCard();
        DailyChecked();
        Redeem();
        LuckyCard();
        AboutSection();
        watchActivity();


    }


    private void initViews() {
        dailyCheckCard = findViewById(R.id.dailyCheckCard);
        luckyCard = findViewById(R.id.LuckySpinCard);
        taskCard = findViewById(R.id.taskCard);
        referCard = findViewById(R.id.referCard);
        redeemCard = findViewById(R.id.redeemCard);
        watchCard = findViewById(R.id.watchCard);
        aboutCard = findViewById(R.id.aboutCard);

        userName = findViewById(R.id.NameTv);
        userImage = findViewById(R.id.UserProfile);
        userEmail = findViewById(R.id.EmailTv);
        coins = findViewById(R.id.coinsTV);
        toolbar = findViewById(R.id.toolbar);

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.loding_dialog);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.setCancelable(false);

        internetCheck = new InternetCheck(MainActivity.this);

    }

    //Get Data Form Database
    private void getDataFromDatabase() {
        //TODO: 22-07-2021
        // dialog.show();

        reference.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                ProfileModel model = snapshot.getValue(ProfileModel.class);

                if (model != null) {
                    userName.setText(model.getName());
                    userEmail.setText(model.getEmail());
                    coins.setText(String.valueOf(model.getCoins()));
                    Glide.with(getApplicationContext()).load(model.getImage()).timeout(16000)
                            .placeholder(R.drawable.profile).into(userImage);
                    Log.d("coin", String.valueOf(model.getCoins()));

                    dialog.dismiss();


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(MainActivity.this, "Error" + error.getMessage(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                finish();
            }
        });
    }

    //Update Profile
    private void UpdateProfile() {
        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowInterstitialAd(1);

            }
        });
    }

    //Redeem Method
    private void RedeemCard() {

        referCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowInterstitialAd(2);
            }
        });
    }

    @Override
    public void onPollfishClosed() {

        Toast.makeText(this, "Survey Close", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPollfishSurveyReceived(@Nullable SurveyInfo surveyInfo) {
        Toast.makeText(this, "Survey Received", Toast.LENGTH_SHORT).show();

    }


    // Internet Connection Checking Class
    @SuppressLint("StaticFieldLeak")
    class isInternetActive extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            InputStream inputStream = null;
            String json = "";

            try {
                String strUrl = "https://icons.iconarchive.com/icons/martz90/circle/256/android-icon.png";
                URL url = new URL(strUrl);

                URLConnection urlConnection = url.openConnection();
                urlConnection.setDoInput(true);
                inputStream = urlConnection.getInputStream();
                json = "success";
            } catch (Exception e) {
                e.printStackTrace();
                json = "failed";


            }
            return json;
        }

        @Override
        protected void onPostExecute(String s) {
            //super.onPostExecute(s);
            if (s != null) {
                if (s.equals("success")) {
                    // Toast.makeText(MainActivity.this, "Internet Connected", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Please Wait for Connection", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(MainActivity.this, "Please Wait", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {
            //Toast.makeText(MainActivity.this, "Validating Internet", Toast.LENGTH_SHORT).show();
            super.onPreExecute();
        }
    }

    // Internet Connection Checking Method
    private void checkInternetConnection() {
        if (internetCheck.isConnected()) {
            new isInternetActive().execute();

        } else {
            Toast.makeText(MainActivity.this, "Please Check your Internet", Toast.LENGTH_SHORT).show();
        }
    }


    //Daily Check Main Method
    private void DailyChecked() {

        dailyCheckCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (internetCheck.isConnected()) {
                    dailyCheck();
                } else {
                    Toast.makeText(MainActivity.this, "Please Check your Internet Connection", Toast.LENGTH_LONG).show();
                }

            }

        });
    }

    //Daily Check Sub Method
    private void dailyCheck() {

        final SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.setTitleText("Please Wait");
        sweetAlertDialog.setCancelable(false);
        sweetAlertDialog.show();

        final Date currentDate = Calendar.getInstance().getTime();

        final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        reference.child("Daily Check").child(firebaseUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String dateString = snapshot.child("date").getValue(String.class);
                            try {

                                if (dateString != null) {
                                    Date mDate = dateFormat.parse(dateString);

                                    String xDate = dateFormat.format(currentDate);
                                    Date date = dateFormat.parse(xDate);


                                    assert date != null;
                                    if (date.after(mDate) && date.compareTo(currentDate) != 0) {

                                        reference.child("User").child(firebaseUser.getUid()).
                                                addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        ProfileModel model = snapshot.getValue(ProfileModel.class);
                                                        if (model != null) {
                                                            int currentCoins = model.getCoins();
                                                            int updatedCoins = currentCoins + 10;

                                                            int spinC = model.getSpins();
                                                            int UpdateSpins = spinC + 2;

                                                            HashMap<String, Object> map = new HashMap<>();
                                                            map.put("coins", updatedCoins);
                                                            map.put("spins", UpdateSpins);

                                                            reference.child("User").child(firebaseUser.getUid())
                                                                    .updateChildren(map);

                                                            Date newDate = Calendar.getInstance().getTime();
                                                            String newDateString = dateFormat.format(newDate);

                                                            HashMap<String, String> DateMap = new HashMap<>();
                                                            DateMap.put("date", newDateString);

                                                            reference.child("Daily Check").child(firebaseUser.getUid())
                                                                    .setValue(DateMap)
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            sweetAlertDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                                                            sweetAlertDialog.setTitleText("Success");
                                                                            sweetAlertDialog.setContentText("Coins Added to your account Successfully");
                                                                            sweetAlertDialog.setConfirmButton("OK", new SweetAlertDialog.OnSweetClickListener() {
                                                                                @Override
                                                                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                                                    sweetAlertDialog.dismissWithAnimation();
                                                                                }
                                                                            });
                                                                            sweetAlertDialog.show();
                                                                        }
                                                                    });

                                                        }


                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                        Toast.makeText(MainActivity.this, "Error : " + error.getMessage(), Toast.LENGTH_SHORT).show();

                                                    }
                                                });

                                    } else {
                                        sweetAlertDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                                        sweetAlertDialog.setTitleText("Failed");
                                        sweetAlertDialog.setContentText("You have already rewarded, come to back tomorrow");
                                        sweetAlertDialog.setConfirmButton("Dismiss", null);
                                        sweetAlertDialog.show();
                                    }

                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                sweetAlertDialog.dismissWithAnimation();
                            }
                        } else {
                            sweetAlertDialog.changeAlertType(SweetAlertDialog.WARNING_TYPE);
                            sweetAlertDialog.setTitleText("System Busy");
                            sweetAlertDialog.setContentText("System is busy now, Please try again letter");
                            sweetAlertDialog.setConfirmButton("Dismiss", new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    sweetAlertDialog.dismissWithAnimation();
                                }
                            });
                            sweetAlertDialog.show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        Toast.makeText(MainActivity.this, "Error : " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        sweetAlertDialog.dismissWithAnimation();
                    }
                });
    }

    private void Redeem() {
        redeemCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowInterstitialAd(3);
            }
        });
    }

    private void LuckyCard() {

        luckyCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowInterstitialAd(4);
            }
        });
    }


    private void AboutSection() {

        aboutCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowInterstitialAd(5);
            }
        });
    }


    private void watchActivity() {

        watchCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowInterstitialAd(6);
            }
        });

    }


    // Ads Area

    private void BannerAds() {
        //ad mob Banner Ads
        adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);


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

        if (FbInterstitialAd.isAdLoaded()) {
            FbInterstitialAd.show();

            InterstitialAdListener adListener = new InterstitialAdListener() {
                @Override
                public void onInterstitialDisplayed(Ad ad) {
                }

                @Override
                public void onInterstitialDismissed(Ad ad) {
                    if (i == 1) {
                        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                        startActivity(intent);
                    }
                    if (i == 2) {
                        Intent intent = new Intent(MainActivity.this, InviteActivity.class);
                        startActivity(intent);
                    }
                    if (i == 3) {
                        Intent intent = new Intent(MainActivity.this, RedeemActivity.class);
                        startActivity(intent);
                    }
                    if (i == 4) {
                        Intent intent = new Intent(MainActivity.this, FragmentActivity.class);
                        intent.putExtra("position", 2);
                        startActivity(intent);
                    }
                    if (i == 5) {

                        Intent intent = new Intent(MainActivity.this, FragmentActivity.class);
                        intent.putExtra("position", 3);
                        startActivity(intent);
                    }
                    if (i == 6) {
                        Intent intent = new Intent(MainActivity.this, WatchActivity.class);
                        startActivity(intent);
                    }

                }

                @Override
                public void onError(Ad ad, AdError adError) {

                }

                @Override
                public void onAdLoaded(Ad ad) {
                    // Interstitial ad is loaded and ready to be displayed
                    Log.d("TAG", "Interstitial ad is loaded and ready to be displayed!");
                    // Show the ad
                    FbInterstitialAd.show();

                }

                @Override
                public void onAdClicked(Ad ad) {

                }

                @Override
                public void onLoggingImpression(Ad ad) {

                }
            };
            FbInterstitialAd.loadAd(
                    FbInterstitialAd.buildLoadAdConfig()
                            .withAdListener(adListener)
                            .build());

            return;
        }

        if (mInterstitialAd.isLoaded()) {

            if (mInterstitialAd != null) {
                mInterstitialAd.show();


                mInterstitialAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();

                        if (i == 1) {
                            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                            startActivity(intent);
                        }
                        if (i == 2) {
                            Intent intent = new Intent(MainActivity.this, InviteActivity.class);
                            startActivity(intent);
                        }
                        if (i == 3) {
                            Intent intent = new Intent(MainActivity.this, RedeemActivity.class);
                            startActivity(intent);
                        }
                        if (i == 4) {
                            Intent intent = new Intent(MainActivity.this, FragmentActivity.class);
                            intent.putExtra("position", 2);
                            startActivity(intent);
                        }
                        if (i == 5) {

                            Intent intent = new Intent(MainActivity.this, FragmentActivity.class);
                            intent.putExtra("position", 3);
                            startActivity(intent);
                        }
                        if (i == 6) {
                            Intent intent = new Intent(MainActivity.this, WatchActivity.class);
                            startActivity(intent);
                        }
                    }

                });
                return;

            }
        }

        if (i == 1) {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        }
        if (i == 2) {
            Intent intent = new Intent(MainActivity.this, InviteActivity.class);
            startActivity(intent);
        }
        if (i == 3) {
            Intent intent = new Intent(MainActivity.this, RedeemActivity.class);
            startActivity(intent);
        }
        if (i == 4) {
            Intent intent = new Intent(MainActivity.this, FragmentActivity.class);
            intent.putExtra("position", 2);
            startActivity(intent);
        }
        if (i == 5) {

            Intent intent = new Intent(MainActivity.this, FragmentActivity.class);
            intent.putExtra("position", 3);
            startActivity(intent);
        }
        if (i == 6) {
            Intent intent = new Intent(MainActivity.this, WatchActivity.class);
            startActivity(intent);
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
    protected void onResume() {
        super.onResume();
        Params params = new Params.Builder("API_KEY")
                .requestUUID(firebaseUser.getUid())
                .releaseMode(false)
                .indicatorPosition(Position.MIDDLE_RIGHT)
                .indicatorPadding(10)
                .build();

        Pollfish.initWith(this, params);
    }


    @Override
    public void onPollfishOpened() {

        Toast.makeText(this, "Survey Opend", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPollfishSurveyCompleted(@NotNull SurveyInfo surveyInfo) {

        dataUpdate(surveyInfo);
    }


    private void dataUpdate(SurveyInfo surveyInfo) {

        int reward = surveyInfo.getRewardValue();
        int currentCoins = Integer.parseInt(coins.getText().toString());
        int updateCoins = currentCoins + reward;

        HashMap<String, Object> map = new HashMap<>();
        map.put("coins", updateCoins);

        reference.child(firebaseUser.getUid()).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Coins Added Successfully", Toast.LENGTH_SHORT).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Error " + e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

}
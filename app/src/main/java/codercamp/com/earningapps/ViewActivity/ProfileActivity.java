package codercamp.com.earningapps.ViewActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.facebook.ads.AudienceNetworkAds;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import codercamp.com.earningapps.Fragment.FragmentActivity;
import codercamp.com.earningapps.LoginAndRegister.LoginActivity;
import codercamp.com.earningapps.MainActivity;
import codercamp.com.earningapps.Model.ProfileModel;
import codercamp.com.earningapps.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private CircleImageView profileImage;
    private TextView nameTextView, emailTextView, shareTextView, redeemHistoryTextView, coinsTextView, logoutTextView;
    private ImageButton EditImage;
    private Button UpdateProfileBtn;
    private Toolbar toolbar;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference reference;
    private static final int IMAGE_PICKER = 1;
    private Uri PhotoUri;
    private String ImageUrl;
    private  ProgressDialog progressDialog;
    private InterstitialAd mInterstitialAd;
    private com.facebook.ads.InterstitialAd FbInterstitialAd ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        //Google Ads  Initialize
        MobileAds.initialize(this);
        //Facebook Ads Initialize
        AudienceNetworkAds.initialize(this);

        initViews();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        LoadDataFromDatabase();
        LogOut();
        Share();
        EditImage();
        LoadInterstitial();

    }


    private void initViews() {
        profileImage = findViewById(R.id.UserProfile);
        nameTextView = findViewById(R.id.NameTX);
        emailTextView = findViewById(R.id.EmailTX);
        shareTextView = findViewById(R.id.shareTX);
        redeemHistoryTextView = findViewById(R.id.RedeemTX);
        coinsTextView = findViewById(R.id.coinTX);
        logoutTextView = findViewById(R.id.LogoutTX);
        EditImage = findViewById(R.id.EditImage);
        UpdateProfileBtn = findViewById(R.id.UpdateBtn);
        toolbar = findViewById(R.id.ProfileToolbar);
        setSupportActionBar(toolbar);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference().child("User");

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCancelable(false);


    }

    private void LoadDataFromDatabase() {

        reference.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                ProfileModel model = snapshot.getValue(ProfileModel.class);

                if (model != null) {
                    nameTextView.setText(model.getName());
                    emailTextView.setText(model.getEmail());
                    coinsTextView.setText(String.valueOf(model.getCoins()));

                    Glide.with(getApplicationContext()).load(model.getImage()).timeout(6000)
                            .placeholder(R.drawable.profile).into(profileImage);
                    Log.d("coin", String.valueOf(model.getCoins()));


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(ProfileActivity.this, "Error" + error.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void LogOut() {

        logoutTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    private void Share() {

        shareTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ShareBodyText = "Check out the best Earning app. Download " + getString(R.string.app_name) +
                        "from Play Store\n" + "https://play.google.com/store/apps/details?id=" +
                        getPackageName();
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, ShareBodyText);
                intent.setType("text/plain");
                startActivity(intent);
            }
        });
    }

    private void EditImage() {
        EditImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dexter.withContext(ProfileActivity.this)
                        .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                        .withListener(new MultiplePermissionsListener() {
                            @Override
                            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {

                                if (multiplePermissionsReport.areAllPermissionsGranted()) {
                                    Intent intent = new Intent(Intent.ACTION_PICK);
                                    intent.setType("image/*");
                                    startActivityForResult(intent,IMAGE_PICKER);

                                } else {
                                    Toast.makeText(ProfileActivity.this, "Please Select an Image", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

                            }
                        }).check();

            }
        });

        UpdateProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               UploadImage();
            }
        });
    }

    private void UploadImage() {

        Log.d("tag","UploadImage call");

        String FileName = user.getUid()+".jpg";
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getInstance().getReference().child("UserImage/"+FileName);

        progressDialog.show();
        storageReference.putFile(PhotoUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {

                    @Override
                    public void onSuccess(Uri uri) {
                        ImageUrl = uri.toString();
                        SaveImageInDatabase();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(ProfileActivity.this, "Error :" +e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                long total = snapshot.getTotalByteCount();
                long transfer =snapshot.getBytesTransferred();
                progressDialog.setMessage("Uploaded "+((int)transfer) +" / "+((int)total));
            }
        });
    }

    private void SaveImageInDatabase() {

        HashMap<String, Object> map = new HashMap<>();
        map.put("image", ImageUrl);
        reference.child(user.getUid()).updateChildren(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        UpdateProfileBtn.setVisibility(View.GONE);
                        progressDialog.dismiss();
                    }
                });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICKER && resultCode == RESULT_OK ){
            if (data != null){
                PhotoUri =data.getData();
                UpdateProfileBtn.setVisibility(View.VISIBLE);
                profileImage.setImageURI(PhotoUri);
            }
        }
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
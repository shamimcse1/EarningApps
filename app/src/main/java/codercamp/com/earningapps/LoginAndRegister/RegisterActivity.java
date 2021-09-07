package codercamp.com.earningapps.LoginAndRegister;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import codercamp.com.earningapps.R;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText username, email, password, confirmPassword;
    private Button RegisterButton;
    private TextView Singup;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;
    private String deviceID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        Register();
        GoTO_LoginActivity();
        firebaseAuth = FirebaseAuth.getInstance();

    }

    private void initViews() {

        username = findViewById(R.id.UsernameTxt);
        email = findViewById(R.id.EmailTxt);
        password = findViewById(R.id.PasswordTxt);
        confirmPassword = findViewById(R.id.ConfPasswordTxt);
        RegisterButton = findViewById(R.id.Registerbtn);
        progressBar = findViewById(R.id.progressBar);
        Singup = findViewById(R.id.LoginTxt);


    }

    private void GoTO_LoginActivity() {
        Singup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void Register() {
        RegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String UserName = username.getText().toString().trim();
                String Email = email.getText().toString().trim();
                String Password = password.getText().toString().trim();
                String ConfirmPassword = confirmPassword.getText().toString().trim();

                if (UserName.isEmpty()) {
                    username.setError("Name Required");
                    return;
                }
                if (Email.isEmpty()) {
                    email.setError("Email Required");
                    return;
                }
                if (Password.isEmpty()) {
                    password.setError("Password Required");
                    return;
                }
                if (ConfirmPassword.isEmpty() || !Password.equals(ConfirmPassword)) {
                    confirmPassword.setError("Your password and confirmation password do not match!!");
                    return;
                }


                QueryAccountExistance(Email, Password);

            }
        });

    }

    private void CreateAccount(String email, String password) {
        progressBar.setVisibility(View.VISIBLE);

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            //OK
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            assert user != null;

                            //Email Verification
                            firebaseAuth.getCurrentUser().sendEmailVerification()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                UpdateUI(user, email);
                                                progressBar.setVisibility(View.GONE);
                                            } else {
                                                progressBar.setVisibility(View.GONE);
                                                Toast.makeText(RegisterActivity.this, "Error" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }

                                        }

                                    });


                        } else {
                            Toast.makeText(RegisterActivity.this, "Error with " + task.getException().getMessage() + " That's Why Registration Failed", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

    }

    @SuppressLint("HardwareIds")
    private void QueryAccountExistance(String email, String password) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("User");

        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        Query query = reference.orderByChild("deviceID").equalTo(deviceID);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    progressBar.setVisibility(View.GONE);

                    Log.d("deviceID", deviceID);
                    Toast.makeText(RegisterActivity.this, "This device is already registered om another email, please login", Toast.LENGTH_SHORT).show();
                } else {

                    //UpdateUI(user,email);
                    CreateAccount(email, password);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(RegisterActivity.this, "Error"+error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    @SuppressLint("HardwareIds")
    private void UpdateUI(FirebaseUser user, String eEmail) {

        progressBar.setVisibility(View.VISIBLE);

        String refer = eEmail.substring(0, eEmail.lastIndexOf("@"));
        String referCode = refer.replace(".", "");
        String name = Objects.requireNonNull(username.getText()).toString();

        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);


        HashMap<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("email", eEmail);
        map.put("image", " ");
        map.put("uid", user.getUid());
        map.put("coins", 0);
        map.put("referCode", referCode);
        map.put("spins", 2);
        map.put("deviceID", deviceID);


        Date newDate = Calendar.getInstance().getTime();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(newDate);
        calendar.add(Calendar.DAY_OF_MONTH, -1);//get yesterday date

        Date previewsDate = calendar.getTime();
        String dateString = dateFormat.format(previewsDate);

        FirebaseDatabase.getInstance().getReference().child("Daily Check").child(user.getUid()).child("date").setValue(dateString);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("User");

        reference.child(user.getUid())
                .setValue(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            //OK
                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(RegisterActivity.this, "Error" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }
}
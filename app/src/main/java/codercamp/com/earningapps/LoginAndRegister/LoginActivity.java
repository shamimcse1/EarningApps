package codercamp.com.earningapps.LoginAndRegister;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
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

import codercamp.com.earningapps.MainActivity;
import codercamp.com.earningapps.R;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText email, password;
    private Button LoginButton;
    private TextView SingUp;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private String deviceID;

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        auth = FirebaseAuth.getInstance();
        iniViews();
        Goto_RegisterActivity();
        deviceID = Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID);

        LogIn();

    }

    private void iniViews() {

        email = findViewById(R.id.LogEmailTxt);
        password = findViewById(R.id.LogPasswordTxt);
        SingUp = findViewById(R.id.Sing_upTxt);
        progressBar = findViewById(R.id.progressBar);
        LoginButton = findViewById(R.id.LoginBtn);

    }

    private void Goto_RegisterActivity() {
        SingUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

    }

    private void LogIn() {

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Email = email.getText().toString();
                String Password = password.getText().toString();

                if (Email.isEmpty()) {
                    email.setError("Enter Valid Email");
                    return;
                }
                if (Password.isEmpty()) {
                    password.setError("Enter Valid Password");
                    return;
                }

                LogInNow(Email, Password);
            }
        });

    }

    private void LogInNow(String email, String password) {


        progressBar.setVisibility(View.VISIBLE);

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            //LogIn Success
                            FirebaseUser user = auth.getCurrentUser();
                            //Check if user is verified

                            if (user.isEmailVerified()) {

                                progressBar.setVisibility(View.GONE);
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();

                            } else {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(LoginActivity.this, "Please Verify your email", Toast.LENGTH_SHORT).show();
                            }


                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, "Login Failed" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }



}
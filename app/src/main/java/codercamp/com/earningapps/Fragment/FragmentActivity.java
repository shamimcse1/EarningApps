package codercamp.com.earningapps.Fragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.TextView;

import codercamp.com.earningapps.R;

public class FragmentActivity extends AppCompatActivity {
    private FrameLayout frameLayout;
    private Toolbar toolbar;
    private TextView toolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        toolbar = findViewById(R.id.Toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        frameLayout = findViewById(R.id.fragment_container);
        toolbarTitle = findViewById(R.id.toolbarName);

        int position = getIntent().getIntExtra("position", 0);


        if (position == 1) {
            if (getSupportActionBar() != null){
                getSupportActionBar().setTitle("Amazon");
                toolbarTitle.setText("Amazon");
            }

            FragmentReplacer(new AmazonFragment());
        }
        if (position == 2) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Lucky Spin");
                toolbarTitle.setText("Lucky Spin");
            }
            FragmentReplacer(new LuckySpinFragment());
        }
        if (position == 3) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("About");
                toolbarTitle.setText("About");
            }
            FragmentReplacer(new AboutFragment());
        }


    }

    private void FragmentReplacer(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        transaction.replace(frameLayout.getId(), fragment);
        transaction.commit();

    }
}
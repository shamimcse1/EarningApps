package codercamp.com.earningapps.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import codercamp.com.earningapps.Model.ProfileModel;
import codercamp.com.earningapps.R;
import codercamp.com.earningapps.Spin.SpinModel;
import codercamp.com.earningapps.Spin.WheelView;


public class LuckySpinFragment extends Fragment {

    private Button playBtn;
    private WheelView wheelView;
    private TextView coin;
    private List<SpinModel> spinModelList = new ArrayList<>();
    private FirebaseUser user;
    private DatabaseReference reference;
    private  int currentSpin;


    public LuckySpinFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_lucky_spin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView(view);
        loadData();
        clickListener();
        spinList();
    }

    private void initView(View view) {

        playBtn = view.findViewById(R.id.playBtn);
        wheelView = view.findViewById(R.id.wheelView);
        coin = view.findViewById(R.id.txtCoin);
        FirebaseAuth auth = FirebaseAuth.getInstance();

        reference = FirebaseDatabase.getInstance().getReference().child("User");
        user = auth.getCurrentUser();

    }

    private void spinList() {

        SpinModel item1 = new SpinModel();
        item1.txt = "4"; //You can change according to your need
        item1.color = 0xffFFF3E0; //Change background color
        spinModelList.add(item1);

        SpinModel item2 = new SpinModel();
        item2.txt = "2";
        item2.color = 0xffFFE0B2;
        spinModelList.add(item2);

        SpinModel item3 = new SpinModel();
        item3.txt = "3";
        item3.color = 0xffFFCC80;
        spinModelList.add(item3);

        SpinModel item4 = new SpinModel();
        item4.txt = "7";
        item4.color = 0xffFFF3E0;
        spinModelList.add(item4);

        SpinModel item5 = new SpinModel();
        item5.txt = "6";
        item5.color = 0xffFFE0B2;
        spinModelList.add(item5);

        SpinModel item6 = new SpinModel();
        item6.txt = "5";
        item6.color = 0xffFFCC80;
        spinModelList.add(item6);

        SpinModel item7 = new SpinModel();
        item7.txt = "8";
        item7.color = 0xffFFF3E0;
        spinModelList.add(item7);

        SpinModel item8 = new SpinModel();
        item8.txt = "10";
        item8.color = 0xffFFE0B2;
        spinModelList.add(item8);


        SpinModel item9 = new SpinModel();
        item9.txt = "9";
        item9.color = 0xffFFCC80;
        spinModelList.add(item9);

        SpinModel item10 = new SpinModel();
        item10.txt = "12";
        item10.color = 0xffFFF3E0;
        spinModelList.add(item10);

        SpinModel item11 = new SpinModel();
        item11.txt = "11";
        item11.color = 0xffFFE0B2;
        spinModelList.add(item11);

        SpinModel item12 = new SpinModel();
        item12.txt = "1";
        item12.color = 0xffFFCC80;
        spinModelList.add(item12);

        wheelView.setData(spinModelList);
        wheelView.setRound(getRandCircleRound());

        wheelView.LuckyRoundItemSelectedListener(new WheelView.LuckyRoundItemSelectedListener() {
            @Override
            public void LuckyRoundItemSelected(int index) {

                playBtn.setEnabled(true);
                playBtn.setAlpha(1f);

                String value = spinModelList.get(index - 1).txt;


                updateDataFirebase(Integer.parseInt(value));


            }
        });

    }

    private void clickListener() {

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int index = getRandomIndex();

                if (currentSpin >=1 && currentSpin < 3){
                    wheelView.startWheelWithTargetIndex(index);
                    Toast.makeText(getContext(), "Watch video to get more spins", Toast.LENGTH_LONG).show();
                }

                if(currentSpin < 1){
                    playBtn.setEnabled(false);
                    playBtn.setAlpha(.6f);
                    Toast.makeText(getContext(), "Watch video to get more spins", Toast.LENGTH_LONG).show();
                }
                else {
                    playBtn.setEnabled(false);
                    playBtn.setAlpha(.6f);
                    wheelView.startWheelWithTargetIndex(index);
                }


            }
        });

    }

    private int getRandomIndex() {
        int[] index = new int[]{1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 6, 6, 7, 7, 9, 9, 10, 11, 12};

        int random = new Random().nextInt(index.length);

        return index[random];

    }

    private int getRandCircleRound() {
        Random random = new Random();

        return random.nextInt(10) + 15;
    }

    private void loadData() {

        reference.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ProfileModel model = snapshot.getValue(ProfileModel.class);

                assert model != null;
                coin.setText(String.valueOf(model.getCoins()));

                currentSpin = model.getSpins();

                String currentSpinner = String.valueOf(currentSpin);
                playBtn.setText("Spin the Wheel " + currentSpinner);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(getContext(), "Error : " + error.getMessage(), Toast.LENGTH_SHORT).show();
                if (getActivity() != null)
                    getActivity().finish();
            }
        });

    }

    private  void  updateDataFirebase(int rewardValue){

        int currentCoin = Integer.parseInt(coin.getText().toString());
        int updateCoin = currentCoin + rewardValue;
        int updateSpin = currentSpin -1;

        HashMap<String, Object> map = new HashMap<>();

        map.put("coins", updateCoin);
        map.put("spins",updateSpin);

        reference.child(user.getUid()).updateChildren(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){
                            Toast.makeText(getContext(), "Coins added", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            if (task.getException() != null)
                            Toast.makeText(getContext(), "Error : "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }


}
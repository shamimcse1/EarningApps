package codercamp.com.earningapps.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.printservice.PrintDocument;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import codercamp.com.earningapps.Model.AmazonCardModel;
import codercamp.com.earningapps.Model.ProfileModel;
import codercamp.com.earningapps.R;


public class AmazonFragment extends Fragment {

    private RadioGroup radioGroup;
    private Button withdrawBtn;
    private TextView coinsText;
    private DatabaseReference reference;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private String uName;
    private String uEmail;
    private Dialog dialog;


    public AmazonFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_amazon, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);

        LoadData();
        Withdraw();
    }


    private void initView(View view) {

        radioGroup = view.findViewById(R.id.radioGroup);
        withdrawBtn = view.findViewById(R.id.Withdraw);
        coinsText = view.findViewById(R.id.TotalCoin);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference().child("User");

        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.loding_dialog);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.setCancelable(false);


    }

    private void LoadData() {
        dialog.show();
        reference.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                ProfileModel model = snapshot.getValue(ProfileModel.class);

                if (model != null) {

                    coinsText.setText(String.valueOf(model.getCoins()));
                    uName = model.getName();
                    uEmail = model.getEmail();

                    dialog.dismiss();


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error : " + error.getMessage(), Toast.LENGTH_SHORT).show();
                getActivity().finish();
                dialog.dismiss();
            }
        });
    }

    private void Withdraw() {

        withdrawBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Dexter.withContext(getActivity()).withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE).withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {

                        if (multiplePermissionsReport.areAllPermissionsGranted()) {

                            String FilePath = Environment.getExternalStorageDirectory() + "/Earning App/Amazon Gift Card/";
                            File file = new File(FilePath);
                            file.mkdirs();


                            int currentCoins = Integer.parseInt(coinsText.getText().toString());
                            int checkID = radioGroup.getCheckedRadioButtonId();
                            switch (checkID) {

                                case R.id.Amazon25:
                                    AmazonCard(25, currentCoins);
                                    break;
                                case R.id.Amazon50:
                                    AmazonCard(50, currentCoins);
                                    break;
                                default:
                                    break;

                            }

                        } else {
                            Toast.makeText(getActivity(), "Please allow Permissions", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

                    }
                }).check();


            }
        });
    }

    private void AmazonCard(int amazonCard, int currentCoins) {

        if (amazonCard == 25) {
            if (currentCoins >= 6000) {
                sendGiftCard(1);
            } else {
                Toast.makeText(getContext(), "You don't have enough Coins for Withdraw ", Toast.LENGTH_LONG).show();
            }

        } else if (amazonCard == 50) {
            if (currentCoins >= 12000) {
                sendGiftCard(2);
            } else {
                Toast.makeText(getContext(), "You don't have enough Coins for Withdraw ", Toast.LENGTH_LONG).show();
            }
        }

    }

    // Database For Amazon GiftCard
    DatabaseReference databaseReference;
    Query query;

    private void sendGiftCard(int CardAmount) {
        dialog.show();

        databaseReference = FirebaseDatabase.getInstance().getReference("Gift Card").child("Amazon");

        if (CardAmount == 1) {
            query = databaseReference.orderByChild("amazon").equalTo(25);

        } else if (CardAmount == 2) {
            query = databaseReference.orderByChild("amazon").equalTo(50);
        }

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Random random = new Random();

                int childCount = (int) snapshot.getChildrenCount();

                int randomChild = random.nextInt(childCount);

                Iterator iterator = snapshot.getChildren().iterator();

                for (int i = 0; i < randomChild; i++) {
                    iterator.next();
                }

                DataSnapshot childSnapshot = (DataSnapshot) iterator.next();

                AmazonCardModel cardModel = childSnapshot.getValue(AmazonCardModel.class);

                if (cardModel != null) {
                    String id = cardModel.getId();
                    String giftCode = cardModel.getAmazonCode();

                    printAmazonCode(id, giftCode, CardAmount);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialog.dismiss();
                Toast.makeText(getActivity(), "Error : " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void printAmazonCode(String id, String giftCode, int cardAmount) {

        UpdateDate(cardAmount, id);
        Date date = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        String currentDate = dateFormat.format(date);

        String text = "Date :" + currentDate + "\n\n"
                + " Name : " + uName + "\n\n" +
                " Email : " + uEmail + "\n\n" +
                " Redeem ID : " + id + "\n\n" + " Amazon Claim Code : " + giftCode;
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(800, 800, 1).create();

        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Paint paint = new Paint();
        page.getCanvas().drawText(text, 10, 12, paint);
        pdfDocument.finishPage(page);

        String FilePath = Environment.getExternalStorageDirectory() + "/Earning App/Amazon Gift Card/" +
                System.currentTimeMillis() +
                user.getUid() + "amazonCode.pdf";

        File file = new File(FilePath);

        try {

            pdfDocument.writeTo(new FileOutputStream(file));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d("Error :", e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("Error :", e.getMessage());
        }

        pdfDocument.close();

        Uri path = Uri.fromFile(file);
        Intent pdfOpenintent = new Intent(Intent.ACTION_VIEW);
        pdfOpenintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        pdfOpenintent.setDataAndType(path, "application/pdf");

        try {
            startActivity(Intent.createChooser(pdfOpenintent, "Open With"));
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(getContext(), "Please install pdf reader", Toast.LENGTH_SHORT).show();
        }


    }

    private void UpdateDate(int cardAmount, String id) {

        int currentCoins = Integer.parseInt(coinsText.getText().toString());

        HashMap<String, Object> map = new HashMap<>();
        if (cardAmount == 1) {

            int updateCoins = currentCoins - 6000;
            map.put("coins", updateCoins);
        } else {
            int updateCoins = currentCoins - 12000;
            map.put("coins", updateCoins);
        }

        reference.child(user.getUid())
                .updateChildren(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Congrats", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    }
                });
        DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        mDatabaseReference.child("Gift Card").child("Amazon").child(id).removeValue();

    }
}
package com.scb.mca;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class TeacherPaymentDetails extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    TextView title,description,price,date,priceTotal;
    SwitchMaterial switchInfo;
    ListView payments_list;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String docID,currentClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_payment_details);
        title=findViewById(R.id.title);
        description=findViewById(R.id.description);
        price=findViewById(R.id.price);
        date=findViewById(R.id.date);
        switchInfo=findViewById(R.id.switchInfo);
        payments_list=findViewById(R.id.payments_list);
        priceTotal=findViewById(R.id.priceTotal);

        switchInfo.setChecked(true);
        switchInfo.setOnCheckedChangeListener(this);
        mAuth=FirebaseAuth.getInstance();
        Bundle extra=getIntent().getExtras();
        docID=extra.getString("docID");
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser=mAuth.getCurrentUser();
        if(currentUser!=null){
            getBasicInfo();
            getCurrentClass();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void getBasicInfo() {
        db.collection("notification").document(docID).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        title.setText(String.valueOf(documentSnapshot.get("name")));
                        description.setText(String.valueOf(documentSnapshot.get("description")));
                        price.setText(String.valueOf(documentSnapshot.get("price")));
                        date.setText(String.valueOf(documentSnapshot.get("expiry")));
                        loadList("Paid");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),"Error:"+e.toString(),Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getCurrentClass() {
        SharedPreferences sharedPreferences= getSharedPreferences("CurrentClass", Context.MODE_PRIVATE);
        currentClass=sharedPreferences.getString("class","");
    }

    private void loadList(String status) {
        DocumentReference documentReference=db.collection("notification").document(docID);
        if(status.equals("Paid")){
            documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                    List<String> data=new ArrayList<>();
                    if(error==null){
                        data= (List<String>) value.get("paid");
                    }
                    String priceTxt=price.getText().toString();
                    if(data.size()>0){
                        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,data);
                        arrayAdapter.notifyDataSetChanged();
                        payments_list.setAdapter(arrayAdapter);
                        int total=data.size()*Integer.parseInt(priceTxt);
                        priceTotal.setText(String.format("Total collected: %d", total));
                    }else {
                        data.add("No payments made");
                        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,data);
                        arrayAdapter.notifyDataSetChanged();
                        payments_list.setAdapter(arrayAdapter);
                    }
                }
            });
        }else {
            documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                    List<String> data=new ArrayList<>();
                    if(error==null){
                        data= (List<String>) value.get("paid");
                    }
                    if(data.size()>0){
                        List<String> finalData = data;
                        db.collection("users")
                                .whereEqualTo("class",currentClass).whereEqualTo("userType","student").get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        List<DocumentSnapshot> documentSnapshots=queryDocumentSnapshots.getDocuments();
                                        List<String> unpaidStudents=new ArrayList<>();
                                        String student=null;
                                        for(DocumentSnapshot documentSnapshot:documentSnapshots){
                                            student= String.valueOf(documentSnapshot.get("email"));
                                            if(!finalData.contains(student)){
                                                unpaidStudents.add(student);
                                            }
                                        }
                                        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,unpaidStudents);
                                        arrayAdapter.notifyDataSetChanged();
                                        payments_list.setAdapter(arrayAdapter);
                                    }
                                });
                    }else {
                        data.add("No unpaid students");
                        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,data);
                        arrayAdapter.notifyDataSetChanged();
                        payments_list.setAdapter(arrayAdapter);
                    }
                }
            });
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked)
            loadList("Paid");
        else
            loadList("UnPaid");
    }

    public void goBack(View view) {
        onBackPressed();
    }
}
package com.scb.mca;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.protobuf.Timestamp;
import com.scb.mca.customList.StudentPaymentsHistoryList;
import com.scb.mca.customList.TeacherPaymentsList;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StudentsPaymentHistory extends AppCompatActivity {
    ListView payments_list;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    List<String> name =new ArrayList<>();
    List<String> paymentIDList =new ArrayList<>();
    List<String> timestamp =new ArrayList<>();
    List<String> price =new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_students_payment_history);
        payments_list=findViewById(R.id.payments_list);
        mAuth=FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser=mAuth.getCurrentUser();
        if(currentUser!=null){
            loadList();
        }
    }

    private void loadList() {
        db.collection("payments").whereEqualTo("email",currentUser.getEmail()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> documentSnapshots=queryDocumentSnapshots.getDocuments();
                com.google.firebase.Timestamp timestampDB;
                if(documentSnapshots.size()>0){
                    name.clear();paymentIDList.clear();timestamp.clear();price.clear();
                    for(DocumentSnapshot documentSnapshot:documentSnapshots){
                        name.add(String.valueOf(documentSnapshot.get("name")));
                        paymentIDList.add(String.valueOf(documentSnapshot.get("paymentID")));

                        timestampDB=(com.google.firebase.Timestamp) documentSnapshot.get("timestamp");
                        Date date=timestampDB.toDate();

                        timestamp.add(String.valueOf(date.toString()));
                        price.add(String.valueOf(documentSnapshot.get("price")));
                    }
                    StudentPaymentsHistoryList studentsPaymentHistoryList=new StudentPaymentsHistoryList(getApplicationContext(),name,paymentIDList,timestamp,price);
                    studentsPaymentHistoryList.notifyDataSetChanged();
                    payments_list.setAdapter(studentsPaymentHistoryList);
                }else {
                    List<String> messages=new ArrayList<>();
                    messages.add("No payments made");
                    ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,messages);
                    arrayAdapter.notifyDataSetChanged();
                    payments_list.setAdapter(arrayAdapter);
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),"Error:"+e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }
}
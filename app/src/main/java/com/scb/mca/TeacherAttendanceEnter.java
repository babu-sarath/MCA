package com.scb.mca;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeacherAttendanceEnter extends AppCompatActivity {
    EditText abscent,hours;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    List<String> subjects=new ArrayList<>();
    Spinner spinner;
    DecimalFormat decimalFormat = new DecimalFormat("0.00");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_attendance_enter);
        abscent=findViewById(R.id.abscent);
        hours=findViewById(R.id.hours);
        spinner=findViewById(R.id.spinner);

        mAuth=FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser=mAuth.getCurrentUser();
        if(currentUser!=null){
            loadSpinner();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void addData(View view) {
        String abscenteesStr=abscent.getText().toString().toUpperCase();
        String hoursStr=hours.getText().toString().toUpperCase();
        List<String> list=new ArrayList<>();
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        if(!hoursStr.isEmpty()){
            float hour=Float.parseFloat(hoursStr);
            if(!abscenteesStr.isEmpty()){
                String temp[]=abscenteesStr.split(",");
                list= Arrays.asList(temp);
            }
            List<String> finalList = list;
            db.collection("attendance").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    float attended,total,percentage;
                    for(DocumentSnapshot documentSnapshot:queryDocumentSnapshots.getDocuments()){

                        if(finalList.contains(documentSnapshot.getId())){
                            String dateString = sdf.format(date);
                            String hoursDB="0";
                            List<String> absentList= (List<String>) documentSnapshot.get("abscent");
                            for(String dates:absentList){
                                if(dates.substring(0,10).equals(dateString)){
                                    db.collection("attendance").document(documentSnapshot.getId()).update("abscent", FieldValue.arrayRemove(dates));
                                    hoursDB=dates.substring(12,13);
                                }
                            }
                            float hoursDb=Float.parseFloat(hoursDB)+hour;
                            String updateVal=dateString+": "+hoursDb+"Classes";

                            db.collection("attendance").document(documentSnapshot.getId()).update("total", FieldValue.increment(hour));
                            db.collection("attendance").document(documentSnapshot.getId()).update("abscent", FieldValue.arrayUnion(updateVal));
                            attended=Float.parseFloat(String.valueOf(documentSnapshot.get("attended")));
                        }else {
                            db.collection("attendance").document(documentSnapshot.getId()).update("attended", FieldValue.increment(hour));
                            db.collection("attendance").document(documentSnapshot.getId()).update("total", FieldValue.increment(hour));
                            attended=Float.parseFloat(String.valueOf(documentSnapshot.get("attended")))+hour;
                        }
                        total=Float.parseFloat(String.valueOf(documentSnapshot.get("total")))+hour;
                        percentage=(float)((attended / total) * 100);
                        db.collection("attendance").document(documentSnapshot.getId()).update("attendance", decimalFormat.format(percentage));
                    }
                    Toast.makeText(getApplicationContext(),"Updated Attendance",Toast.LENGTH_LONG).show();
                    hours.getText().clear();
                }
            });
        }else
            Toast.makeText(getApplicationContext(),"Please enter the hours taken",Toast.LENGTH_LONG).show();
    }

    private void loadSpinner() {
        db.collection("users").whereEqualTo("email",currentUser.getEmail()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> documentSnapshots=queryDocumentSnapshots.getDocuments();
                subjects.clear();
                for(DocumentSnapshot documentSnapshot:documentSnapshots){
                    subjects= (List<String>) documentSnapshot.get("subjects");
                }
                ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item,subjects);
                spinner.setAdapter(arrayAdapter);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    public void goBack(View view) {
        onBackPressed();
    }
}
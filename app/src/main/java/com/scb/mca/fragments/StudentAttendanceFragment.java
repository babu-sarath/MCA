package com.scb.mca.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.scb.mca.R;

import java.util.ArrayList;
import java.util.List;

public class StudentAttendanceFragment extends Fragment {
    View view;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView missedClasses,attendance,regno,name;
    ListView abscentList;
    Chip chip;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view= inflater.inflate(R.layout.fragment_student_attendance,null);
        missedClasses=view.findViewById(R.id.missedClasses);
        attendance=view.findViewById(R.id.attendance);
        regno=view.findViewById(R.id.regno);
        name=view.findViewById(R.id.name);
        abscentList=view.findViewById(R.id.abscentList);
        chip=view.findViewById(R.id.chip);

        mAuth=FirebaseAuth.getInstance();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        currentUser=mAuth.getCurrentUser();
        if(currentUser!=null){
            getBasicInfo();
        }
    }

    private void getBasicInfo() {
        //get student info
        db.collection("users").whereEqualTo("email",currentUser.getEmail()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    QuerySnapshot querySnapshot=task.getResult();
                    assert querySnapshot != null;
                    if(querySnapshot.getDocuments().size()>0){
                        String nameStr="",regnoStr="";
                        List<DocumentSnapshot> docs=querySnapshot.getDocuments();
                        for(DocumentSnapshot documentSnapshot: docs){
                            nameStr= (String) documentSnapshot.get("name");
                            regnoStr= (String) documentSnapshot.getId();
                        }
                        name.setText(nameStr);
                        regno.setText(regnoStr);
                        getAttendance(regnoStr);
                    }
                }else {
                    Toast.makeText(getContext(),"Task Error: "+task.getException(),Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getAttendance(String regnoStr) {
        db.collection("attendance").document(regnoStr).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String atnd,totl,dbAttendance;
                float attended,total;
                if(documentSnapshot.exists()){
                    atnd= String.valueOf(documentSnapshot.get("attended"));
                    totl= String.valueOf(documentSnapshot.get("total"));
                    dbAttendance= String.valueOf(documentSnapshot.get("attendance"));
                    attended=Float.parseFloat(atnd);
                    total=Float.parseFloat(totl);
                    float res2=(float) total-attended;
                    attendance.setText(dbAttendance);
                    missedClasses.setText(Float.toString(res2));
                    setAttendanceState(dbAttendance);
                    loadListView(regnoStr);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setAttendanceState(String dbAttendance) {
        String atd=dbAttendance.substring(0,2);
        float attendance=Float.parseFloat(atd);
        if(attendance>85) {
            chip.setText("Above Average");
            chip.setBackgroundColor(Color.parseColor("#34A853"));
        }
        else if(attendance<85 && attendance>65) {
            chip.setText("Attendance Average");
            chip.setBackgroundColor(Color.parseColor("#FBBC05"));
        }
        else {
            chip.setText("Attendance Low");
            chip.setBackgroundColor(Color.parseColor("#EA4335"));
        }
    }

    private void loadListView(String regnoStr) {
        db.collection("attendance").document(regnoStr).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error==null){
                    ArrayAdapter<String> arrayAdapter=null;
                    List<String> abscent=new ArrayList<>();
                    if(value.exists()){
                        abscent= (List<String>) value.get("abscent");
                        if(abscent!=null){
                            arrayAdapter=new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1,abscent);
                        }else {
                            List<String> empty=new ArrayList<>();
                            empty.add("No records available");
                            arrayAdapter=new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1,empty);
                        }
                        arrayAdapter.notifyDataSetChanged();
                        abscentList.setAdapter(arrayAdapter);
                    }
                }
            }
        });
    }

}

package com.scb.mca.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.scb.mca.R;
import com.scb.mca.TeacherAttendanceEnter;

import java.util.ArrayList;
import java.util.List;

public class TeacherAttendanceFragment extends Fragment {
    View view;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ListView studentList;
    String currentClass=null;
    List<String> abscentData=new ArrayList<>();
    List<String> docID=new ArrayList<>();
    List<String> attendance=new ArrayList<>();
    List<String> attendedClasses=new ArrayList<>();
    List<String> totalClasses=new ArrayList<>();
    List<String> subjects=new ArrayList<>();
    Spinner spinner;
    Button load,enterData;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view= inflater.inflate(R.layout.fragment_teacher_attendance,null);
        studentList=view.findViewById(R.id.studentList);
        load=view.findViewById(R.id.load);
        enterData=view.findViewById(R.id.enterData);
        spinner=view.findViewById(R.id.spinner);
        mAuth=FirebaseAuth.getInstance();

        load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String subject= spinner.getSelectedItem().toString();
                getClassFromDB(subject);

            }
        });

        enterData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(), TeacherAttendanceEnter.class);
                startActivity(intent);
            }
        });

        studentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                db.collection("users").document(docID.get(position)).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
                            builder
                                    .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setTitle("Attendance of "+docID.get(position))
                                    .setMessage("Name: "+documentSnapshot.getString("name")+"\nAbscent on: "+abscentData.get(position)+"\nAttendence: "+attendance.get(position)+"\nClasses attended: "+attendedClasses.get(position)+"/"+totalClasses.get(position)+"\n");

                            AlertDialog alertDialog=builder.create();
                            alertDialog.show();
                        }
                    }
                });
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        currentUser=mAuth.getCurrentUser();
        if(currentUser!=null){
            loadSpinner();
            getCurrentClass();
        }
    }

    private void getClassFromDB(String subject) {
        db.collection("classes").whereArrayContains("subject",subject).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> documentSnapshots=queryDocumentSnapshots.getDocuments();
                String className="";
                for(DocumentSnapshot documentSnapshot:documentSnapshots){
                    className=documentSnapshot.getId();
                }
                loadList(className);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
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
                ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item,subjects);
                spinner.setAdapter(arrayAdapter);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getCurrentClass() {
        SharedPreferences sharedPreferences= getActivity().getSharedPreferences("CurrentClass", Context.MODE_PRIVATE);
        currentClass=sharedPreferences.getString("class","");
        if(!currentClass.isEmpty()){
            loadList(currentClass);
        }
    }

    private void loadList(String classVal) {
        db.collection("attendance").whereEqualTo("class",classVal).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error==null){
                    abscentData.clear();docID.clear();attendance.clear();attendedClasses.clear();totalClasses.clear();
                    List<DocumentSnapshot> documentSnapshots=value.getDocuments();
                    if(documentSnapshots.size()>0){
                        for(DocumentSnapshot documentSnapshot:documentSnapshots){
                            docID.add(documentSnapshot.getId());
                            abscentData.add(String.valueOf(documentSnapshot.get("abscent")));
                            attendance.add(String.valueOf(documentSnapshot.get("attendance")));
                            attendedClasses.add(String.valueOf(documentSnapshot.get("attended")));
                            totalClasses.add(String.valueOf(documentSnapshot.get("total")));
                        }
                        if(getActivity()!=null){
                            ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1,docID);
                            arrayAdapter.notifyDataSetChanged();
                            studentList.setAdapter(arrayAdapter);
                        }
                    }else {
                        if(getActivity()!=null){
                            docID.add("No data available");
                            ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1,docID);
                            arrayAdapter.notifyDataSetChanged();
                            studentList.setAdapter(arrayAdapter);
                        }
                    }

                }else {
                    Toast.makeText(getContext(),"Error:"+error.getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}

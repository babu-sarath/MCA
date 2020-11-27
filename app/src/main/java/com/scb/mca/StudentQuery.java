package com.scb.mca;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.scb.mca.customList.StudentQueryList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentQuery extends AppCompatActivity {
    EditText query;
    ListView queries_list;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    List<String> question=new ArrayList<>();
    List<String> answer=new ArrayList<>();
    List<String> documentIDList=new ArrayList<>();
    String subject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth=FirebaseAuth.getInstance();
        setContentView(R.layout.activity_student_query);

        Bundle extra= getIntent().getExtras();
        subject=extra.getString("subject");

        query=findViewById(R.id.query);
        queries_list=findViewById(R.id.queries_list);
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
        db.collection("queries").whereEqualTo("from",currentUser.getEmail()).whereEqualTo("subject",subject)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error==null){
                            List<DocumentSnapshot> documentSnapshots=value.getDocuments();
                            if(documentSnapshots.size()>0){
                                question.clear();answer.clear();documentIDList.clear();
                                for(DocumentSnapshot documentSnapshot:documentSnapshots){
                                    question.add(String.valueOf(documentSnapshot.get("question")));
                                    answer.add(String.valueOf(documentSnapshot.get("answer")));
                                    documentIDList.add(documentSnapshot.getId());
                                }
                                StudentQueryList studentQueryList=new StudentQueryList(getApplicationContext(),question,answer,documentIDList);
                                studentQueryList.notifyDataSetChanged();
                                queries_list.setAdapter(studentQueryList);
                            }else {
                                question.add("No queries");
                                ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,question);
                                queries_list.setAdapter(arrayAdapter);
                            }
                        }else {
                            Toast.makeText(getApplicationContext(),"Error:"+error.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void sendQuery(View view) {
        String question=query.getText().toString();
        if(!question.isEmpty()){
            Map<String,String> data=new HashMap<>();
            data.put("subject",subject);
            data.put("from",currentUser.getEmail());
            data.put("question",question);
            data.put("answer","Pending Reply");
            db.collection("queries").add(data).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    Toast.makeText(getApplicationContext(),"Your query has been sent!",Toast.LENGTH_LONG).show();
                    query.getText().clear();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(),"Error: "+e.getMessage(),Toast.LENGTH_LONG).show();
                }
            });

        }else {
            Toast.makeText(getApplicationContext(),"Please fill the field",Toast.LENGTH_LONG).show();
        }
    }
}
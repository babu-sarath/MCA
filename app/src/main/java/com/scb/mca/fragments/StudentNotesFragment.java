package com.scb.mca.fragments;

import android.content.Intent;
import android.net.Uri;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.scb.mca.R;
import com.scb.mca.StudentQuery;
import com.scb.mca.ViewNote;
import com.scb.mca.customList.StudentNotesList;

import java.util.ArrayList;
import java.util.List;

public class StudentNotesFragment extends Fragment {
    View view;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    ListView notes_list;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String currentClass;
    Spinner spinner;
    Button queryBtn,contactBtn;
    List<String> subName=new ArrayList<>();
    List<String> titleList =new ArrayList<>();
    List<String> docID=new ArrayList<>();
    List<String> subs=new ArrayList<>();
    Intent intent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view= inflater.inflate(R.layout.fragment_student_notes,null);
        notes_list=view.findViewById(R.id.notes_list);
        spinner=view.findViewById(R.id.spinner);
        queryBtn=view.findViewById(R.id.queryBtn);
        contactBtn=view.findViewById(R.id.contactBtn);
        mAuth=FirebaseAuth.getInstance();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadList(subs.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        queryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String option=spinner.getSelectedItem().toString();
                if(!option.equals("All")){
                    Intent intent=new Intent(getActivity(), StudentQuery.class);
                    intent.putExtra("subject",option);
                    startActivity(intent);
                }else {
                    Toast.makeText(getContext(),"Please select a subject for query",Toast.LENGTH_LONG).show();
                }

            }
        });
        contactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String option=spinner.getSelectedItem().toString();
                if(!option.equals("All")){
                    contactTeacher();
                }else {
                    Toast.makeText(getContext(),"Please select a subject before contacting teacher",Toast.LENGTH_LONG).show();
                }

            }
        });
        notes_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView docTv=(TextView) view.findViewById(R.id.documentID);
                String doc=docTv.getText().toString();
                intent=new Intent(getActivity(), ViewNote.class);
                intent.putExtra("docID",doc);
                startActivity(intent);
            }
        });
        return view;
    }

    private void contactTeacher() {
        String subject=spinner.getSelectedItem().toString();
        db.collection("users").whereArrayContains("subjects",subject).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        String phoneNo = null;
                        List<DocumentSnapshot> documentSnapshots=queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot documentSnapshot:documentSnapshots)
                            phoneNo=String.valueOf(documentSnapshot.get("phone"));
                        Intent intent=new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:"+phoneNo));
                        startActivity(intent);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(),"Error: "+e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        currentUser=mAuth.getCurrentUser();
        if(currentUser!=null){
            getCurrentClass();
        }
    }

    private void getCurrentClass() {
        db.collection("users").whereEqualTo("email",currentUser.getEmail()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> documentSnapshots=queryDocumentSnapshots.getDocuments();
                        for(DocumentSnapshot documentSnapshot:documentSnapshots)
                            currentClass=String.valueOf(documentSnapshot.get("class"));
                        loadSpinner();
                        loadList("All");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(),"Error:"+e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadSpinner() {
        DocumentReference documentReference=db.collection("classes").document(currentClass);
        documentReference.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        subs=(List<String>) documentSnapshot.get("subject");
                        assert subs != null;
                        subs.add("All");
                        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, subs);
                        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner.setAdapter(arrayAdapter);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(),"Error:"+e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadList(String subject) {
        Query query=null;
        if (subject.equals("All"))
            query= db.collection("notes").whereEqualTo("class",currentClass);
        else
            query= db.collection("notes").whereEqualTo("class",currentClass).whereEqualTo("subject",subject);

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                List<DocumentSnapshot> documentSnapshots=value.getDocuments();
                subName.clear();titleList.clear();docID.clear();
                for(DocumentSnapshot documentSnapshot:documentSnapshots){
                    subName.add(String.valueOf(documentSnapshot.get("subject")));
                    titleList.add(String.valueOf(documentSnapshot.get("title")));
                    docID.add(documentSnapshot.getId());
                }
                if(getActivity()!=null) {
                    StudentNotesList studentNotesList = new StudentNotesList(getContext(), subName, titleList, docID);
                    notes_list.setAdapter(studentNotesList);
                }
            }
        });
    }

}

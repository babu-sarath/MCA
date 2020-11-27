package com.scb.mca.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.scb.mca.R;
import com.scb.mca.ViewNote;
import com.scb.mca.customList.TeacherNotesList;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class TeacherNotesFragment extends Fragment{
    View view;
    Button upload,sendNotification;
    EditText notesTitle;
    TextView selectFiles;
    Spinner subjectsUpload,subjectsHistory;
    private static int RESULT_LOAD_DATA = 159;
    Uri fileuri=null;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    StorageReference storageReference;
    ListView notes_list;
    String mime,currentClass;
    Intent intent;

    List<String> subName=new ArrayList<>();
    List<String> uploadDate=new ArrayList<>();
    List<String> titlelist=new ArrayList<>();
    List<String> docID=new ArrayList<>();
    List<String> subs=new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view= inflater.inflate(R.layout.fragment_teacher_notes,null);
        upload=(Button) view.findViewById(R.id.upload);
        notesTitle=(EditText) view.findViewById(R.id.notesTitle);
        selectFiles=(TextView) view.findViewById(R.id.selectFiles);
        subjectsUpload=(Spinner) view.findViewById(R.id.subjectsUpload);
        subjectsHistory=(Spinner) view.findViewById(R.id.subjectsHistory);
        notes_list=(ListView) view.findViewById(R.id.notes_list);

        storageReference= FirebaseStorage.getInstance().getReference("notes/");
        mAuth=FirebaseAuth.getInstance();
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadContent();
            }
        });
        selectFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent=new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("application/pdf");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent,RESULT_LOAD_DATA);
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
        notes_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                TextView docTv=(TextView) view.findViewById(R.id.documentID);
                TextView notesTv=(TextView) view.findViewById(R.id.title);
                TextView subjectTv=(TextView) view.findViewById(R.id.subjectName);
                String doc=docTv.getText().toString();
                String title=notesTv.getText().toString();
                String subject=subjectTv.getText().toString();

                AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
                builder
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteNotes(doc,title,subject);
                            }
                        })
                        .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setMessage("Are you sure you want to delete "+title)
                        .setTitle("Delete Note");
                AlertDialog alertDialog=builder.create();
                alertDialog.show();
                return true;
            }
        });
        subjectsHistory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    loadListView(subs.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return view;
    }

    private void deleteNotes(String doc,String title,String subject) {
        //Delete from DB
        db.collection("notes").document(doc).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //Delete from firebase storage
                storageReference= FirebaseStorage.getInstance().getReference("notes/"+subject);
                StorageReference reference=storageReference.child(title+".pdf");
                reference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(),"Successfully deleted",Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(),"Error: "+e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
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
        SharedPreferences sharedPreferences= getActivity().getSharedPreferences("CurrentClass", Context.MODE_PRIVATE);
        currentClass=sharedPreferences.getString("class","");
        getSpinnerData();
        loadListView("All");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==RESULT_LOAD_DATA && resultCode == RESULT_OK && null !=data){
            fileuri = data.getData();
            ContentResolver contentResolver=getActivity().getContentResolver();
            mime=contentResolver.getType(fileuri);
            selectFiles.setText("File Selected");
        }
    }

    private void getSpinnerData() {
        db.collection("users").whereEqualTo("email",currentUser.getEmail()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> documentSnapshots=queryDocumentSnapshots.getDocuments();
                        for(DocumentSnapshot documentSnapshot:documentSnapshots){
                            subs= (List<String>) documentSnapshot.get("subjects");
                        }
                        if(subs.size()>0)
                            subs.add("All");
                            loadSpinners(subs);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(),"Error: "+e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadSpinners(List<String> subs) {
        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item,subs);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subjectsUpload.setAdapter(arrayAdapter);
        subjectsHistory.setAdapter(arrayAdapter);
    }

    private void loadListView(String subject){

        Query query=null;
        if(subject.equals("All"))
            query=db.collection("notes").whereEqualTo("teacher",currentUser.getEmail());
        else
            query=db.collection("notes").whereEqualTo("teacher",currentUser.getEmail()).whereEqualTo("subject",subject);

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                List<DocumentSnapshot> documentSnapshots=value.getDocuments();
                subName.clear();uploadDate.clear();titlelist.clear();docID.clear();
                if(documentSnapshots.size()>0){
                    for(DocumentSnapshot documentSnapshot: documentSnapshots){
                        subName.add(String.valueOf(documentSnapshot.get("subject")));
                        titlelist.add(String.valueOf(documentSnapshot.get("title")));
                        uploadDate.add(String.valueOf(documentSnapshot.get("uploadedOn")));
                        docID.add(documentSnapshot.getId());
                    }
                    if(getActivity()!=null){
                        TeacherNotesList teacherNotesList=new TeacherNotesList(getContext(),subName,uploadDate,titlelist,docID);
                        teacherNotesList.notifyDataSetChanged();
                        notes_list.setAdapter(teacherNotesList);
                    }
                }else {
                    List<String> messages=new ArrayList<>();
                    messages.add("No materials available");
                    ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1,messages);
                    arrayAdapter.notifyDataSetChanged();
                    notes_list.setAdapter(arrayAdapter);
                }

            }
        });
    }

    private void uploadContent() {
        String title=notesTitle.getText().toString();

        if(fileuri!=null && !title.isEmpty()){
            final ProgressDialog progressDialog=new ProgressDialog(getActivity());
            progressDialog.setCancelable(false);
            progressDialog.setTitle("File Uploading");
            progressDialog.show();
            String subject = subjectsUpload.getSelectedItem().toString();

            storageReference= FirebaseStorage.getInstance().getReference("notes/"+subject);
            StorageReference reference=storageReference.child(title+".pdf");
            reference.putFile(fileuri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(),"File Uploaded",Toast.LENGTH_LONG).show();
                            insertUploadToDB(reference,subject,title);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(),"Error: "+e.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            double progress=(100.0*snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                            progressDialog.setMessage("Uploaded "+((int)progress)+"%...");
                        }
                    });
        }else {
            Toast.makeText(getContext(),"Make sure a file is selected and title is given",Toast.LENGTH_LONG).show();
        }
    }

    private void insertUploadToDB(StorageReference reference,String subject,String title) {
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date date = new Date();
        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
//                Toast.makeText(getContext(),uri.toString(),Toast.LENGTH_LONG).show();
                Map<String, String> data=new HashMap<>();
                data.put("subject",subject);
                data.put("title",title);
                data.put("url",uri.toString());
                data.put("class",currentClass);
                data.put("teacher",currentUser.getEmail());
                data.put("uploadedOn",dateFormat.format(date));
                db.collection("notes").add(data).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(getContext(),"Successfully inserted!",Toast.LENGTH_LONG).show();
                        notesTitle.getText().clear();
                        selectFiles.setText("Select File");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(),"Error: "+e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(),"Error: "+e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }
}

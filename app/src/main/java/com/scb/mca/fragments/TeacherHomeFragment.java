package com.scb.mca.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.scb.mca.R;
import com.scb.mca.customList.StudentQueryList;
import com.scb.mca.customList.TeacherQueryList;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeacherHomeFragment extends Fragment implements DatePickerDialog.OnDateSetListener {
    View view;
    private FirebaseAuth mAuth;
    TextView name,info,missedClasses,attendance,wish,selectNotificationDate;
    EditText notificationName,notificationMessage;
    String currentClass;
    ImageView image;
    Button sendNotification;
    Intent intent;
    ListView notification_list;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser currentUser;
    StorageReference storageReference;
    DatePickerDialog datePickerDialog;
    List<String> subjectList=new ArrayList<>();
    List<String> questionsList=new ArrayList<>();
    List<String> documentID=new ArrayList<>();
    List<String> subs=new ArrayList<>();

    Calendar c;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view= inflater.inflate(R.layout.fragment_teacher_home,null);
        name=(TextView)view.findViewById(R.id.name);
        info=(TextView)view.findViewById(R.id.info);
        sendNotification=(Button) view.findViewById(R.id.sendNotification);
        missedClasses=(TextView)view.findViewById(R.id.missedClasses);
        attendance=(TextView)view.findViewById(R.id.attendance);
        wish=(TextView)view.findViewById(R.id.wish);
        image=(ImageView) view.findViewById(R.id.image);
        selectNotificationDate=(TextView) view.findViewById(R.id.selectNotificationDate);
        notificationName=(EditText) view.findViewById(R.id.notificationName);
        notificationMessage=(EditText) view.findViewById(R.id.notificationMessage);
        notification_list=(ListView) view.findViewById(R.id.notification_list);

        c = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(getContext(), TeacherHomeFragment.this, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

        mAuth=FirebaseAuth.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference("profileImages/");

        selectNotificationDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });
        sendNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendNotification();
            }
        });
        notification_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String doc=documentID.get(position);
                String msg=questionsList.get(position);

                final View editView=getLayoutInflater().inflate(R.layout.dialog_teacher_query_reply,null);
                EditText answerTv=editView.findViewById(R.id.answerTv);
                AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
                builder
                        .setPositiveButton("Reply", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String ans=answerTv.getText().toString();
                                if(!ans.isEmpty()){
                                    Map<String,Object> data=new HashMap<>();
                                    data.put("answer",ans);
                                    db.collection("queries").document(doc).update(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(getContext(),"Your reply has been sent.",Toast.LENGTH_LONG).show();
                                            dialog.dismiss();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getContext(),"Error:"+e.getMessage(),Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }else {
                                    Toast.makeText(getContext(),"Please fill in the field",Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                        .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setView(editView)
                        .setTitle("Answer the query")
                        .setMessage(msg);
                AlertDialog alertDialog=builder.create();
                alertDialog.show();

            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        setWish();
        currentUser=mAuth.getCurrentUser();
        if(currentUser!=null){
            getCurrentClass();
            getBasicInfo();
        }

    }

    private void loadList(List<String> subs) {
        db.collection("queries").whereEqualTo("answer","Pending Reply").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error==null){
                    List<DocumentSnapshot> documentSnapshots=value.getDocuments();
                    if(documentSnapshots.size()>0){
                        subjectList.clear();questionsList.clear();documentID.clear();
                        String subject=null;
                        for(DocumentSnapshot documentSnapshot:documentSnapshots){
                            subject= String.valueOf(documentSnapshot.get("subject"));
                            if(subs.contains(subject)){
                                subjectList.add(subject);
                                questionsList.add(String.valueOf(documentSnapshot.get("question")));
                                documentID.add(documentSnapshot.getId());
                            }
                        }
                        if(getActivity()!=null){
                            TeacherQueryList teacherQueryList=new TeacherQueryList(getContext(),questionsList,subjectList,documentID);
                            teacherQueryList.notifyDataSetChanged();
                            notification_list.setAdapter(teacherQueryList);
                        }
                    }else {
                        if(getActivity()!=null){
                            subjectList.clear();
                            subjectList.add("No notifications available");
                            ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1,subjectList);
                            notification_list.setAdapter(arrayAdapter);
                        }
                    }
                }else {
                    Toast.makeText(getContext(),"Error:"+error.getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void setWish() {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        if(timeOfDay >= 0 && timeOfDay < 12){
            wish.setText("Good Morning");
        }else if(timeOfDay >= 12 && timeOfDay < 16){
            wish.setText("Good Afternoon");
        }else if(timeOfDay >= 16 && timeOfDay < 21){
            wish.setText("Good Evening");
        }else if(timeOfDay >= 21 && timeOfDay < 24){
            wish.setText("Good Night");
        }
    }

    private void getBasicInfo() {
        //load the profile image
        StorageReference fileRef=storageReference.child(currentUser.getEmail()+".jpg");
        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).fit().centerCrop().into(image);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Picasso.get().load("https://thumbs.dreamstime.com/b/default-avatar-profile-image-vector-social-media-user-icon-potrait-182347582.jpg").fit().centerCrop().into(image);
            }
        });

        //load the basic info
        db.collection("users").whereEqualTo("email",currentUser.getEmail()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    QuerySnapshot querySnapshot=task.getResult();
                    assert querySnapshot != null;
                    if(querySnapshot.getDocuments().size()>0){
                        String nameStr="",infoStr="";
                        subs.clear();
                        List<DocumentSnapshot> docs=querySnapshot.getDocuments();
                        for(DocumentSnapshot documentSnapshot: docs){
                            nameStr= (String) documentSnapshot.get("name");
                            infoStr= documentSnapshot.getId() +"\nMob: "+documentSnapshot.get("phone")+"\n"+documentSnapshot.get("email");
                            subs= (List<String>) documentSnapshot.get("subjects");
                        }
                        loadList(subs);
                        name.setText(nameStr);
                        info.setText(infoStr);
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

    private void getCurrentClass() {
        db.collection("users").whereEqualTo("email",currentUser.getEmail()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> documentSnapshots=queryDocumentSnapshots.getDocuments();
                        for(DocumentSnapshot documentSnapshot:documentSnapshots)
                            currentClass=String.valueOf(documentSnapshot.get("class"));
                        SharedPreferences sharedPreferences= getActivity().getSharedPreferences("CurrentClass", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor=sharedPreferences.edit();
                        editor.putString("class",currentClass);
                        editor.apply();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void sendNotification() {
        String name=notificationName.getText().toString();
        String msg=notificationMessage.getText().toString();
        String date=selectNotificationDate.getText().toString();

        if(!name.isEmpty() && !msg.isEmpty() && !date.equals("Select Expiry")){
            Map<String,Object > data=new HashMap<>();
            data.put("name",name);
            data.put("description",msg);
            data.put("expiry",date);
            data.put("class",currentClass);
            data.put("type","normal");
            db.collection("notification").add(data).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    Toast.makeText(getContext(),"Notification Sent",Toast.LENGTH_LONG).show();
                    notificationName.getText().clear();
                    notificationMessage.getText().clear();
                    selectNotificationDate.setText("Select Expiry");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(),"Error: "+e.getMessage(),Toast.LENGTH_LONG).show();
                }
            });
        }else {
            Toast.makeText(getContext(),"Please fill all the feilds",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String day=Integer.toString(dayOfMonth);
        String mon=Integer.toString(month+1);
        String yr=Integer.toString(year);
        String selectedDate=day+"-"+mon+"-"+yr;

        selectNotificationDate.setText(selectedDate);
    }
}

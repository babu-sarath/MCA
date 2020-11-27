package com.scb.mca.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.scb.mca.R;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class StudentHomeFragment extends Fragment {
    View view;
    private FirebaseAuth mAuth;
    TextView name,regno,missedClasses,attendance,wish;
    ImageView image;
    Intent intent;
    ListView notification_list;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser currentUser;
    StorageReference storageReference;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view= inflater.inflate(R.layout.fragment_student_home,null);
        name=(TextView)view.findViewById(R.id.name);
        regno=(TextView)view.findViewById(R.id.regno);
        missedClasses=(TextView)view.findViewById(R.id.missedClasses);
        attendance=(TextView)view.findViewById(R.id.attendance);
        wish=(TextView)view.findViewById(R.id.wish);
        image=(ImageView) view.findViewById(R.id.image);
        notification_list=(ListView) view.findViewById(R.id.notification_list);

        mAuth=FirebaseAuth.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference("profileImages/");
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        currentUser=mAuth.getCurrentUser();
        getBasicInfo();
        setWish();
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
                        String nameStr="",regnoStr="",studentClass="";
                        List<DocumentSnapshot> docs=querySnapshot.getDocuments();
                        for(DocumentSnapshot documentSnapshot: docs){
                            nameStr= (String) documentSnapshot.get("name");
                            studentClass = (String) documentSnapshot.get("class");
                            regnoStr= (String) documentSnapshot.getId();
                        }
                        name.setText(nameStr);
                        regno.setText(regnoStr);
                        getAttendance(regnoStr);
                        getNotification(studentClass);
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
                int attended,total;
                if(documentSnapshot.exists()){
                    atnd= String.valueOf(documentSnapshot.get("attended"));
                    totl= String.valueOf(documentSnapshot.get("total"));
                    dbAttendance= String.valueOf(documentSnapshot.get("attendance"));
                    attended=Integer.parseInt(atnd);
                    total=Integer.parseInt(totl);
                    int res2=total-attended;
                    attendance.setText(dbAttendance);
                    missedClasses.setText(Integer.toString(res2));
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getNotification(String studentClass) {
        //load notifications
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date date = new Date();
        List<String> messages=new ArrayList<>();
        db.collection("notification")
                .whereGreaterThanOrEqualTo("expiry",dateFormat.format(date)).whereEqualTo("class",studentClass)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        List<DocumentSnapshot> documentSnapshots=value.getDocuments();
                        messages.clear();
                        if(documentSnapshots.size()>0){
                            for(DocumentSnapshot documentSnapshot : documentSnapshots){
                                messages.add(String.valueOf(documentSnapshot.get("name")));
                            }
                        }else {
                            messages.add("No notifications available");
                        }
                        if(getActivity()!=null){
                            ArrayAdapter<String> arrayAdapter=new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1,messages);
                            arrayAdapter.notifyDataSetChanged();
                            notification_list.setAdapter(arrayAdapter);
                        }
                    }
                });
//        WORKS
//        db.collection("notification")
//                .whereGreaterThanOrEqualTo("expiry",dateFormat.format(date)).whereEqualTo("class",studentClass)
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if(task.isSuccessful()){
//                    QuerySnapshot querySnapshot=task.getResult();
//                    assert querySnapshot != null;
//                    List<DocumentSnapshot> documentSnapshots=querySnapshot.getDocuments();
//                    if(documentSnapshots.size()>0){
//                        for(DocumentSnapshot documentSnapshot : documentSnapshots){
//                            messages.add(String.valueOf(documentSnapshot.get("name")));
//                        }
//                    }else {
//                        messages.add("You have no notifications");
//                    }
//                    ArrayAdapter<String> arrayAdapter=new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1,messages);
//                    notification_list.setAdapter(arrayAdapter);
//                }
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_LONG).show();
//                Log.d("notification",e.getMessage());
//            }
//        });
    }
}

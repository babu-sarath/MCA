package com.scb.mca.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.scb.mca.MainActivity;
import com.scb.mca.R;
import com.squareup.picasso.Picasso;
import java.util.List;
import static android.app.Activity.RESULT_OK;

public class StudentSettingsFragment extends Fragment {
    View view;
    Button logout,close_change_password,confirm_change_password;
    EditText password_txt, password_new;
    private FirebaseAuth mAuth;
    TextView name,regno,set_image,change_password;
    Intent intent;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser currentUser;
    private static int RESULT_LOAD_IMAGE = 159;
    StorageReference storageReference;
    LinearLayout new_password;
    ImageView image;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_student_settings,null);
        logout=(Button)view.findViewById(R.id.logout);
        close_change_password=(Button)view.findViewById(R.id.close_change_password);
        confirm_change_password=(Button)view.findViewById(R.id.confirm_change_password);
        name=(TextView)view.findViewById(R.id.name);
        regno=(TextView)view.findViewById(R.id.regno);
        set_image=(TextView)view.findViewById(R.id.set_image);
        password_txt=(EditText)view.findViewById(R.id.password_txt);
        password_new =(EditText)view.findViewById(R.id.password_new);
        change_password=(TextView)view.findViewById(R.id.change_password);
        new_password=(LinearLayout)view.findViewById(R.id.new_password);
        image=(ImageView)view.findViewById(R.id.image);
        new_password.setVisibility(View.GONE);

        mAuth=FirebaseAuth.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference("profileImages/");
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
        set_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,RESULT_LOAD_IMAGE);
            }
        });
        change_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new_password.setVisibility(View.VISIBLE);
            }
        });
        close_change_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new_password.setVisibility(View.GONE);
            }
        });
        confirm_change_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        currentUser=mAuth.getCurrentUser();
        getBasicInfo();
        super.onStart();
        if(currentUser==null){
            intent=new Intent(getContext(), MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null!=data) {
            Uri selectedImage=data.getData();
            uploadImage(selectedImage);
        }
    }

    private void changePassword() {
        String pwd,confirmPwd;
        pwd=password_txt.getText().toString();
        confirmPwd= password_new.getText().toString();

        if(!pwd.isEmpty() && !confirmPwd.isEmpty()){
            AuthCredential credential= EmailAuthProvider.getCredential(currentUser.getEmail(),pwd);
            currentUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        currentUser.updatePassword(confirmPwd).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    password_new.getText().clear();
                                    password_txt.getText().clear();
                                    new_password.setVisibility(View.GONE);
                                    Toast.makeText(getContext(),"Password updated",Toast.LENGTH_LONG).show();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getContext(),"Error: "+e.getMessage(),Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(),"Error: "+e.getMessage(),Toast.LENGTH_LONG).show();
                }
            });
        }else {
            Toast.makeText(getContext(),"Do not leave the fields empty",Toast.LENGTH_LONG).show();
        }
    }


    private void uploadImage(Uri selectedImage) {
        StorageReference fileRef=storageReference.child(currentUser.getEmail()+".jpg");
        fileRef.putFile(selectedImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Picasso.get().load(selectedImage).fit().centerCrop().into(image);
                Toast.makeText(getContext(),"Profile picture updated",Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(),"Error: "+e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getBasicInfo() {
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

    private void setImage() {

    }

    private void logout() {
        mAuth.signOut();
        intent=new Intent(getContext(), MainActivity.class);
        startActivity(intent);
    }
}

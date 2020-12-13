package com.scb.mca;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    Intent intent;
    private FirebaseAuth mAuth;
    EditText email,password;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        email=findViewById(R.id.editTextTextEmailAddress);
        password=findViewById(R.id.editTextTextPassword);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null){
            checkUser(currentUser.getEmail());
        }
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
    public void login(View view) {
        String emailStr,passwordStr;
        emailStr=email.getText().toString();
        passwordStr=password.getText().toString();

        if(!emailStr.isEmpty() && !passwordStr.isEmpty()){
            mAuth.signInWithEmailAndPassword(emailStr,passwordStr).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        checkUser(emailStr);
                    }
                    else {
                        Toast.makeText(getApplicationContext(),"Authentication Failed. "+task.getException().getMessage(),Toast.LENGTH_LONG).show();
                    }
                }
            });
        }else {
            Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_SHORT).show();
        }

    }

    private void checkUser(String emailStr) {
        intent=new Intent(this,StudentHome.class);
        db.collection("users").whereEqualTo("email",emailStr).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    QuerySnapshot querySnapshot=task.getResult();
                    assert querySnapshot != null;
                    if(querySnapshot.getDocuments().size()>0){
                        String type="";
                        List<DocumentSnapshot> docs=querySnapshot.getDocuments();
                        for(DocumentSnapshot documentSnapshot: docs){
                            type= (String) documentSnapshot.get("userType");
                        }
                        assert type != null;
                        if(!type.equals("student")) {
                            intent=new Intent(getApplicationContext(),TeacherHome.class);
                        }
                        startActivity(intent);
                    }
                }else {
                    Toast.makeText(getApplicationContext(),"Task Error: "+task.getException(),Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    public void forgotPassword(View view) {
        intent=new Intent(this,ForgotPassword.class);
        startActivity(intent);
    }
}
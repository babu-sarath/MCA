package com.scb.mca;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {
    EditText email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        email=findViewById(R.id.email);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void resetPassword(View view) {
        String mail=email.getText().toString();
        if(!mail.isEmpty()){
            FirebaseAuth.getInstance().sendPasswordResetEmail(mail)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getApplicationContext(), "Password reset instructions have been sent to your email.", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }else
            Toast.makeText(getApplicationContext(), "Please enter your email ID to reset password", Toast.LENGTH_SHORT).show();
    }

    public void goBack(View view) {
        onBackPressed();
    }
}
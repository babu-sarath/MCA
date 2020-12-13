package com.scb.mca;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class StudentsPayment extends AppCompatActivity implements PaymentResultListener {
    Button pay;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String docID, priceStr,name,description,eventName,eventPrice;
    TextView message;
    int price;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_students_payment);
        Bundle extra=getIntent().getExtras();
        docID=extra.getString("docID");
        eventName=extra.getString("eventName");
        eventPrice=extra.getString("eventPrice");

        mAuth=FirebaseAuth.getInstance();
        pay=findViewById(R.id.pay);
        message=findViewById(R.id.message);
        message.setText(String.format("Pay Rs.%s for %s?", eventPrice, eventName));

        Checkout.preload(getApplicationContext());

        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPayment();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser=mAuth.getCurrentUser();
        if(currentUser!=null){
            fetchPaymentDataFromDB();
        }
    }

    private void fetchPaymentDataFromDB() {
        db.collection("notification").document(docID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                priceStr = String.valueOf(documentSnapshot.get("price"));
                name= String.valueOf(documentSnapshot.get("name"));
                description= String.valueOf(documentSnapshot.get("description"));

                price=Integer.parseInt(priceStr);
                int tempPrice =price* 100;
                priceStr=Integer.toString(tempPrice);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),"Error:"+e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    public void startPayment() {

        /**
         * Instantiate Checkout
         */
        Checkout checkout = new Checkout();
//        checkout.setKeyID("rzp_test_3OvTq6T7Wrlbki");
        /**
         * Set your logo here
         */
//        checkout.setImage(R.drawable.logo);

        /**
         * Reference to current activity
         */
        final Activity activity = this;

        /**
         * Pass your payment options to the Razorpay Checkout as a JSONObject
         */
        try {
            JSONObject options = new JSONObject();

            options.put("name", name);
            options.put("description", description);
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png");
//            options.put("order_id", "order_DBJOWzybf0sJbb");//from response of step 3.
            options.put("theme.color", "#FF6200EE");
            options.put("currency", "INR");
            options.put("amount", priceStr);//pass amount in currency subunits(amount in paisa)
//            options.put("prefill.email", "gaurav.kumar@example.com");
//            options.put("prefill.contact","9988776655");
            checkout.open(activity, options);
        } catch(Exception e) {
            Log.e("Payment Error", "Error in starting Razorpay Checkout", e);
        }
    }

    @Override
    public void onPaymentSuccess(String pid) {
//        s here returns the payment id
        Toast.makeText(getApplicationContext(),"Payment Successful",Toast.LENGTH_LONG).show();
        insertPaymentToDB(pid);
        updateNotificationWithStudent();
    }

    @Override
    public void onPaymentError(int i, String s) {
        Toast.makeText(getApplicationContext(),"Payment "+s,Toast.LENGTH_LONG).show();
        Log.d("Payment Error",s);
    }

    private void insertPaymentToDB(String pid) {
        Map<String,Object> data=new HashMap<>();
        data.put("paymentID",pid);
        data.put("email",currentUser.getEmail());
        data.put("price", Integer.toString(price));
        data.put("name",name);
        data.put("timestamp",FieldValue.serverTimestamp());
        db.collection("payments").add(data).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Toast.makeText(getApplicationContext(),"Transaction recorded. Thank you",Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),"DB Error",Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateNotificationWithStudent() {
        Map<String,Object> data=new HashMap<>();
        data.put("paid", FieldValue.arrayUnion(currentUser.getEmail()));
        db.collection("notification").document(docID).update(data).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),"Error:"+e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }
}
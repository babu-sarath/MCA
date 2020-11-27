package com.scb.mca.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.scb.mca.R;
import com.scb.mca.StudentsPayment;
import com.scb.mca.StudentsPaymentHistory;
import com.scb.mca.customList.StudentPaymentsList;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class StudentPaymentsFragment extends Fragment {
    View view;
    ListView payments_list;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    List<String> nameList =new ArrayList<>();
    List<String> descriptionList =new ArrayList<>();
    List<String> dateList =new ArrayList<>();
    List<String> docIDList=new ArrayList<>();
    List<String> priceList=new ArrayList<>();
    Intent intent;
    Button history;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view= inflater.inflate(R.layout.fragment_student_payment,null);
        payments_list=view.findViewById(R.id.payments_list);
        history=view.findViewById(R.id.history);

        mAuth=FirebaseAuth.getInstance();

        payments_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView docTv=(TextView) view.findViewById(R.id.documentID);
                intent=new Intent(getActivity(), StudentsPayment.class);
                intent.putExtra("docID",docTv.getText().toString());
                startActivity(intent);
            }
        });
        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent=new Intent(getActivity(), StudentsPaymentHistory.class);
                startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        currentUser=mAuth.getCurrentUser();
        if(currentUser!=null){
            loadList();
        }
    }

    private void loadList() {
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date date = new Date();

        db.collection("notification")
                .whereEqualTo("type","payment").whereGreaterThanOrEqualTo("expiry",dateFormat.format(date))
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                    if(error==null){

                        //check if the current user has paid anything

                        List<DocumentSnapshot> documentSnapshots=value.getDocuments();
                        if(documentSnapshots.size()>0){
                            List<String> paid=new ArrayList<>();
                            nameList.clear();descriptionList.clear();dateList.clear();priceList.clear();docIDList.clear();paid.clear();
                            for (DocumentSnapshot documentSnapshot:documentSnapshots){
                                paid= (List<String>) documentSnapshot.get("paid");
                                if(!paid.contains(currentUser.getEmail())){
                                    nameList.add(String.valueOf(documentSnapshot.get("name")));
                                    descriptionList.add(String.valueOf(documentSnapshot.get("description")));
                                    dateList.add(String.valueOf(documentSnapshot.get("expiry")));
                                    priceList.add(String.valueOf(documentSnapshot.get("price")));
                                    docIDList.add(documentSnapshot.getId());
                                }
                            }
                            if(getActivity()!=null){
                                StudentPaymentsList studentPaymentsList=new StudentPaymentsList(getContext(),nameList,descriptionList,dateList,priceList,docIDList);
                                studentPaymentsList.notifyDataSetChanged();
                                payments_list.setAdapter(studentPaymentsList);
                            }
                        }else {
                            List<String> messages=new ArrayList<>();
                            messages.add("No payment notification");
                            ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1,messages);
                            arrayAdapter.notifyDataSetChanged();
                            payments_list.setAdapter(arrayAdapter);
                        }
                    }else {
                        Toast.makeText(getContext(),"Error: "+error.getMessage(),Toast.LENGTH_LONG).show();
                        Log.d("DBError",error.getMessage());
                    }

                }
        });
    }
}

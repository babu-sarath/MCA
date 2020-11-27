package com.scb.mca.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ThrowOnExtraProperties;
import com.scb.mca.R;
import com.scb.mca.TeacherHome;
import com.scb.mca.TeacherPaymentDetails;
import com.scb.mca.customList.TeacherPaymentsList;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeacherPaymentsFragment extends Fragment implements DatePickerDialog.OnDateSetListener {
    View view;
    EditText eventName,description,price;
    TextView date;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    String eventNameStr,descriptionStr,priceStr,dateStr;
    Button submit;
    ListView payments_list;
    DatePickerDialog datePickerDialog;
    Calendar c;
    String currentClass;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    List<String> nameList =new ArrayList<>();
    List<String> descriptionList =new ArrayList<>();
    List<String> dateList =new ArrayList<>();
    List<String> docIDList=new ArrayList<>();
    List<String> priceList=new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_teacher_payment,null);
        eventName=(EditText) view.findViewById(R.id.eventName);
        description=(EditText) view.findViewById(R.id.description);
        price=(EditText) view.findViewById(R.id.price);
        date=(TextView) view.findViewById(R.id.date);
        submit=(Button) view.findViewById(R.id.submit);
        payments_list=(ListView) view.findViewById(R.id.payments_list);

        mAuth=FirebaseAuth.getInstance();
        c = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(getContext(), TeacherPaymentsFragment.this, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertToDB();
            }
        });
        payments_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                TextView docTv=(TextView) view.findViewById(R.id.documentID);
                TextView nameTv=(TextView) view.findViewById(R.id.nametv);
                TextView descriptionTv=(TextView) view.findViewById(R.id.descriptiontv);
                TextView priceTv=(TextView) view.findViewById(R.id.price);
                TextView dateTv=(TextView) view.findViewById(R.id.datetv);

                String doc=docTv.getText().toString();
                String title=nameTv.getText().toString();
                String description=descriptionTv.getText().toString();
                String price=priceTv.getText().toString();
                String date=dateTv.getText().toString();

                final View editView=getLayoutInflater().inflate(R.layout.dialog_payment_edit,null);
                EditText titleEt=(EditText) editView.findViewById(R.id.titleEt);
                EditText descriptionEt=(EditText) editView.findViewById(R.id.descriptionEt);
                EditText priceEt=(EditText) editView.findViewById(R.id.priceEt);
                EditText editTextDate=(EditText) editView.findViewById(R.id.editTextDate);
                titleEt.setText(title);
                descriptionEt.setText(description);
                priceEt.setText(price);
                editTextDate.setText(date);

                AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
                builder
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deletePayment(doc);
                            }
                        })
                        .setNegativeButton("Save Changes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                updateNotification(doc,titleEt.getText().toString(),descriptionEt.getText().toString(),priceEt.getText().toString(),editTextDate.getText().toString());
                            }
                        })
                        .setNeutralButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setView(editView)
                        .setMessage("Use this to edit the existing data")
                        .setTitle("Edit");
                AlertDialog alertDialog=builder.create();
                alertDialog.show();
                return true;
            }
        });

        payments_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(getActivity(), TeacherPaymentDetails.class);
                intent.putExtra("docID",docIDList.get(position));
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
            getCurrentClass();
        }
    }

    private void deletePayment(String doc) {
        db.collection("notification").document(doc).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getContext(),"Payment Notification Deleted",Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(),"Error: "+e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateNotification(String doc, String title, String description, String price, String date) {
        if(!doc.isEmpty() && !title.isEmpty() && !description.isEmpty() && !price.isEmpty() && !date.isEmpty()){
            Map<String,Object> data=new HashMap<>();
            data.put("name",title);
            data.put("description",description);
            data.put("price",price);
            data.put("expiry",date);
            db.collection("notification").document(doc).update(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(getContext(),"Updated",Toast.LENGTH_LONG).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(),"Error: "+e.getMessage(),Toast.LENGTH_LONG).show();
                }
            });
        }else {
            Toast.makeText(getContext(),"Please do not leave the fields empty",Toast.LENGTH_LONG).show();
        }
    }

    private void getCurrentClass() {
        SharedPreferences sharedPreferences= getActivity().getSharedPreferences("CurrentClass", Context.MODE_PRIVATE);
        currentClass=sharedPreferences.getString("class","");
        loadList();
    }

    public void loadList(){
        db.collection("notification").whereEqualTo("type","payment")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                nameList.clear();descriptionList.clear();dateList.clear();docIDList.clear();priceList.clear();

                assert value != null;
                List<DocumentSnapshot> documentSnapshots=value.getDocuments();
                if(documentSnapshots.size()>0){
                    for (DocumentSnapshot documentSnapshot:documentSnapshots){
                        nameList.add(String.valueOf(documentSnapshot.get("name")));
                        descriptionList.add(String.valueOf(documentSnapshot.get("description")));
                        dateList.add(String.valueOf(documentSnapshot.get("expiry")));
                        priceList.add(String.valueOf(documentSnapshot.get("price")));
                        docIDList.add(documentSnapshot.getId());
                    }
                    if(getActivity()!=null){
                        TeacherPaymentsList teacherPaymentsList=new TeacherPaymentsList(getContext(),nameList,descriptionList,dateList,priceList,docIDList);
                        teacherPaymentsList.notifyDataSetChanged();
                        payments_list.setAdapter(teacherPaymentsList);
                    }

                }else {
                    List<String> messages=new ArrayList<>();
                    messages.add("No payment notification set");
                    ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1,messages);
                    arrayAdapter.notifyDataSetChanged();
                    payments_list.setAdapter(arrayAdapter);
                }

            }
        });
    }

    private void insertToDB() {
        eventNameStr=eventName.getText().toString();
        descriptionStr=description.getText().toString();
        priceStr=price.getText().toString();
        dateStr=date.getText().toString();

        if(!eventNameStr.isEmpty() && !descriptionStr.isEmpty() && !priceStr.isEmpty() && !dateStr.equals("Select Last Date")){
            Map<String,Object > data=new HashMap<>();
            data.put("name",eventNameStr);
            data.put("description",descriptionStr);
            data.put("price",priceStr);
            data.put("expiry",dateStr);
            data.put("class",currentClass);
            data.put("type","payment");
            data.put("paid", FieldValue.arrayUnion("paid"));
            db.collection("notification").add(data).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    Toast.makeText(getContext(),"Event Notification Sent",Toast.LENGTH_LONG).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(),"Error: "+e.getMessage(),Toast.LENGTH_LONG).show();
                }
            });
        }else {
            Toast.makeText(getContext(),"Fill all the fields",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String day=Integer.toString(dayOfMonth);
        String mon=Integer.toString(month+1);
        String yr=Integer.toString(year);
        String selectedDate=day+"-"+mon+"-"+yr;

        date.setText(selectedDate);
        datePickerDialog.dismiss();
    }
}

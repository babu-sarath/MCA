package com.scb.mca.customList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.scb.mca.R;

import java.util.ArrayList;
import java.util.List;

public class StudentPaymentsHistoryList extends BaseAdapter {
    Context context;
    LayoutInflater layoutInflater;
    List<String> name =new ArrayList<>();
    List<String> paymentIDList =new ArrayList<>();
    List<String> timestamp =new ArrayList<>();
    List<String> price =new ArrayList<>();

    public StudentPaymentsHistoryList(Context applicationContext, List<String> name, List<String> paymentID, List<String> timestamp, List<String> price){
        this.context=applicationContext;
        this.name =name;
        this.paymentIDList =paymentID;
        this.timestamp =timestamp;
        this.price =price;
        layoutInflater=(LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return name.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView=layoutInflater.inflate(R.layout.list_student_payment_history,null);
        TextView nametv=convertView.findViewById(R.id.nametv);
        TextView paymentID=convertView.findViewById(R.id.paymentID);
        TextView timestamp=convertView.findViewById(R.id.timestamp);
        TextView pricetv=convertView.findViewById(R.id.pricetv);
        nametv.setText(name.get(position));
        timestamp.setText(this.timestamp.get(position));
        paymentID.setText(paymentIDList.get(position));
        pricetv.setText(price.get(position));
        return convertView;
    }
}

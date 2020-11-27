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

public class TeacherPaymentsList extends BaseAdapter {
    Context context;
    LayoutInflater layoutInflater;
    List<String> name =new ArrayList<>();
    List<String> description =new ArrayList<>();
    List<String> date =new ArrayList<>();
    List<String> price =new ArrayList<>();
    List<String> docID=new ArrayList<>();

    public TeacherPaymentsList(Context applicationContext, List<String> name, List<String> description, List<String> date, List<String> price, List<String> docID){
        this.context=applicationContext;
        this.name =name;
        this.description =description;
        this.date =date;
        this.price =price;
        this.docID=docID;
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
        convertView=layoutInflater.inflate(R.layout.list_teacher_payment,null);
        TextView nametv=convertView.findViewById(R.id.nametv);
        TextView descriptiontv=convertView.findViewById(R.id.descriptiontv);
        TextView datetv=convertView.findViewById(R.id.datetv);
        TextView documentID=convertView.findViewById(R.id.documentID);
        TextView pricetv=convertView.findViewById(R.id.price);
        nametv.setText(name.get(position));
        datetv.setText(date.get(position));
        descriptiontv.setText(description.get(position));
        documentID.setText(docID.get(position));
        pricetv.setText(price.get(position));
        return convertView;
    }
}

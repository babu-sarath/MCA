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

public class TeacherQueryList extends BaseAdapter {
    Context context;
    LayoutInflater layoutInflater;
    List<String> queryList =new ArrayList<>();
    List<String> subjectList =new ArrayList<>();
    List<String> docID=new ArrayList<>();

    public TeacherQueryList(Context applicationContext, List<String> query, List<String> subject, List<String> docID){
        this.context=applicationContext;
        this.queryList =query;
        this.subjectList =subject;
        this.docID=docID;
        layoutInflater=(LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return queryList.size();
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
        convertView=layoutInflater.inflate(R.layout.list_teacher_query,null);
        TextView subjectTv=convertView.findViewById(R.id.subjectTv);
        TextView queryTv=convertView.findViewById(R.id.queryTv);
        TextView documentID=convertView.findViewById(R.id.documentID);
        subjectTv.setText(subjectList.get(position));
        queryTv.setText(queryList.get(position));
        documentID.setText(docID.get(position));
        return convertView;
    }
}

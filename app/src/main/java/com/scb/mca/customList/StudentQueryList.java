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

public class StudentQueryList extends BaseAdapter {
    Context context;
    LayoutInflater layoutInflater;
    List<String> queryList =new ArrayList<>();
    List<String> answerList =new ArrayList<>();
    List<String> docID=new ArrayList<>();

    public StudentQueryList(Context applicationContext, List<String> query, List<String> answer,List<String> docID){
        this.context=applicationContext;
        this.queryList =query;
        this.answerList =answer;
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
        convertView=layoutInflater.inflate(R.layout.list_student_query,null);
        TextView queryTv=convertView.findViewById(R.id.queryTv);
        TextView answerTv=convertView.findViewById(R.id.answerTv);
        TextView documentID=convertView.findViewById(R.id.documentID);
        queryTv.setText(queryList.get(position));
        answerTv.setText(answerList.get(position));
        documentID.setText(docID.get(position));
        return convertView;
    }
}

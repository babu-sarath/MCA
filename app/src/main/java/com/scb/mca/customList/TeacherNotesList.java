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

public class TeacherNotesList extends BaseAdapter {
    Context context;
    LayoutInflater layoutInflater;
    List<String> subName=new ArrayList<>();
    List<String> uploadDate=new ArrayList<>();
    List<String> titleList =new ArrayList<>();
    List<String> docID=new ArrayList<>();

    public  TeacherNotesList(Context applicationContext, List<String> subName, List<String> uploadDate, List<String> titlelist,List<String> docID){
        this.context=applicationContext;
        this.subName=subName;
        this.uploadDate=uploadDate;
        this.titleList =titlelist;
        this.docID=docID;
        layoutInflater=(LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return subName.size();
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
        convertView=layoutInflater.inflate(R.layout.list_teacher_notes,null);
        TextView subjectName=convertView.findViewById(R.id.subjectName);
        TextView title=convertView.findViewById(R.id.title);
        TextView uploadedOn=convertView.findViewById(R.id.uploadedOn);
        TextView documentID=convertView.findViewById(R.id.documentID);
        subjectName.setText(subName.get(position));
        title.setText(titleList.get(position));
        uploadedOn.setText(uploadDate.get(position));
        documentID.setText(docID.get(position));
        return convertView;
    }
}

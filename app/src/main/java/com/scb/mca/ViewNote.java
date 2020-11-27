package com.scb.mca;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class ViewNote extends AppCompatActivity {
    TextView subject,title;
    PDFView pdfView;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    StorageReference storageReference;
    long downloadID;
    String subStr,titleStr;
    File folder,childFolder;
    boolean success;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_notes);
        subject=findViewById(R.id.subject);
        title=findViewById(R.id.title);
        pdfView=findViewById(R.id.pdfView);

        Bundle extra=getIntent().getExtras();
        assert extra != null;
        String docID=extra.getString("docID");
        loadPDF(docID);
        mAuth=FirebaseAuth.getInstance();

        IntentFilter filter=new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if(id==downloadID){
                    if(getDownloadStatus()==DownloadManager.STATUS_SUCCESSFUL){
                        File file=new File(childFolder.getPath()+File.separator+titleStr+".pdf");

                        pdfView.fromFile(file).enableSwipe(true).enableAntialiasing(true).onError(new OnErrorListener() {
                            @Override
                            public void onError(Throwable t) {
                                Toast.makeText(getApplicationContext(),t.getMessage(),Toast.LENGTH_LONG).show();
                                Log.d("PDFView",t.getMessage());
                            }
                        }).load();
                    }
                }
            }
        },filter);
    }

    private int getDownloadStatus(){
        DownloadManager.Query query=new DownloadManager.Query();
        query.setFilterById(downloadID);
        DownloadManager downloadManager=(DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        Cursor cursor=downloadManager.query(query);
        if(cursor.moveToFirst()){
            int columnIndex=cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            int status=cursor.getInt(columnIndex);

            return status;
        }
        return DownloadManager.ERROR_UNKNOWN;
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser=mAuth.getCurrentUser();
    }

    private void loadPDF(String docID) {
        db.collection("notes").document(docID).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        subStr=String.valueOf(documentSnapshot.get("subject"));
                        titleStr=String.valueOf(documentSnapshot.get("title"));
                        subject.setText(subStr);
                        title.setText(titleStr);
                        createFolder(subStr,titleStr);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(),"Error: "+e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void createFolder(String sub,String title){
        //WORKS AND CREATES FOLDER IN INTERNAL STORAGE
        String folderPath= Environment.getExternalStorageDirectory()+File.separator+"MCA";
        folder=new File(folderPath);
        childFolder=new File(folderPath+File.separator+sub+File.separator);

//        Toast.makeText(getApplicationContext(),childFolder.getAbsolutePath(),Toast.LENGTH_LONG).show();
        if(!folder.exists()){
            success=folder.mkdir();
        }else {
            success=true;
        }

        if(success){
            if(!childFolder.exists()){
                if(childFolder.mkdir()){
                    downloadPDF(sub,title);
                    Log.e("MCA","Folder Created");
                }else {
                    Log.e("MCA","Folder Not Created");
                }
            }else {
                downloadPDF(sub,title);
            }
//            else {
//                if(childFolder.isDirectory()){
//                    String[] children=childFolder.list();
//                    for(int i=0;i<children.length;i++){
//                        new File(childFolder,children[i]).delete();
//                    }
//                }
//            }
        }else {
            Toast.makeText(getApplicationContext(), "ERROR CREATING FOLDER. CHECK APP STORAGE PERMISSIONS", Toast.LENGTH_SHORT).show();
        }
    }

    private void downloadPDF(String subject, String title) {

        String path=Environment.getExternalStorageDirectory()+File.separator+"MCA"+File.separator+subject+File.separator;
        File existingFile=new File(path+title+".pdf");
        if(!existingFile.exists()){
            storageReference= FirebaseStorage.getInstance().getReference("notes/"+subject);
            StorageReference fileRef=storageReference.child(title+".pdf");
            fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
//                WORKS
                    DownloadManager downloadManager= (DownloadManager) getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
                    DownloadManager.Request request=new DownloadManager.Request(uri);
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//                request.setDestinationInExternalPublicDir(path,title+".pdf");
//                request.setDestinationInExternalFilesDir(getApplicationContext(),path,title+".pdf");
                    request.setDestinationUri(Uri.fromFile(new File(path,title+".pdf")));

                    assert downloadManager != null;
                    downloadID=downloadManager.enqueue(request);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(),"Error: "+e.getMessage(),Toast.LENGTH_LONG).show();
                }
            });
        }else {
            pdfView.fromFile(existingFile).onError(new OnErrorListener() {
                @Override
                public void onError(Throwable t) {
                    Toast.makeText(getApplicationContext(),t.getMessage(),Toast.LENGTH_LONG).show();
                }
            }).load();
        }

    }

}
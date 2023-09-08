package com.example.artbook;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.example.artbook.databinding.ActivityArtBinding;
import com.example.artbook.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;

public class ArtActivity extends AppCompatActivity {
    private ActivityArtBinding binding;
    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> permissionLauncher;
    Bitmap selectedImage;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityArtBinding.inflate(getLayoutInflater());
        View view=binding.getRoot();
        setContentView(view);

        registerLauncher();

        Intent intent= getIntent();
        String info=intent.getStringExtra("info");
        if(info.matches("new")){
            clearAll();
        }
        else{
            allEnabled();
            int id=intent.getIntExtra("artId",0);
            binding.buttonSave.setVisibility(View.INVISIBLE);

            try {
                database=openOrCreateDatabase("Arts",MODE_PRIVATE,null);
                Cursor cursor= database.rawQuery("SELECT * FROM arts WHERE id=?",new String[]{String.valueOf(id)});

                int artNameIx= cursor.getColumnIndex("artName");
                int artistNameIx= cursor.getColumnIndex("artistName");
                int yearIx= cursor.getColumnIndex("year");
                int imageIx=cursor.getColumnIndex("image");

                while (cursor.moveToNext()){
                    binding.artNameText.setText(cursor.getString(artNameIx));
                    binding.artistNameText.setText(cursor.getString(artistNameIx));
                    binding.yearText.setText(cursor.getString(yearIx));

                    byte[] bytes= cursor.getBlob(imageIx);
                    Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    binding.imageView.setImageBitmap(bitmap);
                }
                cursor.close();


            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void allEnabled() {
        binding.artNameText.setEnabled(false);
        binding.artistNameText.setEnabled(false);
        binding.yearText.setEnabled(false);
        binding.imageView.setEnabled(false);
    }

    private void clearAll() {
        binding.artNameText.setText("");
        binding.artistNameText.setText("");
        binding.yearText.setText("");
        binding.imageView.setImageResource(R.drawable.baseline_add_photo_alternate_24);
    }

    public void save(View view){

        String artName= binding.artNameText.getText().toString();
        String artistName= binding.artistNameText.getText().toString();
        String year= binding.yearText.getText().toString();

        Bitmap bitmap= makeSmallerImage(selectedImage,400);
        ByteArrayOutputStream outputStream= new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream);
        byte[] bytes = outputStream.toByteArray();

        try {
            database=openOrCreateDatabase("Arts",MODE_PRIVATE,null);
            database.execSQL("CREATE TABLE IF NOT EXISTS arts(id INTEGER PRIMARY KEY, artName VARCHAR,artistName VARCHAR,year VARCHAR,image BLOB )");
            String sql="INSERT INTO arts(artName,artistName,year,image) VALUES (?,?,?,?)";
            SQLiteStatement sqLiteStatement= database.compileStatement(sql);
            sqLiteStatement.bindString(1,artName);
            sqLiteStatement.bindString(2,artistName);
            sqLiteStatement.bindString(3,year);
            sqLiteStatement.bindBlob(4,bytes);
            sqLiteStatement.execute();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        //finish(); yada;
        Intent intent= new Intent(ArtActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);


    }
    public Bitmap makeSmallerImage(Bitmap image, int maximumSize) {

        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;

        if (bitmapRatio > 1) {
            width = maximumSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maximumSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image,width,height,true);
    }
    public void selectImage(View view){
        //izin daha önce verilmiş mi verilmemiş mi kontrolü

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){
            if(ContextCompat.checkSelfPermission(ArtActivity.this, Manifest.permission.READ_MEDIA_IMAGES)!= PackageManager.PERMISSION_GRANTED){
                //izin gerekli
                //kullanıcıya açıklama göstermek zorunda mıyız kontrolü
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_MEDIA_IMAGES)){
                    //açıklama gerekli
                    Snackbar.make(view,"Galeriye gitmek için izin gereklidir.",Snackbar.LENGTH_INDEFINITE).setAction("İzin ver", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //izin işlemleri
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                        }
                    }).show();
                }
                else{
                    //izin işlemleri
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                }

            }
            else{
                //izin daha önceden verilmiş, galeriye git
                Intent intentToGallery= new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);
            }
        }
        else{
            if(ContextCompat.checkSelfPermission(ArtActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                //izin gerekli
                //kullanıcıya açıklama göstermek zorunda mıyız kontrolü
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                    //açıklama gerekli
                    Snackbar.make(view,"Galeriye gitmek için izin gereklidir.",Snackbar.LENGTH_INDEFINITE).setAction("İzin ver", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //izin işlemleri
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                        }
                    }).show();
                }
                else{
                    //izin işlemleri
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                }

            }
            else{
                //izin daha önceden verilmiş, galeriye git
                Intent intentToGallery= new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);
            }
        }




    }

    public void registerLauncher(){
        activityResultLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode()==RESULT_OK){
                    Intent intentFromResult=result.getData();//kullanıcının seçtiği resmi aldık
                    if(intentFromResult!=null){
                       Uri imageData= intentFromResult.getData();//kullanıcın seçtiği resim!!!!
                        //binding.imageView.setImageURI(imageData);

                        //bu resmi veritabanına kaydetmek için aşağıdakiler yapılır.!!!!!!!!!
                        try {
                            if(Build.VERSION.SDK_INT>=28){
                                ImageDecoder.Source source= ImageDecoder.createSource(ArtActivity.this.getContentResolver(),imageData);
                                selectedImage=ImageDecoder.decodeBitmap(source);
                                binding.imageView.setImageBitmap(selectedImage);

                            }
                            else{
                                selectedImage=MediaStore.Images.Media.getBitmap(ArtActivity.this.getContentResolver(),imageData);
                                binding.imageView.setImageBitmap(selectedImage);
                            }
                        }
                        catch (Exception e){
                            e.printStackTrace();
                            Toast.makeText(ArtActivity.this,"HATA",Toast.LENGTH_SHORT).show();
                        }


                    }
                }
            }
        });



        permissionLauncher=registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if(result){
                    Intent intentToGallery= new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery);
                }
                else{
                    Toast.makeText(ArtActivity.this,"İzin verilmedi",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
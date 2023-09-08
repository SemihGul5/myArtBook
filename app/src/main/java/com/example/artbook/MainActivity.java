package com.example.artbook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.example.artbook.databinding.ActivityMainBinding;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    ArrayList<Art> artArrayList;


    ArtAdapter artAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        View view=binding.getRoot();
        setContentView(view);

        artArrayList=new ArrayList<>();

        //adaptör yazdıktan sonra bu kod yazılır. bağlama işlemi
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        artAdapter= new ArtAdapter(artArrayList);
        binding.recyclerView.setAdapter(artAdapter);

        //bu değil
        getData();






    }

    private void getData() {

        try {
            SQLiteDatabase database= openOrCreateDatabase("Arts",MODE_PRIVATE,null);
            Cursor cursor= database.rawQuery("SELECT * FROM arts",null);

            int nameIx= cursor.getColumnIndex("artName");
            int idIx= cursor.getColumnIndex("id");

            while (cursor.moveToNext()){
                String name= cursor.getString(nameIx);
                int id= cursor.getInt(idIx);
                Art art = new Art(name,id);
                artArrayList.add(art);
            }
            //veri geldi, güncelle!
            artAdapter.notifyDataSetChanged();
            cursor.close();
        }

        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater= new MenuInflater(MainActivity.this);
        menuInflater.inflate(R.menu.add_art,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.add_art_item){
            Intent intent= new Intent(MainActivity.this, ArtActivity.class);
            //yeni bir şey mi ekliyor yoksa var olanlara mı basıyor kontrolü için
            intent.putExtra("info","new");
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
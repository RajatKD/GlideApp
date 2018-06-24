package app.mgmt.roaster.com.glideapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.karan.churi.PermissionManager.PermissionManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    static private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    ImageView ivFirst;
    ImageView ivSecond;
    int picNum;

    public static final int EXTERNAL_STORAGE_PERMISSION_CONSTANT = 100;
    public static final int REQUEST_PERMISSION_SETTING = 101;
    private SharedPreferences sharedPreferences;
    final int MY_PERMISSIONS_INTERNET = 222;
    final int REQUEST_PERMISSIONS=555;
    PermissionManager permissionManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ivFirst = findViewById(R.id.ivFirstImage);
        ivSecond = findViewById(R.id.ivSecondImage);

        ivFirst.setOnClickListener(this);
        ivSecond.setOnClickListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater= getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menuGlideApp:
                Intent intent = new Intent(MainActivity.this, GlideApp.class);
                startActivity(intent);
                break;
        }
        return true;
    }



    @TargetApi(Build.VERSION_CODES.M)
    public void showCamera(){
        if ( checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            showCameraPreview();
        }else {
            //Camera permission not granted
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)){
                Toast.makeText(this, "Camera permission needed", Toast.LENGTH_SHORT).show();
            }

            //Request a camera permission
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
        }
    }


    public void showCameraPreview(){

    }

    @Override
    public void onClick(View view) {
        showCamera();
        switch (view.getId()){
            case R.id.ivFirstImage:
                Toast.makeText(this, "1. Image Tapped", Toast.LENGTH_SHORT).show();
                galleryIntent(1);
                break;
            case R.id.ivSecondImage:
                Toast.makeText(this, "2. Image Tapped", Toast.LENGTH_SHORT).show();
                galleryIntent(2);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==REQUEST_CAMERA){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                showCameraPreview();
            }else {
                Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
            }
        }else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    private void galleryIntent(int picNum){
        this.picNum = picNum;
        Intent intent =new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == SELECT_FILE){
                if (picNum == 1)
                    try {
                        GlideApp.with(getApplicationContext())
                                .load(bmp)
                                .override(800, 1200)
                                .into(ivFirst);

                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                else
                    try {

                        GlideApp.with(getApplicationContext())
                                .load(bmp)
                                .into(ivSecond);

                        Toast.makeText(this, "Image second Uri: "+data.getData(), Toast.LENGTH_SHORT).show();
                        ImageCompressor.compressImage(this, data.getData().toString());

                    }catch (Exception ex){
                        Log.e("ERROR:", ""+ex);
                    ex.printStackTrace();
                    }
            }
        }
    }

    public static void SaveImage(Bitmap finalBitmap, String fileName) {
        int QUALITY=100;
        String root = Environment.getExternalStorageDirectory().getAbsolutePath();
        File myDir = new File(root + "/saved_images");
        myDir.mkdirs();
        String fname = "Image-"+ fileName +".jpg";
        File file = new File (myDir, fname);
        if (file.exists ()) file.delete ();

        try {

            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, out);

            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onPermissionGranted(int requestCode){

    }

    public void checkPermission(){

        if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            //Show Information about why you need the permission
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("My Storage permissions");
            builder.setMessage("This app needs storage permission.");
            builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    dialogInterface.cancel();
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CONSTANT);
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });

            builder.show();
        }
    }

}

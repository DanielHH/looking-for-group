package com.example.daniel.lookingforgroup;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CreateGameActivity extends AppCompatActivity implements AsyncResponse {

    private Bitmap bitmap;
    private File destination = null;
    private String imgPath = null;
    private final int PICK_IMAGE_CAMERA = 1, PICK_IMAGE_GALLERY = 2;
    private int MY_PERMISSIONS_REQUEST_CAMERA = 3;
    private int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 4;
    private int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 5;

    ImageView gameAvatar;
    String gameName;
    String description;
    Integer maxPlayers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_game);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        // Capture the layout's TextView and set the string as its text
        TextView gameName = findViewById(R.id.text_game_name);
        gameName.setText(message);

        addListenerOnButton();

        Button buttonSave = findViewById(R.id.btn_save);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitData();
            }
        });
    }

    public void addListenerOnButton() {
        gameAvatar = findViewById(R.id.image_game_avatar);
        gameAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
    }

    // Select image from camera and gallery
    private void selectImage() {
        try {
            if (ContextCompat.checkSelfPermission(CreateGameActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CreateGameActivity.this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);
            }
            if (ContextCompat.checkSelfPermission(CreateGameActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CreateGameActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
            if (ContextCompat.checkSelfPermission(CreateGameActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CreateGameActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            } else {
                final CharSequence[] options = {"Take Photo", "Choose From Gallery","Cancel"};
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(CreateGameActivity.this);
                builder.setTitle("Select Option");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (options[item].equals("Take Photo")) {
                            dialog.dismiss();
                            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                                startActivityForResult(takePictureIntent, PICK_IMAGE_CAMERA);
                            }
                        } else if (options[item].equals("Choose From Gallery")) {
                            dialog.dismiss();
                            Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(pickPhoto, PICK_IMAGE_GALLERY);
                        } else if (options[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
            } } catch (Exception e) {
            Toast.makeText(this, "Camera Permission error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE_CAMERA && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            gameAvatar.setImageBitmap(imageBitmap);
        }
        else if (requestCode == PICK_IMAGE_GALLERY && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bytes);
                Log.e("Activity", "Pick from Gallery::>>> ");

                imgPath = getRealPathFromURI(selectedImage);
                destination = new File(imgPath.toString());
                gameAvatar.setImageBitmap(bitmap);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Audio.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    //get gameAvatar, get textDescription, get gameName. Send to database.
    public String getFormattedDataString () {

        TextView textGameName = findViewById(R.id.text_game_name);
        this.gameName = textGameName.getText().toString();

        EditText editDescription = findViewById(R.id.edit_description);
        this.description = editDescription.getText().toString();

        this.maxPlayers = 5; //TODO: ADD 4 REAL. This is just a test.

        return "{\"game_name\":\"" + gameName + "\",\"location\":\""
                + description + "\",\"max_players\":\"" + maxPlayers + "\"}";
    }

    private void submitData () {
        PostData postData = new PostData();
        postData.delegate = this;
        SharedPreferences sp = getSharedPreferences("myPrefs", MODE_PRIVATE);
        postData.setSP(sp);
        String url = "http://looking-for-group-looking-for-group" +
                ".193b.starter-ca-central-1.openshiftapps.com/matches";
        String jsonData = getFormattedDataString();
        try {
            postData.execute(url, jsonData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void processFinish(String response) {
        if(response.equals("HTTP 200")) {
            Toast.makeText(this, "Added game successfully", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "This did not go well!", Toast.LENGTH_SHORT).show();
        }
    }
}

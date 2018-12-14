package com.example.daniel.lookingforgroup;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.daniel.lookingforgroup.matches.LobbyActivity;
import com.example.daniel.lookingforgroup.matches.Match;
import com.example.daniel.lookingforgroup.matches.MatchesAdapter;
import com.example.daniel.lookingforgroup.matches.OpenGamesActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;


public class UserPageActivity extends AppCompatActivity implements AsyncResponse, AsyncImageResponse {
    private Bitmap bitmap;
    private File imageFile = null;
    private final int PICK_IMAGE_CAMERA = 1, PICK_IMAGE_GALLERY = 2;
    private int MY_PERMISSIONS_REQUEST_CAMERA = 3;
    private int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 4;
    private int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 5;

    TextView name;
    ImageView profilePicture;
    String userId;

    String email;

    ArrayList<Match> matches;

    RecyclerView rvMatches;
    MatchesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_page);

        name = findViewById(R.id.text_profile_name);
        profilePicture = findViewById(R.id.image_profile_picture);
        userId = getIntent().getStringExtra("EXTRA_USER_ID");
        rvMatches = (RecyclerView) findViewById(R.id.my_matches_view);

        getUserData(userId);

        rvMatches.setHasFixedSize(true);
        rvMatches.setLayoutManager(new LinearLayoutManager(this));

        //TODO: Set an if-case here which checks whether this is my profile or someone else's.
        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });
    }

    // Select image from camera and gallery
    private void selectImage() {
        try {
            if (ContextCompat.checkSelfPermission(UserPageActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(UserPageActivity.this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);
            }
            if (ContextCompat.checkSelfPermission(UserPageActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(UserPageActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
            if (ContextCompat.checkSelfPermission(UserPageActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(UserPageActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            } else {
                final CharSequence[] options = {"Take Photo", "Choose From Gallery","Cancel"};
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(UserPageActivity.this);
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

    // TODO: Rescale images.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE_CAMERA && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            bitmap = (Bitmap) extras.get("data");
        }
        else if (requestCode == PICK_IMAGE_GALLERY && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                Log.e("Activity", "Pick from Gallery::>>> ");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Bitmap finalBitmap = bitmapScaler(bitmap);
        profilePicture.setImageBitmap(finalBitmap);
        persistImage(finalBitmap, "profilePic");

        SubmitNewImage();
    }

    private void SubmitNewImage() {
        PostMixedData postMixedData = new PostMixedData();
        postMixedData.delegate = this;
        SharedPreferences sp = getSharedPreferences("myPrefs", MODE_PRIVATE);
        postMixedData.setSP(sp);
        String url = "http://looking-for-group-looking-for-group.193b.starter-ca-central-1.openshiftapps.com/images/" + userId;

            try {
                //execute the async task
                postMixedData.execute(
                        url, "image", "image_" + email + ".jpg", imageFile
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    private Bitmap bitmapScaler(Bitmap bitmap) {
        final int goodWidth = 500;
        float factor = goodWidth / (float) bitmap.getWidth();
        return Bitmap.createScaledBitmap(bitmap, goodWidth, (int) (bitmap.getHeight() * factor), true);
    }

    private void persistImage(Bitmap bitmap, String name) {
        File filesDir = getApplicationContext().getFilesDir();
        imageFile = new File(filesDir, name + ".jpg");

        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
        }
    }

    public void getUserData(String userId) {
        GetData getData = new GetData();
        getData.delegate = this;
        String url = "http://looking-for-group-looking-for-group" +
                ".193b.starter-ca-central-1.openshiftapps.com/user/" + userId;
        try {//execute the async task
            getData.execute(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processFinish(String response) {
        //Handle the response.
        System.out.println(response);

        String name;
        JSONObject userData;
        JSONArray matchesArray;

        if(response.equals("HTTP 200")) {
            Toast.makeText(this, "Successful image change", Toast.LENGTH_SHORT).show();
        }
        else {
            try {
                userData = new JSONObject(response);
                name = userData.getString("name");
                email = userData.getString("email");
                matchesArray = userData.getJSONArray("matches_played");
                matches = Match.createMatchList(matchesArray);
                adapter = new MatchesAdapter(matches, new MatchesAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Match match) {
                        navigateToMatch(match);
                    }
                });
                this.name.setText(name);
                this.rvMatches.setAdapter(adapter);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            getImageData(userId);
        }
    }

    private void navigateToMatch(Match match) {
        Intent intent = new Intent(this, LobbyActivity.class);
        intent.putExtra("match", match);
        startActivity(intent);
    }


    public void getImageData(String userId) {
        GetImageData getImageData = new GetImageData();
        getImageData.delegate = this;

        String url = "http://looking-for-group-looking-for-group" +
                ".193b.starter-ca-central-1.openshiftapps.com/user/" + userId + "/image";
        try {//execute the async task
            getImageData.execute(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processFinish(Bitmap response) {
        try {
            if(response != null) {
                this.profilePicture.setImageBitmap(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
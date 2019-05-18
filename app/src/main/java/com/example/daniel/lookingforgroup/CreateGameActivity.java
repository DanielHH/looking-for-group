package com.example.daniel.lookingforgroup;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.example.daniel.lookingforgroup.HelpClasses.Constants;
import com.example.daniel.lookingforgroup.HelpClasses.FetchAddressIntentService;
import com.example.daniel.lookingforgroup.matches.LobbyActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CreateGameActivity extends AppCompatActivity implements AsyncResponse {

    private Bitmap bitmap;
    private Uri photoURI;
    private File imageFile = null;
    private final int PICK_IMAGE_CAMERA = 1, PICK_IMAGE_GALLERY = 2;
    private int MY_PERMISSIONS_REQUEST_CAMERA = 3;
    private int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 4;
    private int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 5;
    private int REQUEST_LOCATION = 6;

    private String baseUrl;

    ImageView gameAvatar;
    String gameName;
    String location;
    Integer maxPlayers = 2;

    private FusedLocationProviderClient fusedLocationClient;

    protected Location lastLocation;
    private CreateGameActivity.AddressResultReceiver mResultReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_game);

        baseUrl = getResources().getString(R.string.url);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        // Capture the layout's TextView and set the string as its text
        TextView gameName = findViewById(R.id.text_game_name);
        gameName.setText(message);

        addListenerOnButton();

        NumberPicker np = findViewById(R.id.number_max_players);
        np.setMinValue(2);
        np.setMaxValue(100);
        np.setOnValueChangedListener(onValueChangeListener);

        Button buttonAddLocation = findViewById(R.id.button_get_address);
        buttonAddLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLocation();
            }
        });
        Button buttonSave = findViewById(R.id.btn_save);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitData();
            }
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    NumberPicker.OnValueChangeListener onValueChangeListener =
            new NumberPicker.OnValueChangeListener(){
                @Override
                public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                    maxPlayers = numberPicker.getValue();
                }
            };

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
                            dispatchTakePictureIntent();
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

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                // Error occurred while creating the File
                e.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, PICK_IMAGE_CAMERA);
            }
        }
    }

    // TODO: Rescale images.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Uri selectedImage = null;
        if (requestCode == PICK_IMAGE_CAMERA && resultCode == RESULT_OK) {
            selectedImage = photoURI;
        }
        else if (requestCode == PICK_IMAGE_GALLERY && resultCode == RESULT_OK) {
            selectedImage = data.getData();
        }
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 1, bytes);
            Log.e("Activity", "Pick from Gallery::>>> ");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Bitmap finalBitmap = bitmapScaler(bitmap);
        gameAvatar.setImageBitmap(finalBitmap);
        persistImage(finalBitmap, "profilePic");
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
            bitmap.compress(Bitmap.CompressFormat.JPEG, 1, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return image;
    }

    //get gameAvatar, get textDescription, get gameName. Send to database.
    public String getFormattedDataString () {

        TextView textGameName = findViewById(R.id.text_game_name);
        if (!textGameName.getText().toString().equals("")) {
            this.gameName = textGameName.getText().toString();
        } else {
            Toast.makeText(this, "You must have a name for this game", Toast.LENGTH_SHORT).show();
            return "";
        }

        EditText editDescription = findViewById(R.id.edit_location);
        if (!editDescription.getText().toString().equals("")) {
            this.location = editDescription.getText().toString();
        } else {
            Toast.makeText(this, "You must have a location for this game", Toast.LENGTH_SHORT).show();
            return "";
        }

        return "{\"game_name\":\"" + gameName + "\",\"location\":\""
                    + location + "\",\"max_players\":\"" + maxPlayers + "\"}";
    }

    private void submitData () {
        PostData postData = new PostData();
        postData.delegate = this;
        SharedPreferences sp = getSharedPreferences("myPrefs", MODE_PRIVATE);
        postData.setSP(sp);
        String url = baseUrl + "matches";
        String jsonData = getFormattedDataString();
        if(jsonData == "") {
            return;
        }
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
            goToMain();
        }
        else {
            Toast.makeText(this, "This did not go well!", Toast.LENGTH_SHORT).show();
        }
    }

    public void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void checkLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            lastLocation = location;

                            // In some rare cases the location returned can be null
                            if (lastLocation == null) {
                                return;
                            }

                            if (!Geocoder.isPresent()) {
                                Toast.makeText(CreateGameActivity.this,
                                        R.string.no_geocoder_available,
                                        Toast.LENGTH_LONG).show();
                                return;
                            }

                            // Start service and update UI to reflect new location
                            startIntentService();
                        }
                    });
        }
    }

    protected void startIntentService() {
        mResultReceiver = new AddressResultReceiver(new android.os.Handler());
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, lastLocation);
        startService(intent);
    }

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if (resultData == null) {
                return;
            }

            // Display the address string
            // or an error message sent from the intent service.
            String addressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            if (addressOutput == null) {
                addressOutput = "";
            }
            //displayAddressOutput();
            EditText tAddressImage = findViewById(R.id.edit_location);
            tAddressImage.setText(addressOutput);

            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                //showToast(getString(R.string.address_found));
            }

        }
    }


}

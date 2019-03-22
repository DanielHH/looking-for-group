package com.example.daniel.lookingforgroup;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.daniel.lookingforgroup.HelpClasses.Constants;
import com.example.daniel.lookingforgroup.HelpClasses.FetchAddressIntentService;
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

import okhttp3.MediaType;

//TODO: Remake ResisterUserActivity to be a fragment of LoginActivity
public class RegisterUserActivity extends AppCompatActivity implements AsyncResponse {
    private Bitmap bitmap;
    private String currentPhotoPath;
    private Uri photoURI;
    private String email;
    private String name;
    private String password;
    private String passwordRepeat;
    private File imageFile = null;
    private final int PICK_IMAGE_CAMERA = 1, PICK_IMAGE_GALLERY = 2;
    private int MY_PERMISSIONS_REQUEST_CAMERA = 3;
    private int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 4;
    private int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 5;
    private int REQUEST_LOCATION = 6;

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private ImageView profileAvatar;

    private FusedLocationProviderClient fusedLocationClient;

    protected Location lastLocation;
    private AddressResultReceiver mResultReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        addListenerOnButton();

        Button buttonRegister = (Button)findViewById(R.id.btn_register);
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageFile == null) {
                    submitData();
                }
                else {
                    submitMixedData();
                }
            }
        });
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    public void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void submitData() { //TODO: Remove this function if SubmitMixedData works.
        PostData postData = new PostData();
        postData.delegate = this;
        SharedPreferences sp = getSharedPreferences("myPrefs", MODE_PRIVATE);
        postData.setSP(sp);
        String url = "http://looking-for-group-looking-for-group" +
                ".193b.starter-ca-central-1.openshiftapps.com/user";
        String jsonData = getFormattedDataString();

        try {
            //execute the async task
            postData.execute(url, jsonData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void submitMixedData() {
        PostMixedData postMixedData = new PostMixedData();
        postMixedData.delegate = this;
        SharedPreferences sp = getSharedPreferences("myPrefs", MODE_PRIVATE);
        postMixedData.setSP(sp);
        String url = "http://looking-for-group-looking-for-group.193b.starter-ca-central-1.openshiftapps.com/user";

        if (isValidInput()) {
            try {
                //execute the async task
                postMixedData.execute(
                        url, "email", email, "name", name, "password", password,
                        "image", "image_" + email + ".jpg", imageFile
                );
                } catch (Exception e) {
                    e.printStackTrace();
            }
        }
    }

    @Override
    public void processFinish(String response){
        System.out.println(response);
        //TODO: Handle different responses
        if(response.equals("HTTP 200")) {
            Toast.makeText(this, "Registered successfully!", Toast.LENGTH_SHORT).show();
            goToLogin();
        }
        else {
            Toast.makeText(this, "This email is already registered", Toast.LENGTH_SHORT).show();
        }
    }


    public void addListenerOnButton() {
        profileAvatar = findViewById(R.id.image_profile_avatar);
        profileAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
    }

    // Select image from camera and gallery
    private void selectImage() {
        try {
            if (ContextCompat.checkSelfPermission(RegisterUserActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(RegisterUserActivity.this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);
            }
            if (ContextCompat.checkSelfPermission(RegisterUserActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(RegisterUserActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
            if (ContextCompat.checkSelfPermission(RegisterUserActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(RegisterUserActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            } else {
                final CharSequence[] options = {"Take Photo", "Choose From Gallery","Cancel"};
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(RegisterUserActivity.this);
                builder.setTitle("Select Option");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (options[item].equals("Take Photo")) {
                            dialog.dismiss();
                            /*Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                                startActivityForResult(takePictureIntent, PICK_IMAGE_CAMERA);
                            }*/
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
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            Log.e("Activity", "Pick from Gallery::>>> ");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Bitmap finalBitmap = bitmapScaler(bitmap);
        profileAvatar.setImageBitmap(finalBitmap);
        persistImage(finalBitmap, "profilePic");
        checkLocation();
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

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private String getFormattedDataString() { //TODO: Remove this function if SubmitMixedData works.
        TextView tName = findViewById(R.id.nameRegister);
        String name = tName.getText().toString();
        if (name.equals("")) {
            Toast.makeText(this, "There's no name!", Toast.LENGTH_SHORT).show();
            return "";
        }

        TextView tEmail = findViewById(R.id.emailRegister);
        email = tEmail.getText().toString();
        if (!isEmailValid(email)) {
            Toast.makeText(this, "Not a valid email", Toast.LENGTH_SHORT).show();
            return "";
        }

        TextView tPassword = findViewById(R.id.passwordRegister);
        String password = tPassword.getText().toString();

        TextView tPasswordRepeat = findViewById(R.id.passwordRepeatRegister);
        String passwordRepeat = tPasswordRepeat.getText().toString();

        if (!isPasswordValid(password)) {
            Toast.makeText(this, "Password has to consist of at least 7 characters.", Toast.LENGTH_SHORT).show();
            return "";
        }

        if (!password.equals(passwordRepeat)) {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            return "";
        }
         return "{\"email\":\"" + email + "\",\"name\":\"" + name + "\",\"password\":\"" + password + "\"}";
    }

    private boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 6;
    }


    private boolean isValidInput() {
        TextView tName = findViewById(R.id.nameRegister);
        name = tName.getText().toString();
        if (name.equals("")) {
            Toast.makeText(this, "There's no name!", Toast.LENGTH_SHORT).show();
            return false;
        }

        TextView tEmail = findViewById(R.id.emailRegister);
        email = tEmail.getText().toString();
        if (!isEmailValid(email)) {
            Toast.makeText(this, "Not a valid email", Toast.LENGTH_SHORT).show();
            return false;
        }

        TextView tPassword = findViewById(R.id.passwordRegister);
        password = tPassword.getText().toString();

        TextView tPasswordRepeat = findViewById(R.id.passwordRepeatRegister);
        passwordRepeat = tPasswordRepeat.getText().toString();

        if (!isPasswordValid(password)) {
            Toast.makeText(this, "Password has to consist of at least 7 characters.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.equals(passwordRepeat)) {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
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
                                Toast.makeText(RegisterUserActivity.this,
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

    private void fetchAddressButtonHander() {
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
                            Toast.makeText(RegisterUserActivity.this,
                                    R.string.no_geocoder_available,
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        // Start service and update UI to reflect new location
                        startIntentService();
                    }
                });
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
            Log.d("diii: ", addressOutput);
            TextView tAddressImage = findViewById(R.id.textAddressImage);
            tAddressImage.setText(addressOutput);

            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                //showToast(getString(R.string.address_found));
            }

        }
    }
}

package com.timmy.dev.textgif;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import android.os.AsyncTask;

import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import android.widget.Toast;

import com.bumptech.glide.Glide;

import com.timmy.dev.textgif.HelperClasses.Utils;


import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    final String GIF_URL = "https://b.stickchat.io/454/m-ai-spart-rawfile-1476381941859.gif";

    // transparent imageView set on top of the gif to which we apply text when typing
    ImageView ivCaption;
    ImageView ivGif;

    EditText editTextSticker;
    Button buttonSave;

    String currentStickerText;

    byte[] initialGif;

    final int MY_PERMISSIONS_READ_WRITE_EXTERNAL_STORAGE = 1;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case MY_PERMISSIONS_READ_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    buttonSave.setActivated(true);
                } else {
                    Toast.makeText(MainActivity.this, "Write permission not granted", Toast.LENGTH_LONG).show();
                    buttonSave.setActivated(false);
                }
                return;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Utils.stickerTextSize = 12 * getApplicationContext().getResources().getDisplayMetrics().density;

        ivGif = (ImageView) findViewById(R.id.imageView_Gif);
        ivCaption = (ImageView) findViewById(R.id.imageView_Caption);
        buttonSave = (Button) findViewById(R.id.button_save);
        editTextSticker = (EditText) findViewById(R.id.editText_stickerCaption);

        // write on canvas when typing on keyboard
        editTextSticker.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String text = s.toString();
                currentStickerText = text;

                Bitmap bmp = Utils.drawTextOnCanvas(text, null);
                ivCaption.setImageBitmap(bmp);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


        buttonSave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // check if app has write storage permission before saving
                boolean hasPermission = checkAndRequestWritePermissions();
                if (currentStickerText == null || currentStickerText.length() == 0) {
                    Toast.makeText(MainActivity.this, "Please enter text", Toast.LENGTH_LONG).show();
                    return;
                }
                if (hasPermission) {
                    new SaveGif().execute();
                }
            }

            private boolean checkAndRequestWritePermissions() {

                int writePermissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

                if (writePermissionCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_READ_WRITE_EXTERNAL_STORAGE);
                    return false;
                }

                return true;
            }

        });

        new DownloadGif().execute();


    }

    @Override
    protected void onStart() {
        super.onStart();

        // check write storage permission didn't change
        int writePermissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (writePermissionCheck == PackageManager.PERMISSION_GRANTED) {
            buttonSave.setActivated(true);
        } else {
            buttonSave.setActivated(false);
        }
    }


    // performs the initial download of the gif
    class DownloadGif extends AsyncTask<Void, Void, Void> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {

            super.onPreExecute();

            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setTitle("Loading...");
            progressDialog.setMessage("Downloading data from server...");
            progressDialog.setCancelable(false);
            progressDialog.show();

        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                initialGif = Glide.with(getApplicationContext())
                        .load(GIF_URL)
                        .asGif()
                        .toBytes()
                        .into(300, 300)
                        .get();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            } catch (ExecutionException e1) {
                e1.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            Glide.with(getApplicationContext()).load(initialGif).into(ivGif);
            progressDialog.dismiss();

        }
    }

    // performs the saving on the disk of the gif with the text
    class SaveGif extends AsyncTask<Void, Void, Void> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setTitle("Please wait...");
            progressDialog.setMessage("File is saving...");
            progressDialog.setCancelable(false);
            progressDialog.show();

        }

        @Override
        protected Void doInBackground(Void... params) {

            List<Bitmap> encodedGifBitmapArray = Utils.addTextToGifByteArray(initialGif, currentStickerText);
            byte[] encodedGif = Utils.encodeGif(encodedGifBitmapArray);
            Utils.saveGif(encodedGif);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
            Toast.makeText(MainActivity.this, "Save complete", Toast.LENGTH_SHORT).show();
        }
    }


}

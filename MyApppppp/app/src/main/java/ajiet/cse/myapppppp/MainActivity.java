package ajiet.cse.myapppppp;

//import android.support.v7.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;

import ajiet.cse.myapppppp.R;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA_CODE = 100;
    Button buttonCapture, buttonCopy;
    TextView textViewData;
    Bitmap bitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonCapture = findViewById(R.id.button_capture);
        buttonCopy = findViewById(R.id.button_copy);
        textViewData = findViewById(R.id.text_data);
        if(ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    android.Manifest.permission.CAMERA
            },REQUEST_CAMERA_CODE);
        }else{
            initializeCamera();
        }
        buttonCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(MainActivity.this);
            }
        });
        buttonCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String scannedText = textViewData.getText().toString();
                copyToClipboard(scannedText);
            }
        });
    }
    private void initializeCamera() {
        buttonCapture.setEnabled(true);
    }
    private void copyToClipboard(String scannedText) {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService((Context.CLIPBOARD_SERVICE));
        ClipData clip = ClipData.newPlainText("copied Data", scannedText);
        clipboardManager.setPrimaryClip(clip);
        Toast.makeText(MainActivity.this, "copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                    getTextFromImage(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void getTextFromImage(Bitmap bitmap) {
        TextRecognizer recognizer = new TextRecognizer.Builder(this).build();
        if (!recognizer.isOperational()) {
            Toast.makeText(MainActivity.this, "Error Occurred!", Toast.LENGTH_SHORT).show();
        } else {
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> textBlockSparseArray = recognizer.detect(frame);
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < textBlockSparseArray.size(); i++) {
                TextBlock textBlock = textBlockSparseArray.valueAt(i);
                stringBuilder.append(textBlock.getValue());
                stringBuilder.append("\n");
            }
            textViewData.setText(stringBuilder.toString());
            buttonCapture.setText("Retake");
            buttonCopy.setVisibility(View.VISIBLE);
        }

    }


}
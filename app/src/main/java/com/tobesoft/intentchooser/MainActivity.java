package com.tobesoft.intentchooser;

// 참고 소스
// https://developer.android.com/training/camera/photobasics#java
// https://developer.android.com/about/versions/11/privacy/storage

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.tobesoft.intentchooser.databinding.ActivityMainBinding;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    final static String LOG_TAG = "IntentChooser";

    private static final int READ_REQUEST_CODE = 42;

    private ActivityMainBinding binding;

    private String mPhotoFileName = "";
    private String mMovieFileName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        final Context context = this;

        Button getContentButton = binding.getContentButton;
        getContentButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent contentIntent = new Intent(Intent.ACTION_GET_CONTENT);

                contentIntent.addCategory(Intent.CATEGORY_OPENABLE);
                contentIntent.setType("*/*");

                contentIntent.setType(binding.mimetypeSpinner.getSelectedItem().toString());
                contentIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, binding.multiselectCheckbox.isChecked());

                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                Intent camcorderIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

                File photoFile = null, movieFile = null;
                Uri imageFileNameUri, movieFileNameUri;
                try {
                    photoFile = createMediaFile(Environment.DIRECTORY_PICTURES, ".jpg");
                    movieFile = createMediaFile(Environment.DIRECTORY_MOVIES, ".mp4");
                } catch (IOException ex) {
                    // Error occurred while creating the File
                }

                // Android 24 부터 FileProvider를 지원 한다.
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                    assert photoFile != null;
                    imageFileNameUri = FileProvider.getUriForFile(getBaseContext(),
                            "com.tobesoft.intentchooser.fileprovider",
                            photoFile);
                    assert movieFile != null;
                    movieFileNameUri = FileProvider.getUriForFile(getBaseContext(),
                            "com.tobesoft.intentchooser.fileprovider",
                            movieFile);
                } else {
                    imageFileNameUri = Uri.fromFile(photoFile);
                    movieFileNameUri = Uri.fromFile(movieFile);
                }

                cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageFileNameUri);
                camcorderIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, movieFileNameUri);

                Intent chooser  = new Intent(Intent.ACTION_CHOOSER);
                chooser.putExtra(Intent.EXTRA_INTENT, contentIntent);
                chooser.putExtra(Intent.EXTRA_TITLE, "작업 선택");

                Intent[] intentArray = { cameraIntent, camcorderIntent };
                chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

                startActivityForResult(chooser, READ_REQUEST_CODE);
            }
        });

        initSpinnerItem();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == READ_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                assert data != null;

                if (isCameraResult(data)) {
                    binding.uriStringTextview.setText(mPhotoFileName);
                    return;
                } else if (isCamcorderResult(data)) {
                    binding.uriStringTextview.setText(mMovieFileName);
                    return;
                }

                // 파일 탐색기
                if (isMultiSelected(data)) {
                    for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                        binding.uriStringTextview.append(data.getClipData().getItemAt(i).getUri().toString());
                        binding.uriStringTextview.append("\n");
                    }
                } else {
                    binding.uriStringTextview.setText(data.getDataString());
                }

                // TODO: 절대 경로 구하기 nexacro library 이용
                // binding.absolutePathText view.setText();
            } else {
                Toast.makeText(this, "작업이 취소 되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }

        // 임시 파일 삭제는 여기서 한다.
    }

    // mime 타입을 선택하기 위한 스피너 초기화
    private void initSpinnerItem() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter
                .createFromResource(this, R.array.mime_type, R.layout.support_simple_spinner_dropdown_item);
        binding.mimetypeSpinner.setAdapter(adapter);
    }

    private boolean isMultiSelected(Intent data) {
        if (data != null && data.getClipData() != null)
            return data.getClipData().getItemCount() > 1;
        return false;
    }

    private boolean isCameraResult(Intent data) {
        File file = new File(mPhotoFileName);
        if (file.exists() && file.length() == 0) {
            return false;
        } else {
            return file.exists();
        }
    }

    private boolean isCamcorderResult(Intent data) {
        File file = new File(mMovieFileName);
        if (file.exists() && file.length() == 0) {
            return false;
        } else {
            return file.exists();
        }
    }

    private File createMediaFile(String type, String suffix) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String mediaFileName = "tmp_" + timeStamp;
        File storageDir = getExternalFilesDir(type);

        File mediaFile =  File.createTempFile(
                mediaFileName,  /* prefix */
                suffix,
                storageDir      /* directory */
        );

        if (type.equals(Environment.DIRECTORY_PICTURES)) {
            mPhotoFileName = mediaFile.getAbsolutePath();
        } else {
            mMovieFileName = mediaFile.getAbsolutePath();
        }

        return mediaFile;
    }

//    private void tempFileDelete(String path) {
//        File delFile = new File(path);
//        if(delFile.exists()) {
//            if(delFile.delete()){
//                String selection = MediaStore.Images.Media.DATA + " =?";
//                String[] selectionArgs = {path};
//                Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI; //video도 체크해야하나?
//                int count = this.getContentResolver().delete(uri, selection, selectionArgs);
//
//                Log.i(LOG_TAG, "Delete temp image file path: " + path + " provider delete count : " + count);
//            }
//        }
//    }
}
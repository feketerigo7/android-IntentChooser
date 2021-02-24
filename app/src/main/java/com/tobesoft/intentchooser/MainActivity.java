package com.tobesoft.intentchooser;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
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

public class MainActivity extends AppCompatActivity {
    final static String LOG_TAG = "IntentChooser";

    private static final int READ_REQUEST_CODE = 42;

    private ActivityMainBinding binding;

    private String mImageFileName = "";
    private String mVideoFileName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        Button getContentButton = binding.getContentButton;
        getContentButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent contentIntent = new Intent(Intent.ACTION_GET_CONTENT);
                contentIntent.setType(binding.mimetypeSpinner.getSelectedItem().toString());
                contentIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, binding.multiselectCheckbox.isChecked());

                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                Intent camcorderIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

                setOutputFileName();
                Uri imageFileNameUri = Uri.fromFile(new File(mImageFileName));
                Uri videoFileNameUri = Uri.fromFile(new File(mVideoFileName));
                cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageFileNameUri);
                camcorderIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, videoFileNameUri);

                Intent chooser  = new Intent(Intent.ACTION_CHOOSER);
                chooser.putExtra(Intent.EXTRA_INTENT, contentIntent);
                chooser.putExtra(Intent.EXTRA_TITLE, "작업 선택");

                Intent[] intentArray = { cameraIntent, camcorderIntent };
                chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

                startActivityForResult(chooser, READ_REQUEST_CODE);
//                Intent contentCreate = new Intent(Intent.ACTION_CREATE_DOCUMENT);
//                contentCreate.addCategory(Intent.CATEGORY_OPENABLE);
//                contentCreate.setType("application/pdf");
//                contentCreate.putExtra(Intent.EXTRA_TITLE, "invoice.pdf");
//                startActivityForResult(contentCreate, READ_REQUEST_CODE);
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
                    binding.uriStringTextview.setText(mImageFileName);
                    return;
                } else if (isCamcorderResult(data)) {
                    binding.uriStringTextview.setText(mVideoFileName);
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
        File file = new File(mImageFileName);
        return file.exists();
    }

    private boolean isCamcorderResult(Intent data) {
        File file = new File(mVideoFileName);
        return file.exists();
    }

    // 카메라 및 캠코더로부터 생성되는 파일명 설정
    private void setOutputFileName() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            mImageFileName = getBaseContext().getFilesDir() + "/tmp_" + System.currentTimeMillis() + ".jpg";
            mVideoFileName = getBaseContext().getFilesDir() + "/tmp_" + String.valueOf(System.currentTimeMillis()) + ".mp4";
        } else {
            mImageFileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/tmp_" + System.currentTimeMillis() + ".jpg";
            mVideoFileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/tmp_" + System.currentTimeMillis() + ".mp4";
        }
    }
}
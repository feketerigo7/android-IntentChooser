package com.tobesoft.intentchooser;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.tobesoft.intentchooser.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    final static String LOG_TAG = "IntentChooser";

    private static final int READ_REQUEST_CODE = 42;

    private ActivityMainBinding binding;

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
                // TODO: Multi select 처리 필요
                assert data != null;
                binding.uriStringTextview.setText(data.getDataString());

                // TODO: 절대 경로 구하기 nexacro library 이용
                // binding.absolutePathTextview.setText();
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
}
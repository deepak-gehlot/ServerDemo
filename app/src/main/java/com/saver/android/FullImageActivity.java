package com.saver.android;

import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.saver.android.databinding.ActivityFullImageBinding;

import java.io.File;


public class FullImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityFullImageBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_full_image);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            File file = (File) bundle.get("item");
            binding.imageView.setImage(ImageSource.uri(Uri.fromFile(file)));
            binding.setFile(file);
        } else {
            finish();
        }
    }
}

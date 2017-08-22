package com.saver.android;

import android.Manifest;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.github.jksiezni.permissive.PermissionsGrantedListener;
import com.github.jksiezni.permissive.PermissionsRefusedListener;
import com.github.jksiezni.permissive.Permissive;
import com.saver.android.adapter.ImagePagerAdapter;
import com.saver.android.databinding.ActivityMainBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import static org.apache.commons.io.comparator.LastModifiedFileComparator.LASTMODIFIED_REVERSE;

public class DownloadListActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setSupportActionBar(binding.toolbar);
        getListOfFiles();

        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void getListOfFiles() {
        new Permissive.Request(Manifest.permission.READ_EXTERNAL_STORAGE)
                .whenPermissionsGranted(new PermissionsGrantedListener() {
                    @Override
                    public void onPermissionsGranted(String[] permissions) throws SecurityException {
                        // given permissions are granted
                        ArrayList<File> result = new ArrayList<>(); //ArrayList cause you don't know how many files there is
                        try {
                            File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/" + Constant.FOLDER_NAME);
                            if (!folder.exists()) {
                                Toast.makeText(DownloadListActivity.this, "No file found.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            File[] filesInFolder = folder.listFiles(); // This returns all the folders and files in your path
                            Arrays.sort(filesInFolder, LASTMODIFIED_REVERSE);
                            for (File file : filesInFolder) { //For each of the entries do:
                                //check that it's not a dir
                                result.add(file); //push the filename as a string

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        setList(result);
                    }
                }).whenPermissionsRefused(new PermissionsRefusedListener() {
            @Override
            public void onPermissionsRefused(String[] permissions) {
                // given permissions are refused
                Toast.makeText(DownloadListActivity.this, "App Required permission.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).execute(DownloadListActivity.this);
    }

    private void setList(final ArrayList<File> files) {
        files.add(0, null);
        int count = 0;
        for (int i = 0; i < files.size(); i++) {
            count++;
            if (count == 4) {
                count = 0;
                files.add(i, null);
            }
        }

        ImagePagerAdapter adapter = new ImagePagerAdapter(DownloadListActivity.this, files);
        binding.hicvp.setAdapter(adapter);

        //binding.setFile(files.get(0));
        binding.hicvp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                int pos = binding.hicvp.getRealItem();
                if (files.get(pos) != null) {
                    binding.setFile(files.get(pos));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        /*if (files.size() == 1) {
            binding.messageTxt.setVisibility(View.VISIBLE);
        } else {
            binding.messageTxt.setVisibility(View.GONE);
        }*/
    }
}

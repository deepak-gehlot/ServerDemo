package com.saver.android;

import android.Manifest;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.github.jksiezni.permissive.PermissionsGrantedListener;
import com.github.jksiezni.permissive.PermissionsRefusedListener;
import com.github.jksiezni.permissive.Permissive;
import com.saver.android.adapter.ImageListAdapter;
import com.saver.android.databinding.ActivityMainBinding;
import com.saver.android.util.Constant;
import com.saver.android.util.Extension;
import com.saver.android.util.ValidationTemplate;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.InfiniteScrollAdapter;
import com.yarolegovich.discretescrollview.Orientation;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

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

        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getListOfFiles();
            }
        }, 400);
    }

    private void getListOfFiles() {
        new Permissive.Request(Manifest.permission.READ_EXTERNAL_STORAGE)
                .whenPermissionsGranted(new PermissionsGrantedListener() {
                    @Override
                    public void onPermissionsGranted(String[] permissions) throws SecurityException {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                new LoadImageTask().execute();
                            }
                        }).start();
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


    private ArrayList<File> getFiles() {
        final ArrayList<File> result = new ArrayList<>(); //ArrayList cause you don't know how many files there is
        try {
            File folder = new File(Environment.getExternalStorageDirectory() + "/" + Constant.FOLDER_NAME);
            if (!folder.exists()) {
                Toast.makeText(DownloadListActivity.this, "No file found.", Toast.LENGTH_SHORT).show();
            } else {
                File[] filesInFolder = folder.listFiles(); // This returns all the folders and files in your path
                Arrays.sort(filesInFolder, LASTMODIFIED_REVERSE);
                for (File file : filesInFolder) { //For each of the entries do:
                    //check that it's not a dir
                    result.add(file); //push the filename as a string
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private class LoadImageTask extends AsyncTask<Void, Void, ArrayList<File>> {
        @Override
        protected ArrayList<File> doInBackground(Void... voids) {
            return getFiles();
        }

        @Override
        protected void onPostExecute(ArrayList<File> aVoid) {
            super.onPostExecute(aVoid);
            setList(aVoid);
        }
    }

    private void setList(ArrayList<File> files) {
        if (Extension.getInstance().executeStrategy(DownloadListActivity.this, "", ValidationTemplate.INTERNET)) {
            //files.add(0, null);
            int count = 0;
            for (int i = 0; i < files.size(); i++) {
                count++;
                if (count == 4) {
                    count = 0;
                    files.add(i, null);
                }
            }
        }
        setCard(files);
    }

    private void setCard(final ArrayList<File> files) {
        binding.itemPicker.setOrientation(Orientation.HORIZONTAL);
        final InfiniteScrollAdapter infiniteAdapter = InfiniteScrollAdapter.wrap(new ImageListAdapter(DownloadListActivity.this, files));
        binding.itemPicker.setAdapter(infiniteAdapter);
        binding.itemPicker.setItemTransitionTimeMillis(150);
        binding.itemPicker.setItemTransformer(new ScaleTransformer.Builder()
                .setMinScale(0.8f)
                .build());
        binding.itemPicker.addOnItemChangedListener(new DiscreteScrollView.OnItemChangedListener<RecyclerView.ViewHolder>() {
            @Override
            public void onCurrentItemChanged(@Nullable RecyclerView.ViewHolder viewHolder, int adapterPosition) {
                int pos = infiniteAdapter.getRealCurrentPosition();
                File file = files.get(pos);
                if (file != null) {
                    binding.setFile(file);
                }
            }
        });
    }
}

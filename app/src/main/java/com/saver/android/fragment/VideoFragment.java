package com.saver.android.fragment;

import android.Manifest;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.jksiezni.permissive.PermissionsGrantedListener;
import com.github.jksiezni.permissive.PermissionsRefusedListener;
import com.github.jksiezni.permissive.Permissive;
import com.saver.android.R;
import com.saver.android.adapter.ImageListAdapter;
import com.saver.android.databinding.FragmentImageBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import static org.apache.commons.io.comparator.LastModifiedFileComparator.LASTMODIFIED_REVERSE;

/**
 * A simple {@link Fragment} subclass.
 */
public class VideoFragment extends Fragment {


    public VideoFragment() {
        // Required empty public constructor
    }

    public static VideoFragment newInstance() {
        VideoFragment fragment = new VideoFragment();
        return fragment;
    }

    private FragmentImageBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_image, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // File file = new File(new StringBuffer().append(Environment.getExternalStorageDirectory().getAbsolutePath()).append("/WhatsApp/Media/.Statuses/").toString());

        getListOfFiles();
    }


    private void getListOfFiles() {
        new Permissive.Request(Manifest.permission.READ_EXTERNAL_STORAGE)
                .whenPermissionsGranted(new PermissionsGrantedListener() {
                    @Override
                    public void onPermissionsGranted(String[] permissions) throws SecurityException {
                        // given permissions are granted
                        ArrayList<File> result = new ArrayList<>(); //ArrayList cause you don't know how many files there is
                        try {
                            File folder = new File(new StringBuffer().append(Environment.getExternalStorageDirectory().getAbsolutePath()).append("/WhatsApp/Media/.Statuses/").toString());
                            File[] filesInFolder = folder.listFiles(); // This returns all the folders and files in your path
                            Arrays.sort(filesInFolder, LASTMODIFIED_REVERSE);
                            for (File file : filesInFolder) { //For each of the entries do:
                                if (!file.isDirectory() && file.getName().endsWith(".mp4")) { //check that it's not a dir
                                    result.add(file); //push the filename as a string
                                }
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
                Toast.makeText(getActivity(), "App Required permission.", Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        }).execute(getActivity());
    }


    private void setList(ArrayList<File> files) {
        binding.imageList.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        ImageListAdapter adapter = new ImageListAdapter(getActivity(), files);
        binding.imageList.setAdapter(adapter);
        /*if (files.size() == 1) {
            binding.messageTxt.setVisibility(View.VISIBLE);
        } else {
            binding.messageTxt.setVisibility(View.GONE);
        }*/
    }
}

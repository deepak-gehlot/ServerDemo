package com.saver.android.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.appunite.appunitevideoplayer.PlayerActivity;
import com.google.android.gms.ads.AdRequest;
import com.saver.android.FullImageActivity;
import com.saver.android.R;
import com.saver.android.databinding.AdViewBinding;
import com.saver.android.databinding.ImageRowBinding;
import com.saver.android.util.Constant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;


/**
 * Created by RWS 6 on 7/20/2017.
 */
public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.ViewHolder> {

    private Context context;
    private ArrayList<File> files;
    private int TYPE_AD = 0;
    private int TYPE_VIEW = 1;

    public ImageListAdapter(Context context, ArrayList<File> files) {
        this.files = files;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_AD) {
            AdViewBinding adViewBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.ad_view, parent, false);
            return new ViewHolder(adViewBinding);
        } else {
            ImageRowBinding imageRowBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.image_row, parent, false);
            return new ViewHolder(imageRowBinding);
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (files.get(position) == null) {
            AdRequest adRequest1 = new AdRequest.Builder().build();
            AdRequest adRequest2 = new AdRequest.Builder().build();
            AdRequest adRequest3 = new AdRequest.Builder().build();
            holder.adViewBinding.adView1.loadAd(adRequest1);
            holder.adViewBinding.adView2.loadAd(adRequest2);
            holder.adViewBinding.adView3.loadAd(adRequest3);

        } else {
            try {
                holder.binding.setFile(files.get(position));
                holder.binding.setAdapter(this);
                if (isVideoFile(Uri.fromFile(files.get(position)).toString())) {
                    holder.binding.playImg.setVisibility(View.VISIBLE);
                } else {
                    holder.binding.playImg.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (files.get(position) == null) {
            return TYPE_AD;
        } else {
            return TYPE_VIEW;
        }
    }


    @Override
    public int getItemCount() {
        return files.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageRowBinding binding;
        AdViewBinding adViewBinding;

        private ViewHolder(ImageRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        private ViewHolder(AdViewBinding binding) {
            super(binding.getRoot());
            this.adViewBinding = binding;
        }
    }

    private static boolean isVideoFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("video");
    }

    public void onItemClick(File file) {
        if (isVideoFile(Uri.fromFile(file).toString())) {
            context.startActivity(PlayerActivity.getVideoPlayerIntent(context,
                    Uri.fromFile(file).toString(),
                    file.getName(), R.drawable.ic_video_play));
        } else {
            context.startActivity(new Intent(context, FullImageActivity.class).putExtra("item", file));
        }
    }

    public void saveImage(final File sourceLocation) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File path = Environment.getExternalStorageDirectory();
                    File folder = new File(Environment.getExternalStorageDirectory() + "/" + Constant.FOLDER_NAME);
                    if (!folder.exists()) {
                        folder.mkdir();
                    }
                    String fileFormat = "";
                    if (isVideoFile(Uri.fromFile(sourceLocation).toString())) {
                        fileFormat = ".mp4";
                    } else {
                        fileFormat = ".jpeg";
                    }

                    // Create a media file name
                    String timeStamp = "" + new Date().getTime();
                    String mImageName = "Whats_" + timeStamp + fileFormat;
                    File dest = new File(path, Constant.FOLDER_NAME + "/" + mImageName);
                    InputStream in = new FileInputStream(sourceLocation);
                    OutputStream out = new FileOutputStream(dest);

                    // Copy the bits from instream to outstream
                    byte[] buf = new byte[1024];
                    int len;

                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }

                    in.close();
                    out.close();
                    addImageToGallery(dest.getAbsolutePath(), fileFormat, context);
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "Save successfully.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "Save failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }


    private void addImageToGallery(final String filePath, String fileFormat, final Context context) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        if (fileFormat.equals(".mp4")) {
            values.put(MediaStore.Images.Media.MIME_TYPE, "video/mp4");
        } else {
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        }
        values.put(MediaStore.MediaColumns.DATA, filePath);

        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    public void createOtherAppShareIntent(File photoFile) {
        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
        if (isVideoFile(Uri.fromFile(photoFile).toString())) {
            shareIntent.setType("video/*");
        } else {
            shareIntent.setType("image/*");
        }
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(photoFile));
        context.startActivity(Intent.createChooser(shareIntent, "Share image using"));
    }
}
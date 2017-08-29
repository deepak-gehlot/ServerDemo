package com.saver.android.adapter;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.appunite.appunitevideoplayer.PlayerActivity;
import com.google.android.gms.ads.AdRequest;
import com.saver.android.util.Constant;
import com.saver.android.FullImageActivity;
import com.saver.android.R;
import com.saver.android.databinding.AdViewBinding;
import com.saver.android.databinding.PagerImageRowBinding;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


/**
 * Created by RWS 6 on 7/20/2017.
 */
public class ImagePagerAdapter extends PagerAdapter {

    private Context context;
    private ArrayList<File> files;

    public ImagePagerAdapter(Context context, ArrayList<File> files) {
        this.files = files;
        this.context = context;
    }

    @Override
    public int getCount() {
        return files.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (files.get(position) != null) {
            PagerImageRowBinding pagerImageRowBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.pager_image_row, container, false);
            pagerImageRowBinding.setFile(files.get(position));
            pagerImageRowBinding.setAdapter(this);
            //file.getName().endsWith(".jpg")
            container.addView(pagerImageRowBinding.getRoot());

            if (isVideoFile(Uri.fromFile(files.get(position)).toString())) {
                pagerImageRowBinding.playImg.setVisibility(View.VISIBLE);
            } else {
                pagerImageRowBinding.playImg.setVisibility(View.GONE);
            }

            return pagerImageRowBinding.getRoot();
        } else {
            AdViewBinding adViewBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.ad_view, container, false);
            container.addView(adViewBinding.getRoot());
            AdRequest adRequest1 = new AdRequest.Builder().build();
            AdRequest adRequest2 = new AdRequest.Builder().build();
            AdRequest adRequest3 = new AdRequest.Builder().build();
            adViewBinding.adView1.loadAd(adRequest1);
            adViewBinding.adView2.loadAd(adRequest2);
            adViewBinding.adView3.loadAd(adRequest3);
            return adViewBinding.getRoot();
        }
    }

    @Override
    public boolean isViewFromObject(final View view, final Object object) {
        return view.equals(object);
    }

    @Override
    public void destroyItem(final ViewGroup container, final int position, final Object object) {
        container.removeView((View) object);
    }


    public static boolean isVideoFile(String path) {
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

    public void saveImage(File sourceLocation) {
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
                fileFormat = ".jpg";
            }

            // Create a media file name
            String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
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
            Toast.makeText(context, "Save successfully.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Save failed.", Toast.LENGTH_SHORT).show();
        }
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
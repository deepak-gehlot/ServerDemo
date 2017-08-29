package com.saver.android.adapter;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.appunite.appunitevideoplayer.PlayerActivity;
import com.google.android.gms.ads.AdRequest;
import com.saver.android.FullImageActivity;
import com.saver.android.R;
import com.saver.android.databinding.AdViewBinding;
import com.saver.android.databinding.DownloadPagerImageRowBinding;
import com.saver.android.listener.DeleteListener;

import java.io.File;
import java.net.URLConnection;
import java.util.ArrayList;


/**
 * Created by RWS 6 on 7/20/2017.
 */
public class ImageDownloadPagerAdapter extends PagerAdapter {

    private Context context;
    private ArrayList<File> files;
    private DeleteListener deleteListener;

    public ImageDownloadPagerAdapter(Context context, ArrayList<File> files) {
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
            DownloadPagerImageRowBinding pagerImageRowBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.download_pager_image_row, container, false);
            pagerImageRowBinding.setFile(files.get(position));
            pagerImageRowBinding.setAdapter(this);
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

    public void setDeleteListener(DeleteListener deleteListener) {
        this.deleteListener = deleteListener;
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

    public void deleteImage(File file) {
        if (file.delete()) {
            deleteListener.onDelete();
            Toast.makeText(context, "deleted.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Some error occurred.", Toast.LENGTH_SHORT).show();
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
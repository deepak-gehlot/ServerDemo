package com.saver.android.adapter;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
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
import com.saver.android.databinding.DownloadImageRowBinding;
import com.saver.android.listener.DeleteListener;

import java.io.File;
import java.net.URLConnection;
import java.util.ArrayList;


/**
 * Created by RWS 6 on 7/20/2017.
 */
public class DownloadImageListAdapter extends RecyclerView.Adapter<DownloadImageListAdapter.ViewHolder> {

    private Context context;
    private ArrayList<File> files;
    private int TYPE_AD = 0;
    private int TYPE_VIEW = 1;
    private DeleteListener deleteListener;

    public DownloadImageListAdapter(Context context, ArrayList<File> files) {
        this.files = files;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_AD) {
            AdViewBinding adViewBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.ad_view, parent, false);
            return new ViewHolder(adViewBinding);
        } else {
            DownloadImageRowBinding imageRowBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.download_image_row, parent, false);
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

        DownloadImageRowBinding binding;
        AdViewBinding adViewBinding;

        private ViewHolder(DownloadImageRowBinding binding) {
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

    public void deleteImage(File file) {
        if (file.delete()) {
            deleteListener.onDelete();
            files.remove(file);
            Toast.makeText(context, "deleted.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Some error occurred.", Toast.LENGTH_SHORT).show();
        }
    }

    public void setDeleteListener(DeleteListener deleteListener) {
        this.deleteListener = deleteListener;
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
package com.saver.android;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.jksiezni.permissive.PermissionsGrantedListener;
import com.github.jksiezni.permissive.PermissionsRefusedListener;
import com.github.jksiezni.permissive.Permissive;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.saver.android.adapter.ImageListAdapter;
import com.saver.android.databinding.ActivityMainBinding;
import com.saver.android.util.Extension;
import com.saver.android.util.PreferenceConnector;
import com.saver.android.util.ValidationTemplate;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.InfiniteScrollAdapter;
import com.yarolegovich.discretescrollview.Orientation;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import static org.apache.commons.io.comparator.LastModifiedFileComparator.LASTMODIFIED_REVERSE;

public class MainActivity extends AppCompatActivity implements DiscreteScrollView.OnItemChangedListener {

    private ActivityMainBinding binding;
    private int viewTag = 1;
    ArrayList<File> files = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setSupportActionBar(binding.toolbar);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getListOfFiles();
            }
        }, 400);

        int count = PreferenceConnector.readInteger(MainActivity.this, PreferenceConnector.SHOW_AD_COUNT, 0);
        //   if (count == 4) { // show ad
        PreferenceConnector.writeInteger(MainActivity.this, PreferenceConnector.SHOW_AD_COUNT, 0);
        final InterstitialAd mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-7317243541737447/7427644400");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                Log.i("Ads", "onAdLoaded");
                mInterstitialAd.show();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
                Log.i("Ads", "onAdFailedToLoad");
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
                Log.i("Ads", "onAdOpened");
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
                Log.i("Ads", "onAdLeftApplication");
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when when the interstitial ad is closed.
                Log.i("Ads", "onAdClosed");
            }
        });
       /* } else {
            count = count + 1;
            PreferenceConnector.writeInteger(MainActivity.this, PreferenceConnector.SHOW_AD_COUNT, count);
        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_create_invoice, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                shareAppUrl();
                return true;
            case R.id.action_download:
                startActivity(new Intent(MainActivity.this, DownloadListActivity.class));
                return true;
            case R.id.action_disclamer:
                showDisclaimer();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCurrentItemChanged(@Nullable RecyclerView.ViewHolder viewHolder, int adapterPosition) {

    }

    private void getListOfFiles() {
        new Permissive.Request(Manifest.permission.READ_EXTERNAL_STORAGE)
                .whenPermissionsGranted(new PermissionsGrantedListener() {
                    @Override
                    public void onPermissionsGranted(String[] permissions) throws SecurityException {
                        new LoadImageTask().execute();
                    }
                }).whenPermissionsRefused(new PermissionsRefusedListener() {
            @Override
            public void onPermissionsRefused(String[] permissions) {
                // given permissions are refused
                Toast.makeText(MainActivity.this, "App Required permission.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).execute(MainActivity.this);
    }

    private void setList() {
        if (Extension.getInstance().executeStrategy(MainActivity.this, "", ValidationTemplate.INTERNET)) {
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
        setCard();
    }

    private void setCard() {
        binding.itemPicker.setOrientation(Orientation.HORIZONTAL);
        final InfiniteScrollAdapter infiniteAdapter = InfiniteScrollAdapter.wrap(new ImageListAdapter(MainActivity.this, files));
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


    private ArrayList<File> getFiles() {
        final ArrayList<File> result = new ArrayList<>(); //ArrayList cause you don't know how many files there is
        try {
            File folder = new File(new StringBuffer().append(Environment.getExternalStorageDirectory().getAbsolutePath()).append("/WhatsApp/Media/.Statuses/").toString());
            if (!folder.exists()) {
                Toast.makeText(MainActivity.this, "No file found.", Toast.LENGTH_SHORT).show();
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
            files.addAll(aVoid);
            setList();
        }
    }

    private void shareAppUrl() {
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, "Status Saver");
            String sAux = "\nLet me recommend you this application\n\n";
            sAux = sAux + "https://play.google.com/store/apps/details?id=com.saver.android\n\n";
            i.putExtra(Intent.EXTRA_TEXT, sAux);
            startActivity(Intent.createChooser(i, "choose one"));
        } catch (Exception e) {
            //e.toString();
        }
    }

    private void showDisclaimer() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Disclaimer");
        builder.setMessage("If you notice that any content in our app violates copyrights than please inform us so that we remove that content.");
        builder.setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
//Status Downloader for whatsapp is in no way affiliated with, sponsored or endorsed by WhatsApp, Inc. If you notice that any content in our app violates copyrights than please inform us so that we remove that content.
    }
}

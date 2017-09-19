package com.mthaler.knittings;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class PhotoActivity extends AppCompatActivity {

    public static final String EXTRA_PHOTO_ID = "com.mthaler.knitting.PHOTO_ID";
    public static final String LOG_TAG = PhotoActivity.class.getSimpleName();

    private long parentKnittingID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // get the id of the knitting that should be displayed.
        final long id = getIntent().getLongExtra(EXTRA_PHOTO_ID, -1);
        final Photo photo = KnittingsDataSource.getInstance(this).getPhoto(id);

        // save parent knitting id
        parentKnittingID = getIntent().getLongExtra(KnittingActivity.EXTRA_KNITTING_ID, -1);

        // init knitting
        final PhotoDetailsView photoDetailsView = (PhotoDetailsView) getSupportFragmentManager().findFragmentById(R.id.fragment_photo);
        photoDetailsView.init(photo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.photo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_delete_photo:
                AlertDialog.Builder alert = new AlertDialog.Builder(this)
                        .setTitle(R.string.delete_photo_dialog_title)
                        .setMessage(R.string.delete_photo_dialog_question)
                        .setPositiveButton(R.string.delete_photo_dialog_delete_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                final PhotoDetailsView photoDetailsView = (PhotoDetailsView) getSupportFragmentManager().findFragmentById(R.id.fragment_photo);
                                photoDetailsView.deletePhoto();
                                dialogInterface.dismiss();
                                finish();
                            }
                        }).setNegativeButton(R.string.delete_photo_dialog_cancel_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                alert.show();
                return true;
            case R.id.menu_item_set_main_photo:
                final PhotoDetailsView photoDetailsView = (PhotoDetailsView) getSupportFragmentManager().findFragmentById(R.id.fragment_photo);
                final Photo photo = photoDetailsView.getPhoto();
                final Knitting knitting = KnittingsDataSource.getInstance(this).getKnitting(photo.getKnittingID());
                knitting.setDefaultPhoto(photo);
                KnittingsDataSource.getInstance(this).updateKnitting(knitting);
                Log.d(LOG_TAG, "Set " + photo + " as default photo");
                CoordinatorLayout layout = (CoordinatorLayout)findViewById(R.id.photo_activity_layout);
                Snackbar.make(layout, "Used as main photo", Snackbar.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Nullable
    @Override
    public Intent getSupportParentActivityIntent() {
        // add the knitting id so the parent activity can properly restore itself
        final Intent intent = super.getSupportParentActivityIntent();
        intent.putExtra(KnittingActivity.EXTRA_KNITTING_ID, parentKnittingID);
        return intent;
    }
}

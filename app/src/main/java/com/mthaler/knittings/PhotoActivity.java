package com.mthaler.knittings;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

public class PhotoActivity extends AppCompatActivity {

    public static final String EXTRA_PHOTO_ID = "com.mthaler.knitting.PHOTO_ID";

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

    @Nullable
    @Override
    public Intent getSupportParentActivityIntent() {
        // add the knitting id so the parent activity can properly restore itself
        final Intent intent = super.getSupportParentActivityIntent();
        intent.putExtra(KnittingActivity.EXTRA_KNITTING_ID, parentKnittingID);
        return intent;
    }
}

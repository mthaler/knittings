package com.mthaler.knittings;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;

public class PhotoActivity extends AppCompatActivity {

    public static final String EXTRA_PHOTO_ID = "com.mthaler.knitting.PHOTO_ID";

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

        // init knitting
        final PhotoDetailsView photoDetailsView = (PhotoDetailsView) getSupportFragmentManager().findFragmentById(R.id.fragment_photo);
        photoDetailsView.init(photo);
    }
}

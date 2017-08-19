package com.mthaler.knittings;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

/**
 * KnittingActivity displays a single knitting using KnittingFragment
 *
 * The activity is displayed when a new knitting is added or if a knitting is clicked
 * in the knittings list.
 *
 * The id of the knitting that should be displayed must be passed when the activity is started
 */
public class KnittingActivity extends AppCompatActivity {

    public static final String EXTRA_KNITTING_ID = "com.mthaler.knitting.KNITTING_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_knitting);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Knitting knitting;
        if (savedInstanceState != null) {
            final long id = savedInstanceState.getLong(EXTRA_KNITTING_ID);
            knitting = KnittingsDataSource.getInstance(this.getApplicationContext()).getKnitting(id);
        } else {
            // get the id of the knitting that should be displayed.
            final long id = getIntent().getLongExtra(EXTRA_KNITTING_ID, -1);
            knitting = KnittingsDataSource.getInstance(this.getApplicationContext()).getKnitting(id);
        }

        // init knitting
        final KnittingDetailsView knittingDetailsView = (KnittingDetailsView) getSupportFragmentManager().findFragmentById(R.id.fragment_knitting);
        knittingDetailsView.init(knitting);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.knitting, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_delete_knitting:
                AlertDialog.Builder alert = new AlertDialog.Builder(this)
                        .setTitle(R.string.delete_knitting_dialog_title)
                        .setMessage(R.string.delete_knitting_dialog_question)
                        .setPositiveButton(R.string.delete_knitting_dialog_delete_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                final KnittingDetailsView knittingDetailsView = (KnittingDetailsView) getSupportFragmentManager().findFragmentById(R.id.fragment_knitting);
                                knittingDetailsView.deleteKnitting();
                                dialogInterface.dismiss();
                                finish();
                            }
                        }).setNegativeButton(R.string.delete_knitting_dialog_cancel_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                alert.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        final KnittingDetailsView knittingDetailsView = (KnittingDetailsView) getSupportFragmentManager().findFragmentById(R.id.fragment_knitting);
        final Knitting knitting = knittingDetailsView.getKnitting();
        if (knitting != null) {
            outState.putLong(EXTRA_KNITTING_ID, knitting.getId());
        }
        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
    }


}

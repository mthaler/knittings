package com.mthaler.knittings;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

/**
 * The main activity that gets displayed when the app is started.
 * It displays a list of knittings. The user can add new knittings or
 * edit existing ones
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_create_add_knitting);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            final KnittingListView knittingListView = (KnittingListView) getSupportFragmentManager().findFragmentById(R.id.fragment_knitting_list);
            knittingListView.addKnitting();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.knittings_list, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.menu_item_new_knitting:
//                final KnittingListView knittingListView = (KnittingListView) getSupportFragmentManager().findFragmentById(R.id.fragment_knitting_list);
//                knittingListView.addKnitting();
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

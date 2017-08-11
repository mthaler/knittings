package com.mthaler.knittings;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new KnittingListFragment();
    }

    @Override
    protected void onResume() {
        super.onResume();
        KnittingsDataSource.getInstance(this).open();
    }

    @Override
    protected void onPause() {
        super.onPause();
        KnittingsDataSource.getInstance(this).close();
    }
}

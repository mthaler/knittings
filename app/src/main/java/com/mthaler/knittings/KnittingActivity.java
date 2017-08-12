package com.mthaler.knittings;

import android.support.v4.app.Fragment;

/**
 * KnittingActivity displays a single knitting using KnittingFragment
 *
 * The activity is displayed when a new knitting is added or if a knitting is clicked
 * in the knittings list.
 *
 * The id of the knitting that should be displayed must be passed when the activity is started
 */
public class KnittingActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        // get the id of the knitting that should be displayed.
        final long id = getIntent().getLongExtra(KnittingFragment.EXTRA_KNITTING_ID, -1);
        // create a new knitting fragment that will be used by this activity
        return KnittingFragment.newInstance(id);
    }
}

package com.mthaler.knittings;

import android.support.v4.app.Fragment;

public class KnittingActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        final long knittingId = getIntent().getLongExtra(KnittingFragment.EXTRA_KNITTING_ID, -1);
        return KnittingFragment.newInstance(knittingId);
    }
}

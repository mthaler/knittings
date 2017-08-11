package com.mthaler.knittings;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.UUID;

public class KnittingPagerActivity extends AppCompatActivity {

    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewPager = new ViewPager(this);
        mViewPager.setId(R.id.viewPager);
        setContentView(mViewPager);

        final ArrayList<Knitting> knittings = KnittingsDataSource.getInstance(this).getAllKnittings();

        FragmentManager fm = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fm) {
            @Override
            public int getCount() {
                return knittings.size();
            }

            @Override
            public Fragment getItem(int pos) {
                long knittingId = knittings.get(pos).getId();
                return KnittingFragment.newInstance(knittingId);
            }
        });

        long knittingId = getIntent().getLongExtra(KnittingFragment.EXTRA_KNITTING_ID, -1);
        for (int i = 0; i < knittings.size(); i++) {
            if (knittings.get(i).getId() == knittingId) {
                mViewPager.setCurrentItem(i);
                break;
            }
        }
    }
}

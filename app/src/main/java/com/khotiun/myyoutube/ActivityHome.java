package com.khotiun.myyoutube;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

public class ActivityHome extends AppCompatActivity {

    private Drawer mDrawer = null;
    private Toolbar mToolbar;

    private String[] mChannelNames;
    private String[] mChannelId;
    private String[] mVideoTypes;

    private int mSelectedDrawerItem = 0;

    private FrameLayout mLayoutList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mLayoutList = (FrameLayout) findViewById(R.id.fragment_container);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mChannelNames = getResources().getStringArray(R.array.channel_names);
        mChannelId = getResources().getStringArray(R.array.channel_id);
        mVideoTypes = getResources().getStringArray(R.array.video_types);

        PrimaryDrawerItem[] primaryDrawerItems = new PrimaryDrawerItem[mChannelId.length];

        for (int i = 0; i < mChannelId.length; i++) {
            primaryDrawerItems[i] = new PrimaryDrawerItem()
                    .withName(mChannelNames[i])
                    .withIdentifier(i)
                    .withSelectable(false);
        }

        AccountHeader accountHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .build();

        mDrawer = new DrawerBuilder(this)
                .withActivity(ActivityHome.this)
                .withAccountHeader(accountHeader)
                .withDisplayBelowStatusBar(true)
                .withToolbar(mToolbar)
                .withActionBarDrawerToggleAnimated(true)
                .withSavedInstance(savedInstanceState)
                .addDrawerItems(primaryDrawerItems)
                .addStickyDrawerItems(
                        new SecondaryDrawerItem()
                        .withName(getString(R.string.about))
                        .withIdentifier(mChannelId.length - 1)
                        .withSelectable(false)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        return false;
                    }
                })
                .withSavedInstance(savedInstanceState)
                .withShowDrawerOnFirstLaunch(true)
                .build();
    }
}

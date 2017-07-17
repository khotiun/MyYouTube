package com.khotiun.myyoutube.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;

import com.khotiun.myyoutube.R;
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

    private int mSelectedDrawerItem = 0;//пункт меню по умолчанию

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
        //количество items в drawer
        PrimaryDrawerItem[] primaryDrawerItems = new PrimaryDrawerItem[mChannelId.length];
        //создание пункта меню для каждого канала или плэйлиста
        for (int i = 0; i < mChannelId.length; i++) {
            primaryDrawerItems[i] = new PrimaryDrawerItem()
                    .withName(mChannelNames[i])
                    .withIdentifier(i)
                    .withSelectable(false);
        }

        AccountHeader accountHeader = new AccountHeaderBuilder()//шапка панели навигации
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .build();

        mDrawer = new DrawerBuilder(this)
                .withActivity(ActivityHome.this)
                .withAccountHeader(accountHeader)
                .withDisplayBelowStatusBar(true)//панель навигации под статус баром
                .withToolbar(mToolbar)
                .withActionBarDrawerToggleAnimated(true)//анимация кнопки на тул баре
                .withSavedInstance(savedInstanceState)//сохранение состояния
                .addDrawerItems(primaryDrawerItems)//пункты меню
                .addStickyDrawerItems(
                        new SecondaryDrawerItem()//секцция для второстепенного меню
                        .withName(getString(R.string.about))
                        .withIdentifier(mChannelId.length - 1)//id
                        .withSelectable(false)//свойство выделение при нажатии сбрасываем
                )
                //слушатель для нажатия пунктов списка
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        return false;
                    }
                })
                .withSavedInstance(savedInstanceState)
                .withShowDrawerOnFirstLaunch(true)//показ навигации при первом запуске приложения
                .build();
    }
}

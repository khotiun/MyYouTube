package com.khotiun.myyoutube.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeApiServiceUtil;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.khotiun.myyoutube.R;
import com.khotiun.myyoutube.fragments.FragmentChannelVideo;
import com.khotiun.myyoutube.fragments.FragmentVideo;
import com.khotiun.myyoutube.utils.Utils;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public final class ActivityHome extends AppCompatActivity implements YouTubePlayer.OnFullscreenListener,
        FragmentChannelVideo.OnVideoSelectedListener{
    //отступ между списками в альбомной ориентации
    private static final int LANDSCAPE_VIDEO_PADDING_DP = 5;
    //код запроса, для востановления ошибки после API
    private static final int RECOVERY_DIALOG_REQUEST = 1;
    private FragmentVideo fragmentVideo;
    //переменная для обработки полноэкранного состояния
    private boolean isFullscreen;

    private Drawer drawer = null;
    private Toolbar toolbar;
    //представляет внешний вид окна активити со всем его оформлением и содержимым
    private View decorView;

    private String[] channelNames;
    private String[] channelId;
    private String[] videoTypes;

    private int selectedDrawerItem = 0;//пункт меню по умолчанию

    private FrameLayout layoutList;

    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        layoutList = (FrameLayout) findViewById(R.id.fragment_container);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        decorView = getWindow().getDecorView();
        fragmentVideo = (FragmentVideo) getFragmentManager().findFragmentById(R.id.video_fragment_container);

        channelNames = getResources().getStringArray(R.array.channel_names);
        channelId = getResources().getStringArray(R.array.channel_id);
        videoTypes = getResources().getStringArray(R.array.video_types);
        //нужен для проверки подключения юьуб апи в случае не удачи оповещает всплывающим сообщением
        checkYouTubeApi();
        //количество items в drawer
        PrimaryDrawerItem[] primaryDrawerItems = new PrimaryDrawerItem[channelId.length];
        //создание пункта меню для каждого канала или плэйлиста
        for (int i = 0; i < channelId.length; i++) {
            primaryDrawerItems[i] = new PrimaryDrawerItem()
                    .withName(channelNames[i])
                    .withIdentifier(i)
                    .withSelectable(false);
        }

        AccountHeader accountHeader = new AccountHeaderBuilder()//шапка панели навигации
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .build();

        drawer = new DrawerBuilder(this)
                .withActivity(ActivityHome.this)
                .withAccountHeader(accountHeader)
                .withDisplayBelowStatusBar(true)//панель навигации под статус баром
                .withToolbar(toolbar)
                .withActionBarDrawerToggleAnimated(true)//анимация кнопки на тул баре
                .withSavedInstance(savedInstanceState)//сохранение состояния
                .addDrawerItems(primaryDrawerItems)//пункты меню
                .addStickyDrawerItems(
                        new SecondaryDrawerItem()//секцция для второстепенного меню
                                .withName(getString(R.string.about))
                                .withIdentifier(channelId.length - 1)//id
                                .withSelectable(false)//свойство выделение при нажатии сбрасываем
                )
                //слушатель для нажатия пунктов списка
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                        selectedDrawerItem = position;
                        if (drawerItem != null) {
                            if (drawerItem.getIdentifier() >= 0 && selectedDrawerItem != -1) {

                                setToolbarAndSelectedDrawerItem(
                                        channelNames[selectedDrawerItem-1],
                                        (selectedDrawerItem-1)
                                );
                                //действия при нажатии на айтем
                                Bundle bundle = new Bundle();
                                //ложим в бандл тип видео
                                bundle.putString(Utils.TAG_VIDEO_TYPE,
                                        videoTypes[selectedDrawerItem-1]);
                                //ложим в бандл id видео
                                bundle.putString(Utils.TAG_CHANNEL_ID,
                                        channelId[selectedDrawerItem-1]);

                                fragment = new FragmentChannelVideo();
                                fragment.setArguments(bundle);

                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_container, fragment)
                                        .commit();
                            //вызов активити about
                            } else if (selectedDrawerItem == -1) {
                                Intent aboutIntent = new Intent(getApplicationContext(),
                                        ActivityAbout.class);
                                startActivity(aboutIntent);
                                overridePendingTransition(R.anim.open_next, R.anim.close_main);
                            }
                        }
                        return false;
                    }
                })
                .withSavedInstance(savedInstanceState)
                .withShowDrawerOnFirstLaunch(true)//показ навигации при первом запуске приложения
                .build();

        setToolbarAndSelectedDrawerItem(channelNames[0], 0);

        //при старте ативити сразу отображается видео и список
        Bundle bundle = new Bundle();
        bundle.putString(Utils.TAG_VIDEO_TYPE,
                videoTypes[selectedDrawerItem]);
        bundle.putString(Utils.TAG_CHANNEL_ID,
                channelId[selectedDrawerItem]);

        fragment = new FragmentChannelVideo();
        fragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {

            @Override
            public void onBackStackChanged() {
                Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if (f != null) {
                    updateTitleAndDrawer(f);
                }

            }
        });

        if (savedInstanceState == null) {
            drawer.setSelection(0, false);
        }


    }

    //нужен для проверки подключения юьуб апи в случае не удачи оповещает всплывающим сообщением
    private void checkYouTubeApi() {
        YouTubeInitializationResult errorReason =
                YouTubeApiServiceUtil.isYouTubeApiServiceAvailable(this);
        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, RECOVERY_DIALOG_REQUEST).show();
        } else if (errorReason != YouTubeInitializationResult.SUCCESS) {
            String errorMessage =
                    String.format(getString(R.string.error_player),
                            errorReason.toString());
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }
    //вызывается при смене фрагментов
    private void setToolbarAndSelectedDrawerItem(String title, int selectedDrawerItem){
        //устанавливает заголовок окна
        toolbar.setTitle(title);
        //устанавливает выбранный тип меню
        drawer.setSelection(selectedDrawerItem, false);
    }
    //обновляет выбранный заголовок меню и выбранный пункт меню
    private void updateTitleAndDrawer (Fragment mFragment){
        String fragClassName = mFragment.getClass().getName();

        if (fragClassName.equals(FragmentChannelVideo.class.getName())){
            setToolbarAndSelectedDrawerItem(channelNames[selectedDrawerItem ],
                    (selectedDrawerItem ));
        }
    }
    //создает меню доступное через 3 точки в тулбаре
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_home, menu);
        return true;
    }
    //обрабатывает нажатие пунктов меню
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuAbout:
                Intent aboutIntent = new Intent(getApplicationContext(),
                        ActivityAbout.class);
                startActivity(aboutIntent);
                overridePendingTransition(R.anim.open_next, R.anim.close_main);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //восстанавливает активити если пользователь нажал кнопку повтора попытки подключения
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECOVERY_DIALOG_REQUEST) {
            recreate();
        }
    }
    //отслеживает такие события как поворот экрана
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //происходит программная настройка макета для 3-х различных состояний
        layout();
    }

    @Override
    public void onFullscreen(boolean isFullscreen) {
        this.isFullscreen = isFullscreen;
        //происходит программная настройка макета для 3-х различных состояний
        layout();
    }

    //происходит программная настройка макета для 3-х различных состояний
    private void layout() {
        boolean isPortrait =
                getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        //осуществляется быстрый переход между состояниями поэтому xml ресурсы не перезагружаем
        if (isFullscreen) {

            toolbar.setVisibility(View.GONE);
            layoutList.setVisibility(View.GONE);
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            setLayoutSize(fragmentVideo.getView(), MATCH_PARENT, MATCH_PARENT);


        } else if (isPortrait) {

            toolbar.setVisibility(View.VISIBLE);
            layoutList.setVisibility(View.VISIBLE);
            setLayoutSize(fragmentVideo.getView(), WRAP_CONTENT, WRAP_CONTENT);


        } else {

            toolbar.setVisibility(View.VISIBLE);
            layoutList.setVisibility(View.VISIBLE);
            int screenWidth = dpToPx(getResources().getConfiguration().screenWidthDp);
            int videoWidth = screenWidth - screenWidth / 4 - dpToPx(LANDSCAPE_VIDEO_PADDING_DP);
            setLayoutSize(fragmentVideo.getView(), videoWidth, WRAP_CONTENT);
        }
    }
    //создает фрагмент для отображения видео и передает ему id видео
    @Override
    public void onVideoSelected(String ID) {
        FragmentVideo fragmentVideo =
                (FragmentVideo) getFragmentManager().findFragmentById(R.id.video_fragment_container);
        fragmentVideo.setVideoId(ID);
    }
    //метод для преобразования dp в px
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }
    //метод установки размера макета
    private static void setLayoutSize(View view, int width, int height) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = width;
        params.height = height;
        view.setLayoutParams(params);
    }

    @Override
    public void onBackPressed() {
        //если полноэкранный режим возвращает в обычный
        if (isFullscreen){
            fragmentVideo.backnormal();
        } else{
            super.onBackPressed();
        }
    }
}

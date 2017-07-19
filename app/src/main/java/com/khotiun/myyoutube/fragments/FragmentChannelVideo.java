package com.khotiun.myyoutube.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.khotiun.myyoutube.R;
import com.khotiun.myyoutube.adapter.AdapterList;
import com.khotiun.myyoutube.utils.MySingleton;
import com.khotiun.myyoutube.utils.Utils;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.marshalchen.ultimaterecyclerview.ItemTouchListenerAdapter;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hotun on 18.07.2017.
 */
//будет отображать список выбранного канала или плейлиста
public class FragmentChannelVideo extends Fragment implements View.OnClickListener {

    private static final String TAG = FragmentChannelVideo.class.getSimpleName();
    private static final String TAGS = "URL";

    private TextView mLblNoResult;
    private LinearLayout mLytRetry;
    private CircleProgressBar mPrgLoading;
    private UltimateRecyclerView mUltimateRecyclerView;


    private int mVideoType;
    private String mChannelId;


    private OnVideoSelectedListener mCallback;//слушатель возвращает выбранное видео в списке


    private AdapterList mAdapterList = null;

    private ArrayList<HashMap<String, String>> mTempVideoData = new ArrayList<>();//для хранения видеоданных перед получением продолжительности видео
    private ArrayList<HashMap<String, String>> mVideoData = new ArrayList<>();//для хранения окончательных видео данных

    private String mNextPageToken = "";
    private String mVideoIds = "";
    private String mDuration = "00:00";

    private boolean mIsStillLoading = true;

    private boolean mIsAppFirstLaunched = true;//параметр первого запуска

    private boolean mIsFirstVideo = true;//переменная для проверки первого видео

    //будет вызываться когда пользователь выбирает видео из списка
    public interface OnVideoSelectedListener {//реализован в ActivityHome

        public void onVideoSelected(String ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_list, container, false);//создаем view и заполняем макет
        setHasOptionsMenu(true);
        Bundle bundle = this.getArguments();//получаем данные от ActivityHime

        mVideoType = Integer.parseInt(bundle.getString(Utils.TAG_VIDEO_TYPE));
        mChannelId = bundle.getString(Utils.TAG_CHANNEL_ID);

        mUltimateRecyclerView = (UltimateRecyclerView)
                view.findViewById(R.id.ultimate_recycler_view);
        mLblNoResult = (TextView) view.findViewById(R.id.lblNoResult);
        mLytRetry = (LinearLayout) view.findViewById(R.id.lytRetry);
        mPrgLoading = (CircleProgressBar) view.findViewById(R.id.prgLoading);
        AppCompatButton btnRetry = (AppCompatButton) view.findViewById(R.id.raisedRetry);

        btnRetry.setOnClickListener(this);//установка слушателя повтор подключения при отсутствии сети
        mPrgLoading.setColorSchemeResources(R.color.accent_color);//задаем цвет прогресс бара
        mPrgLoading.setVisibility(View.VISIBLE);//видимость прогресс бара

        mIsAppFirstLaunched = true;//переменная первого запуска
        mIsFirstVideo = true;


        mVideoData = new ArrayList<>();//устанавливаем массив видео данных

        mAdapterList = new AdapterList(getActivity(), mVideoData);//определяем адаптер для UltimateRecyclerView
        mUltimateRecyclerView.setAdapter(mAdapterList);
        mUltimateRecyclerView.setHasFixedSize(false);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mUltimateRecyclerView.setLayoutManager(linearLayoutManager);
        mUltimateRecyclerView.enableLoadmore();

        mAdapterList.setCustomLoadMoreView(LayoutInflater.from(getActivity())//устанавливаем макет для кастомного вращающегося индикатора загрузки
                .inflate(R.layout.progressbar, null));
        //слушатель для дополнительной загрузки видео в список
        mUltimateRecyclerView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
            @Override
            public void loadMore(int itemsCount, final int maxLastVisiblePosition) {
                if (mIsStillLoading) {//проверка есть ли еще данные на сервере
                    mIsStillLoading = false;
                    //индикатор загрузки
                    mAdapterList.setCustomLoadMoreView(LayoutInflater.from(getActivity())
                            .inflate(R.layout.progressbar, null));

                    Handler handler = new Handler();//создает обьект для получения видео данных в фоне
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            getVideoData();

                        }
                    }, 1000);
                } else {
                    disableLoadmore();
                }

            }
        });


        ItemTouchListenerAdapter itemTouchListenerAdapter =
                new ItemTouchListenerAdapter(mUltimateRecyclerView.mRecyclerView,
                        new ItemTouchListenerAdapter.RecyclerViewOnItemClickListener() {
                            //слушатель для нажатого элемента в списке
                            @Override
                            public void onItemClick(RecyclerView parent, View clickedView, int position) {

                                if (position < mVideoData.size()) {
                                    //передаем данные выбранного видео в ActivityHome
                                    mCallback.onVideoSelected(mVideoData.get(position).get(Utils.KEY_VIDEO_ID));
                                }
                            }

                            //длительное нажатие на item
                            @Override
                            public void onItemLongClick(RecyclerView recyclerView, View view, int i) {
                            }
                        });
        //слушатель нажатия
        mUltimateRecyclerView.mRecyclerView.addOnItemTouchListener(itemTouchListenerAdapter);
        //получение данных с сервера при первом создании фрагмента
        getVideoData();

        return view;
    }

    //метод гарантирует что вызов обратного метода сработал, если нет создается исключение
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);


        try {
            mCallback = (OnVideoSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnVideoSelectedListener");
        }
    }
    //метод для получения данных канала или плей листа ютуб
    private void getVideoData() {
        mVideoIds = "";
        final String[] videoId = new String[1];//создаем массив для хранения видео id канала

        String url;//переменная для хранения url запроса к youtube api
        if (mVideoType == 2) {
            //формируем запрос для получения плейлиста
            url = Utils.API_YOUTUBE + Utils.FUNCTION_PLAYLIST_ITEMS_YOUTUBE +
                    Utils.PARAM_PART_YOUTUBE + "snippet,id&" +//тип видео
                    Utils.PARAM_FIELD_PLAYLIST_YOUTUBE + "&" +//проверяем это канал или плэлист
                    Utils.PARAM_KEY_YOUTUBE + getResources().getString(R.string.youtube_apikey) + "&" +
                    Utils.PARAM_PLAYLIST_ID_YOUTUBE + mChannelId + "&" +
                    Utils.PARAM_PAGE_TOKEN_YOUTUBE + mNextPageToken + "&" +
                    Utils.PARAM_MAX_RESULT_YOUTUBE + Utils.PARAM_RESULT_PER_PAGE;
        } else {
            //для получения запроса канала
            url = Utils.API_YOUTUBE + Utils.FUNCTION_SEARCH_YOUTUBE +
                    Utils.PARAM_PART_YOUTUBE + "snippet,id&" + Utils.PARAM_ORDER_YOUTUBE + "&" +
                    Utils.PARAM_TYPE_YOUTUBE + "&" +
                    Utils.PARAM_FIELD_SEARCH_YOUTUBE + "&" +
                    Utils.PARAM_KEY_YOUTUBE + getResources().getString(R.string.youtube_apikey) + "&" +
                    Utils.PARAM_CHANNEL_ID_YOUTUBE + mChannelId + "&" +
                    Utils.PARAM_PAGE_TOKEN_YOUTUBE + mNextPageToken + "&" +
                    Utils.PARAM_MAX_RESULT_YOUTUBE + Utils.PARAM_RESULT_PER_PAGE;
        }

        Log.d(TAGS, url);
        //ответ с сервера в json формате
        //получаем имя канала или плей листа
        JsonObjectRequest request = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    JSONArray dataItemArray;
                    JSONObject itemIdObject, itemSnippetObject, itemSnippetThumbnailsObject,
                            itemSnippetResourceIdObject;

                    @Override
                    public void onResponse(JSONObject response) {
                        //проверяем что активити все еще находится на переднем плане
                        Activity activity = getActivity();
                        if (activity != null && isAdded()) {
                            try {
                                //получаем все элементы JSONArray с сервера
                                dataItemArray = response.getJSONArray(Utils.ARRAY_ITEMS);

                                if (dataItemArray.length() > 0) {
                                    //haveResultView(); - отображает ресайкл вью и скрывает другие представления
                                    haveResultView();
                                    for (int i = 0; i < dataItemArray.length(); i++) {
                                        HashMap<String, String> dataMap = new HashMap<>();
                                        //разделяем массив на айтемы
                                        JSONObject itemsObject = dataItemArray.getJSONObject(i);
                                        //для получения заголовка и превью
                                        itemSnippetObject = itemsObject.
                                                getJSONObject(Utils.OBJECT_ITEMS_SNIPPET);

                                        if (mVideoType == 2) {
                                            //получаем id видео в плейлисте
                                            itemSnippetResourceIdObject = itemSnippetObject.
                                                    getJSONObject(Utils.OBJECT_ITEMS_SNIPPET_RESOURCEID);
                                            dataMap.put(Utils.KEY_VIDEO_ID,
                                                    itemSnippetResourceIdObject.
                                                            getString(Utils.KEY_VIDEO_ID));
                                            videoId[0] = itemSnippetResourceIdObject.
                                                    getString(Utils.KEY_VIDEO_ID);
                                            //обьединяем все видео идентификаторы и используем его как параметр для получения длительности всех видео
                                            mVideoIds = mVideoIds + itemSnippetResourceIdObject.
                                                    getString(Utils.KEY_VIDEO_ID) + ",";
                                        } else {
                                            //получаем id видео в канале
                                            itemIdObject = itemsObject.
                                                    getJSONObject(Utils.OBJECT_ITEMS_ID);
                                            dataMap.put(Utils.KEY_VIDEO_ID,
                                                    itemIdObject.getString(Utils.KEY_VIDEO_ID));
                                            videoId[0] = itemIdObject.getString(Utils.KEY_VIDEO_ID);
                                            //обьединяем все видео идентификаторы и используем его как параметр для получения длительности всех видео
                                            mVideoIds = mVideoIds + itemIdObject.
                                                    getString(Utils.KEY_VIDEO_ID) + ",";
                                        }
                                        //когда фрагмент создается отображаем первое видео в видео плеере
                                        if (mIsFirstVideo && i == 0) {
                                            mIsFirstVideo = false;
                                            mCallback.onVideoSelected(videoId[0]);
                                        }
                                        //получаем заголовок видео
                                        dataMap.put(Utils.KEY_TITLE,
                                                itemSnippetObject.getString(Utils.KEY_TITLE));
                                        //получаем дату публикации
                                        String formattedPublishedDate = Utils.formatPublishedDate(
                                                getActivity(),
                                                itemSnippetObject.getString(Utils.KEY_PUBLISHEDAT));

                                        dataMap.put(Utils.KEY_PUBLISHEDAT, formattedPublishedDate);
                                        //получаем превью видео
                                        itemSnippetThumbnailsObject = itemSnippetObject.
                                                getJSONObject(Utils.OBJECT_ITEMS_SNIPPET_THUMBNAILS);
                                        itemSnippetThumbnailsObject = itemSnippetThumbnailsObject.
                                                getJSONObject
                                                        (Utils.OBJECT_ITEMS_SNIPPET_THUMBNAILS_MEDIUM);
                                        dataMap.put(Utils.KEY_URL_THUMBNAILS,
                                                itemSnippetThumbnailsObject.getString
                                                        (Utils.KEY_URL_THUMBNAILS));
                                        //сохраняем видео данные времменно для того что бы получить продолжительность видео
                                        mTempVideoData.add(dataMap);
                                    }
                                    //получаем продолжительность видео
                                    getDuration();
                                    //проверка есть ли еще данные на сервере
                                    if (dataItemArray.length() == Utils.PARAM_RESULT_PER_PAGE) {
                                        mNextPageToken = response.getString(Utils.ARRAY_PAGE_TOKEN);

                                    } else {
                                        //если данных больше нет то очищаем строку
                                        mNextPageToken = "";
                                        disableLoadmore();
                                    }

                                    mIsAppFirstLaunched = false;

                                } else {
                                    //если данные получены это значит что дальнейший запрос будет не первым
                                    //или данные от сервера уже загружены или нет данных на сервере
                                    if (mIsAppFirstLaunched &&
                                            mAdapterList.getAdapterItemCount() <= 0) {
                                        //noResultView() - показывает вью нет данных и скрывает другие вью
                                        noResultView();
                                    }
                                    disableLoadmore();
                                }

                            } catch (JSONException e) {
                                Log.d(Utils.TAG_FANDROID + TAG, "JSON Parsing error: " +
                                        e.getMessage());
                                mPrgLoading.setVisibility(View.GONE);
                            }
                            mPrgLoading.setVisibility(View.GONE);
                        }
                    }
                },
                //метод обработки ошибки ответа
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //активность все еще на переднем плане
                        Activity activity = getActivity();
                        //проверяем присутствует ли интернет подключение
                        if (activity != null && isAdded()) {
                            Log.d(Utils.TAG_FANDROID + TAG, "on Error Response: " + error.getMessage());
                            try {
                                //активируем оповещения через снэк бар
                                String msgSnackBar;
                                if (error instanceof NoConnectionError) {
                                    msgSnackBar = getResources().getString(R.string.no_internet_connection);
                                } else {
                                    msgSnackBar = getResources().getString(R.string.response_error);
                                }

                                if (mVideoData.size() == 0) {
                                    //retryView() - скрывает другие вью и отображает макет повтора попытки подключения
                                    retryView();

                                } else {
                                    mAdapterList.setCustomLoadMoreView(null);
                                    mAdapterList.notifyDataSetChanged();
                                }

                                Utils.showSnackBar(getActivity(), msgSnackBar);
                                mPrgLoading.setVisibility(View.GONE);

                            } catch (Exception e) {
                                Log.d(Utils.TAG_FANDROID + TAG, "failed catch volley " + e.toString());
                                mPrgLoading.setVisibility(View.GONE);
                            }
                        }
                    }
                }
        );
        request.setRetryPolicy(new DefaultRetryPolicy(Utils.ARG_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(getActivity()).getRequestQueue().add(request);

    }
    ///метод для получения продолжительности видео
    private void getDuration() {
        //Utils.API_YOUTUBE - для получения продолжительности видео
        String url = Utils.API_YOUTUBE + Utils.FUNCTION_VIDEO_YOUTUBE +
                Utils.PARAM_PART_YOUTUBE + "contentDetails&" +
                Utils.PARAM_FIELD_VIDEO_YOUTUBE + "&" +
                Utils.PARAM_KEY_YOUTUBE + getResources().getString(R.string.youtube_apikey) + "&" +
                Utils.PARAM_VIDEO_ID_YOUTUBE + mVideoIds;

        JsonObjectRequest request = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    JSONArray dataItemArrays;
                    JSONObject itemContentObject;

                    @Override
                    public void onResponse(JSONObject response) {
                        Activity activity = getActivity();
                        if (activity != null && isAdded()) {
                            try {
                                //haveResultView() - метод для отображения ресайкл вью и скрытия других вью
                                haveResultView();
                                dataItemArrays = response.getJSONArray(Utils.ARRAY_ITEMS);
                                if (dataItemArrays.length() > 0 && !mTempVideoData.isEmpty()) {
                                    for (int i = 0; i < dataItemArrays.length(); i++) {
                                        HashMap<String, String> dataMap = new HashMap<>();
                                        //разделение массива на айтомы
                                        JSONObject itemsObjects = dataItemArrays.getJSONObject(i);
                                        //элемент для получения продолжительности
                                        itemContentObject = itemsObjects.
                                                getJSONObject(Utils.OBJECT_ITEMS_CONTENT_DETAIL);
                                        mDuration = itemContentObject.
                                                getString(Utils.KEY_DURATION);
                                        //преобразовываем строку времени в iso8601
                                        String mDurationInTimeFormat = Utils.
                                                getTimeFromString(mDuration);
                                        //оохраняем заголовки, идентификаторы видео
                                        dataMap.put(Utils.KEY_DURATION, mDurationInTimeFormat);
                                        dataMap.put(Utils.KEY_URL_THUMBNAILS,
                                                mTempVideoData.get(i).get(Utils.KEY_URL_THUMBNAILS));
                                        dataMap.put(Utils.KEY_TITLE,
                                                mTempVideoData.get(i).get(Utils.KEY_TITLE));
                                        dataMap.put(Utils.KEY_VIDEO_ID,
                                                mTempVideoData.get(i).get(Utils.KEY_VIDEO_ID));
                                        dataMap.put(Utils.KEY_PUBLISHEDAT,
                                                mTempVideoData.get(i).get(Utils.KEY_PUBLISHEDAT));
                                        //для хранения видео данных
                                        mVideoData.add(dataMap);
                                        //вставляем данные в адаптер
                                        mAdapterList.notifyItemInserted(mVideoData.size());

                                    }
                                    mIsStillLoading = true;
                                    //очищаем mTempVideoData - после того как выполнили вставку данных
                                    mTempVideoData.clear();
                                    mTempVideoData = new ArrayList<>();
                                    //проверяем что данные с сервера загружены или нет данных
                                } else {
                                    if (mIsAppFirstLaunched && mAdapterList.getAdapterItemCount() <= 0) {
                                        //показывает вью нет данных и скрывает другие вью
                                        noResultView();
                                    }
                                    disableLoadmore();
                                }

                            } catch (JSONException e) {
                                Log.d(Utils.TAG_FANDROID + TAG,
                                        "JSON Parsing error: " + e.getMessage());
                                mPrgLoading.setVisibility(View.GONE);
                            }
                            mPrgLoading.setVisibility(View.GONE);
                        }
                    }
                },
                //проверяем ошибки ответа
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Activity activity = getActivity();
                        if (activity != null && isAdded()) {
                            Log.d(Utils.TAG_FANDROID + TAG, "on Error Response: " + error.getMessage());
                            try {
                                String msgSnackBar;
                                if (error instanceof NoConnectionError) {
                                    msgSnackBar = getResources().getString(R.string.no_internet_connection);
                                } else {
                                    msgSnackBar = getResources().getString(R.string.response_error);
                                }

                                if (mVideoData.size() == 0) {
                                    retryView();
                                }

                                Utils.showSnackBar(getActivity(), msgSnackBar);
                                mPrgLoading.setVisibility(View.GONE);

                            } catch (Exception e) {
                                Log.d(Utils.TAG_FANDROID + TAG, "failed catch volley " + e.toString());
                                mPrgLoading.setVisibility(View.GONE);
                            }
                        }
                    }
                }
        );
        //устанавливаем параметры повторы для запроса и добавляем запрос в очередь запросов
        request.setRetryPolicy(new DefaultRetryPolicy(Utils.ARG_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(getActivity()).getRequestQueue().add(request);
    }
    //метод скрывает другие вью и отображение макета повтора
    private void retryView() {
        mLytRetry.setVisibility(View.VISIBLE);
        mUltimateRecyclerView.setVisibility(View.GONE);
        mLblNoResult.setVisibility(View.GONE);
    }
    //метод для отображени ресайкл вью
    private void haveResultView() {
        mLytRetry.setVisibility(View.GONE);
        mUltimateRecyclerView.setVisibility(View.VISIBLE);
        mLblNoResult.setVisibility(View.GONE);
    }
    //метод для отображения вью отутствия результатов
    private void noResultView() {
        mLytRetry.setVisibility(View.GONE);
        mUltimateRecyclerView.setVisibility(View.GONE);
        mLblNoResult.setVisibility(View.VISIBLE);

    }
    //метод отключения прогресс бара подгрузки видео
    private void disableLoadmore() {
        mIsStillLoading = false;
        if (mUltimateRecyclerView.isLoadMoreEnabled()) {
            mUltimateRecyclerView.disableLoadmore();
        }
        mAdapterList.notifyDataSetChanged();
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onResume() {
        super.onResume();


    }
    //обработчик попытки повтора подключения, делаем видимым прогрессбар,
    // вызываем метод для отображения ресайкл вью и скрываем другие вью,
    // а так же пытаемся получить данные видео
    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.raisedRetry:
                mPrgLoading.setVisibility(View.VISIBLE);
                haveResultView();
                getVideoData();
                break;
            default:
                break;
        }
    }
}

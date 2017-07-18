package com.khotiun.myyoutube.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.github.mrengineer13.snackbar.SnackBar;
import com.google.android.gms.ads.AdView;
import com.khotiun.myyoutube.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by hotun on 17.07.2017.
 */
//класс используется библиотекой volley для построения сетевых запросов
public class Utils {
    public static final String API_YOUTUBE  = "https://www.googleapis.com/youtube/v3/";

    public static final String FUNCTION_SEARCH_YOUTUBE  = "search?";
    public static final String FUNCTION_VIDEO_YOUTUBE   = "videos?";
    public static final String FUNCTION_PLAYLIST_ITEMS_YOUTUBE   = "playlistItems?";

    public static final String PARAM_KEY_YOUTUBE   	    = "key=";
    public static final String PARAM_CHANNEL_ID_YOUTUBE = "channelId=";
    public static final String PARAM_PLAYLIST_ID_YOUTUBE = "playlistId=";
    public static final String PARAM_VIDEO_ID_YOUTUBE   = "id=";
    public static final String PARAM_PART_YOUTUBE   	= "part=";
    public static final String PARAM_PAGE_TOKEN_YOUTUBE = "pageToken=";
    public static final String PARAM_ORDER_YOUTUBE   	= "order=date";
    public static final String PARAM_MAX_RESULT_YOUTUBE = "maxResults=";
    public static final String PARAM_TYPE_YOUTUBE   	= "type=video";
    public static final String PARAM_FIELD_SEARCH_YOUTUBE = "fields=nextPageToken," +
            "pageInfo(totalResults),items(id(videoId),snippet(title,thumbnails,publishedAt))";
    public static final String PARAM_FIELD_VIDEO_YOUTUBE  = "fields=pageInfo(totalResults)," +
            "items(contentDetails(duration))&";
    public static final String PARAM_FIELD_PLAYLIST_YOUTUBE = "fields=nextPageToken," +
            "pageInfo(totalResults),items(snippet(title,thumbnails,publishedAt,resourceId(videoId)))";
    public static final int PARAM_RESULT_PER_PAGE   	  = 8;

    public static final String ARRAY_PAGE_TOKEN  = "nextPageToken";
    public static final String ARRAY_ITEMS       = "items";

    public static final String OBJECT_ITEMS_ID                        = "id";
    public static final String OBJECT_ITEMS_CONTENT_DETAIL            = "contentDetails";
    public static final String OBJECT_ITEMS_SNIPPET                   = "snippet";
    public static final String OBJECT_ITEMS_SNIPPET_THUMBNAILS        = "thumbnails";
    public static final String OBJECT_ITEMS_SNIPPET_RESOURCEID        = "resourceId";
    public static final String OBJECT_ITEMS_SNIPPET_THUMBNAILS_MEDIUM = "medium";

    public static final String KEY_VIDEO_ID        = "videoId";
    public static final String KEY_TITLE           = "title";
    public static final String KEY_PUBLISHEDAT     = "publishedAt";
    public static final String KEY_URL_THUMBNAILS  = "url";
    public static final String KEY_DURATION        = "duration";

    public static final Integer ARG_TIMEOUT_MS  = 4000;

    public static final String TAG_FANDROID = "Fandroid:";

    public static final String TAG_CHANNEL_ID  = "channel_id";
    public static final String TAG_VIDEO_TYPE  = "video_type";


    // метод для проверки видимости банера
    public static boolean admobVisibility(AdView ad, boolean isInDebugMode){
        if(isInDebugMode) {
            ad.setVisibility(View.VISIBLE);
            return true;
        }else {
            ad.setVisibility(View.GONE);
            return false;
        }
    }

    //метод отображения SnackBar, будет оповещать о проблемах с интернетом
    public static void showSnackBar(Activity activity, String message){
        new SnackBar.Builder(activity)
                .withMessage(message)
                .show();
    }
    //метод для преобразования даты в строку
    public static String formatPublishedDate(Activity activity, String publishedDate){
        Date result = new Date();
        DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            result = df1.parse(publishedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return getTimeAgo(result, activity);
    }
    //метод для получения текущей даты
    public static Date currentDate() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTime();
    }
    //метод для отображения строки, показывающей сколько времени прошло с момента создания видео
    public static String getTimeAgo(Date date, Context ctx) {

        if(date == null) {
            return null;
        }

        long time = date.getTime();

        Date curDate = currentDate();
        long now = curDate.getTime();
        if (time > now || time <= 0) {
            return null;
        }

        int dim = getTimeDistanceInMinutes(time);

        String timeAgo = null;

        if (dim == 0) {
            timeAgo = ctx.getResources().getString(R.string.date_util_term_less) + " " +
                    ctx.getResources().getString(R.string.date_util_unit_minute);
        } else if (
                dim == 1 ||
                        dim == 21 ||
                        dim == 31 ||
                        dim == 41
                ) {
            timeAgo = dim + " " + ctx.getResources().getString(R.string.date_util_unit_minuta);
        } else if (
                dim == 2 || dim == 3 || dim == 4 ||
                        dim == 22 || dim == 23 || dim == 24 ||
                        dim == 32 || dim == 33 || dim == 34 ||
                        dim == 42 || dim == 43 || dim == 44
                ) {
            timeAgo = dim + " " + ctx.getResources().getString(R.string.date_util_unit_minute);

        } else if (
                dim >= 5 && dim <= 20 ||
                        dim >= 25 && dim <= 30 ||
                        dim >= 35 && dim <= 40
                ) {
            timeAgo = dim + " " + ctx.getResources().getString(R.string.date_util_unit_minutes);
        } else if (dim >= 45 && dim <= 89) {
            timeAgo = ctx.getResources().getString(R.string.date_util_prefix_about) + " " +
                    ctx.getResources().getString(R.string.date_util_unit_hour);

        } else if (dim >= 90 && dim <= 270) {
            timeAgo = ctx.getResources().getString(R.string.date_util_prefix_about) + " " +
                    (Math.round(dim / 60)) + " " +
                    ctx.getResources().getString(R.string.date_util_unit_hour);
        } else if (dim >= 271 && dim <= 1439) {
            timeAgo = ctx.getResources().getString(R.string.date_util_prefix_about) + " " +
                    (Math.round(dim / 60)) + " " +
                    ctx.getResources().getString(R.string.date_util_unit_hours);
        } else if (dim >= 1440 && dim <= 2519) {
            timeAgo = "1 " + ctx.getResources().getString(R.string.date_util_unit_daya);
        } else if (dim >= 2520 && dim <= 6480) {
            timeAgo = (Math.round(dim / 1440)) + " " +
                    ctx.getResources().getString(R.string.date_util_unit_day);
        } else if (dim >= 6481 && dim <= 29000 || dim >= 34701 && dim <= 43200) {
            timeAgo = (Math.round(dim / 1440)) + " " +
                    ctx.getResources().getString(R.string.date_util_unit_days);
        } else if (dim >= 29001 && dim <= 30500) {
            timeAgo = (Math.round(dim / 1440)) + " " +
                    ctx.getResources().getString(R.string.date_util_unit_daya);
        } else if (dim >= 30501 && dim <= 34700) {
            timeAgo = (Math.round(dim / 1440)) + " " +
                    ctx.getResources().getString(R.string.date_util_unit_day);
        } else if (dim >= 43201 && dim <= 86399) {
            timeAgo = ctx.getResources().getString(R.string.date_util_prefix_about) + " "+
                    ctx.getResources().getString(R.string.date_util_unit_month);
        } else if (dim >= 86400 && dim <= 216000) {
            timeAgo = (Math.round(dim / 43200)) + " " +
                    ctx.getResources().getString(R.string.date_util_unit_month);
        } else if (dim >= 216001 && dim <= 492480) {
            timeAgo = (Math.round(dim / 43200)) + " " +
                    ctx.getResources().getString(R.string.date_util_unit_months);
        } else if (dim >= 492481 && dim <= 518400) {
            timeAgo = ctx.getResources().getString(R.string.date_util_prefix_about) + " "+
                    ctx.getResources().getString(R.string.date_util_unit_year);
        } else if (dim >= 518401 && dim <= 914399) {
            timeAgo = ctx.getResources().getString(R.string.date_util_prefix_over) + " "+
                    ctx.getResources().getString(R.string.date_util_unit_year);
        } else if (dim >= 914400 && dim <= 1051199) {
            timeAgo = ctx.getResources().getString(R.string.date_util_prefix_almost) + " 2 " +
                    ctx.getResources().getString(R.string.date_util_unit_years);
        } else {
            timeAgo = ctx.getResources().getString(R.string.date_util_prefix_about) + " " +
                    (Math.round(dim / 525600)) + " " +
                    ctx.getResources().getString(R.string.date_util_unit_years);
        }

        return timeAgo + " " + ctx.getResources().getString(R.string.date_util_suffix);
    }
    //метод получающий время с момента публикации в минутах
    private static int getTimeDistanceInMinutes(long time) {
        long timeDistance = currentDate().getTime() - time;
        return Math.round((Math.abs(timeDistance) / 1000) / 60);
    }
    //конвертирует время из строки
    public static String getTimeFromString(String duration) {
        String time = "";
        boolean hourexists = false, minutesexists = false, secondsexists = false;
        if (duration.contains("H"))
            hourexists = true;
        if (duration.contains("M"))
            minutesexists = true;
        if (duration.contains("S"))
            secondsexists = true;
        if (hourexists) {
            String hour;
            hour = duration.substring(duration.indexOf("T") + 1,
                    duration.indexOf("H"));
            if (hour.length() == 1)
                hour = "0" + hour;
            time += hour + ":";
        }
        if (minutesexists) {
            String minutes;
            if (hourexists)
                minutes = duration.substring(duration.indexOf("H") + 1,
                        duration.indexOf("M"));
            else
                minutes = duration.substring(duration.indexOf("T") + 1,
                        duration.indexOf("M"));
            if (minutes.length() == 1)
                minutes = "0" + minutes;
            time += minutes + ":";
        } else {
            time += "00:";
        }
        if (secondsexists) {
            String seconds;
            if (hourexists) {
                if (minutesexists)
                    seconds = duration.substring(duration.indexOf("M") + 1,
                            duration.indexOf("S"));
                else
                    seconds = duration.substring(duration.indexOf("H") + 1,
                            duration.indexOf("S"));
            } else if (minutesexists)
                seconds = duration.substring(duration.indexOf("M") + 1,
                        duration.indexOf("S"));
            else
                seconds = duration.substring(duration.indexOf("T") + 1,
                        duration.indexOf("S"));
            if (seconds.length() == 1)
                seconds = "0" + seconds;
            time += seconds;
        }else {
            time += "00";
        }
        return time;
    }
}

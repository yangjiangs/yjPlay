

package chuangyuan.ycj.videolibrary.video;


import android.content.Context;
import android.net.Uri;
import android.os.Handler;

import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import chuangyuan.ycj.videolibrary.okhttp.OkHttpDataSourceFactory;
import okhttp3.OkHttpClient;

/*****
 * 数据源处理类
 * ***/
public class ExoPlayerMediaSourceBuilder {
    private   static  final  String TAG="ExoPlayerMediaSourceBuilder";
    private DefaultBandwidthMeter bandwidthMeter;
    private Context context;
    private Uri uri;
    private int streamType;
    private Handler mainHandler = new Handler();
    private OkHttpClient okHttpClient;
    public ExoPlayerMediaSourceBuilder(Context context, String url) {
        Stetho.initializeWithDefaults(context.getApplicationContext());
        this.context = context;
        this.uri = Uri.parse(url);
        this.bandwidthMeter = new DefaultBandwidthMeter();
        this.streamType = Util.inferContentType(uri.getLastPathSegment());
    }
    public ExoPlayerMediaSourceBuilder(Context context, Uri url) {
        Stetho.initializeWithDefaults(context.getApplicationContext());
        this.context = context;
        this.uri =url;
        this.bandwidthMeter = new DefaultBandwidthMeter();
        this.streamType = Util.inferContentType(uri.getLastPathSegment());
    }

    public MediaSource getMediaSource(boolean preview) {
        switch (streamType) {
            case C.TYPE_SS:
                return new SsMediaSource(uri, new DefaultDataSourceFactory(context, null,
                        getHttpDataSourceFactory(preview)),
                        new DefaultSsChunkSource.Factory(getDataSourceFactory(preview)),
                        mainHandler, null);
            case C.TYPE_DASH:
                return new DashMediaSource(uri,
                        new DefaultDataSourceFactory(context, null,
                                getHttpDataSourceFactory(preview)),
                        new DefaultDashChunkSource.Factory(getDataSourceFactory(preview)),
                        mainHandler, null);
            case C.TYPE_HLS:
                return new HlsMediaSource(uri, getDataSourceFactory(preview), mainHandler, null);
            case C.TYPE_OTHER:

                return new ExtractorMediaSource(uri, getDataSourceFactory(preview),
                        new DefaultExtractorsFactory(), mainHandler, null);
            default: {
                throw new IllegalStateException("Unsupported type: " + streamType);
            }
        }
    }

    private DataSource.Factory getDataSourceFactory(boolean preview) {
        return new DefaultDataSourceFactory(context, preview ? null : bandwidthMeter,
                getHttpDataSourceFactory(preview));
    }

    private DataSource.Factory getHttpDataSourceFactory(boolean preview) {
        // return new DefaultHttpDataSourceFactory(Util.getUserAgent(context,"yjPlay"), preview ? null : bandwidthMeter);
        okHttpClient = new OkHttpClient.Builder().addNetworkInterceptor(new StethoInterceptor()).build();
        return new OkHttpDataSourceFactory(okHttpClient, Util.getUserAgent(context, "yjPlay"), bandwidthMeter);
    }


    /***
     * 获取链接类型
     * @return  int
     ***/
    public int getStreamType() {
        return streamType;
    }

//    public LoopingMediaSource getMediaSourcs() {
//        Uri uri1 = Uri.parse(context.getString(R.string.url_hls));
//        Uri uri2 = Uri.parse(context.getString(R.string.url_dash));
//        Uri uri3 = Uri.parse(context.getString(R.string.url_smooth));
//        ExtractorMediaSource source = new ExtractorMediaSource(uri1, getDataSourceFactory(false),
//                new DefaultExtractorsFactory(), mainHandler, null);
//        ExtractorMediaSource source2 = new ExtractorMediaSource(uri2, getDataSourceFactory(false),
//                new DefaultExtractorsFactory(), mainHandler, null);
//        ExtractorMediaSource source3 = new ExtractorMediaSource(uri3, getDataSourceFactory(false),
//                new DefaultExtractorsFactory(), mainHandler, null);
//        ConcatenatingMediaSource concatenatedSource =
//                new ConcatenatingMediaSource(
//                        source, source2, source3);
//        LoopingMediaSource compositeSource =
//                new LoopingMediaSource(concatenatedSource);
//        return compositeSource;
//    }


    public void release(){
    }
}

package cmu.team5.MaraudersMap.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;

import cmu.team5.MaraudersMap.R;

/**
 * Created by ximengw on 7/26/16.
 *
 * Play a GIF radar animation.
 */
public class GIFView extends View {

    private Movie movie;
    private long moviestart;
    public GIFView(Context context) throws IOException {
        super(context);

        InputStream is = context.getResources().openRawResource(R.raw.radar_test);
        movie = Movie.decodeStream(is);

       // movie= Movie.decodeStream(getResources().getAssets().open("my_radar.gif"));
    }

    public GIFView(Context context, AttributeSet attrs) throws IOException{
        super(context, attrs);
        InputStream is = context.getResources().openRawResource(R.raw.radar_test);
        movie = Movie.decodeStream(is);
    }

    public GIFView(Context context, AttributeSet attrs, int defStyle) throws IOException {
        super(context, attrs, defStyle);
        InputStream is = context.getResources().openRawResource(R.raw.radar_test);
        movie = Movie.decodeStream(is);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        long now=android.os.SystemClock.uptimeMillis();
        Paint p = new Paint();
        p.setAntiAlias(true);
        if (moviestart == 0)
            moviestart = now;
        int relTime;

        relTime = (int)((now - moviestart) % movie.duration());
        movie.setTime(relTime);
        movie.draw(canvas,0,0);
        this.invalidate();
    }
}
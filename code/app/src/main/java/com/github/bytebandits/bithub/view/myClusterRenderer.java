package com.github.bytebandits.bithub.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.github.bytebandits.bithub.model.MoodMarker;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

public class myClusterRenderer extends DefaultClusterRenderer<MoodMarker> {
    private final Context context;
    public myClusterRenderer(Context context, GoogleMap map,
                             ClusterManager<MoodMarker> clusterManager) {
        super(context, map, clusterManager);
        this.context = context;
    }

    /**
     * Determines if a cluster should be rendered as a cluster.
     * @param cluster cluster to examine for rendering
     * @return true if the cluster should be rendered as a cluster, false otherwise
     */
    @Override
    protected boolean shouldRenderAsCluster(Cluster<MoodMarker> cluster) {
        // Cluster items if more than 1 item overlaps.
        return cluster.getSize() > 1;
    }

    /**
     * Called before a cluster item is rendered.
     * @param item          item to be rendered
     * @param markerOptions the markerOptions representing the provided item
     */
    @Override
    protected void onBeforeClusterItemRendered(MoodMarker item, MarkerOptions markerOptions) {
        // Create your custom bitmap.
        Bitmap markerBitmap = createCustomMarkerBitmap(item);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(markerBitmap))
                .title(item.getUserId());
    }

    /**
     * Called before a cluster is rendered.
     * @param cluster       cluster to be rendered
     * @param markerOptions markerOptions representing the provided cluster
     */
    @Override
    protected void onBeforeClusterRendered(Cluster<MoodMarker> cluster, MarkerOptions markerOptions) {
        super.onBeforeClusterRendered(cluster, markerOptions);
    }

    /**
     * Creates a custom marker bitmap.
     * @param moodMarker moodMarker to be rendered
     * @return custom marker bitmap
     */
    private Bitmap createCustomMarkerBitmap(MoodMarker moodMarker) {

        // Dimensions for the marker.
        int desiredWidth = 240;
        int desiredHeight = 240;

        // Create a bitmap and canvas to draw on.
        Bitmap bitmap = Bitmap.createBitmap(desiredWidth, desiredHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Draw an off-white rounded rectangle with shadow.
        Paint rectPaint = new Paint();
        rectPaint.setColor(Color.parseColor("#D3D3D3")); // Light gray
        rectPaint.setAntiAlias(true);
        rectPaint.setShadowLayer(10f, 0f, 5f, Color.BLACK);
        float cornerRadius = 40f;
        float padding = 20f;
        canvas.drawRoundRect(padding, padding, desiredWidth - padding, desiredHeight - padding, cornerRadius, cornerRadius, rectPaint);

        // Draw the emotion drawable
        Drawable drawable = ContextCompat.getDrawable(context, moodMarker.getEmotion().getLogoID());
        if (drawable != null) {
            float drawablePadding = 30f;
            // Adjust bounds for the drawable
            drawable.setBounds((int) (padding + drawablePadding),
                    (int) (padding + drawablePadding),
                    (int) (desiredWidth - padding - drawablePadding),
                    (int) (desiredHeight - padding - drawablePadding - 40f));
            drawable.draw(canvas);
        }

        // Draw a rounded background for text
        Paint textBackgroundPaint = new Paint();
        textBackgroundPaint.setColor(Color.WHITE);
        textBackgroundPaint.setAntiAlias(true);
        float textBackgroundHeight = 50f;
        float textBackgroundTop = desiredHeight - textBackgroundHeight - padding;
        canvas.drawRoundRect(padding + 10f, textBackgroundTop, desiredWidth - padding - 10f,
                textBackgroundTop + textBackgroundHeight, cornerRadius / 2, cornerRadius / 2, textBackgroundPaint);

        // Draw the user id text
        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(50f);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC));
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);

        float xPos = canvas.getWidth() / 2f;
        float yPos = textBackgroundTop + textBackgroundHeight / 2f - ((textPaint.descent() + textPaint.ascent()) / 2f);
        String userId = "@" + moodMarker.getUserId();
        canvas.drawText(userId, xPos, yPos, textPaint);

        return bitmap;
    }

}

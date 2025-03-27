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

    @Override
    protected boolean shouldRenderAsCluster(Cluster<MoodMarker> cluster) {
        // Cluster items if more than 1 item overlaps (which is the default behavior).
        // Return true when cluster size is at least 2.
        return cluster.getSize() > 1;
    }

    @Override
    protected void onBeforeClusterItemRendered(MoodMarker item, MarkerOptions markerOptions) {
        // Create your custom bitmap based on the MoodMarker's properties
        Bitmap markerBitmap = createCustomMarkerBitmap(item);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(markerBitmap))
                .title(item.getUserId());
    }

    // Optional: If you want a custom cluster marker (when there are 2+ items), override onBeforeClusterRendered.
    @Override
    protected void onBeforeClusterRendered(Cluster<MoodMarker> cluster, MarkerOptions markerOptions) {
        // For example, you can call the default or create a custom bitmap using cluster.getSize()
        // Here we call the default implementation:
        super.onBeforeClusterRendered(cluster, markerOptions);
    }

    // This method encapsulates your drawing logic. Adjust parameters as needed.
    private Bitmap createCustomMarkerBitmap(MoodMarker moodMarker) {

        // You can redefine these dimensions to suit your needs.
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

        // Draw the emotion drawable. (Assuming moodMarker holds the resource ID.)
        Drawable drawable = ContextCompat.getDrawable(context, moodMarker.getEmotion().getLogoID());
        if (drawable != null) {
            float drawablePadding = 30f;
            // Adjust bounds for the drawable (leaving space for text if needed)
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
        String userId = "@" + moodMarker.getUserId();  // Assume your MoodMarker stores a String userId.
        canvas.drawText(userId, xPos, yPos, textPaint);

        return bitmap;
    }

}

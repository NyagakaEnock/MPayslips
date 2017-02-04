package adapter;

import android.view.ScaleGestureDetector;

/**
 * Created by Enock on 10/7/2016.
 */
public class OnPinchListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

    float startingSpan;
    float endSpan;
    float startFocusX;
    float startFocusY;


    public boolean onScaleBegin(ScaleGestureDetector detector) {
        startingSpan = detector.getCurrentSpan();
        startFocusX = detector.getFocusX();
        startFocusY = detector.getFocusY();
        return true;
    }


    public boolean onScale(ScaleGestureDetector detector,ZoomLayout mZoomableRelativeLayout) {

      //  mZoomableRelativeLayout.scale(detector.getCurrentSpan()/startingSpan, startFocusX, startFocusY);
        return true;
    }

    public void onScaleEnd(ScaleGestureDetector detector,ZoomLayout mZoomableRelativeLayout) {
       // mZoomableRelativeLayout.restore();
    }
}
/**
 * @file        NestedWebView.java
 * @summary     Source file for the NestedWebView class.
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        Dec 13, 2016
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2015
 */

package io.tengentoppa.yggdrasil;

// Android
import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.webkit.WebView;

/**
 * @summary The NestedWebView class.
 *          It is essentially a WebView that implements the
 *          NestedScrollingChild interface to play well with a
 *          CoordinatorLayout.
 */
public class NestedWebView extends WebView
    implements NestedScrollingChild,
               OnGestureListener {

    // ====================================================================
    // PUBLIC METHODS

    // --------------------------------------------------------------------
    // CONSTRUCTORS

    public NestedWebView(Context context) {
        this(context,
             null);
    }

    public NestedWebView(Context context,
                         AttributeSet attrs) {
        this(context,
             attrs,
             android.R.attr.webViewStyle);
    }

    public NestedWebView(Context context,
                         AttributeSet attrs,
                         int defStyleAttr) {
        super(context,
              attrs,
              defStyleAttr);
        m_nestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        m_gestureDetector = new GestureDetectorCompat(context,
                                                      this);
        setNestedScrollingEnabled(true);
    }

    // --------------------------------------------------------------------
    // VIEW OVERRIDES

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final boolean handled = m_gestureDetector.onTouchEvent(event);
        // A special case for the "up" action must be provided here,
        // since it is missing from the GestureDetector interface.
        if (!handled && (MotionEvent.ACTION_UP == event.getAction())) {
            stopNestedScroll();
        }
        // Pass through to the "super" method, to allow the base view
        // (in this case the WebView) to handle the touch event.
        return super.onTouchEvent(event);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        m_nestedScrollingChildHelper.onDetachedFromWindow();
    }

    // --------------------------------------------------------------------
    // NESTEDSCROLLINGCHILD INTERFACE IMPLEMENTATION

    @Override
    public boolean hasNestedScrollingParent() {
        return m_nestedScrollingChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return m_nestedScrollingChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        m_nestedScrollingChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return m_nestedScrollingChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        m_nestedScrollingChildHelper.stopNestedScroll();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed,
                                        int dyConsumed,
                                        int dxUnconsumed,
                                        int dyUnconsumed,
                                        int[] offsetInWindow) {
        return
            m_nestedScrollingChildHelper.dispatchNestedScroll(dxConsumed,
                                                              dyConsumed,
                                                              dxUnconsumed,
                                                              dyUnconsumed,
                                                              offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx,
                                           int dy,
                                           int[] consumed,
                                           int[] offsetInWindow) {
        return m_nestedScrollingChildHelper.dispatchNestedPreScroll(
                                                            dx,
                                                            dy,
                                                            consumed,
                                                            offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX,
                                       float velocityY,
                                       boolean consumed) {
        return m_nestedScrollingChildHelper.dispatchNestedFling(velocityX,
                                                                velocityY,
                                                                consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return m_nestedScrollingChildHelper.dispatchNestedPreFling(velocityX,
                                                                   velocityY);
    }

    // --------------------------------------------------------------------
    // GESTUREDETECTOR INTERFACE IMPLEMENTATION

    @Override
    public boolean onDown(MotionEvent event) {
        startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent event1,
                            MotionEvent event2,
                            float distanceX,
                            float distanceY) {
        dispatchNestedPreScroll(0,
                                (int)distanceY,
                                null,
                                null);
        dispatchNestedScroll(0,
                             0,
                             0,
                             0,
                             null);
        return true;
    }

    @Override
    public void onLongPress(MotionEvent event) {
        return;
    }

    @Override
    public boolean onFling(MotionEvent event1,
                           MotionEvent event2,
                           float velocityX,
                           float velocityY) {
        return true;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent event) {
        return;
    }

    // ====================================================================
    // PRIVATE METHODS

    // --------------------------------------------------------------------
    // DATA MEMBERS

    /**
     * @summary The NestedScrollingChildHelper.
     */
    private final NestedScrollingChildHelper    m_nestedScrollingChildHelper;

    private GestureDetectorCompat               m_gestureDetector;

}

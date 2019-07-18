package justy.com.pullupdraglayout;

import android.content.Context;

/**
 * @author VenRen
 * @time 2018/1/5  下午5:00
 * @desc ${TODD}
 */

public class DisplayUtils {

    public static final int  S_DESINE_DPI = 480;

    public static final int S_DESINE_WIDTH = 540;

    /**
     * 将px值转换为dip或dp值，保证尺寸大小不变
     */
    public static int px2dip(Context context, float pxValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 将dip或dp值转换为px值，保证尺寸大小不变
     */
    public static int dip2px(Context context, float dipValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     */
    public static int px2sp(Context context, float pxValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     */
    public static int sp2px(Context context, float spValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static int getRelativeViewWidthInpx(Context context, int width) {
        return (int) (context.getResources().getDisplayMetrics().densityDpi * 1.0f / S_DESINE_DPI * width);
    }

    public static int getRelativeViewHeightInpx(Context context, int height) {
        return (int) (context.getResources().getDisplayMetrics().densityDpi * 1.0f / S_DESINE_DPI * height);
    }

    public static int getRelativeTextSize(Context context, int size) {
        double width = Math.min(context.getResources().getDisplayMetrics().widthPixels, context.getResources().getDisplayMetrics().heightPixels);
        return (int) (width / S_DESINE_WIDTH * size);
    }

}

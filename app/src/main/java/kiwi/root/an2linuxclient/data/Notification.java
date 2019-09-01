/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.data;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import androidx.annotation.RequiresApi;

public class Notification {

    private String title;
    private String message;
    private Bitmap icon;
    private NotificationSettings ns;

    public Notification(StatusBarNotification sbn, Context c){
        ns = new NotificationSettings(c, sbn.getPackageName());
        extractStatusBarNotification(sbn, c);
    }

    private void extractStatusBarNotification(StatusBarNotification sbn, Context c){
        PackageManager pm =  c.getPackageManager();

        if (ns.includeTitle()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                extractTitle(sbn, pm);
            } else {
                // Android 4.3 and below (SDK 18) does not support simple extraction of notification
                // data. It still might be possible with reflection...
                title = getAppName(pm, sbn.getPackageName());
            }
            if (title.length() > ns.getTitleMax()){
                title = title.substring(0, ns.getTitleMax()) + "…";
            }
        }

        if (ns.includeMessage()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                extractMessage(sbn);
            }
        }

        if (ns.includeIcon()) {
            try {
                int iconSize = ns.getIconSize();
                icon = Bitmap.createScaledBitmap(drawableToBitmap(pm.getApplicationIcon(sbn.getPackageName())), iconSize, iconSize, true);
            } catch (PackageManager.NameNotFoundException e){
                ns.removeIconFlag();
            }
        }
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        Bitmap bitmap;
        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * @return The app name if successful, otherwise packagename
     */
    private String getAppName(PackageManager pm, String packageName) {
        String appName;
        try {
            appName = (String) pm.getApplicationLabel(pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
        } catch (PackageManager.NameNotFoundException e) {
            appName = packageName;
        }
        return appName;
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private void extractTitle(StatusBarNotification sbn, PackageManager pm) {
        Bundle extras = sbn.getNotification().extras;

        String contentTitle = "";
        CharSequence temp = extras.getCharSequence(android.app.Notification.EXTRA_TITLE);
        if (temp != null){
            contentTitle = temp.toString();
        }

        String appName = getAppName(pm, sbn.getPackageName());

        title = "";

        if (ns.forceTitle()){
            title = appName;
        } else {
            if (!contentTitle.equals("")){
                title = contentTitle;
            } else {
                title = appName;
            }
        }

        title = title.trim();
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private void extractMessage(StatusBarNotification sbn) {
        Bundle extras = sbn.getNotification().extras;

        String contentText = "";
        String subText = "";
        CharSequence temp = extras.getCharSequence(android.app.Notification.EXTRA_TEXT);
        if (temp != null){
            contentText = temp.toString();
        }
        temp = extras.getCharSequence(android.app.Notification.EXTRA_SUB_TEXT);
        if (temp != null){
            subText = temp.toString();
        }

        message = "";
        if (!contentText.equals("")) message += contentText;
        if (!subText.equals("")) message += "\n" + subText;
        message = message.replace("\n\n", "\n").trim();
        if (message.length() > ns.getMessageMax()){
            message = message.substring(0, ns.getMessageMax()) + "…";
        }
    }

    public String getTitle() {
        return this.title;
    }

    public String getMessage() {
        return this.message;
    }

    public Bitmap getIcon() {
        return this.icon;
    }

    public NotificationSettings getNotificationSettings(){
        return this.ns;
    }

}

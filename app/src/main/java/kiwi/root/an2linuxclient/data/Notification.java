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
import android.os.Bundle;
import android.service.notification.StatusBarNotification;

public class Notification {

    private String title;
    private String message;
    private Bitmap icon;
    private NotificationSettings ns;

    public Notification(StatusBarNotification sbn, Context c){
        ns = new NotificationSettings(c);
        extractStatusBarNotification(sbn, c);
    }

    private void extractStatusBarNotification(StatusBarNotification sbn, Context c){
        Bundle extras = sbn.getNotification().extras;

        PackageManager pm =  c.getPackageManager();
        String packageName = sbn.getPackageName();

        if (ns.includeTitle()){
            String contentTitle = "";
            CharSequence temp = extras.getCharSequence(android.app.Notification.EXTRA_TITLE);
            if (temp != null){
                contentTitle = temp.toString();
            }

            String appName;
            try {
                appName = (String) pm.getApplicationLabel(pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
            } catch (PackageManager.NameNotFoundException e) {
                appName = packageName;
            }

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
            if (title.length() > ns.getTitleMax()){
                title = title.substring(0, ns.getTitleMax()) + "…";
            }
        }

        if (ns.includeMessage()){
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

        if (ns.includeIcon()){
            try {
                icon = Bitmap.createScaledBitmap(drawableToBitmap(pm.getApplicationIcon(packageName)), 64, 64, true);
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

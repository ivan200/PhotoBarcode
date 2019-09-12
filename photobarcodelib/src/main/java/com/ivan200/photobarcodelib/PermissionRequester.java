package com.ivan200.photobarcodelib;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public abstract class PermissionRequester {
    private int codeForRequestPermissions = 1201;
    private int codeForResult = 0;
    private List<String> mPermissions;

    /**
     * Each class inherited from this requester must implement a positive result method for requesting permissions
     */
    protected abstract void onPermissionGranted(int requestCode, int resultCode, Intent data);

    /**
     * Each class inherited from this requester should show its own dialog to go to the application settings, with its own texts
     */
    protected abstract void onPermissionRejected(int requestCode, String blockedPermission);

    public void setCodeForRequestPermissions(int codeForRequestPermissions) {
        this.codeForRequestPermissions = codeForRequestPermissions;
    }

    /**
     * The main permission request method that is called in external classes
     */
    public void requestPermissions(List<String> permissions, Activity mActivity, int requestCode) {
        codeForRequestPermissions = requestCode;
        mPermissions = filter(permissions, p -> p != null);

        if (getDeniedPermissions(mActivity).length == 0) {
            onPermissionGranted(codeForRequestPermissions, codeForResult, null);
        } else {
            ActivityCompat.requestPermissions(mActivity, getDeniedPermissions(mActivity), codeForRequestPermissions);
        }
    }

    public void onRequestPermissionsResult(Activity mActivity, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == codeForRequestPermissions) {
            String[] blockedPermissions = getBlockedPermissions(mActivity);
            if (blockedPermissions.length > 0) {
                onPermissionRejected(requestCode, blockedPermissions[0]);
            } else {
                int i = indexOf(grantResults, x -> x != PackageManager.PERMISSION_GRANTED);
                if(i >=0){
                    onPermissionRejected(requestCode, permissions[i]);
                } else {
                    onPermissionGranted(codeForRequestPermissions, codeForResult, null);
                }
            }
        }
    }

    public void onActivityResult(Activity mActivity, int requestCode, int resultCode, Intent data) {
        if (requestCode == codeForRequestPermissions) {
            codeForResult = resultCode;
            String[] deniedPermissions = getDeniedPermissions(mActivity);
            if (deniedPermissions.length == 0) {
                String[] blockedPermissions = getBlockedPermissions(mActivity);
                if (blockedPermissions.length == 0) {
                    onPermissionGranted(codeForRequestPermissions, codeForResult, data);
                } else {
                    onPermissionRejected(requestCode, blockedPermissions[0]);
                }
            } else {
                ActivityCompat.requestPermissions(mActivity, deniedPermissions, codeForRequestPermissions);
            }
        }
    }

    public String[] getBlockedPermissions(Activity activity) {
        List<String> filter = filter(mPermissions, permission ->
                ActivityCompat.shouldShowRequestPermissionRationale(activity, permission));
        return toStringArray(filter);
    }

    public String[] getDeniedPermissions(Context context) {
        List<String> filter = filter(mPermissions, permission ->
                ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED);
        return toStringArray(filter);
    }




    public static <T> ArrayList<T> filter(Iterable<T> list, Function<? super T, Boolean> function) {
        ArrayList<T> newList = new ArrayList<>();
        for (T t : list) {
            if (function.apply(t)) {
                newList.add(t);
            }
        }
        return newList;
    }

    public static String[] toStringArray(Collection<String> a) {
        String[] array = new String[a.size()];
        int i = 0;
        for (String s : a) {
            array[i] = s;
            i++;
        }
        return array;
    }

    public static int indexOf(int[] array, Function<? super Integer, Boolean> predicate) {
        for (int i = 0; i < array.length; i++) {
            if (predicate.apply(array[i])) return i;
        }
        return -1;
    }
}

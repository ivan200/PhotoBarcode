package com.ivan200.photobarcodelib.images;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;

import com.ivan200.photobarcodelib.R;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.exifinterface.media.ExifInterface;

public class ImageHelper {
    private static final String PHOTOS = "photos";
    private static final String THUMBNAILS = "thumbnails";
    public static final int defaultMaxImageSize = 1200;
    private static final int JPEGCompressRate = 80;

    public static String getFileName(){
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        final String JPEG_FILE_PREFIX = "IMG_";
        final String JPEG_FILE_SUFFIX = ".jpg";
        return JPEG_FILE_PREFIX + timeStamp + JPEG_FILE_SUFFIX;
    }

    public static File createImageFile(Context context) throws IOException {
        File photosDir = ImageHelper.getPhotosDir(context);
        photosDir.mkdir();

        File image = new File(photosDir, getFileName());
        image.createNewFile();
        return image;
    }

    public static File getPhotosDir(Context context){
        File file = new File(context.getFilesDir(), PHOTOS);
        if(!file.exists()){
            file.mkdirs();
        }
        return file;
    }

    public static File getThumbsDir(Context context){
        File file = new File(context.getFilesDir(), THUMBNAILS);
        if(!file.exists()){
            file.mkdirs();
        }
        return file;
    }

    public static File saveBitmap(Context context, Bitmap imageBitmap, File saveFile) throws IOException {
        saveFile.getParentFile().mkdirs();
        if (!saveFile.exists()){
            saveFile.createNewFile();
        }

        Bitmap bitmap = imageBitmap;
        if (bitmap == null){
            throw new RuntimeException(context.getString(R.string.error_empty_image));
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, JPEGCompressRate, stream);
        byte[] bytes = stream.toByteArray();
        File file = saveBytes(context, bytes, saveFile);
        bytes = null;
        return file;
    }

    public static File saveBytes(Context context, byte[] bytes, File saveFile) throws IOException {
        if (bytes == null){
            throw new RuntimeException(context.getString(R.string.error_empty_image));
        }

        saveFile.getParentFile().mkdirs();
        if (!saveFile.exists()){
            saveFile.createNewFile();
        }

        FileOutputStream fOut = new FileOutputStream(saveFile);
        // Some servers can't handle jpeg if it has the wrong first or last 2 bytes. (ffd8 and ffd9)
        // So we have to check and edit it manually
        boolean properStart = ((bytes[0]) == ((byte) 0xff)) && ((bytes[1]) == ((byte) 0xd8));
        boolean properEnd = ((bytes[bytes.length - 2]) == ((byte) 0xff)) && ((bytes[bytes.length - 1]) == ((byte) 0xd9));
        BufferedOutputStream bos = new BufferedOutputStream(fOut);
        if (!properStart) {
            bos.write((byte) 0xff);
            bos.write((byte) 0xd8);
        }
        bos.write(bytes);
        if (!properEnd) {
            bos.write((byte) 0xff);
            bos.write((byte) 0xd9);
        }
        bos.flush();
        bos.close();
        fOut.flush();
        fOut.close();
        bytes = null;
        return saveFile;
    }

    public static File resizeAndSaveBitmap(Context context, Bitmap data, File newFile, int maxSize) throws IOException {
        Bitmap bitmap = resizeBitmapAndRotateByExif(context, data, maxSize, ExifInterface.ORIENTATION_NORMAL, false);
        return saveBitmap(context, bitmap, newFile);
    }

    public static Bitmap resizeBitmapAndRotateByExif(Context context, Bitmap src, int maxSize, int exifOrientation, boolean flipHorizontal){
        if (src == null){
            throw new RuntimeException(context.getString(R.string.error_empty_image));
        }

        int width = src.getWidth();
        int height = src.getHeight();
        if (width == 0 || height == 0){
            throw new RuntimeException(context.getString(R.string.error_zero_size_image));
        }

        Matrix m = new Matrix();
        float scale = 1;
        int max = Math.max(width, height);
        //If the image is larger than necessary, then reduce it to maxSize
        if(max > maxSize) {
            int maxHeight = maxSize;
            int maxWidth = maxSize;
            scale = Math.min(((float) maxHeight / width), ((float) maxWidth / height));
        }
        if (scale != 1) {
            m.postScale(scale, scale);
        }
        //If the image is taken on the front camera, it should be flipped
        if(flipHorizontal){
            float cx = (width * scale)/2;
            float cy = (height * scale)/2;
            m.postScale(-1, 1, cx, cy);
        }
        int rotate = ExifData.getRotateAngleByExif(exifOrientation);
        if(rotate > 0) {
            m.postRotate(rotate);
        }
        if(scale == 1 && rotate == 0){
            return src;
        }
        return Bitmap.createBitmap(src, 0, 0, width, height, m, true);
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int rotate){
        if(rotate == 0) {
            return bitmap;
        } else {
            Matrix m = new Matrix();
            m.postRotate(rotate);
            Bitmap bitmap1 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, false);
            bitmap.recycle();
            bitmap = null;
            return bitmap1;
        }
    }

    private static Bitmap decodeFile(File f, int maxSize) throws IOException {
        Bitmap b = null;

        //Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;

        FileInputStream fis = new FileInputStream(f);
        BitmapFactory.decodeStream(fis, null, o);
        fis.close();

        int scale = 1;
        while (o.outHeight > maxSize * scale * 2 || o.outWidth > maxSize * scale * 2){
            scale = scale *2;
        }

        //Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        o2.inPurgeable = true;
        o2.inInputShareable = true;

        fis = new FileInputStream(f);
        b = BitmapFactory.decodeStream(fis, null, o2);
        fis.close();
        return b;
    }

    public static void deleteImageFile(Context context, String path){
        File photoFile = new File(path);
        if (photoFile.exists()) {
            if (photoFile.delete()) {
                File thumbsFile = new File(getThumbsDir(context), photoFile.getName());
                if(thumbsFile.exists()){
                    thumbsFile.delete();
                }
            }
        }
    }

    //When resizing, we delete exif data since some servers just skip them
    public static ExifData resizeFileWithThumb(
            File origFile, File smallFile, File thumbFile, double rotateAngle, Activity activity,
            boolean tryFixOrientation, int maxImageSize, boolean flipHorizontal) throws Exception {
        ExifData exifData = new ExifData(origFile.getAbsolutePath());
        Bitmap bigBitmap = decodeFile(origFile, maxImageSize);

        exifData.fixExifOrientation(tryFixOrientation ? ExifData.FixOrientationMode.UNDEFINED : ExifData.FixOrientationMode.NONE,
                bigBitmap.getWidth(), bigBitmap.getHeight(), rotateAngle, activity);

        Bitmap bitmap = resizeBitmapAndRotateByExif(activity, bigBitmap, maxImageSize, exifData.getOrientation(), flipHorizontal);
        saveBitmap(activity, bitmap, smallFile);
        if(bitmap != bigBitmap){
            bitmap.recycle();
            bitmap = null;
        }
        if(thumbFile!= null) {
            Bitmap thumbBitmap = resizeBitmapAndRotateByExif(activity, bigBitmap, getThumbSize(activity, maxImageSize), exifData.getOrientation(), flipHorizontal);
            saveBitmap(activity, thumbBitmap, thumbFile);
            thumbBitmap.recycle();
            thumbBitmap = null;
        }
        bigBitmap.recycle();
        bigBitmap = null;
        return exifData;
    }

    private static int getThumbSize(Context context, int maxImageSize){
        final int maxThumbSize = maxImageSize/3;
        final int minThumbSize = 150;

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int maxSize = Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels);
        int thumbSize = maxSize/3;
        if(thumbSize > maxThumbSize) {
            thumbSize = maxThumbSize;
        }
        if(thumbSize < minThumbSize) {
            thumbSize = minThumbSize;
        }
        return thumbSize;
    }

    public static void copyImageToGallery(Context context, File image, String ALBUM) throws IOException {
        if(ALBUM == null) return;
        if(!isExternalStorageWritable()){
            throw new RuntimeException(context.getString(R.string.perm_storage_unavailable));
        }
        File externalStoragePublicDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File albumStorageDir = ALBUM.length() == 0 ? externalStoragePublicDirectory : new File(externalStoragePublicDirectory, ALBUM);
        albumStorageDir.mkdirs();
        File newGalleryFile = new File(albumStorageDir, image.getName());
        copyFile(image, newGalleryFile);

        updateGallery(context, ALBUM, newGalleryFile);
    }

    private static void copyFile(File sourceFile, File destFile) throws IOException {
        FileChannel source = new FileInputStream(sourceFile).getChannel();
        if(!destFile.exists()) {
            destFile.createNewFile();
        }
        FileChannel destination = new FileOutputStream(destFile).getChannel();
        destination.transferFrom(source, 0, source.size());
        destination.close();
        source.close();
    }

    /* Checks if external storage is available for read and write */
    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    //Add the image and image album into gallery
    private static void updateGallery(Context context, String albumName, File file) {
        //metadata of new image
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, file.getName());
        values.put(MediaStore.Images.Media.DESCRIPTION, albumName);
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.ImageColumns.BUCKET_ID, file.toString().toLowerCase(Locale.US).hashCode());
        values.put(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, file.getName().toLowerCase(Locale.US));
        values.put("_data", file.getAbsolutePath());

        ContentResolver cr = context.getContentResolver();
        cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        // Tell the media scanner about the new file so that it is
        // immediately available to the user.
        MediaScannerConnection.scanFile(context, new String[]{file.toString()}, null,
                (path, uri) -> {
                    Log.i("ExternalStorage", "Scanned " + path + ":");
                    Log.i("ExternalStorage", "-> uri=" + uri);
                });

    }

    public static void deleteAllPhotos(Context context){
        File photosDir = getPhotosDir(context);
        deleteDir(photosDir);
    }

    public static void deleteDir(File dir) {
        if (dir == null) return;
        File[] files = dir.listFiles();
        if (files != null)
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDir(file);
                } else {
                    file.delete();
                }
            }
        dir.delete();
    }
}

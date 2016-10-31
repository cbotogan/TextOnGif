package com.timmy.dev.textgif.HelperClasses;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Environment;
import android.util.Log;

import com.bumptech.glide.gifencoder.AnimatedGifEncoder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public final class Utils {

    private Utils() {

    }

    public static float stickerTextSize;

    /**
     * Frame delay used for the encoding
     */
    public static int frameDelay;


    /**
     * Draws text on the bitmap canvas
     *
     * @param text - text to appear on the canvas
     * @param bmp  - when null the method is used for the real-time display of the text on the gif
     *             else method is used to write the text on the frame before saving the gif to the disk
     * @return - generated bitmap
     */
    public static Bitmap drawTextOnCanvas(String text, Bitmap bmp) {

        Bitmap.Config conf = Bitmap.Config.ARGB_8888;

        if (bmp == null) {
            bmp = Bitmap.createBitmap(300, 300, conf);
        }

        Canvas canvas = new Canvas(bmp);

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(stickerTextSize);
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textPaint.setShadowLayer(1f, 0f, 1f, Color.DKGRAY);

        RectF oval = new RectF(20, 20, 280, 280);
        Path mArc = new Path();
        mArc.addArc(oval, -135, 200);

        canvas.drawTextOnPath(text, mArc, 0, 20, textPaint);

        return bmp;
    }


    /**
     * Generates a byte array from the text-modified bitmap array
     *
     * @param gifBitmapArray Array containing the modified with text bitmaps
     * @return
     */
    public static byte[] encodeGif(List<Bitmap> gifBitmapArray) {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        AnimatedGifEncoder encoder = new AnimatedGifEncoder();
        encoder.setDelay(frameDelay);
        encoder.setTransparent(Color.BLACK);
        encoder.start(bos);

        for (Bitmap bitmap : gifBitmapArray) {
            encoder.addFrame(bitmap);
        }
        encoder.finish();
        byte[] encodedGif = bos.toByteArray();

        return encodedGif;
    }


    /**
     * Transforms the byte array into a Bitmap array after adding the text on each frame
     *
     * @param gifByteArray Unmodified byte array
     * @param desiredText  Text to be added on each frame when creating the bitmap array from the unmodified byte array
     * @return
     */
    public static List<Bitmap> addTextToGifByteArray(byte[] gifByteArray, String desiredText) {

        List<Bitmap> bitmapArrayList = new ArrayList<Bitmap>();

        GifDecoder gifDecoder = new GifDecoder();
        gifDecoder.read(gifByteArray);

        frameDelay = gifDecoder.getDelay(1);

        gifDecoder.advance();
        for (int i = 0; i < gifDecoder.getFrameCount(); i++) {

            Bitmap currentBitmap = gifDecoder.getNextFrame();
            currentBitmap = currentBitmap.copy(Bitmap.Config.ARGB_8888, true);
            currentBitmap = drawTextOnCanvas(desiredText, currentBitmap);
            bitmapArrayList.add(currentBitmap);
            gifDecoder.advance();

        }

        return bitmapArrayList;
    }

    /**
     * Saves the modified gif byte array on disk
     *
     * @param gifByteArray
     */
    public static void saveGif(byte[] gifByteArray) {

        DateFormat dateFormat = new SimpleDateFormat("yyyMMdd_HH:mm:ss");
        Date date = new Date();
        String currentDate = dateFormat.format(date);


        File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile(), "StickchatGif_" + currentDate + ".gif");
        Log.d("fileDIR", file.getAbsolutePath());

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(file);
            stream.write(gifByteArray);
            stream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

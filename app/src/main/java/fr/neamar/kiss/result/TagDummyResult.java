package fr.neamar.kiss.result;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextPaint;
import android.view.View;

import androidx.annotation.NonNull;

import fr.neamar.kiss.MainActivity;
import fr.neamar.kiss.R;
import fr.neamar.kiss.pojo.TagDummyPojo;
import fr.neamar.kiss.utils.FuzzyScore;

public class TagDummyResult extends Result {
    private BitmapDrawable mDrawable = null;

    TagDummyResult(@NonNull TagDummyPojo pojo) {
        super(pojo);
    }

    @Override
    public Drawable getDrawable(Context context) {
        if (mDrawable != null)
            return mDrawable;

        Drawable drawable;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            drawable = context.getDrawable(R.drawable.ic_launcher_white);
        else
            drawable = context.getResources().getDrawable(R.drawable.ic_launcher_white);

        // the drawable should be the same size as the launcher icon
        int width = 10;
        int height = 10;
        if (drawable != null) {
            int intrinsicWidth = drawable.getIntrinsicWidth();
            int intrinsicHeight = drawable.getIntrinsicHeight();
            width = intrinsicWidth >= 0 ? intrinsicWidth : width;
            height = intrinsicHeight >= 0 ? intrinsicHeight : height;
        }

        // create a canvas from a bitmap
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // use StaticLayout to draw the text centered
        TextPaint paint = new TextPaint();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(.6f * height);

        RectF rectF = new RectF(0, 0, width, height);

        // draw a white rounded background
        paint.setColor(0xFFffffff);
        canvas.drawRoundRect(rectF, width / 2.4f, height / 2.4f, paint);

        int codepoint = pojo.getName().codePointAt(0);
        // If the codepoint glyph is an image we can't use SRC_IN to draw it.
        boolean drawAsHole = true;
        Character.UnicodeBlock block = null;
        try {
            block = Character.UnicodeBlock.of(codepoint);
        } catch (IllegalArgumentException ignored) {
        }
        if (block == null)
            drawAsHole = false;
        else if ("EMOTICONS".equals(block.toString()))
            drawAsHole = false;
        else if ("MISCELLANEOUS_SYMBOLS_AND_PICTOGRAPHS".equals(block.toString()))
            drawAsHole = false;
        String glyph = new String(Character.toChars(codepoint));
        // we can't draw images (emoticons and symbols) using SRC_IN with transparent color, the result is a square
        if (drawAsHole) {
            // write text with "transparent" (create a hole in the background)
            paint.setColor(0);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        } else {
            paint.setColor(0xFFffffff);
        }

        // draw the letter in the center
        Rect b = new Rect();
        paint.getTextBounds(glyph, 0, glyph.length(), b);
        canvas.drawText(glyph, 0, glyph.length(), width / 2.f - b.centerX(), height / 2.f - b.centerY(), paint);

//        rectF.set(b);
//        rectF.offset(width / 2.f - rectF.centerX(), height / 2.f - rectF.centerY());
//        // pad the rectF so we don't touch the letter
//        rectF.inset(rectF.width() * -.3f, rectF.height() * -.4f);
//
//        // stroke a rect with the bounding of the letter
//        paint.setStyle(Paint.Style.STROKE);
//        paint.setStrokeWidth(1.f * context.getResources().getDisplayMetrics().density);
//        canvas.drawRoundRect(rectF, rectF.width() / 2.4f, rectF.height() / 2.4f, paint);

        // keep a reference to the drawable in case we need it again
        mDrawable = new BitmapDrawable(bitmap);
        return mDrawable;
    }

    @Override
    public View display(Context context, int position, View convertView, FuzzyScore fuzzyScore) {
        return null;
    }

    @Override
    protected void doLaunch(Context context, View v) {
        if (context instanceof MainActivity) {
            ((MainActivity) context).showMatchingTags(pojo.getName());
        }
    }
}

package com.radiorunt.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.view.Display;
import android.view.WindowManager;

public class ImageHelper {
	Context mContext;
	Bitmap sampledSrcBitmap_res=null;
	 Bitmap scaledBitmap_res=null ;
	 float desiredScale;
	 int desiredWidth,srcWidth,srcHeight;
	public ImageHelper(Context context){
		this.mContext=context;
	}
	 Display display;
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
		if (bitmap == null) {
			return null;
		}
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPx = pixels;

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		//bitmap.recycle();
		return output;
	}
	
	
//	 public Bitmap BitmapResolution_PATH(String imagePath2,int multiple) {
//		   display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
//	    	 desiredWidth=display.getWidth()/multiple;
//	    	// Get the source image's dimensions
//	    	BitmapFactory.Options options = new BitmapFactory.Options();
//	    	options.inJustDecodeBounds = true;
//	    	BitmapFactory.decodeFile(imagePath2, options);
//
//	    	srcWidth = options.outWidth;
//
//	    	// Only scale if the source is big enough. This code is just trying to fit a image into a certain width.
//	    	if(desiredWidth > srcWidth)
//	    	    desiredWidth = srcWidth;
//
//
//	    	// Calculate the correct inSampleSize/scale value. This helps reduce memory use. It should be a power of 2
//	    	
//	    	int inSampleSize = 1;
//	    	while(srcWidth * 2 > desiredWidth){
//	    	    srcWidth *= 3;
//	    	    srcHeight *= 3;
//	    	    inSampleSize *= 2;
//	    	}
//
//	    	 desiredScale = (float) desiredWidth * srcWidth;
//
//	    	// Decode with inSampleSize
//	    	options.inJustDecodeBounds = false;
//	    	options.inDither = false;
//	    	options.inSampleSize = inSampleSize;
//	    	options.inScaled = false;
//	    	options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//	    	Bitmap sampledSrcBitmap = BitmapFactory.decodeFile(imagePath2, options);
//
//	    	// Resize
//	    	Matrix matrix = new Matrix();
//	    	matrix.postScale(desiredScale, desiredScale);
//	    	Bitmap scaledBitmap = Bitmap.createBitmap(sampledSrcBitmap, 0, 0, sampledSrcBitmap.getWidth(), sampledSrcBitmap.getHeight(), matrix, true);
//	    	//sampledSrcBitmap.recycle();
//
//	    	 matrix=null;
//			return scaledBitmap;
//		}

}
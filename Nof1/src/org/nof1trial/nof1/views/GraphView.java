/*******************************************************************************
 * Nof1 Trials helper, making life easier for clinicians and patients in N of 1 trials.
 * Copyright (C) 2012 John Lawson
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You may obtain a copy of the GNU General Public License at  
 * <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     John Lawson - initial API and implementation
 ******************************************************************************/
package org.nof1trial.nof1.views;

import java.util.ArrayList;

import org.nof1trial.nof1.containers.Label;
import org.nof1trial.nof1.containers.Line;
import org.nof1trial.nof1.containers.Vec2;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * View to draw and display a graph showing the data over time. x = days, y =
 * patient feedback
 * 
 * @author John Lawson
 * 
 */
public class GraphView extends View {

	private static final String TAG = "GraphView";
	private static final boolean DEBUG = false;

	private static final int TICK_SIZE = 5;
	private static final int TEXT_SIZE = 12;

	private static final int TOP_PAD = 10;
	private static final int BOTTOM_PAD = 35;
	private static final int LEFT_PAD = 25;
	private static final int RIGHT_PAD = 10;

	private static final float RATIO = 0.43f;

	/** List of each data point. */
	private ArrayList<Vec2> mVecList;

	/** List of lines to draw as x-axis */
	private final ArrayList<Line> mXAxisList;

	/** List of x-axis labels */
	private final ArrayList<Label> mXLabelList;

	/** List of lines to draw as y-axis */
	private final ArrayList<Line> mYAxisList;

	/** List of y-axis labels */
	private final ArrayList<Label> mYLabelList;

	/** List of re3ctangles used to shade vertical regions */
	private final ArrayList<RectF> mVertShadingList;

	/** Paint to draw the points */
	private final Paint mVecPaint;

	/** Paint to draw axes */
	private final Paint mAxesPaint;

	/** Paint to draw the shading */
	private final Paint mShadingPaint;

	/** Scale in x direction */
	private float mScaleX;

	/** Largest value of x */
	private int mMaxX;

	/** Largest value of y */
	private int mMaxY;

	/** Height of view */
	private int mHeight;

	/** Width of view */
	private int mWidth;

	/** Cursor holding data */
	private Cursor mCursor;

	/** Separation width of the vertical lines, if any is specified */
	private int mVertLineWidth;

	/** Array of which vertical regions should be shaded */
	private boolean[] mShaded;

	private Path mPath;

	public GraphView(Context context) {
		this(context, null, 0);
	}

	public GraphView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GraphView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mVecList = new ArrayList<Vec2>();
		mXAxisList = new ArrayList<Line>();
		mXLabelList = new ArrayList<Label>();
		mYAxisList = new ArrayList<Line>();
		mYLabelList = new ArrayList<Label>();
		mVertShadingList = new ArrayList<RectF>(0);

		mVecPaint = new Paint();
		mVecPaint.setColor(0xFF33B5E5);
		mVecPaint.setAntiAlias(true);
		mVecPaint.setStyle(Style.STROKE);
		mVecPaint.setStrokeWidth(0);

		mAxesPaint = new Paint(mVecPaint);
		mAxesPaint.setColor(Color.BLACK);
		mAxesPaint.setTextAlign(Align.CENTER);
		mAxesPaint.setTextSize(TEXT_SIZE);

		mShadingPaint = new Paint();
		mShadingPaint.setColor(0x30000000);
		mShadingPaint.setAntiAlias(true);
		mShadingPaint.setStyle(Style.FILL);

	}

	/**
	 * Set the list of points to be drawn. Use either this or setCursor to
	 * supply points to graph
	 */
	public void setVecList(ArrayList<Vec2> vecList) {

		mVecList = vecList;

		float[] floatarr = new float[vecList.size() * 2];

		int i = 0;
		for (Vec2 vec : vecList) {
			floatarr[i++] = vec.getX() * mScaleX;
			floatarr[i++] = mHeight - (vec.getY() * (mHeight - BOTTOM_PAD - TOP_PAD) / mMaxY);
		}

		mPath = makePath(floatarr);

		invalidate();
	}

	/**
	 * Set a cursor to read data from. Use either this or setVecList to supply
	 * points to the graph
	 * 
	 * @param cursor
	 *            Cursor with x in first column, y in second column
	 */
	public void setCursor(Cursor cursor) {

		mCursor = cursor;

		cursor.moveToFirst();

		int count = 0;
		while (!cursor.isAfterLast()) {
			if (!cursor.isNull(1)) {
				count++;
			}
			cursor.moveToNext();
		}
		cursor.moveToFirst();

		float[] floatarr = new float[count * 2];

		int i = 0;
		while (!cursor.isAfterLast()) {

			if (!cursor.isNull(1)) {

				floatarr[i++] = cursor.getInt(0) * mScaleX;
				floatarr[i++] = mHeight - ((float) cursor.getInt(1) * (mHeight - BOTTOM_PAD - TOP_PAD) / mMaxY);
			}
			cursor.moveToNext();
		}

		mPath = makePath(floatarr);

		invalidate();
	}

	/** Set largest value on x-axis. Must be initialised before view is drawn */
	public void setMaxX(int maxX) {
		mMaxX = maxX;

		mXAxisList.clear();
		mXLabelList.clear();

		// Add new axis
		mXAxisList.add(new Line(new Vec2(0, mHeight), new Vec2(mWidth, mHeight)));

		// Add ticks to axis
		float xtick = (float) mWidth / maxX;
		for (int i = 1; i <= maxX; i++) {
			mXAxisList.add(new Line(new Vec2((int) (i * xtick), mHeight), new Vec2((int) (i * xtick), mHeight + TICK_SIZE)));
		}
		// Add labels to x-axis
		if (maxX < 10) {
			for (int i = 1; i <= maxX; i++) {
				mXLabelList.add(new Label("" + i, new Vec2((int) (i * xtick), mHeight + TICK_SIZE + TEXT_SIZE)));
			}
		} else {
			for (int i = 5; i <= maxX; i = i + 5) {
				mXLabelList.add(new Label("" + i, new Vec2((int) (i * xtick), mHeight + TICK_SIZE + TEXT_SIZE)));
			}
		}
		mXLabelList.add(new Label("Days", new Vec2(mWidth / 2, mHeight + TICK_SIZE + 2 * TEXT_SIZE)));

		// If vertical lines needed, add them
		if (mVertLineWidth != 0) {
			setVerticalLines(mVertLineWidth);
		}
		// If vertical shaded needed, add this
		if (mShaded != null) {
			setVerticalShading(mShaded);
		}

		invalidate();
	}

	/** Set largest value on y-axis. Must be initialised before view is drawn */
	public void setMaxY(int maxY) {
		mMaxY = maxY;

		mYAxisList.clear();
		mYLabelList.clear();

		// Add axis
		mYAxisList.add(new Line(new Vec2(0, mHeight), new Vec2(0, TOP_PAD + BOTTOM_PAD)));
		if (DEBUG) Log.d(TAG, "Y-axis: ( 0 , " + mHeight + " ), ( 0 , " + (TOP_PAD + BOTTOM_PAD) + ")");

		// Add ticks to axis
		float ytick = (float) (mHeight - TOP_PAD - BOTTOM_PAD) / maxY;
		for (int i = 1; i <= maxY; i++) {
			mYAxisList.add(new Line(new Vec2(0, (int) (mHeight - (i * ytick))), new Vec2(-TICK_SIZE, (int) (mHeight - (i * ytick)))));
		}

		// Add labels to y-axis
		if (maxY < 10) {
			for (int i = 1; i <= maxY; i++) {
				mYLabelList.add(new Label("" + i, new Vec2(-2 * TICK_SIZE, (int) (mHeight - (i * ytick) + TEXT_SIZE / 2))));
			}
		} else {
			for (int i = 5; i <= maxY; i = i + 5) {
				mYLabelList.add(new Label("" + i, new Vec2(-2 * TICK_SIZE, (int) (mHeight - (i * ytick) + TEXT_SIZE / 2))));
			}
		}

		invalidate();
	}

	/**
	 * Set the graph to draw vertical lines on the graph from the x-axis to the
	 * top of the graph. Each line will be
	 * equally spaced <i>width</i> away from the previous
	 */
	public void setVerticalLines(int width) {

		if (width <= 0) {
			throw new IllegalArgumentException("Vertical lines must have a positive separation");
		}

		mVertLineWidth = width;

		// Find the number of lines to draw
		int num = (int) ((float) mMaxX / width);
		float xtick = (float) mWidth / mMaxX;

		if (mShaded == null) {
			// Add lines to the list
			for (int i = 1; i <= num; i++) {
				mXAxisList.add(new Line(new Vec2((int) (i * xtick * width), mHeight), new Vec2((int) (i * xtick * width), TOP_PAD + BOTTOM_PAD)));
			}
		}
	}

	/**
	 * Set the graph to shade the regions between vertical lines. The passed
	 * array indicated whether a region should be
	 * shaded or not. Null can be passed, this causes every other region to be
	 * shaded.
	 */
	public void setVerticalShading(boolean[] shaded) {

		mShaded = shaded;

		int num = (int) ((float) mMaxX / mVertLineWidth);

		// If shaded is null create a new boolean array with alternating values
		// and re-run
		if (shaded == null) {
			boolean[] newShaded = new boolean[num];
			newShaded[0] = false;
			for (int i = 1; i < num; i++) {
				newShaded[i] = !newShaded[i - 1];
			}
			setVerticalShading(newShaded);
			return;
		}

		mVertShadingList.clear();

		int max = num < shaded.length ? num : shaded.length;
		float xtick = (float) mWidth / mMaxX;

		for (int i = 0; i < max; i++) {
			if (shaded[i]) {
				RectF rect = new RectF(i * xtick * mVertLineWidth, mHeight, (i + 1) * xtick * mVertLineWidth, TOP_PAD + BOTTOM_PAD);
				mVertShadingList.add(rect);
			}
		}

	}

	private Path makePath(float[] arr) {
		Path p = new Path();
		if (arr.length > 2) {
			p.moveTo(arr[0], arr[1]);
			for (int i = 2; i < arr.length; i = i + 2) {
				p.lineTo(arr[i], arr[i + 1]);
			}
		}
		return p;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		canvas.save();
		// Move canvas before drawing axes, so when canvas restored
		// there is room to label axes
		canvas.translate(LEFT_PAD, -BOTTOM_PAD);

		if (DEBUG) Log.d(TAG, "Drawing x-axis");
		if (!mXAxisList.isEmpty()) {
			for (int i = 0; i < mXAxisList.size(); i++) {
				mXAxisList.get(i).draw(canvas, mAxesPaint);
			}
		}
		if (!mYAxisList.isEmpty()) {
			if (DEBUG) Log.d(TAG, "Drawing y-axis");
			for (int i = 0; i < mYAxisList.size(); i++) {
				mYAxisList.get(i).draw(canvas, mAxesPaint);
			}
		}

		if (!mVertShadingList.isEmpty()) {
			if (DEBUG) Log.d(TAG, "Drawing shading");
			for (int i = 0; i < mVertShadingList.size(); i++) {
				canvas.drawRect(mVertShadingList.get(i), mShadingPaint);
			}
		}

		if (DEBUG) Log.d(TAG, "Drawing data");
		if (mPath != null) canvas.drawPath(mPath, mVecPaint);

		if (!mXLabelList.isEmpty()) {
			if (DEBUG) Log.d(TAG, "Drawing labels");
			for (int i = 0; i < mXLabelList.size(); i++) {
				mXLabelList.get(i).draw(canvas, mAxesPaint);
			}
		}

		if (!mYLabelList.isEmpty()) {
			for (int i = 0; i < mYLabelList.size(); i++) {
				mYLabelList.get(i).draw(canvas, mAxesPaint);
			}
		}

		canvas.restore();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = measureWidth(widthMeasureSpec);
		int height = measureHeight(heightMeasureSpec, width);
		setMeasuredDimension(width, height);
	}

	/**
	 * Determines the width of this view
	 * 
	 * @param measureSpec A measureSpec packed into an int
	 * @return The width of the view, honoring constraints from measureSpec
	 */
	private int measureWidth(int measureSpec) {
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		if (specMode == MeasureSpec.EXACTLY) {
			// We were told how big to be
			result = specSize;
		} else {
			// Want width as wide as we can
			result = 20000;
			if (specMode == MeasureSpec.AT_MOST) {
				// Respect AT_MOST value if that was what is called for by
				// measureSpec
				result = Math.min(result, specSize);
			}
		}
		if (DEBUG) Log.d(TAG, "Measured width = " + result);
		return result;
	}

	/**
	 * Determines the height of this view
	 * 
	 * @param measureSpec A measureSpec packed into an int
	 * @return The height of the view, honoring constraints from measureSpec
	 */
	private int measureHeight(int measureSpec, int width) {
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		if (specMode == MeasureSpec.EXACTLY) {
			// We were told how big to be
			result = specSize;
		} else {
			// Want height to match given RATIO
			result = (int) (width * RATIO);
			if (specMode == MeasureSpec.AT_MOST) {
				// Respect AT_MOST value if that was what is called for by
				// measureSpec
				result = Math.min(result, specSize);
			}
		}
		if (DEBUG) Log.d(TAG, "Measured height = " + result);
		return result;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (DEBUG) Log.d(TAG, "Layout changed. New height: " + h + " New width: " + w);
		mHeight = h;
		mWidth = w - RIGHT_PAD - LEFT_PAD;

		mScaleX = (float) mWidth / mMaxX;

		if (mCursor != null) setCursor(mCursor);
		else if (mVecList.isEmpty()) setVecList(mVecList);

		setMaxX(mMaxX);
		setMaxY(mMaxY);

		invalidate();
	}
}

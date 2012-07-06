/*******************************************************************************
 * Nof1 Trails helper, making life easier for clinicians and patients in N of 1 trials.
 * Copyright (C) 2012  WMG, University of Warwick
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     John Lawson - initial API and implementation
 ******************************************************************************/
package uk.co.jwlawson.nof1.views;

import java.util.ArrayList;

import uk.co.jwlawson.nof1.Line;
import uk.co.jwlawson.nof1.Vec2;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * View to draw and display a graph showing the data over time. x = days, y = patient feedback
 * 
 * @author John Lawson
 * 
 */
public class GraphView extends View {
	
	/** List of each data point. */
	private ArrayList<Vec2> mVecList;
	
	/** List of lines to draw as axes */
	private ArrayList<Line> mAxesList;
	
	/** Paint to draw the points */
	private Paint mVecPaint;
	
	/** Paint to draw axes */
	private Paint mAxesPaint;
	
	/** Scale in x direction */
	private float mScaleX;
	
	/** Scale in y direction */
	private float mScaleY;
	
	private int mMaxX;
	
	private int mMaxY;
	
	private Context mContext;
	
	public GraphView(Context context) {
		this(context, null, 0);
	}
	
	public GraphView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public GraphView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		mContext = context;
		
		mVecList = new ArrayList<Vec2>();
		mAxesList = new ArrayList<Line>();
		
		mVecPaint = new Paint();
		mVecPaint.setColor(0xFF33B5E5);
		mVecPaint.setStyle(Style.STROKE);
		mVecPaint.setAntiAlias(true);
		mVecPaint.setStrokeWidth(2); // TODO check how this looks with the scaling
		
		mAxesPaint = new Paint(mVecPaint);
		mAxesPaint.setColor(Color.BLACK);
	}
	
	/** Set the list of points to be drawn */
	public void setVecList(ArrayList<Vec2> vecList) {
		mVecList = vecList;
	}
	
	/** Set largest value on x-axis. Must be initialised before view is drawn */
	public void setMaxX(int maxX) {
		mMaxX = maxX;
		mAxesList.add(new Line(new Vec2(0, 0), new Vec2(maxX, 0)));
		for (int i = 1; i < maxX; i++) {
			mAxesList.add(new Line(new Vec2(i, 0), new Vec2(i, -1)));
		}
		invalidate();
	}
	
	/** Set largest value on y-axis. Must be initialised before view is drawn */
	public void setMaxY(int maxY) {
		mMaxY = maxY;
		mAxesList.add(new Line(new Vec2(0, 0), new Vec2(0, maxY)));
		for (int i = 1; i < maxY; i++) {
			mAxesList.add(new Line(new Vec2(0, i), new Vec2(-1, i)));
		}
		invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		// TODO handle canvas having (0,0) at upper left corner >:(
		
		canvas.save();
		
		// canvas.scale(1 / mScaleX, 1 / mScaleY);
		
		// Move canvas before drawing axes, so when canvas restored
		// there is room to label axes
		canvas.translate(10, 10);
		
		for (Line line : mAxesList) {
			line.draw(canvas, mScaleX, mScaleY, mAxesPaint);
			Log.d("GraphView", "Drawing line");
			// line.draw(canvas, mAxesPaint);
		}
		
		for (Vec2 vec : mVecList) {
			canvas.drawPoint(vec.getX() * mScaleX, vec.getY() * mScaleY, mVecPaint);
			// canvas.drawPoint(vec.getX(), vec.getY(), mVecPaint);
		}
		
		canvas.restore();
		
		// TODO draw text labels
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		/* Want to scale the graph to fill the view
		 * The drawing of the data is initially restrained to pixels, so the scale will likely be a large number.
		 * 
		 */
		mScaleX = (float) w / mMaxX;
		mScaleY = (float) h / mMaxY;
		invalidate();
	}
}

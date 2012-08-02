/*******************************************************************************
 * Nof1 Trials helper, making life easier for clinicians and patients in N of 1 trials.
 * Copyright (C) 2012 John Lawson WMG, University of Warwick
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
package org.nof1trial.nof1.containers;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

/**
 * Simple class for string and drawing a line
 * 
 * @author John Lawson
 * 
 */
public class Line {

	private static final boolean DEBUG = false;

	private Vec2 start;
	private Vec2 end;

	public Line(Vec2 start, Vec2 end) {
		this.start = start;
		this.end = end;
	}

	public void draw(Canvas c, Paint paint) {
		if (DEBUG)
			Log.d("Line",
					"drawing line " + start.getX() + " , " + start.getY() + " , " + end.getX()
							+ " , " + end.getY());
		c.drawLine(start.getX(), start.getY(), end.getX(), end.getY(), paint);
	}

	public void draw(Canvas c, float scaleX, float scaleY, Paint paint) {
		if (DEBUG)
			Log.d("Line", "drawing line " + start.getX() * scaleX + " , " + start.getY() * scaleY
					+ " , " + end.getX() * scaleX + " , " + end.getY() * scaleY);
		c.drawLine(start.getX() * scaleX, start.getY() * scaleY, end.getX() * scaleX, end.getY()
				* scaleY, paint);
	}
}

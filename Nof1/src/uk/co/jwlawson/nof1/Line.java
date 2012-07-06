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
package uk.co.jwlawson.nof1;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Simple class for string and drawing a line
 * 
 * @author John Lawson
 * 
 */
public class Line {
	
	private Vec2 start;
	private Vec2 end;
	
	public Line(Vec2 start, Vec2 end) {
		this.start = start;
		this.end = end;
	}
	
	public void draw(Canvas c, Paint paint) {
		c.drawLine(start.getX(), start.getY(), end.getX(), end.getY(), paint);
	}
	
	public void draw(Canvas c, float scaleX, float scaleY, Paint paint) {
		c.drawLine(start.getX() * scaleX, start.getY() * scaleY, end.getX() * scaleX, end.getY() * scaleY, paint);
	}
}

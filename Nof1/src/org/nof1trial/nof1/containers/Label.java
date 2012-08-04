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
package org.nof1trial.nof1.containers;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

/**
 * @author lawson_j
 * 
 */
public class Label {

	private static final boolean DEBUG = false;

	private String name;
	private Vec2 position;

	public Label(String name, Vec2 position) {
		this.name = name;
		this.position = position;
	}

	public void draw(Canvas c, Paint paint) {
		c.drawText(name, position.getX(), position.getY(), paint);
	}

	public void draw(Canvas c, float scaleX, float scaleY, Paint paint) {
		c.drawText(name, position.getX() * scaleX, position.getY() * scaleY, paint);
		if (DEBUG)
			Log.d("Label", "Drawing " + name + position.getX() * scaleX + " , " + position.getY()
					* scaleY);
	}
}

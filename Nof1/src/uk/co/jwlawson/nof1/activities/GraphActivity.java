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
package uk.co.jwlawson.nof1.activities;

import java.util.ArrayList;

import uk.co.jwlawson.nof1.R;
import uk.co.jwlawson.nof1.Vec2;
import uk.co.jwlawson.nof1.views.GraphView;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockActivity;

/**
 * @author John Lawson
 * 
 */
public class GraphActivity extends SherlockActivity {

	public GraphActivity() {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.graph_layout);

		GraphView view = (GraphView) findViewById(R.id.graphView1);

		view.setMaxX(40);
		view.setMaxY(7);

		ArrayList<Vec2> list = new ArrayList<Vec2>();
		list.add(new Vec2(1, 1));
		list.add(new Vec2(4, 3));
		list.add(new Vec2(7, 2));
		list.add(new Vec2(10, 4));
		list.add(new Vec2(15, 1));
		list.add(new Vec2(19, 6));
		list.add(new Vec2(20, 7));
		list.add(new Vec2(23, 5));
		list.add(new Vec2(26, 3));
		list.add(new Vec2(30, 2));
		list.add(new Vec2(38, 0));

		view.setVecList(list);

		// TODO add some data points etc
		// TODO read data from database
	}

}

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
import android.widget.LinearLayout;

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

		LinearLayout scr = (LinearLayout) findViewById(R.id.graph_scroll);

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

		GraphView g = new GraphView(this);
		g.setMaxX(40);
		g.setMaxY(2);

		ArrayList<Vec2> l1 = new ArrayList<Vec2>();
		l1.add(new Vec2(1, 0));
		l1.add(new Vec2(4, 1));
		l1.add(new Vec2(7, 0));
		l1.add(new Vec2(8, 0));
		l1.add(new Vec2(9, 1));
		l1.add(new Vec2(12, 1));
		l1.add(new Vec2(15, 1));
		l1.add(new Vec2(18, 0));
		l1.add(new Vec2(23, 0));
		l1.add(new Vec2(25, 1));
		l1.add(new Vec2(28, 0));
		l1.add(new Vec2(31, 1));
		l1.add(new Vec2(35, 1));
		l1.add(new Vec2(37, 1));
		l1.add(new Vec2(40, 0));

		g.setVecList(l1);

		scr.addView(g);

		GraphView g1 = new GraphView(this);
		g1.setMaxX(40);
		g1.setMaxY(7);

		ArrayList<Vec2> l11 = new ArrayList<Vec2>();
		l11.add(new Vec2(1, 3));
		l11.add(new Vec2(4, 4));
		l11.add(new Vec2(7, 2));
		l11.add(new Vec2(8, 5));
		l11.add(new Vec2(9, 6));
		l11.add(new Vec2(12, 5));
		l11.add(new Vec2(15, 7));
		l11.add(new Vec2(18, 6));
		l11.add(new Vec2(23, 3));
		l11.add(new Vec2(25, 4));
		l11.add(new Vec2(28, 1));
		l11.add(new Vec2(31, 2));
		l11.add(new Vec2(35, 3));
		l11.add(new Vec2(37, 2));
		l11.add(new Vec2(40, 5));

		g1.setVecList(l11);

		scr.addView(g1);

		// TODO add some data points etc
		// TODO read data from database
	}

}

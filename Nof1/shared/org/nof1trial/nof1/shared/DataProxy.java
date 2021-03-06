/*******************************************************************************
 * Nof1 Trials helper, making life easier for clinicians and patients in N of 1 trials.
 * Copyright (C) 2012  John Lawson
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
package org.nof1trial.nof1.shared;

import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.ProxyForName;

import java.util.List;

/**
 * @author John Lawson
 * 
 */
@ProxyForName("org.nof1trial.nof1.server.entities.Data")
public interface DataProxy extends EntityProxy {

	public List<Integer> getQuestionData();

	public void setQuestionData(List<Integer> questionData);

	public String getComment();

	public void setComment(String comment);

	public int getDay();

	public void setDay(int day);

	public Long getTime();

	public void setTime(Long time);

}

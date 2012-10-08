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
@ProxyForName("org.nof1trial.nof1.server.entities.Questionnaire")
public interface QuestionnaireProxy extends EntityProxy {

	public List<String> getQuestionList();

	public void setQuestionList(List<String> questionList);

	public List<Integer> getTypeList();

	public void setTypeList(List<Integer> typeList);

	public List<String> getMinList();

	public void setMinList(List<String> minList);

	public List<String> getMaxList();

	public void setMaxList(List<String> maxList);

	public Long getExpiry();

	public void setExpiry(Long expiry);

	public Long getId();

	public Integer getVersion();

}

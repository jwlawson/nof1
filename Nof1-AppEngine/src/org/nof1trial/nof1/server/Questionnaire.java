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
package org.nof1trial.nof1.server;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Version;

/**
 * @author John Lawson
 * 
 */
@Entity
@SequenceGenerator(name = "seq", initialValue = 10000, allocationSize = 134)
public class Questionnaire {

	private static final Logger log = Logger.getLogger(Questionnaire.class.getName());

	public static Questionnaire findQuestionnaire(Long id) {
		if (id == null) {
			return null;
		}
		EntityManager em = entityManager();
		try {
			Questionnaire result = em.find(Questionnaire.class, id);
			return result;
		} finally {
			em.close();
		}
	}

	public static final EntityManager entityManager() {
		return EMF.get().createEntityManager();
	}

	public void persist() {
		EntityManager em = entityManager();
		try {
			em.persist(this);
			log.log(Level.INFO, "Saving data file: " + toString());
		} finally {
			em.close();
		}
	}

	public void remove() {
		EntityManager em = entityManager();
		try {
			Data attached = em.find(Data.class, this.id);
			em.remove(attached);
		} finally {
			em.close();
		}
	}

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq")
	private Long id;

	@Version
	@Column(name = "version")
	private Integer version;

	private List<String> questionList;

	private List<Integer> typeList;

	private List<String> minList;

	private List<String> maxList;

	public List<String> getQuestionList() {
		return questionList;
	}

	public void setQuestionList(List<String> questionList) {
		this.questionList = questionList;
	}

	public List<Integer> getTypeList() {
		return typeList;
	}

	public void setTypeList(List<Integer> typeList) {
		this.typeList = typeList;
	}

	public List<String> getMinList() {
		return minList;
	}

	public void setMinList(List<String> minList) {
		this.minList = minList;
	}

	public List<String> getMaxList() {
		return maxList;
	}

	public void setMaxList(List<String> maxList) {
		this.maxList = maxList;
	}

	public Long getId() {
		return id;
	}

	public Integer getVersion() {
		return version;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Questionnaire: ").append(id);
		sb.append(" Contains ").append(questionList.size()).append(" questions");

		return sb.toString();
	}
}

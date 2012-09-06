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
import javax.persistence.Query;
import javax.persistence.Version;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

/**
 * @author lawson_j
 * 
 */
@Entity
public class Data {

	private static final Logger log = Logger.getLogger(Data.class.getName());

	/**
	 * Required for requestfactory to link DataProxy and Data
	 * 
	 * @param id
	 * @return
	 */
	public static Data findData(Long id) {
		if (id == null) {
			return null;
		}
		EntityManager em = entityManager();
		try {
			Data data = em.find(Data.class, id);
			return data;
		} finally {
			em.close();
		}
	}

	/**
	 * Adds user email to data and searches Data files for patient config to add doctor email to data.
	 * 
	 * @param data Data file to save
	 * @return Saved data with added patient and doctor emails
	 */
	public static Data save(Data data) {
		data.patientEmail = getUserEmail();

		List<Config> list = Config.findConfigByPatient(getUserEmail(), 1);
		if (list.size() > 0) {
			data.doctorEmail = list.get(0).getDocEmail();
		}

		data.persist();
		return data;

	}

	@SuppressWarnings("unchecked")
	public static List<Data> findDataByEmail(String email, int maxResults) {
		EntityManager em = entityManager();

		try {
			Query query = em.createQuery("select x from Data x WHERE x.doctorEmail =:Email OR x.patientEmail =:Email");
			query.setMaxResults(maxResults);
			query.setParameter("Email", email);
			List<Data> resultList = query.getResultList();
			// force it to materialize
			resultList.size();
			return resultList;
		} finally {
			em.close();
		}
	}

	public static String getUserId() {
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();
		return user.getUserId();
	}

	public static String getUserEmail() {
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();
		return user.getEmail();
	}

	public static final EntityManager entityManager() {
		return EMF.get().createEntityManager();
	}

	/**
	 * Store data file in datastore
	 */
	public void persist() {
		EntityManager em = entityManager();
		try {
			em.persist(this);
			log.log(Level.INFO, "Saving data file: " + toString());
		} finally {
			em.close();
		}
	}

	/**
	 * Remove data file from Datastore
	 */
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
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Version
	@Column(name = "version")
	private Integer version;

	private List<Integer> questionData;

	private String comment;

	private String date;

	private Long time;

	private String patientEmail;

	private String doctorEmail;

	public List<Integer> getQuestionData() {
		return questionData;
	}

	public void setQuestionData(List<Integer> questionData) {
		this.questionData = questionData;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public Long getId() {
		return id;
	}

	public Integer getVersion() {
		return version;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Data file for patient ").append(patientEmail);
		sb.append(" with doctor ").append(doctorEmail);
		sb.append(" and responces to ").append(questionData.size()).append(" questions");

		return sb.toString();
	}

}

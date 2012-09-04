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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Query;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

/**
 * @author John Lawson
 * 
 */
@Entity
public class Config {

	@SuppressWarnings("unchecked")
	public static List<Config> findAllConfigs() {
		EntityManager em = entityManager();
		try {
			List<Config> list = em.createQuery("select x from Config x where x.patientEmail = :email or x.doctorEmail = :email")
					.setParameter("email", getUserEmail()).getResultList();
			// force to get all the employees
			list.size();
			return list;
		} finally {
			em.close();
		}
	}

	public static Config findConfig(Long id) {
		if (id == null) {
			return null;
		}
		EntityManager em = entityManager();
		try {
			Config config = em.find(Config.class, id);
			return config;
		} finally {
			em.close();
		}
	}

	@SuppressWarnings("unchecked")
	public static List<Config> findConfigByDoctor(String doctorEmail, int maxResults) {
		EntityManager em = entityManager();
		try {
			Query query = em.createQuery("select o from Employee o WHERE o.doctorEmail =:doctorEmail");
			query.setMaxResults(maxResults);
			query.setParameter("doctorEmail", doctorEmail);
			List<Config> resultList = query.getResultList();
			// force it to materialize
			resultList.size();
			return resultList;
		} finally {
			em.close();
		}
	}

	@SuppressWarnings("unchecked")
	public static List<Config> findConfigByPatient(String patientEmail, int maxResults) {
		EntityManager em = entityManager();
		try {
			Query query = em.createQuery("select o from Employee o WHERE o.patientEmail =:patientEmail");
			query.setMaxResults(maxResults);
			query.setParameter("patientEmail", patientEmail);
			List<Config> resultList = query.getResultList();
			// force it to materialise
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

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Version
	@Column(name = "version")
	private Integer version;

	@Size(min = 5)
	private String doctorEmail;

	private String patientEmail;

	private String pharmEmail;

	private String treatmentA;

	private String treatmentB;

	private String treatmentNotes;

	@NotNull
	private String startDate;

	private Long numberPeriods;

	private Long lengthPeriods;

	/**
	 * Store config file in datastore
	 */
	public void persist() {
		EntityManager em = entityManager();
		try {
			em.persist(this);
		} finally {
			em.close();
		}
	}

	/**
	 * Remove config file from Datastore
	 */
	public void remove() {
		EntityManager em = entityManager();
		try {
			Config attached = em.find(Config.class, this.id);
			em.remove(attached);
		} finally {
			em.close();
		}
	}

	public String getPatientEmail() {
		return patientEmail;
	}

	public void setPatientEmail(String patientEmail) {
		this.patientEmail = patientEmail;
	}

	public String getDocEmail() {
		return doctorEmail;
	}

	public void setDocEmail(String docEmail) {
		this.doctorEmail = docEmail;
	}

	public String getPharmEmail() {
		return pharmEmail;
	}

	public void setPharmEmail(String pharmEmail) {
		this.pharmEmail = pharmEmail;
	}

	public String getTreatmentA() {
		return treatmentA;
	}

	public void setTreatmentA(String treatmentA) {
		this.treatmentA = treatmentA;
	}

	public String getTreatmentB() {
		return treatmentB;
	}

	public void setTreatmentB(String treatmentB) {
		this.treatmentB = treatmentB;
	}

	public String getTreatmentNotes() {
		return treatmentNotes;
	}

	public void setTreatmentNotes(String treatmentNotes) {
		this.treatmentNotes = treatmentNotes;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public Long getNumberPeriods() {
		return numberPeriods;
	}

	public void setNumberPeriods(Long numberPeriods) {
		this.numberPeriods = numberPeriods;
	}

	public Long getLengthPeriods() {
		return lengthPeriods;
	}

	public void setLengthPeriods(Long lengthPeriods) {
		this.lengthPeriods = lengthPeriods;
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
		sb.append("Config for patient: ").append(patientEmail).append(", ");
		sb.append("Doctor: ").append(doctorEmail).append(", ");
		sb.append("Pharm: ").append(pharmEmail).append(", ");
		sb.append("Treatments: ").append(treatmentA).append(" and ").append(treatmentB).append(". Notes: ").append(treatmentNotes);
		sb.append("Starting: ").append(startDate).append(".");

		return sb.toString();

	}

}

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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
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
 * Config class. Holds data about a patient's app configuration. When the config is updated the server will send out
 * emails to both doctor and pharmacist.
 * 
 * @author John Lawson
 * 
 */
@Entity
public class Config {
	private static final Logger log = Logger.getLogger(Config.class.getName());

	/**
	 * Save the given config file to the data store
	 * 
	 * @param conf
	 * @return
	 */
	public static Config update(Config conf) {
		List<Config> list = findConfigByPatient(getUserEmail(), 1);
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);

		if (list.size() > 0) {
			// Patient config already in datastore, so update that
			log.info("Found old config for user: " + getUserEmail());

			Config old = list.get(0);
			conf = conf.mergeWithExisting(old);

		} else {

			if (conf.patientEmail == null) {
				conf.patientEmail = getUserEmail();
			}

			String dates = generateSchedule(conf);

			// Pharmacist email

			String msgStr = getPharmacistEmailBody(conf, dates);
			sendEmail(session, msgStr, conf.pharmEmail, "");
			log.info("Email sent to Pharmacist " + conf.pharmEmail);

			// Save config to data store
			conf.persist();
			log.info("Saving new config");

		}

		// Doctor Email

		String msgBody = getDoctorEmailBody(conf);
		sendEmail(session, msgBody, conf.doctorEmail, conf.doctorName);

		return conf;
	}

	private static void sendEmail(Session session, String msgBody, String toEmail, String toName) {
		try {
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress("john@nof1trial.org", "Nof1 Admin"));
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail, toName));
			msg.setSubject("Your Nof1 account has been activated");
			msg.setText(msgBody);
			Transport.send(msg);

		} catch (AddressException e) {
			log.warning("Email not sent, invalid address");
		} catch (MessagingException e) {
			log.warning("Email not sent, invalid message");
		} catch (UnsupportedEncodingException e) {
			log.warning("Email not sent, unsupported encoding");
		}
	}

	private static String getDoctorEmailBody(Config conf) {
		final StringBuilder sb = new StringBuilder("Thanks for choosing to use the Nof1 Trial app.");
		sb.append("\n").append("Patient name: ").append(conf.patientName);
		sb.append("\n").append("Treatment A: ").append(conf.treatmentA);
		sb.append("\n").append("Treatment B: ").append(conf.treatmentB);
		sb.append("\n").append("Treatment notes: ").append(conf.treatmentNotes);

		sb.append("\n\n").append("Start date:").append(conf.startDate);
		sb.append("\n").append("Number of treatment periods: ").append(conf.numberPeriods);
		sb.append("\n").append("Length of each treatment period: ").append(conf.lengthPeriods);

		sb.append("\n\n").append("Patient questions: ").append("\n");

		for (String str : conf.questionList) {
			sb.append(str).append("\n");
		}
		String msgBody = sb.toString();
		return msgBody;
	}

	private static String getPharmacistEmailBody(Config conf, String dates) {
		final StringBuilder sb = new StringBuilder();
		sb.append("Thank you for agreeing to provide the treatments for an upcoming Nof1 trial. ");
		sb.append("The information supplied below will hopefully be sufficient for you to make up the treatment plan for the trial. ");
		sb.append("Should you have any questions, please contact the doctor administering the trial, do not reply to this email, as any reply will not be read. ");
		sb.append("Remember that the Nof1 trial should be done double blinded, so the treatment schedule below should not be disclosed with the clinician. ");
		sb.append("\n\nNof1 App team");
		sb.append("\n\n").append("Doctor Name: ").append(conf.doctorName);
		sb.append("\n").append("Doctor email: ").append(conf.doctorEmail);
		sb.append("\n").append("Patient Name: ").append(conf.patientName);
		sb.append("\n\n").append("Treatment A: ").append(conf.treatmentA);
		sb.append("\n").append("Treatment B: ").append(conf.treatmentB);
		sb.append("\n\n").append("Treatment notes: ").append(conf.treatmentNotes);
		// sb.append("\n\n").append("Medicine taken ").append(5).append(" times a day");
		sb.append("\n\n");
		sb.append(dates);

		String msgStr = sb.toString();
		return msgStr;
	}

	private static String generateSchedule(Config conf) {
		// Generate schedule

		// Get basic information
		int number = conf.numberPeriods.intValue();
		int length = conf.lengthPeriods.intValue();
		String start = conf.startDate;

		// Set up fields
		Random rand = new Random();
		StringBuilder sb1 = new StringBuilder();
		StringBuilder dates = new StringBuilder();

		// Set calendar instance to start date
		Calendar cal = getCalendarFromDateString(start);

		/*
		 * For each pair of treatment periods, randomly assign A or B first
		 * Record data in the string builders.
		 * SB contains a short version either AB or BA
		 * dates contains a list of which dates go with which medicine
		 */
		for (int i = 0; i < number; i++) {
			if (rand.nextBoolean()) {
				sb1.append("AB");
				// Get start date
				dates.append(cal.get(Calendar.DAY_OF_MONTH)).append(" ").append(cal.get(Calendar.MONTH) + 1).append("/")
						.append(cal.get(Calendar.YEAR)).append(" - ");
				// Get end date
				cal.add(Calendar.DAY_OF_MONTH, length - 1);
				dates.append(cal.get(Calendar.DAY_OF_MONTH)).append("/").append(cal.get(Calendar.MONTH) + 1).append("/")
						.append(cal.get(Calendar.YEAR)).append(": ");
				dates.append(conf.treatmentA).append("\n");
				// add a day for next start date
				cal.add(Calendar.DAY_OF_MONTH, 1);

				dates.append(cal.get(Calendar.DAY_OF_MONTH)).append("/").append(cal.get(Calendar.MONTH) + 1).append("/")
						.append(cal.get(Calendar.YEAR)).append(" - ");
				cal.add(Calendar.DAY_OF_MONTH, length - 1);
				dates.append(cal.get(Calendar.DAY_OF_MONTH)).append("/").append(cal.get(Calendar.MONTH) + 1).append("/")
						.append(cal.get(Calendar.YEAR)).append(": ");
				dates.append(conf.treatmentB).append("\n");
				cal.add(Calendar.DAY_OF_MONTH, 1);
			} else {
				sb1.append("BA");
				dates.append(cal.get(Calendar.DAY_OF_MONTH)).append("/").append(cal.get(Calendar.MONTH) + 1).append("/")
						.append(cal.get(Calendar.YEAR)).append(" - ");
				cal.add(Calendar.DAY_OF_MONTH, length - 1);
				dates.append(cal.get(Calendar.DAY_OF_MONTH)).append("/").append(cal.get(Calendar.MONTH) + 1).append("/")
						.append(cal.get(Calendar.YEAR)).append(": ");
				dates.append(conf.treatmentB).append("\n");
				cal.add(Calendar.DAY_OF_MONTH, 1);

				dates.append(cal.get(Calendar.DAY_OF_MONTH)).append("/").append(cal.get(Calendar.MONTH) + 1).append("/")
						.append(cal.get(Calendar.YEAR)).append(" - ");
				cal.add(Calendar.DAY_OF_MONTH, length - 1);
				dates.append(cal.get(Calendar.DAY_OF_MONTH)).append("/").append(cal.get(Calendar.MONTH) + 1).append("/")
						.append(cal.get(Calendar.YEAR)).append(": ");
				dates.append(conf.treatmentA).append("\n");
				cal.add(Calendar.DAY_OF_MONTH, 1);
			}
			sb1.append("|");
		}

		conf.schedule = sb1.toString();
		log.info("Config schedule set up: " + conf.schedule);
		return dates.toString();
	}

	private static Calendar getCalendarFromDateString(String dateStr) {
		Calendar cal = Calendar.getInstance();
		String[] startArr = dateStr.split(":");
		int[] startInt = new int[] { Integer.parseInt(startArr[0]), Integer.parseInt(startArr[1]), Integer.parseInt(startArr[2]) };
		cal.set(startInt[2], startInt[1], startInt[0]);
		return cal;
	}

	public static void delete(Config conf) {
		conf.remove();
	}

	@SuppressWarnings("unchecked")
	public static List<Config> findAllConfigs() {
		EntityManager em = entityManager();
		try {
			List<Config> list = new ArrayList<Config>();

			// Need to do two queries, as DataStore throws error if "or" is used in query (for some stupid reason)
			List<Config> patientList = em.createQuery("select x from Config x where x.patientEmail = :email").setParameter("email", getUserEmail())
					.getResultList();
			patientList.size();
			List<Config> doctorList = em.createQuery("select x from Config x where x.doctorEmail = :email").setParameter("email", getUserEmail())
					.getResultList();
			doctorList.size();
			// force to get all the employees
			list.addAll(patientList);
			list.addAll(doctorList);
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
			Query query = em.createQuery("select o from Config o WHERE o.doctorEmail =:doctorEmail");
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
			Query query = em.createQuery("select o from Config o WHERE o.patientEmail =:patientEmail");
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

	private String doctorEmail;

	private String doctorName;

	private String patientEmail;

	private String patientName;

	private String pharmEmail;

	private String treatmentA;

	private String treatmentB;

	private String treatmentNotes;

	private String startDate;

	private Long numberPeriods;

	private Long lengthPeriods;

	private List<String> questionList;

	private String schedule;

	private Long endDate;

	/**
	 * Store config file in datastore
	 */
	public void persist() {
		EntityManager em = entityManager();
		try {
			em.persist(this);
			log.log(Level.INFO, "Saving config file: " + toString());
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

	/**
	 * Merge the data in the current Config instance with that of a Config in the data store. All data in the existing
	 * config will be overwritten and the changes persisted in the store.
	 * 
	 * @param existing Config instance already in the datastore
	 * @return An unmanaged instance of the existing config with updated values
	 */
	public Config mergeWithExisting(Config existing) {
		log.info("Merging config " + getId() + " with config " + existing.getId());
		EntityManager em = entityManager();
		try {
			Config old = em.find(Config.class, existing.getId());

			if (doctorEmail != null) old.doctorEmail = doctorEmail;
			if (doctorName != null) old.doctorName = doctorName;
			if (lengthPeriods != null) old.lengthPeriods = lengthPeriods;
			if (numberPeriods != null) old.numberPeriods = numberPeriods;
			if (patientName != null) old.patientName = patientName;
			if (pharmEmail != null) old.pharmEmail = pharmEmail;
			if (questionList != null) old.questionList = questionList;
			if (startDate != null) old.startDate = startDate;
			if (treatmentA != null) old.treatmentA = treatmentA;
			if (treatmentB != null) old.treatmentB = treatmentB;
			if (treatmentNotes != null) old.treatmentNotes = treatmentNotes;
			if (endDate != null) old.endDate = endDate;

			return old;
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

	public String getPatientName() {
		return patientName;
	}

	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}

	public String getDocEmail() {
		return doctorEmail;
	}

	public void setDocEmail(String docEmail) {
		this.doctorEmail = docEmail;
	}

	public String getDoctorName() {
		return doctorName;
	}

	public void setDoctorName(String doctorName) {
		this.doctorName = doctorName;
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

	public List<String> getQuestionList() {
		return questionList;
	}

	public void setQuestionList(List<String> questionList) {
		this.questionList = questionList;
	}

	public String getSchedule() {
		return schedule;
	}

	public void setSchedule(String schedule) {
		this.schedule = schedule;
	}

	public Long getEndDate() {
		return endDate;
	}

	public void setEndDate(Long endDate) {
		this.endDate = endDate;
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
		sb.append("Treatments: ").append(treatmentA).append(" and ").append(treatmentB).append(". Notes: ").append(treatmentNotes).append(", ");
		sb.append("Starting: ").append(startDate).append(".");

		return sb.toString();

	}

}

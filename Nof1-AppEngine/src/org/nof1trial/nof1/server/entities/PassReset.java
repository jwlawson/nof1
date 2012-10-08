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
package org.nof1trial.nof1.server.entities;


import java.io.UnsupportedEncodingException;
import java.util.Properties;
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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;

/**
 * @author John Lawson
 * 
 */
@Entity
public class PassReset {

	private static final Logger log = Logger.getLogger(Config.class.getName());

	public static PassReset findPassReset(Long id) {
		return new PassReset();
	}

	public static PassReset reset(PassReset pass) {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);

		String message = getResetMessage(pass);

		sendEmail(session, message, pass.getDocEmail(), "");

		return pass;
	}

	private static void sendEmail(Session session, String msgBody, String toEmail, String toName) {
		try {
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress("john@nof1trial.org", "Nof1 Admin"));
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail, toName));
			msg.setSubject("Nof1 app password reset");
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

	private static String getResetMessage(PassReset pass) {
		StringBuilder sb = new StringBuilder();
		sb.append("Your password has now been reset to a temporary password.");
		sb.append("\n\n\n");
		sb.append("New pass: ").append(pass.getPass());
		sb.append("\n\n");
		sb.append("Thanks");
		sb.append("Nof1 trial team");

		return sb.toString();
	}

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Version
	@Column(name = "version")
	private Integer version;

	private String pass;

	private String docEmail;

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public String getDocEmail() {
		return docEmail;
	}

	public void setDocEmail(String docEmail) {
		this.docEmail = docEmail;
	}

	public Long getId() {
		return id;
	}

	public Integer getVersion() {
		return version;
	}
}

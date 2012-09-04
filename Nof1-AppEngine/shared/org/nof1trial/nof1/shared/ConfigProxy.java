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

/**
 * @author John Lawson
 * 
 */
@ProxyForName(value = "Config")
public interface ConfigProxy extends EntityProxy {

	public String getPatientEmail();

	public void setPatientEmail(String patientEmail);

	public String getPatientName();

	public void setPatientName(String name);

	public String getDoctorEmail();

	public void setDoctorEmail(String docEmail);

	public String getDoctorName();

	public void setDoctorName(String doctorName);

	public String getPharmEmail();

	public void setPharmEmail(String pharmEmail);

	public String getTreatmentA();

	public void setTreatmentA(String treatmentA);

	public String getTreatmentB();

	public void setTreatmentB(String treatmentB);

	public String getTreatmentNotes();

	public void setTreatmentNotes(String treatmentNotes);

	public String getStartDate();

	public void setStartDate(String startDate);

	public Long getNumberPeriods();

	public void setNumberPeriods(Long numberPeriods);

	public Long getLengthPeriods();

	public void setLengthPeriods(Long lengthPeriods);

	public Long getId();

	public Integer getVersion();

}

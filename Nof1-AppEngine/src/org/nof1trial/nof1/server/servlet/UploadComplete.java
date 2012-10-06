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
package org.nof1trial.nof1.server.servlet;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreInputStream;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.gwt.core.client.GWT;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.ServerFailure;
import com.ibm.icu.util.Calendar;

import org.nof1trial.nof1.server.readers.CsvReader;
import org.nof1trial.nof1.server.readers.Reader;
import org.nof1trial.nof1.server.readers.XlsReader;
import org.nof1trial.nof1.server.readers.XlsxReader;
import org.nof1trial.nof1.shared.MyRequestFactory;
import org.nof1trial.nof1.shared.QuestionnaireProxy;
import org.nof1trial.nof1.shared.QuestionnaireRequest;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author John Lawson
 * 
 */
@SuppressWarnings("serial")
public class UploadComplete extends HttpServlet {

	private final BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

	private MyRequestFactory mRequestFactory;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
			IOException {

		final PrintWriter writer = resp.getWriter();

		Map<String, List<BlobKey>> keys = blobstoreService.getUploads(req);

		List<BlobKey> list = keys.get("file");
		if (list.size() < 1) {
			writer.append("<p>Problem finding the file.</p><p>Please try uploading again.</p>");
			return;
		}

		final BlobKey key = list.get(list.size() - 1);

		String filename = getFilename(key);

		writer.append("<p>Parsing uploaded file ").append(filename).append("</p>");

		Reader fileReader;
		if (filename.endsWith(".xls")) {
			fileReader = new XlsReader();
		} else if (filename.endsWith(".xlsx")) {
			fileReader = new XlsxReader();
		} else if (filename.endsWith(".csv")) {
			fileReader = new CsvReader();
		} else {
			writer.append("<p>File in incorrect format</p>");
			blobstoreService.delete(key);
			return;
		}

		BlobstoreInputStream stream = new BlobstoreInputStream(key);
		fileReader.setInputStream(stream);

		ArrayList<String> questions = new ArrayList<String>();
		ArrayList<Integer> types = new ArrayList<Integer>();
		ArrayList<String> mins = new ArrayList<String>();
		ArrayList<String> maxs = new ArrayList<String>();

		writer.append("<table border=\"1\">").append("<tr>");
		writer.append("<th>Question</th>");
		writer.append("<th>Type</th>");
		writer.append("<th>Min</th>");
		writer.append("<th>Max</th></tr>");

		while (fileReader.hasNext()) {
			writer.append("<tr>");

			String question = fileReader.getQuestion();
			questions.add(question);
			writer.append("<td>").append(question).append("</td>");

			int type = fileReader.getType();
			types.add(type);
			writer.append("<td>").append(String.valueOf(type)).append("</td>");

			String min = "";
			String max = "";
			if (type == 0) {
				min = fileReader.getMin();
				max = fileReader.getMax();
			}
			mins.add(min);
			maxs.add(max);
			writer.append("<td>").append(min).append("</td>");
			writer.append("<td>").append(max).append("</td>");

			writer.append("</tr>");
		}
		writer.append("</table>");

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, 60);
		long expiry = cal.getTimeInMillis();
		writer.append("<p>This Questionnaire will be kept for 60 days.</p>");

		final EventBus eventBus = new SimpleEventBus();
		mRequestFactory = GWT.create(MyRequestFactory.class);
		mRequestFactory.initialize(eventBus);

		QuestionnaireRequest request = mRequestFactory.questionnaireRequest();
		QuestionnaireProxy proxy = request.create(QuestionnaireProxy.class);

		proxy.setQuestionList(questions);
		proxy.setTypeList(types);
		proxy.setMinList(mins);
		proxy.setMaxList(maxs);
		proxy.setExpiry(expiry);

		request.save(proxy).fire(new Receiver<QuestionnaireProxy>() {

			@Override
			public void onSuccess(QuestionnaireProxy response) {
				writer.append("<p>Questionnaire saved successfully with identifier ");
				writer.append(String.valueOf(response.getId()));
				writer.append("</p>");

				blobstoreService.delete(key);
			}

			@Override
			public void onFailure(ServerFailure error) {
				writer.append("<p>Problem saving questionnaire, try again later.</p>");

				blobstoreService.delete(key);
			}
		});

	}

	private String getFilename(BlobKey key) {
		BlobInfoFactory factory = new BlobInfoFactory();
		BlobInfo info = factory.loadBlobInfo(key);
		String filename = info.getFilename();
		return filename;
	}

}

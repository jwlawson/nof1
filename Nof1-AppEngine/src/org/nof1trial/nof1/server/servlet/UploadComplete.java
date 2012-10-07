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
import com.google.appengine.api.blobstore.BlobstoreFailureException;
import com.google.appengine.api.blobstore.BlobstoreInputStream;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;

import org.nof1trial.nof1.server.Questionnaire;
import org.nof1trial.nof1.server.readers.CsvReader;
import org.nof1trial.nof1.server.readers.Reader;
import org.nof1trial.nof1.server.readers.XlsReader;
import org.nof1trial.nof1.server.readers.XlsxReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

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
	private static final Logger log = Logger.getLogger(UploadComplete.class.getName());

	private final BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		log.info("Starting post method");

		Map<String, List<BlobKey>> keys = blobstoreService.getUploads(req);

		List<BlobKey> list = keys.get("file");
		keys.clear();

		if (list.size() < 1) {
			log.info("Blobkey not found");
			resp.sendRedirect("/uploadcomplete?id=Error");
			return;
		}
		final BlobKey key = list.get(list.size() - 1);

		String filename = getFilename(key);
		log.info("got blobkey and filename " + filename);

		Reader fileReader;
		if (filename.endsWith(".xls")) {
			fileReader = new XlsReader();
		} else if (filename.endsWith(".xlsx")) {
			fileReader = new XlsxReader();
		} else if (filename.endsWith(".csv")) {
			fileReader = new CsvReader();
		} else {
			resp.sendRedirect("/uploadComplete?id=Error");
			blobstoreService.delete(key);
			return;
		}

		BlobstoreInputStream stream = new BlobstoreInputStream(key);
		fileReader.setInputStream(stream);

		ArrayList<String> questions = new ArrayList<String>();
		ArrayList<Integer> types = new ArrayList<Integer>();
		ArrayList<String> mins = new ArrayList<String>();
		ArrayList<String> maxs = new ArrayList<String>();

		while (fileReader.hasNext()) {
			fileReader.moveToNext();

			String question = fileReader.getQuestion();
			questions.add(question);

			int type = fileReader.getType();
			types.add(type);

			String min = "";
			String max = "";
			if (type == 0) {
				min = fileReader.getMin();
				max = fileReader.getMax();
			}
			mins.add(min);
			maxs.add(max);
		}
		fileReader.closeStream();

		try {
			blobstoreService.delete(key);
		} catch (BlobstoreFailureException e) {
			log.info("Got blobstore error, waiting 1sec");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
			}
			try {
				stream.close();
			} catch (IOException ignored) {
			}
			blobstoreService.delete(key);
		}

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, 60);
		long expiry = cal.getTimeInMillis();

		Questionnaire ques = new Questionnaire();

		ques.setQuestionList(questions);
		ques.setTypeList(types);
		ques.setMinList(mins);
		ques.setMaxList(maxs);
		ques.setExpiry(expiry);

		ques = ques.persist();

		log.info("Ques saved with id: " + ques.getId());
		resp.sendRedirect("/uploadcomplete?id=" + ques.getId());
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
			IOException {
		log.info("Get method started");

		String id = req.getParameter("id");
		log.info("Getting id: " + id);
		resp.setHeader("Content-Type", "text/html");
		resp.getWriter().println(id);

	}

	private String getFilename(BlobKey key) {
		BlobInfoFactory factory = new BlobInfoFactory();
		BlobInfo info = factory.loadBlobInfo(key);
		String filename = info.getFilename();
		return filename;
	}

}

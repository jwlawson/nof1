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
package org.nof1trial.nof1.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;

import org.nof1trial.nof1.client.services.UserUploadService;
import org.nof1trial.nof1.client.services.UserUploadServiceAsync;
import org.nof1trial.nof1.shared.MyRequestFactory;

/**
 * @author John
 * 
 */
public class UploadEntryPoint implements EntryPoint {

	private MyRequestFactory mRequestFactory;

	private FlowPanel vPanel;
	private Button btnSubmit;
	private FormPanel uploadForm;
	private FileUpload fileUpload;
	private final UserUploadServiceAsync uploadService = GWT.create(UserUploadService.class);

	@Override
	public void onModuleLoad() {

		final EventBus eventBus = new SimpleEventBus();

		mRequestFactory = GWT.create(MyRequestFactory.class);
		mRequestFactory.initialize(eventBus);

		createUi();
	}

	private void createUi() {
		vPanel = new FlowPanel();
		vPanel.setStyleName("vPanel");

		RootPanel rootPanel = RootPanel.get();
		rootPanel.add(vPanel);

		FlowPanel headerPanel = new FlowPanel();
		headerPanel.setStyleName("headerPanel");
		vPanel.add(headerPanel);

		// Nof1 logo header
		Image imgHeader = new Image("images/Nof1-Icon.png");
		imgHeader.setStyleName("gwt-Image-header");
		imgHeader.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				Window.Location.assign("http://www.nof1trial.org");
			}
		});
		headerPanel.add(imgHeader);

		// Header text
		Label lblHeader = new Label("User data");
		lblHeader.setStyleName("gwt-Label-header");
		headerPanel.add(lblHeader);

		// Need spacer to clear the "float: left;" css attribute
		SimplePanel simplePanel = new SimplePanel();
		simplePanel.setStyleName("spacer");
		headerPanel.add(simplePanel);

		HTML htmlNewHtml = new HTML(
				"<p>Choose a questionnaire file to upload.</p>\r\n\r\n"
						+ "<p>\r\nThis should be a spreadsheet file (.xls, .xlsx or .csv) which contains 4 columns:\r\n"
						+ "</br>\t- question\r\n"
						+ "</br>\t- question type (0 = scale, 1 = number, 2 = checkbox)\r\n"
						+ "</br>\t- min hint (scale type only)\r\n"
						+ "</br>\t- max hint (scale type only)\r\n</p>", true);
		vPanel.add(htmlNewHtml);

		uploadForm = new FormPanel();
		uploadForm.setEncoding(FormPanel.ENCODING_MULTIPART);
		uploadForm.setMethod(FormPanel.METHOD_POST);

		vPanel.add(uploadForm);

		fileUpload = new FileUpload();
		uploadForm.setWidget(fileUpload);
		fileUpload.setSize("100%", "100%");
		fileUpload.setName("file");

		btnSubmit = new Button("Loading...");
		btnSubmit.setEnabled(false);
		vPanel.add(btnSubmit);

		final HTML html = new HTML("", true);
		vPanel.add(html);

		uploadForm.addSubmitCompleteHandler(new SubmitCompleteHandler() {

			@Override
			public void onSubmitComplete(SubmitCompleteEvent event) {
				String result = event.getResults().trim();
				if (isNumber(result)) {
					html.setHTML("<p>Saved with identifier: "
							+ result
							+ ".</p> <p>This can be used to download the questionnaire through the android app.</p>"
							+ "<p>The questionnaire will be available for the next 60 days.</p>");
				} else {
					html.setHTML("<p>Error saving the file. Please try again.</p><p>" + result
							+ "</p>");
				}
				getUploadUrl();
			}
		});

		getUploadUrl();
	}

	private boolean isNumber(String str) {
		try {
			@SuppressWarnings("unused")
			int dummy = Integer.parseInt(str);
			return true;
		} catch (NumberFormatException ignored) {
			return false;
		}
	}

	private void getUploadUrl() {
		uploadService.getBlobstoreUploadUrl(new AsyncCallback<String>() {

			@Override
			public void onFailure(Throwable caught) {
				btnSubmit.setText("Error");
			}

			@Override
			public void onSuccess(String result) {
				uploadForm.setAction(result);
				btnSubmit.setText("Submit");
				btnSubmit.addClickHandler(new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						if (isFileValid()) {
							btnSubmit.setText("Uploading...");
							btnSubmit.setEnabled(false);
							uploadForm.submit();
						} else {
							Window.alert("File needs to be .xls, .xlsx or .csv");
						}
					}

				});
				btnSubmit.setEnabled(true);
			}

		});
	}

	private boolean isFileValid() {
		return fileUpload.getFilename().endsWith(".csv")
				|| fileUpload.getFilename().endsWith(".xls")
				|| fileUpload.getFilename().endsWith(".xlsx");
	}
}

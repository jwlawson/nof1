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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.nof1trial.nof1.shared.ConfigProxy;
import org.nof1trial.nof1.shared.DataProxy;
import org.nof1trial.nof1.shared.MyRequestFactory;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.ServerFailure;

/**
 * Page for showing patient data linked to the current user.
 * 
 * @author John Lawson
 * 
 */
public class RequestEntryPoint implements EntryPoint {

	private MyRequestFactory mRequestFactory;

	private GwtRequestFactory mGwtRequestFactory;

	private FlowPanel vPanel;

	private Label lblLoggedIn;

	private Button btnLogout;

	private FlowPanel loadingPanel;

	@Override
	public void onModuleLoad() {
		final EventBus eventBus = new SimpleEventBus();

		mGwtRequestFactory = GWT.create(GwtRequestFactory.class);
		mGwtRequestFactory.initialize(eventBus);

		findUser();

		mRequestFactory = GWT.create(MyRequestFactory.class);
		mRequestFactory.initialize(eventBus);

		findConfigs();

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

		// User login label
		lblLoggedIn = new Label("Finding user info...");
		vPanel.add(lblLoggedIn);

		// User log out button
		btnLogout = new Button("Logout");
		btnLogout.setEnabled(false);
		btnLogout.setWidth("75px");
		vPanel.add(btnLogout);

		loadingPanel = new FlowPanel();
		loadingPanel.setStyleName("loading-frame");
		rootPanel.add(loadingPanel);

		Label lblLoading = new Label("Loading user data...");
		lblLoading.setStyleName("gwt-Label-loading");
		loadingPanel.add(lblLoading);

		Image imgLoading = new Image("images/spinner.gif");
		loadingPanel.add(imgLoading);
		imgLoading.setStyleName("gwt-Image-loader");
	}

	private void findUser() {
		mGwtRequestFactory.userRequest().get().fire(new Receiver<GaeUserProxy>() {

			@Override
			public void onSuccess(final GaeUserProxy user) {

				if (user.getEmail() != null && user.getEmail().length() != 0) {
					lblLoggedIn.setText("Logged in as: " + user.getEmail());

					btnLogout.setEnabled(true);
					btnLogout.addClickHandler(new ClickHandler() {

						@Override
						public void onClick(ClickEvent event) {
							Window.Location.assign(user.getLogoutUrl());
						}
					});
				} else {
					lblLoggedIn.setText("Not logged in");
					btnLogout.setText("Login");
					btnLogout.addClickHandler(new ClickHandler() {

						@Override
						public void onClick(ClickEvent event) {
							Window.Location.assign(user.getLoginUrl());
						}
					});
				}
			}

			@Override
			public void onFailure(ServerFailure error) {
				lblLoggedIn.setText("Error!");
				super.onFailure(error);
			}
		});
	}

	private void findConfigs() {
		mRequestFactory.configRequest().findAllConfigs().fire(new Receiver<List<ConfigProxy>>() {

			@Override
			public void onSuccess(List<ConfigProxy> confList) {
				RootPanel.get().remove(loadingPanel);

				for (ConfigProxy conf : confList) {
					addConfig(conf);
				}
			}
		});
	}

	private void addConfig(final ConfigProxy conf) {

		final PatientData data = new PatientData(conf, null);

		ConfigTable cTab = new ConfigTable();
		cTab.setPatientData(data);

		DecoratorPanel cTabPanel = new DecoratorPanel();
		cTabPanel.add(cTab);
		cTabPanel.setWidth("auto");
		vPanel.add(cTabPanel);

		final DataTable table = new DataTable();
		table.setText(0, 0, "Finding data for patient " + conf.getPatientName());

		DecoratorPanel dTabPanel = new DecoratorPanel();
		dTabPanel.add(table);
		vPanel.add(dTabPanel);

		// Get data associated with the patient in config
		mRequestFactory.dataRequest().findDataByEmail(conf.getPatientEmail(), Integer.MAX_VALUE).fire(new Receiver<List<DataProxy>>() {

			@Override
			public void onSuccess(List<DataProxy> dataList) {
				Collections.sort(dataList, DATAPROXY_COMPARATOR);
				data.setData(dataList);
				table.setPatientData(data);
			}
		});

	}

	/** Comparator that sorts data by the time it is collected */
	private static final Comparator<DataProxy> DATAPROXY_COMPARATOR = new Comparator<DataProxy>() {

		@Override
		public int compare(DataProxy d0, DataProxy d1) {
			int result = 0;
			if (d0.getTime() == d1.getTime()) {
				result = 0;
			} else if (d0.getTime() > d1.getTime()) {
				result = 1;
			} else {
				result = -1;
			}
			return result;
		}
	};
}

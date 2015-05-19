/**
 * Mule Anypoint Template
 * Copyright (c) MuleSoft, Inc.
 * All rights reserved.  http://www.mulesoft.com
 */

package org.mule.templates.integration;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Rule;
import org.mule.api.config.MuleProperties;
import org.mule.context.notification.NotificationException;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.tck.probe.PollingProber;
import org.mule.tck.probe.Prober;
import org.mule.templates.test.utils.ListenerProbe;
import org.mule.templates.test.utils.PipelineSynchronizeListener;

/**
 * This is the base test class for Anypoint Templates integration tests.
 * 
 * @author damiansima
 */
public abstract class AbstractTemplateTestCase extends FunctionalTestCase {
	private static final String MAPPINGS_FOLDER_PATH = "./mappings";
	private static final String TEST_FLOWS_FOLDER_PATH = "./src/test/resources/flows/";
	private static final String MULE_DEPLOY_PROPERTIES_PATH = "./src/main/app/mule-deploy.properties";
	private static final Logger LOGGER = LogManager.getLogger(AbstractTemplateTestCase.class);

	protected static final String TEMPLATE_NAME = "contact-broadcast";
	protected static final String POLL_FLOW_NAME = "triggerFlow";

	protected final Prober pollProber = new PollingProber(60000, 1000l);
	protected final PipelineSynchronizeListener pipelineListener = new PipelineSynchronizeListener(POLL_FLOW_NAME);

	@Rule
	public DynamicPort port = new DynamicPort("http.port");

	@Override
	protected String getConfigResources() {
		String resources = "";
		try {
			Properties props = new Properties();
			props.load(new FileInputStream(MULE_DEPLOY_PROPERTIES_PATH));
			resources = props.getProperty("config.resources");
		} catch (Exception e) {
			throw new IllegalStateException(
					"Could not find mule-deploy.properties file on classpath. Please add any of those files or override the getConfigResources() method to provide the resources by your own.");
		}

		return resources + getTestFlows();
	}

	protected String getTestFlows() {
		StringBuilder resources = new StringBuilder();

		File testFlowsFolder = new File(TEST_FLOWS_FOLDER_PATH);
		File[] listOfFiles = testFlowsFolder.listFiles();
		if (listOfFiles != null) {
			for (File f : listOfFiles) {
				if (f.isFile() && f.getName().endsWith("xml")) {
					resources.append(",").append(TEST_FLOWS_FOLDER_PATH).append(f.getName());
				}
			}
			return resources.toString();
		} else {
			return "";
		}
	}

	@Override
	protected Properties getStartUpProperties() {
		Properties properties = new Properties(super.getStartUpProperties());

		String pathToResource = MAPPINGS_FOLDER_PATH;
		File graphFile = new File(pathToResource);

		properties.put(MuleProperties.APP_HOME_DIRECTORY_PROPERTY, graphFile.getAbsolutePath());

		return properties;
	}

	protected void registerListeners() throws NotificationException {
		muleContext.registerListener(pipelineListener);
	}

	protected void waitForPollToRun() {
		LOGGER.info("Waiting for poll to run ones...");
		pollProber.check(new ListenerProbe(pipelineListener));
		LOGGER.info("Poll flow done");
	}

	protected String buildUniqueName(String templateName, String name) {
		String timeStamp = new Long(new Date().getTime()).toString();

		StringBuilder builder = new StringBuilder();
		builder.append(name);
		builder.append(templateName);
		builder.append(timeStamp);

		return builder.toString();
	}

	protected String buildUniqueEmail(String contact) {
		String server = "memail";

		StringBuilder builder = new StringBuilder();
		builder.append(TEMPLATE_NAME + contact);
		builder.append("@");
		builder.append(server);
		builder.append(".com");

		return builder.toString();
	}
}

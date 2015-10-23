/**
 * Mule Anypoint Template
 * Copyright (c) MuleSoft, Inc.
 * All rights reserved.  http://www.mulesoft.com
 */

package org.mule.templates.integration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mule.MessageExchangePattern;
import org.mule.api.MuleEvent;
import org.mule.modules.salesforce.bulk.EnrichedSaveResult;
import org.mule.processor.chain.SubflowInterceptingChainLifecycleWrapper;
import org.mule.templates.builders.SfdcObjectBuilder;

import com.mulesoft.module.batch.BatchTestHelper;

/**
 * The objective of this class is to validate the correct behavior of the flows
 * for this Anypoint Tempalte that make calls to external systems.
 * 
 */
public class BusinessLogicIT extends AbstractTemplateTestCase {

	protected static final int TIMEOUT = 240;
	private static final String POLL_FLOW_NAME = "triggerFlow";
	private static final String ACCOUNT_NAME = "Contact Broadcast Test Name";
	
	private BatchTestHelper helper;
	private Map<String, Object> account;
	private Map<String, Object> contact;
	private SubflowInterceptingChainLifecycleWrapper createAccountInSalesforceFlow;
	private SubflowInterceptingChainLifecycleWrapper createContactInSalesforce;
	private SubflowInterceptingChainLifecycleWrapper selectAccountFromSiebelFlow;
	private SubflowInterceptingChainLifecycleWrapper selectContactFromSiebelFlow;
	private SubflowInterceptingChainLifecycleWrapper deleteAccountFromSalesforceFlow;
	private SubflowInterceptingChainLifecycleWrapper deleteAccountsFromSiebelFlow;
	private SubflowInterceptingChainLifecycleWrapper deleteContactsFromSiebelFlow;
	
	@BeforeClass
	public static void init() {
		System.setProperty("poll.frequencyMillis", "10000");
		System.setProperty("account.sync.policy", "syncAccount");
		System.setProperty("poll.startDelayMillis", "20000");
		System.setProperty("watermark.default.expression",
				"#[groovy: new Date(System.currentTimeMillis() - 10000).format(\"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'\", TimeZone.getTimeZone('UTC'))]");
	}

	@Before
	public void setUp() throws Exception {
		stopFlowSchedulers(POLL_FLOW_NAME);
		registerListeners();
		helper = new BatchTestHelper(muleContext);

		createAccountInSalesforceFlow = getSubFlow("createAccountInSalesforce");
		createAccountInSalesforceFlow.initialise();

		createContactInSalesforce = getSubFlow("createContactInSalesforce");
		createContactInSalesforce.initialise();

		selectAccountFromSiebelFlow = getSubFlow("selectAccountFromSiebel");
		selectAccountFromSiebelFlow.initialise();
		
		selectContactFromSiebelFlow = getSubFlow("selectContactFromSiebel");
		selectContactFromSiebelFlow.initialise();
		
		deleteAccountFromSalesforceFlow = getSubFlow("deleteAccountFromSalesforce");
		deleteAccountFromSalesforceFlow.initialise();

		deleteAccountsFromSiebelFlow = getSubFlow("deleteAccountFromSiebel");
		deleteAccountsFromSiebelFlow.initialise();

		deleteContactsFromSiebelFlow = getSubFlow("deleteContactSiebel");
		deleteContactsFromSiebelFlow.initialise();

		// prepare test data
		account = createSalesforceAccount();
		createSalesforceAccount(account);
		
		contact = createContact();
		contact.put("AccountId", account.get("Id").toString());
		createSalesforceContact(contact);
	}

	@After
	public void tearDown() throws Exception {
		stopFlowSchedulers(POLL_FLOW_NAME);

		// delete previously created accounts and contacts from Siebel and Salesforce by matching ID
		deleteAccountFromSalesforce(account);
		deleteAccountsFromSiebel(account);
		deleteContactsFromSiebel(contact);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testMainFlow() throws Exception {
		
		// Run poll and wait for it to run
		runSchedulersOnce(POLL_FLOW_NAME);
		waitForPollToRun();

		// Wait for the batch job executed by the poll flow to finish
		helper.awaitJobTermination(TIMEOUT * 1000, 500);
		helper.assertJobWasSuccessful();
		
		// Execute selectAccountFromSiebel sublow
		MuleEvent event = selectAccountFromSiebelFlow.process(getTestEvent(account, MessageExchangePattern.REQUEST_RESPONSE));
		List<Map<String, Object>> payload = (List<Map<String, Object>>) event.getMessage().getPayload();

		// fill SiebelId
		for (Map<String, Object> acc : payload){
			account.put("SiebelId", acc.get("Id"));
		}

		// Account previously created in Salesforce should be present in Siebel
		Assert.assertEquals("The account should have been sync", 1, payload.size());
		Assert.assertEquals("The account name should match", account.get("Name"), payload.get(0).get("Name"));

		// Execute selectContactFromSiebel sublow
		event = selectContactFromSiebelFlow.process(getTestEvent(contact, MessageExchangePattern.REQUEST_RESPONSE));
		payload = (List<Map<String, Object>>) event.getMessage().getPayload();

		// fill SiebelId
		for (Map<String, Object> c : payload){
			contact.put("SiebelId", c.get("Id"));
		}

		// Contact previously created in Salesforce should be present in Siebel
		Assert.assertEquals("The contact should have been sync", 1, payload.size());
		Assert.assertEquals("The contact name should match", contact.get("Email"), payload.get(0).get("Email Address"));
}

	@SuppressWarnings("unchecked")
	private void createSalesforceAccount(final Map<String, Object> account) throws Exception {

		List<Map<String, Object>> input = new ArrayList<Map<String, Object>>();
		input.add(account);

		final MuleEvent event = createAccountInSalesforceFlow.process(getTestEvent(input, MessageExchangePattern.REQUEST_RESPONSE));
		final List<EnrichedSaveResult> result = (List<EnrichedSaveResult>) event.getMessage().getPayload();

		for (EnrichedSaveResult item : result) {
			account.put("Id", item.getId());
			account.put("LastModifiedDate", item.getPayload().getField("LastModifiedDate"));
		}
	}

	@SuppressWarnings("unchecked")
	private void createSalesforceContact(final Map<String, Object> contact) throws Exception {

		List<Map<String, Object>> input = new ArrayList<Map<String, Object>>();
		input.add(contact);

		final MuleEvent event = createContactInSalesforce.process(getTestEvent(input, MessageExchangePattern.REQUEST_RESPONSE));
		final List<EnrichedSaveResult> result = (List<EnrichedSaveResult>) event.getMessage().getPayload();

		for (EnrichedSaveResult item : result) {
			contact.put("Id", item.getId());
			contact.put("LastModifiedDate", item.getPayload().getField("LastModifiedDate"));
		}
	}

	private void deleteAccountFromSalesforce(final Map<String, Object> acc) throws Exception {

		List<Object> idList = new ArrayList<Object>();
		idList.add(acc.get("Id"));

		deleteAccountFromSalesforceFlow.process(getTestEvent(idList, MessageExchangePattern.REQUEST_RESPONSE));
	}

	private void deleteAccountsFromSiebel(final Map<String, Object> acc) throws Exception {

		List<String> idList = new ArrayList<String>();
		idList.add(acc.get("SiebelId").toString());
		
		deleteAccountsFromSiebelFlow.process(getTestEvent(idList, MessageExchangePattern.REQUEST_RESPONSE));
	}

	private void deleteContactsFromSiebel(final Map<String, Object> cont) throws Exception {

		List<String> idList = new ArrayList<String>();
		idList.add(cont.get("SiebelId").toString());
		
		deleteContactsFromSiebelFlow.process(getTestEvent(idList, MessageExchangePattern.REQUEST_RESPONSE));
	}

	private Map<String, Object> createSalesforceAccount() {
		final SfdcObjectBuilder builder = new SfdcObjectBuilder();
		final Map<String, Object> account = builder
				.with("NumberOfEmployees", 1231)
				.with("AccountNumber", "123123")
				.with("Name", ACCOUNT_NAME + System.currentTimeMillis()).build();
		
		return account;
	}

	protected Map<String, Object> createContact() {
		long sequence = System.currentTimeMillis();
		return SfdcObjectBuilder
				.aContact()
				.with("FirstName", "Petr")
				.with("LastName", buildUniqueName(TEMPLATE_NAME, "LastName_"))
				.with("Email", buildUniqueEmail("some.email." + sequence))
				.with("Description", "Some fake description")
				.with("MailingCity", "Denver")
				.with("MailingCountry", "US")
				.with("MobilePhone", "123456789")
				.with("Department", "department_" + sequence)
				.with("Phone", "123456789")
				.with("Title", "Dr")
				.build();
	}
}


# Anypoint Template: Salesforce to Siebel Contact Broadcast

# License Agreement
This template is subject to the conditions of the 
<a href="https://s3.amazonaws.com/templates-examples/AnypointTemplateLicense.pdf">MuleSoft License Agreement</a>.
Review the terms of the license before downloading and using this template. You can use this template for free 
with the Mule Enterprise Edition, CloudHub, or as a trial in Anypoint Studio.

# Use Case
I want to synchronize contacts from Salesforce to Siebel.

This Template should serve as a foundation for the process of migrating contacts from Salesforce to Siebel, being able to specify filtering criteria and desired behavior when a contact already exists in the Siebel instance. 

As implemented, this Template leverages the [Batch Module](http://www.mulesoft.org/documentation/display/current/Batch+Processing).
The batch job is divided into Process and On Complete stages.
The Template will query Salesforce for all the existing Contacts that match the filtering criteria.
In the Process stage, if the *accountSyncPolicy* is set to 'syncAccount', the Account under which the Contact belongs is created in the Siebel instance (if it does not exist).
The last step of the Process stage will create/update contacts in Siebel.
Finally during the On Complete stage the Template will output statistics data into the console.

# Considerations

To make this Anypoint Template run, there are certain preconditions that must be considered. All of them deal with the preparations in both source (Salesforce) and destination (Siebel) systems, that must be made in order for all to run smoothly. 
**Failing to do so could lead to unexpected behavior of the template.**



## Salesforce Considerations

Here's what you need to know about Salesforce to get this template to work.

### FAQ

- Where can I check that the field configuration for my Salesforce instance is the right one? See: <a href="https://help.salesforce.com/HTViewHelpDoc?id=checking_field_accessibility_for_a_particular_field.htm&language=en_US">Salesforce: Checking Field Accessibility for a Particular Field</a>
- Can I modify the Field Access Settings? How? See: <a href="https://help.salesforce.com/HTViewHelpDoc?id=modifying_field_access_settings.htm&language=en_US">Salesforce: Modifying Field Access Settings</a>

### As a Data Source

If the user who configured the template for the source system does not have at least *read only* permissions for the fields that are fetched, then an *InvalidFieldFault* API fault displays.


## Siebel Considerations

Here's what you need to know to get this template to work with Siebel.

This template may use date time or timestamp fields from Siebel to do comparisons and take further actions.
While the template handles the time zone by sending all such fields in a neutral time zone, it cannot discover the time zone in which the Siebel instance is on.
It is up to you to provide such information. See [Oracle's Setting Time Zone Preferences](http://docs.oracle.com/cd/B40099_02/books/Fundamentals/Fund_settingoptions3.html)



### As a Data Destination

In order to make the Siebel connector work smoothly you have to provide the siebel jars (Siebel.jar, SiebelJI_enu.jar) of correct version of that work with your Siebel installation.


# Run it!
Simple steps to get Salesforce to Siebel Contact Broadcast running.


## Running On Premises
In this section we help you run your template on your computer.


### Where to Download Anypoint Studio and the Mule Runtime
If you are a newcomer to Mule, here is where to get the tools.

+ [Download Anypoint Studio](https://www.mulesoft.com/platform/studio)
+ [Download Mule runtime](https://www.mulesoft.com/lp/dl/mule-esb-enterprise)


### Importing a Template into Studio
In Studio, click the Exchange X icon in the upper left of the taskbar, log in with your
Anypoint Platform credentials, search for the template, and click **Open**.


### Running on Studio
After you import your template into Anypoint Studio, follow these steps to run it:

+ Locate the properties file `mule.dev.properties`, in src/main/resources.
+ Complete all the properties required as per the examples in the "Properties to Configure" section.
+ Right click the template project folder.
+ Hover your mouse over `Run as`
+ Click `Mule Application (configure)`
+ Inside the dialog, select Environment and set the variable `mule.env` to the value `dev`
+ Click `Run`


### Running on Mule Standalone
Complete all properties in one of the property files, for example in mule.prod.properties and run your app with the corresponding environment variable. To follow the example, this is `mule.env=prod`. 


## Running on CloudHub
While creating your application on CloudHub (or you can do it later as a next step), go to Runtime Manager > Manage Application > Properties to set the environment variables listed in "Properties to Configure" as well as the **mule.env**.


### Deploying your Anypoint Template on CloudHub
Studio provides an easy way to deploy your template directly to CloudHub, for the specific steps to do so check this


## Properties to Configure
To use this template, configure properties (credentials, configurations, etc.) in the properties file or in CloudHub from Runtime Manager > Manage Application > Properties. The sections that follow list example values.
### Application Configuration
+ scheduler.frequency `10000`  
These are the miliseconds (also different time units can be used) that will run between two different checks for updates in Salesforce
+ scheduler.start.delay `100`

+ watermark.default.expression `2015-10-23T11:00:00.000Z`  
This property is an important one, as it configures what should be the start point of the synchronization. The date format accepted in SFDC Query Language is either *YYYY-MM-DDThh:mm:ss+hh:mm* or you can use Constants. [More information about Dates in SFDC](https://developer.salesforce.com/docs/atlas.en-us.soql_sosl.meta/soql_sosl/sforce_api_calls_soql_select_dateformats.htm)

**Note:** the property **account.sync.policy** can take any of the two following values: 

+ **empty_value**: if the propety has no value assigned to it then application will do nothing in respect to the account and it'll just move the contact over.
+ **syncAccount**: it will try to create the contact's account should this is not present in the Siebel instance.

### Salesforce Connector configuration
+ sfdc.username `bob.dylan@sfdc`
+ sfdc.password `DylanPassword123`
+ sfdc.securityToken `avsfwCUl7apQs56Xq2AKi3X`

### Oracle Siebel Connector configuration
+ sieb.user `user`
+ sieb.password `secret`
+ sieb.server `server`
+ sieb.serverName `serverName`
+ sieb.objectManager `objectManager`
+ sieb.port `2321`

# API Calls
Salesforce imposes limits on the number of API Calls that can be made. However, in this template, only one call per scheduler cycle is done to retrieve all the information required.


# Customize It!
This brief guide intends to give a high level idea of how this template is built and how you can change it according to your needs.
As Mule applications are based on XML files, this page describes the XML files used with this template.

More files are available such as test classes and Mule application files, but to keep it simple, we focus on these XML files:

* config.xml
* businessLogic.xml
* endpoints.xml
* errorHandling.xml


## config.xml
Configuration for connectors and configuration properties are set in this file. Even change the configuration here, all parameters that can be modified are in properties file, which is the recommended place to make your changes. However if you want to do core changes to the logic, you need to modify this file.

In the Studio visual editor, the properties are on the *Global Element* tab.


## businessLogic.xml
Functional aspect of the Anypoint Template is implemented in this XML, directed by a batch job that will be responsible for creations/updates. The severeal message processors constitute four high level actions that fully implement the logic of this Anypoint Template:

1. Job execution is invoked from triggerFlow (endpoints.xml) everytime there is a new query executed asking for created/updated Contacts.
2. During the Process stage, if syncAccountPolicy is set to `syncAccount`, Account is synced to Siebel instance.
3. The last step of the Process stage will create/update contacts in Siebel.
Finally during the On Complete stage the Anypoint Template will log output statistics data into the console.



## endpoints.xml
This file contains a flow containing the Scheduler that will periodically query Salesforce for updated/created Contacts that meet the defined criteria in the query and then executing the batch job process with the query results.



## errorHandling.xml
This is the right place to handle how your integration reacts depending on the different exceptions. 
This file provides error handling that is referenced by the main flow in the business logic.





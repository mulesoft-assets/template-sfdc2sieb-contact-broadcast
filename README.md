
# Anypoint Template: Salesforce to Siebel Contact Broadcast

+ [License Agreement](#licenseagreement)
+ [Use Case](#usecase)
+ [Considerations](#considerations)
	* [Salesforce Considerations](#salesforceconsiderations)
	* [Siebel Considerations](#siebelconsiderations)
+ [Run it!](#runit)
	* [Running on premise](#runonopremise)
	* [Running on Studio](#runonstudio)
	* [Running on Mule ESB stand alone](#runonmuleesbstandalone)
	* [Running on CloudHub](#runoncloudhub)
	* [Deploying your Anypoint Template on CloudHub](#deployingyouranypointtemplateoncloudhub)
	* [Properties to be configured (With examples)](#propertiestobeconfigured)
+ [API Calls](#apicalls)
+ [Customize It!](#customizeit)
	* [config.xml](#configxml)
	* [businessLogic.xml](#businesslogicxml)
	* [endpoints.xml](#endpointsxml)
	* [errorHandling.xml](#errorhandlingxml)


# License Agreement <a name="licenseagreement"/>
Note that using this template is subject to the conditions of this [License Agreement](AnypointTemplateLicense.pdf).
Please review the terms of the license before downloading and using this template. In short, you are allowed to use the template for free with Mule ESB Enterprise Edition, CloudHub, or as a trial in Anypoint Studio.

# Use Case <a name="usecase"/>
I want to synchronize contacts from Salesforce to Siebel.

This Template should serve as a foundation for the process of migrating contacts from Salesforce to Siebel, being able to specify filtering criteria and desired behavior when a contact already exists in the Siebel instance. 

As implemented, this Template leverages the [Batch Module](http://www.mulesoft.org/documentation/display/current/Batch+Processing).
The batch job is divided into Input, Process and On Complete stages.
During the Input stage, the Template will query Salesforce for all the existing Contacts that match the filtering criteria.
In the Process stage, if the *accountSyncPolicy* is set to 'syncAccount', the Account under which the Contact belongs is created in the Siebel instance (if it does not exist).
The last step of the Process stage will create/update contacts in Siebel.
Finally during the On Complete stage the Template will output statistics data into the console.

# Considerations <a name="considerations"/>

To make this Anypoint Template run, there are certain preconditions that must be considered. All of them deal with the preparations in both source (Salesforce) and destination (Siebel) systems, that must be made in order for all to run smoothly. 
**Failling to do so could lead to unexpected behavior of the template.**



## Salesforce Considerations <a name="salesforceconsiderations"/>

There may be a few things that you need to know regarding Salesforce, in order for this template to work.

In order to have this template working as expected, you should be aware of your own Salesforce field configuration.

###FAQ

 - Where can I check that the field configuration for my Salesforce instance is the right one?

    [Salesforce: Checking Field Accessibility for a Particular Field][1]

- Can I modify the Field Access Settings? How?

    [Salesforce: Modifying Field Access Settings][2]


[1]: https://help.salesforce.com/HTViewHelpDoc?id=checking_field_accessibility_for_a_particular_field.htm&language=en_US
[2]: https://help.salesforce.com/HTViewHelpDoc?id=modifying_field_access_settings.htm&language=en_US

### As source of data

If the user configured in the template for the source system does not have at least *read only* permissions for the fields that are fetched, then a *InvalidFieldFault* API fault will show up.

```
java.lang.RuntimeException: [InvalidFieldFault [ApiQueryFault [ApiFault  exceptionCode='INVALID_FIELD'
exceptionMessage='
Account.Phone, Account.Rating, Account.RecordTypeId, Account.ShippingCity
^
ERROR at Row:1:Column:486
No such column 'RecordTypeId' on entity 'Account'. If you are attempting to use a custom field, be sure to append the '__c' after the custom field name. Please reference your WSDL or the describe call for the appropriate names.'
]
row='1'
column='486'
]
]
```


## Siebel Considerations <a name="siebelconsiderations"/>

There may be a few things that you need to know regarding Siebel, in order for this template to work.

This Anypoint Template may be using date time/timestamp fields from the Siebel in order to do comparisons and take further actions.
While the template handles the time zone by sending all such fields in a neutral time zone, it can not find out on its on the time zone in which the Siebel instance is on.
It will be up to the user of this template to provide such information. To find out more about Siebel time zones please check the following [link](http://docs.oracle.com/cd/B40099_02/books/Fundamentals/Fund_settingoptions3.html)



### As destination of data

In order to make the Siebel connector work smoothly you have to provide the siebel jars (Siebel.jar, SiebelJI_enu.jar) of correct version of that work with your Siebel installation.







# Run it! <a name="runit"/>
Simple steps to get Salesforce to Siebel Contact Broadcast running.


## Running on premise <a name="runonopremise"/>
In this section we detail the way you should run your Anypoint Template on your computer.


### Where to Download Mule Studio and Mule ESB
First thing to know if you are a newcomer to Mule is where to get the tools.

+ You can download Mule Studio from this [Location](http://www.mulesoft.com/platform/mule-studio)
+ You can download Mule ESB from this [Location](http://www.mulesoft.com/platform/soa/mule-esb-open-source-esb)


### Importing an Anypoint Template into Studio
Mule Studio offers several ways to import a project into the workspace, for instance: 

+ Anypoint Studio generated Deployable Archive (.zip)
+ Anypoint Studio Project from External Location
+ Maven-based Mule Project from pom.xml
+ Mule ESB Configuration XML from External Location

You can find a detailed description on how to do so in this [Documentation Page](http://www.mulesoft.org/documentation/display/current/Importing+and+Exporting+in+Studio).


### Running on Studio <a name="runonstudio"/>
Once you have imported you Anypoint Template into Anypoint Studio you need to follow these steps to run it:

+ Locate the properties file `mule.dev.properties`, in src/main/resources
+ Complete all the properties required as per the examples in the section [Properties to be configured](#propertiestobeconfigured)
+ Once that is done, right click on you Anypoint Template project folder 
+ Hover you mouse over `"Run as"`
+ Click on  `"Mule Application"`


### Running on Mule ESB stand alone <a name="runonmuleesbstandalone"/>
Complete all properties in one of the property files, for example in [mule.prod.properties] (../master/src/main/resources/mule.prod.properties) and run your app with the corresponding environment variable to use it. To follow the example, this will be `mule.env=prod`. 


## Running on CloudHub <a name="runoncloudhub"/>
While [creating your application on CloudHub](http://www.mulesoft.org/documentation/display/current/Hello+World+on+CloudHub) (Or you can do it later as a next step), you need to go to Deployment > Advanced to set all environment variables detailed in **Properties to be configured** as well as the **mule.env**.


### Deploying your Anypoint Template on CloudHub <a name="deployingyouranypointtemplateoncloudhub"/>
Mule Studio provides you with really easy way to deploy your Template directly to CloudHub, for the specific steps to do so please check this [link](http://www.mulesoft.org/documentation/display/current/Deploying+Mule+Applications#DeployingMuleApplications-DeploytoCloudHub)


## Properties to be configured (With examples) <a name="propertiestobeconfigured"/>
In order to use this Mule Anypoint Template you need to configure properties (Credentials, configurations, etc.) either in properties file or in CloudHub as Environment Variables. Detail list with examples:
### Application configuration
+ poll.frequencyMillis `10000`  
These are the miliseconds (also different time units can be used) that will run between two different checks for updates in Salesforce

+ watermark.default.expression `2015-10-23T11:00:00.000Z`  
This property is an important one, as it configures what should be the start point of the synchronization. The date format accepted in SFDC Query Language is either *YYYY-MM-DDThh:mm:ss+hh:mm* or you can use Constants. [More information about Dates in SFDC](http://www.salesforce.com/us/developer/docs/officetoolkit/Content/sforce_api_calls_soql_select_dateformats.htm)
+ poll.startDelayMillis `100`

**Note:** the property **account.sync.policy** can take any of the two following values: 

+ **empty_value**: if the propety has no value assigned to it then application will do nothing in respect to the account and it'll just move the contact over.
+ **syncAccount**: it will try to create the contact's account should this is not present in the Siebel instance.

### Salesforce Connector configuration
+ sfdc.username `bob.dylan@sfdc`
+ sfdc.password `DylanPassword123`
+ sfdc.securityToken `avsfwCUl7apQs56Xq2AKi3X`
+ sfdc.url `https://login.salesforce.com/services/Soap/u/32.0`

### Oracle Siebel Connector configuration
+ sieb.user `user`
+ sieb.password `secret`
+ sieb.server `server`
+ sieb.serverName `serverName`
+ sieb.objectManager `objectManager`
+ sieb.port `2321`

# API Calls <a name="apicalls"/>
Salesforce imposes limits on the number of API Calls that can be made. However, in this template, only one call per poll cycle is done to retrieve all the information required.


# Customize It!<a name="customizeit"/>
This brief guide intends to give a high level idea of how this Anypoint Template is built and how you can change it according to your needs.
As mule applications are based on XML files, this page will be organized by describing all the XML that conform the Anypoint Template.
Of course more files will be found such as Test Classes and [Mule Application Files](http://www.mulesoft.org/documentation/display/current/Application+Format), but to keep it simple we will focus on the XMLs.

Here is a list of the main XML files you'll find in this application:

* [config.xml](#configxml)
* [endpoints.xml](#endpointsxml)
* [businessLogic.xml](#businesslogicxml)
* [errorHandling.xml](#errorhandlingxml)


## config.xml<a name="configxml"/>
Configuration for Connectors and [Properties Place Holders](http://www.mulesoft.org/documentation/display/current/Configuring+Properties) are set in this file. **Even you can change the configuration here, all parameters that can be modified here are in properties file, and this is the recommended place to do it so.** Of course if you want to do core changes to the logic you will probably need to modify this file.

In the visual editor they can be found on the *Global Element* tab.


## businessLogic.xml<a name="businesslogicxml"/>
Functional aspect of the Anypoint Template is implemented in this XML, directed by a batch job that will be responsible for creations/updates. The severeal message processors constitute four high level actions that fully implement the logic of this Anypoint Template:

1. Job execution is invoked from triggerFlow (endpoints.xml) everytime there is a new query executed asking for created/updated Contacts.
2. During the Process stage, if syncAccountPolicy is set to `syncAccount`, Account is synced to Siebel instance.
3. The last step of the Process stage will create/update contacts in Siebel.
Finally during the On Complete stage the Anypoint Template will log output statistics data into the console.



## endpoints.xml<a name="endpointsxml"/>
This file contains a flow containing the Poll that will periodically query Salesforce for updated/created Contacts that meet the defined criteria in the query and then executing the batch job process with the query results.



## errorHandling.xml<a name="errorhandlingxml"/>
This is the right place to handle how your integration will react depending on the different exceptions. 
This file holds a [Choice Exception Strategy](http://www.mulesoft.org/documentation/display/current/Choice+Exception+Strategy) that is referenced by the main flow in the business logic.




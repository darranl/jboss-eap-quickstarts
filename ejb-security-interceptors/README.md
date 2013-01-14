ejb-security-interceptors:  Using client and server side interceptors to switch the identity for an EJB call.
====================
Author: Darran Lofthouse
Level: Advanced
Technologies: EJB, Security
Summary: Demonstrates how interceptors can be used to switch the identity for EJB calls on a call by call basis.
Target Product: EAP

What is it?
-----------



System requirements
-------------------




Configure Maven
---------------

If you have not yet done so, you must [Configure Maven](../README.md#mavenconfiguration) before testing the quickstarts.


Add the Application Users
---------------

This quick start is built around the default 'ApplicationRealm' as configured in the AS7 / EAP 6 distribution, the following four 
users should be added using the add-user utility.

'ConnectionUser' with role 'User' and password 'ConnectionPassword1!'.
'AppUserOne' with roles 'User' and 'RoleOne', any password can be specified for this user.
'AppUserTwo' with roles 'User' and 'RoleTwo', again any password can be specified for this user.
'AppUserThree' with roles 'User', 'RoleOne', and 'RoleTwo' and again any password.  

The first user is used for establishing the actual connection to the server, the subsequent two users are the users that this
quickstart demonstrates can be switched to on demand.  The final user is a user that can access everything but can not be switched to.

Add the LoginModule
---------------

The EJB side of this quick start makes use of the 'other' security domain which by default delegates to the 'ApplicationRealm',
in order to support identity switching an additional login module needs to be added to the domain definition.

  <login-module code="org.jboss.as.quickstarts.ejb_security_interceptors.DelegationLoginModule" flag="optional">
    <module-option name="password-stacking" value="useFirstPass"/>
  </login-module>
  
This login module can either be added before or after the existing 'Remoting' login module in the domain but it MUST be somewhere before
the existing RealmDirect login module.  

If this approach is used and the majority of requests will involve an identity switch then it would recommended to have this module as
the first module in the list, however if the majority of requests will run as the connection user with occasional switches it would
be recommended ot place the 'Remoting' login module first and this one second.

This login module will load the properties file 'delegation-mapping.properties' from the deployment, the location of this properties
file can be overridden with the module-option 'delegationProperties'.

At runtime this login module is used to decide if the user of the connection to the server is allowed to ask that the request is executed
as the specified user.

There are four variations of how the key can be specified in the properties file: -

 - user@realm        - Exact match of user and realm.
 - user@*            - Allow a match of user for any realm.
 - *@realm           - Match for any user in the realm specified.
 - *                 - Match for all users in all realms.
 
When a request is received that involves switching the user the identity of the user that opened the connection is used to 
check the properties file for an entry, the check is performed in the order listed above until the first match is found - once
a match is found further entries that could match are not read.

The value in the properties file can either be a wildcard '*' or it can be a comma separated list of users, do be aware
that in the value/mapping side there is no notion of the realm.

For this quick start we use the following entry: -

  ConnectionUser@ApplicationRealm=AppUserOne,AppUserTwo
  
This means that the ConnectionUser added above can only ask that a request is executed as either AppUserOne or AppUserTwo, it is not
allowed to ask for it to be executed as AppUserThree.

All users are permitted to execute requests as themselves as in that case the login module is not called, that is the default behaviour
that exists without the addition of the interceptors in this quick start.    

* Further Use *

Taking this further the DelegationLoginModule can be extended to provide custom delegation checks, one thing not currently 
checked is if the user being switched to actually exists, if the module is extended the following method can be overridden to
provide a custom check.

  protected boolean delegationAcceptable(String requestedUser, OuterUserCredential connectionUser);   

Server to Server Connection
-------------------------

For the purpose of the quickstart we just need an outbound connection that loops back to the same server, this will be
sufficient to demonstrate the server to server capabilities.

Add the following security realm, note the Base64 password is for the ConnectionUser account created above.

   <security-realm name="ejb-outbound-realm">
      <server-identities>
         <secret value="Q29ubmVjdGlvblBhc3N3b3JkMSE="/>
      </server-identities>
   </security-realm>
            
Within the socket-binding-group 'standard-sockets' add the following outbound connection: -

   <outbound-socket-binding name="ejb-outbound">
      <remote-destination host="localhost" port="4447"/>
   </outbound-socket-binding>          

Within the Remoting susbsytem add the following outbound connection: -

   <outbound-connections>
      <remote-outbound-connection name="ejb-outbound-connection" outbound-socket-binding-ref="ejb-outbound" security-realm="ejb-outbound-realm" username="ConnectionUser">
         <properties>
            <property name="SSL_ENABLED" value="false"/>
         </properties>
      </remote-outbound-connection>
   </outbound-connections>




Start JBoss Enterprise Application Platform 6 or JBoss AS 7
-------------------------



Build and Deploy the Quickstart
-------------------------

_NOTE: The following build command assumes you have configured your Maven user settings. If you have not, you must include Maven setting arguments on the command line. See [Build and Deploy the Quickstarts](../README.md#buildanddeploy) for complete instructions and additional options._

1. Make sure you have started the JBoss Server as described above.
2. Open a command line and navigate to the root directory of this quickstart.
3. Type this command to build and deploy the archive:

        mvn clean package jboss-as:deploy

4. This will deploy `target/jboss-as-ejb-security.war` to the running instance of the server.


Access the application 
---------------------




Undeploy the Archive
--------------------

1. Make sure you have started the JBoss Server as described above.
2. Open a command line and navigate to the root directory of this quickstart.
3. When you are finished testing, type this command to undeploy the archive:

        mvn jboss-as:undeploy


Run the Quickstart in JBoss Developer Studio or Eclipse
-------------------------------------
You can also start the server and deploy the quickstarts from Eclipse using JBoss tools. For more information, see [Use JBoss Developer Studio or Eclipse to Run the Quickstarts](../README.md#useeclipse) 


Debug the Application
------------------------------------

If you want to debug the source code or look at the Javadocs of any library in the project, run either of the following commands to pull them into your local repository. The IDE should then detect them.

    mvn dependency:sources
    mvn dependency:resolve -Dclassifier=javadoc

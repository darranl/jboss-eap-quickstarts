/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the 
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.as.quickstarts.ejb_security_interceptors;

import static org.jboss.as.quickstarts.ejb_security_interceptors.EJBUtil.lookupSecuredEJB;
import static org.jboss.as.quickstarts.ejb_security_interceptors.EJBUtil.lookupIntermediateEJB;
import static org.jboss.as.quickstarts.ejb_security_interceptors.EJBUtil.registerClientSecurityInterceptor;

import javax.ejb.EJBAccessException;

/**
 * The remote client responsible for making a number of calls to the server to demonstrate the capabilities of the interceptors.
 * 
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
public class RemoteClient {

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        System.out.println("\n\n\n* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\n\n");
        System.out.println("{RC} Starting RemoteClient.");
        registerClientSecurityInterceptor();
        SecuredEJBRemote remote = lookupSecuredEJB();

        System.out.println("{RC} Initial Call to getSecurityInformation() - " + remote.getSecurityInformation());

        System.out.println("{RC} Verifying methods requiring roles 'RoleOne' and 'RoleTwo' are inaccessible.");
        try {
            remote.roleOneMethod();
            System.err.println("{RC} Call to roleOneMethod was incorectly accepted.");
        } catch (EJBAccessException e) {
            System.out.println("{RC} Call to roleOneMethod was correctly rejected.");
        }

        try {
            remote.roleTwoMethod();
            System.err.println("{RC} Call to roleTwoMethod was incorectly accepted.");
        } catch (EJBAccessException e) {
            System.out.println("{RC} Call to roleTwoMethod was correctly rejected.");
        }

        System.out.println("{RC} Now attempting calls as AppUserOne.");
        ClientSecurityInterceptor.setDesiredUser("AppUserOne");

        System.out.println("{RC} Call to getSecurityInformation() - " + remote.getSecurityInformation());

        System.out
                .println("{RC} Verifying method requiring role 'RoleOne' is accessible and the method requiring 'RoleTwo' is inaccessible.");
        try {
            remote.roleOneMethod();
            System.out.println("{RC} Call to roleOneMethod was correctly accepted..");
        } catch (EJBAccessException e) {
            System.err.println("{RC} Call to roleOneMethod was incorectly rejected.");
        }

        try {
            remote.roleTwoMethod();
            System.err.println("{RC} Call to roleTwoMethod was incorectly accepted.");
        } catch (EJBAccessException e) {
            System.out.println("{RC} Call to roleTwoMethod was correctly rejected.");
        }

        System.out.println("{RC} Now attempting calls as AppUserTwo.");
        ClientSecurityInterceptor.setDesiredUser("AppUserTwo");

        System.out.println("{RC} Call to getSecurityInformation() - " + remote.getSecurityInformation());

        System.out
                .println("{RC} Verifying method requiring role 'RoleOne' is inaccessible and the method requiring 'RoleTwo' is accessible.");
        try {
            remote.roleOneMethod();
            System.err.println("{RC} Call to roleOneMethod was incorrectly accepted..");
        } catch (EJBAccessException e) {
            System.out.println("{RC} Call to roleOneMethod was corectly rejected.");
        }

        try {
            remote.roleTwoMethod();
            System.out.println("{RC} Call to roleTwoMethod was corectly accepted.");
        } catch (EJBAccessException e) {
            System.err.println("{RC} Call to roleTwoMethod was incorrectly rejected.");
        }

        ClientSecurityInterceptor.setDesiredUser(null);
        System.out.println("{RC} Back as ConnectionUser Call to getSecurityInformation() - " + remote.getSecurityInformation());

        System.out.println("{RC} Verifying methods requiring roles 'RoleOne' and 'RoleTwo' are inaccessible.");
        try {
            remote.roleOneMethod();
            System.err.println("{RC} Call to roleOneMethod was incorectly accepted.");
        } catch (EJBAccessException e) {
            System.out.println("{RC} Call to roleOneMethod was correctly rejected.");
        }

        try {
            remote.roleTwoMethod();
            System.err.println("{RC} Call to roleTwoMethod was incorectly accepted.");
        } catch (EJBAccessException e) {
            System.out.println("{RC} Call to roleTwoMethod was correctly rejected.");
        }

        IntermediateEJBRemote intermediate = lookupIntermediateEJB();
        System.out.println("Calling intermediate bean.\n");
        System.out.println(intermediate.makeTestCalls());

        System.out.println("\n\n* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\n\n\n");
    }

}

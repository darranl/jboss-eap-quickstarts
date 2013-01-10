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

import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.security.auth.Subject;

import org.jboss.as.security.remoting.RemotingContext;
import org.jboss.remoting3.Connection;
import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityContextAssociation;
import org.jboss.security.SecurityContextFactory;

/**
 * Security actions for this package only.
 * 
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
final class SecurityActions {

    private SecurityActions() {
    }

    /*
     * RemotingContext Actions
     */

    static void remotingContextClear() {
        if (System.getSecurityManager() == null) {
            RemotingContext.clear();
        } else {
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                @Override
                public Object run() {
                    RemotingContext.clear();
                    return null;
                }
            });
        }
    }

    static Connection remotingContextGetConnection() {
        if (System.getSecurityManager() == null) {
            return RemotingContext.getConnection();
        } else {
            return AccessController.doPrivileged(new PrivilegedAction<Connection>() {
                @Override
                public Connection run() {
                    return RemotingContext.getConnection();
                }
            });
        }
    }

    static boolean remotingContextIsSet() {
        if (System.getSecurityManager() == null) {
            return RemotingContext.isSet();
        } else {
            return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                @Override
                public Boolean run() {
                    return RemotingContext.isSet();
                }
            });
        }
    }

    /*
     * SecurityContext Actions
     */

    static void securityContextSet(final SecurityContext context) {
        if (System.getSecurityManager() == null) {
            SecurityContextAssociation.setSecurityContext(context);
        } else {
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    SecurityContextAssociation.setSecurityContext(context);
                    return null;
                }
            });
        }
    }

    /**
     * @return The SecurityContext previously set if any.
     */
    static SecurityContext securityContextSetPrincipalInfo(final Principal principal, final Object credential) throws Exception {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<SecurityContext>() {

                @Override
                public SecurityContext run() throws Exception {
                    SecurityContext currentContext = SecurityContextAssociation.getSecurityContext();

                    SecurityContext nextContext = SecurityContextFactory.createSecurityContext(principal, credential,
                            new Subject(), "USER_DELEGATION");
                    SecurityContextAssociation.setSecurityContext(nextContext);

                    return currentContext;
                }

            });
        } catch (PrivilegedActionException pae) {
            throw pae.getException();
        }
    }

}

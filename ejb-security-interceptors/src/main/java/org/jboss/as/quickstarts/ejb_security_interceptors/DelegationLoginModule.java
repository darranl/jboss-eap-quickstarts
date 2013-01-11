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

import java.security.Principal;
import java.security.acl.Group;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.login.LoginException;

import org.jboss.security.SimpleGroup;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.auth.callback.ObjectCallback;
import org.jboss.security.auth.spi.AbstractServerLoginModule;

/**
 * Login module to make the decision if one user can ask for the current request to be switched to an alternative specified
 * user.
 *
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
public class DelegationLoginModule extends AbstractServerLoginModule {

    private CallbackHandler callbackHandler;

    private Principal identity;

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        super.initialize(subject, callbackHandler, sharedState, options);
        this.callbackHandler = callbackHandler;
    }

    @Override
    public boolean login() throws LoginException {
        if (super.login() == true) {
            log.debug("super.login()==true");
            return true;
        }

        // Time to see if this is a delegation request.
        NameCallback ncb = new NameCallback("Username:");
        ObjectCallback ocb = new ObjectCallback("Password:");

        try {
            callbackHandler.handle(new Callback[] { ncb, ocb });
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            return false; // If the CallbackHandler can not handle the required callbacks then no chance.
        }

        String name = ncb.getName();
        Object credential = ocb.getCredential();

        if (credential instanceof OuterUserCredential) {
            // This credential type will only be seen for a delegation request, if not seen then the request is not for us.

            // TODO - At this point we make the decision can the user represented by the ObjectCredential request to run as
            // the user represented by name.
            // For now go ahead and accept.

            identity = new SimplePrincipal(name);
            if (getUseFirstPass()) {
                String userName = identity.getName();
                if (log.isDebugEnabled())
                    log.debug("Storing username '" + userName + "' and empty password");
                // Add the username and an empty password to the shared state map
                sharedState.put("javax.security.auth.login.name", identity);
                sharedState.put("javax.security.auth.login.password", "");
            }
            loginOk = true;
            return true;
        }

        return false; // Attempted login but not successful.
    }

    @Override
    protected Principal getIdentity() {
        return identity;
    }

    @Override
    protected Group[] getRoleSets() throws LoginException {
        Group roles = new SimpleGroup("Roles");
        Group callerPrincipal = new SimpleGroup("CallerPrincipal");
        Group[] groups = { roles, callerPrincipal };
        callerPrincipal.addMember(getIdentity());
        return groups;
    }

}

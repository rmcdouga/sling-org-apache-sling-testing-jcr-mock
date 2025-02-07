/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sling.testing.mock.jcr;

import java.security.Principal;
import java.util.Arrays;

import javax.jcr.Credentials;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.SimpleCredentials;

import org.apache.jackrabbit.api.security.user.Impersonation;
import org.apache.jackrabbit.api.security.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mock {@link User} implementation.
 */
class MockUser extends MockAuthorizable implements User {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private char[] pwd = {};
    private boolean disabled;
    private String disabledReason;

    public MockUser(@Nullable String id, @Nullable Principal principal,
            @NotNull Node homeNode,
            @NotNull MockUserManager mockUserMgr) {
        super(id, principal, homeNode, mockUserMgr);
    }

    @Override
    public boolean isAdmin() {
        boolean result = false;
        try {
            result = "admin".equals(getID());
        } catch (RepositoryException e) {
            // ignore and log
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to determine if this is admin", e);
            }
        }
        return result;
    }

    @Override
    public boolean isSystemUser() {
        return false;
    }

    @Override
    public @NotNull Credentials getCredentials() throws RepositoryException {
        return new SimpleCredentials(id, pwd);
    }

    @Override
    public @NotNull Impersonation getImpersonation() throws RepositoryException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void changePassword(@Nullable String password) throws RepositoryException {
        if (password == null) {
            throw new RepositoryException("Attempt to set 'null' password for user " + getID());
        }

        pwd = password.toCharArray();
    }

    @Override
    public void changePassword(@Nullable String password, @NotNull String oldPassword) throws RepositoryException {
        if (Arrays.equals(pwd, oldPassword.toCharArray())) {
            changePassword(password);
        } else {
            throw new RepositoryException("old password did not match");
        }
    }

    @Override
    public void disable(@Nullable String reason) throws RepositoryException {
        if (reason == null) {
            this.disabled = false;
            this.disabledReason = null;
        } else {
            this.disabled = true;
            this.disabledReason = reason;
        }
    }

    @Override
    public boolean isDisabled() throws RepositoryException {
        return disabled;
    }

    @Override
    public @Nullable String getDisabledReason() throws RepositoryException {
        return disabledReason;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MockUser [id=");
        builder.append(id);
        builder.append("]");
        return builder.toString();
    }

}

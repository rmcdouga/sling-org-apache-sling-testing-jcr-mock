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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import javax.jcr.Credentials;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.SimpleCredentials;
import javax.jcr.UnsupportedRepositoryOperationException;

import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.oak.spi.security.user.UserConstants;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.mockito.Mockito;

import ch.qos.logback.classic.Level;

/**
 *
 */
public class MockUserTest extends MockAuthorizableTest<User> {

    @Override
    protected User createAuthorizable() throws RepositoryException {
        return userManager.createUser("user1", "pwd");
    }

    @Test
    @Override
    public void testGetID() throws RepositoryException {
        assertEquals("user1", authorizable.getID());
    }

    @Test
    @Override
    public void testIsGroup() {
        assertFalse(authorizable.isGroup());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockUser#isAdmin()}.
     */
    @Test
    public void testIsAdmin() throws RepositoryException {
        assertFalse(authorizable.isAdmin());
        assertTrue(userManager.createUser("admin", "admin").isAdmin());
    }
    @Test
    public void testIsAdminCatchRepositoryException() throws Exception {
        User mockAuthorizable = Mockito.spy(authorizable);
        Mockito.doThrow(RepositoryException.class).when(mockAuthorizable).getID();

        // verify that the debug msg about exception was not logged
        try (LogCapture capture = new LogCapture(authorizable.getClass().getName(), true)) {
            // bump up the log level
            capture.setLoggerLevel(Level.WARN);

            // this should log an warning message
            assertFalse(mockAuthorizable.isAdmin());

            // verify the msg was logged
            capture.assertNotContains(Level.DEBUG, "Failed to determine if this is admin");
        }

        // verify that the debug msg about exception was logged
        try (LogCapture capture = new LogCapture(authorizable.getClass().getName(), true)) {
            // bump up the log level
            capture.setLoggerLevel(Level.DEBUG);

            // this should log an warning message
            assertFalse(mockAuthorizable.isAdmin());

            // verify the msg was logged
            capture.assertContains(Level.DEBUG, "Failed to determine if this is admin");
        }
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockUser#isSystemUser()}.
     */
    @Test
    public void testIsSystemUser() {
        assertFalse(authorizable.isSystemUser());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockUser#getCredentials()}.
     */
    @Test
    public void testGetCredentials() throws RepositoryException {
        @NotNull Credentials credentials = authorizable.getCredentials();
        assertTrue(credentials instanceof SimpleCredentials);
        assertEquals(authorizable.getID(), ((SimpleCredentials)credentials).getUserID());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockUser#getImpersonation()}.
     */
    @Test
    public void testGetImpersonation() {
        assertThrows(UnsupportedOperationException.class, () -> authorizable.getImpersonation());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockUser#changePassword(java.lang.String)}.
     */
    @Test
    public void testChangePasswordString() throws RepositoryException {
        authorizable.changePassword("changed");
        assertPassword("changed".toCharArray());

        assertThrows(RepositoryException.class, () -> authorizable.changePassword(null));
    }

    protected void assertPassword(char [] expectedPwd) throws RepositoryException {
        SimpleCredentials creds = (SimpleCredentials)authorizable.getCredentials();
        assertArrayEquals(expectedPwd, creds.getPassword());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockUser#changePassword(java.lang.String, java.lang.String)}.
     */
    @Test
    public void testChangePasswordStringString() throws RepositoryException {
        authorizable.changePassword("changed", "");
        assertPassword("changed".toCharArray());

        assertThrows(RepositoryException.class, () -> authorizable.changePassword("changed2", "wrong"));
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockUser#disable(java.lang.String)}.
     */
    @Test
    public void testDisable() throws RepositoryException {
        authorizable.disable("Expired");
        assertTrue(authorizable.isDisabled());
        assertEquals("Expired", authorizable.getDisabledReason());

        authorizable.disable(null);
        assertFalse(authorizable.isDisabled());
        assertNull(authorizable.getDisabledReason());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockUser#isDisabled()}.
     */
    @Test
    public void testIsDisabled() throws RepositoryException {
        assertFalse(authorizable.isDisabled());
        authorizable.disable("Obsolete");
        assertTrue(authorizable.isDisabled());
    }

    /**
     * Test method for {@link org.apache.sling.testing.mock.jcr.MockUser#getDisabledReason()}.
     */
    @Test
    public void testGetDisabledReason() throws RepositoryException {
        assertNull(authorizable.getDisabledReason());
        authorizable.disable("Obsolete");
        assertEquals("Obsolete", authorizable.getDisabledReason());
    }

    @Test
    @Override
    public void testGetPath() throws UnsupportedRepositoryOperationException, RepositoryException {
        assertEquals("/home/users/user1", authorizable.getPath());
        assertTrue(session.nodeExists(authorizable.getPath()));
        Node node = session.getNode(authorizable.getPath());
        assertEquals(authorizable.getID(), 
                node.getProperty(UserConstants.REP_PRINCIPAL_NAME).getString());
    }

}

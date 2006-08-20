// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.common.task;

import java.util.HashMap;

import org.globus.cog.abstraction.interfaces.SecurityContext;

public class SecurityContextImpl implements SecurityContext {
    private HashMap attributes = null;
    private Object credentials = null;
    private String alias = null;

    public SecurityContextImpl() {
        this.attributes = new HashMap();
    }

    public SecurityContextImpl(Object credentials, String alias) {
        this.attributes = new HashMap();
        this.credentials = credentials;
        this.alias = alias;
    }

    public void setCredentials(Object credentials) {
        this.credentials = credentials;
    }

    public Object getCredentials() {
        return this.credentials;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAlias() {

        return this.alias;
    }

    public void setCredentials(Object credentials, String alias) {
        this.credentials = credentials;
        this.alias = alias;
    }

    public void setAttribute(String name, Object value) {
        this.attributes.put(name, value);
    }

    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }
}
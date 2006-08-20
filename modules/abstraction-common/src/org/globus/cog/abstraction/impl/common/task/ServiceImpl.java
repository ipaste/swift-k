// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.common.task;

import java.util.Enumeration;
import java.util.Hashtable;

import org.globus.cog.abstraction.impl.common.IdentityImpl;
import org.globus.cog.abstraction.interfaces.Identity;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.ServiceContact;

public class ServiceImpl implements Service {
    private Identity identity = null;
    private String name = "";
    private ServiceContact serviceContact = null;
    private SecurityContext securityContext = null;
    private Hashtable attributes;
    private String provider = null;
    private int type = 0;
    private int totalCount = 0, failedCount = 0, activeCount = 0;

    public ServiceImpl() {
        this.attributes = new Hashtable();
        this.identity = new IdentityImpl();
    }

    public ServiceImpl(int type) {
        this.attributes = new Hashtable();
        this.identity = new IdentityImpl();
        this.type = type;
    }

    public ServiceImpl(String provider, ServiceContact serviceContact,
            SecurityContext securityContext) {
        this.attributes = new Hashtable();
        this.identity = new IdentityImpl();
        this.provider = provider;
        this.serviceContact = serviceContact;
        this.securityContext = securityContext;
    }

    public ServiceImpl(String provider, int type,
            ServiceContact serviceContact, SecurityContext securityContext) {
        this.attributes = new Hashtable();
        this.identity = new IdentityImpl();
        this.provider = provider;
        this.type = type;
        this.serviceContact = serviceContact;
        this.securityContext = securityContext;
    }

    public void setIdentity(Identity identity) {
        this.identity = identity;
    }

    public Identity getIdentity() {
        return this.identity;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getProvider() {
        return this.provider;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return this.type;
    }

    public void setServiceContact(ServiceContact serviceContact) {
        this.serviceContact = serviceContact;
    }

    public ServiceContact getServiceContact() {
        return this.serviceContact;
    }

    public void setSecurityContext(SecurityContext securityContext) {
        this.securityContext = securityContext;
    }

    public SecurityContext getSecurityContext() {
        return this.securityContext;
    }

    public void setCompletedCount(int count) {
        this.totalCount = count;
    }

    public int getCompletedCount() {
        return this.totalCount;
    }

    public void setActiveCount(int count) {
        this.activeCount = count;
    }

    public int getActiveCount() {
        return this.activeCount;
    }

    public void setFailedCount(int count) {
        this.failedCount = count;
    }

    public int getFailedCount() {
        return this.failedCount;
    }

    public void setAttribute(String name, Object value) {
        this.attributes.put(name, value);
    }

    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    public Enumeration getAllAttributes() {
        return this.attributes.keys();
    }

    public String toString() {
        return this.serviceContact.toString() + "(" + this.provider + ")";
    }
}
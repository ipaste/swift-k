// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.scheduler;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.karajan.util.BoundContact;
import org.globus.cog.karajan.util.Contact;
import org.globus.cog.karajan.util.TypeUtil;

public class DefaultScheduler extends LateBindingScheduler implements Scheduler, Runnable {
	private static final Service[] SERVICEARRAY = new Service[0];

	private static final Contact[] CONTACTARRAY = new Contact[0];

	private static final Logger logger = Logger.getLogger(DefaultScheduler.class);

	private static String[] propertyNames = new String[] { "jobsPerCpu", "maxSimultaneousJobs",
			"showTaskList" };

	private int contactCursor;

	private boolean showTaskList;

	private TaskList tl;

	public DefaultScheduler() {
		contactCursor = 0;
		showTaskList = false;
		setName("Default Scheduler");
	}

	public void setTaskList(boolean value) {
		if (!showTaskList && value) {
			activateTaskList();
		}
		if (showTaskList && !value) {
			deactivateTaskList();
		}
		showTaskList = value;
	}

	public void activateTaskList() {
		tl = new TaskList(getTaskHandlers());
		tl.setVisible(true);
		tl.update(getJobQueue().size(), getRunning());
	}

	public void deactivateTaskList() {
		tl.setVisible(false);
		tl.dispose();
		tl = null;
	}

	protected synchronized BoundContact getNextContact(TaskConstraints t)
			throws NoFreeResourceException {
		checkGlobalLoadConditions();
		int initial = contactCursor;
		while (!checkLoad(getResources().get(contactCursor))) {
			incContactCursor();
			if (contactCursor == initial) {
				logger.debug("No free resources");
				throw new NoFreeResourceException("No free hosts available");
			}
		}
		if (initial == contactCursor) {
			incContactCursor();
		}
		BoundContact contact = getResources().get(contactCursor);
		if (logger.isDebugEnabled()) {
			logger.debug("Contact: " + contact);
		}
		return contact;
	}

	private int incContactCursor() {
		contactCursor++;
		if (contactCursor >= getResources().size()) {
			contactCursor = 0;
		}
		return contactCursor;
	}

	public void setProperty(String name, Object value) {
		if (name.equalsIgnoreCase("showTaskList")) {
			logger.debug("Scheduler: setting showTaskList to " + value);
			setTaskList(TypeUtil.toBoolean(value));
		}
		else {
			super.setProperty(name, value);
		}
	}

	public String[] getPropertyNames() {
		return propertyNames;
	}

	public void submitBoundToServices(Task t, Contact[] contacts, Service[] services)
			throws TaskSubmissionException {
		super.submitBoundToServices(t, contacts, services);
		if (showTaskList) {
			tl.update(getJobQueue().size(), getRunning());
		}
	}

	public void statusChanged(StatusEvent e) {
		super.statusChanged(e);
		if (showTaskList) {
			tl.update(getJobQueue().size(), getRunning());
		}
	}
}
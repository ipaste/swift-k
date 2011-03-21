// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.workflow.nodes.grid;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.scheduler.ContactAllocationTask;
import org.globus.cog.karajan.scheduler.Scheduler;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.Contact;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.events.EventBus;
import org.globus.cog.karajan.workflow.nodes.PartialArgumentsContainer;

public class AllocateHost extends PartialArgumentsContainer implements StatusListener {
	public static final Logger logger = Logger.getLogger(AllocateHost.class);

	public static final Arg A_NAME = new Arg.Positional("name");
	public static final Arg A_CONSTRAINTS = new Arg.Optional("constraints", null);

	public static final String HOST = "##host";

	static {
		setArguments(AllocateHost.class, new Arg[] { A_NAME, A_CONSTRAINTS });
	}

	public AllocateHost() {
		this.setQuotedArgs(true);
	}

	private static Map<Task, Long> stimes = Collections.synchronizedMap(new HashMap<Task, Long>());

	protected void partialArgumentsEvaluated(VariableStack stack) throws ExecutionException {
		Object constraints = A_CONSTRAINTS.getValue(stack);
		try {
			Scheduler s = (Scheduler) stack.getGlobal(SchedulerNode.SCHEDULER);
			if (constraints == null) {
				Contact contact = s.allocateContact();
				if (logger.isDebugEnabled()) {
					logger.debug("Allocated host " + contact);
				}
				stack.setVar(TypeUtil.toString(A_NAME.getValue(stack)), contact);
				stack.setVar(HOST, contact);
				super.partialArgumentsEvaluated(stack);
				startRest(stack);
			}
			else {
				ContactAllocationTask t = new ContactAllocationTask();
				t.setStack(stack.copy());
				Contact contact = s.allocateContact(constraints);
				t.setVirtualContact(contact);
				s.addJobStatusListener(this, t);
				stimes.put(t, System.currentTimeMillis());
				s.enqueue(t, new Contact[] { contact });
			}
		}
		catch (ExecutionException e) {
			throw e;
		}
		catch (Exception e) {
			fail(stack, e.getMessage());
		}
	}
	
	public static class StatusChangeHandler implements Runnable {
		private final StatusEvent event;
		private final AllocateHost ah;
		
		public StatusChangeHandler(StatusEvent event, AllocateHost ah) {
			this.event = event;
			this.ah = ah;
		}
		
		public void run() {
			ah.processStatusChange(event);
		}
	}
	
	public void statusChanged(StatusEvent event) {
		EventBus.post(new StatusChangeHandler(event, this));
	}

	public void processStatusChange(StatusEvent event) {
		ContactAllocationTask t = (ContactAllocationTask) event.getSource();
		VariableStack stack = t.getStack();
		try {
			Scheduler s = (Scheduler) stack.getGlobal(SchedulerNode.SCHEDULER);
			int code = event.getStatus().getStatusCode();
			if (code == Status.FAILED || code == Status.COMPLETED) {
				s.removeJobStatusListener(this, t);
			}
			if (code == Status.FAILED) {
				Exception e = event.getStatus().getException();
				if (e == null) {
					failImmediately(stack, "Failed to allocate host");
				}
				else {
					failImmediately(stack, event.getStatus().getMessage(), e);
				}
			}
			else if (code == Status.COMPLETED) {
				stack.setVar(TypeUtil.toString(A_NAME.getValue(stack)), t.getContact());
				stack.setVar(HOST, t.getVirtualContact());
				super.partialArgumentsEvaluated(stack);
				startRest(stack);
			}
		}
		catch (ExecutionException e) {
			failImmediately(stack, e);
		}
	}

	protected void _finally(VariableStack stack) throws ExecutionException {
		super._finally(stack);
		Scheduler s = (Scheduler) stack.getGlobal(SchedulerNode.SCHEDULER);
		Contact c = (Contact) stack.currentFrame().getVar(HOST);
		if (s != null && c != null) {
			s.releaseContact(c);
		}
	}
}

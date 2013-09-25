package org.globus.cog.abstraction.coaster.service.job.manager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

class PullThread extends Thread {

    Logger logger = Logger.getLogger(PullThread.class);

    /** 
       Cpus actively looking for work and not sleeping
     */
    private final LinkedList<Cpu> queue;
    
    /** 
       Cpus sleeping (tried to pull but found no work)
     */
    private final SortedSet<Cpu> sleeping;

    /**
       Time slept since last report in milliseconds 
     */
    private long sleepTime;
    
    /**
       Time slept in milliseconds
     */
    private long runTime;
    
    /** 
       Time of last time measurement in milliseconds 
     */
    private long last;
    
    private final BlockQueueProcessor bqp;

    public PullThread(BlockQueueProcessor bqp) {
        this.bqp = bqp;
        setName("PullThread");
        setDaemon(true);
        queue = new LinkedList<Cpu>();
        sleeping = new TreeSet<Cpu>(new Comparator<Cpu>() {
            public int compare(Cpu a, Cpu b) {
                double aq = a.getQuality();
                double bq = b.getQuality();
                if (aq > bq) {
                	return -1;
                }
                else if (aq < bq) {
                	return 1;
                }
                else {
                    // need some arbitrary ordering for different 
                    // cpus, otherwise cpus with the same quality
                    // will overwrite each other in the sleeping set
                    return System.identityHashCode(a) - System.identityHashCode(b);
                }
            }
        });
    }

    public synchronized void enqueue(Cpu cpu) {
        queue.add(cpu);
        notify();
    }

    public synchronized void sleep(Cpu cpu) {
        if (logger.isDebugEnabled()) {
            logger.debug("sleep: " + cpu);
        }
        sleeping.add(cpu);
    }

    public synchronized int sleepers() {
        return sleeping.size();
    }

    public synchronized Cpu getSleeper() {
        if (sleeping.isEmpty()) {
        	return null;
        }
        Cpu result = sleeping.first();
        sleeping.remove(result);
        return result;
    }

    /**
       Used to obtain Cpus for MPI jobs 
     */
    public synchronized List<Cpu> getSleepers(int count) {
        
        logger.debug("getSleepers");
        
        // Allocate space for count sleepers plus the one active Cpu
        List<Cpu> result = new ArrayList<Cpu>(count+1);

        while (result.size() < count) {
            Cpu sleeper = getSleeper();
            assert(sleeper != null);
            result.add(sleeper);
        }

        return result;
    }

    @Override
    public void run() {
        last = System.currentTimeMillis();
        while (true) {
            Cpu cpu;
            synchronized (this) {
                while (queue.isEmpty()) {
                    if (!awakeUseable()) {
                        try {
                            mwait(50);
                        }
                        catch (InterruptedException e) {
                            return;
                        }
                    }
                }
                cpu = queue.removeFirst();
            }
            cpu.pull();
        }
    }

    private boolean awakeUseable() {
        int seq = bqp.getQueueSeq();
        int sz = sleeping.size();
        Iterator<Cpu> i = sleeping.iterator();
        while (i.hasNext()) {
            Cpu cpu = i.next();
            if (cpu.getLastSeq() < seq) {
                enqueue(cpu);
                i.remove();
            }
        }
        return sz != sleeping.size();
    }

    private void mwait(int ms) throws InterruptedException {
        runTime += countAndResetTime();
        wait(ms);
        sleepTime += countAndResetTime();
        if (runTime + sleepTime > 10000) {
            logger.debug("time running (milliseconds): " + runTime);
            logger.debug("time sleeping (milliseconds):" + sleepTime);
            runTime = 0;
            sleepTime = 0;
        }
    }

    private long countAndResetTime() {
        long t = System.currentTimeMillis();
        long d = t - last;
        last = t;
        return d;
    }
}

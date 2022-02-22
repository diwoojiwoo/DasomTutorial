package com.roobo.vision.util;

import android.text.TextUtils;

import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by tianli on 16-3-17.
 */
public class SingleThreadExecutor implements Executor {

    private AtomicBoolean mGurad = new AtomicBoolean(false);

    private String mWorkName = null;

    private long mKeepAliveTime = 30000; // 30 seconds

    private LinkedList<Job> mJobQueue = new LinkedList<Job>();

    private volatile boolean mQuit = false;
    private Runnable mRunner = new Runnable() {
        @Override
        public void run() {
            while (true) {
                Runnable runnable = null;
                synchronized (mJobQueue) {
                    if (!mJobQueue.isEmpty()) {
                        Job job = mJobQueue.pollFirst();
                        if (job == null || job.mRunnable == null) {
                            continue;
                        }
                        runnable = job.mRunnable;
                        long ts = System.currentTimeMillis();
                        if (job.mTriggerTime > ts) {
                            try {
                                mJobQueue.addFirst(job);
                                mJobQueue.wait(job.mTriggerTime - ts);
                                continue;
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                if (runnable != null) {
                    // don't put runnable run invoke under synchronized block.
                    runnable.run();
                }
                synchronized (mJobQueue) {
                    if (mJobQueue.isEmpty()) {
                        try {
                            // Keep alive for mKeepLiveTime ms
                            mJobQueue.wait(mKeepAliveTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (mJobQueue.isEmpty()) {
                            break;
                        }
                    }
                }
            }
            mGurad.set(false);

            if (!mJobQueue.isEmpty()) {
                trigger();
            }
        }
    };

    public SingleThreadExecutor(String name) {
        mWorkName = name;
    }

    @Override
    public void execute(Runnable runnable) {
        executeDelayed(runnable, 0);
    }

    public void executeDelayed(Runnable runnable, int delay) {
        synchronized (mJobQueue) {
            if (mQuit) {
                return;
            }
            for (Job job : mJobQueue) {
                if (job != null && job.mRunnable == runnable) {
                    return;
                }
            }
            Job job = new Job(runnable, delay);
            int index = 0;
            for (Job cur : mJobQueue) {
                if (cur != null && job.mTriggerTime < cur.mTriggerTime) {
                    mJobQueue.add(index, job);
                    if (index == 0) {
                        mJobQueue.notifyAll();
                    }
                    return;
                }
                index++;
            }
            mJobQueue.addLast(job);
            if (mJobQueue.size() == 1) {
                mJobQueue.notifyAll();
            }
        }
        trigger();
    }

    public void remove(Runnable runnable) {
        synchronized (mJobQueue) {
            int index = 0;
            for (Job job : mJobQueue) {
                if (job != null && job.mRunnable == runnable) {
                    mJobQueue.remove(job);
                    if (index == 0) {
                        mJobQueue.notifyAll();
                    }
                    return;
                }
                index++;
            }
        }
    }

    public void quit() {
        synchronized (mJobQueue) {
            mQuit = true;
            mJobQueue.clear();
        }
    }

    private void trigger() {
        if (mGurad.compareAndSet(false, true)) {
            Thread thread = new Thread(mRunner);
            if (!TextUtils.isEmpty(mWorkName)) {
                thread.setName(mWorkName);
            }
            thread.start();
        }
    }

    private static class Job {

        Runnable mRunnable;
        long mTriggerTime;
        int mDelay = 0;

        public Job(Runnable runnable) {
            this(runnable, 0);
        }

        public Job(Runnable runnable, int delay) {
            mRunnable = runnable;
            mDelay = delay;
            mTriggerTime = System.currentTimeMillis() + delay;
        }
    }
}

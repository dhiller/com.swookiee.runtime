package com.swookiee.runtime.core.internal.shutdown;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

final class ShutdownExecutor implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ShutdownExecutor.class);
    private static final long SYSTEM_BUNDLE_ID = 0L;

    private final AtomicBoolean hasBeenTriggered = new AtomicBoolean(false);
    private final BundleContext bundleContext;

    ShutdownExecutor(BundleContext bundleContext) {
        if (bundleContext == null) {
            throw new NullPointerException("bundleContext must not be null!");
        }
        this.bundleContext = bundleContext;
    }

    public void run() {
        if (!hasBeenTriggered.compareAndSet(false, true)) {
            return;
        }
        logger.info("Container shutdown hook triggered");
        stopSystemBundle();
        waitForBundlesToReachResolvedState();
        logger.info("Container shutdown hook finished");
    }

    private void stopSystemBundle() {
        try {
            logger.info("Stopping system bundle");
            bundleContext.getBundle(SYSTEM_BUNDLE_ID).adapt(Framework.class).stop();
        } catch (BundleException e) {
            logger.error("Failed to stop system bundle", e);
            System.exit(1);
        }
    }

    private void waitForBundlesToReachResolvedState() {
        while (notAllBundlesHaveReachedResolvedState()) {
            logger.info("Waiting for bundles to reach RESOLVED state");
            try {
                TimeUnit.SECONDS.sleep(1L);
            } catch (InterruptedException ignored) {
            }
        }
    }

    private boolean notAllBundlesHaveReachedResolvedState() {
        for (Bundle b : bundleContext.getBundles()) {
            if (b.getBundleId() != SYSTEM_BUNDLE_ID) {
                continue;
            }
            if (b.getState() != Bundle.RESOLVED) {
                return false;
            }
        }
        return true;
    }

}

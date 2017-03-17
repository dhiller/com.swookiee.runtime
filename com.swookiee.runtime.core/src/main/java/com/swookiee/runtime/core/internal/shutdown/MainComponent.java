package com.swookiee.runtime.core.internal.shutdown;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class MainComponent {

    private static final Logger logger = LoggerFactory.getLogger(MainComponent.class);

    @Activate
    public void activate(BundleContext bundleContext) {
        logger.info("Enabling container shutdown hook");
        Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownExecutor(bundleContext)));
        logger.info("Container shutdown hook enabled");
    }

}

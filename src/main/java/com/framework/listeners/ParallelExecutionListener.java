package com.framework.listeners;

import com.framework.config.ConfigKeys;
import com.framework.config.ConfigReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IAlterSuiteListener;
import org.testng.xml.XmlSuite;

import java.util.List;

/**
 * Sets the scenario thread count at runtime from {@code parallel.threads}, so parallelism is configured
 * per environment (or with {@code -Dparallel.threads=N}) instead of being hard-coded in testng.xml.
 */
public class ParallelExecutionListener implements IAlterSuiteListener {

    private static final Logger LOG = LoggerFactory.getLogger(ParallelExecutionListener.class);

    @Override
    public void alter(List<XmlSuite> suites) {
        int threads = Math.max(1, ConfigReader.getInt(ConfigKeys.PARALLEL_THREADS, 1));
        suites.forEach(suite -> suite.setDataProviderThreadCount(threads));
        LOG.info("Running scenarios with {} parallel thread(s) [env={}]", threads, ConfigReader.getEnvironment());
    }
}

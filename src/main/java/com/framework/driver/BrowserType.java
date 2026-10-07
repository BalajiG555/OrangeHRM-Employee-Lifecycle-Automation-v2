package com.framework.driver;

import com.framework.exceptions.ConfigurationException;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.util.Arrays;
import java.util.Locale;

/**
 * Supported browsers. Each constant knows how to build its own options and local driver,
 * so adding a browser means adding one enum constant - no switch statements elsewhere.
 */
public enum BrowserType {

    CHROME {
        @Override
        public MutableCapabilities createOptions(boolean headless, WindowSize windowSize) {
            ChromeOptions options = new ChromeOptions();
            if (headless) {
                options.addArguments("--headless=new");
            }
            options.addArguments(CHROMIUM_ARGUMENTS);
            options.addArguments("--window-size=" + windowSize.width() + "," + windowSize.height());
            return options;
        }

        @Override
        public WebDriver createLocalDriver(MutableCapabilities options) {
            return new ChromeDriver((ChromeOptions) options);
        }
    },

    EDGE {
        @Override
        public MutableCapabilities createOptions(boolean headless, WindowSize windowSize) {
            EdgeOptions options = new EdgeOptions();
            if (headless) {
                options.addArguments("--headless=new");
            }
            options.addArguments(CHROMIUM_ARGUMENTS);
            options.addArguments("--window-size=" + windowSize.width() + "," + windowSize.height());
            return options;
        }

        @Override
        public WebDriver createLocalDriver(MutableCapabilities options) {
            return new EdgeDriver((EdgeOptions) options);
        }
    },

    FIREFOX {
        @Override
        public MutableCapabilities createOptions(boolean headless, WindowSize windowSize) {
            FirefoxOptions options = new FirefoxOptions();
            if (headless) {
                options.addArguments("-headless");
            }
            options.addArguments("--width=" + windowSize.width(), "--height=" + windowSize.height());
            return options;
        }

        @Override
        public WebDriver createLocalDriver(MutableCapabilities options) {
            return new FirefoxDriver((FirefoxOptions) options);
        }
    };

    private static final String[] CHROMIUM_ARGUMENTS = {
        "--disable-notifications",
        "--disable-search-engine-choice-screen",
        "--disable-dev-shm-usage",
    };

    /**
     * Builds browser options for local or remote (Grid) execution.
     */
    public abstract MutableCapabilities createOptions(boolean headless, WindowSize windowSize);

    /**
     * Starts a local browser; Selenium Manager resolves the driver binary automatically.
     */
    public abstract WebDriver createLocalDriver(MutableCapabilities options);

    /**
     * Resolves a browser from its configured name (case-insensitive).
     */
    public static BrowserType from(String name) {
        try {
            return valueOf(name.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ConfigurationException("Unsupported browser '" + name + "'. Supported: "
                    + Arrays.toString(values()), e);
        }
    }

    /**
     * Browser viewport size parsed from a {@code WIDTHxHEIGHT} string, e.g. {@code 1920x1080}.
     *
     * @param width  viewport width in pixels
     * @param height viewport height in pixels
     */
    public record WindowSize(int width, int height) {

        /**
         * Parses {@code 1920x1080}-style values.
         */
        public static WindowSize parse(String value) {
            String[] parts = value.toLowerCase(Locale.ROOT).split("x");
            if (parts.length != 2) {
                throw new ConfigurationException("Window size must look like 1920x1080 but was '" + value + "'.");
            }
            try {
                return new WindowSize(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()));
            } catch (NumberFormatException e) {
                throw new ConfigurationException("Window size must look like 1920x1080 but was '" + value + "'.", e);
            }
        }
    }
}

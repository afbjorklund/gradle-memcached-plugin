package com.github.afbjorklund.gradle.caching.memcached;

import org.gradle.caching.configuration.AbstractBuildCache;

/**
 * Build cache configuration for memcached backends. Pushing to this cache is enabled by default.
 *
 * <p>Configuration via {@code settings.gradle}:</p>
 *
 * <pre>
 * buildCache {
 *     remote(com.github.afbjorklund.memcached.MemcachedBuildCache) {
 *         // ...
 *     }
 * }
 * </pre>
 */
public class MemcachedBuildCache extends AbstractBuildCache {

    private String host;
    private int port;

    public MemcachedBuildCache() {
        this.host = getHostValue();
        this.port = getPortValue();
    }

    private static String getHostValue() {
        return System.getProperty("com.github.afbjorklund.gradle.caching.memcached.host", "127.0.0.1");
    }

    private static int getPortValue() {
        String portString = System.getProperty("com.github.afbjorklund.gradle.caching.memcached.port", "11211");
        int portValue;
        try {
            portValue = Integer.parseInt(portString);
        } catch (NumberFormatException e) {
            portValue = 11211;
        }
        return portValue;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}

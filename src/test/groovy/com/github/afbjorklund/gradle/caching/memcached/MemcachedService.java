package com.github.afbjorklund.gradle.caching.memcached;

import org.junit.rules.ExternalResource;

import java.lang.Runtime;

public class MemcachedService extends ExternalResource {

    private final int port;
    private Process process;

    public MemcachedService() {
        this(11211);
    }

    public MemcachedService(int port) {
        this.port = port;
    }

    @Override
    protected void before() throws Throwable {
        process = Runtime.getRuntime().exec("memcached -p " + port);
    }

    public int getPort() {
        return port;
    }

    @Override
    protected void after() {
        process.destroy();
    }
}

package com.github.afbjorklund.gradle.caching.memcached;

import net.spy.memcached.MemcachedClient;
import org.gradle.api.Plugin;
import org.gradle.api.initialization.Settings;
import org.gradle.caching.BuildCacheService;
import org.gradle.caching.BuildCacheServiceFactory;
import org.gradle.caching.MapBasedBuildCacheService;
import org.gradle.caching.configuration.BuildCacheConfiguration;

import javax.annotation.Nonnull;
import java.util.concurrent.ConcurrentMap;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * {@link Settings} plugin to register memcached as a build cache backend.
 *
 * @see MemcachedBuildCache
 */
public class MemcachedPlugin implements Plugin<Settings> {
    @Override
    public void apply(@Nonnull Settings settings) {
        BuildCacheConfiguration buildCacheConfiguration = settings.getBuildCache();
        buildCacheConfiguration.registerBuildCacheService(MemcachedBuildCache.class, MemcachedBuildCacheServiceFactory.class);
        // Use memcached as remote cache and disable local cache
        buildCacheConfiguration.getLocal().setEnabled(false);
        buildCacheConfiguration.remote(MemcachedBuildCache.class);
    }

    static class MemcachedBuildCacheServiceFactory implements BuildCacheServiceFactory<MemcachedBuildCache> {
        @Override
        public BuildCacheService createBuildCacheService(@Nonnull MemcachedBuildCache cacheConfig, @Nonnull Describer describer) {
			try {
                MemcachedClient client = new MemcachedClient(
                    new InetSocketAddress(cacheConfig.getHost(), cacheConfig.getPort()));
                ConcurrentMap<String,byte[]> map = new MemcachedCacheMap(client);
                return new MapBasedBuildCacheService(map);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
		}
    }
}

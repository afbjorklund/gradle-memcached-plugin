# Gradle Memcached cache backend plugin

A simple [settings plugin](https://docs.gradle.org/current/dsl/org.gradle.api.initialization.Settings.html) that enables [build caching](https://guides.gradle.org/using-build-cache/) in Gradle with [Memcached](http://memcached.org) as the backend. The Memcached servers need to be set up separately.
Based on the [Hazelcast cache backend plugin](https://github.com/sinwe/gradle-hazelcast-plugin).

For a production-ready build cache implementation (featuring node management, usage statistics, health monitoring, replication, access control and more), see [Gradle Enteprise](https://gradle.com/build-cache).

## How to use

Add this to your `settings.gradle`:

```groovy
buildscript {
  repositories {
    mavenCentral()
  }

  dependencies {
    classpath "com.github.afbjorklund.gradle.caching.memcached:gradle-memcached-plugin:0.10"
  }
}

apply plugin: "com.github.afbjorklund.gradle.caching.memcached"

buildCache {
  // Note: the local cache is disabled by default when applying the plugin
  remote(com.github.afbjorklund.gradle.caching.memcached.MemcachedBuildCache) {
    host = "127.0.0.1"  //support comma separated multiple hosts
    port = 11211
    enabled = true
    push = true
  }
}
```

You can also specify the location and name of the Memcached cache via system properties (though values specified in the `settings.gradle` override the ones specified by system properties):

System property                                        | Function                        | Default value
------------------------------------------------------ | ------------------------------- | ------------
`com.github.afbjorklund.gradle.caching.memcached.host` | host name of the memcached node | `127.0.0.1`
`com.github.afbjorklund.gradle.caching.memcached.port` | TCP port of the memcached node  | `11211`

/**
 * Copyright (C) 2006-2009 Dustin Sallings
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALING
 * IN THE SOFTWARE.
 */

package com.github.afbjorklund.gradle.caching.memcached;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;

import net.spy.memcached.CASValue;
import net.spy.memcached.CASResponse;
import net.spy.memcached.MemcachedClientIF;
import net.spy.memcached.transcoders.Transcoder;

/**
 * Base class for a ConcurrentMap interface to memcached.
 *
 * <p>
 * This Map interface makes memcached a bit easier to use for some purposes by
 * providing a limited Map implementation.
 * </p>
 *
 * <p>
 * Do note that nothing that iterates over the map will work (such is
 * memcached). All iteration mechanisms will return empty iterators and such.
 * </p>
 *
 * @param <V> the type of value taken and returned by this Map's underlying
 *          transcoder, and thus taken and returned by this Map.
 */
public class MemcachedBaseCacheMap<V> implements ConcurrentMap<String, V> {

  private final String keyPrefix;
  private final Transcoder<V> transcoder;
  private final MemcachedClientIF client;
  private final int exp;

  /**
   * Build a MemcachedBaseCacheMap.
   *
   * @param c the underlying client
   * @param expiration the expiration for objects set through this Map
   * @param prefix a prefix to ensure objects in this map are unique
   * @param t the transcoder to serialize and deserialize objects
   */
  public MemcachedBaseCacheMap(MemcachedClientIF c, int expiration, String prefix,
      Transcoder<V> t) {
        super();
        keyPrefix = prefix;
        transcoder = t;
        client = c;
        exp = expiration;
  }

  public void clear() {
    // TODO: Support a rolling key generation.
    throw new UnsupportedOperationException();
  }

  private String getKey(String k) {
    return keyPrefix + k;
  }

  public boolean containsKey(Object key) {
    return get(key) != null;
  }

  /**
   * This method always returns false, as truth cannot be determined without
   * iteration.
   */
  public boolean containsValue(Object value) {
    return false;
  }

  public Set<Map.Entry<String, V>> entrySet() {
    return Collections.emptySet();
  }

  public V get(Object key) {
    V rv = null;
    try {
      rv = client.get(getKey((String) key), transcoder);
    } catch (ClassCastException e) {
      // Most likely, this is because the key wasn't a String.
      // Either way, it's a no.
    }
    //System.out.println("get " + key + " " + ((rv != null) ? ((byte[]) rv).length : 0));
    return rv;
  }

  public boolean isEmpty() {
    return false;
  }

  public Set<String> keySet() {
    return Collections.emptySet();
  }

  public void putAll(Map<? extends String, ? extends V> t) {
    for (Map.Entry<? extends String, ? extends V> me : t.entrySet()) {
      client.set(getKey(me.getKey()), exp, me.getValue());
    }
  }

  public V remove(Object key) {
    V rv = null;
    try {
      rv = get(key);
      client.delete(getKey((String) key));
    } catch (ClassCastException e) {
      // Not a string key. Ignore.
    }
    return rv;
  }

  public boolean remove(Object key, Object value) {
    try {
      String k = (String) key;
      @SuppressWarnings("unchecked")
      V v = (V) value;
      Future<V> rc = client.asyncGet(getKey(k), transcoder);
      if (rc != null && rc.get() == v) {
        return client.delete(getKey(k)).get().booleanValue();
      }
    } catch (ClassCastException e) {
      return false;
    } catch (ExecutionException e) {
      return false;
    } catch (InterruptedException e) {
      return false;
    }
    return false;
  }

  public int size() {
    return 0;
  }

  public Collection<V> values() {
    return Collections.emptySet();
  }

  public V put(String key, V value) {
    //System.out.println("put " + key + " " + ((value != null) ? ((byte[]) value).length : 0));
    V rv = get(key);
    client.set(getKey(key), exp, value);
    return rv;
  }

  public V replace(String key, V value) {
    V rv = get(key);
    client.replace(getKey(key), exp, value);
    return rv;
  }

  public boolean replace(String key, V oldValue, V newValue) {
    CASValue<V> rc = client.gets(key, transcoder);
    if (rc != null && rc.getValue() == oldValue) {
      return client.cas(getKey(key), rc.getCas(), exp, newValue, transcoder) == CASResponse.OK;
    }
    return false;
  }

  public V putIfAbsent(String key, V value) {
    V rv = get(key);
    try {
      client.replace(getKey(key), exp, value).get();
    } catch (ExecutionException e) {
    } catch (InterruptedException e) {
    }
    return rv;
  }
}

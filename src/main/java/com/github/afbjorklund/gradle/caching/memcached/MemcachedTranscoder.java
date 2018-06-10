package com.github.afbjorklund.gradle.caching.memcached;

import net.spy.memcached.CachedData;
import net.spy.memcached.transcoders.Transcoder;

public class MemcachedTranscoder implements Transcoder<byte[]> {

  private static final int SPECIAL_MASK = 0xff00;
  static final int SPECIAL_BYTEARRAY = (8 << 8);

  public boolean asyncDecode(CachedData d)
  {
    return true;
  }

  public CachedData encode(byte[] o)
  {
    byte[] data = o;
    int flags = SPECIAL_BYTEARRAY;
    return new CachedData(flags, data, getMaxSize());
  }

  public byte[] decode(CachedData d)
  {
    int flags = d.getFlags() & SPECIAL_MASK;
    byte[] data = d.getData();
    assert((flags & SPECIAL_BYTEARRAY) != 0);
    return data;
  }

  public int getMaxSize()
  {
    return CachedData.MAX_SIZE;
  }
}
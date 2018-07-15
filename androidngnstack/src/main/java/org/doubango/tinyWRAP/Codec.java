/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.doubango.tinyWRAP;

public class Codec {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected Codec(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(Codec obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() throws Throwable {
	  super.finalize();
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        tinyWRAPJNI.delete_Codec(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public twrap_media_type_t getMediaType() {
    return twrap_media_type_t.swigToEnum(tinyWRAPJNI.Codec_getMediaType(swigCPtr, this));
  }

  public String getName() {
    return tinyWRAPJNI.Codec_getName(swigCPtr, this);
  }

  public String getDescription() {
    return tinyWRAPJNI.Codec_getDescription(swigCPtr, this);
  }

  public String getNegFormat() {
    return tinyWRAPJNI.Codec_getNegFormat(swigCPtr, this);
  }

  public int getAudioSamplingRate() {
    return tinyWRAPJNI.Codec_getAudioSamplingRate(swigCPtr, this);
  }

  public int getAudioChannels() {
    return tinyWRAPJNI.Codec_getAudioChannels(swigCPtr, this);
  }

  public int getAudioPTime() {
    return tinyWRAPJNI.Codec_getAudioPTime(swigCPtr, this);
  }

}

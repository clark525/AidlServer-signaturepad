// SignAidlInterface.aidl
package com.hangc.signaturepad;

// Declare any non-default types here with import statements

interface SignAidlInterface {

   int satrtSign(int timeOut, int startX, int endX, int startY, int endY, String pngPath, String txtPath , out byte[] message);

   int cancelSign(out byte[] message);

   int clearSign(out byte[] message);

   int confirmSign(out byte[] message);

   int getRandom();

}
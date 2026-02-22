# Add project specific ProGuard rules here.
-keep class com.hanu.pdfconverter.** { *; }
-keep class org.apache.poi.** { *; }
-keep class com.tom_roush.pdfbox.** { *; }
-dontwarn org.apache.poi.**
-dontwarn com.tom_roush.pdfbox.**
-dontwarn org.bouncycastle.**

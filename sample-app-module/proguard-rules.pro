# kontakt.io SDK configuration
-keep class com.kontakt.sdk.android.** {
    *;
}
-keep public class com.kontakt.sdk.core.data.** {
    *;
}

# kontakt.io SDK configuration
# From the very beginning of Android SDK existence
# there was also a Core SDK for Java SE providing REST API interaction only.
# To some extent the Android SDK derives some classes from the Core SDK.
# However, at some point the Android-specific functionalities were introduced
# e.g. implementing Parcelable interface in models.
# For the packages below, both of which come from the Core SDK
# there are Android equivalents. Thus, we do not care
# about them and allow Proguard to take care of them during shrinking process.
-dontwarn com.kontakt.sdk.core.http.**
-dontwarn com.kontakt.sdk.core.data.changelog.**

# ButterKnife configuration copied from the official website http://jakewharton.github.io/butterknife/
-printmapping mapping.txt
-printseeds seeds.txt
-printusage unused.txt

-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewInjector { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

-dontwarn okio.**
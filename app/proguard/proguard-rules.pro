# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-dontobfuscate

# AGPBI META-INF/services warnings
-dontwarn org.apache.xalan.extensions.bsf.BSFManager
-dontwarn org.w3c.dom.DOMImplementationSourceList
-dontwarn org.xml.sax.driver

# For AndroidX Paging, seems to be a library config error
-keep class org.xml.sax.helpers.NamespaceSupport$Context

# For AndroidX Compose, seems to be a library config error
-keep class org.xml.sax.helpers.AttributesImpl

-dontwarn edu.umd.cs.findbugs.annotations.SuppressFBWarnings
-dontwarn java.awt.font.FontRenderContext
-dontwarn java.awt.font.LineBreakMeasurer
-dontwarn java.awt.font.TextLayout
-dontwarn java.awt.geom.AffineTransform
-dontwarn java.beans.BeanDescriptor
-dontwarn java.beans.BeanInfo
-dontwarn java.beans.IntrospectionException
-dontwarn java.beans.Introspector
-dontwarn java.beans.PropertyDescriptor
-dontwarn javax.imageio.ImageIO
-dontwarn javax.imageio.ImageReader
-dontwarn javax.imageio.stream.ImageInputStream
-dontwarn javax.lang.model.SourceVersion
-dontwarn javax.naming.InvalidNameException
-dontwarn javax.naming.NamingException
-dontwarn javax.naming.directory.Attribute
-dontwarn javax.naming.directory.Attributes
-dontwarn javax.naming.ldap.LdapName
-dontwarn javax.naming.ldap.Rdn
-dontwarn javax.servlet.ServletContainerInitializer
-dontwarn org.apache.bsf.BSFManager
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.ietf.jgss.GSSContext
-dontwarn org.ietf.jgss.GSSCredential
-dontwarn org.ietf.jgss.GSSException
-dontwarn org.ietf.jgss.GSSManager
-dontwarn org.ietf.jgss.GSSName
-dontwarn org.ietf.jgss.Oid
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE

-dontwarn com.google.errorprone.annotations.CanIgnoreReturnValue
-dontwarn com.google.errorprone.annotations.CheckReturnValue
-dontwarn com.google.errorprone.annotations.Immutable
-dontwarn com.google.errorprone.annotations.RestrictedApi

# Cronet embedded
-dontwarn org.chromium.net.ThreadStatsUid
-dontwarn org.chromium.net.impl.CallbackExceptionImpl
-dontwarn org.chromium.net.impl.CronetEngineBase
-dontwarn org.chromium.net.impl.CronetEngineBuilderImpl$Pkp
-dontwarn org.chromium.net.impl.CronetEngineBuilderImpl$QuicHint
-dontwarn org.chromium.net.impl.CronetEngineBuilderImpl
-dontwarn org.chromium.net.impl.CronetExceptionImpl
-dontwarn org.chromium.net.impl.CronetLogger$CronetEngineBuilderInfo
-dontwarn org.chromium.net.impl.CronetLogger$CronetSource
-dontwarn org.chromium.net.impl.CronetLogger$CronetTrafficInfo
-dontwarn org.chromium.net.impl.CronetLogger$CronetVersion
-dontwarn org.chromium.net.impl.CronetLogger
-dontwarn org.chromium.net.impl.CronetLoggerFactory
-dontwarn org.chromium.net.impl.ImplVersion
-dontwarn org.chromium.net.impl.NetworkExceptionImpl
-dontwarn org.chromium.net.impl.Preconditions
-dontwarn org.chromium.net.impl.QuicExceptionImpl
-dontwarn org.chromium.net.impl.RequestFinishedInfoImpl
-dontwarn org.chromium.net.impl.UrlRequestBase
-dontwarn org.chromium.net.impl.UrlResponseInfoImpl$HeaderBlockImpl
-dontwarn org.chromium.net.impl.UrlResponseInfoImpl
-dontwarn org.chromium.net.impl.UserAgent
-dontwarn org.chromium.net.impl.VersionSafeCallbacks$BidirectionalStreamCallback
-dontwarn org.chromium.net.impl.VersionSafeCallbacks$LibraryLoader
-dontwarn org.chromium.net.impl.VersionSafeCallbacks$NetworkQualityRttListenerWrapper
-dontwarn org.chromium.net.impl.VersionSafeCallbacks$NetworkQualityThroughputListenerWrapper
-dontwarn org.chromium.net.impl.VersionSafeCallbacks$RequestFinishedInfoListener
-dontwarn org.chromium.net.impl.VersionSafeCallbacks$UploadDataProviderWrapper
-dontwarn org.chromium.net.impl.VersionSafeCallbacks$UrlRequestCallback
-dontwarn org.chromium.net.impl.VersionSafeCallbacks$UrlRequestStatusListener

# For export zip
-keep class org.apache.commons.compress.archivers.zip.** { *; }

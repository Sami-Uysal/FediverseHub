## Release rules kept narrow. Compose/Hilt/Room/Ktor mostly ship consumer rules.

-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,AnnotationDefault,Signature,InnerClasses,EnclosingMethod

## Keep generated kotlinx serializers reachable for API DTOs.
-keepclassmembers class **$$serializer { *; }
-keepclassmembers class **$Companion { kotlinx.serialization.KSerializer serializer(...); }
-keep @kotlinx.serialization.Serializable class com.samiuysal.fediversehub.** { *; }

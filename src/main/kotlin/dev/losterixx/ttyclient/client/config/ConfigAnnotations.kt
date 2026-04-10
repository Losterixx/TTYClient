package dev.losterixx.ttyclient.client.config

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Comment(vararg val lines: String)


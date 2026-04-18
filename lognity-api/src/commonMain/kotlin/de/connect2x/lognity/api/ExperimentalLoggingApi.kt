package de.connect2x.lognity.api

@RequiresOptIn(message = "The API you are trying to use is experimental and may change at any time")
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.CONSTRUCTOR)
annotation class ExperimentalLoggingApi

package com.uni.remote.tech.compositebuild

import org.gradle.api.artifacts.dsl.DependencyHandler

internal fun DependencyHandler.implementation(dependency: Any) = add("implementation", dependency)
internal fun DependencyHandler.annotationProcessor(dependency: Any) =
    add("annotationProcessor", dependency)

internal fun DependencyHandler.kapt(dependency: Any) = add("kapt", dependency)
internal fun DependencyHandler.ksp(dependency: Any) = add("ksp", dependency)
internal fun DependencyHandler.api(dependency: Any) = add("api", dependency)
internal fun DependencyHandler.kaptAndroidTest(dependency: Any) = add("kaptAndroidTest", dependency)
internal fun DependencyHandler.androidTestImplementation(dependency: Any) =
    add("androidTestImplementation", dependency)
package gg.grounds.runtime

import javax.inject.Inject
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

abstract class GroundsRuntimeExtension @Inject constructor(objects: ObjectFactory) {
    val runtimeVersion: Property<String> = objects.property(String::class.java)

    val allowBundledSharedRuntime: Property<Boolean> =
        objects.property(Boolean::class.java).convention(false)
}

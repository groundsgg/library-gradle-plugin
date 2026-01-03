package gg.grounds.gradle.common

import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

open class GroundsExtension(objects: ObjectFactory) {
    val addDependencies: Property<Boolean> = objects.property(Boolean::class.java).convention(true)
}

internal fun Project.getGroundsExtension(): GroundsExtension {
    return extensions.findByType(GroundsExtension::class.java)
        ?: extensions.create("grounds", GroundsExtension::class.java, objects)
}

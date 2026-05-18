package gg.grounds

import gg.grounds.runtime.RuntimeConventionFlavor
import gg.grounds.runtime.configureVelocityRuntimeConvention

plugins {
    id("com.github.gmazzo.buildconfig")
    id("gg.grounds.paper-base-conventions")
}

configureVelocityRuntimeConvention(RuntimeConventionFlavor.Standalone)

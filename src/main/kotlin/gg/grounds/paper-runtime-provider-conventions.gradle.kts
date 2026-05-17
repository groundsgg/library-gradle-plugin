package gg.grounds

import gg.grounds.runtime.RuntimeConventionFlavor
import gg.grounds.runtime.configurePaperRuntimeConvention

plugins { id("gg.grounds.paper-base-conventions") }

configurePaperRuntimeConvention(RuntimeConventionFlavor.RuntimeProvider)

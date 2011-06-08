package org.digimead.documentumelasticus.ui.overview.regstateblock

import com.sun.star.lang.XMultiComponentFactory
import com.sun.star.uno.XComponentContext
import org.digimead.documentumelasticus.ui.OControlContainer

class RegisterPersonalPlus(tmcf: XMultiComponentFactory,
    tprefix: String,
    tparent: OControlContainer,
    tctx: XComponentContext) extends RegisterOption(tmcf,
        "RegisterPersonalPlus",
        "Plus",
        tprefix,
        Array("Satisfaction", "Updates", "Influence"),
        tparent,
        tctx) {

}
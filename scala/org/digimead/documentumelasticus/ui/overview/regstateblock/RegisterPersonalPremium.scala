package org.digimead.documentumelasticus.ui.overview.regstateblock

import com.sun.star.lang.XMultiComponentFactory
import com.sun.star.uno.XComponentContext
import org.digimead.documentumelasticus.ui.OControlContainer

class RegisterPersonalPremium(tmcf: XMultiComponentFactory,
    tprefix: String,
    tparent: OControlContainer,
    tctx: XComponentContext) extends RegisterOption(tmcf,
        "RegisterPersonalPremium",
        "Premium",
        tprefix,
        Array("Satisfaction", "Updates", "Influence", "Support"),
        tparent,
        tctx) {

}
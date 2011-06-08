package org.digimead.documentumelasticus.ui.overview.regstateblock

import com.sun.star.lang.XMultiComponentFactory
import com.sun.star.uno.XComponentContext
import org.digimead.documentumelasticus.ui.OControlContainer

class RegisterCommercialPremium(tmcf: XMultiComponentFactory,
    tprefix: String,
    tparent: OControlContainer,
    tctx: XComponentContext) extends RegisterOption(tmcf,
        "RegisterCommercialPremium",
        "Premium",
        tprefix,
        Array(""),
        tparent,
        tctx) {

}
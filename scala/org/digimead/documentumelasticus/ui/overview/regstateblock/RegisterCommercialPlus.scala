package org.digimead.documentumelasticus.ui.overview.regstateblock

import com.sun.star.lang.XMultiComponentFactory
import com.sun.star.uno.XComponentContext
import org.digimead.documentumelasticus.ui.OControlContainer

class RegisterCommercialPlus(tmcf: XMultiComponentFactory,
    tbase: String,
    tparent: OControlContainer,
    tctx: XComponentContext) extends RegisterOption(tmcf,
        "RegisterCommercialPlus",
        "Plus",
        tbase + "PPlus.png",
        tbase + "sign.png",
        Array(("", "")),
        tparent,
        tctx) {

}
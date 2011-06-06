package org.digimead.documentumelasticus.ui.overview.regstateblock

import com.sun.star.lang.XMultiComponentFactory
import com.sun.star.uno.XComponentContext
import org.digimead.documentumelasticus.ui.OControlContainer

class RegisterCommercialLight(tmcf: XMultiComponentFactory,
    tbase: String,
    tparent: OControlContainer,
    tctx: XComponentContext) extends RegisterOption(tmcf,
        "RegisterCommercialLight",
        "Light",
        tbase + "PLight.png",
        tbase + "sign.png",
        Array(("", "")),
        tparent,
        tctx) {

}
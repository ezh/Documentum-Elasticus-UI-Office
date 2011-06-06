package org.digimead.documentumelasticus.ui.overview.regstateblock

import com.sun.star.lang.XMultiComponentFactory
import com.sun.star.uno.XComponentContext
import org.digimead.documentumelasticus.ui.OControlContainer

class RegisterPersonalPremium(tmcf: XMultiComponentFactory,
    tbase: String,
    tparent: OControlContainer,
    tctx: XComponentContext) extends RegisterOption(tmcf,
        "RegisterPersonalPremium",
        "Premium",
        tbase + "PPremium.png",
        tbase + "sign.png",
        Array((tbase + "satisfaction.png", "Satisfaction"), (tbase + "updates.png", "Updates"), (tbase + "influence.png", "Influence"), (tbase + "support.png", "Support")),
        tparent,
        tctx) {

}
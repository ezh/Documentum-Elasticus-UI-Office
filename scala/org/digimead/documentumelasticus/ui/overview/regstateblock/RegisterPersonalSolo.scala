package org.digimead.documentumelasticus.ui.overview.regstateblock

import com.sun.star.lang.XMultiComponentFactory
import com.sun.star.uno.XComponentContext
import org.digimead.documentumelasticus.ui.OControlContainer

class RegisterPersonalSolo(tmcf: XMultiComponentFactory,
    tbase: String,
    tparent: OControlContainer,
    tctx: XComponentContext) extends RegisterOption(tmcf,
        "RegisterPersonalSolo",
        "Solo",
        tbase + "PSolo.png",
        tbase + "sign.png",
        Array((tbase + "satisfaction.png", "Satisfaction"), (tbase + "updates.png", "Updates")),
        tparent,
        tctx) {

}
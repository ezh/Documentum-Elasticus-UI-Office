package org.digimead.documentumelasticus.ui.overview

import com.sun.star.lang.XMultiComponentFactory
import org.digimead.documentumelasticus.ui.OControlContainer
import com.sun.star.uno.XComponentContext
import org.digimead.documentumelasticus.ui.MigLayout

class NewsBlock(tmcf: XMultiComponentFactory,
    tparent: OControlContainer,
    tctx: XComponentContext) extends OControlContainer(tmcf,
        "NewsBlock",
        tparent,
        tctx,
        new MigLayout("debug")) {
}
package org.digimead.documentumelasticus.ui.overview.regstateblock

import org.digimead.documentumelasticus.ui.OControlContainer
import com.sun.star.uno.XComponentContext
import com.sun.star.lang.XMultiComponentFactory
import org.digimead.documentumelasticus.ui.MigLayout

class Registered(tmcf: XMultiComponentFactory,
    tparent: OControlContainer,
    tctx: XComponentContext) extends OControlContainer(tmcf,
        "Registered",
        tparent,
        tctx,
        new MigLayout("debug, insets 0, gap 0")) {
  def reload() {
    logger.trace("reload")
    
  }
  def update() {
    
  }

}
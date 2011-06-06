package org.digimead.documentumelasticus.ui

import com.sun.star.uno.XComponentContext
import com.sun.star.frame.XFrame
import org.slf4j.LoggerFactory
import com.sun.star.lang.EventObject
import com.sun.star.awt.{WindowEvent, XWindowListener}
import overview.RegStateBlock
import overview.ComponentsBlock
import overview.NewsBlock

class Overview(tname: String,
               tparentFrame: XFrame,
               tctx: XComponentContext) extends Tab(tname, tparentFrame, tctx) {
  val logger = LoggerFactory.getLogger(this.getClass)
  val (frame, container) = init(new MigLayout("debug, nocache", "[c, grow, fill]", "[c][c, grow, fill]"))
  container.model.setPropertyValue("BackgroundColor", 0x000000FF)
  var uiRegStateBlock = new RegStateBlock(mcf, container, ctx)
  var uiComponentsBlock = new ComponentsBlock(mcf, container, ctx)
  var uiNewsBlock = new NewsBlock(mcf, container, ctx)
  
  def show() {}
  def refresh() {}
  def reload() {
    logger.trace("reload")
    uiRegStateBlock.reload()
    //uiComponentsBlock.reload()
    //uiNewsBlock.reload()
  }
  // -------------
  // - listeners -
  // -------------
  class WindowListener extends XWindowListener {
    // --------------------------------------
    // - implement trait XWindowListener -
    // --------------------------------------
    def windowHidden(e: EventObject) {
      logger.trace("window hidden")
    }

    def windowShown(e: EventObject) {
      logger.trace("window shown")
    }

    def windowMoved(e: WindowEvent) {
      logger.trace("window moved")
    }

    def windowResized(e: WindowEvent) {
      logger.trace("window resized")
    }

    // ----------------------------------
    // - implement trait XEventListener -
    // ----------------------------------
    def disposing(e: EventObject) {
      logger.trace("disposing")
    }
  }

}

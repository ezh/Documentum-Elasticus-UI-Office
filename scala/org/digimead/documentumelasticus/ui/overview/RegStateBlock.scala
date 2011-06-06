package org.digimead.documentumelasticus.ui.overview

import com.sun.star.lang.XMultiComponentFactory
import com.sun.star.uno.XComponentContext
import org.digimead.documentumelasticus.helper.Library
import org.digimead.documentumelasticus.ui.{MigLayout, OControlContainer}
import regstateblock.{Unregistered, Registered}
import org.digimead.documentumelasticus.helper.O
import com.sun.star.beans.XIntrospection
import org.slf4j.LoggerFactory
import org.digimead.documentumelasticus.ui.OControlContainer

class RegStateBlock(mcf: XMultiComponentFactory,
    parent: OControlContainer,
    ctx: XComponentContext) {
  private val logger = LoggerFactory.getLogger(this.getClass)
  val container = new OControlContainer(mcf,
        this.toString,
        parent,
        ctx,
        new MigLayout())
  load
  //update
  private val configuration = O.configuration(ctx)
  val registered = new Registered(mcf, container, ctx)
  val unregistered = new Unregistered(mcf, container, ctx)
  def reload() {
    logger.trace("reload")
    unregistered.reload()
    registered.reload()
    load()
    container.layout(true)
  }
  def load() {
    val opt: Array[String] = try {
      Library.invokeMacro(mcf,
        "vnd.sun.star.script:DE_UI_OFFICE.Overview." + getClass.getName.split('.').last + "?language=Basic&location=application",
        ctx).asInstanceOf[Array[String]]
    } catch {
      case e ⇒
        logger.error("Subroutine vnd.sun.star.script:DE_UI_OFFICE.Overview." + getClass.getName.split('.').last + "?language=Basic&location=application failed: " + e.getMessage, e)
        Array("", "", "", "")
    }
    container.getLayout.setLayoutConstraints(opt(0))
    container.getLayout.setColumnConstraints(opt(1))
    container.getLayout.setRowConstraints(opt(2))
    container.setConstraint(opt(3))
  }
  def update {
   if (configuration.getExtensionState.registered == true) {
      logger.debug("show block for registered users")
      registered.update
      unregistered.setVisible(false)
      registered.setVisible(true)
    } else {
      logger.debug("show block for unregistered users")
      unregistered.update
      registered.setVisible(false)
      unregistered.setVisible(true)
    }
  }
}
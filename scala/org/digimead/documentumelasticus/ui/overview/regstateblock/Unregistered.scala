package org.digimead.documentumelasticus.ui.overview.regstateblock

import com.sun.star.lang.XMultiComponentFactory
import com.sun.star.uno.XComponentContext
import org.digimead.documentumelasticus.helper.E
import org.digimead.documentumelasticus.ui.helper.Tabs
import org.digimead.documentumelasticus.ui.{Office, OControl, MigLayout, OControlContainer}
import org.digimead.documentumelasticus.helper.Library

class Unregistered(tmcf: XMultiComponentFactory,
    tparent: OControlContainer,
    tctx: XComponentContext) extends OControlContainer(tmcf,
        "Unregistered",
        tparent,
        tctx,
        new MigLayout()) {
  private var isPersonal = true
  private val imagePathPrefix = E.getURL(classOf[Office], ctx) + "images/regstateblock/unregistered/"
  /*
   * left side - controls
   */
  val left = new OControlContainer(mcf, "Left", this, ctx, new MigLayout())
  val tab = new Tabs("UnregisteredTab", imagePathPrefix, left, ctx)
  tab.add("UnregisteredTabCommercial", null)
  tab.add("UnregisteredTabPersonal", null)
  /*
   * right side
   */
  val right = new OControlContainer(mcf, "Right", this, ctx, new MigLayout())
  // commercial
  val commercial = new OControlContainer(mcf, "Commercial", right, ctx, new MigLayout())
  val uiRegisterCommercialPremium = new RegisterCommercialPremium(mcf,  imagePathPrefix, commercial, ctx)
  val uiRegisterCommercialPlus = new RegisterCommercialPlus(mcf, imagePathPrefix, commercial, ctx)
  val uiRegisterCommercialLight = new RegisterCommercialLight(mcf, imagePathPrefix, commercial, ctx)
  // personal
  val personal = new OControlContainer(mcf, "Personal", right, ctx, new MigLayout())
  val uiRegisterPersonalPremium = new RegisterPersonalPremium(mcf, imagePathPrefix, personal, ctx)
  val uiRegisterPersonalPlus = new RegisterPersonalPlus(mcf, imagePathPrefix, personal, ctx)
  val uiRegisterPersonalSolo = new RegisterPersonalSolo(mcf, imagePathPrefix, personal, ctx)
  // refund label
  val refundTargetLabel = new OControl(mcf, "RefundTargetLabel", "UnoControlFixedText", right, ctx)
  refundTargetLabel.model.setPropertyValue("Label", "all funds for developing + your price for your design")
  load()
  update()
  def reload() {
    logger.trace("reload")
    tab.reload()
    uiRegisterCommercialPremium.reload()
    uiRegisterCommercialPlus.reload()
    uiRegisterCommercialLight.reload()
    uiRegisterPersonalPremium.reload()
    uiRegisterPersonalPlus.reload()
    uiRegisterPersonalSolo.reload()
    load()
    layout(true)
  }
  def load() {
    val opt: Array[String] = try {
      Library.invokeMacro(mcf,
        "vnd.sun.star.script:DE_UI_OFFICE.Overview." + getClass.getName.split('.').last + "?language=Basic&location=application",
        ctx).asInstanceOf[Array[String]]
    } catch {
      case e ⇒
        logger.error("Subroutine vnd.sun.star.script:DE_UI_OFFICE.Overview." + getClass.getName.split('.').last + "?language=Basic&location=application failed: ", e)
        Array("", "", "", "")
    }
    getLayout.setLayoutConstraints(opt(0))
    getLayout.setColumnConstraints(opt(1))
    getLayout.setRowConstraints(opt(2))
    setConstraint(opt(3))
    /*
     * left
     */
    left.getLayout.setLayoutConstraints(opt(4))
    left.getLayout.setColumnConstraints(opt(5))
    left.getLayout.setRowConstraints(opt(6))
    left.setConstraint(opt(7))
    // UnregisteredTab
    // UnregisteredTabCommercial
    // UnregisteredTabPersonal
    /*
     * right
     */
    right.getLayout.setLayoutConstraints(opt(8))
    right.getLayout.setColumnConstraints(opt(9))
    right.getLayout.setRowConstraints(opt(10))
    right.setConstraint(opt(11))
    commercial.getLayout.setLayoutConstraints(opt(12))
    commercial.getLayout.setColumnConstraints(opt(13))
    commercial.getLayout.setRowConstraints(opt(14))
    commercial.setConstraint(opt(15))
    // RegisterCommercialPremium
    // RegisterCommercialPlus
    // RegisterCommercialLight
    personal.getLayout.setLayoutConstraints(opt(16))
    personal.getLayout.setColumnConstraints(opt(17))
    personal.getLayout.setRowConstraints(opt(18))
    personal.setConstraint(opt(19))
    // RegisterPersonalPremium
    // RegisterPersonalPlus
    // RegisterPersonalSolo
    refundTargetLabel.setConstraint(opt(20))
  }
  def update() {
    if (isPersonal) {
      commercial.setVisible(false)
      personal.setVisible(true)
    } else {
      commercial.setVisible(false)
      personal.setVisible(true)      
    }
  }
}
/*
,
      "verticalLeft",
      imgPath + "tabBegin.png",
      imgPath + "tabBeginSelected.png",
      imgPath + "tabMiddleBegin.png",
      imgPath + "tabMiddleBeginSelected.png",
      imgPath + "tabMiddleEnd.png",
      imgPath + "tabMiddleEndSelected.png",
      imgPath + "tabEnd.png",
      imgPath + "tabEndSelected.png",
      imgPath + "tabEndSelected.png"*/
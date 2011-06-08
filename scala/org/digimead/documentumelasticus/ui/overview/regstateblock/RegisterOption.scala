package org.digimead.documentumelasticus.ui.overview.regstateblock

import com.sun.star.lang.XMultiComponentFactory
import org.digimead.documentumelasticus.ui.OControlContainer
import com.sun.star.uno.XComponentContext
import org.digimead.documentumelasticus.ui.MigLayout
import org.digimead.documentumelasticus.ui.OControl
import com.sun.star.awt.FontDescriptor
import org.digimead.documentumelasticus.helper.Library
import com.sun.star.uno.AnyConverter
import com.sun.star.awt.MouseEvent
import com.sun.star.awt.XMouseListener
import com.sun.star.awt.XWindow
import com.sun.star.lang.EventObject
import org.digimead.documentumelasticus.helper.O
import scala.collection.mutable.HashMap

class RegisterOption(tmcf: XMultiComponentFactory,
    tid: String,
    val label: String,
    val prefix: String,
    val benefits: Array[String],
    tparent: OControlContainer,
    tctx: XComponentContext) extends OControlContainer(tmcf,
        tid,
        tparent,
        tctx,
        new MigLayout()) {
  implicit def actionPerformedWrapper(closure: (MouseEvent) => Unit) =
    new XMouseListener() {
      def mousePressed(e: MouseEvent) = closure(e)
      def mouseReleased(e: MouseEvent) {}
      def mouseEntered(e: MouseEvent) {}
      def mouseExited(e: MouseEvent) {}
      def disposing(e: EventObject) {}
    }
  val linksHash: HashMap[String, String] = HashMap()
  O.I[XWindow](control.getPeer).addMouseListener((e: MouseEvent) => {clickLink("Container")})
  val uiIcon = new OControl(mcf, "Icon", "UnoControlImageControl", this, ctx, arg => {
    arg.model.setPropertyValue("ScaleImage", false)
    arg.model.setPropertyValue("Border", 0.toShort)
  })
  O.I[XWindow](uiIcon.control.getPeer).addMouseListener((e: MouseEvent) => {clickLink("Icon")})
  val uiBlock = new OControlContainer(mcf, "Block", this, ctx, new MigLayout())
  O.I[XWindow](uiBlock.control.getPeer).addMouseListener((e: MouseEvent) => {clickLink("Block")})
  val uiTitle = new OControl(mcf, "Title", "UnoControlFixedText", uiBlock, ctx)
  O.I[XWindow](uiTitle.control.getPeer).addMouseListener((e: MouseEvent) => {clickLink("Title")})
  uiTitle.model.setPropertyValue("Label", label)
  val uiLine = new OControl(mcf, "Line", "UnoControlFixedLine", uiBlock, ctx)
  O.I[XWindow](uiLine.control.getPeer).addMouseListener((e: MouseEvent) => {clickLink("Line")})
  // add benefits
  for (i ← 0 to benefits.length - 1) {
    val benefitIcon = new OControl(mcf, "BenefitIcon" + i, "UnoControlImageControl", uiBlock, ctx, arg ⇒ {
      arg.model.setPropertyValue("ScaleImage", false)
      arg.model.setPropertyValue("Border", 0.toShort)
    }).setConstraint("split 2")
    O.I[XWindow](benefitIcon.control.getPeer).addMouseListener((e: MouseEvent) => {clickLink("BenefitIcon" + i)})
    val benefitLabel = new OControl(mcf, "BenefitLabel" + i, "UnoControlFixedText", uiBlock, ctx).setConstraint("wrap")
    O.I[XWindow](benefitLabel.control.getPeer).addMouseListener((e: MouseEvent) => {clickLink("BenefitLabel" + i)})
    benefitLabel.model.setPropertyValue("Label", benefits(i))
  }
  val uiSignUp = new OControl(mcf, "SignUp", "UnoControlImageControl", uiBlock, ctx, arg => {
    arg.model.setPropertyValue("ScaleImage", false)
    arg.model.setPropertyValue("Border", 0.toShort)
  })
  O.I[XWindow](uiSignUp.control.getPeer).addMouseListener((e: MouseEvent) => {clickLink("SignUp")})
  load()
  def reload() {
    logger.trace("reload")
    load()
    layout(true)
  }
  def load() {
    val opt: Array[Any] = try {
      Library.invokeMacro(mcf,
        "vnd.sun.star.script:DE_UI_OFFICE.Overview." + tid + "?language=Basic&location=application",
        ctx, Array(prefix)).asInstanceOf[Array[Any]]
    } catch {
      case e ⇒
        logger.error("Subroutine vnd.sun.star.script:DE_UI_OFFICE.Overview." + tid + "?language=Basic&location=application failed: ", e)
        null
    }
    // container
    getLayout.setLayoutConstraints(opt(0).asInstanceOf[String])
    getLayout.setColumnConstraints(opt(1).asInstanceOf[String])
    getLayout.setRowConstraints(opt(2).asInstanceOf[String])
    setConstraint(opt(3).asInstanceOf[String])
    linksHash("Container") = opt(4).asInstanceOf[String]
    // block
    uiBlock.getLayout.setLayoutConstraints(opt(5).asInstanceOf[String])
    uiBlock.getLayout.setColumnConstraints(opt(6).asInstanceOf[String])
    uiBlock.getLayout.setRowConstraints(opt(7).asInstanceOf[String])
    uiBlock.setConstraint(opt(8).asInstanceOf[String])
    linksHash("Block") = opt(9).asInstanceOf[String]
    // icon
    uiIcon.model.setPropertyValue("ImageURL", opt(10).asInstanceOf[String])
    uiIcon.setConstraint(opt(11).asInstanceOf[String])
    linksHash("Icon") = opt(12).asInstanceOf[String]
    // title
    val titleFont = new FontDescriptor()
    titleFont.Weight = AnyConverter.getType(opt(13)).getTypeName match {
      case "short" => opt(13).asInstanceOf[Short].toFloat
      case "int" => opt(13).asInstanceOf[Int].toFloat
      case "long" => opt(13).asInstanceOf[Int].toFloat
      case "float" => opt(13).asInstanceOf[Float]
    }
    try {throw new com.sun.star.uno.RuntimeException} catch {case _ => ()}
    titleFont.Height = AnyConverter.getType(opt(14)).getTypeName match {
      case "byte" => opt(14).asInstanceOf[Byte].toShort
      case "short" => opt(14).asInstanceOf[Short]
    }
    uiTitle.model.setPropertyValue("FontDescriptor", titleFont)
    uiTitle.setConstraint(opt(15).asInstanceOf[String])
    linksHash("Title") = opt(16).asInstanceOf[String]
    // line
    uiLine.setConstraint(opt(17).asInstanceOf[String])
    linksHash("Line") = opt(18).asInstanceOf[String]
    // signup
    uiSignUp.model.setPropertyValue("ImageURL", opt(19).asInstanceOf[String])
    uiSignUp.setConstraint(opt(20).asInstanceOf[String])
    linksHash("SignUp") = opt(21).asInstanceOf[String]
    // benefits
    for (i ← 0 to benefits.length - 1) {
      val idx = 21 + i * 5
      val benefitIcon = uiBlock.getControl("BenefitIcon" + i)
      benefitIcon.model.setPropertyValue("ImageURL", opt(idx + 1).asInstanceOf[String])
      benefitIcon.setConstraint(opt(idx + 2).asInstanceOf[String])
      linksHash("BenefitIcon" + i) = opt(idx + 3).asInstanceOf[String]
      val benefitLabel = uiBlock.getControl("BenefitLabel" + i)
      benefitLabel.setConstraint(opt(idx + 4).asInstanceOf[String])
      linksHash("BenefitLabel" + i) = opt(idx + 5).asInstanceOf[String]
    }
  }
  def clickLink(name: String) {
    logger.warn("click " + name + ", url: " + linksHash(name))
    if (!java.awt.Desktop.isDesktopSupported()) {
      logger.error("Desktop is not supported (fatal)")
      return
    }
    if (linksHash(name).length == 0 ) {
      logger.error("Usage: OpenURI [URI [URI ... ]]")
      return
    }
    val desktop = java.awt.Desktop.getDesktop()
    if (!desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
      logger.error( "Desktop doesn't support the browse action (fatal)" )
      return
    }
    try {
      val uri = new java.net.URI(linksHash(name))
      desktop.browse(uri)
    } catch {
      case e => logger.error(e.getMessage(), e)
    }
  }
}

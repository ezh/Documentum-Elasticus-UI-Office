package org.digimead.documentumelasticus.ui.overview.regstateblock

import com.sun.star.lang.XMultiComponentFactory
import org.digimead.documentumelasticus.ui.OControlContainer
import com.sun.star.uno.XComponentContext
import org.digimead.documentumelasticus.ui.MigLayout
import org.digimead.documentumelasticus.ui.OControl
import org.digimead.documentumelasticus.helper.O
import com.sun.star.awt.XButton
import com.sun.star.awt.XFixedText
import com.sun.star.awt.FontDescriptor

class RegisterOption(tmcf: XMultiComponentFactory,
    tid: String,
    val label: String,
    val logo: String,
    val signup: String,
    val benefits: Array[Tuple2[String, String]],
    tparent: OControlContainer,
    tctx: XComponentContext) extends OControlContainer(tmcf,
        tid,
        tparent,
        tctx,
        new MigLayout("inset 0, gap 0, nocache, align 50% 50%", "[c, fill]", "[c, fill]")) {
  val logoIcon = new OControl(mcf, "LogoImage", "UnoControlImageControl", this, ctx, arg => {
    arg.model.setPropertyValue("ScaleImage", false)
    arg.model.setPropertyValue("ImageURL", logo)
    arg.model.setPropertyValue("Border", 0.toShort)
  })
  val block = new OControlContainer(mcf, "PersonalBlock", this, ctx, new MigLayout("", "", "[]0[][]")).setConstraint("pushy, growy, wrap")
  val titleLabel = new OControl(mcf, "Title", "UnoControlFixedText", block, ctx).setConstraint("wrap")
  titleLabel.model.setPropertyValue("Label", label)
  val titleFont = new FontDescriptor()
  titleFont.Weight = com.sun.star.awt.FontWeight.BOLD
  titleFont.Height = 20
  titleLabel.model.setPropertyValue("FontDescriptor", titleFont)
  val line = new OControl(mcf, "Line", "UnoControlFixedLine", block, ctx).setConstraint("growx, wrap")
  // add benefits
  for (i ← 0 to benefits.length - 1) {
    val benefitIcon = new OControl(mcf, "BenefitLabel" + i, "UnoControlImageControl", block, ctx, arg ⇒ {
      arg.model.setPropertyValue("ScaleImage", false)
      arg.model.setPropertyValue("ImageURL", benefits(i)._1)
      arg.model.setPropertyValue("Border", 0.toShort)
    }).setConstraint("split 2")
    val benefitLabel = new OControl(mcf, "BenefitLabel" + i, "UnoControlFixedText", block, ctx).setConstraint("wrap")
    benefitLabel.model.setPropertyValue("Label", benefits(i)._2)
  }
  val signUpButton = new OControl(mcf, "SignUp", "UnoControlImageControl", block, ctx, arg => {
    arg.model.setPropertyValue("ScaleImage", false)
    arg.model.setPropertyValue("ImageURL", signup)
    arg.model.setPropertyValue("Border", 0.toShort)
  }).setConstraint("pushy, aligny bottom")
}
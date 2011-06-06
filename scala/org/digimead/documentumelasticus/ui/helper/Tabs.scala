package org.digimead.documentumelasticus.ui.helper

import com.sun.star.uno.XComponentContext
import org.digimead.documentumelasticus.helper.Library
import org.digimead.documentumelasticus.ui.{MigLayout, OControl, OControlContainer}
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap

class Tabs(val tid: String,
    val prefix: String,
    val tparent: OControlContainer,
    val tctx: XComponentContext) extends OControlContainer(tctx.getServiceManager,
        tid,
        tparent,
        tctx,
        new MigLayout()) {
  private val delimiterName = tid + "Delimiter"
  private val tabs: ArrayBuffer[String] = ArrayBuffer()
  private val tabResources: HashMap[String, (OControl, String, String)] = HashMap() // OControl, passive, active
  private val tabActive = ""
  private var selectedPos = 0
  logger.debug("create tab \"" + tid)
  val imgBefore = new OControl(mcf, "imgBefore", "UnoControlImageControl", this, ctx, arg => {
    arg.model.setPropertyValue("ScaleImage", false)
    arg.model.setPropertyValue("Border", 0.toShort)
  })
  val imgBegin = new OControl(mcf, "imgBegin", "UnoControlImageControl", this, ctx, arg => {
    arg.model.setPropertyValue("ScaleImage", false)
    arg.model.setPropertyValue("Border", 0.toShort)
  })
  val imgEnd = new OControl(mcf, "imgEnd", "UnoControlImageControl", this, ctx, arg => {
    arg.model.setPropertyValue("ScaleImage", false)
    arg.model.setPropertyValue("Border", 0.toShort)
  })
  val imgAfter = new OControl(mcf, "imgAfter", "UnoControlImageControl", this, ctx, arg => {
    arg.model.setPropertyValue("ScaleImage", false)
    arg.model.setPropertyValue("Border", 0.toShort)
  })
  load()
  def reload() {
    logger.trace("reload")
    tabs.foreach(name ⇒ {
      if (name.startsWith(delimiterName))
        loadDelimiter(name.substring(delimiterName.length).toInt)
      else
        loadTab(name)
    })
    load()
    layout(true)
  }
  def load() {
    val opt: Array[String] = try {
      Library.invokeMacro(mcf,
        "vnd.sun.star.script:DE_UI_OFFICE.Overview." + tid + "?language=Basic&location=application",
        ctx, Array(prefix)).asInstanceOf[Array[String]]
    } catch {
      case e ⇒
        logger.error("Subroutine vnd.sun.star.script:DE_UI_OFFICE.Overview." + tid + "?language=Basic&location=application failed: ", e)
        null
    }
    getLayout.setLayoutConstraints(opt(0))
    getLayout.setColumnConstraints(opt(1))
    getLayout.setRowConstraints(opt(2))
    setConstraint(opt(3))
    imgBefore.model.setPropertyValue("ImageURL", opt(4))
    imgBefore.setConstraint(opt(5))
    imgBegin.model.setPropertyValue("ImageURL", opt(6))
    imgBegin.setConstraint(opt(7))
    imgEnd.model.setPropertyValue("ImageURL", opt(8))
    imgEnd.setConstraint(opt(9))
    imgAfter.model.setPropertyValue("ImageURL", opt(10))
    imgAfter.setConstraint(opt(11))
  }
  def loadTab(name: String) {
    val opt: Array[String] = try {
      Library.invokeMacro(mcf,
        "vnd.sun.star.script:DE_UI_OFFICE.Overview." + name + "?language=Basic&location=application",
        ctx, Array(prefix)).asInstanceOf[Array[String]]
    } catch {
      case e ⇒
        logger.error("Subroutine vnd.sun.star.script:DE_UI_OFFICE.Overview." + tid + "?language=Basic&location=application failed: ", e)
        null
    }
    tabResources(name) = (tabResources(name)._1, opt(0), opt(1))
    if (tabActive == name)
      tabResources(name)._1.model.setPropertyValue("ImageURL", tabResources(name)._3)
    else
      tabResources(name)._1.model.setPropertyValue("ImageURL", tabResources(name)._2)
    tabResources(name)._1.setConstraint(opt(2))
  }
  def loadDelimiter(position: Int) {
    val opt: Array[String] = try {
      Library.invokeMacro(mcf,
        "vnd.sun.star.script:DE_UI_OFFICE.Overview." + delimiterName + "?language=Basic&location=application",
        ctx, Array(prefix)).asInstanceOf[Array[String]]
    } catch {
      case e ⇒
        logger.error("Subroutine vnd.sun.star.script:DE_UI_OFFICE.Overview." + tid + "?language=Basic&location=application failed: ", e)
        null
    }
    tabResources(delimiterName + position.toString) = (tabResources(delimiterName + position.toString)._1, opt(0), opt(1))
    // TODO active
    tabResources(delimiterName + position.toString)._1.model.setPropertyValue("ImageURL", tabResources(delimiterName + position.toString)._2)
    tabResources(delimiterName + position.toString)._1.setConstraint(opt(2))
  }
  def add(name: String, callback: Any) {
    val (l1, l2) = controls splitAt (controls.length - 2)
    controls = l1
    if (tabs.length == 0) {
      // first tab
      tabs += name
      tabResources(name) = (new OControl(mcf, name, "UnoControlImageControl", this, ctx, arg ⇒ {
        arg.model.setPropertyValue("ScaleImage", false)
        arg.model.setPropertyValue("Border", 0.toShort)
      }), null, null)
      loadTab(name)
    } else {
      // add delimiter
      val delimiterPos = tabs.length - 1
      tabs += (delimiterName + delimiterPos.toString)
      tabResources(delimiterName + delimiterPos.toString) = (new OControl(mcf, name, "UnoControlImageControl", this, ctx, arg ⇒ {
        arg.model.setPropertyValue("ScaleImage", false)
        arg.model.setPropertyValue("Border", 0.toShort)
      }), null, null)
      loadDelimiter(delimiterPos)
      // other
      tabs += name
      tabResources(name) = (new OControl(mcf, name, "UnoControlImageControl", this, ctx, arg ⇒ {
        arg.model.setPropertyValue("ScaleImage", false)
        arg.model.setPropertyValue("Border", 0.toShort)
      }), null, null)
      loadTab(name)
    }
    controls = controls ++ l2
    /*if (tabs.length != 0) {
      if (tabs.length - 1 != selectedPos) {
        val imgEnd = new OControl(mcf, "imgEnd", "UnoControlImageControl", this, ctx, arg ⇒ {
          arg.model.setPropertyValue("ScaleImage", false)
          arg.model.setPropertyValue("Border", 0.toShort)
        })
      } else {
        val imgEnd = new OControl(mcf, "imgEnd", "UnoControlImageControl", this, ctx, arg ⇒ {
          arg.model.setPropertyValue("ScaleImage", false)
          arg.model.setPropertyValue("Border", 0.toShort)
        })
      }
    }
    val tabControl = if (tabs.length - 1 != selectedPos) {
      new OControl(mcf, "imgEnd", "UnoControlImageControl", this, ctx, arg ⇒ {
        arg.model.setPropertyValue("ScaleImage", false)
        arg.model.setPropertyValue("Border", 0.toShort)
      })
    } else {
      new OControl(mcf, "imgEnd", "UnoControlImageControl", this, ctx, arg ⇒ {
        arg.model.setPropertyValue("ScaleImage", false)
        arg.model.setPropertyValue("Border", 0.toShort)
      })
    }
    tabs += Tuple3(tabControl, icon, iconSelected)*/
  }
}
//        arg.model.setPropertyValue("ImageURL", iconSelected)
/*
,
    val iconTabBegin: String,
    val iconTabBeginSelected: String,
    val iconTabMiddleStart: String,
    val iconTabMiddleStartSelected: String,
    val iconTabMiddleEnd: String,
    val iconTabMiddleEndSelected: String,
    val iconTabEnd: String,
    val iconTabEndSelected: String,
    val iconTabFiller: String*/
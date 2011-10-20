/*
 *
 * This file is part of the Documentum Elasticus project.
 * Copyright (c) 2010-2011 Limited Liability Company «MEZHGALAKTICHESKIJ TORGOVYJ ALIANS»
 * Author: Alexey Aksenov
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Global License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED
 * BY Limited Liability Company «MEZHGALAKTICHESKIJ TORGOVYJ ALIANS»,
 * Limited Liability Company «MEZHGALAKTICHESKIJ TORGOVYJ ALIANS» DISCLAIMS
 * THE WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Global License for more details.
 * You should have received a copy of the GNU Affero General Global License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA, 02110-1301 USA, or download the license from the following URL:
 * http://www.gnu.org/licenses/agpl.html
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Global License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Global License,
 * you must retain the producer line in every report, form or document
 * that is created or manipulated using Documentum Elasticus.
 *
 * You can be released from the requirements of the license by purchasing
 * a commercial license. Buying such a license is mandatory as soon as you
 * develop commercial activities involving the Documentum Elasticus software without
 * disclosing the source code of your own applications.
 * These activities include: offering paid services to customers,
 * serving files in a web or/and network application,
 * shipping Documentum Elasticus with a closed source product.
 *
 * For more information, please contact Documentum Elasticus Team at this
 * address: ezh@ezh.msk.ru
 *
 */

package org.digimead.documentumelasticus.ui

import com.sun.star.awt.{XWindowPeer, XWindowListener, XWindow2, XWindow, XTopWindowListener, XTopWindow, XToolkit, XMouseListener, XControlModel, WindowEvent, WindowDescriptor, WindowClass, WindowAttribute, PosSize, MouseEvent}
import com.sun.star.beans.PropertyValue
import com.sun.star.frame.{XFrame, XFramesSupplier}
import com.sun.star.lang.{XMultiComponentFactory, EventObject}
import com.sun.star.uno.XComponentContext
import com.sun.star.util.{CloseVetoException, XCloseListener, XCloseBroadcaster}
import java.util.concurrent.atomic.AtomicBoolean
import net.miginfocom.layout.{PlatformDefaults, ConstraintParser, ComponentWrapper, BoundSize, LayoutCallback}
import org.digimead.documentumelasticus.component.{XBaseObject, XBaseClass}
import org.digimead.documentumelasticus.helper._
import org.digimead.documentumelasticus.ui.wizard.ConfigWizard
import org.digimead.documentumelasticus._
import org.slf4j.LoggerFactory
import org.slf4j.{LoggerFactory, Logger}
import scala.collection.mutable.ArrayBuffer

class Office(val ctx: XComponentContext) extends XUI {
  protected val log = LoggerFactory.getLogger(this.getClass)
  var componentSingleton = Office.componentSingleton
  val componentTitle = Office.componentTitle
  val componentDescription = Office.componentDescription
  val componentURL = Office.componentURL
  val componentName = Office.componentName
  val componentServices = Office.componentServices
  val componentDisabled = Office.componentDisabled
  initialize(Array()) // initialized by default
  log.info(componentName + " active")

  // -----------------------
  // - implement trait XUI -
  // -----------------------
  def tabsContainer = if (isInitialized())
    Office.contentFrame
  else
    throw new RuntimeException("call uninitialized instance of " + componentName)

  def show() {
    log.debug("show UI")
    if (isInitialized())
      Office.show()
    else
      throw new RuntimeException("call uninitialized instance of " + componentName)
  }

  def hide() {
    if (isInitialized())
      Office.hide()
    else
      throw new RuntimeException("call uninitialized instance of " + componentName)
  }

  def refresh(): Boolean = if (isInitialized())
    Office.refresh()
  else
    throw new RuntimeException("call uninitialized instance of " + componentName)

  def reload(): Boolean = if (isInitialized())
    Office.reload()
  else
    throw new RuntimeException("call uninitialized instance of " + componentName)
  
  def configWizard(name: String): Array[PropertyValue] = synchronized {
    if (!isInitialized())
      initialize(Array())
    Office.configWizard(name, ctx)
  }

  def panelAdd(tab: Tab) {
    if (isInitialized())
      Office.panelAdd(tab)
    else
      throw new RuntimeException("call uninitialized instance of " + componentName)
  }
  def tabAdd(clazz: Class[_], name: String): Tab =
    if (isInitialized())
      Office.tabAdd(clazz, name)
    else
      throw new RuntimeException("call uninitialized instance of " + componentName)

  def isInitialized() = Office.initialized.get

  // -----------------------------------
  // - implement trait XInitialization -
  // -----------------------------------
  def initialize(args: Array[AnyRef]) {
    synchronized {
      log.info("initialize " + componentName)
      if (isInitialized())
        throw new RuntimeException("Initialization of " + componentName + " already done")
      Office.initialize(ctx)
    }
  }

  // ------------------------------
  // - implement trait XComponent -
  // ------------------------------
  override def dispose() {
    synchronized {
      log.info("dispose " + componentName)
      if (!isInitialized()) {
        log.warn("dispose of " + componentName + " already done")
        return
      }
      Office.dispose()
      super.dispose()
    }
  }
}

object Office extends XBaseObject {

  class Panel(val tab: Tab, var active: Boolean = false) {}

  private val log = LoggerFactory.getLogger(this.getClass.getName)
  var componentSingleton: Option[XBaseClass] = None
  val componentTitle = "Documentum Elasticus UI"
  val componentDescription = "standard UI component"
  val componentURL = "http://www."
  val componentName = classOf[Office].getName
  val componentServices: Array[String] = Array(componentName)
  val componentDisabled = false
  private val initialized: AtomicBoolean = new AtomicBoolean(false)
  log.info(componentName + " active")

  /**
   * @param args the command line arguments
   */
  def main(args: Array[String]) {
    println("Hello, world!")
  }

  /*
  * singleton
  */
  private var ctx: XComponentContext = null
  private var mcf: XMultiComponentFactory = null
  private var extension: Extension = null
  // element positions
  private val margin = 5
  private val iMinimumWidth = 500
  private val iMinimumHeight = 500
  private val controlPos = "left"
  // elements
  private var baseFrame: XFrame = null
  private var controlFrame: XFrame = null
  private var contentFrame: XFrame = null
  private var panelsWrapper: OControlContainer = null
  private var panelsScroll: OControl = null
  private var panelsContainer: OControlContainer = null
  private val tabComponents: ArrayBuffer[TabEntity] = ArrayBuffer()
  // initialized tabs
  private var activePanel: OControlContainer = null
  private var fTopWindowActive: Boolean = false
  private var toolkit: XToolkit = null

  private def initialize(arg: XComponentContext) {
    synchronized {
      log.debug("initialize")
      ctx = arg
      mcf = ctx.getServiceManager
      toolkit = O.SI[XToolkit](mcf, "com.sun.star.awt.Toolkit", ctx)
      extension = ctx.getValueByName("/singletons/org.digimead.documentumelasticus.Extension").asInstanceOf[Extension]
      val (base, control, content) = createMainFrame() // top frame, frame with buttons, frame with content
      baseFrame = base
      controlFrame = control
      contentFrame = content
      // wrapper
      panelsWrapper = new OControlContainer(mcf, "panel_wrapper", null, ctx,
        new MigLayout("insets 0, gap 0, fillx, hidemode 3"),
        argn => {
          // reattach frame window as component window
          argn.control.setModel(O.I[XControlModel](argn.model))
          argn.control.createPeer(toolkit, O.I[XWindowPeer](controlFrame.getContainerWindow))
          controlFrame.setComponent(O.I[XWindow](argn.control.getPeer), null)
        })
      // scroll
      panelsContainer = new OControlContainer(mcf, "panel_container", panelsWrapper, ctx, new MigLayout("flowy, nocache"))
      panelsContainer.setConstraint("w 100%")
      panelsScroll = new OControl(mcf, "panel_scroll", "UnoControlScrollBar", panelsWrapper, ctx)
      panelsScroll.model.setPropertyValue("Orientation", 1)
      panelsScroll.setConstraint("width 5mm!, gap 0!")
      O.I[XWindow](panelsScroll.control.getPeer).setVisible(false)
      // container
      panelsContainer.model.setPropertyValue("BackgroundColor", 0x00E0E0E0)
      panelsContainer.model.setPropertyValue("Border", 1.toShort)
      panelsContainer.model.setPropertyValue("BorderColor", 0x00AFAF00)
      panelsContainer.getLayout().addLayoutCallback(new LayoutCallback() {
        override def getSize(comp: ComponentWrapper): Array[BoundSize] = {
          val parent = comp.getParent.asInstanceOf[OControlContainer]
          if (parent.getWidth() == 0)
            return null
          val insets = parent.getLayout().getLayoutConstraints().getInsets
          // width - left - right
          val w = if (insets != null)
            parent.getWidth() - insets(1).getPixels(0, parent, null) - insets(3).getPixels(0, parent, null)
          else
            parent.getWidth() - PlatformDefaults.getPanelInsets(1).getPixels(0, parent, null) - PlatformDefaults.getPanelInsets(3).getPixels(0, parent, null)
          log.trace("adjust '" + comp.asInstanceOf[OControl].controlID + "' width/size from " + comp.getWidth + " to " + w + ", parent width " + parent.getWidth())
          Array[BoundSize](ConstraintParser.parseBoundSize(w + "!", false, true),
            ConstraintParser.parseBoundSize(w + "!", false, false))
        }
      })
      panelsContainer.setCallbackResized(arg => {
        arg.layout()
        val rect = arg.getBounds()
        // adjust position and size
        val pref = arg.getPreferredHeight(0)
        val max = arg.getParent().getHeight
        arg.setBounds(rect.X, rect.Y, rect.Width, if (pref > max) pref else max)
      })
      initialized.set(true)
    }
  }

  private def dispose() {
    log.debug("dispose")
    Office.initialized.set(false)
  }

  // -----------------------
  // - implement trait XUI -
  // -----------------------
  private def show() {
    baseFrame.getContainerWindow.setVisible(true)
  }

  private def hide() {
    baseFrame.getContainerWindow.setVisible(false)
  }
  private def reload(): Boolean = {
    panelsContainer.getComponents().foreach(_.asInstanceOf[OControl].stash.asInstanceOf[Panel].tab.reload())
    val size = baseFrame.getContainerWindow.getPosSize()
    baseFrame.getContainerWindow.setPosSize(size.X, size.Y, size.Width, size.Height, PosSize.POSSIZE)
    true
  }
  private def refresh(): Boolean = {
    // TODO
    panelAdd(tabAdd(classOf[Overview], "Overview"))
    //panelAdd(tabAdd(classOf[Storages], "Storages"))
    var position = 2
    log.debug("initialize tabs")
    extension.components().foreach(name => {
      if (name != "org.digimead.documentumelasticus.Core" &&
        extension.component(name).componentSingleton != null &&
        extension.component(name).componentSingleton != None &&
        extension.component(name).componentSingleton.get.isInstanceOf[TabEntity]
      ) {
        val component = extension.component(name).componentSingleton.get.asInstanceOf[TabEntity]
        log.debug("add tab for component " + name)
        if (tabComponents.find(_ == component) == None) {
          tabComponents.append(component)
          component.tabAdd(contentFrame, position)
          position += 1
        }
      } else {
        log.debug("skip component " + name)
      }
    })
    panelsContainer.getComponents().foreach(_.asInstanceOf[OControl].stash.asInstanceOf[Panel].tab.refresh())
    //panelAdd(tabAdd(classOf[TabSettings], "settings"))
    //panelAdd(tabAdd(classOf[TabAbout], "about"))
    true
  }

  private def configWizard(name: String, arg: XComponentContext): Array[PropertyValue] = {
    val wizard = new ConfigWizard(arg, O.SI[XFrame]("com.sun.star.frame.Desktop", arg))
    wizard.execute(name)
  }

  private def panelAdd(tab: Tab, position: Int = -1): Panel = {
    val panelPrefix = "panel_"
    val controls = Office.panelsContainer.getControls()
    val panelPos = controls.length
    log.debug("add new " + panelPrefix + panelPos)
    // create panel base
    val panelContainer = new OControlContainer(mcf, panelPrefix + panelPos, Office.panelsContainer, ctx, new MigLayout("insets 1, gap 0, align center", "", "[grow][]"))
    val panelClass = new Panel(tab)
    panelContainer.stash = panelClass
    // panel image
    val image = new OControl(mcf, "img", "UnoControlImageControl", panelContainer, ctx)
    image.setConstraint("cell 0 0, align center")
    image.model.setPropertyValue("ScaleImage", false)
    image.model.setPropertyValue("ImageURL", "file:///tmp/a.png")
    // text label
    val label = new OControl(mcf, "label", "UnoControlFixedText", panelContainer, ctx)
    label.setConstraint("cell 0 1, w 100%")
    label.model.setPropertyValue("Align", 1.toShort)
    label.model.setPropertyValue("Label", tab.name)
    label.model.setPropertyValue("MultiLine", true)
    // listener
    class MouseListener(val control: OControlContainer) extends XMouseListener {
      def mousePressed(e: MouseEvent) {
        panelMousePressed(e, control)
      }

      def mouseReleased(e: MouseEvent) {}

      def mouseEntered(e: MouseEvent) {
        panelMouseEntered(e, control)
      }

      def mouseExited(e: MouseEvent) {
        panelMouseExited(e, control)
      }

      def disposing(e: EventObject) {}
    }
    val mouseListener = new MouseListener(panelContainer)
    O.I[XWindow](panelContainer.control.getPeer).addMouseListener(mouseListener)
    O.I[XWindow](image.control.getPeer).addMouseListener(mouseListener)
    O.I[XWindow](label.control.getPeer).addMouseListener(mouseListener)
    panelClass
  }

  private def tabAdd(clazz: Class[_], name: String): Tab = {
    val ctor = clazz.getConstructor(classOf[String], classOf[XFrame], classOf[XComponentContext])
    ctor.newInstance(name, contentFrame, ctx).asInstanceOf[Tab]
  }

  private def tabGet(name: String): Tab = {
    panelsContainer.getComponents().find(
      _.asInstanceOf[OControl].stash.asInstanceOf[Panel].tab.name == name).asInstanceOf[OControl].stash.asInstanceOf[Panel].tab
  }

  private def createMainFrame(): (XFrame, XFrame, XFrame) = {
    /*
     * top
     */
    val oTopWindowDescriptor = new WindowDescriptor()
    oTopWindowDescriptor.Type = WindowClass.TOP
    oTopWindowDescriptor.ParentIndex = (-1).toShort // desktop
    oTopWindowDescriptor.Parent = null
    oTopWindowDescriptor.Bounds = new com.sun.star.awt.Rectangle(0, 0, 0, 0)
    oTopWindowDescriptor.WindowAttributes = WindowAttribute.BORDER +
      WindowAttribute.SIZEABLE +
      WindowAttribute.MOVEABLE +
      WindowAttribute.CLOSEABLE +
      WindowAttribute.OPTIMUMSIZE
    val oTopWindow = toolkit.createWindow(oTopWindowDescriptor)
    val oTopWindowListener = new TopWindowListener()
    O.I[XTopWindow](oTopWindow).addTopWindowListener(oTopWindowListener)
    val xTopWindow: XWindow = O.I[XWindow](oTopWindow)
    xTopWindow.addWindowListener(oTopWindowListener)
    val xTopFrame = O.SI[XFrame](mcf, "com.sun.star.frame.Frame", ctx)
    O.I[XCloseBroadcaster](xTopFrame).addCloseListener(new TopFrameCloseListener())
    val xTopFrameSupplier = O.I[XFramesSupplier](xTopFrame)
    xTopFrame.initialize(xTopWindow)
    xTopFrame.setCreator(O.SI[XFramesSupplier](mcf, "com.sun.star.comp.framework.Desktop", ctx))
    /*
     * control
     */
    val oControlWindowDescriptor = new WindowDescriptor()
    oControlWindowDescriptor.Type = WindowClass.CONTAINER
    oControlWindowDescriptor.Parent = oTopWindow
    oControlWindowDescriptor.Bounds = new com.sun.star.awt.Rectangle(0, 0, 0, 0)
    oControlWindowDescriptor.WindowAttributes = WindowAttribute.SHOW
    val oControlWindow = toolkit.createWindow(oControlWindowDescriptor)
    O.I[XWindow](oControlWindow).addWindowListener(new ControlWindowListener())
    val xControlFrame = O.SI[XFrame](mcf, "com.sun.star.frame.Frame", ctx)
    xControlFrame.initialize(O.I[XWindow](oControlWindow))
    xControlFrame.setCreator(xTopFrameSupplier)
    xTopFrameSupplier.getFrames.append(xControlFrame)
    /*
     * content
     */
    val oContentWindowDescriptor = new WindowDescriptor()
    oContentWindowDescriptor.Type = WindowClass.CONTAINER
    oContentWindowDescriptor.Parent = oTopWindow
    oContentWindowDescriptor.Bounds = new com.sun.star.awt.Rectangle(0, 0, 0, 0)
    oContentWindowDescriptor.WindowAttributes = WindowAttribute.SHOW + WindowAttribute.BORDER
    val oContentWindow = toolkit.createWindow(oContentWindowDescriptor)
    O.I[XWindow](oContentWindow).addWindowListener(new ContentWindowListener())
    val xContentFrame = O.SI[XFrame](mcf, "com.sun.star.frame.Frame", ctx)
    xContentFrame.initialize(O.I[XWindow](oContentWindow))
    xContentFrame.setCreator(xTopFrameSupplier)
    xTopFrameSupplier.getFrames.append(xContentFrame)
    (xTopFrame, xControlFrame, xContentFrame)
  }

  private def panelMouseEntered(e: MouseEvent, control: OControl) {
    if (control.stash.asInstanceOf[Panel].active != true)
      control.stash.asInstanceOf[Panel].active = true
  }

  private def panelMouseExited(e: MouseEvent, control: OControl) {
    if (e.X <= 1 || e.X >= control.getWidth() || e.Y <= 1 || e.Y >= control.getHeight())
      control.stash.asInstanceOf[Panel].active = false
  }

  private def panelMousePressed(e: MouseEvent, control: OControlContainer) {
    if (Office.activePanel == control)
      return
    if (Office.activePanel != null) {
      val oldTab = Office.activePanel.stash.asInstanceOf[Panel].tab
      log.debug("hide tab '" + oldTab.name + "'")
      oldTab.frame.getContainerWindow.setVisible(false)
    }
    val newTab = control.stash.asInstanceOf[Panel].tab
    log.debug("activate tab '" + newTab.name + "'")
    val contentSize = O.I[XWindow2](Office.contentFrame.getContainerWindow).getOutputSize
    val tabWindow = newTab.frame.getContainerWindow
    val tabPos = tabWindow.getPosSize
    if (tabPos.X != 0 || tabPos.Y != 0 || tabPos.Width != contentSize.Width || tabPos.Height != contentSize.Height)
      tabWindow.setPosSize(0, 0, contentSize.Width, contentSize.Height, PosSize.POSSIZE)
    tabWindow.setVisible(true)
    Office.activePanel = control
  }

  // -------------
  // - listeners -
  // -------------
  class TopFrameCloseListener() extends XCloseListener {
    def queryClosing(arg0: EventObject, arg1: Boolean) {
      log.warn("queryClosing")
      if (fTopWindowActive && O.I[XWindow2](baseFrame.getContainerWindow).isVisible) {
        hide()
        throw new CloseVetoException // user try to close with X, TODO fix it, need deeper check!!!
      }
    }

    def notifyClosing(arg0: EventObject) {
      log.warn("notifyClosing")
    }

    // ----------------------------------
    // - implement trait XEventListener -
    // ----------------------------------
    def disposing(e: EventObject) {
      log.trace("disposing")
    }
  }

  //UNO.XCloseBroadcaster(calcDoc).removeCloseListener(myCalcListener);

  class TopWindowListener() extends XWindowListener with XTopWindowListener {
    val log = LoggerFactory.getLogger(this.getClass)
    var initialResize = true
    // --------------------------------------
    // - implement trait XTopWindowListener -
    // --------------------------------------
    def windowOpened(e: EventObject) {
      log.trace("window opened")
    }

    def windowClosing(e: EventObject) {
      log.trace("window closing")
    }

    def windowClosed(e: EventObject) {
      log.trace("window closed")
    }

    def windowMinimized(e: EventObject) {
      log.trace("window minimized")
    }

    def windowNormalized(e: EventObject) {
      log.trace("window normalized")
    }

    def windowActivated(e: EventObject) {
      log.trace("window activated")
      fTopWindowActive = true
      if (initialResize) {
        initialResize = false
        val te = new WindowEvent
        val position = O.I[XWindow](e.Source).getPosSize
        te.Source = e.Source
        te.X = position.X
        te.Y = position.Y
        te.Width = position.Width
        te.Height = position.Height
        windowResized(te)
      }
    }

    def windowDeactivated(e: EventObject) {
      log.trace("window deactivated")
      fTopWindowActive = false
    }

    // --------------------------------------
    // - implement trait XWindowListener -
    // --------------------------------------
    def windowHidden(e: EventObject) {
      log.trace("window hidden")
    }

    def windowShown(e: EventObject) {
      log.trace("window shown")
    }

    def windowMoved(e: WindowEvent) {}

    def windowResized(e: WindowEvent) {
      log.trace("window resized")
      val iWidth = if (e.Width < Office.iMinimumWidth) Office.iMinimumWidth else e.Width
      val iHeight = if (e.Height < Office.iMinimumHeight) Office.iMinimumHeight else e.Height
      if (iWidth != e.Width || iHeight != e.Height) {
        O.I[XWindow](e.Source).setPosSize(e.X, e.Y, iWidth, iHeight, PosSize.POSSIZE)
      }
      // children
      val panelWidth: Int = scala.math.round(panelsWrapper.xPixelFactor * 500) // 5cm
      val panelHeight: Int = scala.math.round(panelsWrapper.yPixelFactor * 500) // 5cm
      // control
      val controlX = controlPos match {
        case "left" => margin
        case "right" => 0
        case "top" => margin
        case "bottom" => margin
        case pos => throw new RuntimeException("invalid control frame position '" + pos + "'")
      }
      val controlY = controlPos match {
        case "left" => margin
        case "right" => margin
        case "top" => margin
        case "bottom" => 0
        case pos => throw new RuntimeException("invalid control frame position '" + pos + "'")
      }
      val controlWidth = controlPos match {
        case "left" => panelWidth
        case "right" => panelWidth
        case "top" => iWidth - 2 * margin
        case "bottom" => iWidth - 2 * margin
        case pos => throw new RuntimeException("invalid control frame position '" + pos + "'")
      }
      val controlHeight = controlPos match {
        case "left" => iHeight - 2 * margin
        case "right" => iHeight - 2 * margin
        case "top" => panelHeight
        case "bottom" => panelHeight
        case pos => throw new RuntimeException("invalid control frame position '" + pos + "'")
      }
      controlFrame.getContainerWindow.setPosSize(controlX, controlY, controlWidth, controlHeight, PosSize.POSSIZE)
      // content
      val contentX = controlPos match {
        case "left" => controlX + controlWidth + margin
        case "right" => 0
        case "top" => margin
        case "bottom" => margin
        case pos => throw new RuntimeException("invalid content frame position '" + pos + "'")
      }
      val contentY = controlPos match {
        case "left" => margin
        case "right" => margin
        case "top" => margin
        case "bottom" => 0
        case pos => throw new RuntimeException("invalid content frame position '" + pos + "'")
      }
      val contentWidth = controlPos match {
        case "left" => iWidth - contentX - margin
        case "right" => panelWidth
        case "top" => iWidth - 2 * margin
        case "bottom" => iWidth - 2 * margin
        case pos => throw new RuntimeException("invalid content frame position '" + pos + "'")
      }
      val contentHeight = controlPos match {
        case "left" => iHeight - 2 * margin
        case "right" => iHeight - 2 * margin
        case "top" => panelHeight
        case "bottom" => panelHeight
        case pos => throw new RuntimeException("invalid content frame position '" + pos + "'")
      }
      Office.contentFrame.getContainerWindow.setPosSize(contentX, contentY, contentWidth, contentHeight, PosSize.POSSIZE)
    }

    // ----------------------------------
    // - implement trait XEventListener -
    // ----------------------------------
    def disposing(e: EventObject) {
      log.trace("disposing")
    }
  }

  class ContentWindowListener() extends XWindowListener {
    val log = LoggerFactory.getLogger(this.getClass)
    // --------------------------------------
    // - implement trait XWindowListener -
    // --------------------------------------
    def windowHidden(e: EventObject) {
      log.trace("window hidden")
    }

    def windowShown(e: EventObject) {
      log.trace("window shown")
    }

    def windowMoved(e: WindowEvent) {
      log.trace("window moved")
    }

    def windowResized(e: WindowEvent) {
      log.trace("window resized")
      if (Office.activePanel != null) {
        val contentSize = O.I[XWindow2](Office.contentFrame.getContainerWindow).getOutputSize
        val tab = Office.activePanel.stash.asInstanceOf[Panel].tab
        tab.frame.getContainerWindow.setPosSize(0, 0, contentSize.Width, contentSize.Height, PosSize.POSSIZE)
        tab.container.layout()
      }
    }

    // ----------------------------------
    // - implement trait XEventListener -
    // ----------------------------------
    def disposing(e: EventObject) {
      log.trace("disposing")
    }
  }

  class ControlWindowListener() extends XWindowListener {
    val log = LoggerFactory.getLogger(this.getClass)
    // -----------------------------------
    // - implement trait XWindowListener -
    // -----------------------------------
    def windowHidden(e: EventObject) {
      log.trace("window hidden")
    }

    def windowShown(e: EventObject) {
      log.trace("window shown")
    }

    def windowMoved(e: WindowEvent) {
      log.trace("window moved")
    }

    def windowResized(e: WindowEvent) {
      log.trace("window resized")
      Office.panelsWrapper.layout()
    }

    // ----------------------------------
    // - implement trait XEventListener -
    // ----------------------------------
    def disposing(e: EventObject) {
      log.trace("disposing")
    }
  }

}

/*
 *
  val instance: Office = synchronized {
    if (org.digimead.documentumelasticus.ui.Office.instance == null)
      org.digimead.documentumelasticus.ui.Office.instance = this
    org.digimead.documentumelasticus.ui.Office.instance
  }
  if (this == instance)
    init()

  // ---------------------------------------------------------------------------
  private def init() {
  }

 */


/*      val FIXWIDTH = 3
val oContainerSize = O.I[XWindow2](Office.controlFrame.getContainerWindow()).getOutputSize()
val iHorisontalMargin = 2
val iVerticalMargin = 2
val iPanelWidth = oContainerSize.Width
val iImageHeight = 100
var aPrevPanelRect = new Rectangle(0,0,0,0)
var iLabelHeight = 0
Office.panelsContainer.getControls().foreach(panel => {
val iLabelPrefferedSize = O.I[XLayoutConstrains](O.I[XControlContainer](panel).getControl("label")).getPreferredSize()
if (iLabelPrefferedSize.Width > iPanelWidth) {*/
/*                        Dim iArea As Integer
iArea = iLabelPrefferedSize.Height * iLabelPrefferedSize.Width
iLabelHeight = iArea/iPanelWidth
iLabelHeight = iLabelHeight\iLabelPrefferedSize.Height
iLabelHeight = IIf((iLabelHeight mod iLabelPrefferedSize.Height)&gt;0, iLabelHeight + 1, iLabelHeight)
iLabelHeight = iLabelHeight*iLabelPrefferedSize.Height*/
/*            iLabelHeight = iLabelPrefferedSize.Height
          } else {
            iLabelHeight = iLabelPrefferedSize.Height
          }
          O.I[XWindow](panel).setPosSize(iHorisontalMargin,
                                         iVerticalMargin + aPrevPanelRect.Y + aPrevPanelRect.Height,
                                         iPanelWidth - 2*iHorisontalMargin - FIXWIDTH,
                                         iImageHeight + iLabelHeight + 1, PosSize.POSSIZE)
          O.I[XWindow](O.I[XControlContainer](panel).getControl("img")).setPosSize(0,
                                                                                   0,
                                                                                   iPanelWidth - 2*iHorisontalMargin - FIXWIDTH,
                                                                                   iImageHeight, PosSize.POSSIZE)
          O.I[XWindow](O.I[XControlContainer](panel).getControl("label")).setPosSize(0,
                                                                                     iImageHeight + 1,
                                                                                     iPanelWidth - 2*iHorisontalMargin - FIXWIDTH,
                                                                                     iLabelHeight, PosSize.POSSIZE)
          aPrevPanelRect = O.I[XWindow](panel).getPosSize()
        })
*/


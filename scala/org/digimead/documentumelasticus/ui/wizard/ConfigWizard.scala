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

package org.digimead.documentumelasticus.ui.wizard

import com.sun.star.awt.WindowEvent
import com.sun.star.awt.XDialog
import com.sun.star.awt.XDialogEventHandler
import com.sun.star.awt.XDialogProvider
import com.sun.star.awt.XToolkit
import com.sun.star.awt.XTopWindowListener
import com.sun.star.awt.XWindowListener
import com.sun.star.beans.PropertyValue
import com.sun.star.deployment.XPackageInformationProvider
import com.sun.star.frame.XFrame
import com.sun.star.lang.EventObject
import com.sun.star.lang.XMultiComponentFactory
import com.sun.star.uno.XComponentContext
import org.digimead.documentumelasticus.Extension
import org.digimead.documentumelasticus.helper._
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ConfigWizard(val ctx: XComponentContext, val parent: XFrame) {
  private val mcf: XMultiComponentFactory = ctx.getServiceManager()
  private val logger : Logger = LoggerFactory.getLogger(this.getClass)
  private val packageInformationProvider = O.I[XPackageInformationProvider](ctx.getValueByName("/singletons/com.sun.star.deployment.PackageInformationProvider"))
  private val extensionLocation = packageInformationProvider.getPackageLocation(Extension.name)
  logger.info("active")
  private val xDialog = init()
  def init(): XDialog = {
    val xDialogProvider = O.SI[XDialogProvider](mcf, "com.sun.star.awt.DialogProvider", ctx)
    val dialogURL = "vnd.sun.star.script:DE_UI_OFFICE.BuildConfigurationWizard?location=application"
    val xDialog = xDialogProvider.createDialog(dialogURL)
    if (xDialog == null)
      throw new RuntimeException("dialog not found: \"" + dialogURL + "\"")
    xDialog
  }
  def execute(name: String): Array[PropertyValue] = {
    val result = Library.invokeMacro(mcf, "vnd.sun.star.script:DE_UI_OFFICE.ConfigWizard.DialogWithArg?language=Basic&location=application", ctx, Array(name))
    if (result != null && result.getClass.isArray) {
      result.asInstanceOf[Array[PropertyValue]]
    } else {
      null
    }
  }
  // ------------
  // - handlers -
  // ------------
  class ConfigWizardHandler extends XDialogEventHandler {
    def callHandlerMethod(xDialog: XDialog, aEventObject: Any, sMethodName: String): Boolean = {
      if (sMethodName == "external_event") {
        println("a")
/*              try {
                  return handleExternalEvent(xWindow, aEventObject);
              } catch (com.sun.star.uno.RuntimeException re) {
                  throw re;
              } catch (com.sun.star.uno.Exception e) {
                  throw new WrappedTargetException(sMethodName, this, e);
              }*/
      }
      true
    }
    def getSupportedMethodNames(): Array[String] = {
         return Array[String]("external_event")
    }
  }
  // - listeners -
  // -------------
  class TopWindowListener() extends XWindowListener with XTopWindowListener {
    val logger : Logger = LoggerFactory.getLogger(this.getClass)
    var initialResize = true
    // --------------------------------------
    // - implement trait XTopWindowListener -
    // --------------------------------------
    def windowOpened(e: EventObject) = {
      logger.trace("window opened")
    }
    def windowClosing(e: EventObject) = {
      logger.trace("window closing")
    }
    def windowClosed(e: EventObject) = {
      logger.trace("window closed")
    }
    def windowMinimized(e: EventObject) = {
      logger.trace("window minimized")
    }
    def windowNormalized(e: EventObject) = {
      logger.trace("window normalized")
    }
    def windowActivated(e: EventObject) = {
      logger.trace("window activated")
    }
    def windowDeactivated(e: EventObject) = {
      logger.trace("window deactivated")
    }
    // --------------------------------------
    // - implement trait XWindowListener -
    // --------------------------------------
    def windowHidden(e: EventObject) = {
      logger.trace("window hidden")
    }
    def windowShown(e: EventObject) = {
      logger.trace("window shown")
    }
    def windowMoved(e: WindowEvent) = {
      logger.trace("window moved")
    }
    def windowResized(e: WindowEvent) = {
      logger.trace("window resized")
    }
    // ----------------------------------
    // - implement trait XEventListener -
    // ----------------------------------
    def disposing(e: EventObject) = {
      logger.trace("disposing")
    }
  }
}

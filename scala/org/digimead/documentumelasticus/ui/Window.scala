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

import com.sun.star.awt.Rectangle
import com.sun.star.awt.WindowAttribute
import com.sun.star.awt.WindowClass
import com.sun.star.awt.WindowDescriptor
import com.sun.star.awt.XPointer
import com.sun.star.awt.XSystemChildFactory
import com.sun.star.awt.XSystemDependentWindowPeer
import com.sun.star.awt.XToolkit
import com.sun.star.awt.XWindow
import com.sun.star.awt.XWindowPeer
import com.sun.star.lang.XEventListener
import com.sun.star.uno.XComponentContext
import org.digimead.documentumelasticus.helper._

object Window {
   /**
    * create a new window which can be used as container window of an office frame
    * We know two modes for creation:
    *   - the office window will be a child of one of our java windows
    *   - the office will be a normal system window outside this java application
    * This behaviour will be regulated by the second parameter of this operation.
    * If a parentview is given the first mode will be activated - otherwhise
    * the second one.
    *
    * Note: First mode (creation of a child window) can be reached by two different
    *       ways.
    *   - pack the required window handle of our java window inside an UNO object
    *     to transport it to the remote office toolkit and get a child office
    *     window.
    *     This is the old way. It's better to use the second one - but to be
    *     future prove this old one should be tried too.
    *   - it's possible to pass the native window handle directly to the toolkit.
    *     A special interface method was enabled to accept that.
    *
    *   The right way to create an office window should be then:
    *   - try to use second creation mode (directly using of the window handle)
    *   - if it failed ... use the old way by packing the handle inside an object
    *
    * @param xSMGR
    *          we need a service manager to be able to create remote office
    *          services
    *
    * @param aParentView
    *          the java window as parent for the office window if an inplace office
    *          is required. If it is set to null the created office window will be
    *          a normal system window outside of our java application.
    *
    * @return [com.sun.star.awt.XWindow]
    *          The new created office window which can be used to set it as
    *          a ContainerWindow on an empty office frame.
    */
   def createWindow(ctx:XComponentContext): XWindow = {
      var xWindow: XWindow = null
      var xPeer: XWindowPeer = null

      // get access to toolkit of remote office to create the container window of
      // new target frame
      val xToolkit = O.SI[XToolkit](ctx.getServiceManager(), "com.sun.star.awt.Toolkit", ctx)

      // mode 1) create an external system window
      if (false) {
        // Describe the properties of the container window.
        val aDescriptor: WindowDescriptor = new WindowDescriptor()
        aDescriptor.Type = WindowClass.TOP;
        aDescriptor.WindowServiceName = "window";
        aDescriptor.ParentIndex = -1;
        aDescriptor.Parent = null;
        aDescriptor.Bounds = new Rectangle(0,0,0,0);
        aDescriptor.WindowAttributes = WindowAttribute.BORDER | WindowAttribute.MOVEABLE | WindowAttribute.SIZEABLE | WindowAttribute.CLOSEABLE
        xPeer = xToolkit.createWindow( aDescriptor)
        // mode 2) create an internal office window as child of our given java
        // parent window
      } else {
        // try new version of creation first: directly using of the window
        // handle. The old implementation of the corresponding toolkit method
        // requires a process ID. If this id isn't the right one a null object
        // is returned. But normaly nobody outside the office knows this id.
        // New version of this method ignore the id parameter and creation will
        // work.
        // Note: You must be shure if your window handle can be realy used by
        // the remote office. Means if this java client and the remote office
        // use the same display!
        val xChildFactory: XSystemChildFactory = O.I[XSystemChildFactory](xToolkit)
          //val nHandle: Int = aParentView.getHWND()
          //val nSystem: Short = aParentView.getNativeWindowSystemType()
          val lProcID: Array[Byte] = Array()
          //xPeer = xChildFactory.createSystemChild(nHandle.asInstanceOf[Object], lProcID, nSystem);

      }

      // It doesn't matter which way was used to get the window peer.
      // Cast it to the right return interface and return it.
      /*xWindow = (com.sun.star.awt.XWindow)UnoRuntime.queryInterface(
        com.sun.star.awt.XWindow.class,
        xPeer);*/

      return xWindow;
    }

   }

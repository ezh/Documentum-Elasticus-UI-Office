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

    /*aProps(0).Name = "DataSourceName"
    aProps(0).Value = "Bibliography"
    aProps(1).Name = "CommandType"
    aProps(1).Value = 2.asInstanceOf[Object] // SQL statement
    aProps(2).Name = "Command"
    aProps(2).Value = "SELECT * from biblio  WHERE Author like '%Sun%'"
    aProps(3).Name = "ShowTreeView"
    aProps(3).Value = false.asInstanceOf[Object]
    aProps(4).Name = "ShowTreeViewButton"
    aProps(4).Value = false.asInstanceOf[Object]
    aProps(5).Name = "ShowMenu"
    aProps(5).Value = true.asInstanceOf[Object]
    val frame2 = O.I[XComponentLoader](dataFrame).loadComponentFromURL(".component:DB/DataSourceBrowser", "_self", 0, aProps)*/


import com.sun.star.awt.FontWeight
import com.sun.star.awt.XWindow
import com.sun.star.awt.tree.TreeExpansionEvent
import com.sun.star.awt.tree.XMutableTreeDataModel
import com.sun.star.awt.tree.XMutableTreeNode
import com.sun.star.awt.tree.XTreeControl
import com.sun.star.awt.tree.XTreeExpansionListener
import com.sun.star.beans.PropertyValue
//import com.sun.star.beans.XIntrospection
import com.sun.star.beans.XPropertySet
import com.sun.star.frame.XComponentLoader
import com.sun.star.frame.XFrame
import com.sun.star.frame.XFramesSupplier
import com.sun.star.lang.EventObject
import com.sun.star.lang.XMultiComponentFactory
import com.sun.star.sheet.XCellAddressable
import com.sun.star.sheet.XCellRangeAddressable
import com.sun.star.sheet.XCellRangesQuery
import com.sun.star.sheet.XDatabaseRanges
import com.sun.star.sheet.XSheetAnnotation
import com.sun.star.sheet.XSheetAnnotationsSupplier
import com.sun.star.sheet.XSheetCondition
import com.sun.star.sheet.XSpreadsheet
import com.sun.star.sheet.XSpreadsheetDocument
import com.sun.star.sheet.XSpreadsheets
import com.sun.star.table.CellHoriJustify
import com.sun.star.table.XCell
import com.sun.star.table.XCellRange
import com.sun.star.text.XText
import com.sun.star.text.XTextRange
import com.sun.star.uno.AnyConverter
import com.sun.star.uno.XComponentContext
import com.sun.star.util.DateTime
import com.sun.star.util.XMergeable
import com.sun.star.util.XModifyBroadcaster
import com.sun.star.util.XModifyListener
import java.awt.Color
import org.digimead.documentumelasticus.helper._
import org.digimead.documentumelasticus.component.XBase
import org.digimead.documentumelasticus.storage.AvailableState
import org.digimead.documentumelasticus.storage.XFile
import org.digimead.documentumelasticus.storage.XFolder
import org.digimead.documentumelasticus.storage.XFolderUNO
import org.digimead.documentumelasticus.storage.XStorage
import org.digimead.documentumelasticus.Core
import org.digimead.documentumelasticus.Extension
import org.slf4j.LoggerFactory
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap

class TabStoragesOverview(override val name: String,
                          override val parentFrame: XFrame,
                          override val ctx: XComponentContext) extends Tab(name, parentFrame, ctx) {
  val logger = LoggerFactory.getLogger(this.getClass)
  private val core = ctx.getValueByName("/singletons/org.digimead.documentumelasticus.Core").asInstanceOf[Core]
  private val extension = ctx.getValueByName("/singletons/org.digimead.documentumelasticus.Extension").asInstanceOf[Extension]
  // "private:graphicrepository" means images.zip
  private val treeImgDir = "private:graphicrepository";
  private val treeExpandedURL  = treeImgDir + "/res/folderop.png"
  private val treeCollapsedURL = treeImgDir + "/res/foldercl.png"
  val (frame, container, treeModel, treeRoot, xSpreadsheetDocument) = init()
  val dataCellMap: HashMap[XBase, Tuple2[XCell, DateTime]] = HashMap() // folder of file, cell and date
  val cellDataMap: HashMap[XCell, XBase] = HashMap() // cell, folder or file
  def init(): (XFrame, OControlContainer,
               XMutableTreeDataModel, XMutableTreeNode, XSpreadsheetDocument) = {
    val (frame, container) = super.init(new MigLayout("insets 0, nocache")) // TODO fix bug and remove nocache
    val (treeDataModel,treeRootNode) = addTreeControl(container)
    val dataComponent = addDataViewer(frame, container)
    // create components grid
    // row 2 - core
    TabStoragesOverview.dataRows.append((extension.component(core.componentName),
                                         (mcf: XMultiComponentFactory,
                                          file: XBase,
                                          component: Extension.componentInfo,
                                          cell: XCell,
                                          sheet: XSpreadsheet,
                                          document: XSpreadsheetDocument,
                                          tab: TabStoragesOverview,
                                          ctx: XComponentContext) => {
          if (file.isInstanceOf[XFile])
            TabStoragesOverview.fileSetComponent(mcf, file.asInstanceOf[XFile], component, cell, sheet, xSpreadsheetDocument, tab, ctx)
        }))
    (frame, container, treeDataModel,treeRootNode, dataComponent)
  }
  def addTreeControl(parent: OControlContainer): (XMutableTreeDataModel, XMutableTreeNode) = {
    val tree = new OControl(mcf, "tree", "tree.TreeControl", parent, ctx)
    tree.setConstraint("width 30%:30%:8cm, height 100%")
    tree.model.setPropertyValue("SelectionType", com.sun.star.view.SelectionType.SINGLE)
    val treeDataModel = O.SI[XMutableTreeDataModel](mcf, "com.sun.star.awt.tree.MutableTreeDataModel", ctx)
    // tree expand listener
    O.I[XTreeControl](tree.control).addTreeExpansionListener(new TreeListener())
    // add root node
    val treeRootNode = treeDataModel.createNode("documentum elasticus", true)
    // set images of the root
    treeRootNode.setExpandedGraphicURL(treeExpandedURL)
    treeRootNode.setCollapsedGraphicURL(treeCollapsedURL)
    // set data to the root
    treeDataModel.setRoot(treeRootNode)
    tree.model.setPropertyValue("DataModel", treeDataModel)
    (treeDataModel,treeRootNode)
  }
  def addDataViewer(parentFrame: XFrame, parent: OControlContainer): XSpreadsheetDocument = {
    val dataPanel = new OControlContainer(mcf, "data_panel", parent, ctx, new MigLayout())
    dataPanel.setConstraint("push, grow")
    // add frame to container
    val xFrame = O.SI[XFrame](mcf, "com.sun.star.frame.Frame", ctx)
    xFrame.initialize(O.I[XWindow](dataPanel.control.getPeer))
    xFrame.setCreator(O.I[XFramesSupplier](parentFrame))
    O.I[XFramesSupplier](parentFrame).getFrames().append(xFrame)
    val dataComponent = O.I[XComponentLoader](xFrame).loadComponentFromURL("private:factory/scalc", "_self", 0, Array[PropertyValue]())
    O.I[XSpreadsheetDocument](dataComponent)
  }
  def refresh() {
    logger.debug("refresh")
    return
    // modify components grid
    // iterate over components
    extension.components.foreach(name => {
        val component = extension.component(name)
        if (component.componentSingleton != None &&
            component.componentSingleton != Some(null) &&
            !TabStoragesOverview.dataRows.exists(_._1 == component) &&
            component.componentSingleton.get.isInstanceOf[TabStoragesOverviewEntity]) {
          // new row
          logger.info("add new row to data sheets '" + component.componentTitle + "'")
          TabStoragesOverview.dataRows.append((component,
                                               (mcf: XMultiComponentFactory,
                                                file: XBase,
                                                component: Extension.componentInfo,
                                                cell: XCell,
                                                sheet: XSpreadsheet,
                                                document: XSpreadsheetDocument,
                                                tab: TabStoragesOverview,
                                                ctx: XComponentContext) => {
                if (file.isInstanceOf[XFile])
                  TabStoragesOverview.fileSetComponent(mcf, file.asInstanceOf[XFile], component, cell, sheet, xSpreadsheetDocument, tab, ctx)
              }))
        }
      })
    try {
      val sheets = xSpreadsheetDocument.getSheets()
      core.getStorages().foreach(storage => {
          // tree
          val storageNode = treeModel.createNode(storage.getName(), true)
          storageNode.setExpandedGraphicURL(treeExpandedURL)
          storageNode.setCollapsedGraphicURL(treeCollapsedURL)
          storageNode.setDataValue(storage.getID())
          treeRoot.appendChild(storageNode)
          // spreadsheet
          if (!sheets.hasByName(storage.getName()))
            sheetCreate(sheets, storage.getName())
          // TODO move out (by event)
          val sheet = O.I[XSpreadsheet](sheets.getByName(storage.getName()))
          sheetRefresh(sheet, storage.asInstanceOf[XStorage])
        })
      sheets.getElementNames.foreach(sheetName => {
          if (!core.getStorages().exists(_.getName == sheetName))
            sheets.removeByName(sheetName)
        })
    } catch {
      case e => logger.error(e.getMessage, e)
    }
  }
  private def sheetCreate(sheets: XSpreadsheets, name: String) {
    sheets.insertNewByName(name, 0) // TODO adjust position
    val sheet = O.I[XSpreadsheet](sheets.getByName(name))
    // add title
    val xAnnotations = O.I[XSheetAnnotationsSupplier](sheet).getAnnotations()
    for (i <- 0 to TabStoragesOverview.dataRows.length-1) {
      val cell = O.I[XCellRange](sheet).getCellByPosition(i, 0)
      if (TabStoragesOverview.dataRows(i)._1 == null) {
        // set name
        O.I[XText](cell).setString("name")
        xAnnotations.insertNew(O.I[XCellAddressable](cell).getCellAddress(), "%name%")
      } else {
        // set component
        val componentInfo = TabStoragesOverview.dataRows(i)._1
        O.I[XText](cell).setString(componentInfo.componentTitle)
        xAnnotations.insertNew(O.I[XCellAddressable](cell).getCellAddress(), "%" + componentInfo.componentName+ "%")
      }
    }
    // add autofilter
    // TODO exclude folders from range
    val databaseRanges = O.I[XDatabaseRanges](O.I[XPropertySet](xSpreadsheetDocument).getPropertyValue("DatabaseRanges"))
    if (!databaseRanges.hasByName("DE_" + name + "_autofilter")) {
      val cursor = sheet.createCursor()
      val addr = O.I[XCellRangeAddressable](cursor).getRangeAddress()
      val autofilterRangeAddr = O.I[XCellRangeAddressable](sheet.getCellRangeByPosition(0, 0, TabStoragesOverview.dataRows.length-1, addr.EndRow)).getRangeAddress()
      databaseRanges.addNewByName("DE_" + name + "_autofilter", autofilterRangeAddr)
    }
    val autofilterRange = databaseRanges.getByName("DE_" + name + "_autofilter")
    O.I[XPropertySet](autofilterRange).setPropertyValue("AutoFilter", true)
    // basic callback
    try {
      Library.invokeMacro(mcf, "vnd.sun.star.script:DE_UI_OFFICE.TabStoragesOverview.ThemeDataSheetNewTitle?language=Basic&location=application",
                          ctx, Array(sheet.getCellByPosition(0, 0), sheet, xSpreadsheetDocument))
    } catch {
      case e => logger.warn("Subroutine DE_UI_OFFICE.TabStoragesOverview.ThemeDataSheetNewTitle failed: " + e.getMessage())
    }
  }
  private def sheetRefresh(sheet: XSpreadsheet, storage: XStorage) {
    // before change basic callback
    try {
      Library.invokeMacro(mcf, "vnd.sun.star.script:DE_UI_OFFICE.TabStoragesOverview.ThemeDataSheetBeforeChange?language=Basic&location=application",
                          ctx, Array(xSpreadsheetDocument, sheet))
    } catch {
      case e => logger.warn("Subroutine DE_UI_OFFICE.TabStoragesOverview.ThemeDataSheetBeforeChange failed: " + e.getMessage())
    }
    // search annotations and create dataRows -> annotations map
    val specialColMap: HashMap[Int, XSheetAnnotation] = HashMap()
    val xAnnotations = O.I[XSheetAnnotationsSupplier](sheet).getAnnotations()
    for (i <- 0 to xAnnotations.getCount()-1) {
      val annotation = O.I[XSheetAnnotation](xAnnotations.getByIndex(i))
      val index = TabStoragesOverview.dataRows.findIndexOf(arg => O.I[XTextRange](annotation).getString() match {
          case "%name%" => true
          case annotation => if (arg._1 == null) {
              // empty arg._1 - first element with %name%
              false
            } else {
              annotation == "%" + arg._1.componentName + "%"
            }
        })
      if (index >= 0)
        specialColMap(index) = annotation
    }
    def visitFile(file: XFile, folder: XFolder) {
      if (dataCellMap.contains(file)) {
        // update
      } else {
        // insert
        sheetInsertFile(file, folder, sheet, specialColMap)
      }
    }
    def visitFolder(folder: XFolder) {
      logger.warn(folder.getName())
      if (dataCellMap.contains(folder)) {
        // update
        // TODO
      } else {
        // insert
        sheetInsertFolder(folder, sheet, specialColMap)
      }
      folder.getFolders.sortWith((f1,f2) => f1.getName() < f2.getName()).foreach(folder => {
          visitFolder(folder.asInstanceOf[XFolder])
        })
      folder.getFiles().sortWith((f1,f2) => f1.getName() < f2.getName()).foreach(file => {
          visitFile(file.asInstanceOf[XFile], folder.asInstanceOf[XFolder])
      })
    }
    // do it
    visitFolder(storage.getRoot().asInstanceOf[XFolder])
    // after change basic callback
    try {
      Library.invokeMacro(mcf, "vnd.sun.star.script:DE_UI_OFFICE.TabStoragesOverview.ThemeDataSheetAfterChange?language=Basic&location=application",
                          ctx, Array(xSpreadsheetDocument, sheet))
    } catch {
      case e => logger.warn("Subroutine DE_UI_OFFICE.TabStoragesOverview.ThemeDataSheetAfterChange failed: " + e.getMessage())
    }
  }
  def updateTree() {}
  private def sheetInsertFolder(folder: XFolder, sheet: XSpreadsheet, spCellMap: HashMap[Int, XSheetAnnotation]) {
    logger.warn("insert folder '" + folder.getName() + "'")
    val parent = folder.getParent()
    val row = if (parent == null) {
      // root
      val cursor = sheet.createCursor()
      val addr = O.I[XCellRangeAddressable](cursor).getRangeAddress()
      val range = sheet.getCellRangeByPosition(0, 0, addr.EndColumn, addr.EndRow)
      val ranges = O.I[XCellRangesQuery](range).queryContentCells(1023) // 1023 - flags of com.sun.star.sheet.CellFlags
      val lastUsedRow = ranges.getRangeAddresses().map(_.EndRow).max
      lastUsedRow + 1
    } else {
      // other
      def findFollowingAncestor(folder: XFolder): XFolder = {null} //TODO
      def findFollowingSibling(folder: XFolder): XFolder = {null} //TODO
      // search for begin row
      val ancestor = folder.getParent().asInstanceOf[XFolder]
      val rowBegin = O.I[XCellAddressable](dataCellMap(ancestor)._1).getCellAddress().Row
      // search for end row
      val followingAncestor = findFollowingAncestor(ancestor)
      val rowEnd = if (followingAncestor != null) {
        O.I[XCellAddressable](dataCellMap(followingAncestor)._1).getCellAddress().Row
      } else {
        val cursor = sheet.createCursor()
        val addr = O.I[XCellRangeAddressable](cursor).getRangeAddress()
        val range = sheet.getCellRangeByPosition(0, 0, addr.EndColumn, addr.EndRow)
        val ranges = O.I[XCellRangesQuery](range).queryContentCells(1023) // 1023 - flags of com.sun.star.sheet.CellFlags
        ranges.getRangeAddresses().map(_.EndRow).max + 1
      }
      // search for middle row
      val followingSibling = findFollowingSibling(folder)
      val rowMiddle = if (followingAncestor != null) {
        O.I[XCellAddressable](dataCellMap(followingSibling)._1).getCellAddress().Row
      } else {
        rowEnd
      }
      rowMiddle
    }
    // insert record
    TabStoragesOverview.folderBeforeInsert(mcf, folder, sheet.getCellByPosition(0, row), sheet, xSpreadsheetDocument, this, ctx)
    spCellMap.foreach(map => {
        val fn = TabStoragesOverview.dataRows(map._1)._2
        val col = map._2.getPosition.Column
        fn(mcf, folder, TabStoragesOverview.dataRows(map._1)._1, sheet.getCellByPosition(col, row), sheet, xSpreadsheetDocument, this, ctx)
      })
    TabStoragesOverview.folderAfterInsert(mcf, folder, sheet.getCellByPosition(0, row), sheet, xSpreadsheetDocument, this, ctx)
  }
  private def sheetInsertFile(file: XFile, folder: XFolder, sheet: XSpreadsheet, spCellMap: HashMap[Int, XSheetAnnotation]) {
    def findRow() = {
      def findFollowingAncestor(folder: XFolder): XFolder = {null} //TODO
      def findFollowingSibling(folder: XFolder): XFolder = {null} //TODO
      // search for begin row
      val ancestor = folder.getParent().asInstanceOf[XFolder]
      val rowBegin = O.I[XCellAddressable](dataCellMap(folder)._1).getCellAddress().Row
      // search for end row
      val followingAncestor = findFollowingAncestor(ancestor)
      val rowEnd = if (followingAncestor != null) {
        O.I[XCellAddressable](dataCellMap(followingAncestor)._1).getCellAddress().Row
      } else {
        val cursor = sheet.createCursor()
        val addr = O.I[XCellRangeAddressable](cursor).getRangeAddress()
        val range = sheet.getCellRangeByPosition(0, 0, addr.EndColumn, addr.EndRow)
        val ranges = O.I[XCellRangesQuery](range).queryContentCells(1023) // 1023 - flags of com.sun.star.sheet.CellFlags
        ranges.getRangeAddresses().map(_.EndRow).max + 1
      }
      // search for middle row
      val followingSibling = findFollowingSibling(folder)
      val rowMiddle = if (followingAncestor != null) {
        O.I[XCellAddressable](dataCellMap(followingSibling)._1).getCellAddress().Row
      } else {
        rowEnd
      }
      rowMiddle
    }
    val row = findRow()
    // insert record
    TabStoragesOverview.fileBeforeInsert(mcf, file, sheet.getCellByPosition(0, row), sheet, xSpreadsheetDocument, this, ctx)
    spCellMap.foreach(map => {
        val fn = TabStoragesOverview.dataRows(map._1)._2
        val col = map._2.getPosition.Column
        fn(mcf, file, TabStoragesOverview.dataRows(map._1)._1, sheet.getCellByPosition(col, row), sheet, xSpreadsheetDocument, this, ctx)
      })
    TabStoragesOverview.fileAfterInsert(mcf, file, sheet.getCellByPosition(0, row), sheet, xSpreadsheetDocument, this, ctx)
  }
  // -------------
  // - listeners -
  // -------------
  class TreeListener extends XTreeExpansionListener {
    class WorkerThread(val folder: XFolderUNO, val node: XMutableTreeNode, val waitNode: XMutableTreeNode) extends Runnable {
      def run(): Unit = {
        folder.getFiles().sortWith((f1,f2) => f1.getName() < f2.getName()).foreach(file => {
            val storageNode = treeModel.createNode(file.getName(), false)
            storageNode.setDataValue(file.getID())
            node.appendChild(storageNode)
          })
        folder.getFolders().sortWith((f1,f2) => f1.getName() < f2.getName()).foreach(folder => {
            val storageNode = treeModel.createNode(folder.getName(), true)
            storageNode.setExpandedGraphicURL(treeExpandedURL)
            storageNode.setCollapsedGraphicURL(treeCollapsedURL)
            storageNode.setDataValue(folder.getID())
            node.appendChild(storageNode)
          })
        if (node.getChildCount() == 1) {
          val newNode = treeModel.createNode("empty, click to new...", false)
          node.appendChild(newNode)
        }
        val idx = node.getIndex(waitNode)
        if (idx >= 0)
          node.removeChildByIndex(idx)
      }
    }
    def requestChildNodes(e: TreeExpansionEvent) {
      logger.debug("requestChildNodes")
      val node = O.I[XMutableTreeNode](e.Node)
      if (node == null)
        return
      val dataValue = node.getDataValue()
      if (!AnyConverter.isLong(dataValue))
        return
      val id = AnyConverter.toLong(dataValue)
      while (node.getChildCount() > 0)
        node.removeChildByIndex(0)
      val waitNode = treeModel.createNode("Please wait...", false)
      node.appendChild(waitNode)
      if (node.getParent() == treeRoot) {
        // storage
        logger.debug("process storage id " + id)
        try {
          val worker = new WorkerThread(core.getStorageByID(id).getRoot(), node, waitNode)
          val thread = new Thread(worker)
          thread.setDaemon(true)
          thread.start()
        } catch {
          case e => {
              logger.error(e.getMessage(), e)
              val idx = node.getIndex(waitNode)
              if (idx >= 0)
                node.removeChildByIndex(idx)
              val errNode = treeModel.createNode("Error. " + e.getMessage, false)
              node.appendChild(errNode)
          }
        }
      } else {
        // regular node
        logger.debug("process node id " + id)
        try {
          val worker = new WorkerThread(core.getFolderByID(id), node, waitNode)
          val thread = new Thread(worker)
          thread.setDaemon(true)
          thread.start()
        } catch {
          case e => {
              logger.error(e.getMessage(), e)
              val idx = node.getIndex(waitNode)
              if (idx >= 0)
                node.removeChildByIndex(idx)
              val errNode = treeModel.createNode("Error. " + e.getMessage, false)
              node.appendChild(errNode)
          }
        }
      }
    }
    def treeExpanding(e: TreeExpansionEvent) {
      logger.debug("treeExpanding")
    }
    def treeCollapsing(e: TreeExpansionEvent) {
      logger.debug("treeCollapsing")
    }
    def treeExpanded(e: TreeExpansionEvent) {
      logger.debug("treeExpanded")
    }
    def treeCollapsed(e: TreeExpansionEvent) {
      logger.debug("treeCollapsed")
    }
    // ----------------------------------
    // - implement trait XEventListener -
    // ----------------------------------
    def disposing(e: EventObject) = {
      logger.trace("disposing")
    }
  }
}

object TabStoragesOverview {
  private val logger = LoggerFactory.getLogger(this.getClass)
  val dataRows: ArrayBuffer[Tuple2[Extension.componentInfo, (
        XMultiComponentFactory,
        XBase,
        Extension.componentInfo,
        XCell,
        XSpreadsheet,
        XSpreadsheetDocument,
        TabStoragesOverview,
        XComponentContext) => Unit]] = ArrayBuffer(
    // row 1 - NAME
    (null, (mcf, folderOrFile, component, cell, sheet, document, tab, ctx) => {
        if (folderOrFile.isInstanceOf[XFolder])
          folderSetName(mcf, folderOrFile.asInstanceOf[XFolder], cell, sheet, document, tab, ctx)
        else
          fileSetName(mcf, folderOrFile.asInstanceOf[XFile], cell, sheet, document, tab, ctx)
      })
  )
  private def folderBeforeInsert(mcf: XMultiComponentFactory,
                                 folder: XFolder,
                                 cell: XCell, // first cell in row
                                 sheet: XSpreadsheet,
                                 document: XSpreadsheetDocument,
                                 tab: TabStoragesOverview,
                                 ctx: XComponentContext) {
    tab.dataCellMap(folder) = (cell, folder.getUpdatedAt())
    tab.cellDataMap(cell) = folder
    try {
      Library.invokeMacro(mcf, "vnd.sun.star.script:DE_UI_OFFICE.TabStoragesOverview.ThemeDataSheetFolderBeforeInsert?language=Basic&location=application",
                          ctx, Array(folder, cell, sheet, document))
    } catch {
      case e => logger.warn("Subroutine DE_UI_OFFICE.TabStoragesOverview.ThemeDataSheetFolderBeforeInsert failed: " + e.getMessage())
    }
  }
  private def folderSetName(mcf: XMultiComponentFactory,
                            folder: XFolder,
                            cell: XCell,
                            sheet: XSpreadsheet,
                            document: XSpreadsheetDocument,
                            tab: TabStoragesOverview,
                            ctx: XComponentContext) {
    // prepare
    val addrFrom = O.I[XCellAddressable](cell).getCellAddress()
    val cursor = sheet.createCursor()
    val addrTo = O.I[XCellRangeAddressable](cursor).getRangeAddress()
    val range = sheet.getCellRangeByPosition(addrFrom.Column + 1, addrFrom.Row, addrTo.EndColumn, addrFrom.Row)
    O.I[XMergeable](range).merge(true)
    // insert
    O.I[XText](cell).setString(folder.getName())
    val addr = O.I[XCellAddressable](cell).getCellAddress()
    val nextCell = O.I[XCellRange](sheet).getCellByPosition(addr.Column+1, addr.Row)
    O.I[XText](nextCell).setString(folder.getPath() + "/" + folder.getName())
    try {
      Library.invokeMacro(mcf, "vnd.sun.star.script:DE_UI_OFFICE.TabStoragesOverview.ThemeDataSheetFolderSetName?language=Basic&location=application",
                          ctx, Array(folder, cell, sheet, document))
    } catch {
      case e => logger.warn("Subroutine DE_UI_OFFICE.TabStoragesOverview.ThemeDataSheetFolderSetName failed: " + e.getMessage())
    }
  }
  private def folderAfterInsert(mcf: XMultiComponentFactory,
                                folder: XFolder,
                                cell: XCell, // first cell in row
                                sheet: XSpreadsheet,
                                document: XSpreadsheetDocument,
                                tab: TabStoragesOverview,
                                ctx: XComponentContext) {
    try {
      Library.invokeMacro(mcf, "vnd.sun.star.script:DE_UI_OFFICE.TabStoragesOverview.ThemeDataSheetFolderAfterInsert?language=Basic&location=application",
                          ctx, Array(folder, cell, sheet, document))
    } catch {
      case e => logger.warn("Subroutine DE_UI_OFFICE.TabStoragesOverview.ThemeDataSheetFolderAfterInsert failed: " + e.getMessage())
    }
  }
  private def fileBeforeInsert(mcf: XMultiComponentFactory,
                               file: XFile,
                               cell: XCell,
                               sheet: XSpreadsheet,
                               document: XSpreadsheetDocument,
                               tab: TabStoragesOverview,
                               ctx: XComponentContext) {
    try {
      Library.invokeMacro(mcf, "vnd.sun.star.script:DE_UI_OFFICE.TabStoragesOverview.ThemeDataSheetFileBeforeInsert?language=Basic&location=application",
                          ctx, Array(file, cell, sheet, document))
    } catch {
      case e => logger.warn("Subroutine DE_UI_OFFICE.TabStoragesOverview.ThemeDataSheetFileBeforeInsert failed: " + e.getMessage())
    }
  }
  private def fileSetName(mcf: XMultiComponentFactory,
                          file: XFile,
                          cell: XCell,
                          sheet: XSpreadsheet,
                          document: XSpreadsheetDocument,
                          tab: TabStoragesOverview,
                          ctx: XComponentContext) {
    O.I[XText](cell).setString(file.getName())
    try {
      Library.invokeMacro(mcf, "vnd.sun.star.script:DE_UI_OFFICE.TabStoragesOverview.ThemeDataSheetFileSetName?language=Basic&location=application",
                          ctx, Array(file, cell, sheet, document))
    } catch {
      case e => logger.warn("Subroutine DE_UI_OFFICE.TabStoragesOverview.ThemeDataSheetFileSetName failed: " + e.getMessage())
    }
  }
  private def fileSetComponent(mcf: XMultiComponentFactory,
                               file: XFile,
                               component: Extension.componentInfo,
                               cell: XCell,
                               sheet: XSpreadsheet,
                               document: XSpreadsheetDocument,
                               tab: TabStoragesOverview,
                               ctx: XComponentContext) {
    val cellProperty = O.I[XPropertySet](cell)
    cellProperty.setPropertyValue("CharWeight", FontWeight.BOLD)
    cellProperty.setPropertyValue("HoriJustify", CellHoriJustify.CENTER)
    val xValidPropSet = O.I[XPropertySet](cellProperty.getPropertyValue("Validation"))
    xValidPropSet.setPropertyValue("Type", com.sun.star.sheet.ValidationType.LIST)
    val xCondition = O.I[XSheetCondition](xValidPropSet)
    xCondition.setOperator(com.sun.star.sheet.ConditionOperator.EQUAL)
    xCondition.setFormula1("\"yes\";\"pass\";\"no\"")
    cellProperty.setPropertyValue("Validation", xValidPropSet)
    // add modify listener
    O.I[XModifyBroadcaster](cell).addModifyListener(new DataFileCellModifyListener(file, component, null, ctx))
    // modify listener update it to proper value
    O.I[XText](cell).setString("-")
    try {
      Library.invokeMacro(mcf, "vnd.sun.star.script:DE_UI_OFFICE.TabStoragesOverview.ThemeDataSheetFileSetComponent?language=Basic&location=application",
                          ctx, Array(file, component, cell, sheet, document))
    } catch {
      case e => logger.warn("Subroutine DE_UI_OFFICE.TabStoragesOverview.ThemeDataSheetFileSetComponent failed: " + e.getMessage())
    }
  }
  private def fileAfterInsert(mcf: XMultiComponentFactory,
                              file: XFile,
                              cell: XCell,
                              sheet: XSpreadsheet,
                              document: XSpreadsheetDocument,
                              tab: TabStoragesOverview,
                              ctx: XComponentContext) {
  }
  // -------------
  // - listeners -
  // -------------
  private class DataFileCellModifyListener(val file: XFile, val component: Extension.componentInfo, var availableState: AvailableState, val ctx: XComponentContext) extends XModifyListener {
    // -----------------------------------
    // - implement trait XModifyListener -
    // -----------------------------------
    def modified(e: EventObject) {
      val cell = O.I[XCell](e.Source)
      val cellProperty = O.I[XPropertySet](cell)
      // set want state
      val wantState: AvailableState = O.I[XText](cell).getString() match {
        case "yes" => AvailableState.AVAILABLE
        case "pass" => AvailableState.BLOCKED
        case "no" => AvailableState.NOT_AVAILABLE
        case _ => file.getAvailable(component.componentName)
      }
      if (wantState != availableState) {
        // try to change state
        val changed: Boolean = if (availableState == null) {
          true
        } else {
          wantState match {
            case AvailableState.AVAILABLE => {
                if (component.componentName == "org.digimead.documentumelasticus.Core")
                  TabStoragesOverviewCoreEntity.fileAdd(file, true)
                else
                  component.componentSingleton.get.asInstanceOf[TabStoragesOverviewEntity].fileAdd(file, true)
              }
            case AvailableState.BLOCKED => {
                if (component.componentName == "org.digimead.documentumelasticus.Core")
                  TabStoragesOverviewCoreEntity.fileSkip(file, true)
                else
                  component.componentSingleton.get.asInstanceOf[TabStoragesOverviewEntity].fileSkip(file, true)
              }
            case AvailableState.NOT_AVAILABLE => {
                if (component.componentName == "org.digimead.documentumelasticus.Core")
                  TabStoragesOverviewCoreEntity.fileDel(file, true)
                else
                  component.componentSingleton.get.asInstanceOf[TabStoragesOverviewEntity].fileDel(file, true)
              }
          }
        }
        if (changed) {
          availableState = wantState
          file.setAvailable(component.componentName, availableState)
        }
        availableState match {
          case AvailableState.AVAILABLE => {
              O.I[XText](cell).setString("yes")
              cellProperty.setPropertyValue("CharColor", new Color(0x00, 0x99, 0x00, 0x00).getRGB())
            }
          case AvailableState.BLOCKED => {
              O.I[XText](cell).setString("pass")
              cellProperty.setPropertyValue("CharColor", new Color(0x99, 0x00, 0x00, 0x00).getRGB())
            }
          case AvailableState.NOT_AVAILABLE => {
              O.I[XText](cell).setString("no")
              cellProperty.setPropertyValue("CharColor", new Color(0xA0, 0xA0, 0xA0, 0x00).getRGB())
            }
        }
      } else {

      }
//      val oMri = O.SI[XIntrospection](ctx.getServiceManager(), "mytools.Mri", ctx)
//      oMri.inspect(e)
    }
    // ----------------------------------
    // - implement trait XEventListener -
    // ----------------------------------
    def disposing(e: EventObject) = {
      logger.trace("disposing")
    }
  }
}

trait TabStoragesOverviewEntity {
  def fileAdd(file: XFile, interactive: Boolean): Boolean
  def fileSkip(file: XFile, interactive: Boolean): Boolean
  def fileDel(file: XFile, interactive: Boolean): Boolean
}

object TabStoragesOverviewCoreEntity extends TabStoragesOverviewEntity {
  private val logger = LoggerFactory.getLogger(this.getClass)
  def fileAdd(file: XFile, interactive: Boolean): Boolean = synchronized {
    logger.warn("coreFileAdd")
    true
  }
  def fileSkip(file: XFile, interactive: Boolean): Boolean = synchronized {
    logger.warn("coreFileSkip")
    true
  }
  def fileDel(file: XFile, interactive: Boolean): Boolean = synchronized {
    logger.warn("coreFileDel")
    true
  }
}

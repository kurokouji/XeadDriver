package xeadDriver;

/*
 * Copyright (c) 2013 WATANABE kozo <qyf05466@nifty.com>,
 * All rights reserved.
 *
 * This file is part of XEAD Driver.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the XEAD Project nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.*;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.EventObject;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.StringTokenizer;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFFooter;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.hssf.util.HSSFColor;
import org.w3c.dom.*;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class XF310 extends JDialog implements XFExecutable, XFScriptable {
	private static final long serialVersionUID = 1L;
	private org.w3c.dom.Element functionElement_ = null;
	private Session session_ = null;
	private boolean instanceIsAvailable = true;
	private boolean isToBeCanceled = false;
	private boolean anyRecordsDeleted = false;
	private int instanceArrayIndex_ = -1;
	private int programSequence;
	private StringBuffer processLog = new StringBuffer();
	private XF310_KeyInputDialog keyInputDialog = null;
	private XF310_AddRowList addRowListDialog = null;
	private JPanel jPanelMain = new JPanel();
	private JPanel jPanelHeaderFields = new JPanel();
	private JScrollPane jScrollPaneHeaderFields = new JScrollPane();
	private XF310_HeaderTable headerTable;
	private ReferChecker headerReferChecker = null;
	private XF310_HeaderField firstEditableHeaderField = null;
	private HashMap<String, Object> parmMap_ = null;
	private HashMap<String, Object> returnMap_ = new HashMap<String, Object>();
	private ArrayList<XF310_HeaderField> headerFieldList = new ArrayList<XF310_HeaderField>();
	private ArrayList<XF310_HeaderReferTable> headerReferTableList = new ArrayList<XF310_HeaderReferTable>();
	private NodeList headerReferElementList;
	private XF310_DetailTable detailTable;
	private ReferChecker detailReferChecker = null;
	private ArrayList<XF310_DetailColumn> detailColumnList = new ArrayList<XF310_DetailColumn>();
	private ArrayList<XF310_DetailReferTable> detailReferTableList = new ArrayList<XF310_DetailReferTable>();
	private ArrayList<XF310_DetailRowNumber> deleteRowNumberList = new ArrayList<XF310_DetailRowNumber>();
	private ArrayList<WorkingRow> tableRowList = new ArrayList<WorkingRow>();
	private TableModelEditableList tableModelMain = null;
	private JTable jTableMain = new JTable();
	private String initialMsg = "";
	private NodeList detailReferElementList = null;
	private TableHeadersRenderer headersRenderer;
	private TableCellsRenderer cellsRenderer;
	private TableCellsEditor cellsEditor;
	private boolean isHeaderResizing = false;
	private JPanel jPanelBottom = new JPanel();
	private JPanel jPanelButtons = new JPanel();
	private Action checkAction = new AbstractAction(){
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e){
			messageList.clear();
			checkErrorsToUpdate(true, true);
			setMessagesOnPanel();
		}
	};
	private Action tabAction = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e) {
			cellsEditor.transferFocusOfCell(-1, -1, true);
		}
	};
	private Action shiftTabAction = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e) {
			cellsEditor.transferFocusOfCell(-1, -1, false);
		}
	};
	private Action arrowUpAction = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e) {
			cellsEditor.requestFocusOnVerticalCell("UP");
		}
	};
	private Action arrowDownAction = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e) {
			cellsEditor.requestFocusOnVerticalCell("DOWN");
		}
	};
	private String buttonUpdateCaption = "";
	private JPanel jPanelInfo = new JPanel();
	private GridLayout gridLayoutButtons = new GridLayout();
	private GridLayout gridLayoutInfo = new GridLayout();
	private ArrayList<String> messageList = new ArrayList<String>();
	private JLabel jLabelFunctionID = new JLabel();
	private JLabel jLabelSessionID = new JLabel();
	private JProgressBar jProgressBar = new JProgressBar();
	private JSplitPane jSplitPaneMain = new JSplitPane();
	private JPanel jPanelCenter = new JPanel();
	private JScrollPane jScrollPaneTable = new JScrollPane();
	private JScrollPane jScrollPaneMessages = new JScrollPane();
	private JTextArea jTextAreaMessages = new JTextArea();
	private Color selectionColorWithFocus = new Color(49,106,197);
	private Color selectionColorWithoutFocus = new Color(213,213,213);
	private SortableDomElementListModel sortingList1;
	private SortableDomElementListModel sortingList2;
	private Action helpAction = new AbstractAction(){
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e){
			session_.browseHelp();
		}
	};
	private JPanel[] jPanelButtonArray = new JPanel[7];
	private JButton[] jButtonArray = new JButton[7];
	private Action[] actionButtonArray = new Action[7];
	private String[] actionDefinitionArray = new String[7];
	private ScriptEngine scriptEngine;
	private Bindings engineScriptBindings;
	private String scriptNameRunning = "";
	private final int FIELD_HORIZONTAL_MARGIN = 1;
	private final int FIELD_VERTICAL_MARGIN = 5;
	private final int FONT_SIZE = 14;
	private ByteArrayOutputStream exceptionLog;
	private PrintStream exceptionStream;
	private String exceptionHeader = "";
	private long detailRowNoLastValue = 0;
	private boolean isAbleToSetupKeysOfNewRecord;
	private String addRowListTitle = "";
	private KeyStroke keyStrokeToUpdate = null;
	private HashMap<String, Object> headerFieldValueMap = new HashMap<String, Object>();
	private HashMap<String, Object> headerFieldOldValueMap = new HashMap<String, Object>();
	private Thread threadToSetupReferChecker = null;
	private HSSFPatriarch patriarch = null;

	public XF310(Session session, int instanceArrayIndex) {
		super(session, "", true);
		try {
			session_ = session;
			instanceArrayIndex_ = instanceArrayIndex;
			initComponentsAndVariants();
		} catch(Exception e) {
			e.printStackTrace(exceptionStream);
		}
	}

	void initComponentsAndVariants() throws Exception {
		jPanelMain.setLayout(new BorderLayout());
		jSplitPaneMain.setOrientation(JSplitPane.VERTICAL_SPLIT);
		jSplitPaneMain.add(jPanelCenter, JSplitPane.TOP);
		jSplitPaneMain.add(jScrollPaneMessages, JSplitPane.BOTTOM);
		jSplitPaneMain.setFocusable(false);
		jScrollPaneHeaderFields.getViewport().add(jPanelHeaderFields, null);
		jScrollPaneHeaderFields.setBorder(null);
		jScrollPaneHeaderFields.setFocusable(false);
		jPanelHeaderFields.setLayout(null);
		jPanelHeaderFields.setFocusable(false);
		jTextAreaMessages.setEditable(false);
		jTextAreaMessages.setBorder(BorderFactory.createEtchedBorder());
		jTextAreaMessages.setFont(new java.awt.Font("SansSerif", 0, FONT_SIZE));
		jTextAreaMessages.setFocusable(false);
		jTextAreaMessages.setLineWrap(true);
		jTextAreaMessages.setWrapStyleWord(true);
		jScrollPaneMessages.getViewport().add(jTextAreaMessages, null);
		jPanelCenter.setLayout(new BorderLayout());
		jPanelCenter.add(jScrollPaneHeaderFields, BorderLayout.NORTH);
		jPanelCenter.add(jScrollPaneTable, BorderLayout.CENTER);

		jTableMain.setFont(new java.awt.Font("SansSerif", 0, FONT_SIZE));
		jTableMain.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		jTableMain.setRowSelectionAllowed(false);
		jTableMain.setFocusable(false);
		JTableHeader header = new JTableHeader(jTableMain.getColumnModel()) {
			private static final long serialVersionUID = 1L;
			public String getToolTipText(MouseEvent e) {
				return headersRenderer.getToolTipText(e);
			}
		};
		header.setResizingAllowed(false);
		header.setReorderingAllowed(false);
		header.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (headersRenderer.hasMouseOnColumnBorder(e.getX())) {
					isHeaderResizing = true;
					headersRenderer.setSizingHeader(e.getX());
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (isHeaderResizing) {
					headersRenderer.setNewBoundsToHeaders(e.getX());
					TableColumn column = jTableMain.getColumnModel().getColumn(0);
					column.setPreferredWidth(headersRenderer.getWidth());
					cellsRenderer.setupCellBounds();
					jScrollPaneTable.updateUI();
				}
				isHeaderResizing = false;
			}
			public void mouseExited(MouseEvent e) {
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		});
		header.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				if (headersRenderer.hasMouseOnColumnBorder(e.getX())) {
					setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
				} else {
					setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
			}
			public void mouseDragged(MouseEvent e) {
				Graphics2D g2 = (Graphics2D)jScrollPaneTable.getGraphics();
				g2.setColor(Color.gray.darker());
				int pointX;
				if (e.getX() < 30) {
					pointX = 30;
				} else {
					pointX = e.getX()-1;
				}
				g2.fillRect(pointX,0,3,jTableMain.getHeight()+headersRenderer.getHeight());
				jScrollPaneTable.updateUI();
			}
		});
		jTableMain.setTableHeader(header);
		jScrollPaneTable.getViewport().add(jTableMain, null);
		jScrollPaneTable.addMouseListener(new XF310_jScrollPaneTable_mouseAdapter(this));
		jScrollPaneTable.setFocusable(true);
		jScrollPaneTable.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(FocusEvent e) {
				if (jTableMain.getRowCount() > 0) {
					jTableMain.editCellAt(0, 0);
					cellsEditor.transferFocusOfCell(0, 0, true);
				}
			}
		});

		jPanelBottom.setPreferredSize(new Dimension(10, 35));
		jPanelBottom.setLayout(new BorderLayout());
		jPanelBottom.setBorder(null);
		jLabelFunctionID.setFont(new java.awt.Font("Dialog", 0, FONT_SIZE));
		jLabelFunctionID.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabelFunctionID.setForeground(Color.gray);
		jLabelFunctionID.setFocusable(false);
		jLabelSessionID.setFont(new java.awt.Font("Dialog", 0, FONT_SIZE));
		jLabelSessionID.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabelSessionID.setForeground(Color.gray);
		jLabelSessionID.setFocusable(false);
		jPanelButtons.setBorder(null);
		jPanelButtons.setLayout(gridLayoutButtons);
		jPanelButtons.setFocusable(false);
		gridLayoutInfo.setColumns(1);
		gridLayoutInfo.setRows(2);
		gridLayoutInfo.setVgap(4);
		jPanelInfo.setLayout(gridLayoutInfo);
		jPanelInfo.add(jLabelSessionID);
		jPanelInfo.add(jLabelFunctionID);
		jPanelInfo.setFocusable(false);
		jProgressBar.setStringPainted(true);
		jProgressBar.setString(XFUtility.RESOURCE.getString("ChrossCheck"));
		gridLayoutButtons.setColumns(7);
		gridLayoutButtons.setRows(1);
		gridLayoutButtons.setHgap(2);

		for (int i = 0; i < 7; i++) {
			jButtonArray[i] = new JButton();
			jButtonArray[i].setBounds(new Rectangle(0, 0, 90, 30));
			jButtonArray[i].setFocusable(false);
			jButtonArray[i].addActionListener(new XF310_FunctionButton_actionAdapter(this));
			jPanelButtonArray[i] = new JPanel();
			jPanelButtonArray[i].setLayout(new BorderLayout());
			jPanelButtonArray[i].add(jButtonArray[i], BorderLayout.CENTER);
			actionButtonArray[i] = new ButtonAction(jButtonArray[i]);
			jPanelButtons.add(jPanelButtonArray[i]);
		}

		jPanelMain.add(jSplitPaneMain, BorderLayout.CENTER);
		jPanelMain.add(jPanelBottom, BorderLayout.SOUTH);
		jPanelBottom.add(jPanelInfo, BorderLayout.EAST);
		jPanelBottom.add(jPanelButtons, BorderLayout.CENTER);
		this.getContentPane().add(jPanelMain, BorderLayout.CENTER);
	}

	public HashMap<String, Object> execute(org.w3c.dom.Element functionElement, HashMap<String, Object> parmMap) {
		org.w3c.dom.Element workElement;
		String workStr, workAlias, workTableID, workFieldID;
		StringTokenizer workTokenizer;

		try {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));

			////////////////////////
			// Process parameters //
			////////////////////////
			parmMap_ = parmMap;
			if (parmMap_ == null) {
				parmMap_ = new HashMap<String, Object>();
			}
			returnMap_.clear();
			returnMap_.putAll(parmMap_);
			returnMap_.put("RETURN_CODE", "21");

			///////////////////////////
			// Initializing variants //
			///////////////////////////
			instanceIsAvailable = false;
			isToBeCanceled = false;
			exceptionLog = new ByteArrayOutputStream();
			exceptionStream = new PrintStream(exceptionLog);
			exceptionHeader = "";
			processLog.delete(0, processLog.length());
			messageList.clear();
			functionElement_ = functionElement;
			programSequence = session_.writeLogOfFunctionStarted(functionElement_.getAttribute("ID"), functionElement_.getAttribute("Name"));
			threadToSetupReferChecker = null;
			keyInputDialog = null;
			anyRecordsDeleted = false;
			threadToSetupReferChecker = null;

			//////////////////////////////////////
			// Setup Script Engine and Bindings //
			//////////////////////////////////////
			scriptEngine = session_.getScriptEngineManager().getEngineByName("js");
			engineScriptBindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
			engineScriptBindings.clear();
			engineScriptBindings.put("instance", (XFScriptable)this);
			
			//////////////////////////////
			// Set panel configurations //
			//////////////////////////////
			jLabelSessionID.setText(session_.getSessionID());
			jLabelFunctionID.setText("310" + "-" + instanceArrayIndex_ + "-" + functionElement_.getAttribute("ID"));
			FontMetrics metrics = jLabelFunctionID.getFontMetrics(new java.awt.Font("Dialog", 0, FONT_SIZE));
			jPanelInfo.setPreferredSize(new Dimension(metrics.stringWidth(jLabelFunctionID.getText()), 35));
			this.setTitle(functionElement_.getAttribute("Name"));
	        Rectangle screenRect = session_.getMenuRectangle();
			if (functionElement_.getAttribute("Size").equals("")) {
				this.setPreferredSize(new Dimension(screenRect.width, screenRect.height));
				this.setLocation(screenRect.x, screenRect.y);
			} else {
				if (!functionElement_.getAttribute("Size").equals("AUTO")) {
					workTokenizer = new StringTokenizer(functionElement_.getAttribute("Size"), ";" );
					int width = Integer.parseInt(workTokenizer.nextToken());
					int height = Integer.parseInt(workTokenizer.nextToken());
					this.setPreferredSize(new Dimension(width, height));
					int posX = ((screenRect.width - width) / 2) + screenRect.x;
					int posY = ((screenRect.height - height) / 2) + screenRect.y;
					this.setLocation(posX, posY);
				}
			}
			initialMsg = functionElement_.getAttribute("InitialMsg");
			jPanelBottom.remove(jProgressBar);
			jPanelBottom.add(jPanelInfo, BorderLayout.EAST);

			/////////////////////////////////////////////////
			// Setup information of Header Table and Lists //
			/////////////////////////////////////////////////
			headerTable = new XF310_HeaderTable(functionElement_, this);
			if (headerTable.getUpdateCounterID().equals("")) {
				throw new Exception(XFUtility.RESOURCE.getString("FunctionError51"));
			} else {
				headerReferTableList.clear();
				headerReferElementList = headerTable.getTableElement().getElementsByTagName("Refer");
				sortingList1 = XFUtility.getSortedListModel(headerReferElementList, "Order");
				for (int i = 0; i < sortingList1.getSize(); i++) {
					headerReferTableList.add(new XF310_HeaderReferTable((org.w3c.dom.Element)sortingList1.getElementAt(i), this));
				}
			}

			/////////////////////////////////////////////////
			// Setup Header Fields and Fetch Header Record //
			/////////////////////////////////////////////////
			jPanelHeaderFields.removeAll();
			headerFieldList.clear();
			firstEditableHeaderField = null;
			Dimension dimOfPriviousField = new Dimension(0,0);
			Dimension dim = new Dimension(0,0);
			int posX = 0;
			int posY = 0;
			int biggestWidth = 300;
			int biggestHeight = 30;
			boolean firstVisibleField = true;
			XFEditableField zipField = null;
			
			/////////////////////////////////////////////////
			// Add visible fields on the header field list //
			/////////////////////////////////////////////////
			NodeList headerFieldElementList = functionElement_.getElementsByTagName("Field");
			sortingList1 = XFUtility.getSortedListModel(headerFieldElementList, "Order");
			for (int i = 0; i < sortingList1.getSize(); i++) {
				headerFieldList.add(new XF310_HeaderField((org.w3c.dom.Element)sortingList1.getElementAt(i), this));
				if (headerFieldList.get(i).getTypeOptionList().contains("ZIPNO")) {
					zipField = headerFieldList.get(i).getComponent();
				}
				if (headerFieldList.get(i).getTypeOptionList().contains("ZIPADRS")) {
					headerFieldList.get(i).setRefferComponent(zipField);
				}
				if (firstVisibleField) {
					posX = 0;
					posY = this.FIELD_VERTICAL_MARGIN + 3;
					firstVisibleField = false;
				} else {
					if (headerFieldList.get(i).isHorizontal()) {
						posX = posX + dimOfPriviousField.width + headerFieldList.get(i).getPositionMargin() + this.FIELD_HORIZONTAL_MARGIN;
					} else {
						posX = 0;
						posY = posY + dimOfPriviousField.height+ headerFieldList.get(i).getPositionMargin() + this.FIELD_VERTICAL_MARGIN;
					}
				}
				dim = headerFieldList.get(i).getPreferredSize();
				headerFieldList.get(i).setBounds(posX, posY, dim.width, dim.height);
				jPanelHeaderFields.add(headerFieldList.get(i));

				if (posX + dim.width > biggestWidth) {
					biggestWidth = posX + dim.width;
				}
				if (posY + dim.height > biggestHeight) {
					biggestHeight = posY + dim.height;
				}

				if (headerFieldList.get(i).isHorizontal()) {
					dimOfPriviousField = new Dimension(dim.width, XFUtility.FIELD_UNIT_HEIGHT);
				} else {
					dimOfPriviousField = new Dimension(dim.width, dim.height);
				}
			}

			////////////////////////////////////////////
			// Setup panel sizes if AUTO is specified //
			////////////////////////////////////////////
			if (functionElement_.getAttribute("Size").equals("AUTO")) {
				int strViewWidth = 0;
				if (!functionElement_.getAttribute("StructureTable").equals("")) {
					strViewWidth = Integer.parseInt(functionElement_.getAttribute("StructureViewWidth"));
				}
				int workWidth = biggestWidth + 50 + strViewWidth;
				if (workWidth < 800) {
					workWidth = 800;
				}
				if (workWidth > screenRect.width) {
					workWidth = screenRect.width;
					posX = screenRect.x;
				} else {
					posX = ((screenRect.width - workWidth) / 2) + screenRect.x;
					if ((posX + workWidth + 10) < screenRect.width) {
						posX = posX + 10;
					}
				}
				int workHeight = biggestHeight + 500;
				if (workHeight > (screenRect.height - 60)) {
					workHeight = screenRect.height - 60;
					posY = screenRect.y + 30;
				} else {
					posY = ((screenRect.height - workHeight) / 2) + screenRect.y;
				}
				this.setPreferredSize(new Dimension(workWidth, workHeight));
				this.setLocation(posX, posY);
			}
			
			////////////////////////////////////////////
			// Add header table keys as HIDDEN fields //
			////////////////////////////////////////////
			for (int i = 0; i < headerTable.getKeyFieldIDList().size(); i++) {
				if (!containsHeaderField(headerTable.getTableID(), "", headerTable.getKeyFieldIDList().get(i))) {
					headerFieldList.add(new XF310_HeaderField(headerTable.getTableID(), "", headerTable.getKeyFieldIDList().get(i), this));
				}
			}
			
			////////////////////////////////////////////
			// Add unique key fields as HIDDEN fields //
			////////////////////////////////////////////
			for (int i = 0; i < headerTable.getUniqueKeyList().size(); i++) {
				workTokenizer = new StringTokenizer(headerTable.getUniqueKeyList().get(i), ";" );
				while (workTokenizer.hasMoreTokens()) {
					workFieldID = workTokenizer.nextToken();
					if (!containsHeaderField(headerTable.getTableID(), "", workFieldID)) {
						headerFieldList.add(new XF310_HeaderField(headerTable.getTableID(), "", workFieldID, this));
					}
				}
			}
			
			//////////////////////////////////////////////////////////////
			// Analyze fields in scripts and add them as HIDDEN columns //
			//////////////////////////////////////////////////////////////
			for (int i = 0; i < headerTable.getScriptList().size(); i++) {
				if	(headerTable.getScriptList().get(i).isToBeRunAtEvent("BR", "")
				|| headerTable.getScriptList().get(i).isToBeRunAtEvent("AR", "")
				|| headerTable.getScriptList().get(i).isToBeRunAtEvent("BU", "")
				|| headerTable.getScriptList().get(i).isToBeRunAtEvent("AU", "")) {
					for (int j = 0; j < headerTable.getScriptList().get(i).getFieldList().size(); j++) {
						workTokenizer = new StringTokenizer(headerTable.getScriptList().get(i).getFieldList().get(j), "." );
						workAlias = workTokenizer.nextToken();
						workTableID = getTableIDOfTableAlias(workAlias);
						workFieldID = workTokenizer.nextToken();
						if (!containsHeaderField(workTableID, workAlias, workFieldID)) {
							workElement = session_.getFieldElement(workTableID, workFieldID);
							if (workElement == null) {
								String msg = XFUtility.RESOURCE.getString("FunctionError1") + headerTable.getTableID() + XFUtility.RESOURCE.getString("FunctionError2") + headerTable.getScriptList().get(i).getName() + XFUtility.RESOURCE.getString("FunctionError3") + workAlias + "_" + workFieldID + XFUtility.RESOURCE.getString("FunctionError4");
								JOptionPane.showMessageDialog(null, msg);
								throw new Exception(msg);
							} else {
								if (headerTable.isValidDataSource(workTableID, workAlias, workFieldID)) {
									headerFieldList.add(new XF310_HeaderField(workTableID, workAlias, workFieldID, this));
								}
							}
						}
					}
				}
			}
			
			//////////////////////////////////////////////////////////////////////////////
			// Analyze header refer tables and add their fields as HIDDEN header fields //
			//////////////////////////////////////////////////////////////////////////////
			for (int i = headerReferTableList.size()-1; i > -1; i--) {
				for (int j = 0; j < headerReferTableList.get(i).getFieldIDList().size(); j++) {
					if (containsHeaderField(headerReferTableList.get(i).getTableID(), headerReferTableList.get(i).getTableAlias(), headerReferTableList.get(i).getFieldIDList().get(j))) {
						headerReferTableList.get(i).setToBeExecuted(true);
						break;
					}
				}
				if (!headerReferTableList.get(i).isToBeExecuted()) {
					for (int j = 0; j < headerReferTableList.get(i).getWithKeyFieldIDList().size(); j++) {
						workTokenizer = new StringTokenizer(headerReferTableList.get(i).getWithKeyFieldIDList().get(j), "." );
						workAlias = workTokenizer.nextToken();
						workFieldID = workTokenizer.nextToken();
						if (workAlias.equals(headerTable.getTableID()) && containsHeaderField(headerTable.getTableID(), "", workFieldID)) {
							headerReferTableList.get(i).setToBeExecuted(true);
							break;
						}
					}
				}
				if (headerReferTableList.get(i).isToBeExecuted()) {
					for (int j = 0; j < headerReferTableList.get(i).getFieldIDList().size(); j++) {
						if (!containsHeaderField(headerReferTableList.get(i).getTableID(), headerReferTableList.get(i).getTableAlias(), headerReferTableList.get(i).getFieldIDList().get(j))) {
							headerFieldList.add(new XF310_HeaderField(headerReferTableList.get(i).getTableID(), headerReferTableList.get(i).getTableAlias(), headerReferTableList.get(i).getFieldIDList().get(j), this));
						}
					}
					for (int j = 0; j < headerReferTableList.get(i).getWithKeyFieldIDList().size(); j++) {
						workTokenizer = new StringTokenizer(headerReferTableList.get(i).getWithKeyFieldIDList().get(j), "." );
						workAlias = workTokenizer.nextToken();
						workTableID = getTableIDOfTableAlias(workAlias);
						workFieldID = workTokenizer.nextToken();
						if (!containsHeaderField(workTableID, workAlias, workFieldID)) {
							headerFieldList.add(new XF310_HeaderField(workTableID, workAlias, workFieldID, this));
						}
					}
				}
			}
			
			////////////////////////////////////////////////////////
			// Add detail fields on header table as HIDDEN fields //
			////////////////////////////////////////////////////////
			NodeList columnFieldList = functionElement_.getElementsByTagName("Column");
			for (int i = 0; i < columnFieldList.getLength(); i++) {
				workElement = (org.w3c.dom.Element)columnFieldList.item(i);
				workStr = workElement.getAttribute("DataSource");
				if (workStr.substring(0, workStr.indexOf(".")).equals(headerTable.getTableID())) {
					if (!containsHeaderField(headerTable.getTableID(), "", workStr.substring(workStr.indexOf(".") + 1, workStr.length()))) {
						headerFieldList.add(new XF310_HeaderField(headerTable.getTableID(), "", workStr.substring(workStr.indexOf(".") + 1, workStr.length()), this));
					}
				}
			}
			
			/////////////////////////////////////
			// Set size of header fields panel //
			/////////////////////////////////////
			jPanelHeaderFields.setPreferredSize(new Dimension(biggestWidth, biggestHeight));
			jSplitPaneMain.setDividerLocation(this.getPreferredSize().height - 125);
			this.pack();

			/////////////////////////
			// Fetch Header Record //
			/////////////////////////
			fetchHeaderRecord();

			if (!this.isToBeCanceled) {

				///////////////////////////////////////
				// Setup information of detail table //
				///////////////////////////////////////
				detailTable = new XF310_DetailTable(functionElement_, this);
				if (detailTable.getUpdateCounterID().equals("")) {
					throw new Exception(XFUtility.RESOURCE.getString("FunctionError51"));
				} else {
					detailReferTableList.clear();
					detailReferElementList = detailTable.getTableElement().getElementsByTagName("Refer");
					sortingList2 = XFUtility.getSortedListModel(detailReferElementList, "Order");
					for (int j = 0; j < sortingList2.getSize(); j++) {
						detailReferTableList.add(new XF310_DetailReferTable((org.w3c.dom.Element)sortingList2.getElementAt(j), this));
					}
					deleteRowNumberList.clear();
				}

				//////////////////////////////////////////
				// Add and setup columns on table model //
				//////////////////////////////////////////
				tableModelMain = new TableModelEditableList();
				jTableMain.setModel(tableModelMain);
				detailColumnList.clear();
				int columnIndex = 0;
				columnFieldList = functionElement_.getElementsByTagName("Column");
				sortingList2 = XFUtility.getSortedListModel(columnFieldList, "Order");
				for (int j = 0; j < sortingList2.getSize(); j++) {
					detailColumnList.add(new XF310_DetailColumn((org.w3c.dom.Element)sortingList2.getElementAt(j), this));
					if (detailColumnList.get(j).isVisibleOnPanel()) {
						columnIndex++;
						detailColumnList.get(j).setColumnIndex(columnIndex);
					}
				}
				headersRenderer = new TableHeadersRenderer(); 
				cellsRenderer = new TableCellsRenderer(headersRenderer); 
				cellsEditor = new TableCellsEditor(headersRenderer); 
				jTableMain.setRowHeight(headersRenderer.getHeight());
				tableModelMain.addColumn(""); //column index:0 //
				TableColumn column = jTableMain.getColumnModel().getColumn(0);
				column.setHeaderRenderer(headersRenderer);
				column.setCellRenderer(cellsRenderer);
				column.setCellEditor(cellsEditor);
				column.setPreferredWidth(headersRenderer.getWidth());
				
				//////////////////////////////////////////////////
				// Add detail table key fields as HIDDEN column //
				//////////////////////////////////////////////////
				for (int j = 0; j < detailTable.getKeyFieldIDList().size(); j++) {
					if (!containsDetailField(detailTable.getTableID(), "", detailTable.getKeyFieldIDList().get(j))) {
						detailColumnList.add(new XF310_DetailColumn(detailTable.getTableID(), "", detailTable.getKeyFieldIDList().get(j), this));
					}
				}

				////////////////////////////////////////////
				// Add unique key fields as HIDDEN column //
				////////////////////////////////////////////
				for (int i = 0; i < detailTable.getUniqueKeyList().size(); i++) {
					workTokenizer = new StringTokenizer(detailTable.getUniqueKeyList().get(i), ";" );
					while (workTokenizer.hasMoreTokens()) {
						workFieldID = workTokenizer.nextToken();
						if (!containsDetailField(detailTable.getTableID(), "", workFieldID)) {
							detailColumnList.add(new XF310_DetailColumn(detailTable.getTableID(), "", workFieldID, this));
						}
					}
				}
				
				//////////////////////////////////////////
				// Add order-By fields as HIDDEN column //
				//////////////////////////////////////////
				for (int j = 0; j < detailTable.getOrderByFieldIDList().size(); j++) {
					workStr = detailTable.getOrderByFieldIDList().get(j).replace("(D)", "");
					workStr = workStr.replace("(A)", "");
					workTokenizer = new StringTokenizer(workStr, "." );
					workAlias = workTokenizer.nextToken();
					workTableID = getTableIDOfTableAlias(workAlias);
					workFieldID = workTokenizer.nextToken();
					if (!containsDetailField(workTableID, workAlias, workFieldID)) {
						detailColumnList.add(new XF310_DetailColumn(workTableID, workAlias, workFieldID, this));
					}
				}
				
				//////////////////////////////////////////////////////////////
				// Analyze fields in scripts and add them as HIDDEN columns //
				//////////////////////////////////////////////////////////////
				for (int j = 0; j < detailTable.getScriptList().size(); j++) {
					if	(detailTable.getScriptList().get(j).isToBeRunAtEvent("BC", "")
					|| detailTable.getScriptList().get(j).isToBeRunAtEvent("AC", "")
					|| detailTable.getScriptList().get(j).isToBeRunAtEvent("BR", "")
					|| detailTable.getScriptList().get(j).isToBeRunAtEvent("AR", "")
					|| detailTable.getScriptList().get(j).isToBeRunAtEvent("BU", "")
					|| detailTable.getScriptList().get(j).isToBeRunAtEvent("AU", "")
					|| detailTable.getScriptList().get(j).isToBeRunAtEvent("BD", "")
					|| detailTable.getScriptList().get(j).isToBeRunAtEvent("AD", "")) {
						for (int k = 0; k < detailTable.getScriptList().get(j).getFieldList().size(); k++) {
							workTokenizer = new StringTokenizer(detailTable.getScriptList().get(j).getFieldList().get(k), "." );
							workAlias = workTokenizer.nextToken();
							workTableID = getTableIDOfTableAlias(workAlias);
							workFieldID = workTokenizer.nextToken();
							if (!containsDetailField(workTableID, workAlias, workFieldID)) {
								workElement = session_.getFieldElement(workTableID, workFieldID);
								if (workElement == null) {
									String msg = XFUtility.RESOURCE.getString("FunctionError1") + detailTable.getTableID() + XFUtility.RESOURCE.getString("FunctionError2") + detailTable.getScriptList().get(j).getName() + XFUtility.RESOURCE.getString("FunctionError3") + workAlias + "_" + workFieldID + XFUtility.RESOURCE.getString("FunctionError4");
									JOptionPane.showMessageDialog(null, msg);
									throw new Exception(msg);
								} else {
									if (detailTable.isValidDataSource(workTableID, workAlias, workFieldID)) {
										detailColumnList.add(new XF310_DetailColumn(workTableID, workAlias, workFieldID, this));
									}
								}
							}
						}
					}
				}
				
				////////////////////////////////////////
				// Analyze detail refer tables and    //
				// add their fields as HIDDEN columns //
				////////////////////////////////////////
				for (int j = detailReferTableList.size()-1; j > -1; j--) {
					for (int k = 0; k < detailReferTableList.get(j).getFieldIDList().size(); k++) {
						if (containsDetailField(detailReferTableList.get(j).getTableID(), detailReferTableList.get(j).getTableAlias(), detailReferTableList.get(j).getFieldIDList().get(k))) {
							detailReferTableList.get(j).setToBeExecuted(true);
							break;
						}
					}
					if (!detailReferTableList.get(j).isToBeExecuted()) {
						for (int k = 0; k < detailReferTableList.get(j).getWithKeyFieldIDList().size(); k++) {
							workTokenizer = new StringTokenizer(detailReferTableList.get(j).getWithKeyFieldIDList().get(k), "." );
							workAlias = workTokenizer.nextToken();
							workFieldID = workTokenizer.nextToken();
							if (workAlias.equals(detailTable.getTableID()) && containsDetailField(detailTable.getTableID(), "", workFieldID)) {
								detailReferTableList.get(j).setToBeExecuted(true);
								break;
							}
						}
					}
					if (detailReferTableList.get(j).isToBeExecuted()) {
						for (int k = 0; k < detailReferTableList.get(j).getFieldIDList().size(); k++) {
							if (!containsDetailField(detailReferTableList.get(j).getTableID(), detailReferTableList.get(j).getTableAlias(), detailReferTableList.get(j).getFieldIDList().get(k))) {
								detailColumnList.add(new XF310_DetailColumn(detailReferTableList.get(j).getTableID(), detailReferTableList.get(j).getTableAlias(), detailReferTableList.get(j).getFieldIDList().get(k), this));
							}
						}
						for (int k = 0; k < detailReferTableList.get(j).getWithKeyFieldIDList().size(); k++) {
							workTokenizer = new StringTokenizer(detailReferTableList.get(j).getWithKeyFieldIDList().get(k), "." );
							workAlias = workTokenizer.nextToken();
							workTableID = getTableIDOfTableAlias(workAlias);
							workFieldID = workTokenizer.nextToken();
							if (!containsDetailField(workTableID, workAlias, workFieldID)) {
								detailColumnList.add(new XF310_DetailColumn(workTableID, workAlias, workFieldID, this));
							}
						}
					}
				}
				
				////////////////////////////////////////////
				// Analyze if adding blank row is allowed //
				////////////////////////////////////////////
				if ((headerTable.getKeyFieldIDList().size() + 1) == detailTable.getKeyFieldIDList().size() && !detailTable.getDetailRowNoID().equals("")) {
					isAbleToSetupKeysOfNewRecord = true;
				} else {
					isAbleToSetupKeysOfNewRecord = false;
				}

				/////////////////////////////////////
				// Setup Function Keys and Buttons //
				/////////////////////////////////////
				setupFunctionKeysAndButtons();

				///////////////////////////////
				// Setup Add-Row-List Dialog //
				///////////////////////////////
				if (functionElement_.getAttribute("AddRowListTable").equals("")) {
					addRowListDialog = null;
				} else {
					addRowListDialog = new XF310_AddRowList(this);
					if (addRowListDialog.isInvalid()) {
						throw new Exception();
					}
				}

				////////////////////////////////////////////////
				// Select Detail Records and Setup Table rows //
				////////////////////////////////////////////////
				selectDetailRecordsAndSetupTableRows();

				////////////////////////
				// Setup referChecker //
				////////////////////////
				XF310_ReferCheckerConstructor constructor = new XF310_ReferCheckerConstructor(this);
		        threadToSetupReferChecker = new Thread(constructor);
		        threadToSetupReferChecker.start();

				////////////////////////////////////
				// Initialize errors and messages //
				////////////////////////////////////
				for (int i = 0; i < headerFieldList.size(); i++) {
					headerFieldList.get(i).setError(false);
				}
				if (parmMap_.containsKey("INITIAL_MESSAGE")) {
					jTextAreaMessages.setText((String)parmMap_.get("INITIAL_MESSAGE"));
					parmMap_.remove("INITIAL_MESSAGE");
				} else {
					if (initialMsg.equals("")) {
						jTextAreaMessages.setText(XFUtility.RESOURCE.getString("FunctionMessage7") + buttonUpdateCaption + XFUtility.RESOURCE.getString("FunctionMessage8"));
					} else {
						jTextAreaMessages.setText(initialMsg);
					}
				}
				
				//////////////////////////////////////////
				// Set focus on top focusable component //
				//////////////////////////////////////////
				if (firstEditableHeaderField == null) {
					jScrollPaneTable.setFocusable(false);
					if (jTableMain.getRowCount() > 0) {
						jTableMain.editCellAt(0, 0);
						cellsEditor.transferFocusOfCell(0, 0, true);
					}
				} else {
					jScrollPaneTable.setFocusable(true);
					firstEditableHeaderField.requestFocus();
				}
			}

		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("FunctionError5"));
			e.printStackTrace(exceptionStream);
			setErrorAndCloseFunction();
		} finally {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}

		////////////////
		// Show Panel //
		////////////////
		this.setVisible(true);

		///////////////////////////////
		// Release instance and exit //
		///////////////////////////////
		return returnMap_;
	}

	public boolean isAvailable() {
		return instanceIsAvailable;
	}
	
	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			closeFunction();
		}
	}

	void setErrorAndCloseFunction() {
		isToBeCanceled = true;
		returnMap_.put("RETURN_CODE", "99");
		closeFunction();
	}

	void closeFunction() {
		try {
			if (threadToSetupReferChecker != null) {
				threadToSetupReferChecker.join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		instanceIsAvailable = true;
		if (anyRecordsDeleted && !returnMap_.get("RETURN_CODE").toString().equals("99")) {
			returnMap_.put("RETURN_CODE", "20");
		}
		String errorLog = "";
		if (exceptionLog.size() > 0 || !exceptionHeader.equals("")) {
			errorLog = exceptionHeader + exceptionLog.toString();
		}
		session_.writeLogOfFunctionClosed(programSequence, returnMap_.get("RETURN_CODE").toString(), processLog.toString(), errorLog);
		this.setVisible(false);
	}
	
	public void cancelWithMessage(String message) {
		if (!message.equals("")) {
			JOptionPane.showMessageDialog(null, message);
		}
		returnMap_.put("RETURN_CODE", "21");
		isToBeCanceled = true;
	}
	
	public void cancelWithScriptException(ScriptException e, String scriptName) {
		JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("FunctionError7") + scriptName + XFUtility.RESOURCE.getString("FunctionError8"));
		exceptionHeader = "'" + scriptName + "' Script error\n";
		e.printStackTrace(exceptionStream);
		this.rollback();
		setErrorAndCloseFunction();
	}
	
	public void cancelWithException(Exception e) {
		JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("FunctionError5") + "\n" + e.getMessage());
		e.printStackTrace(exceptionStream);
		this.rollback();
		setErrorAndCloseFunction();
	}
	
	public void callFunction(String functionID) {
		try {
			returnMap_ = session_.executeFunction(functionID, parmMap_);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
			exceptionHeader = e.getMessage();
			setErrorAndCloseFunction();
		}
	}
	
	public void setHeaderReferChecker(ReferChecker checker) {
		headerReferChecker = checker;
	}
	
	public void setDetailReferChecker(ReferChecker checker) {
		detailReferChecker = checker;
	}
	
	public void startProgress(int maxValue) {
		jProgressBar.setMaximum(maxValue);
		jProgressBar.setValue(0);
		jPanelBottom.remove(jPanelInfo);
		jProgressBar.setPreferredSize(jPanelInfo.getPreferredSize());
		jPanelBottom.add(jProgressBar, BorderLayout.EAST);
		this.pack();
	}
	
	public void incrementProgress() {
		jProgressBar.setValue(jProgressBar.getValue() + 1);
		jProgressBar.paintImmediately(0,0,jProgressBar.getWidth(), jProgressBar.getHeight());
		if (jProgressBar.getValue() >= jProgressBar.getMaximum()) {
			jPanelBottom.remove(jProgressBar);
			jPanelBottom.add(jPanelInfo, BorderLayout.EAST);
			this.pack();
			jPanelBottom.repaint();
		}
	}
	
	public void commit() {
		session_.commit(true, processLog);
	}
	
	public void rollback() {
		session_.commit(false, processLog);
	}

	void fetchHeaderRecord() {
		try {
			boolean recordNotFound = true;
			String inputDialogMessage = "";
			boolean keyInputRequired = false;
			for (int i = 0; i < headerFieldList.size(); i++) {
				if (headerFieldList.get(i).isKey()) {
					if (parmMap_.containsKey(headerFieldList.get(i).getFieldID())) {
						if (parmMap_.get(headerFieldList.get(i).getFieldID()).equals(headerFieldList.get(i).getNullValue())) {
							keyInputRequired = true;
						}
					} else {
						keyInputRequired = true;
					}
				} else {
					headerFieldList.get(i).setValue(headerFieldList.get(i).getNullValue());
				}
			}

			while (recordNotFound) {
				if (keyInputRequired) {
					if (keyInputDialog == null) {
						keyInputDialog = new XF310_KeyInputDialog(this);
					}
					parmMap_ = keyInputDialog.requestKeyValues(inputDialogMessage);
					if (parmMap_.size() == 0) {
						isToBeCanceled = true;
						returnMap_.put("RETURN_CODE", "01");
						closeFunction();
						return;
					}
					returnMap_.putAll(parmMap_);
				}

				////////////////////////////////////////////
				// Run Header-table-script for BeforeRead //
				////////////////////////////////////////////
				headerTable.runScript("BR", "");

				XFTableOperator operator = createTableOperator(headerTable.getSQLToSelect());
				if (operator.next()) {
					recordNotFound = false;
					for (int i = 0; i < headerFieldList.size(); i++) {
						if (headerFieldList.get(i).getTableID().equals(headerTable.getTableID())) {
							headerFieldList.get(i).setValueOfResultSet(operator);
						}
					}
					headerTable.setUpdateCounterValue(operator);
				} else {
					if (keyInputRequired) {
						inputDialogMessage = XFUtility.RESOURCE.getString("FunctionError37");
					} else {
						JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("FunctionError38"));
						isToBeCanceled = true;
						returnMap_.put("RETURN_CODE", "01");
						closeFunction();
						return;
					}
				}
			}

			//////////////////////////////////////////////////////////
			// Fetch Header-refer-tables and                        //
			// Run Header-table-script for AfterRead & BeforeUpdate //
			//////////////////////////////////////////////////////////
			fetchHeaderReferRecords("AR,BU", false, "");

			headerFieldOldValueMap.clear();
			for (int i = 0; i < headerFieldList.size(); i++) {
				if (headerFieldList.get(i).getTableID().equals(headerTable.getTableID())) {
					headerFieldOldValueMap.put(headerFieldList.get(i).getFieldID(), headerFieldList.get(i).getInternalValue());
				}
			}

		} catch(ScriptException e) {
			cancelWithScriptException(e, this.getScriptNameRunning());
		} catch (Exception e) {
			cancelWithException(e);
		}
	}
	
	int fetchHeaderReferRecords(String events, boolean toBeChecked, String specificReferTable) {
		int countOfErrors = 0;
		boolean recordNotFound;
		XFTableOperator operator;

		try {
			////////////////////////////////////////////
			// Run Script for BeforeRead-refer-tables //
			////////////////////////////////////////////
			countOfErrors = countOfErrors + headerTable.runScript(events, "BR()");

			for (int i = 0; i < headerReferTableList.size(); i++) {
				if (specificReferTable.equals("") || specificReferTable.equals(headerReferTableList.get(i).getTableAlias())) {
					if (headerReferTableList.get(i).isToBeExecuted()) {

						////////////////////////////////////////////////////
						// Run Script for BeforeRead-specific-refer-table //
						////////////////////////////////////////////////////
						countOfErrors = countOfErrors + headerTable.runScript(events, "BR(" + headerReferTableList.get(i).getTableAlias() + ")");

						if (!headerReferTableList.get(i).isKeyNullable() || !headerReferTableList.get(i).isKeyNull()) {
							recordNotFound = true;

							//////////////////////////////
							// Fetch refer-table record //
							//////////////////////////////
							operator = createTableOperator(headerReferTableList.get(i).getSelectSQL(false));
							while (operator.next()) {
								if (headerReferTableList.get(i).isRecordToBeSelected(operator)) {

									recordNotFound = false;
									for (int j = 0; j < headerFieldList.size(); j++) {
										if (headerFieldList.get(j).getTableAlias().equals(headerReferTableList.get(i).getTableAlias())) {
											headerFieldList.get(j).setValueOfResultSet(operator);
										}
									}

									///////////////////////////////////////////////////
									// Run Script for AfterRead-specific-refer-table //
									///////////////////////////////////////////////////
									countOfErrors = countOfErrors + headerTable.runScript(events, "AR(" + headerReferTableList.get(i).getTableAlias() + ")");
								}
							}

							if (recordNotFound && toBeChecked && !headerReferTableList.get(i).isOptional()) {
								countOfErrors++;
								headerReferTableList.get(i).setErrorOnRelatedFields();
							}
						}
					}
				}
			}

			///////////////////////////////////////////////
			// Run Script for AfterRead-all-refer-tables //
			///////////////////////////////////////////////
			countOfErrors = countOfErrors + headerTable.runScript(events, "AR()");

			//////////////////////////////////////////////////
			// Check if prompt-key is EditControlled or not //
			//////////////////////////////////////////////////
			for (int i = 0; i < headerFieldList.size(); i++) {
				headerFieldList.get(i).checkPromptKeyEdit();
			}

		} catch(ScriptException e) {
			cancelWithScriptException(e, this.getScriptNameRunning());
		} catch (Exception e) {
			cancelWithException(e);
		}

		return countOfErrors;
	}
	
	int fetchDetailReferRecords(String events, boolean toBeChecked, String specificReferTable, HashMap<String, Object> columnValueMap, HashMap<String, Object> columnOldValueMap) {
		int countOfErrors = 0;
		boolean recordNotFound;
		XFTableOperator operator;
		String sql;

		try {
			////////////////////////////////////////////
			// Run Script for BeforeRead-refer-tables //
			////////////////////////////////////////////
			countOfErrors = countOfErrors + detailTable.runScript(events, "BR()", columnValueMap, columnOldValueMap); /* Script to be run AFTER READ primary table */

			for (int i = 0; i < detailReferTableList.size(); i++) {
				if (specificReferTable.equals("") || specificReferTable.equals(detailReferTableList.get(i).getTableAlias())) {
					if (detailReferTableList.get(i).isToBeExecuted()) {

						////////////////////////////////////////////////////
						// Run Script for BeforeRead-specific-refer-table //
						////////////////////////////////////////////////////
						countOfErrors = countOfErrors + detailTable.runScript(events, "BR(" + detailReferTableList.get(i).getTableAlias() + ")", columnValueMap, columnOldValueMap); /* Script to be run BEFORE READ */

						if (!detailReferTableList.get(i).isKeyNullable() || !detailReferTableList.get(i).isKeyNull()) {
							recordNotFound = true;

							//////////////////////////////
							// Fetch refer-table record //
							//////////////////////////////
							sql = detailReferTableList.get(i).getSelectSQL(false);
							if (!sql.equals("")) {
								operator = createTableOperator(sql);
								while (operator.next()) {
									if (detailReferTableList.get(i).isRecordToBeSelected(operator)) {

										recordNotFound = false;
										for (int j = 0; j < detailColumnList.size(); j++) {
											if (detailColumnList.get(j).getTableAlias().equals(detailReferTableList.get(i).getTableAlias())) {
												detailColumnList.get(j).setValueOfResultSet(operator);
												columnValueMap.put(detailColumnList.get(j).getDataSourceName(), detailColumnList.get(j).getInternalValue());
											}
										}

										///////////////////////////////////////////////////
										// Run Script for AfterRead-specific-refer-table //
										///////////////////////////////////////////////////
										countOfErrors = countOfErrors + detailTable.runScript(events, "AR(" + detailReferTableList.get(i).getTableAlias() + ")", columnValueMap, columnOldValueMap); /* Script to be run AFTER READ */
									}
								}
							}

							if (recordNotFound && toBeChecked && !detailReferTableList.get(i).isOptional()) {
								countOfErrors++;
								detailReferTableList.get(i).setErrorOnRelatedFields();
							}
						}
					}
				}
			}

			///////////////////////////////////////////////
			// Run Script for AfterRead-all-refer-tables //
			///////////////////////////////////////////////
			countOfErrors = countOfErrors + detailTable.runScript(events, "AR()", columnValueMap, columnOldValueMap); /* Script to be run AFTER READ */

			//////////////////////////////////////////////////
			// Check if prompt-key is EditControlled or not //
			//////////////////////////////////////////////////
			for (int i = 0; i < detailColumnList.size(); i++) {
				detailColumnList.get(i).checkPromptKeyEdit();
			}

		} catch(ScriptException e) {
			cancelWithScriptException(e, this.getScriptNameRunning());
		} catch (Exception e) {
			cancelWithException(e);
		}

		return countOfErrors;
	}

	void setupFunctionKeysAndButtons() {
		InputMap inputMapHeaderFields  = jPanelHeaderFields.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		ActionMap actionMapHeaderFields = jPanelHeaderFields.getActionMap();
		InputMap inputMapTableMain  = jTableMain.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		ActionMap actionMapTableMain = jTableMain.getActionMap();

		inputMapHeaderFields.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "CHECK");
		actionMapHeaderFields.put("CHECK", checkAction);
		inputMapHeaderFields.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "HELP");
		actionMapHeaderFields.put("HELP", helpAction);
		actionMapHeaderFields.put(inputMapHeaderFields.get(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0)), tabAction);
		actionMapHeaderFields.put(inputMapHeaderFields.get(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, Event.SHIFT_MASK)), shiftTabAction);
		actionMapHeaderFields.put(inputMapHeaderFields.get(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0)), arrowUpAction);
		actionMapHeaderFields.put(inputMapHeaderFields.get(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0)), arrowDownAction);

		inputMapTableMain.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "CHECK");
		actionMapTableMain.put("CHECK", checkAction);
		inputMapTableMain.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "HELP");
		actionMapTableMain.put("HELP", helpAction);
		actionMapTableMain.put(inputMapTableMain.get(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0)), tabAction);
		actionMapTableMain.put(inputMapTableMain.get(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, Event.SHIFT_MASK)), shiftTabAction);
		actionMapTableMain.put(inputMapTableMain.get(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0)), arrowUpAction);
		actionMapTableMain.put(inputMapTableMain.get(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0)), arrowDownAction);

		for (int i = 0; i < 7; i++) {
			jButtonArray[i].setText("");
			jButtonArray[i].setVisible(false);
			actionDefinitionArray[i] = "";
		}

		int workIndex;
		org.w3c.dom.Element element;
		NodeList buttonList = functionElement_.getElementsByTagName("Button");
		for (int i = 0; i < buttonList.getLength(); i++) {
			element = (org.w3c.dom.Element)buttonList.item(i);
			workIndex = Integer.parseInt(element.getAttribute("Position"));
			actionDefinitionArray[workIndex] = element.getAttribute("Action");
			XFUtility.setCaptionToButton(jButtonArray[workIndex], element, "");
			jButtonArray[workIndex].setVisible(true);

			inputMapHeaderFields.put(XFUtility.getKeyStroke(element.getAttribute("Number")), "actionButton" + workIndex);
			actionMapHeaderFields.put("actionButton" + workIndex, actionButtonArray[workIndex]);

			inputMapTableMain.put(XFUtility.getKeyStroke(element.getAttribute("Number")), "actionButton" + workIndex);
			actionMapTableMain.put("actionButton" + workIndex, actionButtonArray[workIndex]);

			if (element.getAttribute("Action").equals("ADD_ROW")) {
				addRowListTitle = element.getAttribute("Caption");
			}
			if (element.getAttribute("Action").equals("UPDATE")) {
				buttonUpdateCaption = element.getAttribute("Caption");
				keyStrokeToUpdate = XFUtility.getKeyStroke(element.getAttribute("Number"));
			}
		}
	}
	
	int getKeyCodeToUpdate() {
		return keyStrokeToUpdate.getKeyCode();
	}

	void selectDetailRecordsAndSetupTableRows() {
		HashMap<String, Object> keyValueMap, columnValueMap;
		HashMap<String, Boolean> columnIsEditableMap;
		ArrayList<Object> orderByValueList;
		String workStr;

		try {
			int rowCount = tableModelMain.getRowCount();
			for (int i = 0; i < rowCount; i++) {
				tableModelMain.removeRow(0);
			}
			tableRowList.clear();
			int countOfRows = 0;

			/////////////////////////////////
			// Select Detail-table records //
			/////////////////////////////////
			XFTableOperator operator = createTableOperator(detailTable.getSQLToSelect());
			while (operator.next()) {

				for (int i = 0; i < detailColumnList.size(); i++) {
					detailColumnList.get(i).initValue();
				}

				////////////////////////////////////////////
				// Run Detail-table-script for BeforeRead //
				////////////////////////////////////////////
				detailTable.runScript("BR", "", null, null);
				
				for (int i = 0; i < detailColumnList.size(); i++) {
					if (detailColumnList.get(i).getTableID().equals(detailTable.getTableID())) {
						detailColumnList.get(i).setValueOfResultSet(operator);
					}
				}

				keyValueMap = new HashMap<String, Object>();
				columnValueMap = new HashMap<String, Object>();
				for (int i = 0; i < detailColumnList.size(); i++) {
					columnValueMap.put(detailColumnList.get(i).getDataSourceName(), detailColumnList.get(i).getInternalValue());
					if (detailColumnList.get(i).isKey()) {
						keyValueMap.put(detailColumnList.get(i).getFieldID(), detailColumnList.get(i).getInternalValue());
					}
				}
				columnValueMap.put(detailTable.getUpdateCounterID(), Long.parseLong(operator.getValueOf(detailTable.getUpdateCounterID()).toString()));

				//////////////////////////////////////////////////////////
				// Fetch Detail-refer-tables and                        //
				// Run Detail-table-script for AfterRead & BeforeUpdate //
				//////////////////////////////////////////////////////////
				fetchDetailReferRecords("AR,BU", false, "", columnValueMap, null);

				if (detailTable.hasOrderByAsItsOwnFields()) {
					Object[] cell = new Object[1];
					cell[0] = new XF310_DetailRowNumber(countOfRows + 1, "CURRENT", keyValueMap, columnValueMap, this);
					tableModelMain.addRow(cell);
				} else {
					columnIsEditableMap = new HashMap<String, Boolean>();
					for (int i = 0; i < detailColumnList.size(); i++) {
						columnIsEditableMap.put(detailColumnList.get(i).getDataSourceName(), detailColumnList.get(i).isEditable());
					}
					orderByValueList = new ArrayList<Object>();
					for (int i = 0; i < detailTable.getOrderByFieldIDList().size(); i++) {
						workStr = detailTable.getOrderByFieldIDList().get(i).replace("(D)", "");
						workStr = workStr.replace("(A)", "");
						for (int j = 0; j < detailColumnList.size(); j++) {
							if (detailColumnList.get(j).getDataSourceName().equals(workStr)) {
								orderByValueList.add(detailColumnList.get(j).getExternalValue());
								break;
							}
						}
					}
					tableRowList.add(new WorkingRow(keyValueMap, columnValueMap, orderByValueList, columnIsEditableMap));
				}

				countOfRows++;
			}

			////////////////////////////////////////////////
			// Run Header-table-script for AR(After Read) //
			//                      and BU(Before Update) //
			//                      and AS(after Summary) //
			////////////////////////////////////////////////
			headerTable.runScript("AR,BU", "AS()");

			if (!detailTable.hasOrderByAsItsOwnFields()) {
				WorkingRow[] workingRowArray = tableRowList.toArray(new WorkingRow[0]);
				Arrays.sort(workingRowArray, new WorkingRowComparator());
				for (int i = 0; i < workingRowArray.length; i++) {
					Object[] cell = new Object[1];
					cell[0] = new XF310_DetailRowNumber(i + 1, "CURRENT", workingRowArray[i].getKeyValueMap(), workingRowArray[i].getColumnValueMap(), this);
					tableModelMain.addRow(cell);
				}
			}

		} catch(ScriptException e) {
			cancelWithScriptException(e, this.getScriptNameRunning());
		} catch (Exception e) {
			cancelWithException(e);
		}
	}

	void checkErrorsToUpdate(boolean isCheckOnly, boolean isCellsEditorUpdateRequired) {
		XF310_DetailRowNumber tableRowNumber;
		XFTableOperator operator;
		int recordCount;

		try {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));

			if (isCellsEditorUpdateRequired) {
				cellsEditor.stopCellEditing();
			}

			int countOfErrors = 0;
			headerFieldValueMap.clear();
			for (int i = 0; i < headerFieldList.size(); i++) {
				headerFieldList.get(i).setError(false);
				if (headerFieldList.get(i).getTableID().equals(headerTable.getTableID())) {
					headerFieldValueMap.put(headerFieldList.get(i).getFieldID(), headerFieldList.get(i).getInternalValue());
				}
			}

			countOfErrors = fetchHeaderReferRecords("BU", true, "");

			for (int i = 0; i < headerFieldList.size(); i++) {
				if (headerFieldList.get(i).isFieldOnPrimaryTable()) {
					if (headerFieldList.get(i).isNullError()) {
						countOfErrors++;
					}
				}
			}

			for (int i = 0; i < jTableMain.getRowCount(); i++) {
				tableRowNumber = (XF310_DetailRowNumber)tableModelMain.getValueAt(i, 0);
				tableRowNumber.setValuesToDetailColumns();
				countOfErrors = countOfErrors + tableRowNumber.countErrors(messageList);
			}

			countOfErrors = countOfErrors + headerTable.runScript("BU", "AS()"); /* Script to be run BEFORE UPDATE and AFTER SUMMARY */

			if (countOfErrors == 0) {
				if (hasNoErrorWithKey(isCheckOnly)) {
					if (isCheckOnly) {
						messageList.add(XFUtility.RESOURCE.getString("FunctionMessage9"));
					} else {

						///////////////////////////////////////////
						// Delete detail records of removed rows //
						///////////////////////////////////////////
						for (int i = 0; i < deleteRowNumberList.size(); i++) {
							tableRowNumber = (XF310_DetailRowNumber)deleteRowNumberList.get(i);
							tableRowNumber.setValuesToDetailColumns();
							ArrayList<String> errorMsgList = detailReferChecker.getOperationErrors("DELETE", tableRowNumber.getColumnValueMapWithFieldID(), null);
							if (errorMsgList.size() > 0) {
								StringBuffer buf = new StringBuffer();
								for (int j = 0; j < errorMsgList.size(); j++) {
									if (j > 0) {
										buf.append("\n");
									}
									buf.append(errorMsgList.get(j));
								}
								JOptionPane.showMessageDialog(jPanelMain, buf.toString());
								exceptionHeader = buf.toString();
								this.rollback();
								setErrorAndCloseFunction();
							}

							operator = createTableOperator(detailTable.getSQLToDelete(deleteRowNumberList.get(i).getKeyValueMap(), (Long)deleteRowNumberList.get(i).getColumnValueMap().get(detailTable.getUpdateCounterID())));
							recordCount = operator.execute();
							if (recordCount == 1) {
								detailTable.runScript("AD", "", deleteRowNumberList.get(i).getColumnValueMap(), deleteRowNumberList.get(i).getColumnOldValueMap());
								anyRecordsDeleted = true;
							} else {
								String errorMessage = XFUtility.RESOURCE.getString("FunctionError33");
								JOptionPane.showMessageDialog(jPanelMain, errorMessage);
								exceptionHeader = errorMessage.replace("\n", " ");
								this.rollback();
								setErrorAndCloseFunction();
							}
						}

						//////////////////////////////
						// Update the header record //
						//////////////////////////////
						operator = createTableOperator(headerTable.getSQLToUpdate());
						recordCount = operator.execute();
						if (recordCount == 1) {
							headerTable.runScript("AU", "");

							//////////////////////////////////////
							// Update/Insert the detail records //
							//////////////////////////////////////
							for (int i = 0; i < jTableMain.getRowCount(); i++) {
								int rowNumber = jTableMain.convertRowIndexToModel(i);
								if (rowNumber > -1) {
									tableRowNumber = (XF310_DetailRowNumber)tableModelMain.getValueAt(rowNumber,0);
									tableRowNumber.setValuesToDetailColumns();
									if (tableRowNumber.getRecordType().equals("CURRENT")) {
										operator = createTableOperator(detailTable.getSQLToUpdate(tableRowNumber));
										recordCount = operator.execute();
										if (recordCount == 1) {
											detailTable.runScript("AU", "", tableRowNumber.getColumnValueMap(), tableRowNumber.getColumnOldValueMap());
										} else {
											String errorMessage = XFUtility.RESOURCE.getString("FunctionError19");
											JOptionPane.showMessageDialog(jPanelMain, errorMessage);
											exceptionHeader = errorMessage.replace("\n", " ");
											this.rollback();
											setErrorAndCloseFunction();
										}
									}
									if (tableRowNumber.getRecordType().equals("NEW")) {
										operator = createTableOperator(detailTable.getSQLToInsert(tableRowNumber));
										recordCount = operator.execute();
										if (recordCount == 1) {
											detailTable.runScript("AC", "", tableRowNumber.getColumnValueMap(), tableRowNumber.getColumnOldValueMap());
										} else {
											String errorMessage = XFUtility.RESOURCE.getString("FunctionError50");
											JOptionPane.showMessageDialog(jPanelMain, errorMessage);
											exceptionHeader = errorMessage;
											this.rollback();
											setErrorAndCloseFunction();
										}
									}
								}
							}

							returnMap_.put("RETURN_CODE", "20");

						} else {
							String errorMessage = XFUtility.RESOURCE.getString("FunctionError19");
							JOptionPane.showMessageDialog(jPanelMain, errorMessage);
							exceptionHeader = errorMessage.replace("\n", " ");
							this.rollback();
							setErrorAndCloseFunction();
						}
						closeFunction();
					}
				}
			}

			if (this.isToBeCanceled) {
				this.rollback();
				closeFunction();
			} else {
				this.commit();
			}

		} catch(ScriptException e) {
			cancelWithScriptException(e, this.getScriptNameRunning());
		} catch (Exception e) {
			cancelWithException(e);
		} finally {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}

	boolean hasNoErrorWithKey(boolean isCheckOnly) {
		boolean hasNoError = true;
		ArrayList<String> uniqueKeyList = new ArrayList<String>();
		ArrayList<String> keyFieldList = new ArrayList<String>();
		ArrayList<String> errorMsgList = new ArrayList<String>();
		StringTokenizer workTokenizer;
		XFTableOperator operator;
		String sql = "";

		try {
			if (headerTable.hasPrimaryKeyValueAltered()) {
				hasNoError = false;
				messageList.add(XFUtility.RESOURCE.getString("FunctionError43"));
				for (int i = 0; i < headerFieldList.size(); i++) {
					if (headerFieldList.get(i).isKey()) {
						headerFieldList.get(i).setError(true);
					}
				}
			}

			if (hasNoError) {
				uniqueKeyList = headerTable.getUniqueKeyList();
				for (int i = 0; i < uniqueKeyList.size(); i++) {
					keyFieldList.clear();
					workTokenizer = new StringTokenizer(uniqueKeyList.get(i), ";" );
					while (workTokenizer.hasMoreTokens()) {
						keyFieldList.add(workTokenizer.nextToken());
					}
					operator = createTableOperator(headerTable.getSQLToCheckSKDuplication(keyFieldList));
					if (operator.next()) {
						hasNoError = false;
						messageList.add(XFUtility.RESOURCE.getString("FunctionError22"));
						for (int j = 0; j < headerFieldList.size(); j++) {
							if (keyFieldList.contains(headerFieldList.get(j).getFieldID())) {
								headerFieldList.get(j).setError(true);
							}
						}
					}
				}
			}

			if (hasNoError && !isCheckOnly) {
				threadToSetupReferChecker.join();
				errorMsgList = headerReferChecker.getOperationErrors("UPDATE", headerFieldValueMap, headerFieldOldValueMap, detailTable.getTableID());
				for (int i = 0; i < errorMsgList.size(); i++) {
					hasNoError = false;
					messageList.add(errorMsgList.get(i));
				}
			}

			uniqueKeyList = detailTable.getUniqueKeyList();
			int rowNumber;
			for (int j = 0; j < jTableMain.getRowCount(); j++) {
				XF310_DetailRowNumber tableRowNumber = (XF310_DetailRowNumber)tableModelMain.getValueAt(j, 0);
				rowNumber = tableRowNumber.getRowIndex() + 1;

				if (tableRowNumber.getRecordType().equals("CURRENT")) {
					if (detailTable.hasPrimaryKeyValueAltered(tableRowNumber)) {
						hasNoError = false;
						messageList.add(XFUtility.RESOURCE.getString("FunctionError26") + rowNumber + XFUtility.RESOURCE.getString("FunctionError27"));
					}
				}

				if (hasNoError) {
					for (int i = 0; i < uniqueKeyList.size(); i++) {
						keyFieldList.clear();
						workTokenizer = new StringTokenizer(uniqueKeyList.get(i), ";" );
						while (workTokenizer.hasMoreTokens()) {
							keyFieldList.add(workTokenizer.nextToken());
						}
						if (tableRowNumber.getRecordType().equals("CURRENT")) {
							sql = detailTable.getSQLToCheckSKDuplication(tableRowNumber, keyFieldList, true);
						} else {
							sql = detailTable.getSQLToCheckSKDuplication(tableRowNumber, keyFieldList, false);
						}
						operator = createTableOperator(sql);
						if (operator.next()) {
							hasNoError = false;
							messageList.add(XFUtility.RESOURCE.getString("FunctionError28") + rowNumber + XFUtility.RESOURCE.getString("FunctionError29"));
						}
						if (!hasNoError) {
							break;
						}
					}
				} else {
					break;
				}

				if (hasNoError && !isCheckOnly) {
					if (tableRowNumber.getRecordType().equals("CURRENT")) {
						errorMsgList = detailReferChecker.getOperationErrors("UPDATE", tableRowNumber.getColumnValueMapWithFieldID(), tableRowNumber.getColumnOldValueMapWithFieldID(), rowNumber);
					} else {
						errorMsgList = detailReferChecker.getOperationErrors("INSERT", tableRowNumber.getColumnValueMapWithFieldID(), null, rowNumber);
					}
					for (int i = 0; i < errorMsgList.size(); i++) {
						hasNoError = false;
						messageList.add(errorMsgList.get(i));
					}
				} else {
					break;
				}
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("FunctionError5") + "\n" + e.getMessage());
			e.printStackTrace(exceptionStream);
			setErrorAndCloseFunction();
		}

		return hasNoError;
	}
	
	boolean isAbleToSetupKeysOfNewRecord() {
		return isAbleToSetupKeysOfNewRecord;
	}
	
	void addRow() {
		try {
			threadToSetupReferChecker.join();

			////////////////////////////////
			//Get latest detail row number//
			////////////////////////////////
			if (!detailTable.getDetailRowNoID().equals("")) {
				detailRowNoLastValue = 0;
				for (int i = 0; i < jTableMain.getRowCount(); i++) {
					XF310_DetailRowNumber tableRowNumber = (XF310_DetailRowNumber)tableModelMain.getValueAt(i, 0);
					long rowNoValue = (Long)tableRowNumber.getKeyValueMap().get(detailTable.getDetailRowNoID());
					if (rowNoValue > detailRowNoLastValue) {
						detailRowNoLastValue = rowNoValue;
					}
				}
			}

			if (addRowListDialog == null) {
				if (isAbleToSetupKeysOfNewRecord) {
					setupNewRowAndAddToJTable(null);
				} else {
					messageList.add(XFUtility.RESOURCE.getString("FunctionError43"));
				}
			} else {
				ArrayList<XF310_AddRowListNumber> addRowListNumberList = addRowListDialog.getDefaultRow();
				if (addRowListNumberList == null) {
					int result = addRowListDialog.requestSelection();
					if (result == 0) {
						messageList.add(XFUtility.RESOURCE.getString("FunctionMessage33"));
					}
					if (result == 1) {
						addRowListNumberList = addRowListDialog.getSelectionList();
					}
					if (result == 2) {
						setupNewRowAndAddToJTable(null);
					}
					if (result == 3) {
						closeFunction();
					}
				}

				if (addRowListNumberList != null) {
					int countOfAdded = 0;
					for (int i = 0; i < addRowListNumberList.size(); i++) {
						countOfAdded = countOfAdded + setupNewRowAndAddToJTable(addRowListNumberList.get(i));
					}
					int countOfNotAdded = addRowListNumberList.size() - countOfAdded;
					if (countOfNotAdded > 0) {
						messageList.add(XFUtility.RESOURCE.getString("FunctionMessage34") + countOfNotAdded + XFUtility.RESOURCE.getString("FunctionMessage35"));
					}
				}
			}
			checkErrorsToUpdate(true, false);

		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("FunctionError5") + "\n" + e.getMessage());
			e.printStackTrace(exceptionStream);
			setErrorAndCloseFunction();
		}
	}
	
	int setupNewRowAndAddToJTable(XF310_AddRowListNumber rowNumber) {
		XF310_HeaderField headerField;
		XF310_DetailColumn detailColumn;
		int countOfAdded = 0;
		detailRowNoLastValue++;

		/////////////////////////////
		// Initialize field values //
		/////////////////////////////
//		for (int i = 0; i < detailColumnList.size(); i++) {
//			if (detailColumnList.get(i).getTableID().equals(detailTable.getTableID())
//					&& detailColumnList.get(i).getFieldID().equals(detailTable.getDetailRowNoID())) {
//				detailColumnList.get(i).setValue(detailRowNoLastValue);
//			} else {
//				if (detailColumnList.get(i).isAutoNumberField()) {
//					detailColumnList.get(i).setValue(detailColumnList.get(i).getAutoNumberValue());
//				} else {
//					detailColumnList.get(i).setValue(detailColumnList.get(i).getNullValue());
//				}
//			}
//		}
		for (int i = 0; i < detailColumnList.size(); i++) {
			if (detailColumnList.get(i).getTableID().equals(detailTable.getTableID())) {
				if (detailColumnList.get(i).getFieldID().equals(detailTable.getDetailRowNoID())) {
					detailColumnList.get(i).setValue(detailRowNoLastValue);
				} else {
					if (detailColumnList.get(i).isAutoNumberField()) {
						detailColumnList.get(i).setValue(detailColumnList.get(i).getAutoNumberValue());
					} else {
						detailColumnList.get(i).setValue(detailColumnList.get(i).getNullValue());
					}
				}
			} else {
				detailColumnList.get(i).setValue(detailColumnList.get(i).getNullValue());
			}
		}

		////////////////////////////////////////////////
		// Set values from Header Key or Add-Row-List //
		////////////////////////////////////////////////
		for (int i = 0; i < headerTable.getKeyFieldIDList().size(); i++) {
			headerField = getHeaderFieldObjectByID(headerTable.getTableID(), "", headerTable.getKeyFieldIDList().get(i));
			detailColumn = getDetailColumnObjectByID(detailTable.getTableID(), "", detailTable.getKeyFieldIDList().get(i));
			detailColumn.setValue(headerField.getInternalValue());
		}
		if (rowNumber != null) {
			for (int i = 0; i < detailColumnList.size(); i++) {
				if (rowNumber.getReturnFieldMap().containsKey(detailColumnList.get(i).getDataSourceName())) {
					detailColumnList.get(i).setValue(rowNumber.getReturnFieldMap().get(detailColumnList.get(i).getDataSourceName()));
				}
			}
		}

		/////////////////////////////////////////////////////////////////////////
		// Setup column-map(DataSourceName, Value) and key-map(FieldID, Value) //
		/////////////////////////////////////////////////////////////////////////
		HashMap<String, Object> columnValueMap = new HashMap<String, Object>();
		HashMap<String, Object> keyValueMap = new HashMap<String, Object>();
		for (int i = 0; i < detailColumnList.size(); i++) {
			columnValueMap.put(detailColumnList.get(i).getDataSourceName(), detailColumnList.get(i).getInternalValue());
			if (detailColumnList.get(i).isKey()) {
				keyValueMap.put(detailColumnList.get(i).getFieldID(), detailColumnList.get(i).getInternalValue());
			}
		}

		fetchDetailReferRecords("BC", false, "", columnValueMap, null);

		boolean duplicatedKey = false;
		int wrkInt;
		XF310_DetailRowNumber wrkRowNumber;
		for (int i = 0; i < jTableMain.getRowCount(); i++) {
			wrkInt = jTableMain.convertRowIndexToModel(i);
			if (wrkInt > -1) {
				wrkRowNumber = (XF310_DetailRowNumber)tableModelMain.getValueAt(wrkInt,0);
				if (wrkRowNumber.getKeyValueMap().equals(keyValueMap)) {
					duplicatedKey = true;
				}
			}
		}
		if (duplicatedKey) {
			countOfAdded = 0;
		} else {
			countOfAdded = 1;
			Object[] cell = new Object[1];
			cell[0] = new XF310_DetailRowNumber(tableModelMain.getRowCount() + 1, "NEW", keyValueMap, columnValueMap, this);
			tableModelMain.addRow(cell);
		}

		return countOfAdded;
	}
	
	void removeRow() {
		if (cellsEditor == null) {
			messageList.add(XFUtility.RESOURCE.getString("FunctionError44"));
		} else {
			if (cellsEditor.getActiveRowObject() == null) {
				messageList.add(XFUtility.RESOURCE.getString("FunctionError44"));
			} else {
				cellsEditor.stopCellEditing();

				try {
					threadToSetupReferChecker.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				XF310_DetailRowNumber rowNumber = (XF310_DetailRowNumber)tableModelMain.getValueAt(cellsEditor.getActiveRowObject().getRowIndex(), 0);
				Object[] bts = {XFUtility.RESOURCE.getString("Yes"), XFUtility.RESOURCE.getString("No")} ;
				int reply = JOptionPane.showOptionDialog(this, XFUtility.RESOURCE.getString("FunctionMessage36") + rowNumber.getRowNumberString() + XFUtility.RESOURCE.getString("FunctionMessage37"), XFUtility.RESOURCE.getString("CheckToDelete"), JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, bts, bts[0]);
				if (reply == 0) {
					try {
						setCursor(new Cursor(Cursor.WAIT_CURSOR));

						rowNumber.resetErrors();

						int countOfErrors = fetchDetailReferRecords("BD", false, "", rowNumber.getColumnValueMap(), rowNumber.getColumnOldValueMap());
						countOfErrors = countOfErrors + headerTable.runScript("BD", "AS()"); /* Script to be run AFTER SUMMARY */
						if (countOfErrors != 0)	{
							/////////////////////////
							// Set Errors on Cells //
							/////////////////////////
							for (int i = 0; i < detailColumnList.size(); i++) {
								if (detailColumnList.get(i).isVisibleOnPanel() && detailColumnList.get(i).isEditable()) {
									if (detailColumnList.get(i).isError()) {
										rowNumber.setErrorOnCellAt(i);
									}
								}
							}
						} else {
							if (rowNumber.getRecordType().equals("CURRENT")) {
								ArrayList<String> errorMsgList = detailReferChecker.getOperationErrors("DELETE", rowNumber.getColumnValueMapWithFieldID(), null);
								for (int i = 0; i < errorMsgList.size(); i++) {
									messageList.add(errorMsgList.get(i));
								}
								countOfErrors = errorMsgList.size();
							}
							if (countOfErrors == 0) {
								if (rowNumber.getRecordType().equals("CURRENT")) {
									deleteRowNumberList.add(rowNumber);
								}
								tableModelMain.removeRow(cellsEditor.getActiveRowObject().getRowIndex());
								checkErrorsToUpdate(true, false);
							}
						}
					} catch(Exception e) {
						e.printStackTrace(exceptionStream);
						setErrorAndCloseFunction();
					} finally {
						setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					}
				} else {
					messageList.add(XFUtility.RESOURCE.getString("FunctionMessage39"));
				}
			}
		}
	}

	void jFunctionButton_actionPerformed(ActionEvent e) {
		Component com = (Component)e.getSource();
		for (int i = 0; i < 7; i++) {
			if (com.equals(jButtonArray[i])) {
				doButtonAction(actionDefinitionArray[i]);
				break;
			}
		}
	}

	void doButtonAction(String action) {
		try {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			messageList.clear();

			if (action.equals("EXIT")) {
				closeFunction();
			}

			if (action.equals("UPDATE")) {
				checkErrorsToUpdate(false, true);
			}

			if (action.equals("ADD_ROW")) {
				addRow();
			}

			if (action.equals("REMOVE_ROW")) {
				removeRow();
			}

			if (action.equals("OUTPUT")) {
				session_.browseFile(getExcellBookURI());
			}

			setMessagesOnPanel();
		} finally {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}
	
	class TableModelEditableList extends DefaultTableModel {
		private static final long serialVersionUID = 1L;
		public boolean isCellEditable(int row, int col) {
			return true;
		}
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Class getColumnClass(int col){
			return XF310_DetailRowNumber.class;
		}
	}

	class TableHeadersRenderer extends JPanel implements TableCellRenderer {   
		private static final long serialVersionUID = 1L;
		private JLabel numberLabel = new JLabel("No.");
		private JPanel centerPanel = new JPanel();
		private ArrayList<JLabel> headerList = new ArrayList<JLabel>();
		private int totalWidthOfCenterPanel = 0;
		private int totalHeight = 0;
		private Component sizingHeader = null;

		public TableHeadersRenderer() {
			arrangeColumnsPosition(true);
			centerPanel.setLayout(null);
			numberLabel.setFont(new java.awt.Font("SansSerif", 0, 14));
			numberLabel.setBorder(new HeaderBorder());
			numberLabel.setHorizontalAlignment(SwingConstants.CENTER);
			this.setLayout(new BorderLayout());
			this.add(numberLabel, BorderLayout.WEST);
			this.add(centerPanel, BorderLayout.CENTER);
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {  
			return this;
		}
		
		public int getWidth() {
			return this.getPreferredSize().width;
		}
		
		public int getSequenceWidth() {
			return numberLabel.getPreferredSize().width;
		}
		
		public String getSequenceLabel() {
			return numberLabel.getText();
		}
		
		public int getHeight() {
			return this.getPreferredSize().height;
		}
		
		public ArrayList<JLabel> getColumnHeaderList() {
			return headerList;
		}
		
		public boolean hasMouseOnColumnBorder(int headersPosX) {
			boolean result = false;
			double posX = headersPosX - numberLabel.getBounds().getWidth();
			if (posX >= -3 && posX <= 0) {
				result = true;
			} else {
				for (int i = 0; i < headerList.size(); i++) {
					if (posX >= (headerList.get(i).getBounds().x + headerList.get(i).getBounds().width - 3)
							&& posX <= (headerList.get(i).getBounds().x + headerList.get(i).getBounds().width)) {
						result = true;
						break;
					}
				}
			}
			return result;
		}
		
		public void setSizingHeader(int headersPosX) {
			double posX = headersPosX - numberLabel.getBounds().getWidth();
			sizingHeader = numberLabel;
			for (int i = 0; i < headerList.size(); i++) {
				if (posX >= (headerList.get(i).getBounds().x + headerList.get(i).getBounds().width - 3)
						&& posX <= (headerList.get(i).getBounds().x + headerList.get(i).getBounds().width)) {
					sizingHeader = headerList.get(i);
					break;
				}
			}
		}
		
		public void setNewBoundsToHeaders(int posXOnHeaders) {
			if (sizingHeader == numberLabel) {
				numberLabel.setPreferredSize(new Dimension(posXOnHeaders, totalHeight));
				this.setPreferredSize(new Dimension(totalWidthOfCenterPanel + posXOnHeaders, totalHeight));
			} else {
				int posX = posXOnHeaders - numberLabel.getBounds().width;
				int widthAdjusted = 0;
				for (int i = 0; i < headerList.size(); i++) {
					if (sizingHeader == headerList.get(i)) {
						int newWidth = posX - headerList.get(i).getBounds().x;
						if (newWidth > 0) {
							detailColumnList.get(i).setWidth(newWidth);
							widthAdjusted = newWidth - headerList.get(i).getBounds().width;
						}
						break;
					}
				}
				if (widthAdjusted != 0) {
					arrangeColumnsPosition(false);
				}
			}
		}
		
		public void arrangeColumnsPosition(boolean isWithDefaultSequenceWidth) {
			int fromX = 0;
			int fromY = 0;
			int width, height, wrkInt1, wrkInt2;
			JLabel header;
			totalWidthOfCenterPanel = 0;
			centerPanel.removeAll();
			headerList.clear();
			for (int i = 0; i < detailColumnList.size(); i++) {
				if (detailColumnList.get(i).isVisibleOnPanel()) {
					header = new JLabel();
					header.setFont(new java.awt.Font("SansSerif", 0, 14));
					if (detailColumnList.get(i).getValueType().equals("IMAGE")
							|| detailColumnList.get(i).getValueType().equals("FLAG")) {
						header.setHorizontalAlignment(SwingConstants.CENTER);
					} else {
						if (detailColumnList.get(i).getBasicType().equals("INTEGER")
								|| detailColumnList.get(i).getBasicType().equals("FLOAT")) {
							header.setHorizontalAlignment(SwingConstants.RIGHT);
						} else {
							header.setHorizontalAlignment(SwingConstants.LEFT);
						}
					}
					header.setText(detailColumnList.get(i).getCaption());
					header.setOpaque(true);

					width = detailColumnList.get(i).getWidth();
					height = XFUtility.ROW_UNIT_HEIGHT * detailColumnList.get(i).getRows();
					if (i > 0) {
						fromX = headerList.get(i-1).getBounds().x + headerList.get(i-1).getBounds().width;
						fromY = headerList.get(i-1).getBounds().y + headerList.get(i-1).getBounds().height;
						for (int j = i-1; j >= 0; j--) {
							if (detailColumnList.get(i).getLayout().equals("VERTICAL")) {
								wrkInt1 = headerList.get(j).getBounds().y + headerList.get(j).getBounds().height;
								if (wrkInt1 <= fromY) {
									fromX = headerList.get(j).getBounds().x;
								} else {
									break;
								}
							} else {
								wrkInt1 = headerList.get(j).getBounds().x + headerList.get(j).getBounds().width;
								if (wrkInt1 <= fromX) {
									fromY = headerList.get(j).getBounds().y;
								} else {
									break;
								}
							}
						}
						for (int j = i-1; j >= 0; j--) {
							wrkInt1 = headerList.get(j).getBounds().x + headerList.get(j).getBounds().width;
							wrkInt2 = fromX + width;
							if (wrkInt2 < wrkInt1 && wrkInt2+2 > wrkInt1) {
								width = wrkInt1 - fromX;
							}
						}
					}

					header.setBounds(new Rectangle(fromX, fromY, width, height));
					header.setBorder(new HeaderBorder());
					headerList.add(header);
					centerPanel.add(header);

					if (fromX + width > totalWidthOfCenterPanel) {
						totalWidthOfCenterPanel = fromX + width;
					}
					if (fromY + height > totalHeight) {
						totalHeight = fromY + height;
					}
				}
			}
			if (isWithDefaultSequenceWidth) {
				numberLabel.setPreferredSize(new Dimension(XFUtility.SEQUENCE_WIDTH, totalHeight));
			}
			centerPanel.setPreferredSize(new Dimension(totalWidthOfCenterPanel, totalHeight));
			this.setPreferredSize(new Dimension(totalWidthOfCenterPanel + numberLabel.getPreferredSize().width, totalHeight));
		}

		public String getToolTipText(MouseEvent e) {
			String text = "";
			XF310_DetailColumn column;
			if (e.getPoint().x > numberLabel.getPreferredSize().width) {
				Component compo = centerPanel.getComponentAt(e.getPoint().x-numberLabel.getPreferredSize().width, e.getPoint().y);
				if (compo != null) {
					for (int i = 0; i < headerList.size(); i++) {
						if (compo.equals(headerList.get(i))) {
							column = detailColumnList.get(i);
							if (column.getDecimalSize() > 0) {
								text = "<html>" + column.getFieldName() + " " + column.getDataSourceName() + " (" + column.getDataSize() + "," + column.getDecimalSize() + ")<br>" + column.getFieldRemarks();
							} else {
								text = "<html>" + column.getFieldName() + " " + column.getDataSourceName() + " (" + column.getDataSize() + ")<br>" + column.getFieldRemarks();
							}
							break;
						}
					}
				}
			} else {
				text = numberLabel.getText();
			}
			return text;
		}
	}  

	public class TableCellsRenderer extends JPanel implements TableCellRenderer {
		private static final long serialVersionUID = 1L;
		private JLabel numberCell = new JLabel("");
		private JPanel multiLinesPanel = new JPanel();
		private ArrayList<JLabel> cellList = new ArrayList<JLabel>();
		private TableHeadersRenderer headersRenderer_;

		public TableCellsRenderer(TableHeadersRenderer headersRenderer) {
			headersRenderer_ = headersRenderer;
			numberCell.setFont(new java.awt.Font("SansSerif", 0, 12));
			numberCell.setBorder(new CellBorder());
			numberCell.setHorizontalAlignment(SwingConstants.CENTER);
			multiLinesPanel.setLayout(null);
			multiLinesPanel.setOpaque(false);
			setupCellBounds();
			this.setLayout(new BorderLayout());
			this.add(numberCell, BorderLayout.WEST);
			this.add(multiLinesPanel, BorderLayout.CENTER);
		}   
		
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if (isSelected) {
				setBackground(table.getSelectionBackground());
				numberCell.setForeground(table.getSelectionForeground());
			} else {
				if (row%2==0) {
					setBackground(SystemColor.text);
				} else {
					setBackground(XFUtility.ODD_ROW_COLOR);
				}
				numberCell.setForeground(table.getForeground());
			}
			setFocusable(false);

			XF310_DetailRowNumber rowObject = (XF310_DetailRowNumber)value;
			rowObject.setValuesToDetailColumns();
			numberCell.setText(rowObject.getRowNumberString());
			for (int i = 0; i < cellList.size(); i++) {
				cellList.get(i).setFont(new java.awt.Font("Monospaced", 0, 14));
				if (detailColumnList.get(i).getValueType().equals("IMAGE")
						|| detailColumnList.get(i).getValueType().equals("FLAG")) {
					cellList.get(i).setIcon((Icon)detailColumnList.get(i).getExternalValue());
				} else {
					cellList.get(i).setText((String)detailColumnList.get(i).getExternalValue());
					if (detailColumnList.get(i).getColor().equals(Color.black)) {
						if (isSelected) {
							cellList.get(i).setForeground(table.getSelectionForeground());
						} else {
							cellList.get(i).setForeground(table.getForeground());
						}
					} else {
						cellList.get(i).setForeground(Color.getColor(detailColumnList.get(i).getColor()));
					}
				}
				if (rowObject.getErrorCellIndexList().contains(i)) {
					cellList.get(i).setBackground(XFUtility.ERROR_COLOR);
				} else {
					if (row%2==0) {
						cellList.get(i).setBackground(SystemColor.text);
					} else {
						cellList.get(i).setBackground(XFUtility.ODD_ROW_COLOR);
					}
				}
			}
			return this;
		}

		private void setupCellBounds() {
			JLabel cell;
			Rectangle rec;
			cellList.clear();
			multiLinesPanel.removeAll();
			for (int i = 0; i < headersRenderer_.getColumnHeaderList().size(); i++) {
				cell = new JLabel();
				cell.setFont(new java.awt.Font("SansSerif", 0, 14));
				cell.setHorizontalAlignment(headersRenderer_.getColumnHeaderList().get(i).getHorizontalAlignment());
				rec = headersRenderer_.getColumnHeaderList().get(i).getBounds();
				cell.setBounds(rec.x, rec.y, rec.width, rec.height);
				cell.setBorder(new HeaderBorder());
				cell.setOpaque(true);
				cellList.add(cell);
				multiLinesPanel.add(cell);
			}
			int totalWidth = headersRenderer_.getWidth() - headersRenderer_.getSequenceWidth();
			int totalHeight = headersRenderer_.getHeight();
			multiLinesPanel.setPreferredSize(new Dimension(totalWidth, totalHeight));
			numberCell.setPreferredSize(new Dimension(headersRenderer_.getSequenceWidth(), totalHeight));
			this.setPreferredSize(new Dimension(totalWidth + headersRenderer_.getSequenceWidth(), totalHeight));
		}

		public void updateCellWidths() {
			for (int i = 0; i < headersRenderer_.getColumnHeaderList().size(); i++) {
				cellList.get(i).setBounds(headersRenderer_.getColumnHeaderList().get(i).getBounds());
			}
			int totalWidth = headersRenderer_.getWidth() - headersRenderer_.getSequenceWidth();
			int totalHeight = headersRenderer_.getHeight();
			multiLinesPanel.setPreferredSize(new Dimension(totalWidth, totalHeight));
			numberCell.setPreferredSize(new Dimension(headersRenderer_.getSequenceWidth(), totalHeight));
			this.setPreferredSize(new Dimension(totalWidth + headersRenderer_.getSequenceWidth(), totalHeight));
		}
	}
	
	public class TableCellsEditor extends AbstractCellEditor implements TableCellEditor {
		private static final long serialVersionUID = 1L;
	    private JPanel jPanel = new JPanel();
		private JLabel numberCell = new JLabel("");
		private JPanel multiLinesPanel = new JPanel();
		private ArrayList<XFTableColumnEditor> cellList = new ArrayList<XFTableColumnEditor>();
		private TableHeadersRenderer headersRenderer_;
		private int currentActiveRowIndex = -1;
		private int currentActiveCellIndex = -1;
		private XF310_DetailRowNumber activeRowObject = null;

		public TableCellsEditor(TableHeadersRenderer headersRenderer) {
			headersRenderer_ = headersRenderer;
			numberCell.setBorder(new CellBorder());
			numberCell.setHorizontalAlignment(SwingConstants.CENTER);
			numberCell.setFont(new java.awt.Font("SansSerif", 0, 12));
			numberCell.setOpaque(true);
			multiLinesPanel.setLayout(null);
			multiLinesPanel.setOpaque(false);

			XFTableColumnEditor cell;
			cellList.clear();
			multiLinesPanel.removeAll();
			for (int i = 0; i < headersRenderer_.getColumnHeaderList().size(); i++) {
				cell = detailColumnList.get(i).getColumnEditor();
				cell.setHorizontalAlignment(headersRenderer_.getColumnHeaderList().get(i).getHorizontalAlignment());
				cell.setBounds(headersRenderer_.getColumnHeaderList().get(i).getBounds());
				cell.setBorder(new HeaderBorder());
				cellList.add(cell);
				multiLinesPanel.add((Component)cell);
			}

			int totalWidth = headersRenderer_.getWidth() - headersRenderer_.getSequenceWidth();
			int totalHeight = headersRenderer_.getHeight();
			multiLinesPanel.setPreferredSize(new Dimension(totalWidth, totalHeight));
			numberCell.setPreferredSize(new Dimension(headersRenderer_.getSequenceWidth(), totalHeight));
			jPanel.setPreferredSize(new Dimension(totalWidth + headersRenderer_.getSequenceWidth(), totalHeight));

			jPanel.setLayout(new BorderLayout());
			jPanel.add(numberCell, BorderLayout.WEST);
			jPanel.add(multiLinesPanel, BorderLayout.CENTER);
		}

		public void updateCellWidths() {
			for (int i = 0; i < headersRenderer_.getColumnHeaderList().size(); i++) {
				cellList.get(i).setBounds(headersRenderer_.getColumnHeaderList().get(i).getBounds());
			}
			int totalWidth = headersRenderer_.getWidth() - headersRenderer_.getSequenceWidth();
			int totalHeight = headersRenderer_.getHeight();
			multiLinesPanel.setPreferredSize(new Dimension(totalWidth, totalHeight));
			numberCell.setPreferredSize(new Dimension(headersRenderer_.getSequenceWidth(), totalHeight));
		}
		
	    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int vColIndex) {
			currentActiveRowIndex = row;

			activeRowObject = (XF310_DetailRowNumber)value;
			activeRowObject.setValuesToDetailColumns();

			numberCell.setText(activeRowObject.getRowNumberString());
			numberCell.setBackground(table.getSelectionBackground());
			numberCell.setForeground(table.getSelectionForeground());

			for (int i = 0; i < cellList.size(); i++) {
				cellList.get(i).setEditable(detailColumnList.get(i).isEditable());
				cellList.get(i).setFocusable(detailColumnList.get(i).isEditable());
				cellList.get(i).setValue(detailColumnList.get(i).getInternalValue());
				if (activeRowObject.getErrorCellIndexList().contains(i)) {
					cellList.get(i).setColorOfError();
				} else {
					cellList.get(i).setColorOfNormal(row);
				}
			}
			
			return jPanel;
	    }

	    public void transferFocusOfCell(int fromRowIndex, int fromColumnIndex, boolean isForward) {
	    	boolean cellSelected = false;
	    	int column, row;

			if (fromRowIndex > -1) {
				row = fromRowIndex;
			} else {
				row = currentActiveRowIndex;
			}

			if (fromColumnIndex > -1) {
				column = fromColumnIndex;
			} else {
				if (isForward) {
					column = currentActiveCellIndex + 1;
				} else {
					column = currentActiveCellIndex - 1;
				}
			}

			if (isForward) {
				if (column >= cellList.size()) {
					column = 0;
					row++;
				}
				for (int i = row; i < jTableMain.getRowCount(); i++) {
					for (int j = column; j < cellList.size(); j++) {
						if (cellList.get(j).isEditable()) {
							jTableMain.editCellAt(i, 0);
							cellList.get(j).requestFocus();
							currentActiveRowIndex = i;
							currentActiveCellIndex = j;
							cellSelected = true;
							break;
						}
					}
					if (cellSelected) {
						break;
					} else {
						column = 0;
					}
				}
				if (!cellSelected) {
					if (firstEditableHeaderField != null) {
						stopCellEditing();
						jScrollPaneTable.transferFocus();
					}
				}

			} else {
				if (column < 0) {
					column = cellList.size() - 1;
					row--;
				}
				for (int i = row; i >= 0; i--) {
					for (int j = column; j >= 0; j--) {
						if (cellList.get(j).isEditable()) {
							jTableMain.editCellAt(i, 0);
							cellList.get(j).requestFocus();
							currentActiveRowIndex = i;
							currentActiveCellIndex = j;
							cellSelected = true;
							break;
						}
					}
					if (cellSelected) {
						break;
					} else {
						column = cellList.size() - 1;
					}
				}
				if (!cellSelected) {
					if (firstEditableHeaderField != null) {
						stopCellEditing();
						jScrollPaneTable.transferFocusBackward();
					}
				}
			}
		}
	    
	    public void requestFocusOnVerticalCell(String direction) {
	    	if (direction.equals("UP") && currentActiveRowIndex >= 1) {
	    		transferFocusOfCell(currentActiveRowIndex-1, currentActiveCellIndex, false);
	    	}
	    	if (direction.equals("DOWN") && currentActiveRowIndex < jTableMain.getRowCount()-1) {
		    	transferFocusOfCell(currentActiveRowIndex+1, currentActiveCellIndex, true);
		    }
	    }
	    
	    public void requestFocusOnCellAt(int row, int column) {
	    	if (row >= 0 && row < jTableMain.getRowCount()) {
	    		jTableMain.editCellAt(row, 0);
	    		if (column >= 0
	    				&& column < cellList.size()
	    				&& cellList.get(column).isEditable()) {
	    			cellList.get(column).requestFocus();
	    			currentActiveRowIndex = row;
	    			currentActiveCellIndex = column;
	    		}
	    	}
	    }

	    public XF310_DetailRowNumber getActiveRowObject() {
			return activeRowObject;
		}
	    
	    public void updateRowObject() {
	    	Object value;
	    	if (activeRowObject != null) {
	    		for (int i = 0; i < cellList.size(); i++) {
	    			value = cellList.get(i).getInternalValue();
	    			if (value instanceof XFHashMap) {
	    				XFHashMap keyValues = (XFHashMap)value;
	    				for (int j = 0; j < keyValues.size(); j++) {
	    					activeRowObject.getColumnValueMap().put(
	    							keyValues.getKeyIDByIndex(j), keyValues.getValueByIndex(j));
	    				}
	    			} else {
	    				activeRowObject.getColumnValueMap().put(
	    						detailColumnList.get(i).getDataSourceName(), value);
	    			}
	    		}
	    	}
	    }
	    
	    public void updateActiveColumnIndex() {
			for (int i = 0; i < cellList.size(); i++) {
				if (cellList.get(i).hasFocus()) {
					currentActiveCellIndex = i;
				}
			}
	    }
	    
	    public ArrayList<XFTableColumnEditor> getCellList() {
	    	return cellList;
	    }

	    public Object getCellEditorValue() {
	    	updateRowObject();
			return activeRowObject;
	    }
	}

	class WorkingRow extends Object {
		private HashMap<String, Object> keyValueMap_ = new HashMap<String, Object>();
		private HashMap<String, Object> columnValueMap_ = new HashMap<String, Object>();
		private HashMap<String, Boolean> columnIsEditableMap_ = new HashMap<String, Boolean>();
		private ArrayList<Object> orderByValueList_ = new ArrayList<Object>();
		public WorkingRow(HashMap<String, Object> keyValueMap, HashMap<String, Object> columnValueMap, ArrayList<Object> orderByValueList, HashMap<String, Boolean> columnIsEditableMap) {
			keyValueMap_ = keyValueMap;
			columnValueMap_ = columnValueMap;
			orderByValueList_ = orderByValueList;
			columnIsEditableMap_ = columnIsEditableMap;
		}
		public HashMap<String, Object> getKeyValueMap() {
			return keyValueMap_;
		}
		public HashMap<String, Object> getColumnValueMap() {
			return columnValueMap_;
		}
		public HashMap<String, Boolean> getColumnIsEditableMap() {
			return columnIsEditableMap_;
		}
		public ArrayList<Object> getOrderByValueList() {
			return orderByValueList_;
		}
	}

	class WorkingRowComparator implements java.util.Comparator<WorkingRow>{
		public int compare(WorkingRow row1, WorkingRow row2){
			int compareResult = 0;
			for (int i = 0; i < row1.getOrderByValueList().size(); i++) {
				compareResult = row1.getOrderByValueList().get(i).toString().compareTo(row2.getOrderByValueList().get(i).toString());
				if (detailTable.getOrderByFieldIDList().get(i).contains("(D)")) {
					compareResult = compareResult * -1;
				}
				if (compareResult != 0) {
					break;
				}
			}
			return compareResult;
		}
	}

	private URI getExcellBookURI() {
		File xlsFile = null;
		String xlsFileName = "";
		FileOutputStream fileOutputStream = null;
		HSSFFont font = null;
		XF310_DetailRowNumber rowObject;
		String imageFileName = "";
		String wrkStr;

		HSSFWorkbook workBook = new HSSFWorkbook();
		wrkStr = functionElement_.getAttribute("Name").replace("/", "_").replace("�^", "_");
		HSSFSheet workSheet = workBook.createSheet(wrkStr);
		workSheet.setDefaultRowHeight( (short) 300);
		HSSFFooter workSheetFooter = workSheet.getFooter();
		workSheetFooter.setRight(functionElement_.getAttribute("Name") + "  Page " + HSSFFooter.page() + " / " + HSSFFooter.numPages() );
		patriarch = workSheet.createDrawingPatriarch();

		HSSFFont fontHeader = workBook.createFont();
		fontHeader = workBook.createFont();
		fontHeader.setFontName(XFUtility.RESOURCE.getString("XLSFontHDR"));
		fontHeader.setFontHeightInPoints((short)11);

		HSSFFont fontDataBlack = workBook.createFont();
		fontDataBlack.setFontName(XFUtility.RESOURCE.getString("XLSFontDTL"));
		fontDataBlack.setFontHeightInPoints((short)11);
		HSSFFont fontDataRed = workBook.createFont();
		fontDataRed.setFontName(XFUtility.RESOURCE.getString("XLSFontDTL"));
		fontDataRed.setFontHeightInPoints((short)11);
		fontDataRed.setColor(HSSFColor.RED.index);
		HSSFFont fontDataBlue = workBook.createFont();
		fontDataBlue.setFontName(XFUtility.RESOURCE.getString("XLSFontDTL"));
		fontDataBlue.setFontHeightInPoints((short)11);
		fontDataBlue.setColor(HSSFColor.BLUE.index);
		HSSFFont fontDataGreen = workBook.createFont();
		fontDataGreen.setFontName(XFUtility.RESOURCE.getString("XLSFontDTL"));
		fontDataGreen.setFontHeightInPoints((short)11);
		fontDataGreen.setColor(HSSFColor.GREEN.index);
		HSSFFont fontDataOrange = workBook.createFont();
		fontDataOrange.setFontName(XFUtility.RESOURCE.getString("XLSFontDTL"));
		fontDataOrange.setFontHeightInPoints((short)11);
		fontDataOrange.setColor(HSSFColor.ORANGE.index);

		HSSFCellStyle styleHeaderLabel = workBook.createCellStyle();
		styleHeaderLabel.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		styleHeaderLabel.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		styleHeaderLabel.setBorderRight(HSSFCellStyle.BORDER_THIN);
		styleHeaderLabel.setBorderTop(HSSFCellStyle.BORDER_THIN);
		styleHeaderLabel.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		styleHeaderLabel.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		styleHeaderLabel.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		styleHeaderLabel.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		styleHeaderLabel.setFont(fontHeader);

		HSSFCellStyle styleDetailLabel = workBook.createCellStyle();
		styleDetailLabel.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		styleDetailLabel.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		styleDetailLabel.setBorderRight(HSSFCellStyle.BORDER_THIN);
		styleDetailLabel.setBorderTop(HSSFCellStyle.BORDER_THIN);
		styleDetailLabel.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		styleDetailLabel.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		styleDetailLabel.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		styleDetailLabel.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		styleDetailLabel.setFont(fontHeader);
		styleDetailLabel.setWrapText(true);

		HSSFCellStyle styleDetailNumberLabel = workBook.createCellStyle();
		styleDetailNumberLabel.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		styleDetailNumberLabel.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		styleDetailNumberLabel.setBorderRight(HSSFCellStyle.BORDER_THIN);
		styleDetailNumberLabel.setBorderTop(HSSFCellStyle.BORDER_THIN);
		styleDetailNumberLabel.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		styleDetailNumberLabel.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		styleDetailNumberLabel.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		styleDetailNumberLabel.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		styleDetailNumberLabel.setFont(fontHeader);

		HSSFCellStyle styleDataInteger = workBook.createCellStyle();
		styleDataInteger.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		styleDataInteger.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		styleDataInteger.setBorderRight(HSSFCellStyle.BORDER_THIN);
		styleDataInteger.setBorderTop(HSSFCellStyle.BORDER_THIN);
		styleDataInteger.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		styleDataInteger.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
		styleDataInteger.setFont(fontDataBlack);
		styleDataInteger.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0"));

		int currentRowNumber = -1;
		int mergeRowNumberFrom = -1;

		try {
			xlsFile = session_.createTempFile(functionElement_.getAttribute("ID"), ".xls");
			xlsFileName = xlsFile.getPath();
			fileOutputStream = new FileOutputStream(xlsFileName);
			int columnIndex;
			
			////////////////////////
			// Header Field Lines //
			////////////////////////
			for (int i = 0; i < headerFieldList.size() && i < 24; i++) {
				if (headerFieldList.get(i).isVisibleOnPanel()) {
					for (int j = 0; j < headerFieldList.get(i).getRows(); j++) {
						currentRowNumber++;
						HSSFRow rowData = workSheet.createRow(currentRowNumber);

						/////////////////////////////////
						// Cell for header field label //
						/////////////////////////////////
						HSSFCell cellHeader = rowData.createCell(0);
						cellHeader.setCellStyle(styleHeaderLabel);
						if (j==0) {
							mergeRowNumberFrom = currentRowNumber;
							if (!headerFieldList.get(i).getFieldOptionList().contains("NO_CAPTION")) {
								cellHeader.setCellValue(new HSSFRichTextString(headerFieldList.get(i).getCaption()));
							}
						}
						rowData.createCell(1).setCellStyle(styleHeaderLabel);

						////////////////////////////////
						// Cell for header field data //
						////////////////////////////////
						font = fontDataBlack;
						if (headerFieldList.get(i).getColor().equals("red")) {
							font = fontDataRed;
						}
						if (headerFieldList.get(i).getColor().equals("blue")) {
							font = fontDataBlue;
						}
						if (headerFieldList.get(i).getColor().equals("green")) {
							font = fontDataGreen;
						}
						if (headerFieldList.get(i).getColor().equals("orange")) {
							font = fontDataOrange;
						}
						setupCellAttributesForHeaderField(rowData, workBook, workSheet, headerFieldList.get(i), currentRowNumber, j, font);
					}
					workSheet.addMergedRegion(new CellRangeAddress(mergeRowNumberFrom, currentRowNumber, 0, 1));
					workSheet.addMergedRegion(new CellRangeAddress(mergeRowNumberFrom, currentRowNumber, 2, 6));
				}
			}

			////////////
			// Spacer //
			////////////
			currentRowNumber++;
			workSheet.createRow(currentRowNumber);

			/////////////////////////////////
			// Detail Item Column Headings //
			/////////////////////////////////
			currentRowNumber++;
			HSSFRow rowCaption = workSheet.createRow(currentRowNumber);
			HSSFCell cell = rowCaption.createCell(0);
			cell.setCellStyle(styleDetailNumberLabel);
			workSheet.setColumnWidth(0, headersRenderer.getSequenceWidth() * 40);
			wrkStr = XFUtility.getCaptionForCell(headersRenderer.getSequenceLabel());
			cell.setCellValue(new HSSFRichTextString(wrkStr));
			for (int j = 0; j < detailColumnList.size(); j++) {
				if (detailColumnList.get(j).isVisibleOnPanel()) {
					cell = rowCaption.createCell(j+1);
					if (detailColumnList.get(j).getBasicType().equals("INTEGER")
							|| detailColumnList.get(j).getBasicType().equals("FLOAT")) {
						if (detailColumnList.get(j).getTypeOptionList().contains("MSEQ") || detailColumnList.get(j).getTypeOptionList().contains("FYEAR")) {
							cell.setCellStyle(styleDetailLabel);
						} else {
							cell.setCellStyle(styleDetailNumberLabel);
						}
					} else {
						cell.setCellStyle(styleDetailLabel);
					}
					Rectangle rect = headersRenderer.getColumnHeaderList().get(j).getBounds();
					workSheet.setColumnWidth(j+1, rect.width * 40);
					wrkStr = XFUtility.getCaptionForCell(headersRenderer.getColumnHeaderList().get(j).getText());
					cell.setCellValue(new HSSFRichTextString(wrkStr));
				} else {
					break;
				}
			}

			////////////////////////////
			// Detail Item Data Lines //
			////////////////////////////
			cellsEditor.stopCellEditing();
			for (int i = 0; i < tableModelMain.getRowCount(); i++) {
				currentRowNumber++;
				HSSFRow rowData = workSheet.createRow(currentRowNumber);

				cell = rowData.createCell(0); //Column of Sequence Number
				cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
				cell.setCellStyle(styleDataInteger);
				cell.setCellValue(i + 1);

				rowObject = (XF310_DetailRowNumber)tableModelMain.getValueAt(i,0);
				rowObject.setValuesToDetailColumns();
				columnIndex = 0;
				for (int j = 0; j < detailColumnList.size(); j++) {
					if (detailColumnList.get(j).isVisibleOnPanel()) {
						columnIndex++;
						if (detailColumnList.get(j).getColor().equals(Color.black)) {
							font = fontDataBlack;
						}
						if (detailColumnList.get(j).getColor().equals(Color.red)) {
							font = fontDataRed;
						}
						if (detailColumnList.get(j).getColor().equals(Color.blue)) {
							font = fontDataBlue;
						}
						if (detailColumnList.get(j).getColor().equals(Color.green)) {
							font = fontDataGreen;
						}
						if (detailColumnList.get(j).getColor().equals(Color.orange)) {
							font = fontDataOrange;
						}
						setupCellAttributesForDetailColumn(rowData.createCell(columnIndex), workBook, detailColumnList.get(j), font);
						if (detailColumnList.get(j).getValueType().equals("IMAGE") && !detailColumnList.get(j).getInternalValue().equals("")) {
							imageFileName = session_.getImageFileFolder() + detailColumnList.get(j).getInternalValue();
							XFUtility.setupImageCellForDetailColumn(workBook, workSheet, currentRowNumber, columnIndex, imageFileName, patriarch);
						}
					}
				}
			}

			workBook.write(fileOutputStream);

			messageList.add(XFUtility.RESOURCE.getString("XLSComment1"));

		} catch (Exception e) {
			messageList.add(XFUtility.RESOURCE.getString("XLSErrorMessage"));
			e.printStackTrace(exceptionStream);
		} finally {
			try {
				fileOutputStream.close();
			} catch (Exception e) {
				e.printStackTrace(exceptionStream);
			}
		}
		return xlsFile.toURI();
	}

	private void setupCellAttributesForHeaderField(HSSFRow rowData, HSSFWorkbook workBook, HSSFSheet workSheet, XF310_HeaderField object, int currentRowNumber, int rowIndexInCell, HSSFFont font) {
		String wrk;

		HSSFCellStyle style = workBook.createCellStyle();
		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);
		style.setFont(font);
		style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		style.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
		style.setWrapText(true);
		style.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));

		HSSFCell cellValue = rowData.createCell(2);

		String basicType = object.getBasicType();
		if (basicType.equals("INTEGER")) {
			if (object.getTypeOptionList().contains("MSEQ") || object.getTypeOptionList().contains("FYEAR")) {
				cellValue.setCellType(HSSFCell.CELL_TYPE_STRING);
				cellValue.setCellStyle(style);
				cellValue.setCellValue(new HSSFRichTextString((String)object.getExternalValue()));
			} else {
				style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
				style.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
				wrk = XFUtility.getStringNumber(object.getExternalValue().toString());
				if (wrk.equals("") || object.getTypeOptionList().contains("NO_EDIT")) {
					cellValue.setCellType(HSSFCell.CELL_TYPE_STRING);
					cellValue.setCellStyle(style);
					if (rowIndexInCell==0) {
						cellValue.setCellValue(new HSSFRichTextString(wrk));
					}
				} else {
					cellValue.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
					if (!object.getTypeOptionList().contains("NO_EDIT")
						&& !object.getTypeOptionList().contains("ZERO_SUPPRESS")) {
						style.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0"));
					}
					cellValue.setCellStyle(style);
					if (rowIndexInCell==0) {
						cellValue.setCellValue(Double.parseDouble(wrk));
					}
				}
			}
		} else {
			if (basicType.equals("FLOAT")) {
				style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
				style.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
				wrk = XFUtility.getStringNumber(object.getExternalValue().toString());
				if (wrk.equals("") || object.getTypeOptionList().contains("NO_EDIT")) {
					cellValue.setCellType(HSSFCell.CELL_TYPE_STRING);
					cellValue.setCellStyle(style);
					if (rowIndexInCell==0) {
						cellValue.setCellValue(new HSSFRichTextString(wrk));
					}
				} else {
					cellValue.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
					if (!object.getTypeOptionList().contains("NO_EDIT")
							&& !object.getTypeOptionList().contains("ZERO_SUPPRESS")) {
						style.setDataFormat(XFUtility.getFloatFormat(workBook, object.getDecimalSize()));
					}
					cellValue.setCellStyle(style);
					if (rowIndexInCell==0) {
						cellValue.setCellValue(Double.parseDouble(wrk));
					}
				}
			} else {
				cellValue.setCellType(HSSFCell.CELL_TYPE_STRING);
				cellValue.setCellStyle(style);
				if (rowIndexInCell==0) {
					if (basicType.equals("STRING")) {
						if (object.isImage()) {
							style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
							style.setVerticalAlignment(HSSFCellStyle.VERTICAL_BOTTOM);
							style.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));
							cellValue.setCellStyle(style);
							cellValue.setCellValue(new HSSFRichTextString((String)object.getInternalValue()));
							try {
								XFUtility.setupImageCellForField(workBook, workSheet, 2, currentRowNumber, 4, object.getRows(), (String)object.getExternalValue(), patriarch);
							} catch(Exception e) {
								e.printStackTrace(exceptionStream);
							}
						} else {
							cellValue.setCellStyle(style);
							cellValue.setCellValue(new HSSFRichTextString((String)object.getExternalValue()));
						}
					} else {
						if (basicType.equals("DATE")) {
							java.util.Date utilDate = XFUtility.convertDateFromStringToUtil((String)object.getInternalValue());
							String text = XFUtility.getUserExpressionOfUtilDate(utilDate, session_.getDateFormat(), false);
							cellValue.setCellValue(new HSSFRichTextString(text));
						}
						if (object.getBasicType().equals("DATETIME") || object.getBasicType().equals("TIME")) {
							cellValue.setCellValue(new HSSFRichTextString(object.getInternalValue().toString()));
						}
					}
				}
			}
		}
		rowData.createCell(3).setCellStyle(style);
		rowData.createCell(4).setCellStyle(style);
		rowData.createCell(5).setCellStyle(style);
		rowData.createCell(6).setCellStyle(style);
	}

	private void setupCellAttributesForDetailColumn(HSSFCell cell, HSSFWorkbook workBook, XF310_DetailColumn column, HSSFFont font) {
		String wrk;
		HSSFCellStyle style = workBook.createCellStyle();
		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);
		style.setFont(font);
		style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		style.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
		style.setWrapText(true);
		style.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));

		Object value = column.getExternalValue();
		if (column.getBasicType().equals("INTEGER")) {
			if (column.getTypeOptionList().contains("MSEQ") || column.getTypeOptionList().contains("FYEAR")) {
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);
				cell.setCellValue(new HSSFRichTextString(value.toString()));
				style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
				style.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
				style.setWrapText(true);
				style.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));
				cell.setCellStyle(style);
			} else {
			if (value == null) {
				wrk = "";
			} else {
				wrk = XFUtility.getStringNumber(value.toString());
			}
			if (wrk.equals("") || column.getTypeOptionList().contains("NO_EDIT")) {
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);
				cell.setCellStyle(style);
				cell.setCellValue(new HSSFRichTextString(wrk));
			} else {
				cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
				style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
				style.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
				if (!column.getTypeOptionList().contains("NO_EDIT")
						&& !column.getTypeOptionList().contains("ZERO_SUPPRESS")) {
					style.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0"));
				}
				cell.setCellStyle(style);
				cell.setCellValue(Double.parseDouble(wrk));
			}
			}
		} else {
			if (column.getBasicType().equals("FLOAT")) {
				if (value == null) {
					wrk = "";
				} else {
					wrk = XFUtility.getStringNumber(value.toString());
				}
				if (wrk.equals("") || column.getTypeOptionList().contains("NO_EDIT")) {
					cell.setCellType(HSSFCell.CELL_TYPE_STRING);
					cell.setCellStyle(style);
					cell.setCellValue(new HSSFRichTextString(wrk));
				} else {
					cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
					style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
					style.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
					if (!column.getTypeOptionList().contains("NO_EDIT")
							&& !column.getTypeOptionList().contains("ZERO_SUPPRESS")) {
						style.setDataFormat(XFUtility.getFloatFormat(workBook, column.getDecimalSize()));
					}
					cell.setCellStyle(style);
					cell.setCellValue(Double.parseDouble(wrk));
				}
			} else {
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);
				cell.setCellStyle(style);
				if (value == null || column.getValueType().equals("IMAGE")) {
					wrk = "";
				} else {
					if (column.getValueType().equals("FLAG")) {
						wrk = column.getInternalValue().toString();
					} else {
						wrk = value.toString();
					}
				}
				cell.setCellValue(new HSSFRichTextString(wrk));
			}
		}
	}
	
	void jTableMain_focusGained(FocusEvent e) {
		if (jTableMain.getRowCount() > 0) {
			jTableMain.setSelectionBackground(selectionColorWithFocus);
			jTableMain.setSelectionForeground(Color.white);
		}
	}

	void jTableMain_focusLost(FocusEvent e) {
		jTableMain.setSelectionBackground(selectionColorWithoutFocus);
		jTableMain.setSelectionForeground(Color.black);
	}

	void jScrollPaneTable_mousePressed(MouseEvent e) {
		jTableMain.requestFocus();
	}

	void setMessagesOnPanel() {
		XF310_DetailRowNumber rowObject;

		int workRow = 0;
		boolean topErrorFieldNotFound = true;
		for (int i = 0; i < headerFieldList.size(); i++) {
			if (headerFieldList.get(i).isError()) {
				if (topErrorFieldNotFound) {
					headerFieldList.get(i).requestFocus();
					topErrorFieldNotFound = false;
				}
				messageList.add(workRow, headerFieldList.get(i).getCaption() + XFUtility.RESOURCE.getString("Colon") + headerFieldList.get(i).getError());
				workRow++;
			}
		}
		if (topErrorFieldNotFound) {
			for (int i = 0; i < jTableMain.getRowCount(); i++) {
				rowObject = (XF310_DetailRowNumber)tableModelMain.getValueAt(i, 0);
				if (rowObject.getFirstErrorCellIndex() > -1) {
					cellsEditor.requestFocusOnCellAt(i, rowObject.getFirstErrorCellIndex());
					break;
				}
			}
		}

		jTextAreaMessages.setText("");
		StringBuffer sb = new StringBuffer("");
		for (int i = 0; i < messageList.size(); i++) {
			if (i > 0) {
				sb.append("\n");
			}
			if (messageList.size() > 1) {
				sb.append("(" + Integer.toString(i+1) + "/"+ Integer.toString(messageList.size()) + ") " + messageList.get(i));
			} else {
				sb.append(messageList.get(i));
			}
		}
		jTextAreaMessages.setText(sb.toString());

		int heightOfErrorMessages = (messageList.size() + 1) * 20;
		if (heightOfErrorMessages <= 40) {
			jSplitPaneMain.setDividerLocation(this.getHeight() - 125);
		}
		if (heightOfErrorMessages > 40 && heightOfErrorMessages <= 240) {
			jSplitPaneMain.setDividerLocation(this.getHeight() - heightOfErrorMessages - 80);
		}
		if (heightOfErrorMessages > 240) {
			jSplitPaneMain.setDividerLocation(this.getHeight() - 240 - 80);
		}
	}

	public String getFunctionID() {
		return functionElement_.getAttribute("ID");
	}
	
	public ArrayList<String> getMessageList() {
		return messageList;
	}

	public String getScriptNameRunning() {
		return scriptNameRunning;
	}

	public String getAddRowListTitle() {
		return addRowListTitle;
	}

	public org.w3c.dom.Element getFunctionElement() {
		return functionElement_;
	}

	public ArrayList<String> getKeyFieldList() {
		return headerTable.getKeyFieldIDList();
	}

	public Session getSession() {
		return session_;
	}
	
	public String getFunctionInfo() {
		return jLabelFunctionID.getText();
	}

	public PrintStream getExceptionStream() {
		return exceptionStream;
	}

	public void setExceptionHeader(String value) {
		exceptionHeader = value;
	}
	
	public ScriptEngine getScriptEngine() {
		return scriptEngine;
	}

	public Bindings getEngineScriptBindings() {
		return 	engineScriptBindings;
	}
	
	public Object getFieldObjectByID(String tableID, String fieldID) {
		String id = tableID + "_" + fieldID;
		if (engineScriptBindings.containsKey(id)) {
			return engineScriptBindings.get(id);
		} else {
			JOptionPane.showMessageDialog(null, "Field object " + id + " is not found.");
			return null;
		}
	}

	public void evalScript(String scriptName, String scriptText, Bindings bindings) throws ScriptException {
		if (!scriptText.equals("")) {
			scriptNameRunning = scriptName;
			StringBuffer bf = new StringBuffer();
			bf.append(scriptText);
			bf.append(session_.getScriptFunctions());
			if (bindings == null) {
				scriptEngine.eval(bf.toString());
			} else {
				scriptEngine.eval(bf.toString(), bindings);
				
			}
		}
	}

	public HashMap<String, Object> getParmMap() {
		return parmMap_;
	}
	
	public void setProcessLog(String text) {
		XFUtility.appendLog(text, processLog);
	}
	
	public StringBuffer getProcessLog() {
		return processLog;
	}

	public XFTableOperator createTableOperator(String oparation, String tableID) {
		//return new XFTableOperator(session_, processLog, oparation, tableID);
		XFTableOperator operator = null;
		try {
			operator = new XFTableOperator(session_, processLog, oparation, tableID);
		} catch (Exception e) {
			e.printStackTrace(exceptionStream);
			setErrorAndCloseFunction();
		}
		return operator;
	}

	public XFTableOperator createTableOperator(String sqlText) {
		return new XFTableOperator(session_, processLog, sqlText);
	}

	public HashMap<String, Object> getReturnMap() {
		return returnMap_;
	}

	public void setReturnMap(HashMap<String, Object> map) {
		returnMap_ = map;
	}
	
	public XF310_HeaderTable getHeaderTable() {
		return headerTable;
	}
	
	public XF310_HeaderField getFirstEditableHeaderField() {
		return firstEditableHeaderField;
	}
	
	public void setFirstEditableHeaderField(XF310_HeaderField field) {
		firstEditableHeaderField = field;
	}
	
	public ArrayList<XF310_HeaderReferTable> getHeaderReferTableList() {
		return headerReferTableList;
	}

	public ArrayList<XF310_HeaderField> getHeaderFieldList() {
		return headerFieldList;
	}

	public XF310_HeaderField getHeaderFieldObjectByID(String tableID, String tableAlias, String fieldID) {
		XF310_HeaderField headerField = null;
		for (int i = 0; i < headerFieldList.size(); i++) {
			if (tableID.equals("")) {
				if (headerFieldList.get(i).getTableAlias().equals(tableAlias) && headerFieldList.get(i).getFieldID().equals(fieldID)) {
					headerField = headerFieldList.get(i);
					break;
				}
			}
			if (tableAlias.equals("")) {
				if (headerFieldList.get(i).getTableID().equals(tableID) && headerFieldList.get(i).getFieldID().equals(fieldID)) {
					headerField = headerFieldList.get(i);
					break;
				}
			}
			if (!tableID.equals("") && !tableAlias.equals("")) {
				if (headerFieldList.get(i).getTableID().equals(tableID) && headerFieldList.get(i).getTableAlias().equals(tableAlias) && headerFieldList.get(i).getFieldID().equals(fieldID)) {
					headerField = headerFieldList.get(i);
					break;
				}
			}
		}
		return headerField;
	}

	public boolean containsHeaderField(String tableID, String tableAlias, String fieldID) {
		boolean result = false;
		for (int i = 0; i < headerFieldList.size(); i++) {
			if (tableID.equals("")) {
				if (headerFieldList.get(i).getTableAlias().equals(tableAlias)) {
					result = true;
				}
			}
			if (tableAlias.equals("")) {
				if (headerFieldList.get(i).getTableID().equals(tableID) && headerFieldList.get(i).getFieldID().equals(fieldID)) {
					result = true;
				}
			}
			if (!tableID.equals("") && !tableAlias.equals("")) {
				if (headerFieldList.get(i).getTableID().equals(tableID) && headerFieldList.get(i).getTableAlias().equals(tableAlias) && headerFieldList.get(i).getFieldID().equals(fieldID)) {
					result = true;
				}
			}
		}
		return result;
	}

	public boolean containsDetailField(String tableID, String tableAlias, String fieldID) {
		boolean result = false;
		for (int i = 0; i < detailColumnList.size(); i++) {
			if (tableID.equals("")) {
				if (detailColumnList.get(i).getTableAlias().equals(tableAlias)
						&& detailColumnList.get(i).getFieldID().equals(fieldID)) {
					result = true;
				}
			}
			if (tableAlias.equals("")) {
				if (detailColumnList.get(i).getTableID().equals(tableID)
						&& detailColumnList.get(i).getFieldID().equals(fieldID)) {
					result = true;
				}
			}
			if (!tableID.equals("") && !tableAlias.equals("")) {
				if (detailColumnList.get(i).getTableID().equals(tableID)
						&& detailColumnList.get(i).getTableAlias().equals(tableAlias)
						&& detailColumnList.get(i).getFieldID().equals(fieldID)) {
					result = true;
				}
			}
		}
		return result;
	}

	public XF310_DetailTable getDetailTable() {
		return detailTable;
	}

	public DefaultTableModel getTableModel() {
		return tableModelMain;
	}

	public JTable getJTableMain() {
		return jTableMain;
	}
	
	public TableCellsEditor getCellsEditor() {
		return cellsEditor;
	}

	public ArrayList<XF310_DetailColumn> getDetailColumnList() {
		return detailColumnList;
	}

	public ArrayList<XF310_DetailReferTable> getDetailReferTableList() {
		return detailReferTableList;
	}

	public XF310_DetailColumn getDetailColumnObjectByID(String tableID, String tableAlias, String fieldID) {
		XF310_DetailColumn detailColumnField = null;
		for (int i = 0; i < detailColumnList.size(); i++) {
			if (tableID.equals("")) {
				if (detailColumnList.get(i).getTableAlias().equals(tableAlias) && detailColumnList.get(i).getFieldID().equals(fieldID)) {
					detailColumnField = detailColumnList.get(i);
					break;
				}
			}
			if (tableAlias.equals("")) {
				if (detailColumnList.get(i).getTableID().equals(tableID) && detailColumnList.get(i).getFieldID().equals(fieldID)) {
					detailColumnField = detailColumnList.get(i);
					break;
				}
			}
			if (!tableID.equals("") && !tableAlias.equals("")) {
				if (detailColumnList.get(i).getTableID().equals(tableID) && detailColumnList.get(i).getTableAlias().equals(tableAlias) && detailColumnList.get(i).getFieldID().equals(fieldID)) {
					detailColumnField = detailColumnList.get(i);
					break;
				}
			}
		}
		return detailColumnField;
	}

	public String getTableIDOfTableAlias(String tableAlias) {
		String tableID = tableAlias;
		org.w3c.dom.Element workElement;
		for (int j = 0; j < headerReferElementList.getLength(); j++) {
			workElement = (org.w3c.dom.Element)headerReferElementList.item(j);
			if (workElement.getAttribute("TableAlias").equals(tableAlias)) {
				tableID = workElement.getAttribute("ToTable");
				break;
			}
		}
		if (detailReferElementList != null) {
			for (int j = 0; j < detailReferElementList.getLength(); j++) {
				workElement = (org.w3c.dom.Element)detailReferElementList.item(j);
				if (workElement.getAttribute("TableAlias").equals(tableAlias)) {
					tableID = workElement.getAttribute("ToTable");
					break;
				}
			}
		}
		return tableID;
	}

	public Object getValueOfHeaderFieldByName(String dataSourceName) {
		Object obj = null;
		for (int i = 0; i < headerFieldList.size(); i++) {
			if (headerFieldList.get(i).getDataSourceName().equals(dataSourceName)) {
				obj = headerFieldList.get(i).getInternalValue();
				break;
			}
		}
		return obj;
	}
}

class XF310_HeaderField extends XFFieldScriptable {
	private static final long serialVersionUID = 1L;
	org.w3c.dom.Element functionFieldElement_ = null;
	private org.w3c.dom.Element tableElement = null;
	private String tableID_ = "";
	private String tableAlias_ = "";
	private String fieldID_ = "";
	private String fieldName = "";
	private String fieldRemarks = "";
	private String dataType = "";
	private String dataTypeOptions = "";
	private ArrayList<String> dataTypeOptionList;
	private String fieldCaption = "";
	private int dataSize = 5;
	private int decimalSize = 0;
	private String fieldOptions = "";
	private ArrayList<String> fieldOptionList;
	private int fieldRows = 1;
	private JPanel jPanelField = new JPanel();
	private JLabel jLabelField = new JLabel();
	private JPanel jPanelFieldComment = null;
	private JLabel jLabelFieldComment = new JLabel();
	private XFEditableField component = null;
	private XFEditableField refferComponent = null;
	private JButton jButtonToRefferZipNo = null;
	private boolean isKey = false;
	private boolean isNullable = true;
	private boolean isFieldOnPrimaryTable = false;
	private boolean isVisibleOnPanel = true;
	private boolean isEditable = true;
	private boolean isHorizontal = false;
	private boolean isVirtualField = false;
	private boolean isRangeKeyFieldValid = false;
	private boolean isRangeKeyFieldExpire = false;
	private boolean isImage = false;
	private boolean isError = false;
	private String errorMessage = "";
	private int positionMargin = 0;
	private Color foreground = Color.black;
	private XF310 dialog_;

	public XF310_HeaderField(org.w3c.dom.Element functionFieldElement, XF310 dialog){
		super();
		String wrkStr;
		dialog_ = dialog;
		functionFieldElement_ = functionFieldElement;
		fieldOptions = functionFieldElement_.getAttribute("FieldOptions");
		fieldOptionList = XFUtility.getOptionList(fieldOptions);

		StringTokenizer workTokenizer = new StringTokenizer(functionFieldElement_.getAttribute("DataSource"), "." );
		tableAlias_ = workTokenizer.nextToken();
		tableID_ = dialog.getTableIDOfTableAlias(tableAlias_);
		fieldID_ =workTokenizer.nextToken();

		if (tableID_.equals(dialog_.getHeaderTable().getTableID())
				&& tableID_.equals(tableAlias_)) {
			isFieldOnPrimaryTable = true;
			ArrayList<String> keyFieldList = dialog_.getHeaderTable().getKeyFieldIDList();
			for (int i = 0; i < keyFieldList.size(); i++) {
				if (keyFieldList.get(i).equals(fieldID_)) {
					isKey = true;
					isEditable = false;
					break;
				}
			}
		} else {
			isEditable = false;
		}

		if (fieldOptionList.contains("HORIZONTAL")) {
			isHorizontal = true;
		}
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "HORIZONTAL");
		if (!wrkStr.equals("")) {
			isHorizontal = true;
			positionMargin = Integer.parseInt(wrkStr);
		}
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "VERTICAL");
		if (!wrkStr.equals("")) {
			positionMargin = Integer.parseInt(wrkStr);
		}

		org.w3c.dom.Element workElement = dialog.getSession().getFieldElement(tableID_, fieldID_);
		if (workElement == null) {
			JOptionPane.showMessageDialog(this, tableID_ + "." + fieldID_ + XFUtility.RESOURCE.getString("FunctionError11"));
		}
		fieldName = workElement.getAttribute("Name");
		fieldRemarks = XFUtility.substringLinesWithTokenOfEOL(workElement.getAttribute("Remarks"), "<br>");
		dataType = workElement.getAttribute("Type");
		dataTypeOptions = workElement.getAttribute("TypeOptions");
		dataTypeOptionList = XFUtility.getOptionList(dataTypeOptions);
		if (workElement.getAttribute("Name").equals("")) {
			fieldCaption = workElement.getAttribute("ID");
		} else {
			fieldCaption = fieldName;
		}
		dataSize = Integer.parseInt(workElement.getAttribute("Size"));
		if (!workElement.getAttribute("Decimal").equals("")) {
			decimalSize = Integer.parseInt(workElement.getAttribute("Decimal"));
		}
		if (workElement.getAttribute("Nullable").equals("F")) {
			isNullable = false;
		}

		tableElement = (org.w3c.dom.Element)workElement.getParentNode();
		if (!tableElement.getAttribute("RangeKey").equals("")) {
			workTokenizer = new StringTokenizer(tableElement.getAttribute("RangeKey"), ";" );
			if (workTokenizer.nextToken().equals(fieldID_)) {
				isRangeKeyFieldValid = true;
			}
			if (workTokenizer.nextToken().equals(fieldID_)) {
				isRangeKeyFieldExpire = true;
			}
		}

		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "CAPTION");
		if (!wrkStr.equals("")) {
			fieldCaption = XFUtility.getCaptionValue(wrkStr, dialog_.getSession());
		}
		jLabelField.setText(fieldCaption);
		jLabelField.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabelField.setVerticalAlignment(SwingConstants.TOP);
		jLabelField.setFont(new java.awt.Font("Dialog", 0, 14));
		FontMetrics metrics = jLabelField.getFontMetrics(new java.awt.Font("Dialog", 0, 14));
		if (fieldOptionList.contains("CAPTION_LENGTH_VARIABLE")) {
			jLabelField.setPreferredSize(new Dimension(metrics.stringWidth(fieldCaption), XFUtility.FIELD_UNIT_HEIGHT));
		} else {
			jLabelField.setPreferredSize(new Dimension(120, XFUtility.FIELD_UNIT_HEIGHT));
			if (metrics.stringWidth(fieldCaption) > 120) {
				jLabelField.setFont(new java.awt.Font("Dialog", 0, 12));
				metrics = jLabelField.getFontMetrics(new java.awt.Font("Dialog", 0, 12));
				if (metrics.stringWidth(fieldCaption) > 120) {
					jLabelField.setFont(new java.awt.Font("Dialog", 0, 10));
				}
			}
		}

		if (fieldOptionList.contains("PROMPT_LIST")) {
			isEditable = true;
			XF310_HeaderReferTable referTable = null;
			ArrayList<XF310_HeaderReferTable> referTableList = dialog_.getHeaderReferTableList();
			for (int i = 0; i < referTableList.size(); i++) {
				if (referTableList.get(i).getTableID().equals(tableID_)) {
					if (referTableList.get(i).getTableAlias().equals("") || referTableList.get(i).getTableAlias().equals(tableAlias_)) {
						referTable = referTableList.get(i);
						break;
					}
				}
			}
			component = new XF310_HeaderComboBox(functionFieldElement_.getAttribute("DataSource"), dataTypeOptions, dialog_, referTable, isNullable);
			component.setLocation(5, 0);
		} else {
			wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "PROMPT_CALL");
			if (!wrkStr.equals("")) {
				isEditable = true;
				component = new XF310_HeaderPromptCall(functionFieldElement_, wrkStr, dialog_);
				component.setLocation(5, 0);
				if (this.isFieldOnPrimaryTable) {
					component.setEditable(true);
				}
			} else {
				if (!XFUtility.getOptionValueWithKeyword(dataTypeOptions, "KUBUN").equals("") || !XFUtility.getOptionValueWithKeyword(dataTypeOptions, "VALUES").equals("")) {
					component = new XF310_HeaderComboBox(functionFieldElement_.getAttribute("DataSource"), dataTypeOptions, dialog_, null, isNullable);
					component.setLocation(5, 0);
				} else {
					if (!XFUtility.getOptionValueWithKeyword(dataTypeOptions, "BOOLEAN").equals("")) {
						component = new XFCheckBox(dataTypeOptions);
						component.setLocation(5, 0);
					} else {
						if (dataType.equals("VARCHAR") || dataType.equals("LONG VARCHAR")) {
							component = new XFTextArea(dataSize, dataTypeOptions, fieldOptions);
							component.setLocation(5, 0);
							component.setEditable(false);
						} else {
							if (dataTypeOptionList.contains("URL")) {
								component = new XFUrlField(dataSize, fieldOptions);
								component.setLocation(5, 0);
								component.setEditable(false);
							} else {
								if (dataTypeOptionList.contains("IMAGE")) {
									component = new XFImageField(fieldOptions, dataSize, dialog_.getSession().getImageFileFolder());
									component.setLocation(5, 0);
									isImage = true;
								} else {
									if (dataType.equals("DATE")) {
										component = new XFDateField(dialog_.getSession());
										component.setLocation(5, 0);
										component.setEditable(false);
									} else {
										if (dataTypeOptionList.contains("YMONTH")) {
											component = new XFYMonthBox(dialog_.getSession());
											component.setLocation(5, 0);
											component.setEditable(false);
										} else {
											if (dataTypeOptionList.contains("MSEQ")) {
												component = new XFMSeqBox(dialog_.getSession());
												component.setLocation(5, 0);
												component.setEditable(false);
											} else {
												if (dataTypeOptionList.contains("FYEAR")) {
													component = new XFFYearBox(dialog_.getSession());
													component.setLocation(5, 0);
													component.setEditable(false);
												} else {
													component = new XFTextField(this.getBasicType(), dataSize, decimalSize, dataTypeOptions, fieldOptions);
													component.setLocation(5, 0);
													component.setEditable(false);
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "PROMPT_CALL");
		if ((isFieldOnPrimaryTable && isEditable)
				|| (!isFieldOnPrimaryTable && fieldOptionList.contains("PROMPT_LIST"))
				|| (!isFieldOnPrimaryTable && !wrkStr.equals(""))) {
			component.setEditable(true);
			if (dialog_.getFirstEditableHeaderField() == null) {
				dialog_.setFirstEditableHeaderField(this);
			}
		} else {
			component.setEditable(false);
		}

		fieldRows = component.getRows();
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "WIDTH");
		if (!wrkStr.equals("")) {
			component.setWidth(Integer.parseInt(wrkStr));
		}
		jPanelField.setLayout(null);
		jPanelField.setPreferredSize(new Dimension(component.getWidth() + 5, component.getHeight()));
		jPanelField.add((JComponent)component);

		this.setOpaque(false);
		this.setLayout(new BorderLayout());
		if (fieldOptionList.contains("NO_CAPTION")) {
			this.setPreferredSize(new Dimension(component.getWidth() + 10, component.getHeight()));
		} else {
			this.setPreferredSize(new Dimension(component.getWidth() + jLabelField.getPreferredSize().width + 10, component.getHeight()));
			this.add(jLabelField, BorderLayout.WEST);
		}
		this.add(jPanelField, BorderLayout.CENTER);

		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "COMMENT");
		if (!wrkStr.equals("")) {
			jLabelFieldComment.setText(" " + wrkStr);
			jLabelFieldComment.setForeground(Color.blue);
			jLabelFieldComment.setFont(new java.awt.Font("Dialog", 0, 12));
			jLabelFieldComment.setVerticalAlignment(SwingConstants.TOP);
			metrics = jLabelFieldComment.getFontMetrics(new java.awt.Font("Dialog", 0, 12));
			this.setPreferredSize(new Dimension(this.getPreferredSize().width + metrics.stringWidth(wrkStr) + 6, this.getPreferredSize().height));
		}

		if (dataTypeOptionList.contains("ZIPADRS")) {
			jButtonToRefferZipNo = new JButton();
			jButtonToRefferZipNo.setText("<");
			jButtonToRefferZipNo.setFont(new java.awt.Font("SansSerif", 0, 9));
			jButtonToRefferZipNo.setPreferredSize(new Dimension(37, this.getPreferredSize().height));
			jButtonToRefferZipNo.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						setCursor(new Cursor(Cursor.WAIT_CURSOR));
						String zipNo = refferComponent.getExternalValue().toString();
						if (refferComponent != null && !zipNo.equals("")) {
							String address = dialog_.getSession().getAddressFromZipNo(zipNo);
							if (!address.equals("")) {
								setValue(address);
							}
						}
					} finally {
						setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					}
				}
			});
			jButtonToRefferZipNo.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
				    if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0){
						if (e.getKeyCode() == KeyEvent.VK_ENTER) {
							jButtonToRefferZipNo.doClick();
						}
					}
				} 
			});
			this.setPreferredSize(new Dimension(this.getPreferredSize().width + 34, this.getPreferredSize().height));
		}

		if (jButtonToRefferZipNo != null || jLabelFieldComment != null) {
			jPanelFieldComment = new JPanel();
			jPanelFieldComment.setLayout(new BorderLayout());
			int width = 2;
			if (jButtonToRefferZipNo != null) {
				width = width + jButtonToRefferZipNo.getPreferredSize().width;
			}
			if (jLabelFieldComment != null) {
				width = width + jLabelFieldComment.getPreferredSize().width + 3;
				jPanelFieldComment.add(jLabelFieldComment, BorderLayout.CENTER);
			}
			jPanelFieldComment.setPreferredSize(new Dimension(width, component.getHeight()));
			this.add(jPanelFieldComment, BorderLayout.EAST);
		}

		if (dataTypeOptionList.contains("VIRTUAL")) {
			isVirtualField = true;
		}

		if (decimalSize > 0) {
			wrkStr = "<html>" + fieldName + " " + tableAlias_ + "." + fieldID_ + " (" + dataSize + "," + decimalSize + ")<br>" + fieldRemarks;
		} else {
			wrkStr = "<html>" + fieldName + " " + tableAlias_ + "." + fieldID_ + " (" + dataSize + ")<br>" + fieldRemarks;
		}
		this.setToolTipText(wrkStr);
		component.setToolTipText(wrkStr);

		if (!dialog_.getEngineScriptBindings().containsKey(this.getFieldIDInScript())) {
			dialog_.getEngineScriptBindings().put(this.getFieldIDInScript(), this);
		}
		this.setFocusable(true);
		this.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent event){
			}
			public void focusGained(FocusEvent event){
				if (isEditable) {
					component.requestFocus();
				} else {
					transferFocus();
				}
			}
		});
	}

	public XF310_HeaderField(String tableID, String tableAlias, String fieldID, XF310 dialog){
		super();
		dialog_ = dialog;
		functionFieldElement_ = null;
		fieldOptions = "";
		fieldOptionList = new ArrayList<String>();
		isVisibleOnPanel = false;

		tableID_ = tableID;
		fieldID_ = fieldID;
		dialog_ = dialog;
		if (tableAlias.equals("")) {
			tableAlias_ = tableID;
		} else {
			tableAlias_ = tableAlias;
		}

		if (tableID_.equals(dialog_.getHeaderTable().getTableID()) && tableID_.equals(tableAlias_)) {
			isFieldOnPrimaryTable = true;
			ArrayList<String> keyFieldList = dialog_.getHeaderTable().getKeyFieldIDList();
			for (int i = 0; i < keyFieldList.size(); i++) {
				if (keyFieldList.get(i).equals(fieldID_)) {
					isKey = true;
					break;
				}
			}
		}

		org.w3c.dom.Element workElement = dialog.getSession().getFieldElement(tableID_, fieldID_);
		if (workElement == null) {
			JOptionPane.showMessageDialog(this, tableID_ + "." + fieldID_ + XFUtility.RESOURCE.getString("FunctionError11"));
		}
		dataType = workElement.getAttribute("Type");
		dataTypeOptions = workElement.getAttribute("TypeOptions");
		dataTypeOptionList = XFUtility.getOptionList(dataTypeOptions);
		if (workElement.getAttribute("Name").equals("")) {
			fieldCaption = workElement.getAttribute("ID");
		} else {
			fieldCaption = workElement.getAttribute("Name");
		}
		dataSize = Integer.parseInt(workElement.getAttribute("Size"));
		if (!workElement.getAttribute("Decimal").equals("")) {
			decimalSize = Integer.parseInt(workElement.getAttribute("Decimal"));
		}
		if (workElement.getAttribute("Nullable").equals("F")) {
			isNullable = false;
		}

		tableElement = (org.w3c.dom.Element)workElement.getParentNode();
		if (!tableElement.getAttribute("RangeKey").equals("")) {
			StringTokenizer workTokenizer = new StringTokenizer(tableElement.getAttribute("RangeKey"), ";" );
			if (workTokenizer.nextToken().equals(fieldID_)) {
				isRangeKeyFieldValid = true;
			}
			if (workTokenizer.nextToken().equals(fieldID_)) {
				isRangeKeyFieldExpire = true;
			}
		}

		component = new XFTextField(this.getBasicType(), dataSize, decimalSize, dataTypeOptions, fieldOptions);
		component.setLocation(5, 0);
		component.setEditable(false);

		fieldRows = component.getRows();
		jPanelField.setLayout(null);
		jPanelField.setPreferredSize(new Dimension(component.getWidth() + 5, component.getHeight()));
		jPanelField.add((JComponent)component);

		this.setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(component.getWidth() + 130, component.getHeight()));
		this.add(jPanelField, BorderLayout.CENTER);

		if (dataTypeOptionList.contains("VIRTUAL")) {
			isVirtualField = true;
		}

		if (!dialog_.getEngineScriptBindings().containsKey(this.getFieldIDInScript())) {
			dialog_.getEngineScriptBindings().put(this.getFieldIDInScript(), this);
		}
	}

	public XFEditableField getComponent() {
		return component;
	}

	public void setRefferComponent(XFEditableField compo) {
		refferComponent = compo;
	}

	public void checkPromptKeyEdit(){
		if (!XFUtility.getOptionValueWithKeyword(fieldOptions, "PROMPT_CALL").equals("")) {
			if (((XF310_HeaderPromptCall)component).hasEditControlledKey()) {
				this.setEditable(false);
			}
		}
		if (fieldOptionList.contains("PROMPT_LIST")) {
			if (((XF310_HeaderComboBox)component).hasEditControlledKey()) {
				this.setEditable(false);
			}
		}
	}

	public boolean isNull(){
		return XFUtility.isNullValue(this.getBasicType(), component.getInternalValue());
	}

	public String getTableID(){
		return tableID_;
	}

	public String getTableAlias(){
		return tableAlias_;
	}

	public String getFieldID(){
		return fieldID_;
	}

	public String getDataSourceName(){
		return tableAlias_ + "." + fieldID_;
	}

	public int getDecimalSize(){
		return decimalSize;
	}

	public String getCaption(){
		return fieldCaption;
	}

	public boolean isVisibleOnPanel(){
		return isVisibleOnPanel;
	}

	public boolean isEditable(){
		return isEditable;
	}

	public boolean isComponentFocusable() {
		return component.isComponentFocusable();
	}

	public boolean isFocusable() {
		return false;
	}
	
	public void requestFocus(){
		component.requestFocus();
	}

	public void setEditable(boolean editable){
		isEditable = editable;
		component.setEditable(editable);
		if (jButtonToRefferZipNo != null) {
			if (isEditable) {
				jPanelFieldComment.add(jButtonToRefferZipNo, BorderLayout.WEST);
				jPanelFieldComment.repaint();
			} else {
				jPanelFieldComment.remove(jButtonToRefferZipNo);
				jPanelFieldComment.repaint();
			}
		}
	}

	public boolean isHorizontal(){
		return isHorizontal;
	}

	public int getPositionMargin(){
		return positionMargin;
	}

	public boolean isNullable(){
		return isNullable;
	}

	public boolean isVirtualField(){
		return isVirtualField;
	}

	public boolean isRangeKeyFieldValid(){
		return isRangeKeyFieldValid;
	}

	public boolean isRangeKeyFieldExpire(){
		return isRangeKeyFieldExpire;
	}

	public org.w3c.dom.Element getTableElement(){
		return tableElement;
	}

	public boolean isImage(){
		return isImage;
	}

	public int getRows(){
		return fieldRows;
	}

	public String getBasicType(){
		return XFUtility.getBasicTypeOf(dataType);
	}
	
	public ArrayList<String> getTypeOptionList() {
		return dataTypeOptionList;
	}
	
	public ArrayList<String> getFieldOptionList() {
		return fieldOptionList;
	}

	public boolean isFieldOnPrimaryTable(){
		return isFieldOnPrimaryTable;
	}

	public boolean isKey(){
		return isKey;
	}

	public boolean isNullError(){
		String basicType = this.getBasicType();
		boolean isNullError = false;
		if (basicType.equals("INTEGER")) {
			long value = Long.parseLong((String)component.getInternalValue());
			if (!this.isNullable) {
				if (value == 0) {
					isNullError = true;
				}
			}
		}
		if (basicType.equals("FLOAT")) {
			double value = Double.parseDouble((String)component.getInternalValue());
			if (!this.isNullable) {
				if (value == 0) {
					isNullError = true;
				}
			}
		}
		if (basicType.equals("DATE")) {
			String strDate = (String)component.getInternalValue();
			if (!this.isNullable) {
				if (strDate == null || strDate.equals("")) {
					isNullError = true;
				}
			}
		}
		if (basicType.equals("STRING")) {
			String strWrk = (String)component.getInternalValue();
			if (!this.isNullable) {
				if (strWrk.equals("")) {
					isNullError = true;
				}
			}
			if (dataTypeOptionList.contains("YMONTH") && strWrk.length() > 0 && strWrk.length() < 6) {
				isNullError = true;
			}
			if (dataTypeOptionList.contains("FYEAR") && strWrk.length() > 0 && strWrk.length() < 4) {
				isNullError = true;
			}
		}
		if (isNullError) { 
			this.setError(XFUtility.RESOURCE.getString("FunctionError16"));
		}
		return isNullError;
	}

	public void setValueOfResultSet(XFTableOperator operator){
		try {
			if (this.isVirtualField) {
				if (this.isRangeKeyFieldExpire()) {
					component.setValue(XFUtility.calculateExpireValue(this.getTableElement(), operator, dialog_.getSession(), dialog_.getProcessLog()));
				}
			} else {
				Object value = operator.getValueOf(this.getFieldID()); 
				String basicType = this.getBasicType();
				if (basicType.equals("INTEGER")) {
					if (value == null || value.equals("")) {
						component.setValue("");
					} else {
						String wrkStr = value.toString();
						int pos = wrkStr.indexOf(".");
						if (pos >= 0) {
							wrkStr = wrkStr.substring(0, pos);
						}
						component.setValue(Long.parseLong(wrkStr));
					}
				} else {
					if (basicType.equals("FLOAT")) {
						if (value == null || value.equals("")) {
							component.setValue("");
						} else {
							component.setValue(Double.parseDouble(value.toString()));
						}
					} else {
						if (basicType.equals("STRING") || basicType.equals("TIME") || basicType.equals("DATETIME")) {
							if (value == null) {
								component.setValue("");
							} else {
								component.setValue(value.toString().trim());
							}
						}
						if (basicType.equals("DATE")) {
							component.setValue(value);
						}
					}
				}
			}
			component.setOldValue(component.getInternalValue());
		} catch (Exception e) {
			e.printStackTrace(dialog_.getExceptionStream());
			dialog_.setErrorAndCloseFunction();
		}
	}

	public void setValue(Object object){
		XFUtility.setValueToEditableField(this.getBasicType(), object, component);
	}

	public Object getInternalValue(){
		Object returnObj = null;
		returnObj = (String)component.getInternalValue();
		return returnObj;
	}

	public void setOldValue(Object object){
		XFUtility.setOldValueToEditableField(this.getBasicType(), object, component);
	}

	public Object getOldValue(){
		Object returnObj = null;
		if (this.getBasicType().equals("INTEGER")) {
			returnObj = Long.parseLong((String)component.getOldValue());
		} else {
			if (this.getBasicType().equals("FLOAT")) {
				returnObj = Double.parseDouble((String)component.getOldValue());
			} else {
				if (component.getOldValue() == null) {
					returnObj = "";
				} else {
					returnObj = (String)component.getOldValue();
				}
			}
		}
		return returnObj;
	}
	
	public boolean isValueChanged() {
		return !this.getValue().equals(this.getOldValue());
	}

	public Object getValueForScript(){
		Object returnObj = null;
		if (this.getBasicType().equals("INTEGER")) {
			returnObj = Long.parseLong((String)component.getInternalValue());
		} else {
			if (this.getBasicType().equals("FLOAT")) {
				returnObj = Double.parseDouble((String)component.getInternalValue());
			} else {
				returnObj = (String)component.getInternalValue();
			}
		}
		return returnObj;
	}

	public Object getExternalValue(){
		Object returnObj = (String)component.getExternalValue();
		return returnObj;
	}

	public Object getNullValue(){
		return XFUtility.getNullValueOfBasicType(this.getBasicType());
	}

	public Object getValue() {
		Object returnObj = null;
		if (this.getBasicType().equals("INTEGER")) {
			returnObj = Long.parseLong((String)component.getInternalValue());
		} else {
			if (this.getBasicType().equals("FLOAT")) {
				returnObj = Double.parseDouble((String)component.getInternalValue());
			} else {
				if (component.getInternalValue() == null) {
					returnObj = "";
				} else {
					returnObj = (String)component.getInternalValue();
				}
			}
		}
		return returnObj;
	}

	public void setError(boolean error) {
		if (error) {
			isError = true;
			if (component.isEditable()) {
				component.setBackground(XFUtility.ERROR_COLOR);
			}
		} else {
			isError = false;
			this.errorMessage = "";
			if (component.isEditable()) {
				component.setBackground(XFUtility.ACTIVE_COLOR);
			} else {
				component.setBackground(XFUtility.INACTIVE_COLOR);
			}
		}
	}

	public boolean isError() {
		return isError;
	}

	public void setError(String message) {
		if (!message.equals("") && this.errorMessage.equals("")) {
			setError(true);
			this.errorMessage = message;
		}
	}

	public String getError() {
		return errorMessage;
	}

	public void setColor(String color) {
		foreground = XFUtility.convertStringToColor(color);
		component.setForeground(foreground);
	}

	public String getColor() {
		return XFUtility.convertColorToString(foreground);
	}

	public String getFieldIDInScript(){
		return tableAlias_ + "_" + fieldID_;
	}
}

class XF310_CellEditorWithTextField extends XFTextField implements XFTableColumnEditor {
	private static final long serialVersionUID = 1L;
	private XF310 dialog_ = null;

	public XF310_CellEditorWithTextField(XF310_DetailColumn detailColumn, XF310 dialog) {
		super(detailColumn.getBasicType(), detailColumn.getDataSize(), detailColumn.getDecimalSize(), detailColumn.getDataTypeOptions(), detailColumn.getFieldOptions());
		dialog_ = dialog;
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setOpaque(true);
		if (detailColumn.getBasicType().equals("INTEGER") || detailColumn.getBasicType().equals("FLOAT")) {
			this.setHorizontalAlignment(SwingConstants.RIGHT);
		} else {
			this.setHorizontalAlignment(SwingConstants.LEFT);
		}
		this.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(FocusEvent e) {
				dialog_.getCellsEditor().updateActiveColumnIndex();
			}
		});
	}
	
	public void setColorOfError() {
		this.setBackground(XFUtility.ERROR_COLOR);
	}
	
	public void setColorOfNormal(int row) {
		if (this.isEditable()) {
			if (row%2==0) {
				this.setBackground(SystemColor.text);
			} else {
				this.setBackground(XFUtility.ODD_ROW_COLOR);
			}
		} else {
			this.setBackground(SystemColor.control);
		}
	}
}            

class XF310_CellEditorWithImageField extends JPanel implements XFTableColumnEditor {
	private static final long serialVersionUID = 1L;
	private JLabel jLabel = new JLabel();
	private JButton jButton = new JButton(XFUtility.RESOURCE.getString("Edit"));
	private XF310 dialog_ = null;
	private String imageFileName_ = "";
	private boolean isEditable_ = false;

	public XF310_CellEditorWithImageField(XF310 dialog) {
		super();
		dialog_ = dialog;
		jLabel.setOpaque(true);
		jLabel.setHorizontalAlignment(SwingConstants.CENTER);
		jButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String answer = JOptionPane.showInputDialog(null, XFUtility.RESOURCE.getString("InputImageFileName"), imageFileName_);
				if (answer != null) {
					imageFileName_ = answer;
					dialog_.getCellsEditor().updateRowObject();
					jButton.setToolTipText(imageFileName_);
				}
			}
		});
		this.setLayout(new BorderLayout());
		this.add(jLabel, BorderLayout.CENTER);
		this.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(FocusEvent e) {
				dialog_.getCellsEditor().updateActiveColumnIndex();
			}
		});
	}

	public void setHorizontalAlignment(int alignment) {
	}
	
	public Object getInternalValue() {
		return imageFileName_;
	}
	
	public Object getExternalValue() {
		return jLabel.getIcon();
	}
	
	public void setValue(Object obj) {
		imageFileName_ = obj.toString();
		String fileName = dialog_.getSession().getImageFileFolder() + imageFileName_;
		jLabel.setIcon(XFUtility.createSmallIcon(fileName, this.getHeight()));
		jButton.setToolTipText(imageFileName_);
		jLabel.setToolTipText(imageFileName_);
	}
	
	public void setEditable(boolean isEditable) {
		isEditable_ = isEditable;
		this.removeAll();
		if (isEditable) {
			this.add(jButton, BorderLayout.CENTER);
		} else {
			this.add(jLabel, BorderLayout.CENTER);
		}
	}
	
	public boolean isEditable() {
		return isEditable_;
	}
	
	public void setColorOfError() {
		jButton.setForeground(Color.red);
	}
	
	public void setColorOfNormal(int row) {
		if (this.isEditable()) {
			jButton.setForeground(Color.black);
		} else {
			this.setBackground(SystemColor.control);
		}
	}
}            

class XF310_CellEditorWithDateField extends XFDateField implements XFTableColumnEditor {
	private static final long serialVersionUID = 1L;
	private XF310 dialog_ = null;

	public XF310_CellEditorWithDateField(XF310 dialog) {
		super(dialog.getSession());
		dialog_ = dialog;
		this.setInternalBorder(null);
		this.setOpaque(true);
		this.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(FocusEvent e) {
				dialog_.getCellsEditor().updateActiveColumnIndex();
			}
		});
	}

	public void setHorizontalAlignment(int alignment) {
	}
	
	public void setColorOfError() {
		this.setBackground(XFUtility.ERROR_COLOR);
	}
	
	public void setColorOfNormal(int row) {
		if (this.isEditable()) {
			if (row%2==0) {
				this.setBackground(SystemColor.text);
			} else {
				this.setBackground(XFUtility.ODD_ROW_COLOR);
			}
		} else {
			this.setBackground(SystemColor.control);
		}
	}
}            

class XF310_CellEditorWithLongTextEditor extends JPanel implements XFTableColumnEditor {
	private static final long serialVersionUID = 1L;
	private JTextField jTextField;
	private JButton jButton = new JButton();
	private String fieldCaption_ = "";
	private ArrayList<String> dataTypeOptionList_ = null;
    private XF310 dialog_;

	public XF310_CellEditorWithLongTextEditor(String fieldCaption, ArrayList<String> dataTypeOptionList, XF310 dialog){
		super();
		fieldCaption_ = fieldCaption;
		dataTypeOptionList_ = dataTypeOptionList;
		dialog_ = dialog;

		jTextField = new JTextField();
		jTextField.setOpaque(true);
		jTextField.setBorder(null);
		jTextField.setEditable(false);
		jTextField.setFont(new java.awt.Font("Dialog", 0, 14));

		ImageIcon imageIcon = new ImageIcon(xeadDriver.XF310.class.getResource("prompt.png"));
	 	jButton.setIcon(imageIcon);
		jButton.setPreferredSize(new Dimension(26, XFUtility.FIELD_UNIT_HEIGHT));
		jButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String edittedText = dialog_.getSession().getLongTextEditor().request(dialog_.getTitle(), fieldCaption_, dataTypeOptionList_, jTextField.getText());
				jTextField.setText(edittedText);
			}
		});
		jButton.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(KeyEvent e)  {
			    if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0){
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						jButton.doClick();
					}
				}
				if (e.getKeyCode() == KeyEvent.VK_TAB) {
					e.setSource(dialog_.getJTableMain());
					dialog_.getJTableMain().dispatchEvent(e);
				}
			}
		});

		this.setLayout(new BorderLayout());
		this.setFocusable(true);
		this.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(FocusEvent e) {
				dialog_.getCellsEditor().updateActiveColumnIndex();
				jButton.requestFocus();
			}
		});
		this.add(jTextField, BorderLayout.CENTER);
		this.add(jButton, BorderLayout.EAST);
	}

	public void setHorizontalAlignment(int alignment) {
	}
	
	public void setEditable(boolean isEditable) {
		jButton.setEnabled(isEditable);
	}
	
	public boolean isEditable() {
		return jButton.isEnabled();
	}

	public Object getInternalValue() {
		return jTextField.getText();
	}

	public Object getExternalValue() {
		return jTextField.getText();
	}
	
	public void setValue(Object obj) {
		if (obj == null) {
			jTextField.setText("");
		} else {
			jTextField.setText(obj.toString());
		}
	}
	
	public void setColorOfError() {
		jTextField.setBackground(XFUtility.ERROR_COLOR);
	}
	
	public void setColorOfNormal(int row) {
		if (jButton.isEnabled()) {
			if (row%2==0) {
				jTextField.setBackground(SystemColor.text);
			} else {
				jTextField.setBackground(XFUtility.ODD_ROW_COLOR);
			}
		} else {
			jTextField.setBackground(SystemColor.control);
		}
	}
}            

class XF310_CellEditorWithYMonthBox extends JPanel implements XFTableColumnEditor {
	private static final long serialVersionUID = 1L;
	private JComboBox jComboBoxYear = new JComboBox();
	private JComboBox jComboBoxMonth = new JComboBox();
	private JLabel jLabel = new JLabel();
	private boolean isEditable_ = true;
	private XF310 dialog_ = null;

	public XF310_CellEditorWithYMonthBox(XF310 dialog) {
		super();
		dialog_ = dialog;
		GregorianCalendar calendar = new GregorianCalendar();
		int currentYear = calendar.get(Calendar.YEAR);
		int minimumYear = currentYear - 10;
		int maximumYear = currentYear + 10;

		jComboBoxYear.setFont(new java.awt.Font("Dialog", 0, 11));
		jComboBoxYear.setBounds(new Rectangle(-1, 0, 50, XFUtility.ROW_UNIT_HEIGHT));
		jComboBoxYear.addItem("");
		for (int i = minimumYear; i <= maximumYear; i++) {
			jComboBoxYear.addItem(String.valueOf(i));
		}
		jComboBoxYear.addItem("9999");
		jComboBoxYear.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuCanceled(PopupMenuEvent arg0) {
			}
			public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
				dialog_.getCellsEditor().updateRowObject();
			}
			public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
			}
		});
		jComboBoxMonth.setFont(new java.awt.Font("Dialog", 0, 11));
		jComboBoxMonth.setBounds(new Rectangle(48, 0, 38, XFUtility.ROW_UNIT_HEIGHT));
		jComboBoxMonth.addItem("");
		jComboBoxMonth.addItem("01");
		jComboBoxMonth.addItem("02");
		jComboBoxMonth.addItem("03");
		jComboBoxMonth.addItem("04");
		jComboBoxMonth.addItem("05");
		jComboBoxMonth.addItem("06");
		jComboBoxMonth.addItem("07");
		jComboBoxMonth.addItem("08");
		jComboBoxMonth.addItem("09");
		jComboBoxMonth.addItem("10");
		jComboBoxMonth.addItem("11");
		jComboBoxMonth.addItem("12");
		jComboBoxMonth.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuCanceled(PopupMenuEvent arg0) {
			}
			public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
				dialog_.getCellsEditor().updateRowObject();
			}
			public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
			}
		});
		jLabel.setFont(new java.awt.Font("Monospaced", 0, 14));
		jLabel.setBackground(SystemColor.control);
		jLabel.setBounds(new Rectangle(0, 0, 85, XFUtility.ROW_UNIT_HEIGHT));

		this.setSize(new Dimension(85, XFUtility.ROW_UNIT_HEIGHT));
		this.setBorder(BorderFactory.createEmptyBorder());
		this.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(FocusEvent e) {
				dialog_.getCellsEditor().updateActiveColumnIndex();
				jComboBoxYear.setBackground(Color.cyan);
				jComboBoxMonth.setBackground(Color.cyan);
			}
			public void focusLost(FocusEvent e) {
				jComboBoxYear.setBackground(SystemColor.text);
				jComboBoxMonth.setBackground(SystemColor.text);
			}
		});
		this.setLayout(null);
		this.add(jComboBoxYear);
		this.add(jComboBoxMonth);
	}
	
	public void setHorizontalAlignment(int alignment) {
		jLabel.setHorizontalAlignment(alignment);
	}
	
	public void setEditable(boolean isEditable) {
		isEditable_ = isEditable;
		this.removeAll();
		if (isEditable) {
			this.add(jComboBoxYear);
			this.add(jComboBoxMonth);
		} else {
			jLabel.setText(this.getExternalValue().toString());
			this.add(jLabel);
		}
	}
	
	public boolean isEditable() {
		return isEditable_;
	}

	public Object getInternalValue() {
		String year = (String)jComboBoxYear.getItemAt(jComboBoxYear.getSelectedIndex());
		String month = (String)jComboBoxMonth.getItemAt(jComboBoxMonth.getSelectedIndex());
		return year + month;
	}

	public Object getExternalValue() {
		return this.getInternalValue();
	}
	
	public void setValue(Object obj) {
		String value = (String)obj;
		if (value != null) {
			value = value.trim();
		}
		if (value == null || value.equals("")) {
			jComboBoxYear.setSelectedIndex(0);
			jComboBoxMonth.setSelectedIndex(0);
		} else {
			if (value.length() == 6) {
				String yearValue = value.substring(0, 4);
				String monthValue = value.substring(4, 6);
				for (int i = 0; i < jComboBoxYear.getItemCount(); i++) {
					if (jComboBoxYear.getItemAt(i).equals(yearValue)) {
						jComboBoxYear.setSelectedIndex(i);
						break;
					}
				}
				for (int i = 0; i < jComboBoxMonth.getItemCount(); i++) {
					if (jComboBoxMonth.getItemAt(i).equals(monthValue)) {
						jComboBoxMonth.setSelectedIndex(i);
						break;
					}
				}
			}
		}
	}

	public void setBackground(Color color) {
		if (jComboBoxYear != null && jComboBoxMonth != null) {
			jComboBoxYear.setBackground(color);
			jComboBoxMonth.setBackground(color);
		}
	}
	
	public void setColorOfError() {
		jComboBoxYear.setBackground(XFUtility.ERROR_COLOR);
		jComboBoxMonth.setBackground(XFUtility.ERROR_COLOR);
	}
	
	public void setColorOfNormal(int row) {
		if (this.isEditable()) {
			if (row%2==0) {
				jComboBoxYear.setBackground(SystemColor.text);
				jComboBoxMonth.setBackground(SystemColor.text);
			} else {
				jComboBoxYear.setBackground(XFUtility.ODD_ROW_COLOR);
				jComboBoxMonth.setBackground(XFUtility.ODD_ROW_COLOR);
			}
		} else {
			jComboBoxYear.setBackground(SystemColor.control);
			jComboBoxMonth.setBackground(SystemColor.control);
		}
	}
}

class XF310_CellEditorWithFYearBox extends JPanel implements XFTableColumnEditor {
	private static final long serialVersionUID = 1L;
	private JComboBox jComboBoxYear = new JComboBox();
	private JLabel jLabel = new JLabel();
	private boolean isEditable_ = true;
	private XF310 dialog_ = null;

	public XF310_CellEditorWithFYearBox(XF310 dialog) {
		super();
		dialog_ = dialog;

		GregorianCalendar calendar = new GregorianCalendar();
		int currentYear = calendar.get(Calendar.YEAR);
		int minimumYear = currentYear - 10;
		int maximumYear = currentYear + 10;

		jComboBoxYear.setFont(new java.awt.Font("Dialog", 0, 11));
		jComboBoxYear.setBounds(new Rectangle(-1, 0, 50, 17));
		jComboBoxYear.addItem("");
		for (int i = minimumYear; i <= maximumYear; i++) {
			jComboBoxYear.addItem(String.valueOf(i));
		}
		jComboBoxYear.addItem("9999");
		jComboBoxYear.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuCanceled(PopupMenuEvent arg0) {
			}
			public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
				dialog_.getCellsEditor().updateRowObject();
			}
			public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
			}
		});
		jLabel.setFont(new java.awt.Font("Monospaced", 0, 14));
		jLabel.setBackground(SystemColor.control);

		this.setSize(new Dimension(50, 17));
		this.setBorder(BorderFactory.createEmptyBorder());
		this.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(FocusEvent e) {
				dialog_.getCellsEditor().updateActiveColumnIndex();
				jComboBoxYear.requestFocus();
			}
		});
		this.setLayout(new BorderLayout());
		this.add(jComboBoxYear, BorderLayout.CENTER);
	}
	
	public void setHorizontalAlignment(int alignment) {
		jLabel.setHorizontalAlignment(alignment);
	}
	
	public void setEditable(boolean isEditable) {
		isEditable_ = isEditable;
		this.removeAll();
		if (isEditable) {
			this.add(jComboBoxYear, BorderLayout.CENTER);
		} else {
			jLabel.setText(this.getExternalValue().toString());
			this.add(jLabel, BorderLayout.CENTER);
		}
	}
	
	public boolean isEditable() {
		return isEditable_;
	}

	public Object getInternalValue() {
		String year = (String)jComboBoxYear.getItemAt(jComboBoxYear.getSelectedIndex());
		return year;
	}

	public Object getExternalValue() {
		return this.getInternalValue();
	}
	
	public void setValue(Object obj) {
		String value = (String)obj;
		if (value != null) {
			value = value.trim();
		}
		if (value == null || value.equals("")) {
			jComboBoxYear.setSelectedIndex(0);
		} else {
			if (value.length() == 4) {
				for (int i = 0; i < jComboBoxYear.getItemCount(); i++) {
					if (jComboBoxYear.getItemAt(i).equals(value)) {
						jComboBoxYear.setSelectedIndex(i);
						break;
					}
				}
			}
		}
	}

	public void setBackground(Color color) {
		if (jComboBoxYear != null) {
			jComboBoxYear.setBackground(color);
		}
	}
	
	public void setColorOfError() {
		this.setBackground(XFUtility.ERROR_COLOR);
	}
	
	public void setColorOfNormal(int row) {
		if (this.isEditable()) {
			if (row%2==0) {
				this.setBackground(SystemColor.text);
			} else {
				this.setBackground(XFUtility.ODD_ROW_COLOR);
			}
		} else {
			this.setBackground(SystemColor.control);
		}
	}
}

class XF310_CellEditorWithMSeqBox extends JPanel implements XFTableColumnEditor {
	private static final long serialVersionUID = 1L;
	private JComboBox jComboBoxMSeq = new JComboBox();
	private ArrayList<Integer> listMSeq = new ArrayList<Integer>();
	private JLabel jLabel = new JLabel();
	private boolean isEditable_ = true;
	private XF310 dialog_ = null;
    private String language = "";
    private String[] monthArrayEn = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec","Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov"};
    private String[] monthArrayJp = {"�P���x","�Q���x","�R���x","�S���x","�T���x","�U���x","�V���x","�W���x","�X���x","10���x","11���x","12���x","�P���x","�Q���x","�R���x","�S���x","�T���x","�U���x","�V���x","�W���x","�X���x","10���x","11���x"};
    private int startMonth = 1;

	public XF310_CellEditorWithMSeqBox(XF310 dialog) {
		super();
		dialog_ = dialog;

		language = dialog_.getSession().getDateFormat().substring(0, 2);
		startMonth = dialog_.getSession().getSystemVariantInteger("FIRST_MONTH");

		jComboBoxMSeq.setFont(new java.awt.Font("Dialog", 0, 12));
		jComboBoxMSeq.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuCanceled(PopupMenuEvent arg0) {
			}
			public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
				dialog_.getCellsEditor().updateRowObject();
			}
			public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
			}
		});
		listMSeq.add(0);
		listMSeq.add(1);
		listMSeq.add(2);
		listMSeq.add(3);
		listMSeq.add(4);
		listMSeq.add(5);
		listMSeq.add(6);
		listMSeq.add(7);
		listMSeq.add(8);
		listMSeq.add(9);
		listMSeq.add(10);
		listMSeq.add(11);
		listMSeq.add(12);
		if (language.equals("en")) {
			jComboBoxMSeq.setBounds(new Rectangle(0, 0, 50, XFUtility.FIELD_UNIT_HEIGHT));
			this.setSize(new Dimension(50, XFUtility.FIELD_UNIT_HEIGHT));
			jComboBoxMSeq.addItem("");
			for (int i = startMonth -1; i < startMonth + 11; i++) {
				jComboBoxMSeq.addItem(monthArrayEn[i]);
			}
		}
		if (language.equals("jp")) {
			jComboBoxMSeq.setBounds(new Rectangle(0, 0, 62, XFUtility.FIELD_UNIT_HEIGHT));
			this.setSize(new Dimension(62, XFUtility.FIELD_UNIT_HEIGHT));
			jComboBoxMSeq.addItem("");
			for (int i = startMonth -1; i < startMonth + 11; i++) {
				jComboBoxMSeq.addItem(monthArrayJp[i]);
			}
		}
		jLabel.setFont(new java.awt.Font("Monospaced", 0, 14));
		jLabel.setBackground(SystemColor.control);

		this.setBorder(BorderFactory.createEmptyBorder());
		this.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(FocusEvent e) {
				dialog_.getCellsEditor().updateActiveColumnIndex();
				jComboBoxMSeq.requestFocus();
			}
		});
		this.setLayout(new BorderLayout());
		this.add(jComboBoxMSeq, BorderLayout.CENTER);
	}
	
	public void setHorizontalAlignment(int alignment) {
		jLabel.setHorizontalAlignment(alignment);
	}
	
	public void setEditable(boolean isEditable) {
		isEditable_ = isEditable;
		this.removeAll();
		if (isEditable) {
			this.add(jComboBoxMSeq, BorderLayout.CENTER);
		} else {
			jLabel.setText(this.getExternalValue().toString());
			this.add(jLabel, BorderLayout.CENTER);
		}
	}
	
	public boolean isEditable() {
		return isEditable_;
	}

	public Object getInternalValue() {
		return listMSeq.get(jComboBoxMSeq.getSelectedIndex());
	}

	public Object getExternalValue() {
		return (String)jComboBoxMSeq.getItemAt(jComboBoxMSeq.getSelectedIndex());
	}
	
	public void setValue(Object obj) {
		int value = 0;
		try {
			value = Integer.parseInt(obj.toString());
		} catch (NumberFormatException e) {
		}
		for (int i = 0; i < listMSeq.size(); i++) {
			if (listMSeq.get(i) == value) {
				jComboBoxMSeq.setSelectedIndex(i);
				break;
			}
		}
	}

	public void setBackground(Color color) {
		if (jComboBoxMSeq != null) {
			jComboBoxMSeq.setBackground(color);
		}
	}
	
	public void setColorOfError() {
		this.setBackground(XFUtility.ERROR_COLOR);
	}
	
	public void setColorOfNormal(int row) {
		if (this.isEditable()) {
			if (row%2==0) {
				this.setBackground(SystemColor.text);
			} else {
				this.setBackground(XFUtility.ODD_ROW_COLOR);
			}
		} else {
			this.setBackground(SystemColor.control);
		}
	}
}

class XF310_CellEditorWithCheckBox extends XFCheckBox implements XFTableColumnEditor {
	private static final long serialVersionUID = 1L;
	private XF310 dialog_ = null;

	public XF310_CellEditorWithCheckBox(String dataTypeOptions, XF310 dialog) {
		super(dataTypeOptions);
		dialog_ = dialog;
		this.setOpaque(true);
		this.setBorderPainted(true);
		this.setHorizontalAlignment(SwingConstants.CENTER);
		this.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(FocusEvent e) {
				dialog_.getCellsEditor().updateActiveColumnIndex();
			}
		});
		this.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialog_.getCellsEditor().updateRowObject();
			}
		});
	}

	public boolean isCellEditable(EventObject event) {
		return true;
	} 

	public Object getInternalValue() {
		return super.getInternalValue();
	}
	
	public void setValue(Object obj) {
		super.setValue(obj);
	}
	
	public void setColorOfError() {
		this.setBackground(XFUtility.ERROR_COLOR);
	}
	
	public void setColorOfNormal(int row) {
		if (this.isEditable()) {
			if (row%2==0) {
				this.setBackground(SystemColor.text);
			} else {
				this.setBackground(XFUtility.ODD_ROW_COLOR);
			}
		} else {
			this.setBackground(SystemColor.control);
		}
	}
}            

class XF310_CellEditorWithComboBox extends JPanel implements XFTableColumnEditor {
	private static final long serialVersionUID = 1L;
	private String dataTypeOptions_ = "";
	private String tableAlias = "";
	private String fieldID = "";
	private String listType = "";
	private JComboBox jComboBox = new JComboBox();
	private JLabel jLabel = new JLabel();
	private boolean isEditable_ = true;
	private ArrayList<String> kubunKeyValueList = new ArrayList<String>();
	private ArrayList<XFHashMap> tableKeyValuesList = new ArrayList<XFHashMap>();
	private ArrayList<String> keyFieldList = new ArrayList<String>();
	private XF310_DetailReferTable referTable_ = null;
	private XF310 dialog_;

	public XF310_CellEditorWithComboBox(String dataSourceName, String dataTypeOptions, XF310 dialog, XF310_DetailReferTable referTable, boolean isNullable) {
		super();
		StringTokenizer workTokenizer;
		String wrk = "";
		String strWrk;
		
		dataTypeOptions_ = dataTypeOptions;
		workTokenizer = new StringTokenizer(dataSourceName, "." );
		tableAlias = workTokenizer.nextToken();
		referTable_ = referTable;
		fieldID =workTokenizer.nextToken();
		dialog_ = dialog;

		strWrk = XFUtility.getOptionValueWithKeyword(dataTypeOptions_, "VALUES");
		if (!strWrk.equals("")) {
			listType = "VALUES_LIST";
			if (isNullable) {
				jComboBox.addItem("");
			}
			workTokenizer = new StringTokenizer(strWrk, ";" );
			while (workTokenizer.hasMoreTokens()) {
				wrk = workTokenizer.nextToken();
				jComboBox.addItem(wrk);
			}
		} else {
			strWrk = XFUtility.getOptionValueWithKeyword(dataTypeOptions_, "KUBUN");
			if (!strWrk.equals("")) {
				listType = "KUBUN_LIST";
				if (isNullable) {
					kubunKeyValueList.add("");
					jComboBox.addItem("");
				}
				try {
					XFTableOperator operator = dialog_.createTableOperator("Select", dialog_.getSession().getTableNameOfUserVariants());
					operator.addKeyValue("IDUSERKUBUN", strWrk);
					operator.setOrderBy("SQLIST");
					while (operator.next()) {
						kubunKeyValueList.add(operator.getValueOf("KBUSERKUBUN").toString().trim());
						jComboBox.addItem(operator.getValueOf("TXUSERKUBUN").toString().trim());
					}
					if (jComboBox.getItemCount() == 0) {
						JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("FunctionError24") + dataSourceName + XFUtility.RESOURCE.getString("FunctionError25"));
					}
				} catch (Exception e) {
					e.printStackTrace(dialog_.getExceptionStream());
					dialog_.setErrorAndCloseFunction();
				}
			} else {
				if (referTable_ != null) {
					listType = "RECORDS_LIST";
					keyFieldList = referTable_.getKeyFieldIDList();
				}
			}
		}

		jComboBox.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuCanceled(PopupMenuEvent arg0) {
			}
			public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
				dialog_.getCellsEditor().updateRowObject();
			}
			public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
				setupRecordList();
			}
		});
		jLabel.setFont(new java.awt.Font("Monospaced", 0, 14));
		jLabel.setBackground(SystemColor.control);

		this.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(FocusEvent e) {
				dialog_.getCellsEditor().updateActiveColumnIndex();
				jComboBox.requestFocus();
			}
		});
		this.setLayout(new BorderLayout());
		this.add(jComboBox, BorderLayout.CENTER);
	}
	
	public void setEditable(boolean isEditable) {
		isEditable_ = isEditable;
		this.removeAll();
		if (isEditable) {
			this.add(jComboBox, BorderLayout.CENTER);
		} else {
			this.add(jLabel, BorderLayout.CENTER);
		}
	}
	
	public boolean isEditable() {
		return isEditable_;
	}
	
	public void setHorizontalAlignment(int alignment) {
		jLabel.setHorizontalAlignment(alignment);
	}
	
	public boolean hasEditControlledKey() {
		boolean anyOfKeysAreEditControlled = false;
		for (int i = 0; i < referTable_.getWithKeyFieldIDList().size(); i++) {
			for (int j = 0; j < dialog_.getDetailColumnList().size(); j++) {
				if (referTable_.getWithKeyFieldIDList().get(i).equals(dialog_.getDetailColumnList().get(j).getTableAlias() + "." + dialog_.getDetailColumnList().get(j).getFieldID())) {
					if (!dialog_.getDetailColumnList().get(j).isEditable() && dialog_.getDetailTable().getTableID().equals(dialog_.getDetailColumnList().get(j).getTableAlias())) {
						anyOfKeysAreEditControlled = true;
						break;
					}
				}
			}
		}
		return anyOfKeysAreEditControlled;
	}

	public void setupRecordList() {
		if (referTable_ != null && listType.equals("RECORDS_LIST")) {
			try {
				setCursor(new Cursor(Cursor.WAIT_CURSOR));

				String selectedItemValue = "";
				if (jComboBox.getSelectedIndex() >= 0) {
					selectedItemValue = jComboBox.getItemAt(jComboBox.getSelectedIndex()).toString();
				}

				tableKeyValuesList.clear();
				jComboBox.removeAllItems();

				boolean blankItemRequired = false;
				XFHashMap blankKeyValues = new XFHashMap();
				for (int i = 0; i < referTable_.getWithKeyFieldIDList().size(); i++) {
					for (int j = 0; j < dialog_.getDetailColumnList().size(); j++) {
						if (referTable_.getWithKeyFieldIDList().get(i).equals(dialog_.getDetailColumnList().get(j).getTableAlias() + "." + dialog_.getDetailColumnList().get(j).getFieldID())) {
							if (dialog_.getDetailColumnList().get(j).isNullable()) {
								blankItemRequired = true;
								//blankKeyValues.addValue(referTable_.getWithKeyFieldIDList().get(i), dialog_.getDetailColumnList().get(j).getNullValue());
								if (dialog_.getDetailColumnList().get(j).isVisibleOnPanel()) {
									blankKeyValues.addValue(referTable_.getWithKeyFieldIDList().get(i), dialog_.getDetailColumnList().get(j).getValue());
								} else {
									blankKeyValues.addValue(referTable_.getWithKeyFieldIDList().get(i), dialog_.getDetailColumnList().get(j).getNullValue());
								}
							} else {
								blankKeyValues.addValue(referTable_.getWithKeyFieldIDList().get(i), dialog_.getDetailColumnList().get(j).getValue());
							}
						}
					}
				}
				if (blankItemRequired) {
					tableKeyValuesList.add(blankKeyValues);
					jComboBox.addItem("");
				}

				String wrk;
				XFHashMap keyValues;
				XFTableOperator operator = dialog_.createTableOperator(referTable_.getSelectSQL(true));
				while (operator.next()) {
					keyValues = new XFHashMap();
					for (int i = 0; i < keyFieldList.size(); i++) {
						keyValues.addValue(referTable_.getWithKeyFieldIDList().get(i), operator.getValueOf(keyFieldList.get(i)));
					}
					wrk = operator.getValueOf(fieldID).toString().trim();
					keyValues.addValue(tableAlias + "." + fieldID, wrk);
					tableKeyValuesList.add(keyValues);
					jComboBox.addItem(wrk);
				}

				jComboBox.setSelectedItem(selectedItemValue);
				this.updateUI();

			} catch (Exception e) {
				e.printStackTrace(dialog_.getExceptionStream());
				dialog_.setErrorAndCloseFunction();
			} finally {
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		}
	}

	public Object getInternalValue() {
		Object value = "";
		if (listType.equals("VALUES_LIST")) {
			value = jComboBox.getItemAt(jComboBox.getSelectedIndex()).toString();
		}
		if (listType.equals("KUBUN_LIST")) {
			value = kubunKeyValueList.get(jComboBox.getSelectedIndex());
		}
		if (listType.equals("RECORDS_LIST")) {
			if (tableKeyValuesList.size() > 0) {
				value = tableKeyValuesList.get(jComboBox.getSelectedIndex());
			}
		}
		return value;
	}

	public Object getExternalValue() {
		return jComboBox.getItemAt(jComboBox.getSelectedIndex()).toString();
	}
	
	public void setValue(Object obj) {
		String value = (String)obj;
		value = value.trim();
		if (listType.equals("VALUES_LIST")) {
			for (int i = 0; i < jComboBox.getItemCount(); i++) {
				if (jComboBox.getItemAt(i).toString().equals(value)) {
					jComboBox.setSelectedIndex(i);
					break;
				}
			}
		}
		if (listType.equals("KUBUN_LIST")) {
			for (int i = 0; i < kubunKeyValueList.size(); i++) {
				if (kubunKeyValueList.get(i).equals(value)) {
					jComboBox.setSelectedIndex(i);
					break;
				}
			}
		}
		if (listType.equals("RECORDS_LIST")) {
			if (jComboBox.getItemCount() == 0) {
				setupRecordList();
			}
			if (value == null || value.equals("")) {
			} else {
				for (int i = 0; i < jComboBox.getItemCount(); i++) {
					if (jComboBox.getItemAt(i).toString().equals(value)) {
						jComboBox.setSelectedIndex(i);
						break;
					}
				}
			}
		}
		if (jComboBox.getSelectedIndex() >= 0) {
			jLabel.setText(this.getExternalValue().toString());
		}
	}
	
	public void setColorOfError() {
		jComboBox.setBackground(XFUtility.ERROR_COLOR);
	}
	
	public void setColorOfNormal(int row) {
		if (row%2==0) {
			jComboBox.setBackground(SystemColor.text);
		} else {
			jComboBox.setBackground(XFUtility.ODD_ROW_COLOR);
		}
	}
}

class XF310_CellEditorWithPromptCall extends JPanel implements XFTableColumnEditor {
	private static final long serialVersionUID = 1L;
	private JTextField jTextField;
	private JButton jButton = new JButton();
    private XF310 dialog_;
    private String functionID_;
    private org.w3c.dom.Element fieldElement_;
    private ArrayList<String> fieldsToPutList_ = new ArrayList<String>();
    private ArrayList<String> fieldsToPutToList_ = new ArrayList<String>();
    private ArrayList<String> fieldsToGetList_ = new ArrayList<String>();
    private ArrayList<String> fieldsToGetToList_ = new ArrayList<String>();

	public XF310_CellEditorWithPromptCall(org.w3c.dom.Element fieldElement, String functionID, XF310 dialog){
		super();
		fieldElement_ = fieldElement;
		functionID_ = functionID;
		dialog_ = dialog;

		jTextField = new JTextField();
		jTextField.setOpaque(true);
		jTextField.setBorder(null);
		jTextField.setEditable(false);
		jTextField.setFont(new java.awt.Font("Monospaced", 0, 14));

		StringTokenizer workTokenizer;
		String fieldOptions = fieldElement_.getAttribute("FieldOptions");
		String wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "PROMPT_CALL_TO_PUT");
		if (!wrkStr.equals("")) {
			workTokenizer = new StringTokenizer(wrkStr, ";" );
			while (workTokenizer.hasMoreTokens()) {
				fieldsToPutList_.add(workTokenizer.nextToken());
			}
		}
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "PROMPT_CALL_TO_PUT_TO");
		if (!wrkStr.equals("")) {
			workTokenizer = new StringTokenizer(wrkStr, ";" );
			while (workTokenizer.hasMoreTokens()) {
				fieldsToPutToList_.add(workTokenizer.nextToken());
			}
		}
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "PROMPT_CALL_TO_GET");
		if (!wrkStr.equals("")) {
			workTokenizer = new StringTokenizer(wrkStr, ";" );
			while (workTokenizer.hasMoreTokens()) {
				fieldsToGetList_.add(workTokenizer.nextToken());
			}
		}
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "PROMPT_CALL_TO_GET_TO");
		if (!wrkStr.equals("")) {
			workTokenizer = new StringTokenizer(wrkStr, ";" );
			while (workTokenizer.hasMoreTokens()) {
				fieldsToGetToList_.add(workTokenizer.nextToken());
			}
		}

		ImageIcon imageIcon = new ImageIcon(xeadDriver.XF310.class.getResource("prompt.png"));
	 	jButton.setIcon(imageIcon);
		jButton.setPreferredSize(new Dimension(26, XFUtility.FIELD_UNIT_HEIGHT));
		jButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object value;
				try {
					setCursor(new Cursor(Cursor.WAIT_CURSOR));

					HashMap<String, Object> parmValueMap = new HashMap<String, Object>();
					HashMap<String, Object> columnValueMap = dialog_.getCellsEditor().getActiveRowObject().getColumnValueMap();
					for (int i = 0; i < fieldsToPutList_.size(); i++) {
						value = columnValueMap.get(fieldsToPutList_.get(i));
						if (value != null) {
							parmValueMap.put(fieldsToPutToList_.get(i), value);
						}
					}
					if (parmValueMap.size() < fieldsToPutList_.size()) {
						for (int i = 0; i < fieldsToPutList_.size(); i++) {
							value = dialog_.getValueOfHeaderFieldByName(fieldsToPutList_.get(i));
							if (value != null) {
								parmValueMap.put(fieldsToPutToList_.get(i), value);
							}
						}
					}

					HashMap<String, Object> returnMap = dialog_.getSession().executeFunction(functionID_, parmValueMap);
					if (!returnMap.get("RETURN_CODE").equals("99")) {
						HashMap<String, Object> fieldsToGetMap = new HashMap<String, Object>();
						for (int i = 0; i < fieldsToGetList_.size(); i++) {
							value = returnMap.get(fieldsToGetList_.get(i));
							if (value != null) {
								fieldsToGetMap.put(fieldsToGetToList_.get(i), value);
							}
						}
						String dataSourceName;
						for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
							dataSourceName = dialog_.getDetailColumnList().get(i).getDataSourceName();
							if (fieldsToGetMap.containsKey(dataSourceName) && columnValueMap.containsKey(dataSourceName)) {
								columnValueMap.put(dataSourceName, fieldsToGetMap.get(dataSourceName));
							}
						}
						for (int i = 0; i < dialog_.getCellsEditor().getCellList().size(); i++) {
							dataSourceName = dialog_.getDetailColumnList().get(i).getDataSourceName();
							if (fieldsToGetMap.containsKey(dataSourceName)) {
								dialog_.getCellsEditor().getCellList().get(i).setValue(fieldsToGetMap.get(dataSourceName));
							}
						}

					}
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(null, ex.getMessage());
				} finally {
					setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
			}
		});
		jButton.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(KeyEvent e)  {
			    if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0){
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						jButton.doClick();
					}
				}
				if (e.getKeyCode() == KeyEvent.VK_TAB) {
					e.setSource(dialog_.getJTableMain());
					dialog_.getJTableMain().dispatchEvent(e);
				}
			}
		});

		this.setLayout(new BorderLayout());
		this.setFocusable(true);
		this.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(FocusEvent e) {
				dialog_.getCellsEditor().updateActiveColumnIndex();
				jButton.requestFocus();
			}
		});
		this.add(jTextField, BorderLayout.CENTER);
		this.add(jButton, BorderLayout.EAST);
	}
	
	public void setHorizontalAlignment(int alignment) {
	}
	
	public void setEditable(boolean isEditable) {
		jButton.setEnabled(isEditable);
	}
	
	public boolean isEditable() {
		return jButton.isEnabled();
	}
	
	public boolean hasEditControlledKey() {
		boolean anyOfKeysAreEditControlled = false;
		for (int i = 0; i < fieldsToGetToList_.size(); i++) {
			for (int j = 0; j < dialog_.getDetailColumnList().size(); j++) {
				if (fieldsToGetToList_.get(i).equals(dialog_.getDetailColumnList().get(j).getTableAlias() + "." + dialog_.getDetailColumnList().get(j).getFieldID())) {
					if (!dialog_.getDetailColumnList().get(j).isEditable() && dialog_.getDetailTable().getTableID().equals(dialog_.getDetailColumnList().get(j).getTableAlias())) {
						anyOfKeysAreEditControlled = true;
						break;
					}
				}
			}
		}
		return anyOfKeysAreEditControlled;
	}

	public Object getInternalValue() {
		return jTextField.getText();
	}

	public Object getExternalValue() {
		return jTextField.getText();
	}
	
	public void setValue(Object obj) {
		if (obj == null) {
			jTextField.setText("");
		} else {
			jTextField.setText(obj.toString());
		}
	}
	
	public void setColorOfError() {
		jTextField.setBackground(XFUtility.ERROR_COLOR);
	}
	
	public void setColorOfNormal(int row) {
		if (jButton.isEnabled()) {
			if (row%2==0) {
				jTextField.setBackground(SystemColor.text);
			} else {
				jTextField.setBackground(XFUtility.ODD_ROW_COLOR);
			}
		} else {
			jTextField.setBackground(SystemColor.control);
		}
	}
}


@SuppressWarnings("unchecked")
class XF310_DetailRowNumber extends Object {
	private static final long serialVersionUID = 1L;
	private int number_;
	private String recordType_ = "";
	private HashMap<String, Object> keyValueMap_;
	private HashMap<String, Object> columnValueMapWithDSName_;
	private HashMap<String, Object> columnOldValueMapWithDSName_;
	private ArrayList<Integer> errorCellIndexList = new ArrayList<Integer>();
	private XF310 dialog_ = null;

	public XF310_DetailRowNumber(int num, String recordType, HashMap<String, Object> keyValueMap, HashMap<String, Object> columnValueMap, XF310 dialog) {
		number_ = num;
		keyValueMap_ = keyValueMap;
		recordType_ = recordType;
		columnValueMapWithDSName_ = columnValueMap;
		columnOldValueMapWithDSName_ = (HashMap<String, Object>)columnValueMapWithDSName_.clone();
		dialog_ = dialog;
	}

	public String getRecordType() {
		return recordType_;
	}

	public HashMap<String, Object> getKeyValueMap() {
		return keyValueMap_;
	}
	
	public HashMap<String, Object> getColumnValueMap() {
		return columnValueMapWithDSName_;
	}
	
	public HashMap<String, Object> getColumnValueMapWithFieldID() {
		HashMap<String, Object> valueMapWithFieldID = new HashMap<String, Object>();
		for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
			if (dialog_.getDetailColumnList().get(i).getTableAlias().equals(dialog_.getDetailTable().getTableID())) {
				if (columnValueMapWithDSName_.containsKey(dialog_.getDetailColumnList().get(i).getDataSourceName())) {
					valueMapWithFieldID.put(dialog_.getDetailColumnList().get(i).getFieldID(), columnValueMapWithDSName_.get(dialog_.getDetailColumnList().get(i).getDataSourceName()));
				}
			}
		}
		return valueMapWithFieldID;
	}
	
	public HashMap<String, Object> getColumnOldValueMap() {
		return columnOldValueMapWithDSName_;
	}
	
	public HashMap<String, Object> getColumnOldValueMapWithFieldID() {
		HashMap<String, Object> oldValueMapWithFieldID = new HashMap<String, Object>();
		for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
			if (dialog_.getDetailColumnList().get(i).getTableAlias().equals(dialog_.getDetailTable().getTableID())) {
				if (columnOldValueMapWithDSName_.containsKey(dialog_.getDetailColumnList().get(i).getDataSourceName())) {
					oldValueMapWithFieldID.put(dialog_.getDetailColumnList().get(i).getFieldID(), columnOldValueMapWithDSName_.get(dialog_.getDetailColumnList().get(i).getDataSourceName()));
				}
			}
		}
		return oldValueMapWithFieldID;
	}
	
	public void setRecordType(String type) {
		recordType_ = type;
	}
	
	public void setValuesToDetailColumns() {
		for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
			dialog_.getDetailColumnList().get(i).setValue(columnValueMapWithDSName_.get(dialog_.getDetailColumnList().get(i).getDataSourceName()));
			dialog_.getDetailColumnList().get(i).setOldValue(columnOldValueMapWithDSName_.get(dialog_.getDetailColumnList().get(i).getDataSourceName()));
		}
	}
	
	public int countErrors(ArrayList<String> messageList) {
		int countOfErrors = 0;
		String event = "";
		if (recordType_.equals("NEW")) {
			event = "BC";
		}
		if (recordType_.equals("CURRENT")) {
			event = "BU";
		}

		resetErrors();
		for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
			dialog_.getDetailColumnList().get(i).setEditable(true);
			dialog_.getDetailColumnList().get(i).setError(false);
		}

		countOfErrors = dialog_.fetchDetailReferRecords(event, true, "", columnValueMapWithDSName_, columnOldValueMapWithDSName_);

		////////////////////////////
		// Check Null-Constraints //
		////////////////////////////
		for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
			columnValueMapWithDSName_.put(dialog_.getDetailColumnList().get(i).getDataSourceName(), dialog_.getDetailColumnList().get(i).getInternalValue());
			if (dialog_.getDetailColumnList().get(i).getTableAlias().equals(dialog_.getDetailTable().getTableID())
					&& dialog_.getDetailColumnList().get(i).isVisibleOnPanel()
					&& dialog_.getDetailColumnList().get(i).isEditable()) {
				if (dialog_.getDetailColumnList().get(i).isNullError(columnValueMapWithDSName_.get(dialog_.getDetailColumnList().get(i).getDataSourceName()))) {
					countOfErrors++;
				}
			}
		}

		////////////////////////
		// Set Error on Cells //
		////////////////////////
		int rowNumber;
		for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
			if (dialog_.getDetailColumnList().get(i).isError()) {
				if (dialog_.getDetailColumnList().get(i).isVisibleOnPanel() && dialog_.getDetailColumnList().get(i).isEditable()) {
					setErrorOnCellAt(i);
				}
				rowNumber = this.getRowIndex() + 1;
				messageList.add(dialog_.getDetailColumnList().get(i).getCaption() + XFUtility.RESOURCE.getString("LineNumber1") + rowNumber + XFUtility.RESOURCE.getString("LineNumber2") + dialog_.getDetailColumnList().get(i).getError());
			}
		}

		return countOfErrors;
	}

	public void resetErrors() {
		errorCellIndexList.clear();
	}
	
	public void setErrorOnCellAt(int index) {
		errorCellIndexList.add(index);
	}
	
	public int getFirstErrorCellIndex() {
		if (errorCellIndexList.size() > 0) {
			return errorCellIndexList.get(0);
		} else {
			return -1;
		}
	}
	
	public int getRowIndex() {
		int index = -1;
		XF310_DetailRowNumber rowNumber = null;
		for (int i = 0; i < dialog_.getTableModel().getRowCount(); i++) {
			rowNumber = (XF310_DetailRowNumber)dialog_.getTableModel().getValueAt(i, 0);
			if (rowNumber.equals(this)) {
				index = i;
			}
		}
		return index;
	}
	
	public void setNumber(int num) {
		number_ = num;
	}
	
	public ArrayList<Integer> getErrorCellIndexList() {
		return errorCellIndexList;
	}
	
	public String getRowNumberString() {
		return Integer.toString(number_);
	}
}

class XF310_DetailColumn extends XFColumnScriptable {
	private static final long serialVersionUID = 1L;
	private org.w3c.dom.Element functionColumnElement_ = null;
	private org.w3c.dom.Element tableElement = null;
	private XF310 dialog_ = null;
	private String tableID_ = "";
	private String tableAlias_ = "";
	private String fieldID_ = "";
	private String fieldName = "";
	private String fieldRemarks = "";
	private String dataType = "";
	private int dataSize = 5;
	private int decimalSize = 0;
	private String dataTypeOptions = "";
	private ArrayList<String> dataTypeOptionList;
	private ArrayList<String> fieldOptionList;
	private String fieldOptions = "";
	private String fieldCaption = "";
	private int fieldWidth = 50;
	private int columnIndex = -1;
	private boolean isKey = false;
	private boolean isNullable = true;
	private boolean isFieldOnDetailTable = false;
	private boolean isVisibleOnPanel = true;
	private boolean isVirtualField = false;
	private boolean isEditable = true;
	private boolean isNonEditableField = false;
	private boolean isRangeKeyFieldValid = false;
	private boolean isRangeKeyFieldExpire = false;
	private String valueType = "STRING";
	private String flagTrue = "";
	private ArrayList<String> kubunValueList = new ArrayList<String>();
	private ArrayList<String> kubunTextList = new ArrayList<String>();
	private ArrayList<String> additionalHiddenFieldList = new ArrayList<String>();
	private Object value_ = null;
	private Object oldValue_ = null;
	private Color foreground = Color.black;
	private XFTableColumnEditor editor = null;
	private boolean isError = false;
	private String errorMessage = "";
	private String numberingID = "";
	private int fieldRows = 1;
	private String fieldLayout = "HORIZONTAL";

	public XF310_DetailColumn(org.w3c.dom.Element functionColumnElement, XF310 dialog){
		super();
		String wrkStr;
		functionColumnElement_ = functionColumnElement;
		dialog_ = dialog;
		fieldOptions = functionColumnElement_.getAttribute("FieldOptions");
		fieldOptionList = XFUtility.getOptionList(fieldOptions);

		StringTokenizer workTokenizer = new StringTokenizer(functionColumnElement_.getAttribute("DataSource"), "." );
		tableAlias_ = workTokenizer.nextToken();
		tableID_ = dialog.getTableIDOfTableAlias(tableAlias_);
		fieldID_ =workTokenizer.nextToken();

		if (tableID_.equals(dialog_.getDetailTable().getTableID()) && tableID_.equals(tableAlias_)) {
			isFieldOnDetailTable = true;
			ArrayList<String> keyNameList = dialog_.getDetailTable().getKeyFieldIDList();
			for (int i = 0; i < keyNameList.size(); i++) {
				if (keyNameList.get(i).equals(fieldID_)) {
					isKey = true;
					isNonEditableField = true;
					break;
				}
			}
		} else {
			wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "PROMPT_CALL");
			if (!fieldOptionList.contains("PROMPT_LIST") && wrkStr.equals("")) {
				isNonEditableField = true;
			}
		}

		org.w3c.dom.Element workElement = dialog.getSession().getFieldElement(tableID_, fieldID_);
		if (workElement == null) {
			JOptionPane.showMessageDialog(null, tableID_ + "." + fieldID_ + XFUtility.RESOURCE.getString("FunctionError11"));
		}
		fieldName = workElement.getAttribute("Name");
		fieldRemarks = XFUtility.substringLinesWithTokenOfEOL(workElement.getAttribute("Remarks"), "<br>");
		dataType = workElement.getAttribute("Type");
		dataTypeOptions = workElement.getAttribute("TypeOptions");
		dataTypeOptionList = XFUtility.getOptionList(dataTypeOptions);
		if (workElement.getAttribute("Name").equals("")) {
			fieldCaption = workElement.getAttribute("ID");
		} else {
			fieldCaption = fieldName;
		}
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "CAPTION");
		if (!wrkStr.equals("")) {
			fieldCaption = XFUtility.getCaptionValue(wrkStr, dialog_.getSession());
		}
		dataSize = Integer.parseInt(workElement.getAttribute("Size"));
		if (!workElement.getAttribute("Decimal").equals("")) {
			decimalSize = Integer.parseInt(workElement.getAttribute("Decimal"));
		}
		if (workElement.getAttribute("Nullable").equals("F")) {
			if (!fieldOptionList.contains("PROMPT_LIST")) {
				isNullable = false;
			}
		}
		wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "AUTO_NUMBER");
		if (!wrkStr.equals("")) {
			numberingID = wrkStr;
			isNonEditableField = true;
		}

		tableElement = (org.w3c.dom.Element)workElement.getParentNode();
		if (!tableElement.getAttribute("RangeKey").equals("")) {
			workTokenizer = new StringTokenizer(tableElement.getAttribute("RangeKey"), ";" );
			if (workTokenizer.nextToken().equals(fieldID_)) {
				isRangeKeyFieldValid = true;
			}
			if (workTokenizer.nextToken().equals(fieldID_)) {
				isRangeKeyFieldExpire = true;
			}
		}

		if (dataTypeOptionList.contains("VIRTUAL")) {
			isVirtualField = true;
		}

		JLabel jLabel = new JLabel();
		FontMetrics metrics = jLabel.getFontMetrics(new java.awt.Font("Dialog", 0, 14));
		int captionWidth = metrics.stringWidth(fieldCaption) + 18;

		ArrayList<String> fieldOptionList = XFUtility.getOptionList(fieldOptions);
		if (fieldOptionList.contains("VERTICAL")) {
			fieldLayout = "VERTICAL";
		}

		String basicType = this.getBasicType();
		wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "BOOLEAN");
		if (!wrkStr.equals("")) {
			workTokenizer = new StringTokenizer(wrkStr, ";");
			if (workTokenizer.countTokens() >= 1) {
				flagTrue = workTokenizer.nextToken();
			}
			valueType = "FLAG";
			fieldWidth = 20;
			editor = new XF310_CellEditorWithCheckBox(dataTypeOptions, dialog_);

		} else {
			if (fieldOptionList.contains("PROMPT_LIST")) {
				XF310_DetailReferTable referTable = null;
				ArrayList<XF310_DetailReferTable> referTableList = dialog_.getDetailReferTableList();
				for (int i = 0; i < referTableList.size(); i++) {
					if (referTableList.get(i).getTableID().equals(tableID_)) {
						if (referTableList.get(i).getTableAlias().equals("") || referTableList.get(i).getTableAlias().equals(tableAlias_)) {
							referTable = referTableList.get(i);
							break;
						}
					}
				}
				if (dataTypeOptionList.contains("KANJI") || dataTypeOptionList.contains("ZIPADRS")) {
					fieldWidth = dataSize * 14 + 5;
				} else {
					fieldWidth = dataSize * 7 + 12;
				}
				editor = new XF310_CellEditorWithComboBox(functionColumnElement_.getAttribute("DataSource"), dataTypeOptions, dialog_, referTable, isNullable);

			} else {
				wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "PROMPT_CALL");
				if (!wrkStr.equals("")) {
					if (dataTypeOptionList.contains("KANJI") || dataTypeOptionList.contains("ZIPADRS")) {
						fieldWidth = dataSize * 14 + 5;
					} else {
						fieldWidth = dataSize * 7 + 15;
					}
					editor = new XF310_CellEditorWithPromptCall(functionColumnElement_, wrkStr, dialog_);
					wrkStr = XFUtility.getOptionValueWithKeyword(wrkStr, "PROMPT_CALL_TO_GET_TO");
					if (!wrkStr.equals("")) {
						workTokenizer = new StringTokenizer(wrkStr, ";" );
						while (workTokenizer.hasMoreTokens()) {
							additionalHiddenFieldList.add(workTokenizer.nextToken());
						}
					}
				
				} else {
					wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "KUBUN");
					if (!wrkStr.equals("")) {
						try {
							String wrk;
							XFTableOperator operator = dialog_.createTableOperator("Select", dialog_.getSession().getTableNameOfUserVariants());
							operator.addKeyValue("IDUSERKUBUN", wrkStr);
							operator.setOrderBy("SQLIST");
							while (operator.next()) {
								kubunValueList.add(operator.getValueOf("KBUSERKUBUN").toString().trim());
								wrk = operator.getValueOf("TXUSERKUBUN").toString().trim();
								if (metrics.stringWidth(wrk) + 20 > fieldWidth) {
									fieldWidth = metrics.stringWidth(wrk) + 20;
								}
								kubunTextList.add(wrk);
							}
							editor = new XF310_CellEditorWithComboBox(functionColumnElement_.getAttribute("DataSource"), dataTypeOptions, dialog_, null, isNullable);
						} catch (Exception e) {
							e.printStackTrace(dialog_.getExceptionStream());
							dialog_.setErrorAndCloseFunction();
						}

					} else {
						if ((dataTypeOptionList.contains("KANJI") || dataTypeOptionList.contains("ZIPADRS"))
								&& !dataType.equals("VARCHAR") && !dataType.equals("LONG VARCHAR")) {
							fieldWidth = dataSize * 14 + 5;
							editor = new XF310_CellEditorWithTextField(this, dialog_);
						
						} else {
							if (dataTypeOptionList.contains("YMONTH")) {
								fieldWidth = 85;
								editor = new XF310_CellEditorWithYMonthBox(dialog_);

							} else {
								if (dataTypeOptionList.contains("MSEQ")) {
									fieldWidth = 70;
									editor = new XF310_CellEditorWithMSeqBox(dialog_);

								} else {
									if (dataTypeOptionList.contains("FYEAR")) {
										fieldWidth = 85;
										editor = new XF310_CellEditorWithFYearBox(dialog_);

									} else {
										if (basicType.equals("INTEGER") || basicType.equals("FLOAT")) {
											fieldWidth = XFUtility.getLengthOfEdittedNumericValue(dataSize, decimalSize, dataTypeOptionList) * 7 + 21;
											editor = new XF310_CellEditorWithTextField(this, dialog_);

										} else {
											if (basicType.equals("DATE")) {
												fieldWidth = XFUtility.getWidthOfDateValue(dialog_.getSession().getDateFormat(), 14);
												editor = new XF310_CellEditorWithDateField(dialog_);

											} else {
												if (dataTypeOptionList.contains("IMAGE")) {
													valueType = "IMAGE";
													fieldWidth = 60;
													fieldRows = 2;
													editor = new XF310_CellEditorWithImageField(dialog_);

												} else {
													if (dataType.equals("VARCHAR") || dataType.equals("LONG VARCHAR")) {
														fieldWidth = 320;
														editor = new XF310_CellEditorWithLongTextEditor(fieldCaption, dataTypeOptionList, dialog_);

													} else {
														fieldWidth = dataSize * 7 + 15;
														editor = new XF310_CellEditorWithTextField(this, dialog_);
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}

		isEditable = !isNonEditableField;

		if (fieldWidth > 320) {
			fieldWidth = 320;
		}
		if (captionWidth > fieldWidth) {
			fieldWidth = captionWidth;
		}
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "WIDTH");
		if (!wrkStr.equals("")) {
			fieldWidth = Integer.parseInt(wrkStr);
		}
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "ROWS");
		if (!wrkStr.equals("")) {
			fieldRows = Integer.parseInt(wrkStr);
		}

		if (!dialog_.getEngineScriptBindings().containsKey(this.getFieldIDInScript())) {
			dialog_.getEngineScriptBindings().put(this.getFieldIDInScript(), this);
		}
	}

	public XF310_DetailColumn(String tableID, String tableAlias, String fieldID, XF310 dialog){
		super();
		functionColumnElement_ = null;
		dialog_ = dialog;
		fieldOptions = "";
		fieldOptionList = new ArrayList<String>();
		isVisibleOnPanel = false;

		tableID_ = tableID;
		if (tableAlias.equals("")) {
			tableAlias_ = tableID;
		} else {
			tableAlias_ = tableAlias;
		}
		fieldID_ = fieldID;

		if (tableID_.equals(dialog_.getDetailTable().getTableID()) && tableID_.equals(tableAlias_)) {
			isFieldOnDetailTable = true;
			ArrayList<String> keyNameList = dialog_.getDetailTable().getKeyFieldIDList();
			for (int i = 0; i < keyNameList.size(); i++) {
				if (keyNameList.get(i).equals(fieldID_)) {
					isKey = true;
					isNonEditableField = true;
					break;
				}
			}
		} else {
			isNonEditableField = true;
		}

		org.w3c.dom.Element workElement = dialog.getSession().getFieldElement(tableID_, fieldID_);
		if (workElement == null) {
			JOptionPane.showMessageDialog(null, tableID_ + "." + fieldID_ + XFUtility.RESOURCE.getString("FunctionError11"));
		}
		dataType = workElement.getAttribute("Type");
		dataTypeOptions = workElement.getAttribute("TypeOptions");
		dataTypeOptionList = XFUtility.getOptionList(dataTypeOptions);
		if (workElement.getAttribute("Name").equals("")) {
			fieldCaption = workElement.getAttribute("ID");
		} else {
			fieldCaption = workElement.getAttribute("Name");
		}
		dataSize = Integer.parseInt(workElement.getAttribute("Size"));
		if (workElement.getAttribute("Nullable").equals("F")) {
			isNullable = false;
		}

		tableElement = (org.w3c.dom.Element)workElement.getParentNode();
		if (!tableElement.getAttribute("RangeKey").equals("")) {
			StringTokenizer workTokenizer = new StringTokenizer(tableElement.getAttribute("RangeKey"), ";" );
			if (workTokenizer.nextToken().equals(fieldID_)) {
				isRangeKeyFieldValid = true;
			}
			if (workTokenizer.nextToken().equals(fieldID_)) {
				isRangeKeyFieldExpire = true;
			}
		}

		String wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "AUTO_NUMBER");
		if (!wrkStr.equals("")) {
			numberingID = wrkStr;
			isNonEditableField = true;
		}

		if (dataTypeOptionList.contains("VIRTUAL")) {
			isVirtualField = true;
		}

		isEditable = !isNonEditableField;

		if (!dialog_.getEngineScriptBindings().containsKey(this.getFieldIDInScript())) {
			dialog_.getEngineScriptBindings().put(this.getFieldIDInScript(), this);
		}
	}
	
	public ArrayList<String> getAdditionalHiddenFieldList() {
		return additionalHiddenFieldList;
	}

	public void checkPromptKeyEdit() {
		if (!XFUtility.getOptionValueWithKeyword(fieldOptions, "PROMPT_CALL").equals("")
				&& !isNonEditableField) {
			if (((XF310_CellEditorWithPromptCall)editor).hasEditControlledKey()) {
				this.setEditable(false);
			}
		}
		if (fieldOptionList.contains("PROMPT_LIST")
				&& !isNonEditableField) {
			if (((XF310_CellEditorWithComboBox)editor).hasEditControlledKey()) {
				this.setEditable(false);
			}
		}
	}
	
	public boolean isNullError(Object object){
		String basicType = this.getBasicType();
		String strWrk;
		boolean isNullError = false;
		if (basicType.equals("INTEGER")) {
			if (object.equals("")) {
				object = "0";
			}
			long value = Long.parseLong(object.toString());
			if (!this.isNullable) {
				if (value == 0) {
					isNullError = true;
				}
			}
		}
		if (basicType.equals("FLOAT")) {
			if (object.equals("")) {
				object = "0.0";
			}
			double value = Double.parseDouble(object.toString());
			if (!this.isNullable) {
				if (value == 0) {
					isNullError = true;
				}
			}
		}
		if (basicType.equals("DATE")) {
			if ((object == null || object.equals("")) && !this.isNullable) {
				isNullError = true;
			}
		}
		if (basicType.equals("STRING")) {
			strWrk = "";
			if (object != null) {
				strWrk = object.toString();
			}
			if (strWrk.equals("") && !this.isNullable) {
				isNullError = true;
			}
			if (dataTypeOptionList.contains("YMONTH") && strWrk.length() > 0 && strWrk.length() < 6) {
				isNullError = true;
			}
			if (dataTypeOptionList.contains("FYEAR") && strWrk.length() > 0 && strWrk.length() < 4) {
				isNullError = true;
			}
		}
		if (isNullError) {
			this.setError(XFUtility.RESOURCE.getString("FunctionError16"));
		}
		return isNullError;
	}

	public void setError(boolean error) {
		if (error) {
			isError = true;
		} else {
			isError = false;
			this.errorMessage = "";
		}
	}

	public String getError(){
		return errorMessage;
	}

	public void setError(String message){
		if (!message.equals("") && this.errorMessage.equals("")) {
			setError(true);
			this.errorMessage = message;
		}
	}

	public boolean isError() {
		return isError;
	}

	public boolean isVisibleOnPanel(){
		return isVisibleOnPanel;
	}

	public boolean isVirtualField(){
		return isVirtualField;
	}

	public boolean isRangeKeyFieldValid(){
		return isRangeKeyFieldValid;
	}

	public boolean isRangeKeyFieldExpire(){
		return isRangeKeyFieldExpire;
	}

	public org.w3c.dom.Element getTableElement(){
		return tableElement;
	}

	public String getBasicType(){
		return XFUtility.getBasicTypeOf(dataType);
	}
	
	public ArrayList<String> getTypeOptionList() {
		return dataTypeOptionList;
	}

	public String getTableID(){
		return tableID_;
	}

	public String getFieldID(){
		return fieldID_;
	}

	public String getFieldName(){
		return fieldName;
	}

	public String getFieldRemarks(){
		return fieldRemarks;
	}

	public String getFieldIDInScript(){
		return tableAlias_ + "_" + fieldID_;
	}

	public String getTableAlias(){
		return tableAlias_;
	}

	public String getDataSourceName(){
		return tableAlias_ + "." + fieldID_;
	}

	public boolean isKey(){
		return isKey;
	}

	public boolean isFieldOnDetailTable(){
		return isFieldOnDetailTable;
	}

	public Object getInternalValue(){
		return value_;
	}

	public Object getExternalValue(){
		Object value = null;
		String basicType = this.getBasicType();
		if (basicType.equals("INTEGER")) {
			if (value_ == null || value_.toString().equals("")) {
				value = "";
			} else {
				value = XFUtility.getFormattedIntegerValue(value_.toString(), dataTypeOptionList, dataSize);
			}
		} else {
			if (basicType.equals("FLOAT")) {
				if (value_ == null || value_.toString().equals("")) {
					value = "";
				} else {
					value = XFUtility.getFormattedFloatValue(value_.toString(), decimalSize);
				}
			} else {
				if (basicType.equals("DATE")) {
					if (value_ == null || value_.equals("")) {
						value = "";
					} else {
						value = XFUtility.getUserExpressionOfUtilDate(XFUtility.convertDateFromSqlToUtil(java.sql.Date.valueOf(value_.toString())), dialog_.getSession().getDateFormat(), false);
					}
				} else {
					if (basicType.equals("DATETIME")) {
						if (value_ != null) {
							value = value_.toString().replace("-", "/");
						}
					} else {
						if (!XFUtility.getOptionValueWithKeyword(dataTypeOptions, "KUBUN").equals("")) {
							if (value_ == null || value_.toString().trim().equals("")) {
								value = "";
							} else {
								String wrkStr = value_.toString().trim();
								for (int i = 0; i < kubunValueList.size(); i++) {
									if (kubunValueList.get(i).equals(wrkStr)) {
										value = kubunTextList.get(i);
										break;
									}
								}
							}
						} else {
							if (valueType.equals("STRING")) {
								if (value_ == null) {
									value = "";
								} else {
									value = value_.toString().trim();
									if (dataTypeOptionList.contains("YMONTH") || dataTypeOptionList.contains("FYEAR")) {
										String wrkStr = value.toString();
										if (!wrkStr.equals("")) {
											value = XFUtility.getUserExpressionOfYearMonth(wrkStr, dialog_.getSession().getDateFormat());
										}
									}
									if (dataTypeOptionList.contains("MSEQ")) {
										value = XFUtility.getUserExpressionOfMSeq(Integer.parseInt(value.toString()), dialog_.getSession());
									}
								}
							}
							if (valueType.equals("IMAGE")) {
								String fileName = dialog_.getSession().getImageFileFolder() + value_.toString().trim();
								int iconHeight = fieldRows * XFUtility.ROW_UNIT_HEIGHT;
								value = XFUtility.createSmallIcon(fileName, iconHeight);
							}
							if (valueType.equals("FLAG")) {
								if (value_.toString().trim().equals(flagTrue)) {
									value = XFUtility.ICON_CHECK_1D;
								} else {
									value = XFUtility.ICON_CHECK_0D;
								}
							}
						}
					}
				}
			}
		}
		return value;
	}
	
	public String getValueType() {
		return valueType;
	}

	public boolean isNull(){
		return XFUtility.isNullValue(this.getBasicType(), value_);
	}

	public boolean isNullable() {
		return isNullable;
	}

	public Object getNullValue(){
		return XFUtility.getNullValueOfBasicType(this.getBasicType());
	}

	public boolean isAutoNumberField(){
		return !numberingID.equals("");
	}

	public Object getAutoNumberValue(){
		Object value = null;
		if (!numberingID.equals("")) {
			value = dialog_.getSession().getNextNumber(numberingID);
		}
		return value;
	}

	public String getDataTypeOptions() {
		return dataTypeOptions;
	}

	public String getFieldOptions() {
		return fieldOptions;
	}

	public XFTableColumnEditor getColumnEditor(){
		return editor;
	}

	public String getCaption(){
		return fieldCaption;
	}

	public int getDataSize(){
		return dataSize;
	}

	public int getDecimalSize(){
		return decimalSize;
	}

	public int getWidth(){
		return fieldWidth;
	}

	public void setWidth(int width){
		fieldWidth = width;
	}

	public int getRows(){
		return fieldRows;
	}

	public String getLayout(){
		return fieldLayout;
	}

	public int getColumnIndex(){
		return columnIndex;
	}

	public void setColumnIndex(int index){
		columnIndex = index;
	}

	public void setValueOfResultSet(XFTableOperator operator) {
		String basicType = this.getBasicType();
		this.setColor("");

		try {
			if (this.isVirtualField) {
				if (this.isRangeKeyFieldExpire()) {
					value_ = XFUtility.calculateExpireValue(this.getTableElement(), operator, dialog_.getSession(), dialog_.getProcessLog());
				}
			} else {
				Object value = operator.getValueOf(this.getFieldID()); 
				if (basicType.equals("INTEGER")) {
					if (value == null || value.equals("")) {
						value_ = "";
					} else {
						String wrkStr = value.toString();
						int pos = wrkStr.indexOf(".");
						if (pos >= 0) {
							wrkStr = wrkStr.substring(0, pos);
						}
						value_ = Long.parseLong(wrkStr);
					}
				} else {
					if (basicType.equals("FLOAT")) {
						if (value == null || value.equals("")) {
							value_ = "";
						} else {
							value_ = Double.parseDouble(value.toString());
						}
					} else {
						if (value == null) {
							value_ = "";
						} else {
							value_ = value.toString().trim();
						}
					}
				}
			}
			oldValue_ = value_;
		} catch (Exception e) {
			e.printStackTrace(dialog_.getExceptionStream());
			dialog_.setErrorAndCloseFunction();
		}
	}

	public void initValue() {
		value_ = this.getNullValue();
		if (!isNonEditableField) {
			isEditable = true;
		}
	}

	public void setValue(Object value){
		value_ = XFUtility.getValueAccordingToBasicType(this.getBasicType(), value);
	}

	public Object getValue() {
		return getInternalValue();
	}

	public void setOldValue(Object value){
		oldValue_ = XFUtility.getValueAccordingToBasicType(this.getBasicType(), value);
	}

	public Object getOldValue(){
		if (oldValue_ == null) {
			if (this.getBasicType().equals("INTEGER") || this.getBasicType().equals("FLOAT")) {
				return 0;
			} else {
				return "";
			}
		} else {
			return oldValue_;
		}
	}
	
	public boolean isValueChanged() {
		return !this.getValue().equals(this.getOldValue());
	}

	public boolean isEditable() {
		return isEditable;
	}

	public boolean isNonEditableField() {
		return isNonEditableField;
	}

	public void setEditable(boolean editable) {
		if (editable && !isNonEditableField) {
			isEditable = true;
		} else {
			isEditable = false;
		}
	}

	public void setColor(String color) {
		foreground = XFUtility.convertStringToColor(color);
	}

	public String getColor() {
		return XFUtility.convertColorToString(foreground);
	}

	public Color getForeground() {
		return foreground;
	}
}

class XF310_HeaderTable extends Object {
	private static final long serialVersionUID = 1L;
	private org.w3c.dom.Element tableElement = null;
	private org.w3c.dom.Element functionElement_ = null;
	private String tableID = "";
	private String activeWhere = "";
	private String fixedWhere = "";
	private ArrayList<String> keyFieldIDList = new ArrayList<String>();
	private ArrayList<String> uniqueKeyList = new ArrayList<String>();
	private ArrayList<XFScript> scriptList = new ArrayList<XFScript>();
	private XF310 dialog_;
	private StringTokenizer workTokenizer;
	private String updateCounterID = "";
	private long updateCounterValue = 0;

	public XF310_HeaderTable(org.w3c.dom.Element functionElement, XF310 dialog){
		super();
		functionElement_ = functionElement;
		dialog_ = dialog;

		tableID = functionElement_.getAttribute("HeaderTable");
		tableElement = dialog_.getSession().getTableElement(tableID);
		activeWhere = tableElement.getAttribute("ActiveWhere");
		updateCounterID = tableElement.getAttribute("UpdateCounter");
		if (updateCounterID.equals("")) {
			updateCounterID = XFUtility.DEFAULT_UPDATE_COUNTER;
		} else {
			if (updateCounterID.toUpperCase().equals("*NONE")) {
				updateCounterID = "";
			}
		}
		fixedWhere = functionElement_.getAttribute("HeaderFixedWhere");

		String workString;
		org.w3c.dom.Element workElement;

		if (functionElement_.getAttribute("HeaderKeyFields").equals("")) {
			NodeList nodeList = tableElement.getElementsByTagName("Key");
			for (int i = 0; i < nodeList.getLength(); i++) {
				workElement = (org.w3c.dom.Element)nodeList.item(i);
				if (workElement.getAttribute("Type").equals("PK")) {
					workTokenizer = new StringTokenizer(workElement.getAttribute("Fields"), ";" );
					while (workTokenizer.hasMoreTokens()) {
						workString = workTokenizer.nextToken();
						keyFieldIDList.add(workString);
					}
					break;
				}
			}
		} else {
			workTokenizer = new StringTokenizer(functionElement_.getAttribute("HeaderKeyFields"), ";" );
			while (workTokenizer.hasMoreTokens()) {
				keyFieldIDList.add(workTokenizer.nextToken());
			}
		}

		NodeList nodeList = tableElement.getElementsByTagName("Key");
		for (int i = 0; i < nodeList.getLength(); i++) {
			workElement = (org.w3c.dom.Element)nodeList.item(i);
			if (workElement.getAttribute("Type").equals("SK")) {
				uniqueKeyList.add(workElement.getAttribute("Fields"));
			}
		}

		org.w3c.dom.Element element;
		NodeList workList = tableElement.getElementsByTagName("Script");
		SortableDomElementListModel sortList = XFUtility.getSortedListModel(workList, "Order");
		for (int i = 0; i < sortList.size(); i++) {
	        element = (org.w3c.dom.Element)sortList.getElementAt(i);
	        scriptList.add(new XFScript(tableID, element, dialog_.getSession().getTableNodeList()));
		}
	}

	public String getUpdateCounterID(){
		return updateCounterID;
	}
	
	public void setUpdateCounterValue(XFTableOperator operator) throws Exception {
		updateCounterValue = Long.parseLong(operator.getValueOf(updateCounterID).toString());
	}

	public String getSQLToSelect(){
		int count;
		StringBuffer buf = new StringBuffer();
		
		////////////////////////////////
		// Select-Fields-From section //
		////////////////////////////////
		buf.append("select ");
		count = -1;
		for (int i = 0; i < dialog_.getHeaderFieldList().size(); i++) {
			if (dialog_.getHeaderFieldList().get(i).isFieldOnPrimaryTable() && !dialog_.getHeaderFieldList().get(i).isVirtualField()) {
				count++;
				if (count > 0) {
					buf.append(",");
				}
				buf.append(dialog_.getHeaderFieldList().get(i).getFieldID());
			}
		}
		buf.append(",");
		buf.append(updateCounterID);
		buf.append(" from ");
		buf.append(tableID);
		
		///////////////////
		// Where section //
		///////////////////
		buf.append(" where ") ;
		count = -1;
		for (int i = 0; i < dialog_.getHeaderFieldList().size(); i++) {
			if (dialog_.getHeaderFieldList().get(i).isKey()) {
				count++;
				if (count > 0) {
					buf.append(" and ") ;
				}
				buf.append(dialog_.getHeaderFieldList().get(i).getFieldID()) ;
				buf.append("=") ;
				if (XFUtility.isLiteralRequiredBasicType(dialog_.getHeaderFieldList().get(i).getBasicType())) {
					buf.append("'") ;
					buf.append(dialog_.getParmMap().get(dialog_.getHeaderFieldList().get(i).getFieldID()));
					buf.append("'") ;
				} else {
					buf.append(dialog_.getParmMap().get(dialog_.getHeaderFieldList().get(i).getFieldID()));
				}
			}
		}
		if (!activeWhere.equals("")) {
			buf.append(" and (");
			buf.append(activeWhere);
			buf.append(") ");
		}
		if (!fixedWhere.equals("")) {
			buf.append(" and (");
			buf.append(fixedWhere);
			buf.append(") ");
		}

		return buf.toString();
	}

	String getSQLToUpdate() {
		StringBuffer statementBuf = new StringBuffer();

		////////////////////////
		// Update-Set section //
		////////////////////////
		statementBuf.append("update ");
		statementBuf.append(tableID);
		statementBuf.append(" set ");
		boolean firstField = true;
		for (int i = 0; i < dialog_.getHeaderFieldList().size(); i++) {
			if (dialog_.getHeaderFieldList().get(i).isFieldOnPrimaryTable()
			&& !dialog_.getHeaderFieldList().get(i).isKey()
			&& !dialog_.getHeaderFieldList().get(i).isVirtualField()) {
				if (!firstField) {
					statementBuf.append(", ");
				}
				statementBuf.append(dialog_.getHeaderFieldList().get(i).getFieldID());
				statementBuf.append("=");
				statementBuf.append(XFUtility.getTableOperationValue(dialog_.getHeaderFieldList().get(i).getBasicType(), dialog_.getHeaderFieldList().get(i).getInternalValue()));
				firstField = false;
			}
		}
		statementBuf.append(", ");
		statementBuf.append(updateCounterID);
		statementBuf.append("=");
		statementBuf.append(updateCounterValue + 1);
		
		///////////////////
		// Where section //
		///////////////////
		statementBuf.append(" where ") ;
		firstField = true;
		for (int i = 0; i < dialog_.getHeaderFieldList().size(); i++) {
			if (dialog_.getHeaderFieldList().get(i).isKey()) {
				if (!firstField) {
					statementBuf.append(" and ") ;
				}
				statementBuf.append(dialog_.getHeaderFieldList().get(i).getFieldID());
				statementBuf.append("=");
				statementBuf.append(XFUtility.getTableOperationValue(dialog_.getHeaderFieldList().get(i).getBasicType(), dialog_.getHeaderFieldList().get(i).getInternalValue()));
				firstField = false;
			}
		}
		statementBuf.append(" and ");
		statementBuf.append(updateCounterID);
		statementBuf.append("=");
		statementBuf.append(updateCounterValue);

		return statementBuf.toString();
	}
	
	public String getSQLToCheckSKDuplication(ArrayList<String> keyFieldList) {
		StringBuffer statementBuf = new StringBuffer();
		
		////////////////////////////////
		// Select-Fields-From section //
		////////////////////////////////
		statementBuf.append("select ");
		boolean firstField = true;
		for (int i = 0; i < dialog_.getHeaderFieldList().size(); i++) {
			if (dialog_.getHeaderFieldList().get(i).isFieldOnPrimaryTable()
					&& !dialog_.getHeaderFieldList().get(i).isVirtualField()) {
				if (!firstField) {
					statementBuf.append(",");
				}
				statementBuf.append(dialog_.getHeaderFieldList().get(i).getFieldID());
				firstField = false;
			}
		}
		statementBuf.append(" from ");
		statementBuf.append(tableID);
		
		///////////////////
		// Where section //
		///////////////////
		statementBuf.append(" where ");
		firstField = true;
		for (int j = 0; j < dialog_.getHeaderFieldList().size(); j++) {
			if (dialog_.getHeaderFieldList().get(j).isFieldOnPrimaryTable()) {
				for (int p = 0; p < keyFieldList.size(); p++) {
					if (dialog_.getHeaderFieldList().get(j).getFieldID().equals(keyFieldList.get(p))) {
						if (!firstField) {
							statementBuf.append(" and ");
						}
						statementBuf.append(dialog_.getHeaderFieldList().get(j).getFieldID());
						statementBuf.append("=");
						statementBuf.append(XFUtility.getTableOperationValue(dialog_.getHeaderFieldList().get(j).getBasicType(), dialog_.getHeaderFieldList().get(j).getInternalValue()));
						firstField = false;
					}
				}
			}
		}
		firstField = true;
		for (int j = 0; j < dialog_.getHeaderFieldList().size(); j++) {
			if (dialog_.getHeaderFieldList().get(j).isFieldOnPrimaryTable()) {
				if (dialog_.getHeaderFieldList().get(j).isKey()) {
					if (firstField) {
						statementBuf.append(" and (");
					} else {
						statementBuf.append(" or ");
					}
					statementBuf.append(dialog_.getHeaderFieldList().get(j).getFieldID());
					statementBuf.append("!=");
					statementBuf.append(XFUtility.getTableOperationValue(dialog_.getHeaderFieldList().get(j).getBasicType(), dialog_.getHeaderFieldList().get(j).getInternalValue()));
					firstField = false;
				}
			}
		}
		statementBuf.append(")") ;

		return statementBuf.toString();
	}
	
	boolean hasPrimaryKeyValueAltered() {
		boolean altered = false;
		for (int i = 0; i < dialog_.getHeaderFieldList().size(); i++) {
			if (dialog_.getHeaderFieldList().get(i).isKey()) {
				if (!dialog_.getHeaderFieldList().get(i).getValue().equals(dialog_.getHeaderFieldList().get(i).getOldValue())) {
					altered = true;
				}
			}
		}
		return altered;
	}

	public String getTableID(){
		return tableID;
	}

	public org.w3c.dom.Element getTableElement(){
		return tableElement;
	}
	
	public ArrayList<String> getKeyFieldIDList(){
		return keyFieldIDList;
	}
	
	public ArrayList<String> getUniqueKeyList(){
		return uniqueKeyList;
	}
	
	public ArrayList<XFScript> getScriptList(){
		return scriptList;
	}
	
	public boolean isValidDataSource(String tableID, String tableAlias, String fieldID) {
		boolean isValid = false;
		XF310_HeaderReferTable referTable;
		org.w3c.dom.Element workElement;
		if (this.getTableID().equals(tableID) && this.getTableID().equals(tableAlias)) {
			NodeList nodeList = tableElement.getElementsByTagName("Field");
			for (int i = 0; i < nodeList.getLength(); i++) {
				workElement = (org.w3c.dom.Element)nodeList.item(i);
				if (workElement.getAttribute("ID").equals(fieldID)) {
					isValid = true;
					break;
				}
			}
		} else {
			for (int i = 0; i < dialog_.getHeaderReferTableList().size(); i++) {
				referTable = dialog_.getHeaderReferTableList().get(i);
				if (referTable.getTableID().equals(tableID) && referTable.getTableAlias().equals(tableAlias)) {
					for (int j = 0; j < referTable.getFieldIDList().size(); j++) {
						if (referTable.getFieldIDList().get(j).equals(fieldID)) {
							isValid = true;
							break;
						}
					}
				}
				if (isValid) {
					break;
				}
			}
		}
		return isValid;
	}

	public int runScript(String event1, String event2) throws ScriptException {
		int countOfErrors = 0;
		XFScript script;
		ArrayList<XFScript> validScriptList = new ArrayList<XFScript>();
		for (int i = 0; i < scriptList.size(); i++) {
			script = scriptList.get(i);
			if (script.isToBeRunAtEvent(event1, event2)) {
				validScriptList.add(script);
			}
		}
		if (validScriptList.size() > 0) {
			for (int i = 0; i < validScriptList.size(); i++) {
				dialog_.evalScript(validScriptList.get(i).getName(), validScriptList.get(i).getScriptText(), null);
			}
			for (int i = 0; i < dialog_.getHeaderFieldList().size(); i++) {
				if (dialog_.getHeaderFieldList().get(i).isError()) {
					countOfErrors++;
				}
			}
		}
		return countOfErrors;
	}
}

class XF310_HeaderReferTable extends Object {
	private static final long serialVersionUID = 1L;
	private org.w3c.dom.Element referElement_ = null;
	private org.w3c.dom.Element tableElement = null;
	private XF310 dialog_ = null;
	private String tableID = "";
	private String tableAlias = "";
	private String activeWhere = "";
	private ArrayList<String> fieldIDList = new ArrayList<String>();
	private ArrayList<String> toKeyFieldIDList = new ArrayList<String>();
	private ArrayList<String> withKeyFieldIDList = new ArrayList<String>();
	private ArrayList<String> orderByFieldIDList = new ArrayList<String>();
	private boolean isToBeExecuted = false;
	private boolean isOptional = false;
	private int rangeKeyType = 0;
	private String rangeKeyFieldValid = "";
	private String rangeKeyFieldExpire = "";
	private String rangeKeyFieldSearch = "";
	private boolean rangeValidated;

	public XF310_HeaderReferTable(org.w3c.dom.Element referElement, XF310 dialog){
		super();
		referElement_ = referElement;
		dialog_ = dialog;

		tableID = referElement_.getAttribute("ToTable");
		tableElement = dialog_.getSession().getTableElement(tableID);

		StringTokenizer workTokenizer;
		String wrkStr = tableElement.getAttribute("RangeKey");
		if (!wrkStr.equals("")) {
			workTokenizer = new StringTokenizer(wrkStr, ";" );
			rangeKeyFieldValid =workTokenizer.nextToken();
			rangeKeyFieldExpire =workTokenizer.nextToken();
			org.w3c.dom.Element workElement = dialog_.getSession().getFieldElement(tableID, rangeKeyFieldExpire);
			if (XFUtility.getOptionList(workElement.getAttribute("TypeOptions")).contains("VIRTUAL")) {
				rangeKeyType = 1;
			} else {
				rangeKeyType = 2;
			}
		}

		activeWhere = tableElement.getAttribute("ActiveWhere");

		tableAlias = referElement_.getAttribute("TableAlias");
		if (tableAlias.equals("")) {
			tableAlias = tableID;
		}

		workTokenizer = new StringTokenizer(referElement_.getAttribute("Fields"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			fieldIDList.add(workTokenizer.nextToken());
		}

		if (referElement_.getAttribute("ToKeyFields").equals("")) {
			org.w3c.dom.Element workElement = dialog.getSession().getTablePKElement(tableID);
			workTokenizer = new StringTokenizer(workElement.getAttribute("Fields"), ";" );
			while (workTokenizer.hasMoreTokens()) {
				toKeyFieldIDList.add(workTokenizer.nextToken());
			}
		} else {
			workTokenizer = new StringTokenizer(referElement_.getAttribute("ToKeyFields"), ";" );
			while (workTokenizer.hasMoreTokens()) {
				toKeyFieldIDList.add(workTokenizer.nextToken());
			}
		}

		workTokenizer = new StringTokenizer(referElement_.getAttribute("WithKeyFields"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			withKeyFieldIDList.add(workTokenizer.nextToken());
		}

		workTokenizer = new StringTokenizer(referElement_.getAttribute("OrderBy"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			orderByFieldIDList.add(workTokenizer.nextToken());
		}

		if (referElement_.getAttribute("Optional").equals("T")) {
			isOptional = true;
		}
	}

	public String getSelectSQL(boolean isToGetRecordsForComboBox){
		int count;
		StringBuffer buf = new StringBuffer();
		
		////////////////////////////////
		// Select-Fields-From section //
		////////////////////////////////
		buf.append("select ");
		org.w3c.dom.Element workElement;
		count = 0;
		for (int i = 0; i < fieldIDList.size(); i++) {
			workElement = dialog_.getSession().getFieldElement(tableID, fieldIDList.get(i));
			if (!XFUtility.getOptionList(workElement.getAttribute("TypeOptions")).contains("VIRTUAL")) {
				if (count > 0) {
					buf.append(",");
				}
				count++;
				buf.append(fieldIDList.get(i));
			}
		}
		for (int i = 0; i < toKeyFieldIDList.size(); i++) {
			if (count > 0) {
				buf.append(",");
			}
			count++;
			buf.append(toKeyFieldIDList.get(i));
		}
		if (!rangeKeyFieldValid.equals("")) {
			if (count > 0) {
				buf.append(",");
			}
			buf.append(rangeKeyFieldValid);
			//
			workElement = dialog_.getSession().getFieldElement(tableID, rangeKeyFieldExpire);
			if (!XFUtility.getOptionList(workElement.getAttribute("TypeOptions")).contains("VIRTUAL")) {
				buf.append(",");
				buf.append(rangeKeyFieldExpire);
			}
		}
		buf.append(" from ");
		buf.append(tableID);
		
		///////////////////
		// Where section //
		///////////////////
		StringTokenizer workTokenizer;
		String keyFieldID, keyFieldTableID;
		count = 0;
		boolean isToBeWithValue;
		for (int i = 0; i < toKeyFieldIDList.size(); i++) {
			if (toKeyFieldIDList.get(i).equals(rangeKeyFieldValid)) {
				rangeKeyFieldSearch = withKeyFieldIDList.get(i);
			} else {
				if (isToGetRecordsForComboBox) {
					//////////////////////////////////////////////////////////////////
					// Value of the field which has either of these conditions      //
					// should be within WHERE to SELECT records:                    //
					// 1. The with-key-field is not edit-able                       //
					// 2. The with-key-field is part of PK of the header table      //
					// 3. The with-key-field is on the header table and consists of //
					//    upper part of with-key-fields                             //
					// 4. The with-key-field is part of PK of the other join table  //
					//////////////////////////////////////////////////////////////////
					for (int j = 0; j < dialog_.getHeaderFieldList().size(); j++) {
						if (withKeyFieldIDList.get(i).equals(dialog_.getHeaderFieldList().get(j).getDataSourceName())) {
							isToBeWithValue = false;
							if (!dialog_.getHeaderFieldList().get(j).isEditable()) {
								isToBeWithValue = true;
							} else {
								workTokenizer = new StringTokenizer(withKeyFieldIDList.get(i), "." );
								keyFieldTableID = workTokenizer.nextToken();
								keyFieldID = workTokenizer.nextToken();
								if (keyFieldTableID.equals(dialog_.getHeaderTable().getTableID())) {
									if (withKeyFieldIDList.size() > 1 && i < (withKeyFieldIDList.size() - 1)) {
										isToBeWithValue = true;
									} else {
										for (int k = 0; k < dialog_.getKeyFieldList().size(); k++) {
											if (keyFieldID.equals(dialog_.getKeyFieldList().get(k))) {
												isToBeWithValue = true;
											}
										}
									}
								} else {
									if (!keyFieldTableID.equals(this.tableAlias)) {
										isToBeWithValue = true;
									}
								}
							}
							if (isToBeWithValue) {
								if (count == 0) {
									buf.append(" where ");
								} else {
									buf.append(" and ");
								}
								buf.append(toKeyFieldIDList.get(i));
								buf.append("=");
								buf.append(XFUtility.getTableOperationValue(dialog_.getHeaderFieldList().get(j).getBasicType(), dialog_.getHeaderFieldList().get(j).getInternalValue()));
								count++;
								break;
							}
						}
					}
				} else {
					if (count == 0) {
						buf.append(" where ");
					} else {
						buf.append(" and ");
					}
					buf.append(toKeyFieldIDList.get(i));
					buf.append("=");
					for (int j = 0; j < dialog_.getHeaderFieldList().size(); j++) {
						if (withKeyFieldIDList.get(i).equals(dialog_.getHeaderFieldList().get(j).getTableAlias() + "." + dialog_.getHeaderFieldList().get(j).getFieldID())) {
							if (XFUtility.isLiteralRequiredBasicType(dialog_.getHeaderFieldList().get(j).getBasicType())) {
								buf.append("'") ;
								buf.append(dialog_.getHeaderFieldList().get(j).getInternalValue());
								buf.append("'") ;
							} else {
								buf.append(dialog_.getHeaderFieldList().get(j).getInternalValue());
							}
							break;
						}
					}
					count++;
				}
			}
		}
		if (!activeWhere.equals("")) {
			buf.append(" and ");
			buf.append(activeWhere);
		}
		
		//////////////////////
		// Order-by section //
		//////////////////////
		if (this.rangeKeyType != 0) {
			buf.append(" order by ");
			buf.append(rangeKeyFieldValid);
			buf.append(" DESC ");
		} else {
			if (orderByFieldIDList.size() > 0) {
				int pos0,pos1;
				buf.append(" order by ");
				for (int i = 0; i < orderByFieldIDList.size(); i++) {
					if (i > 0) {
						buf.append(",");
					}
					pos0 = orderByFieldIDList.get(i).indexOf(".");
					pos1 = orderByFieldIDList.get(i).indexOf("(A)");
					if (pos1 >= 0) {
						buf.append(orderByFieldIDList.get(i).substring(pos0+1, pos1));
					} else {
						pos1 = orderByFieldIDList.get(i).indexOf("(D)");
						if (pos1 >= 0) {
							buf.append(orderByFieldIDList.get(i).substring(pos0+1, pos1));
							buf.append(" DESC ");
						} else {
							buf.append(orderByFieldIDList.get(i).substring(pos0+1, orderByFieldIDList.get(i).length()));
						}
					}
				}
			}
		}

		rangeValidated = false;
		return buf.toString();
	}

	public String getTableID(){
		return tableID;
	}
	
	public String getTableAlias(){
		return tableAlias;
	}
	
	public boolean isOptional() {
		return isOptional;
	}

	public ArrayList<String> getKeyFieldIDList(){
		return toKeyFieldIDList;
	}
	
	public ArrayList<String> getWithKeyFieldIDList(){
		return withKeyFieldIDList;
	}

	public ArrayList<String> getFieldIDList(){
		return  fieldIDList;
	}
	
	public void setToBeExecuted(boolean executed){
		isToBeExecuted = executed;
	}
	
	public boolean isToBeExecuted(){
		return isToBeExecuted;
	}

	public boolean isKeyNullable() {
		boolean isKeyNullable = false;
		for (int i = 0; i < withKeyFieldIDList.size(); i++) {
			for (int j = 0; j < dialog_.getHeaderFieldList().size(); j++) {
				if (withKeyFieldIDList.get(i).equals(dialog_.getHeaderFieldList().get(j).getTableAlias() + "." + dialog_.getHeaderFieldList().get(j).getFieldID())) {
					if (dialog_.getHeaderFieldList().get(j).isNullable()) {
						isKeyNullable = true;
						break;
					}
				}
			}
		}
		return isKeyNullable;
	}

	public boolean isKeyNull() {
		boolean isKeyNull = false;
		for (int i = 0; i < withKeyFieldIDList.size(); i++) {
			for (int j = 0; j < dialog_.getHeaderFieldList().size(); j++) {
				if (withKeyFieldIDList.get(i).equals(dialog_.getHeaderFieldList().get(j).getTableAlias() + "." + dialog_.getHeaderFieldList().get(j).getFieldID())) {
					if (dialog_.getHeaderFieldList().get(j).isNull()) {
						isKeyNull = true;
						break;
					}
				}
			}
		}
		return isKeyNull;
	}

	public boolean isRecordToBeSelected(XFTableOperator operator) throws Exception {
		boolean returnValue = false;
		
		///////////////////
		// NORMAL SEARCH //
		///////////////////
		if (rangeKeyType == 0) {
			returnValue = true;
		}
		
		////////////////////
		// VIRTUAL SEARCH //
		////////////////////
		if (rangeKeyType == 1) {
			if (!rangeValidated) {
				////////////////////////////////////////////////////////////////
				// Note that result set is ordered by rangeKeyFieldValue DESC //
				////////////////////////////////////////////////////////////////
				Object valueKey = dialog_.getValueOfHeaderFieldByName(rangeKeyFieldSearch);
				Object valueFrom = operator.getValueOf(rangeKeyFieldValid);
				int comp1 = valueKey.toString().compareTo(valueFrom.toString());
				if (comp1 >= 0) {
					returnValue = true;
					rangeValidated = true;
				}
			}
		}

		/////////////////////
		// PHYSICAL SEARCH //
		/////////////////////
		if (rangeKeyType == 2) {
			Object valueKey = dialog_.getValueOfHeaderFieldByName(rangeKeyFieldSearch);
			Object valueFrom = operator.getValueOf(rangeKeyFieldValid);
			Object valueThru = operator.getValueOf(rangeKeyFieldExpire);
			if (valueThru == null) {
				int comp1 = valueKey.toString().compareTo(valueFrom.toString());
				if (comp1 >= 0) {
					returnValue = true;
				}
			} else {
				int comp1 = valueKey.toString().compareTo(valueFrom.toString());
				int comp2 = valueKey.toString().compareTo(valueThru.toString());
				if (comp1 >= 0 && comp2 < 0) {
					returnValue = true;
				}
			}
		}

		return returnValue;
	}

	public void setErrorOnRelatedFields() {
		boolean noneOfKeyFieldsWereSetError = true;

		//////////////////////////////////////////////////
		// Set error on the visible edit-able key field //
		//////////////////////////////////////////////////
		for (int i = 0; i < toKeyFieldIDList.size(); i++) {
			for (int j = 0; j < dialog_.getHeaderFieldList().size(); j++) {
				if (dialog_.getHeaderFieldList().get(j).isVisibleOnPanel()
						&& dialog_.getHeaderFieldList().get(j).isEditable()
						&& dialog_.getHeaderFieldList().get(j).getDataSourceName().equals(withKeyFieldIDList.get(i))
						&& !dialog_.getHeaderFieldList().get(j).isError()) {
					//dialog_.getHeaderFieldList().get(j).setError(dialog_.getHeaderFieldList().get(j).getCaption() + XFUtility.RESOURCE.getString("Colon") + tableElement.getAttribute("Name") + XFUtility.RESOURCE.getString("FunctionError45"));
					dialog_.getHeaderFieldList().get(j).setError(tableElement.getAttribute("Name") + XFUtility.RESOURCE.getString("FunctionError45"));
					//dialog_.getMessageList().add(dialog_.getHeaderFieldList().get(j).getError());
					noneOfKeyFieldsWereSetError = false;
					break;
				}
			}
		}

		if (noneOfKeyFieldsWereSetError) {
			////////////////////////////////////////////////////////
			// Set error on the visible edit-able attribute field //
			////////////////////////////////////////////////////////
			for (int i = 0; i < fieldIDList.size(); i++) {
				for (int j = 0; j < dialog_.getHeaderFieldList().size(); j++) {
					if (dialog_.getHeaderFieldList().get(j).isVisibleOnPanel()
							&& dialog_.getHeaderFieldList().get(j).isEditable()
							&& dialog_.getHeaderFieldList().get(j).getFieldID().equals(fieldIDList.get(i))
							&& dialog_.getHeaderFieldList().get(j).getTableAlias().equals(this.tableAlias)
							&& !dialog_.getHeaderFieldList().get(j).isError()) {
						dialog_.getHeaderFieldList().get(j).setError(tableElement.getAttribute("Name") + XFUtility.RESOURCE.getString("FunctionError45"));
						noneOfKeyFieldsWereSetError = false;
						break;
					}
				}
			}
		}

		if (noneOfKeyFieldsWereSetError) {
			//////////////////////////////////////
			// Set error on the first key field //
			//////////////////////////////////////
			for (int i = 0; i < toKeyFieldIDList.size(); i++) {
				for (int j = 0; j < dialog_.getHeaderFieldList().size(); j++) {
					if (dialog_.getHeaderFieldList().get(j).getDataSourceName().equals(withKeyFieldIDList.get(i))
							&& !dialog_.getHeaderFieldList().get(j).isError()) {
						dialog_.getHeaderFieldList().get(j).setError(tableElement.getAttribute("Name") + XFUtility.RESOURCE.getString("FunctionError45"));
						break;
					}
				}
			}
		}
	}

	public void setKeyFieldValues(XFHashMap keyValues){
		for (int i = 0; i < withKeyFieldIDList.size(); i++) {
			for (int j = 0; j < dialog_.getHeaderFieldList().size(); j++) {
				if (dialog_.getHeaderFieldList().get(j).getDataSourceName().equals(withKeyFieldIDList.get(i))) {
					dialog_.getHeaderFieldList().get(j).setValue(keyValues.getValue(withKeyFieldIDList.get(i)));
					break;
				}
			}
		}
	}
}

class XF310_DetailTable extends Object {
	private static final long serialVersionUID = 1L;
	private org.w3c.dom.Element tableElement = null;
	private org.w3c.dom.Element functionElement_ = null;
	private String tableID_ = "";
	private String activeWhere = "";
	private String updateValueToInactivate = "";
	private String fixedWhere = "";
	private ArrayList<String> keyFieldIDList = new ArrayList<String>();
	private ArrayList<String> uniqueKeyList = new ArrayList<String>();
	private ArrayList<String> orderByFieldIDList = new ArrayList<String>();
	private ArrayList<XFScript> scriptList = new ArrayList<XFScript>();
	private XF310 dialog_;
	private StringTokenizer workTokenizer;
	private boolean hasOrderByAsItsOwnFields = true;
	private String detailRowNoID = "";
	private String updateCounterID = "";

	public XF310_DetailTable(org.w3c.dom.Element functionElement, XF310 dialog){
		super();
		functionElement_ = functionElement;
		dialog_ = dialog;

		tableID_ = functionElement_.getAttribute("DetailTable");
		tableElement = dialog_.getSession().getTableElement(tableID_);
		activeWhere = tableElement.getAttribute("ActiveWhere");
		updateValueToInactivate = tableElement.getAttribute("DeleteOperation");
		updateCounterID = tableElement.getAttribute("UpdateCounter");
		if (updateCounterID.equals("")) {
			updateCounterID = XFUtility.DEFAULT_UPDATE_COUNTER;
		} else {
			if (updateCounterID.toUpperCase().equals("*NONE")) {
				updateCounterID = "";
			}
		}
		fixedWhere = functionElement_.getAttribute("DetailFixedWhere");

		int pos1;
		String wrkStr1, wrkStr2;
		org.w3c.dom.Element workElement, fieldElement;

		if (functionElement_.getAttribute("DetailKeyFields").equals("")) {
			NodeList nodeList = tableElement.getElementsByTagName("Key");
			for (int i = 0; i < nodeList.getLength(); i++) {
				workElement = (org.w3c.dom.Element)nodeList.item(i);
				if (workElement.getAttribute("Type").equals("PK")) {
					workTokenizer = new StringTokenizer(workElement.getAttribute("Fields"), ";" );
					while (workTokenizer.hasMoreTokens()) {
						wrkStr1 = workTokenizer.nextToken();
						keyFieldIDList.add(wrkStr1);
					}
					break;
				}
			}
		} else {
			workTokenizer = new StringTokenizer(functionElement_.getAttribute("DetailKeyFields"), ";" );
			while (workTokenizer.hasMoreTokens()) {
				keyFieldIDList.add(workTokenizer.nextToken());
			}
		}

		NodeList nodeList = tableElement.getElementsByTagName("Key");
		for (int i = 0; i < nodeList.getLength(); i++) {
			workElement = (org.w3c.dom.Element)nodeList.item(i);
			if (workElement.getAttribute("Type").equals("PK")) {
				wrkStr1 = "";
				workTokenizer = new StringTokenizer(workElement.getAttribute("Fields"), ";" );
				if (workTokenizer.countTokens() > 1) {
					while (workTokenizer.hasMoreTokens()) {
						wrkStr1 = workTokenizer.nextToken();
					}
					if (!wrkStr1.equals("")) {
						fieldElement = dialog_.getSession().getFieldElement(tableID_, wrkStr1);
						if (XFUtility.getBasicTypeOf(fieldElement.getAttribute("Type")).equals("INTEGER")
								&& tableElement.getAttribute("DetailRowNumberAuto").equals("T")) {
							detailRowNoID = wrkStr1;
						}
					}
				}
			}
			if (workElement.getAttribute("Type").equals("SK")) {
				uniqueKeyList.add(workElement.getAttribute("Fields"));
			}
		}

		workTokenizer = new StringTokenizer(functionElement_.getAttribute("DetailOrderBy"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			wrkStr1 = workTokenizer.nextToken();
			pos1 = wrkStr1.indexOf(".");
			if (pos1 > -1) { 
				wrkStr2 = wrkStr1.substring(0, pos1);
				if (!wrkStr2.equals(tableID_)) {
					hasOrderByAsItsOwnFields = false;
				}
			}
			orderByFieldIDList.add(wrkStr1);
		}

		org.w3c.dom.Element element;
		NodeList workList = tableElement.getElementsByTagName("Script");
		SortableDomElementListModel sortList = XFUtility.getSortedListModel(workList, "Order");
		for (int i = 0; i < sortList.size(); i++) {
	        element = (org.w3c.dom.Element)sortList.getElementAt(i);
	        scriptList.add(new XFScript(tableID_, element, dialog_.getSession().getTableNodeList()));
		}
	}
	
	public String getDetailRowNoID() {
		return detailRowNoID;
	}
	
	public String getSQLToSelect(){
		int count;
		StringBuffer buf = new StringBuffer();
		XF310_HeaderField headerField;
		
		////////////////////////////////
		// Select-Fields-From section //
		////////////////////////////////
		buf.append("select ");
		count = -1;
		for (int i = 0; i < keyFieldIDList.size(); i++) {
			count++;
			if (count > 0) {
				buf.append(",");
			}
			buf.append(keyFieldIDList.get(i));
		}
		for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
			if (dialog_.getDetailColumnList().get(i).getTableID().equals(tableID_) && !dialog_.getDetailColumnList().get(i).isVirtualField()) {
				count++;
				if (count > 0) {
					buf.append(",");
				}
				buf.append(dialog_.getDetailColumnList().get(i).getFieldID());
			}
		}
		if (count > 0) {
			buf.append(",");
		}
		buf.append(updateCounterID);
		buf.append(" from ");
		buf.append(tableID_);
		
		///////////////////
		// Where section //
		///////////////////
		buf.append(" where ") ;
		count = -1;
		for (int i = 0; i < dialog_.getHeaderTable().getKeyFieldIDList().size(); i++) {
			count++;
			if (count > 0) {
				buf.append(" and ") ;
			}
			buf.append(keyFieldIDList.get(i)) ;
			buf.append("=") ;
			headerField = dialog_.getHeaderFieldObjectByID(dialog_.getHeaderTable().getTableID(), "", dialog_.getHeaderTable().getKeyFieldIDList().get(i));
			if (XFUtility.isLiteralRequiredBasicType(headerField.getBasicType())) {
				buf.append("'");
				buf.append(dialog_.getParmMap().get(dialog_.getHeaderTable().getKeyFieldIDList().get(i)));
				buf.append("'");
			} else {
				buf.append(dialog_.getParmMap().get(dialog_.getHeaderTable().getKeyFieldIDList().get(i)));
			}
		}
		if (!activeWhere.equals("")) {
			buf.append(" and (");
			buf.append(activeWhere);
			buf.append(") ");
		}
		if (!fixedWhere.equals("")) {
			buf.append(" and (");
			buf.append(fixedWhere);
			buf.append(") ");
		}
		
		//////////////////////
		// Order-by section //
		//////////////////////
		if (this.hasOrderByAsItsOwnFields) {
			if (orderByFieldIDList.size() > 0) {
				int pos0,pos1;
				buf.append(" order by ");
				for (int i = 0; i < orderByFieldIDList.size(); i++) {
					if (i > 0) {
						buf.append(",");
					}
					pos0 = orderByFieldIDList.get(i).indexOf(".");
					pos1 = orderByFieldIDList.get(i).indexOf("(A)");
					if (pos1 >= 0) {
						buf.append(orderByFieldIDList.get(i).substring(pos0+1, pos1));
					} else {
						pos1 = orderByFieldIDList.get(i).indexOf("(D)");
						if (pos1 >= 0) {
							buf.append(orderByFieldIDList.get(i).substring(pos0+1, pos1));
							buf.append(" DESC ");
						} else {
							buf.append(orderByFieldIDList.get(i).substring(pos0+1, orderByFieldIDList.get(i).length()));
						}
					}
				}
			} else {
				buf.append(" order by ");
				for (int i = 0; i < keyFieldIDList.size(); i++) {
					if (i > 0) {
						buf.append(",");
					}
					buf.append(keyFieldIDList.get(i));
				}
			}
		}

		return buf.toString();
	}

	String getSQLToUpdate(XF310_DetailRowNumber rowNumber) {
		StringBuffer statementBuf = new StringBuffer();
		
		////////////////////////
		// Update-Set section //
		////////////////////////
		statementBuf.append("update ");
		statementBuf.append(tableID_);
		statementBuf.append(" set ");
		boolean firstField = true;
		for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
			if (dialog_.getDetailColumnList().get(i).isFieldOnDetailTable()
			&& !dialog_.getDetailColumnList().get(i).isKey()
			&& !dialog_.getDetailColumnList().get(i).isVirtualField()) {
				if (!firstField) {
					statementBuf.append(", ");
				}
				statementBuf.append(dialog_.getDetailColumnList().get(i).getFieldID());
				statementBuf.append("=");
				statementBuf.append(XFUtility.getTableOperationValue(dialog_.getDetailColumnList().get(i).getBasicType(), rowNumber.getColumnValueMap().get(dialog_.getDetailColumnList().get(i).getDataSourceName())));
				firstField = false;
			}
		}
		statementBuf.append(", ");
		statementBuf.append(updateCounterID);
		statementBuf.append("=");
		statementBuf.append((Long)rowNumber.getColumnValueMap().get(updateCounterID) + 1);
		
		///////////////////
		// Where section //
		///////////////////
		statementBuf.append(" where ") ;
		boolean firstKey = true;
		for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
			for (int j = 0; j < keyFieldIDList.size(); j++) {
				if (dialog_.getDetailColumnList().get(i).getTableID().equals(tableID_) && 
						dialog_.getDetailColumnList().get(i).getFieldID().equals(keyFieldIDList.get(j))) {
					if (!firstKey) {
						statementBuf.append(" and ") ;
					}
					statementBuf.append(keyFieldIDList.get(j)) ;
					statementBuf.append("=") ;
					statementBuf.append(XFUtility.getTableOperationValue(dialog_.getDetailColumnList().get(i).getBasicType(), rowNumber.getKeyValueMap().get(keyFieldIDList.get(j))));
					firstKey = false;
				}
			}
		}
		statementBuf.append(" and ") ;
		statementBuf.append(updateCounterID) ;
		statementBuf.append("=") ;
		statementBuf.append(rowNumber.getColumnValueMap().get(updateCounterID));

		return statementBuf.toString();
	}

	String getSQLToInsert(XF310_DetailRowNumber rowNumber) {
		StringBuffer statementBuf = new StringBuffer();

		/////////////////////////
		// Insert-Into section //
		/////////////////////////
		statementBuf.append("insert into ");
		statementBuf.append(tableID_);

		////////////////////
		// Fields section //
		////////////////////
		statementBuf.append(" (");
		boolean firstField = true;
		for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
			if (dialog_.getDetailColumnList().get(i).isFieldOnDetailTable()
			&& !dialog_.getDetailColumnList().get(i).isVirtualField()) {
				if (!firstField) {
					statementBuf.append(", ");
				}
				statementBuf.append(dialog_.getDetailColumnList().get(i).getFieldID());
				firstField = false;
			}
		}

		////////////////////
		// Values section //
		////////////////////
		statementBuf.append(") values(") ;
		firstField = true;
		for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
			if (dialog_.getDetailColumnList().get(i).isFieldOnDetailTable()
			&& !dialog_.getDetailColumnList().get(i).isVirtualField()) {
				if (!firstField) {
					statementBuf.append(", ");
				}
				if (dialog_.getDetailColumnList().get(i).isKey()) {
					statementBuf.append(XFUtility.getTableOperationValue(dialog_.getDetailColumnList().get(i).getBasicType(), rowNumber.getKeyValueMap().get(dialog_.getDetailColumnList().get(i).getFieldID())));
				} else {
					statementBuf.append(XFUtility.getTableOperationValue(dialog_.getDetailColumnList().get(i).getBasicType(), rowNumber.getColumnValueMap().get(dialog_.getDetailColumnList().get(i).getDataSourceName())));
				}
				firstField = false;
			}
		}
		statementBuf.append(")") ;

		return statementBuf.toString();
	}

	String getSQLToDelete(HashMap<String, Object> keyMap, long updateCounterValue) {
		StringBuffer statementBuf = new StringBuffer();
		
		///////////////////////////////////
		// Delete/Inactive-Update section //
		///////////////////////////////////
		if (updateValueToInactivate.equals("")) {
			statementBuf.append("delete from ");
			statementBuf.append(tableID_);
		} else {
			statementBuf.append("update ");
			statementBuf.append(tableID_);
			statementBuf.append(" set ");
			statementBuf.append(updateValueToInactivate) ;
			statementBuf.append(", ") ;
			statementBuf.append(updateCounterID) ;
			statementBuf.append("=") ;
			statementBuf.append(updateCounterValue + 1) ;
		}
		
		///////////////////
		// Where section //
		///////////////////
		statementBuf.append(" where ") ;
		boolean firstKey = true;
		for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
			for (int j = 0; j < keyFieldIDList.size(); j++) {
				if (dialog_.getDetailColumnList().get(i).getTableID().equals(tableID_) && 
						dialog_.getDetailColumnList().get(i).getFieldID().equals(keyFieldIDList.get(j))) {
					if (!firstKey) {
						statementBuf.append(" and ") ;
					}
					statementBuf.append(keyFieldIDList.get(j)) ;
					statementBuf.append("=") ;
					if (XFUtility.isLiteralRequiredBasicType(dialog_.getDetailColumnList().get(i).getBasicType())) {
						statementBuf.append("'");
						statementBuf.append(keyMap.get(keyFieldIDList.get(j))) ;
						statementBuf.append("'");
					} else {
						statementBuf.append(keyMap.get(keyFieldIDList.get(j))) ;
					}
					firstKey = false;
				}
			}
		}
		statementBuf.append(" and ") ;
		statementBuf.append(updateCounterID) ;
		statementBuf.append("=") ;
		statementBuf.append(updateCounterValue) ;

		return statementBuf.toString();
	}
	
	public String getSQLToCheckSKDuplication(XF310_DetailRowNumber rowNumber, ArrayList<String> keyFieldList, boolean isToUpdate) {
		StringBuffer statementBuf = new StringBuffer();
		
		////////////////////////////////
		// Select-Fields-From section //
		////////////////////////////////
		statementBuf.append("select ");
		boolean firstField = true;
		for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
			if (dialog_.getDetailColumnList().get(i).isFieldOnDetailTable()
					&& !dialog_.getDetailColumnList().get(i).isVirtualField()) {
				if (!firstField) {
					statementBuf.append(",");
				}
				statementBuf.append(dialog_.getDetailColumnList().get(i).getFieldID());
				firstField = false;
			}
		}
		statementBuf.append(" from ");
		statementBuf.append(tableID_);

		///////////////////
		// Where section //
		///////////////////
		statementBuf.append(" where ") ;
		firstField = true;
		for (int j = 0; j < dialog_.getDetailColumnList().size(); j++) {
			if (dialog_.getDetailColumnList().get(j).isFieldOnDetailTable()) {
				for (int p = 0; p < keyFieldList.size(); p++) {
					if (dialog_.getDetailColumnList().get(j).getFieldID().equals(keyFieldList.get(p))) {
						if (!firstField) {
							statementBuf.append(" and ") ;
						}
						statementBuf.append(dialog_.getDetailColumnList().get(j).getFieldID()) ;
						statementBuf.append("=") ;
						statementBuf.append(XFUtility.getTableOperationValue(dialog_.getDetailColumnList().get(j).getBasicType(), rowNumber.getColumnValueMap().get(dialog_.getDetailColumnList().get(j).getDataSourceName())));
						firstField = false;
					}
				}
			}
		}
		if (isToUpdate) {
			firstField = true;
			for (int j = 0; j < dialog_.getDetailColumnList().size(); j++) {
				if (dialog_.getDetailColumnList().get(j).isFieldOnDetailTable()) {
					if (dialog_.getDetailColumnList().get(j).isKey()) {
						if (firstField) {
							statementBuf.append(" and (") ;
						} else {
							statementBuf.append(" or ") ;
						}
						statementBuf.append(dialog_.getDetailColumnList().get(j).getFieldID()) ;
						statementBuf.append("!=") ;
						statementBuf.append(XFUtility.getTableOperationValue(dialog_.getDetailColumnList().get(j).getBasicType(), rowNumber.getKeyValueMap().get(dialog_.getDetailColumnList().get(j).getFieldID())));
						firstField = false;
					}
				}
			}
			statementBuf.append(")") ;
		}

		return statementBuf.toString();
	}

	public String getTableID(){
		return tableID_;
	}
	
	public String getUpdateCounterID(){
		return updateCounterID;
	}
	
	public ArrayList<String> getKeyFieldIDList(){
		return keyFieldIDList;
	}
	
	public ArrayList<String> getUniqueKeyList(){
		return uniqueKeyList;
	}
	
	public ArrayList<XFScript> getScriptList(){
		return scriptList;
	}

	public org.w3c.dom.Element getTableElement() {
		return tableElement;
	}
	
	public ArrayList<String> getOrderByFieldIDList(){
		return orderByFieldIDList;
	}
	
	public boolean hasOrderByAsItsOwnFields(){
		return hasOrderByAsItsOwnFields;
	}
	
	boolean hasPrimaryKeyValueAltered(XF310_DetailRowNumber rowNumber) {
		boolean altered = false;
		for (int j = 0; j < dialog_.getDetailColumnList().size(); j++) {
			if (dialog_.getDetailColumnList().get(j).isKey()) {
				if (!rowNumber.getColumnValueMap().get(dialog_.getDetailColumnList().get(j).getDataSourceName()).equals(rowNumber.getColumnOldValueMap().get(dialog_.getDetailColumnList().get(j).getDataSourceName()))) {
					altered = true;
				}
			}
		}
		return altered;
	}

	public boolean isValidDataSource(String tableID, String tableAlias, String fieldID) {
		boolean isValid = false;
		XF310_DetailReferTable referTable;
		org.w3c.dom.Element workElement;
		if (this.getTableID().equals(tableID) && this.getTableID().equals(tableAlias)) {
			NodeList nodeList = tableElement.getElementsByTagName("Field");
			for (int i = 0; i < nodeList.getLength(); i++) {
				workElement = (org.w3c.dom.Element)nodeList.item(i);
				if (workElement.getAttribute("ID").equals(fieldID)) {
					isValid = true;
					break;
				}
			}
		} else {
			for (int i = 0; i < dialog_.getDetailReferTableList().size(); i++) {
				referTable = dialog_.getDetailReferTableList().get(i);
				if (referTable.getTableID().equals(tableID) && referTable.getTableAlias().equals(tableAlias)) {
					for (int j = 0; j < referTable.getFieldIDList().size(); j++) {
						if (referTable.getFieldIDList().get(j).equals(fieldID)) {
							isValid = true;
							break;
						}
					}
				}
				if (isValid) {
					break;
				}
			}
		}
		return isValid;
	}

	public int runScript(String event1, String event2, HashMap<String, Object> columnValueMap, HashMap<String, Object> columnOldValueMap) throws ScriptException {
		int countOfErrors = 0;
		XFScript script;
		ArrayList<XFScript> validScriptList = new ArrayList<XFScript>();
		for (int i = 0; i < scriptList.size(); i++) {
			script = scriptList.get(i);
			if (script.isToBeRunAtEvent(event1, event2)) {
				validScriptList.add(script);
			}
		}
		if (validScriptList.size() > 0) {
			for (int i = 0; i < validScriptList.size(); i++) {
				dialog_.evalScript(validScriptList.get(i).getName(), validScriptList.get(i).getScriptText(), null);
			}
			for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
				if (columnValueMap != null) {
					columnValueMap.put(dialog_.getDetailColumnList().get(i).getDataSourceName(), dialog_.getDetailColumnList().get(i).getInternalValue());
				}
				if (columnOldValueMap != null) {
					columnOldValueMap.put(dialog_.getDetailColumnList().get(i).getDataSourceName(), dialog_.getDetailColumnList().get(i).getOldValue());
				}
				if (dialog_.getDetailColumnList().get(i).isError()) {
					countOfErrors++;
				}
			}
		}
		return countOfErrors;
	}
}

class XF310_DetailReferTable extends Object {
	private static final long serialVersionUID = 1L;
	private org.w3c.dom.Element referElement_ = null;
	private org.w3c.dom.Element tableElement = null;
	private XF310 dialog_ = null;
	private String tableID = "";
	private String tableAlias = "";
	private String activeWhere = "";
	private ArrayList<String> fieldIDList = new ArrayList<String>();
	private ArrayList<String> toKeyFieldIDList = new ArrayList<String>();
	private ArrayList<String> withKeyFieldIDList = new ArrayList<String>();
	private ArrayList<String> orderByFieldIDList = new ArrayList<String>();
	private boolean isToBeExecuted = false;
	private boolean isOptional = false;
	private int rangeKeyType = 0;
	private String rangeKeyFieldValid = "";
	private String rangeKeyFieldExpire = "";
	private String rangeKeyFieldSearch = "";
	private boolean rangeValidated;

	public XF310_DetailReferTable(org.w3c.dom.Element referElement, XF310 dialog){
		super();
		referElement_ = referElement;
		dialog_ = dialog;

		tableID = referElement_.getAttribute("ToTable");
		tableElement = dialog_.getSession().getTableElement(tableID);

		StringTokenizer workTokenizer;
		String wrkStr = tableElement.getAttribute("RangeKey");
		if (!wrkStr.equals("")) {
			workTokenizer = new StringTokenizer(wrkStr, ";" );
			rangeKeyFieldValid =workTokenizer.nextToken();
			rangeKeyFieldExpire =workTokenizer.nextToken();
			org.w3c.dom.Element workElement = dialog_.getSession().getFieldElement(tableID, rangeKeyFieldExpire);
			if (XFUtility.getOptionList(workElement.getAttribute("TypeOptions")).contains("VIRTUAL")) {
				rangeKeyType = 1;
			} else {
				rangeKeyType = 2;
			}
		}

		activeWhere = tableElement.getAttribute("ActiveWhere");

		tableAlias = referElement_.getAttribute("TableAlias");
		if (tableAlias.equals("")) {
			tableAlias = tableID;
		}

		workTokenizer = new StringTokenizer(referElement_.getAttribute("Fields"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			fieldIDList.add(workTokenizer.nextToken());
		}

		if (referElement_.getAttribute("ToKeyFields").equals("")) {
			org.w3c.dom.Element workElement = dialog.getSession().getTablePKElement(tableID);
			workTokenizer = new StringTokenizer(workElement.getAttribute("Fields"), ";" );
			while (workTokenizer.hasMoreTokens()) {
				toKeyFieldIDList.add(workTokenizer.nextToken());
			}
		} else {
			workTokenizer = new StringTokenizer(referElement_.getAttribute("ToKeyFields"), ";" );
			while (workTokenizer.hasMoreTokens()) {
				toKeyFieldIDList.add(workTokenizer.nextToken());
			}
		}

		workTokenizer = new StringTokenizer(referElement_.getAttribute("WithKeyFields"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			withKeyFieldIDList.add(workTokenizer.nextToken());
		}

		workTokenizer = new StringTokenizer(referElement_.getAttribute("OrderBy"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			orderByFieldIDList.add(workTokenizer.nextToken());
		}

		if (referElement_.getAttribute("Optional").equals("T")) {
			isOptional = true;
		}
	}

	public String getSelectSQL(boolean isToGetRecordsForComboBox){
		int count;
		org.w3c.dom.Element workElement;
		StringBuffer buf = new StringBuffer();
		boolean validWhereKeys = false;
		
		////////////////////////////////
		// Select-Fields-From section //
		////////////////////////////////
		buf.append("select ");
		count = 0;
		for (int i = 0; i < fieldIDList.size(); i++) {
			workElement = dialog_.getSession().getFieldElement(tableID, fieldIDList.get(i));
			if (!XFUtility.getOptionList(workElement.getAttribute("TypeOptions")).contains("VIRTUAL")) {
				if (count > 0) {
					buf.append(",");
				}
				count++;
				buf.append(fieldIDList.get(i));
			}
		}
		for (int i = 0; i < toKeyFieldIDList.size(); i++) {
			if (count > 0) {
				buf.append(",");
			}
			count++;
			buf.append(toKeyFieldIDList.get(i));
		}
		if (!rangeKeyFieldValid.equals("")) {
			if (count > 0) {
				buf.append(",");
			}
			buf.append(rangeKeyFieldValid);
			//
			workElement = dialog_.getSession().getFieldElement(tableID, rangeKeyFieldExpire);
			if (!XFUtility.getOptionList(workElement.getAttribute("TypeOptions")).contains("VIRTUAL")) {
				buf.append(",");
				buf.append(rangeKeyFieldExpire);
			}
		}
		buf.append(" from ");
		buf.append(tableID);
		
		///////////////////
		// Where section //
		///////////////////
		StringTokenizer workTokenizer;
		String keyFieldID, keyFieldTableID;
		count = 0;
		boolean isToBeWithValue;
		for (int i = 0; i < toKeyFieldIDList.size(); i++) {
			if (toKeyFieldIDList.get(i).equals(rangeKeyFieldValid)) {
				rangeKeyFieldSearch = withKeyFieldIDList.get(i);
			} else {
				if (isToGetRecordsForComboBox) {
					//////////////////////////////////////////////////////////////////
					// Value of the field which has either of these conditions      //
					// should be within WHERE to SELECT records:                    //
					// 1. The with-key-field is not edit-able                       //
					// 2. The with-key-field is part of PK of the header table      //
					// 3. The with-key-field is on the header table and consists of //
					//    upper part of with-key-fields                             //
					// 4. The with-key-field is part of PK of the other join table  //
					//////////////////////////////////////////////////////////////////
					for (int j = 0; j < dialog_.getDetailColumnList().size(); j++) {
						if (withKeyFieldIDList.get(i).equals(dialog_.getDetailColumnList().get(j).getDataSourceName())) {
							isToBeWithValue = false;
							if (!dialog_.getDetailColumnList().get(j).isEditable()) {
								isToBeWithValue = true;
							} else {
								workTokenizer = new StringTokenizer(withKeyFieldIDList.get(i), "." );
								keyFieldTableID = workTokenizer.nextToken();
								keyFieldID = workTokenizer.nextToken();
								if (keyFieldTableID.equals(dialog_.getDetailTable().getTableID())) {
									if (withKeyFieldIDList.size() > 1 && i < (withKeyFieldIDList.size() - 1)) {
										isToBeWithValue = true;
									} else {
										for (int k = 0; k < dialog_.getKeyFieldList().size(); k++) {
											if (keyFieldID.equals(dialog_.getKeyFieldList().get(k))) {
												isToBeWithValue = true;
											}
										}
									}
								} else {
									if (!keyFieldTableID.equals(this.tableAlias)) {
										isToBeWithValue = true;
									}
								}
							}
							if (isToBeWithValue) {
								if (count == 0) {
									buf.append(" where ");
								} else {
									buf.append(" and ");
								}
								buf.append(toKeyFieldIDList.get(i));
								buf.append("=");
								buf.append(XFUtility.getTableOperationValue(dialog_.getDetailColumnList().get(j).getBasicType(), dialog_.getDetailColumnList().get(j).getInternalValue())) ;
								count++;
								break;
							}
						}
					}
				} else {
					if (count == 0) {
						buf.append(" where ");
					} else {
						buf.append(" and ");
					}
					buf.append(toKeyFieldIDList.get(i));
					buf.append("=");
					for (int j = 0; j < dialog_.getDetailColumnList().size(); j++) {
						if (withKeyFieldIDList.get(i).equals(dialog_.getDetailColumnList().get(j).getDataSourceName())) {
							if (XFUtility.isLiteralRequiredBasicType(dialog_.getDetailColumnList().get(j).getBasicType())) {
								buf.append("'");
								buf.append(dialog_.getDetailColumnList().get(j).getInternalValue());
								buf.append("'");
								if (!dialog_.getDetailColumnList().get(j).getInternalValue().equals("")) {
									validWhereKeys = true;
								}
							} else {
								buf.append(dialog_.getDetailColumnList().get(j).getInternalValue());
								validWhereKeys = true;
							}
							break;
						}
					}
					count++;
				}
			}
		}
		if (!activeWhere.equals("")) {
			buf.append(" and ");
			buf.append(activeWhere);
		}
		
		//////////////////////
		// Order-by section //
		//////////////////////
		if (this.rangeKeyType != 0) {
			buf.append(" order by ");
			buf.append(rangeKeyFieldValid);
			buf.append(" DESC ");
		} else {
			if (orderByFieldIDList.size() > 0) {
				int pos0,pos1;
				buf.append(" order by ");
				for (int i = 0; i < orderByFieldIDList.size(); i++) {
					if (i > 0) {
						buf.append(",");
					}
					pos0 = orderByFieldIDList.get(i).indexOf(".");
					pos1 = orderByFieldIDList.get(i).indexOf("(A)");
					if (pos1 >= 0) {
						buf.append(orderByFieldIDList.get(i).substring(pos0+1, pos1));
					} else {
						pos1 = orderByFieldIDList.get(i).indexOf("(D)");
						if (pos1 >= 0) {
							buf.append(orderByFieldIDList.get(i).substring(pos0+1, pos1));
							buf.append(" DESC ");
						} else {
							buf.append(orderByFieldIDList.get(i).substring(pos0+1, orderByFieldIDList.get(i).length()));
						}
					}
				}
			}
		}

		rangeValidated = false;

		if (isToGetRecordsForComboBox) {
			return buf.toString();
		} else {	
			if (validWhereKeys) {
				return buf.toString();
			} else {
				return "";
			}
		}
	}

	public String getTableID(){
		return tableID;
	}

	public String getTableAlias(){
		return tableAlias;
	}
	
	public boolean isOptional() {
		return isOptional;
	}
	
	public ArrayList<String> getKeyFieldIDList(){
		return toKeyFieldIDList;
	}
	
	public ArrayList<String> getWithKeyFieldIDList(){
		return withKeyFieldIDList;
	}

	public ArrayList<String> getFieldIDList(){
		return  fieldIDList;
	}
	
	public void setToBeExecuted(boolean executed){
		isToBeExecuted = executed;
	}
	
	public boolean isToBeExecuted(){
		return isToBeExecuted;
	}

	public boolean isKeyNullable() {
		boolean isKeyNullable = false;
		for (int i = 0; i < withKeyFieldIDList.size(); i++) {
			for (int j = 0; j < dialog_.getDetailColumnList().size(); j++) {
				if (withKeyFieldIDList.get(i).equals(dialog_.getDetailColumnList().get(j).getTableAlias() + "." + dialog_.getDetailColumnList().get(j).getFieldID())) {
					if (dialog_.getDetailColumnList().get(j).isNullable()) {
						isKeyNullable = true;
						break;
					}
				}
			}
		}
		return isKeyNullable;
	}

	public boolean isKeyNull() {
		boolean isKeyNull = false;
		for (int i = 0; i < withKeyFieldIDList.size(); i++) {
			for (int j = 0; j < dialog_.getDetailColumnList().size(); j++) {
				if (withKeyFieldIDList.get(i).equals(dialog_.getDetailColumnList().get(j).getTableAlias() + "." + dialog_.getDetailColumnList().get(j).getFieldID())) {
					if (dialog_.getDetailColumnList().get(j).isNull()) {
						isKeyNull = true;
						break;
					}
				}
			}
		}
		return isKeyNull;
	}

	public boolean isRecordToBeSelected(XFTableOperator operator) throws Exception {
		boolean returnValue = false;
		if (rangeKeyType == 0) {
			returnValue = true;
		}
		if (rangeKeyType == 1) {
			////////////////////////////////////////////////////////////////
			// Note that result set is ordered by rangeKeyFieldValue DESC //
			////////////////////////////////////////////////////////////////
			if (!rangeValidated) { 
				StringTokenizer workTokenizer = new StringTokenizer(rangeKeyFieldSearch, "." );
				String workTableAlias = workTokenizer.nextToken();
				String workFieldID = workTokenizer.nextToken();
				Object valueKey = dialog_.getDetailColumnObjectByID("", workTableAlias, workFieldID).getInternalValue();
				Object valueFrom = operator.getValueOf(rangeKeyFieldValid);
				int comp1 = valueKey.toString().compareTo(valueFrom.toString());
				if (comp1 >= 0) {
					returnValue = true;
					rangeValidated = true;
				}
			}
		}
		if (rangeKeyType == 2) {
			StringTokenizer workTokenizer = new StringTokenizer(rangeKeyFieldSearch, "." );
			String workTableAlias = workTokenizer.nextToken();
			String workFieldID = workTokenizer.nextToken();
			Object valueKey = dialog_.getDetailColumnObjectByID("", workTableAlias, workFieldID).getInternalValue();
			Object valueFrom = operator.getValueOf(rangeKeyFieldValid);
			Object valueThru = operator.getValueOf(rangeKeyFieldExpire);
			if (valueThru == null) {
				int comp1 = valueKey.toString().compareTo(valueFrom.toString());
				if (comp1 >= 0) {
					returnValue = true;
				}
			} else {
				int comp1 = valueKey.toString().compareTo(valueFrom.toString());
				int comp2 = valueKey.toString().compareTo(valueThru.toString());
				if (comp1 >= 0 && comp2 < 0) {
					returnValue = true;
				}
			}
		}
		return returnValue;
	}

	public void setKeyFieldValues(XFHashMap keyValues){
		for (int i = 0; i < withKeyFieldIDList.size(); i++) {
			for (int j = 0; j < dialog_.getDetailColumnList().size(); j++) {
				if (dialog_.getDetailColumnList().get(j).getDataSourceName().equals(withKeyFieldIDList.get(i))) {
					dialog_.getDetailColumnList().get(j).setValue(keyValues.getValue(toKeyFieldIDList.get(i)));
					break;
				}
			}
		}
	}

	public void setErrorOnRelatedFields() {
		boolean noneOfKeyFieldsWereSetError = true;

		//////////////////////////////////////////////////
		// Set error on the visible edit-able key field //
		//////////////////////////////////////////////////
		for (int i = 0; i < toKeyFieldIDList.size(); i++) {
			for (int j = 0; j < dialog_.getDetailColumnList().size(); j++) {
				if (dialog_.getDetailColumnList().get(j).isVisibleOnPanel()
						&& dialog_.getDetailColumnList().get(j).isEditable()
						&& dialog_.getDetailColumnList().get(j).getDataSourceName().equals(withKeyFieldIDList.get(i))
						&& !dialog_.getDetailColumnList().get(j).isError()) {
					dialog_.getDetailColumnList().get(j).setError(tableElement.getAttribute("Name") + XFUtility.RESOURCE.getString("FunctionError45"));
					noneOfKeyFieldsWereSetError = false;
					break;
				}
			}
		}

		if (noneOfKeyFieldsWereSetError) {
			////////////////////////////////////////////////////////
			// Set error on the visible edit-able attribute field //
			////////////////////////////////////////////////////////
			for (int i = 0; i < fieldIDList.size(); i++) {
				for (int j = 0; j < dialog_.getDetailColumnList().size(); j++) {
					if (dialog_.getDetailColumnList().get(j).isVisibleOnPanel()
							&& !dialog_.getDetailColumnList().get(j).isNonEditableField()
							&& dialog_.getDetailColumnList().get(j).getFieldID().equals(fieldIDList.get(i))
							&& dialog_.getDetailColumnList().get(j).getTableAlias().equals(this.tableAlias)
							&& !dialog_.getDetailColumnList().get(j).isError()) {
						dialog_.getDetailColumnList().get(j).setError(tableElement.getAttribute("Name") + XFUtility.RESOURCE.getString("FunctionError45"));
						noneOfKeyFieldsWereSetError = false;
						break;
					}
				}
			}
		}

		if (noneOfKeyFieldsWereSetError) {
			//////////////////////////////////////
			// Set error on the first key field //
			//////////////////////////////////////
			for (int i = 0; i < toKeyFieldIDList.size(); i++) {
				for (int j = 0; j < dialog_.getDetailColumnList().size(); j++) {
					if (dialog_.getDetailColumnList().get(j).getDataSourceName().equals(withKeyFieldIDList.get(i))
							&& !dialog_.getDetailColumnList().get(j).isError()) {
						dialog_.getDetailColumnList().get(j).setError(tableElement.getAttribute("Name") + XFUtility.RESOURCE.getString("FunctionError45"));
						break;
					}
				}
			}
		}
	}
}

class XF310_HeaderComboBox extends JPanel implements XFEditableField {
	private static final long serialVersionUID = 1L;
	private String dataTypeOptions_ = "";
	private String tableID = "";
	private String tableAlias = "";
	private String fieldID = "";
	private int rows_ = 1;
	private String listType = "";
	private ArrayList<String> kubunKeyValueList = new ArrayList<String>();
	private ArrayList<XFHashMap> tableKeyValuesList = new ArrayList<XFHashMap>();
	private JTextField jTextField = new JTextField();
	private JComboBox jComboBox = new JComboBox();
	private boolean isEditable = true;
	private ArrayList<String> keyFieldList = new ArrayList<String>();
	private XF310_HeaderReferTable referTable_ = null;
	private XF310 dialog_;
	private String oldValue = "";
	
	public XF310_HeaderComboBox(String dataSourceName, String dataTypeOptions, XF310 dialog, XF310_HeaderReferTable chainTable, boolean isNullable){
		super();
		StringTokenizer workTokenizer;
		org.w3c.dom.Element workElement;
		int fieldWidth = 0;
		String wrk = "";
		String strWrk;

		dataTypeOptions_ = dataTypeOptions;
		workTokenizer = new StringTokenizer(dataSourceName, "." );
		tableAlias = workTokenizer.nextToken();
		tableID = tableAlias;
		referTable_ = chainTable;
		if (referTable_ != null && referTable_.getTableAlias().equals(tableAlias)) {
			tableID = referTable_.getTableID();
		}
		fieldID =workTokenizer.nextToken();
		dialog_ = dialog;

		jTextField.setFont(new java.awt.Font("Dialog", 0, 14));
		jTextField.setEditable(false);
		jTextField.setFocusable(false);
		FontMetrics metrics = jTextField.getFontMetrics(new java.awt.Font("Dialog", 0, 14));
		jComboBox.setFont(new java.awt.Font("Dialog", 0, 14));
		jComboBox.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(KeyEvent e)  {
			    if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0){
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						jComboBox.showPopup();
					}
				}
				if (e.getKeyCode() == KeyEvent.VK_ENTER && !jComboBox.isPopupVisible()) {
					requestFocus();
					dispatchEvent(e);
				}
			}
		});
		jComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (referTable_ != null
						&& isEditable
						&& jComboBox.getSelectedIndex() > -1) {
					referTable_.setKeyFieldValues(tableKeyValuesList.get(jComboBox.getSelectedIndex()));
				}
			}
		});
		jComboBox.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuCanceled(PopupMenuEvent arg0) {
			}
			public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
			}
			public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
				setupRecordList();
			}
		});

		strWrk = XFUtility.getOptionValueWithKeyword(dataTypeOptions_, "VALUES");
		if (!strWrk.equals("")) {
			listType = "VALUES_LIST";
			if (isNullable) {
				jComboBox.addItem("");
			}
			workTokenizer = new StringTokenizer(strWrk, ";" );
			while (workTokenizer.hasMoreTokens()) {
				wrk = workTokenizer.nextToken();
				jComboBox.addItem(wrk);
				if (metrics.stringWidth(wrk) > fieldWidth) {
					fieldWidth = metrics.stringWidth(wrk);
				}
			}
			fieldWidth = fieldWidth + 30;
		} else {
			strWrk = XFUtility.getOptionValueWithKeyword(dataTypeOptions_, "KUBUN");
			if (!strWrk.equals("")) {
				listType = "KUBUN_LIST";
				if (isNullable) {
					kubunKeyValueList.add("");
					jComboBox.addItem("");
				}
				try {
					XFTableOperator operator = dialog_.createTableOperator("Select", dialog_.getSession().getTableNameOfUserVariants());
					operator.addKeyValue("IDUSERKUBUN", strWrk);
					operator.setOrderBy("SQLIST");
					while (operator.next()) {
						kubunKeyValueList.add(operator.getValueOf("KBUSERKUBUN").toString().trim());
						wrk = operator.getValueOf("TXUSERKUBUN").toString().trim();
						jComboBox.addItem(wrk);
						if (metrics.stringWidth(wrk) > fieldWidth) {
							fieldWidth = metrics.stringWidth(wrk);
						}
					}
					fieldWidth = fieldWidth + 30;
					if (jComboBox.getItemCount() == 0) {
						JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("FunctionError24") + dataSourceName + XFUtility.RESOURCE.getString("FunctionError25"));
					}
				} catch (Exception e) {
					e.printStackTrace(dialog_.getExceptionStream());
					dialog_.setErrorAndCloseFunction();
				}

			} else {
				if (referTable_ != null) {
					listType = "RECORDS_LIST";
					keyFieldList = referTable_.getKeyFieldIDList();
					workElement = dialog_.getSession().getFieldElement(tableID, fieldID);
					ArrayList<String> workDataTypeOptionList = XFUtility.getOptionList(workElement.getAttribute("TypeOptions"));
					int dataSize = Integer.parseInt(workElement.getAttribute("Size"));
					if (workDataTypeOptionList.contains("KANJI")) {
						fieldWidth = dataSize * 14 + 20;
					} else {
						fieldWidth = dataSize * 7 + 30;
					}
					if (fieldWidth > 800) {
						fieldWidth = 800;
					}
				}
			}
		}

		this.setSize(new Dimension(fieldWidth, XFUtility.FIELD_UNIT_HEIGHT));
		this.setLayout(new BorderLayout());
		this.add(jComboBox, BorderLayout.CENTER);
	}

	public void setupRecordList() {
		if (referTable_ != null && listType.equals("RECORDS_LIST")) {
			String selectedItemValue = "";
			if (jComboBox.getSelectedIndex() >= 0) {
				selectedItemValue = jComboBox.getItemAt(jComboBox.getSelectedIndex()).toString();
			}

			tableKeyValuesList.clear();
			jComboBox.removeAllItems();

			boolean blankItemRequired = false;
			XFHashMap blankKeyValues = new XFHashMap();
			for (int i = 0; i < referTable_.getWithKeyFieldIDList().size(); i++) {
				for (int j = 0; j < dialog_.getHeaderFieldList().size(); j++) {
					if (referTable_.getWithKeyFieldIDList().get(i).equals(dialog_.getHeaderFieldList().get(j).getTableAlias() + "." + dialog_.getHeaderFieldList().get(j).getFieldID())) {
						if (dialog_.getHeaderFieldList().get(j).isNullable()) {
							blankItemRequired = true;
							//blankKeyValues.addValue(referTable_.getWithKeyFieldIDList().get(i), dialog_.getHeaderFieldList().get(j).getNullValue());
							if (dialog_.getHeaderFieldList().get(j).isVisibleOnPanel()) {
								blankKeyValues.addValue(referTable_.getWithKeyFieldIDList().get(i), dialog_.getHeaderFieldList().get(j).getValue());
							} else {
								blankKeyValues.addValue(referTable_.getWithKeyFieldIDList().get(i), dialog_.getHeaderFieldList().get(j).getNullValue());
							}
						} else {
							blankKeyValues.addValue(referTable_.getWithKeyFieldIDList().get(i), dialog_.getHeaderFieldList().get(j).getValue());
						}
					}
				}
			}
			if (blankItemRequired) {
				tableKeyValuesList.add(blankKeyValues);
				jComboBox.addItem("");
			}

			try {
				XFHashMap keyValues;
				XFTableOperator operator = dialog_.createTableOperator(referTable_.getSelectSQL(true));
				while (operator.next()) {
					if (referTable_.isRecordToBeSelected(operator)) {
						keyValues = new XFHashMap();
						for (int i = 0; i < keyFieldList.size(); i++) {
							keyValues.addValue(referTable_.getWithKeyFieldIDList().get(i), operator.getValueOf(keyFieldList.get(i)));
						}
						tableKeyValuesList.add(keyValues);
						jComboBox.addItem(operator.getValueOf(fieldID).toString().trim());
					}
				}
			} catch (Exception e) {
				e.printStackTrace(dialog_.getExceptionStream());
				dialog_.setErrorAndCloseFunction();
			}

			jComboBox.setSelectedItem(selectedItemValue);
		}
	}
	
	public boolean hasEditControlledKey() {
		boolean anyOfKeysAreEditControlled = false;
		for (int i = 0; i < referTable_.getWithKeyFieldIDList().size(); i++) {
			for (int j = 0; j < dialog_.getHeaderFieldList().size(); j++) {
				if (referTable_.getWithKeyFieldIDList().get(i).equals(dialog_.getHeaderFieldList().get(j).getTableAlias() + "." + dialog_.getHeaderFieldList().get(j).getFieldID())) {
					if (!dialog_.getHeaderFieldList().get(j).isEditable() && dialog_.getHeaderTable().getTableID().equals(dialog_.getHeaderFieldList().get(j).getTableAlias())) {
						anyOfKeysAreEditControlled = true;
						break;
					}
				}
			}
		}
		return anyOfKeysAreEditControlled;
	}
	
	public void setFollowingField(XFEditableField field) {
	}
	
	public void setEditable(boolean editable) {
		this.removeAll();
		if (editable) {
			this.add(jComboBox, BorderLayout.CENTER);
		} else {
			this.add(jTextField, BorderLayout.CENTER);
		}
		isEditable = editable;
	}
	
	public void setToolTipText(String text) {
		jComboBox.setToolTipText(text);
		jTextField.setToolTipText(text);
	}

	public Object getInternalValue() {
		String value = "";
		if (jComboBox.getSelectedIndex() >= 0) {
			if (listType.equals("VALUES_LIST")) {
				value = jComboBox.getItemAt(jComboBox.getSelectedIndex()).toString();
			}
			if (listType.equals("KUBUN_LIST")) {
				value = kubunKeyValueList.get(jComboBox.getSelectedIndex());
			}
			if (listType.equals("RECORDS_LIST")) {
				value = jComboBox.getItemAt(jComboBox.getSelectedIndex()).toString();
			}
		}
		return value;
	}
	
	public void setOldValue(Object obj) {
		oldValue = (String)obj;
	}

	public Object getOldValue() {
		return oldValue;
	}

	public Object getExternalValue() {
		return jComboBox.getItemAt(jComboBox.getSelectedIndex()).toString();
	}
	
	public boolean isEditable() {
		return isEditable;
	}

	public boolean isComponentFocusable() {
		return jComboBox.isFocusable();
	}

	public boolean isFocusable() {
		return false;
	}
	
	public void setValue(Object obj) {
		String value = (String)obj;
		value = value.trim();
		if (listType.equals("VALUES_LIST")) {
			for (int i = 0; i < jComboBox.getItemCount(); i++) {
				if (jComboBox.getItemAt(i).toString().equals(value)) {
					jComboBox.setSelectedIndex(i);
					break;
				}
			}
		}
		if (listType.equals("KUBUN_LIST")) {
			for (int i = 0; i < kubunKeyValueList.size(); i++) {
				if (kubunKeyValueList.get(i).equals(value)) {
					jComboBox.setSelectedIndex(i);
					break;
				}
			}
		}
		if (listType.equals("RECORDS_LIST")) {
			setupRecordList();
			if (jComboBox.getItemCount() > 0) {
				if (value == null || value.equals("")) {
				} else {
					for (int i = 0; i < jComboBox.getItemCount(); i++) {
						if (jComboBox.getItemAt(i).toString().equals(value)) {
							if (i != jComboBox.getSelectedIndex()) {
								jComboBox.setSelectedIndex(i);
							}
							break;
						}
					}
				}
			}
		}
		if (jComboBox.getSelectedIndex() >= 0) {
			jTextField.setText(jComboBox.getItemAt(jComboBox.getSelectedIndex()).toString());
		}
	}
	
	public void setWidth(int width) {
		jComboBox.setSize(width, jComboBox.getHeight());
		jTextField.setSize(width - 17, jTextField.getHeight());
	}

	public void setBackground(Color color) {
		if (jComboBox != null) {
			jComboBox.setBackground(color);
		}
	}

	public int getRows() {
		return rows_;
	}
}

class XF310_HeaderPromptCall extends JPanel implements XFEditableField {
	private static final long serialVersionUID = 1L;
	private String tableID = "";
	private String tableAlias = "";
	private String fieldID = "";
	private int rows_ = 1;
	private XFTextField xFTextField;
	private JButton jButton = new JButton();
	private boolean isEditable = true;
    private XF310 dialog_;
    private String functionID_;
    private org.w3c.dom.Element fieldElement_;
    private ArrayList<XF310_HeaderReferTable> referTableList_;
    private String oldValue = "";
    private ArrayList<String> fieldsToPutList_ = new ArrayList<String>();
    private ArrayList<String> fieldsToPutToList_ = new ArrayList<String>();
    private ArrayList<String> fieldsToGetList_ = new ArrayList<String>();
    private ArrayList<String> fieldsToGetToList_ = new ArrayList<String>();

	public XF310_HeaderPromptCall(org.w3c.dom.Element fieldElement, String functionID, XF310 dialog){
		super();
		fieldElement_ = fieldElement;
		functionID_ = functionID;
		dialog_ = dialog;

		String fieldOptions = fieldElement_.getAttribute("FieldOptions");
		StringTokenizer workTokenizer = new StringTokenizer(fieldElement_.getAttribute("DataSource"), "." );
		tableAlias = workTokenizer.nextToken();
		fieldID =workTokenizer.nextToken();

		tableID = tableAlias;
		referTableList_ = dialog_.getHeaderReferTableList();
		for (int i = 0; i < referTableList_.size(); i++) {
			if (referTableList_.get(i).getTableAlias().equals(tableAlias)) {
				tableID = referTableList_.get(i).getTableID();
				break;
			}
		}

		org.w3c.dom.Element workElement = dialog_.getSession().getFieldElement(tableID, fieldID);
		if (workElement == null) {
			JOptionPane.showMessageDialog(this, tableID + "." + fieldID + XFUtility.RESOURCE.getString("FunctionError11"));
		}
		String dataType = workElement.getAttribute("Type");
		String dataTypeOptions = workElement.getAttribute("TypeOptions");
		int dataSize = Integer.parseInt(workElement.getAttribute("Size"));
		if (dataSize > 50) {
			dataSize = 50;
		}
		int decimalSize = 0;
		if (!workElement.getAttribute("Decimal").equals("")) {
			decimalSize = Integer.parseInt(workElement.getAttribute("Decimal"));
		}

		String wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "PROMPT_CALL_TO_PUT");
		if (!wrkStr.equals("")) {
			workTokenizer = new StringTokenizer(wrkStr, ";" );
			while (workTokenizer.hasMoreTokens()) {
				fieldsToPutList_.add(workTokenizer.nextToken());
			}
		}
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "PROMPT_CALL_TO_PUT_TO");
		if (!wrkStr.equals("")) {
			workTokenizer = new StringTokenizer(wrkStr, ";" );
			while (workTokenizer.hasMoreTokens()) {
				fieldsToPutToList_.add(workTokenizer.nextToken());
			}
		}
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "PROMPT_CALL_TO_GET");
		if (!wrkStr.equals("")) {
			workTokenizer = new StringTokenizer(wrkStr, ";" );
			while (workTokenizer.hasMoreTokens()) {
				fieldsToGetList_.add(workTokenizer.nextToken());
			}
		}
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "PROMPT_CALL_TO_GET_TO");
		if (!wrkStr.equals("")) {
			workTokenizer = new StringTokenizer(wrkStr, ";" );
			while (workTokenizer.hasMoreTokens()) {
				fieldsToGetToList_.add(workTokenizer.nextToken());
			}
		}

		xFTextField = new XFTextField(XFUtility.getBasicTypeOf(dataType), dataSize, decimalSize, dataTypeOptions, fieldOptions);
		xFTextField.setEditable(false);
		xFTextField.setFocusable(false);
		xFTextField.setLocation(5, 0);

		ImageIcon imageIcon = new ImageIcon(xeadDriver.XF310.class.getResource("prompt.png"));
	 	jButton.setIcon(imageIcon);
		jButton.setPreferredSize(new Dimension(26, XFUtility.FIELD_UNIT_HEIGHT));
		jButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object value;
				try {
					setCursor(new Cursor(Cursor.WAIT_CURSOR));
					HashMap<String, Object> fieldValuesMap = new HashMap<String, Object>();
					for (int i = 0; i < fieldsToPutList_.size(); i++) {
						value = dialog_.getValueOfHeaderFieldByName(fieldsToPutList_.get(i));
						if (value != null) {
							fieldValuesMap.put(fieldsToPutToList_.get(i), value);
						}
					}
					HashMap<String, Object> returnMap = dialog_.getSession().executeFunction(functionID_, fieldValuesMap);
					if (!returnMap.get("RETURN_CODE").equals("99")) {
						HashMap<String, Object> fieldsToGetMap = new HashMap<String, Object>();
						for (int i = 0; i < fieldsToGetList_.size(); i++) {
							value = returnMap.get(fieldsToGetList_.get(i));
							if (value != null) {
								fieldsToGetMap.put(fieldsToGetToList_.get(i), value);
							}
						}
						for (int i = 0; i < dialog_.getHeaderFieldList().size(); i++) {
							value = fieldsToGetMap.get(dialog_.getHeaderFieldList().get(i).getDataSourceName());
							if (value != null) {
								dialog_.getHeaderFieldList().get(i).setValue(value);
							}
						}
					}
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(null, ex.getMessage());
				} finally {
					setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
			}
		});
		jButton.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(KeyEvent e)  {
			    if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0){
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						jButton.doClick();
					}
				}
			}
		});

		this.setSize(new Dimension(xFTextField.getWidth() + 27, XFUtility.FIELD_UNIT_HEIGHT));
		this.setLayout(new BorderLayout());
		this.add(xFTextField, BorderLayout.CENTER);
		this.add(jButton, BorderLayout.EAST);
	}
	
	public boolean hasEditControlledKey() {
		boolean anyOfKeysAreEditControlled = false;
		for (int i = 0; i < fieldsToGetToList_.size(); i++) {
			for (int j = 0; j < dialog_.getHeaderFieldList().size(); j++) {
				if (fieldsToGetToList_.get(i).equals(dialog_.getHeaderFieldList().get(j).getTableAlias() + "." + dialog_.getHeaderFieldList().get(j).getFieldID())) {
					if (!dialog_.getHeaderFieldList().get(j).isEditable() && dialog_.getHeaderTable().getTableID().equals(dialog_.getHeaderFieldList().get(j).getTableAlias())) {
						anyOfKeysAreEditControlled = true;
						break;
					}
				}
			}
		}
		return anyOfKeysAreEditControlled;
	}

	public void setEditable(boolean editable) {
		isEditable = editable;
	}
	
	public void setToolTipText(String text) {
		jButton.setToolTipText(text);
		xFTextField.setToolTipText(text);
	}

	public Object getInternalValue() {
		return xFTextField.getText();
	}

	public Object getExternalValue() {
		return xFTextField.getText();
	}
	
	public void setOldValue(Object obj) {
		oldValue = (String)obj;
	}

	public Object getOldValue() {
		return oldValue;
	}

	public String getValueClass() {
		return "String";
	}
	
	public void setValue(Object obj) {
		if (obj == null) {
			xFTextField.setText("");
		} else {
			xFTextField.setText(obj.toString());
		}
	}
	
	public void setWidth(int width) {
		xFTextField.setWidth(width - 27);
	}

	public void setBackground(Color color) {
		if (xFTextField != null) {
			if (color.equals(XFUtility.ACTIVE_COLOR)) {
				if (xFTextField.isEditable()) {
					xFTextField.setBackground(XFUtility.ACTIVE_COLOR);
				} else {
					xFTextField.setBackground(XFUtility.INACTIVE_COLOR);
				}
			}
			if (color.equals(XFUtility.ERROR_COLOR)) {
				xFTextField.setBackground(XFUtility.ERROR_COLOR);
			}
		}
	}

	public int getRows() {
		return rows_;
	}

	public boolean isEditable() {
		return isEditable;
	}

	public boolean isComponentFocusable() {
		return isEditable;
	}

	public boolean isFocusable() {
		return false;
	}
}

class XF310_KeyInputDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private JPanel jPanelMain = new JPanel();
	private JPanel jPanelKeyFields = new JPanel();
	private JPanel jPanelTop = new JPanel();
	private JPanel jPanelBottom = new JPanel();
	private JPanel jPanelButtons = new JPanel();
	private JPanel jPanelInfo = new JPanel();
	private GridLayout gridLayoutInfo = new GridLayout();
	private JLabel jLabelFunctionID = new JLabel();
	private JLabel jLabelSessionID = new JLabel();
	private JScrollPane jScrollPaneMessages = new JScrollPane();
	private JTextArea jTextAreaMessages = new JTextArea();
	private JButton jButtonCancel = new JButton();
	private JButton jButtonOK = new JButton();
	private XF310 dialog_;
	private final int FIELD_VERTICAL_MARGIN = 5;
	private final int FONT_SIZE = 14;
	private ArrayList<XF310_HeaderField> fieldList = new ArrayList<XF310_HeaderField>();
	private HashMap<String, Object> keyMap_ = new HashMap<String, Object>();
	
	public XF310_KeyInputDialog(XF310 dialog) {
		super(dialog, "", true);
		dialog_ = dialog;
		initComponentsAndVariants();
	}

	void initComponentsAndVariants() {
		jPanelMain.setLayout(new BorderLayout());
		jPanelTop.setLayout(new BorderLayout());
		jPanelKeyFields.setLayout(null);
		jPanelKeyFields.setFocusable(false);
		jTextAreaMessages.setEditable(false);
		jTextAreaMessages.setBorder(BorderFactory.createEtchedBorder());
		jTextAreaMessages.setFont(new java.awt.Font("SansSerif", 0, FONT_SIZE));
		jTextAreaMessages.setFocusable(false);
		jTextAreaMessages.setLineWrap(true);
		jScrollPaneMessages.getViewport().add(jTextAreaMessages, null);
		jScrollPaneMessages.setPreferredSize(new Dimension(10, 35));
		jPanelBottom.setPreferredSize(new Dimension(10, 35));
		jPanelBottom.setLayout(new BorderLayout());
		jPanelBottom.setBorder(null);
		jLabelFunctionID.setFont(new java.awt.Font("Dialog", 0, FONT_SIZE));
		jLabelFunctionID.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabelFunctionID.setForeground(Color.gray);
		jLabelFunctionID.setFocusable(false);
		jLabelSessionID.setFont(new java.awt.Font("Dialog", 0, FONT_SIZE));
		jLabelSessionID.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabelSessionID.setForeground(Color.gray);
		jLabelSessionID.setFocusable(false);
		jPanelButtons.setBorder(null);
		jPanelButtons.setLayout(null);
		jPanelButtons.setFocusable(false);
		gridLayoutInfo.setColumns(1);
		gridLayoutInfo.setRows(2);
		gridLayoutInfo.setVgap(4);
		jPanelInfo.setLayout(gridLayoutInfo);
		jPanelInfo.add(jLabelSessionID);
		jPanelInfo.add(jLabelFunctionID);
		jPanelInfo.setFocusable(false);
		jPanelMain.add(jPanelTop, BorderLayout.CENTER);
		jPanelMain.add(jPanelBottom, BorderLayout.SOUTH);
		jPanelTop.add(jPanelKeyFields, BorderLayout.CENTER);
		jPanelTop.add(jScrollPaneMessages, BorderLayout.SOUTH);
		jPanelBottom.add(jPanelInfo, BorderLayout.EAST);
		jPanelBottom.add(jPanelButtons, BorderLayout.CENTER);
		jButtonCancel.setFont(new java.awt.Font("Dialog", 0, FONT_SIZE));
		jButtonCancel.setText(XFUtility.RESOURCE.getString("Cancel"));
		jButtonCancel.setBounds(new Rectangle(5, 2, 80, 32));
		jButtonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				keyMap_.clear();
				setVisible(false);
			}
		});
		jButtonOK.setFont(new java.awt.Font("Dialog", 0, FONT_SIZE));
		jButtonOK.setText("OK");
		jButtonOK.setBounds(new Rectangle(200, 2, 80, 32));
		jButtonOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean anyOfFieldsAreNull = false;
				for (int i = 0; i < fieldList.size(); i++) {
					if (fieldList.get(i).getInternalValue().equals(fieldList.get(i).getNullValue())) {
						anyOfFieldsAreNull = true;
						break;
					} else {
						keyMap_.put(fieldList.get(i).getFieldID(), fieldList.get(i).getInternalValue());
					}
				}
				if (anyOfFieldsAreNull) {
					jTextAreaMessages.setText(XFUtility.RESOURCE.getString("FunctionError23"));
				} else {
					setVisible(false);
				}
			}
		});
		jPanelButtons.add(jButtonCancel);
		jPanelButtons.add(jButtonOK);
		this.getRootPane().setDefaultButton(jButtonOK);
		this.getContentPane().add(jPanelMain, BorderLayout.CENTER);

		StringTokenizer workTokenizer;
		String tableAlias, tableID, fieldID;
		org.w3c.dom.Element element;
		int posX = 0;
		int posY = 0;
		int biggestWidth = 0;
		int biggestHeight = 0;
		Dimension dim = new Dimension();
		Dimension dimOfPriviousField = new Dimension();
		boolean topField = true;
		XF310_HeaderField field;

		this.setTitle(dialog_.getTitle());
		jLabelSessionID.setText(dialog_.getSession().getSessionID());
		jLabelFunctionID.setText(dialog_.getFunctionInfo());
		FontMetrics metrics = jLabelFunctionID.getFontMetrics(new java.awt.Font("Dialog", 0, FONT_SIZE));
		jPanelInfo.setPreferredSize(new Dimension(metrics.stringWidth(jLabelFunctionID.getText()), 35));

		NodeList headerFieldElementList = dialog_.getFunctionElement().getElementsByTagName("Field");
		SortableDomElementListModel sortingList1 = XFUtility.getSortedListModel(headerFieldElementList, "Order");
		for (int i = 0; i < sortingList1.getSize(); i++) {
			element = (org.w3c.dom.Element)sortingList1.getElementAt(i);
			workTokenizer = new StringTokenizer(element.getAttribute("DataSource"), "." );
			tableAlias = workTokenizer.nextToken();
			tableID = dialog_.getTableIDOfTableAlias(tableAlias);
			fieldID =workTokenizer.nextToken();

			if (tableID.equals(dialog_.getHeaderTable().getTableID())) {
				ArrayList<String> keyFieldList = dialog_.getHeaderTable().getKeyFieldIDList();
				for (int j = 0; j < keyFieldList.size(); j++) {
					if (keyFieldList.get(j).equals(fieldID)) {
						field = new XF310_HeaderField((org.w3c.dom.Element)sortingList1.getElementAt(i), dialog_);
						field.setEditable(true);
						fieldList.add(field);

						if (topField) {
							posX = 0;
							posY = this.FIELD_VERTICAL_MARGIN + 8;
							topField = false;
						} else {
							posX = 0;
							posY = posY + dimOfPriviousField.height+ field.getPositionMargin() + this.FIELD_VERTICAL_MARGIN;
						}
						dim = field.getPreferredSize();
						field.setBounds(posX, posY, dim.width, dim.height);
						jPanelKeyFields.add(field);

						if (posX + dim.width > biggestWidth) {
							biggestWidth = posX + dim.width;
						}
						if (posY + dim.height > biggestHeight) {
							biggestHeight = posY + dim.height;
						}
						dimOfPriviousField = new Dimension(dim.width, dim.height);
					}
				}
			}
		}

		int width = 450;
		if (biggestWidth > 430) {
			width = biggestWidth + 20;
		}
		int height = biggestHeight + 117;
		this.setPreferredSize(new Dimension(width, height));
		this.pack();
	}
	
	public HashMap<String, Object> requestKeyValues(String message) {
		keyMap_.clear();
		if (message.equals("")) {
			jTextAreaMessages.setText(XFUtility.RESOURCE.getString("FunctionMessage29"));
		} else {
			jTextAreaMessages.setText(message);
		}
		Rectangle screenRect = dialog_.getSession().getMenuRectangle();
		int posX = (screenRect.width - (int)this.getPreferredSize().getWidth()) / 2 + screenRect.x;
		int posY = (screenRect.height - (int)this.getPreferredSize().getHeight()) / 2 + screenRect.y;
		this.setLocation(posX, posY);
		this.setVisible(true);
		return keyMap_;
	}
}

class XF310_ReferCheckerConstructor implements Runnable {
	XF310 adaptee;
	XF310_ReferCheckerConstructor(XF310 adaptee) {
		this.adaptee = adaptee;
	}
	public void run() {
		ReferChecker headerChecker = adaptee.getSession().createReferChecker(adaptee.getHeaderTable().getTableID(), adaptee);
		adaptee.setHeaderReferChecker(headerChecker);
		ReferChecker detailChecker = adaptee.getSession().createReferChecker(adaptee.getDetailTable().getTableID(), adaptee);
		adaptee.setDetailReferChecker(detailChecker);
	}
}

class XF310_jTableMain_focusAdapter extends java.awt.event.FocusAdapter {
	XF310 adaptee;
	XF310_jTableMain_focusAdapter(XF310 adaptee) {
		this.adaptee = adaptee;
	}
	public void focusGained(FocusEvent e) {
		adaptee.jTableMain_focusGained(e);
	}
	public void focusLost(FocusEvent e) {
		adaptee.jTableMain_focusLost(e);
	}
}

class XF310_jScrollPaneTable_mouseAdapter extends java.awt.event.MouseAdapter {
	XF310 adaptee;
	XF310_jScrollPaneTable_mouseAdapter(XF310 adaptee) {
		this.adaptee = adaptee;
	}
	public void mousePressed(MouseEvent e) {
		adaptee.jScrollPaneTable_mousePressed(e);
	}
}

class XF310_FunctionButton_actionAdapter implements java.awt.event.ActionListener {
	XF310 adaptee;
	XF310_FunctionButton_actionAdapter(XF310 adaptee) {
		this.adaptee = adaptee;
	}
	public void actionPerformed(ActionEvent e) {
		adaptee.jFunctionButton_actionPerformed(e);
	}
}
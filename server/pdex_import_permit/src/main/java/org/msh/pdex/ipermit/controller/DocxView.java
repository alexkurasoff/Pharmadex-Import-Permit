package org.msh.pdex.ipermit.controller;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.msh.pdex.dto.tables.TableCell;
import org.msh.pdex.dto.tables.TableHeader;
import org.msh.pdex.dto.tables.TableQtb;
import org.msh.pdex.dto.tables.TableRow;
import org.msh.pdex.exceptions.ObjectNotFoundException;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.view.AbstractView;

/**
 * Create a view from MS Word docx file
 * @author alexk
 *
 */
public class DocxView extends AbstractView {

	private static final Logger logger = LoggerFactory.getLogger(DocxView.class);
	/**
	 * pattern to find parameter definition
	 */
	private static Pattern pat = Pattern.compile("[\\#,\\$]\\{.+?\\}");
	/**
	 * Byte input stream to read the Word document from a file or from BLOB database field
	 */
	private InputStream inputDocument;
	/**
	 * How many parameters has been resolved
	 */
	private int resolved = 0;
	private XWPFDocument doc;
	private List<ParagraphByTableQtb> listByPaint = null;

	class ParagraphByTableQtb{
		private XWPFTableCell cell = null;
		private XWPFParagraph paragraph = null;
		private TableQtb table = null;
		
		public ParagraphByTableQtb(XWPFTableCell cell, XWPFParagraph paragraph, TableQtb table) {
			super();
			this.cell = cell;
			this.paragraph = paragraph;
			this.table = table;
		}
		public XWPFTableCell getCell() {
			return cell;
		}
		public XWPFParagraph getParagraph() {
			return paragraph;
		}
		public TableQtb getTable() {
			return table;
		}
	}
	
	public DocxView(InputStream _inputDocument) {
		super();
		setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
		this.inputDocument=_inputDocument;
	}

	public InputStream getInputDocument() {
		return inputDocument;
	}

	public void setInputDocument(InputStream inputDocument) {
		this.inputDocument = inputDocument;
	}

	public int getResolved() {
		return resolved;
	}

	public void setResolved(int resolved) {
		this.resolved = resolved;
	}

	private void incResolved(int count) {
		this.resolved += count;
	}
	
	public XWPFDocument getDoc() {
		return doc;
	}

	public void setDoc(XWPFDocument doc) {
		this.doc = doc;
	}

	@Override
	protected boolean generatesDownloadContent() {
		return true;
	}

	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		resolveDocument(model);
		response.setContentType(getContentType());
		ServletOutputStream out = response.getOutputStream();
		getDoc().write(out);
		getDoc().close();
	}

	/**
	 * Resolve model parameters in the doc
	 * @param doc
	 * @param model 
	 * @throws ObjectNotFoundException 
	 * @throws IOException 
	 * @throws InvalidFormatException 
	 */
	public void resolveDocument(Map<String, Object> model) throws ObjectNotFoundException, IOException{

//		Runtime.getRuntime().gc();
//		long initialHeap = Runtime.getRuntime().freeMemory(); 
//		System.out.println("Initial heap is "+initialHeap);
		setDoc(new XWPFDocument(getInputDocument()));
//		Runtime.getRuntime().gc();
//		long heapAfterLoad = Runtime.getRuntime().freeMemory(); 
//		System.out.println("Heap after load is "+heapAfterLoad +" using " + (initialHeap-heapAfterLoad));
		getInputDocument().close();
		// fill out the first level EL values
		for (XWPFParagraph par : getDoc().getParagraphs()){
			resolveParagraph(par, null, model);
		}
		//sometimes, the document's layout may be represent as a table or a set of tables
		resolveTables(getDoc().getTables(), model);
		//some EL may represents a table
		if(listByPaint != null) {
			for(ParagraphByTableQtb it:listByPaint) {
				paintTable(it.getCell(), it.getParagraph(), it.getTable());
			}
		}
	}

	/**
	 *Resolve paragraphs inside the table. It is because a document layout can be represents as a table
	 *This procedure is recursive - a table inside a table
	 */
	private void resolveTables(List<XWPFTable> listTable, Map<String, Object> model) throws ObjectNotFoundException {
		for (XWPFTable tbl : listTable) {
			for (XWPFTableRow row : tbl.getRows()) {
				for (XWPFTableCell cell : row.getTableCells()) {
					List<XWPFTable> list = cell.getTables();
					if(list != null && list.size() > 0) {
						resolveTables(list, model);
					}else {
						for (XWPFParagraph par : cell.getParagraphs()) {
							resolveParagraph(par, cell, model);
						}
					}
				}
			}
		}
	}

	/**
	 * Resolve paragraph data
	 * @param par paragraph
	 * @param model 
	 * @return quantity of EL
	 * @throws ObjectNotFoundException 
	 */
	private void resolveParagraph(XWPFParagraph par, XWPFTableCell cell, Map<String, Object> model) throws ObjectNotFoundException{
		int ret = 0;
		XWPFRun prevRun = null;
		// normalize runs
		for(XWPFRun run : par.getRuns()){
			if(run != null){
				if(prevRun==null){
					prevRun=run;
				}else{
					if(compareRuns(prevRun,run)){
						prevRun.setText(prevRun.getText(0) + run.getText(0),0);
						run.setText("",0);
					}else{
						prevRun = run;
					}
				}
			}
		}
		// resolve
		for(XWPFRun run:par.getRuns()){
			if(run.getText(0) != null){
				Matcher match = pat.matcher(run.getText(0));
				StringBuffer sb = new StringBuffer();
				while(match.find()){
					String toEval = match.group();
					Object repl = getReplacementFromModel(toEval, model);
					if(repl != null){
						if(repl instanceof String) {
							String replStr = (String) repl;
							match.appendReplacement(sb, replStr); //real evaluation
						}

						if(repl instanceof TableQtb) { 
							match.appendReplacement(sb, ""); 
							TableQtb table = (TableQtb) repl; 
							if(listByPaint == null)
								listByPaint = new ArrayList<ParagraphByTableQtb>();
							ParagraphByTableQtb it = new ParagraphByTableQtb(cell, par, table);
							listByPaint.add(it);
						}
					}else{
						throw new ObjectNotFoundException("Impossibe to evaluate " + toEval, logger);
					}
					ret++;
				}
				match.appendTail(sb);
				run.setText(sb.toString(),0); //test resolve
			}
		}
		
		incResolved(ret);
	}

	/**
	 * Paint a table starting with the run
	 * @param run
	 * @param table
	 */
	private void paintTable(XWPFTableCell cell, XWPFParagraph par, TableQtb table) {
		XWPFTable tableDoc = null;
		XWPFTableRow headerRow = null;
		if(cell == null) {
			tableDoc = par.getDocument().insertNewTbl(par.getCTP().newCursor());
			headerRow = tableDoc.getRow(0);
			headerRow.setRepeatHeader(true);
		}else {
			tableDoc = cell.insertNewTbl(par.getCTP().newCursor());
			headerRow = tableDoc.createRow();
		}
		for(int i = 0; i < table.getHeaders().getHeaders().size(); i++) {
			if (headerRow.getCell(i) == null) {
				headerRow.createCell();
			}
		}

		setTableBorders(tableDoc);
		
		if(tableDoc != null) {
			CTTblWidth tableWidth = tableDoc.getCTTbl().addNewTblPr().addNewTblW();
			tableWidth.setType(STTblWidth.PCT);
			tableWidth.setW(BigInteger.valueOf(100*50));

			tableDoc = createHeaders(tableDoc, headerRow, table.getHeaders().getHeaders());
			for(TableRow row : table.getRows()) {
				createRow(table.getHeaders().getHeaders(), tableDoc, row);
			}
		}
	}

	/**
	 * Add a new row and fill cells by data
	 * @param headers 
	 * @param tableDoc
	 * @param row
	 * @return a new created row
	 */
	private XWPFTableRow createRow(List<TableHeader> headers, XWPFTable tableDoc, TableRow row) {
		XWPFTableRow ret = tableDoc.createRow();
		int cellIndex=0;
		for(TableCell cell :row.getRow()) {
			if (ret.getCell(cellIndex) == null) 
				ret.createCell();
			
			switch(headers.get(cellIndex).getColumnType()) {
			case TableHeader.COLUMN_DECIMAL:
			case TableHeader.COLUMN_LONG:
				setTextAndAlignCell(ParagraphAlignment.RIGHT,cell.getValue(),ret.getCell(cellIndex),false);
				break;
			case TableHeader.COLUMN_LOCALDATE:
			case TableHeader.COLUMN_LOCALDATETIME:
				setTextAndAlignCell(ParagraphAlignment.CENTER,cell.getValue(),ret.getCell(cellIndex),false);
				break;
			default:
				ret.getCell(cellIndex).setText(cell.getValue());
			}
			cellIndex++;
		}
		return ret;
	}

	/**
	 * Create headers just after the current run
	 * @param tableDoc
	 * @param headers
	 * @return 
	 */
	private XWPFTable createHeaders(XWPFTable tableDoc, XWPFTableRow headerRow, List<TableHeader> headers) {
		//create header
		//XWPFTableRow headerRow = tableDoc.getRow(0);
		int cellIndex=0;
		for(TableHeader header : headers) {
			XWPFTableCell cell = headerRow.getCell(cellIndex);
			setTextAndAlignCell(ParagraphAlignment.CENTER, header.getDisplayValue(), cell,true);
			if(header.getExcelWidth()>0) {
				CTTblWidth cellWidth = cell.getCTTc().addNewTcPr().addNewTcW();
				cellWidth.setW(BigInteger.valueOf(header.getExcelWidth()*50));
				cellWidth.setType(STTblWidth.PCT);
			}
			cellIndex++;
		}

		return tableDoc;
	}
	/**
	 * Align a value inside a cell
	 * @param text 
	 * @param header
	 * @param cell
	 * @param isHeader - this cell is from header
	 */
	public void setTextAndAlignCell(ParagraphAlignment align, String text, XWPFTableCell cell, boolean isHeader) {
		XWPFParagraph para = cell.getParagraphs().get(0);
		// create a run to contain the content
		XWPFRun rh = para.createRun();
		String[] lines = text.split("<br>");
		rh.setText(lines[0], 0); // set first line into XWPFRun
        for(int i=1;i<lines.length;i++){
            // add break and insert new text
            rh.addBreak();
            rh.setText(lines[i]);
        }
		rh.setBold(isHeader);
		para.setAlignment(align);
	}

	/**
	 * Replace a parameter expression to a parameter value
	 * in case not found, no replace
	 * Case INSENSETIVE!
	 * @param toEval
	 * @param model
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	private Object getReplacementFromModel(String toEval, Map<String, Object> model) throws ObjectNotFoundException {
		int begIndex = toEval.indexOf("{");
		int endIndex = toEval.indexOf("}");
		String varName = toEval.substring(begIndex + 1, endIndex);
		Object ret = model.get(varName);
		if(ret != null) {
			return ret;
		}else {
			throw new ObjectNotFoundException("Variable not found " + varName, logger);
		}
	}

	/**
	 * set all table borders by insert table
	 * @param tableDoc
	 */
	private void setTableBorders(XWPFTable tableDoc) {
		tableDoc.getCTTbl().addNewTblPr().addNewTblBorders().addNewLeft().setVal(
				org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder.SINGLE);
		tableDoc.getCTTbl().getTblPr().getTblBorders().addNewRight().setVal(
				org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder.SINGLE);
		tableDoc.getCTTbl().getTblPr().getTblBorders().addNewTop().setVal(
				org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder.SINGLE);
		tableDoc.getCTTbl().getTblPr().getTblBorders().addNewBottom().setVal(
				org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder.SINGLE);
		tableDoc.getCTTbl().getTblPr().getTblBorders().addNewInsideH().setVal(
				org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder.SINGLE);
		tableDoc.getCTTbl().getTblPr().getTblBorders().addNewInsideV().setVal(
				org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder.SINGLE);
	}

	/**
	 * Sometimes one paragraph belong to several runs
	 * @param prev
	 * @param actual
	 * @return
	 */
	public boolean compareRuns(XWPFRun prev, XWPFRun actual){
		return	prev.getCharacterSpacing() == actual.getCharacterSpacing()
				&& prev.getKerning() == actual.getKerning()
				&& prev.getSubscript().equals(actual.getSubscript())
				&& prev.getUnderline().equals(actual.getUnderline())
				&& prev.isBold() == actual.isBold()
				&& prev.isCapitalized() == actual.isCapitalized()
				&& prev.isDoubleStrikeThrough() == actual.isDoubleStrikeThrough()
				&& prev.isEmbossed() == actual.isEmbossed()
				&& prev.isHighlighted() == actual.isHighlighted()
				&& prev.isImprinted() == actual.isImprinted()
				&& prev.isItalic() == actual.isItalic()
				&& prev.isShadowed() == actual.isShadowed()
				&& prev.isSmallCaps() == actual.isSmallCaps()
				&& prev.isStrikeThrough() == actual.isStrikeThrough()
				&& prev.getEmbeddedPictures().size()==0;
	}

}

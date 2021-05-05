package org.msh.pdex.ipermit.controller;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Test;
import org.msh.pdex.dto.tables.TableCell;
import org.msh.pdex.dto.tables.TableHeader;
import org.msh.pdex.dto.tables.TableQtb;
import org.msh.pdex.dto.tables.TableRow;
import org.msh.pdex.exceptions.ObjectNotFoundException;

public class DocxViewPlainTest {

	
	@Test
	public void shouldParseDocx() throws IOException, ObjectNotFoundException {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("TEMPLMBEAN.PRODNAME", "Test product");
		model.put("templMBean.table", "The table");
		Path resourceDirectory = Paths.get("src","test","resources");
		String absolutePath = resourceDirectory.toFile().getAbsolutePath();
		byte[] docBytes = Files.readAllBytes(Paths.get(absolutePath + "/Below.docx"));
		InputStream inputDocument = new ByteArrayInputStream(docBytes);
		FileOutputStream out = new FileOutputStream(absolutePath+"/BelowResult.docx");
		DocxView view = new DocxView(inputDocument);
		view.resolveDocument(model);
		view.getDoc().write(out);
		view.getDoc().close();
		view.getDoc().close();
	}
	
	/**
	 * @throws IOException 
	 * @throws ObjectNotFoundException 
	 * 
	 */
	@Test
	public void shouldDoesTables() throws IOException, ObjectNotFoundException {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("TEMPLMBEAN.PRODNAME", "Test product");
		TableQtb table = new TableQtb();
		table.getHeaders().getHeaders().add(TableHeader.instanceOf(
				"first", 
				"#",
				true,
				true,
				true,
				TableHeader.COLUMN_STRING,
				5));
		table.getHeaders().getHeaders().add(TableHeader.instanceOf(
				"second", 
				"Medicine",
				true,
				true,
				true,
				TableHeader.COLUMN_STRING,
				0));
		table.getHeaders().getHeaders().add(TableHeader.instanceOf(
				"third", 
				"Quantity \n eeew weewe wewewewe",
				true,
				true,
				true,
				TableHeader.COLUMN_LONG,
				30));
		table.getRows().add(TableRow.instanceOf(0));
		table.getRows().get(0).getRow().add(TableCell.instanceOf("first", "1"));
		table.getRows().get(0).getRow().add(TableCell.instanceOf("second", "Medicine name and params of it"));
		table.getRows().get(0).getRow().add(TableCell.instanceOf("third", 300l, Locale.US));
		model.put("templMBean.table", table);
		Path resourceDirectory = Paths.get("src","test","resources");
		String absolutePath = resourceDirectory.toFile().getAbsolutePath();
		byte[] docBytes = Files.readAllBytes(Paths.get(absolutePath + "/Below.docx"));
		InputStream inputDocument = new ByteArrayInputStream(docBytes);
		FileOutputStream out = new FileOutputStream(absolutePath+"/BelowResult.docx");
		DocxView view = new DocxView(inputDocument);
		view.resolveDocument(model);
		view.getDoc().write(out);
		view.getDoc().close();
		view.getDoc().close();
	}

}

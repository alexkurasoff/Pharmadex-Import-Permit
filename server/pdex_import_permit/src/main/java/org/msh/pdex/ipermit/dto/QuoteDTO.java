package org.msh.pdex.ipermit.dto;

import org.msh.pdex.dto.form.AllowValidation;
import org.msh.pdex.dto.tables.TableQtb;

public class QuoteDTO extends  AllowValidation {
	private long id=0; //product ID
	private String productName="";
	private String manufacturer="";
	private TableQtb table = new TableQtb();
	private TableQtb tableExpand = new TableQtb();
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public String getManufacturer() {
		return manufacturer;
	}
	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}
	public TableQtb getTable() {
		return table;
	}
	public void setTable(TableQtb table) {
		this.table = table;
	}
	public TableQtb getTableExpand() {
		return tableExpand;
	}
	public void setTableExpand(TableQtb tableExpand) {
		this.tableExpand = tableExpand;
	}

}

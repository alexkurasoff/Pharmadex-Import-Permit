package org.msh.pdex.ipermit.dto;

import org.msh.pdex.dto.form.AllowValidation;
import org.msh.pdex.dto.tables.TableQtb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
/**
 * List of applications
 * @author alexk
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class ApplicationsDTO extends  AllowValidation{
	private TableQtb table = new TableQtb();
	private String applicantName="";
	private String tabName="";
	private ApplicationsButtonsDTO buttons=new ApplicationsButtonsDTO();
	public TableQtb getTable() {
		return table;
	}
	public void setTable(TableQtb table) {
		this.table = table;
	}
	public String getApplicantName() {
		return applicantName;
	}
	public void setApplicantName(String applicantName) {
		this.applicantName = applicantName;
	}
	public String getTabName() {
		return tabName;
	}
	public void setTabName(String tabName) {
		this.tabName = tabName;
	}
	public ApplicationsButtonsDTO getButtons() {
		return buttons;
	}
	public void setButtons(ApplicationsButtonsDTO buttons) {
		this.buttons = buttons;
	}

	
}

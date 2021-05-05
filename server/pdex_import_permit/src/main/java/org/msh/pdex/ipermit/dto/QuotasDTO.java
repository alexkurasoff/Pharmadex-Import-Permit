package org.msh.pdex.ipermit.dto;

import java.util.ArrayList;
import java.util.List;

import org.msh.pdex.dto.form.AllowValidation;
import org.msh.pdex.dto.tables.TableQtb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Product quotas for an applicant
 * @author alexk
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class QuotasDTO extends  AllowValidation{
	private TableQtb table = new TableQtb();
	private boolean selectedOnly=false;
	private String applicantName="";
	
	
	public TableQtb getTable() {
		return table;
	}
	public void setTable(TableQtb table) {
		this.table = table;
	}
	public boolean isSelectedOnly() {
		return selectedOnly;
	}
	
	public void setSelectedOnly(boolean selectedOnly) {
		this.selectedOnly = selectedOnly;
	}
	public String getApplicantName() {
		return applicantName;
	}
	public void setApplicantName(String applicantName) {
		this.applicantName = applicantName;
	}
	
	
	
}

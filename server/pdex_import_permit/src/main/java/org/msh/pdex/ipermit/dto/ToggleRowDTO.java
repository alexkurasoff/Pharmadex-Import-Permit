package org.msh.pdex.ipermit.dto;

import org.msh.pdex.dto.form.AllowValidation;
import org.msh.pdex.dto.tables.TableRow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO for toggle row operation
 * @author alexk
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class ToggleRowDTO extends  AllowValidation{
	/**
	 * To identify the main object the table belongs
	 * E.g. application id for table application details
	 */
	private long id=0L;
	/**
	 * To account selected right
	 */
	private int selectedCount;
	private TableRow row = TableRow.instanceOf(0);
	
	public TableRow getRow() {
		return row;
	}
	public void setRow(TableRow row) {
		this.row = row;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public int getSelectedCount() {
		return selectedCount;
	}
	public void setSelectedCount(int selectedCount) {
		this.selectedCount = selectedCount;
	}
	
}

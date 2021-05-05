package org.msh.pdex.ipermit.dto;

import org.msh.pdex.dto.form.AllowValidation;
import org.msh.pdex.dto.tables.TableQtb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Choose a moderator for an application
 * Moderator will be selected row in the table
 * @author alexk
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class ModeratorChooseDTO extends  AllowValidation{
	private long id=0L;	//application ID
	TableQtb table = new TableQtb();
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public TableQtb getTable() {
		return table;
	}
	public void setTable(TableQtb table) {
		this.table = table;
	}
	
	
}

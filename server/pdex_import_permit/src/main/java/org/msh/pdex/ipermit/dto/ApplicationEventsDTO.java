package org.msh.pdex.ipermit.dto;

import org.msh.pdex.dto.form.AllowValidation;
import org.msh.pdex.dto.tables.TableQtb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class ApplicationEventsDTO extends  AllowValidation {
	long id=0;
	TableQtb events = new TableQtb();
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public TableQtb getEvents() {
		return events;
	}
	public void setEvents(TableQtb events) {
		this.events = events;
	}
	
}

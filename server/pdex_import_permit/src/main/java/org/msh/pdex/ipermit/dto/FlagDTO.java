package org.msh.pdex.ipermit.dto;

import org.msh.pdex.dto.form.AllowValidation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
/***
 * Language switch, full data!
 * @author alexk
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class FlagDTO extends  AllowValidation{
	private boolean selected=false;
	private String localeStr="en_US";
	private String displayName="English";
	public boolean isSelected() {
		return selected;
	}
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public String getLocaleStr() {
		return localeStr;
	}
	public void setLocaleStr(String localeStr) {
		this.localeStr = localeStr;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
}

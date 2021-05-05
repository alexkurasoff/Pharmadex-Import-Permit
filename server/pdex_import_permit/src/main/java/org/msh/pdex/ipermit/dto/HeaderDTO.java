package org.msh.pdex.ipermit.dto;

import java.util.ArrayList;
import java.util.List;

import org.msh.pdex.dto.WorkspaceDTO;
import org.msh.pdex.dto.form.AllowValidation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Header related data
 * @author alexk
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class HeaderDTO extends  AllowValidation {
	private WorkspaceDTO workspace = new WorkspaceDTO();
	private List<FlagDTO> flags = new ArrayList<FlagDTO>();

	public WorkspaceDTO getWorkspace() {
		return workspace;
	}

	public void setWorkspace(WorkspaceDTO workspace) {
		this.workspace = workspace;
	}

	public List<FlagDTO> getFlags() {
		return flags;
	}

	public void setFlags(List<FlagDTO> flags) {
		this.flags = flags;
	}
	
	
}

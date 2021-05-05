package org.msh.pdex.ipermit.dto;

import org.springframework.core.io.Resource;

/**
 * Byte resource with filename
 * @author Alex Kurasoff
 *
 */
public class FileResource {
	private Resource resource;
	private String fileName="";
	private String contentType="";
	
	public Resource getResource() {
		return resource;
	}
	public void setResource(Resource resource) {
		this.resource = resource;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	
}

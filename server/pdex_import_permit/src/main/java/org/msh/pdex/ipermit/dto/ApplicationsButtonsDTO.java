package org.msh.pdex.ipermit.dto;

import org.msh.pdex.dto.form.AllowValidation;

/**
 * Buttons to initialize an application are complex, therefore extracted to separate DTO
 * @author alexk
 *
 */
public class ApplicationsButtonsDTO extends AllowValidation{
	private int appType=0;			//0 -all, 1 -normal, 2-special, 3-contest, 4 - donation
	private int productType=0;	//0-all, 1 - medicines, 2 - medical products
	private long newApplicationId=0l;	//ID of a new created application
	private boolean visible = false;
	
	public int getAppType() {
		return appType;
	}
	public void setAppType(int appType) {
		this.appType = appType;
	}
	public int getProductType() {
		return productType;
	}
	public void setProductType(int productType) {
		this.productType = productType;
	}
	public long getNewApplicationId() {
		return newApplicationId;
	}
	public void setNewApplicationId(long newApplicationId) {
		this.newApplicationId = newApplicationId;
	}
	public boolean isVisible() {
		return visible;
	}
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
}

package org.msh.pdex.ipermit.dto;

import java.math.BigDecimal;

import org.msh.pdex.dto.form.AllowValidation;
import org.msh.pdex.dto.form.FormFieldDTO;
import org.msh.pdex.dto.tables.TableQtb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
/**
 * Represents Application Details. e.g. medicines, medical products, etc
 * @author alexk
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class ApplicationDetailsDTO extends AllowValidation{
	long applicationId=0;
	TableQtb details = new TableQtb();						//long table with selection and links
	TableQtb detailsShort = new TableQtb();				//short table to play with product edit form
	String detailsSuggest="";										//the suggest message for the details table
	private FormFieldDTO<BigDecimal> totalAmount=new FormFieldDTO<BigDecimal>(BigDecimal.ZERO);
	boolean showDetails=true;									//show details, otherwise show all products
	String orderType = "Normal";								//type of the application
	String itemType = "medicines";								//type of the  detail items
	public long getApplicationId() {
		return applicationId;
	}
	public void setApplicationId(long applicationId) {
		this.applicationId = applicationId;
	}
	public TableQtb getDetails() {
		return details;
	}
	public void setDetails(TableQtb details) {
		this.details = details;
	}
	public TableQtb getDetailsShort() {
		return detailsShort;
	}
	public void setDetailsShort(TableQtb detailsShort) {
		this.detailsShort = detailsShort;
	}
	public String getDetailsSuggest() {
		return detailsSuggest;
	}
	public void setDetailsSuggest(String detailsSuggest) {
		this.detailsSuggest = detailsSuggest;
	}
	public FormFieldDTO<BigDecimal> getTotalAmount() {
		return totalAmount;
	}
	public void setTotalAmount(FormFieldDTO<BigDecimal> totalAmount) {
		this.totalAmount = totalAmount;
	}
	public boolean isShowDetails() {
		return showDetails;
	}
	public void setShowDetails(boolean showDetails) {
		this.showDetails = showDetails;
	}
	public String getOrderType() {
		return orderType;
	}
	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}
	public String getItemType() {
		return itemType;
	}
	public void setItemType(String itemType) {
		this.itemType = itemType;
	}
	
	
}

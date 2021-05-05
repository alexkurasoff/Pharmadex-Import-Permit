package org.msh.pdex.ipermit.dto;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.msh.pdex.dto.NavigatorDTO;
import org.msh.pdex.dto.QuestionDTO;
import org.msh.pdex.dto.Validator;
import org.msh.pdex.dto.form.AllowValidation;
import org.msh.pdex.dto.form.FormFieldDTO;
import org.msh.pdex.dto.form.OptionDTO;
import org.msh.pdex.services.Magic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Import permit application
 * This DTO contains some depricated fields that should be removed in a future
 * @author alexk
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class ApplicationDTO extends  AllowValidation  {
	
	private long id=0;
	private LocalDateTime lastSaved=LocalDateTime.now();
	private String alertMessage="";
	private boolean showCancel=false;															//application cancel is possible
	private ApplicationDetailsDTO details=new ApplicationDetailsDTO();		//details tables etc
	private NavigatorDTO navigator = new NavigatorDTO();							//where the current user can open - tabset, tab, component	
	
	@Validator(above=3,below=254)
	@Magic(name="orderType")
	private FormFieldDTO<OptionDTO> orderType =  new FormFieldDTO<OptionDTO>(new OptionDTO());
	@Validator(above=3,below=254)
	@Magic(name="itemType")
	private FormFieldDTO<OptionDTO> itemType =  new FormFieldDTO<OptionDTO>(new OptionDTO());
	@Validator(above=3,below=254)
	@Magic(name="applicant")
	private FormFieldDTO<OptionDTO> applicant =  new FormFieldDTO<OptionDTO>(new OptionDTO());
	@Validator(above=3,below=254)
	@Magic(name="pipStatus")
	private FormFieldDTO<OptionDTO> pipStatus = new FormFieldDTO<OptionDTO>(new OptionDTO());
	@Validator(above=3,below=254)
	@Magic(name="port")
	private FormFieldDTO<OptionDTO> port = new FormFieldDTO<OptionDTO>(new OptionDTO());
	@Validator(above=3,below=254)
	@Magic(name="custom")
	private FormFieldDTO<OptionDTO>custom = new FormFieldDTO<OptionDTO>(new OptionDTO());
	@Validator(above=3,below=3)
	@Magic(name="incoterms")
	private FormFieldDTO<OptionDTO> incoterms = new FormFieldDTO<OptionDTO>(new OptionDTO());
	@Validator(above=3,below=25)
	@Magic(name="transport")
	private FormFieldDTO<OptionDTO> transport = new FormFieldDTO<OptionDTO>(new OptionDTO());
	@Validator(above=3,below=3)
	@Magic(name="currency")
	private FormFieldDTO<OptionDTO> currency = new FormFieldDTO<OptionDTO>(new OptionDTO());
	@Validator(above=3,below=254)
	@Magic(name="pipNumber")
	private FormFieldDTO<String> pipNumber=new FormFieldDTO<String>("");
	@Validator(above=3,below=254)
	@Magic(name="proformaNumber")
	private FormFieldDTO<String> proformaNumber=new FormFieldDTO<String>("");
	@Validator(above=5,below=100)
	@Magic(name="requested_date")
	private FormFieldDTO<LocalDate> requested_date=new FormFieldDTO<LocalDate>(null); //when the permit should be received
	@Validator(above=5,below=365)
	@Magic(name="expiry_date")
	private FormFieldDTO<LocalDate> expiry_date=new FormFieldDTO<LocalDate>(null);      //the last day of the validity of the permit
	@Validator(above=10,below=10)
	@Magic(name="auth_date")
	private FormFieldDTO<LocalDate> auth_date=new FormFieldDTO<LocalDate>(null);      //Date when Pharmacetical has been completed  
	@Validator(above=10,below=10)
	@Magic(name="approvalDate")
	private FormFieldDTO<LocalDate> approvalDate=new FormFieldDTO<LocalDate>(null);      //Date when an application has been approved
	@Validator(above=10,below=10)
	@Magic(name="validation_date")
	private FormFieldDTO<LocalDate> validation_date=new FormFieldDTO<LocalDate>(null);      //date when documents have been marked by validation stamps
	@Validator(above=3,below=254)
	@Magic(name="approver")
	private FormFieldDTO<OptionDTO> approver = new FormFieldDTO<OptionDTO>(new OptionDTO());	
	@Validator(above=3,below=254)
	@Magic(name="inspector")
	private FormFieldDTO<OptionDTO> inspector = new FormFieldDTO<OptionDTO>(new OptionDTO());	//Pharmaceutical inspector
	@Magic(name="remark")
	@Validator(above=15,below=1000)
	private FormFieldDTO<String> remark=new FormFieldDTO<String>("");

	/**
	 * draft checklist
	 */
	private List<QuestionDTO> questions= new ArrayList<QuestionDTO>();
	private String typeTmplCheckList = "";
	/**field "prevStatusId" from ApplicationEventsDTO by show CheckList by click item  */
	private long prevStatusId = 0;
	
	public long getId() {
		return id;
	}


	public void setId(long id) {
		this.id = id;
	}

	public String getAlertMessage() {
		return alertMessage;
	}


	public void setAlertMessage(String alertMessage) {
		this.alertMessage = alertMessage;
	}


	public LocalDateTime getLastSaved() {
		return lastSaved;
	}


	public void setLastSaved(LocalDateTime lastSaved) {
		this.lastSaved = lastSaved;
	}
	
	public FormFieldDTO<OptionDTO> getApplicant() {
		return applicant;
	}


	public FormFieldDTO<OptionDTO> getOrderType() {
		return orderType;
	}


	public void setOrderType(FormFieldDTO<OptionDTO> orderType) {
		this.orderType = orderType;
	}

	public FormFieldDTO<OptionDTO> getItemType() {
		return itemType;
	}
	public void setItemType(FormFieldDTO<OptionDTO> itemType) {
		this.itemType = itemType;
	}
	public void setApplicant(FormFieldDTO<OptionDTO> applicant) {
		this.applicant = applicant;
	}
	public FormFieldDTO<OptionDTO> getPipStatus() {
		return pipStatus;
	}
	public void setPipStatus(FormFieldDTO<OptionDTO> pipStatus) {
		this.pipStatus = pipStatus;
	}
	public FormFieldDTO<OptionDTO> getPort() {
		return port;
	}


	public void setPort(FormFieldDTO<OptionDTO> port) {
		this.port = port;
	}


	public FormFieldDTO<OptionDTO> getCustom() {
		return custom;
	}


	public void setCustom(FormFieldDTO<OptionDTO> custom) {
		this.custom = custom;
	}


	public FormFieldDTO<OptionDTO> getIncoterms() {
		return incoterms;
	}


	public void setIncoterms(FormFieldDTO<OptionDTO> incoterms) {
		this.incoterms = incoterms;
	}


	public FormFieldDTO<OptionDTO> getTransport() {
		return transport;
	}


	public void setTransport(FormFieldDTO<OptionDTO> transport) {
		this.transport = transport;
	}

	public FormFieldDTO<OptionDTO> getCurrency() {
		return currency;
	}


	public void setCurrency(FormFieldDTO<OptionDTO> currency) {
		this.currency = currency;
	}


	public FormFieldDTO<String> getPipNumber() {
		return pipNumber;
	}


	public void setPipNumber(FormFieldDTO<String> pipNumber) {
		this.pipNumber = pipNumber;
	}


	public FormFieldDTO<String> getProformaNumber() {
		return proformaNumber;
	}


	public void setProformaNumber(FormFieldDTO<String> proformaNumber) {
		this.proformaNumber = proformaNumber;
	}


	public FormFieldDTO<LocalDate> getRequested_date() {
		return requested_date;
	}


	public void setRequested_date(FormFieldDTO<LocalDate> requested_date) {
		this.requested_date = requested_date;
	}

	public FormFieldDTO<LocalDate> getExpiry_date() {
		return expiry_date;
	}


	public FormFieldDTO<LocalDate> getApprovalDate() {
		return approvalDate;
	}


	public void setApprovalDate(FormFieldDTO<LocalDate> approvalDate) {
		this.approvalDate = approvalDate;
	}


	public FormFieldDTO<OptionDTO> getApprover() {
		return approver;
	}


	public void setApprover(FormFieldDTO<OptionDTO> approver) {
		this.approver = approver;
	}


	public void setExpiry_date(FormFieldDTO<LocalDate> expiry_date) {
		this.expiry_date = expiry_date;
	}
	
	public FormFieldDTO<LocalDate> getAuth_date() {
		return auth_date;
	}


	public void setAuth_date(FormFieldDTO<LocalDate> auth_date) {
		this.auth_date = auth_date;
	}


	public FormFieldDTO<LocalDate> getValidation_date() {
		return validation_date;
	}


	public void setValidation_date(FormFieldDTO<LocalDate> validation_date) {
		this.validation_date = validation_date;
	}


	public FormFieldDTO<String> getRemark() {
		return remark;
	}


	public void setRemark(FormFieldDTO<String> remark) {
		this.remark = remark;
	}

	public List<QuestionDTO> getQuestions() {
		return questions;
	}


	public void setQuestions(List<QuestionDTO> questions) {
		this.questions = questions;
	}

	public boolean isShowCancel() {
		return showCancel;
	}

	public void setShowCancel(boolean showCancel) {
		this.showCancel = showCancel;
	}


	public ApplicationDetailsDTO getDetails() {
		return details;
	}


	public void setDetails(ApplicationDetailsDTO details) {
		this.details = details;
	}	

	public FormFieldDTO<OptionDTO> getInspector() {
		return inspector;
	}


	public void setInspector(FormFieldDTO<OptionDTO> inspector) {
		this.inspector = inspector;
	}


	public String getTypeTmplCheckList() {
		return typeTmplCheckList;
	}


	public void setTypeTmplCheckList(String typeTmplCheckList) {
		this.typeTmplCheckList = typeTmplCheckList;
	}


	public NavigatorDTO getNavigator() {
		return navigator;
	}


	public void setNavigator(NavigatorDTO navigator) {
		this.navigator = navigator;
	}


	public long getPrevStatusId() {
		return prevStatusId;
	}


	public void setPrevStatusId(long prevStatusId) {
		this.prevStatusId = prevStatusId;
	}

}

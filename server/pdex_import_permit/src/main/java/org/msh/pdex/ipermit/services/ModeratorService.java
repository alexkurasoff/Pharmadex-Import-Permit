package org.msh.pdex.ipermit.services;

import java.io.IOException;
import java.util.List;

import org.msh.pdex.dto.DocTypesDTO;
import org.msh.pdex.dto.NavigatorDTO;
import org.msh.pdex.dto.tables.Headers;
import org.msh.pdex.dto.tables.TableHeader;
import org.msh.pdex.dto.tables.TableQtb;
import org.msh.pdex.dto.tables.TableRow;
import org.msh.pdex.exceptions.ObjectNotFoundException;
import org.msh.pdex.i18N.Messages;
import org.msh.pdex.ipermit.dto.ApplicationDTO;
import org.msh.pdex.ipermit.dto.ApplicationsDTO;
import org.msh.pdex.model.User;
import org.msh.pdex.model.pip.Import_permit;
import org.msh.pdex.model.rsecond.Context;
import org.msh.pdex.model.rsecond.DocType;
import org.msh.pdex.repository.JdbcRepository;
import org.msh.pdex.repository.UserRepository;
import org.msh.pdex.repository.pip.Import_permitRepo;
import org.msh.pdex.repository.rsecond.DocTypeRepo;
import org.msh.pdex.services.ContextServices;
import org.msh.pdex.services.DictionaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * Business logic for the moderator
 * @author alexk
 *
 */
@Service
public class ModeratorService {
	private static final String APPLICATIONS_HEADERS = "APPLICATIONS_HEADERS";
	private static final Logger logger = LoggerFactory.getLogger(ModeratorService.class);
	@Autowired
	ContextServices contextServices;
	@Autowired
	Messages messages;
	@Autowired
	JdbcRepository jdbcRepo;
	@Autowired
	BoilerplateServices boilerServ;
	@Autowired
	Import_permitRepo applRepo;
	@Autowired
	UserRepository userRepo;
	@Autowired
	CommonService commonServ;
	@Autowired
	ValidationService validator;
	@Autowired
	EntityToDtoService entityToDTOServ;
	@Autowired
	Import_permitRepo modelRepo;
	@Autowired
	DtoToEntityService dtoToEntityServ;
	@Autowired
	DictionaryService dictServ;
	@Autowired
	DocTypeRepo docTypeRepo;

	/**
	 * List of all submitted applications
	 * @param userLogin
	 * @param appsDTO
	 * @param context
	 * @return
	 * @throws ObjectNotFoundException 
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	public ApplicationsDTO applications(String userLogin, ApplicationsDTO appsDTO, Context context) 
			throws ObjectNotFoundException, JsonParseException, JsonMappingException, IOException {
		appsDTO.setApplicantName("");
		String select=boilerServ.loadSelect("applications");
		CreateHeaders createHeaders=this::applicationsHeaders;
		String contextKey = APPLICATIONS_HEADERS+appsDTO.getTabName().toLowerCase();
		Headers headers = boilerServ.loadHeaders(context,contextKey, appsDTO.getTable(),createHeaders);
		appsDTO.getTable().setHeaders(headers);
		String where=selectApplicationsCriteria(appsDTO);
		List<TableRow> rows= jdbcRepo.qtbGroupReport(select, 
				"",
				where, 
				headers);
		TableQtb.tablePage(rows, appsDTO.getTable());
		appsDTO.setTable(boilerServ.translateRows(appsDTO.getTable()));
		appsDTO.getTable().setSelectable(false);
		contextServices.saveHeaders(context, headers,  contextKey);
		return appsDTO;
	}
	/**
	 * Phrase "where" depends on tab 
	 * @param appsDTO
	 * @return
	 */
	private String selectApplicationsCriteria(ApplicationsDTO appsDTO) {
		switch(appsDTO.getTabName().toLowerCase()) {
		case "validation":
			return "statCode in ('VALD')";
		case "invoicing":
			return "statCode in ('FACT')";
		case "finalize":
			return "statCode in ('SGNT')";
		case "inprocess":
			return "statCode not in ('ARCH', 'DRFT')";
		case "archive":
			return "statCode in ('ARCH')";
		default:
			return "(statCode not in ('DRFT'))";  //all submitted
		}
	}

	/**
	 * Create the headers
	 * @return
	 */
	private Headers applicationsHeaders() {
		Headers ret = ApplicantService.ApplicationHeadersCommon();
		ret.getHeaders().add(TableHeader.instanceOf(
				"appName",
				"applicant_name",
				true,
				true,
				true,
				TableHeader.COLUMN_STRING,
				15));
		ret.getHeaders().add(TableHeader.instanceOf(
				"address",
				"address",
				false,
				true,
				true,
				TableHeader.COLUMN_STRING,
				15));
		return ret;
	}

	/**
	 * Save only marking data
	 * @param userLogin
	 * @param data
	 * @param context
	 * @return
	 * @throws ObjectNotFoundException 
	 * @throws IOException 
	 */
	public ApplicationDTO markupSave(String userLogin, String userRole,  ApplicationDTO data, Context context) throws ObjectNotFoundException, IOException {
		Import_permit model = boilerServ.loadApplication(data.getId());
		model = dtoToEntityServ.modelFromMarking(model, data);
		model = modelRepo.save(model);
		data= entityToDTOServ.ApplicationToDto(context, model, userLogin, userRole, data);
		data=validator.validateDTO(data, true, false);
		return data;
	}
	
	/**
	 * Save only finalize data
	 * @param userLogin
	 * @param data
	 * @param context
	 * @return
	 * @throws ObjectNotFoundException 
	 * @throws IOException 
	 */
	public ApplicationDTO finalizeSave(String userLogin, String userRole,  ApplicationDTO data, Context context) throws ObjectNotFoundException, IOException {
		Import_permit model = boilerServ.loadApplication(data.getId());
		model = dtoToEntityServ.modelFromFinalize(model, data);
		model = modelRepo.save(model);
		data= entityToDTOServ.ApplicationToDto(context, model, userLogin, userRole, data);
		data=validator.validateDTO(data, true, false);
		return data;
	}

	/**
	 * Submit an application to invoicing
	 * @param userLogin
	 * @param data
	 * @param context
	 * @return
	 * @throws ObjectNotFoundException 
	 * @throws IOException 
	 */
	public ApplicationDTO submitInvoicing(String userLogin, String userRole, ApplicationDTO data, Context context) throws ObjectNotFoundException, IOException {
		Import_permit model = boilerServ.loadApplication(data.getId());
		data = entityToDTOServ.ApplicationToDto(context, model, userLogin, userRole, data);
		data = validator.validateDTO(data, true, true);
		if(data.isValid()) {
			data=validator.validatePIPChecklist(model, data);
			if(data.isValid()) {
				data=commonServ.doneJob(context, userLogin,userRole,"FACT_submit" , "FACT", data);
			}
		}
		return data;
	}
	/**
	 * Open an applicatuion, validate the marking
	 * @param userLogin
	 * @param data
	 * @param context
	 * @return
	 * @throws ObjectNotFoundException 
	 * @throws IOException 
	 */
	public ApplicationDTO markingOpen(String userLogin, String userRole, ApplicationDTO data, Context context) throws ObjectNotFoundException, IOException {
		Import_permit model = boilerServ.loadApplication(data.getId());
		data = entityToDTOServ.ApplicationToDto(context, model, userLogin, userRole, data);
		data=validator.validateDTO(data, true, false);
		return data;
	}

	/**
	 * Submit an application to approval
	 * @param userLogin
	 * @param data
	 * @param context
	 * @return
	 * @throws ObjectNotFoundException 
	 * @throws IOException 
	 */
	public ApplicationDTO submitApproval(String userLogin, String userRole, ApplicationDTO data, Context context) throws ObjectNotFoundException, IOException {
		Import_permit model = boilerServ.loadApplication(data.getId());
		data = entityToDTOServ.ApplicationToDto(context, model, userLogin, userRole, data);
		data = validator.validateDTO(data, true, true);
		if(data.isValid()) {
			data=validator.validatePIPChecklist(model, data);
			if(data.isValid()) {
				data=commonServ.doneJob(context, userLogin, userRole,"pendingapproval" , "SGNT", data);
			}
		}
		return data;
	}
	/**
	 * Submit an application to rejection
	 * @param userLogin
	 * @param data
	 * @param context
	 * @return
	 * @throws ObjectNotFoundException 
	 * @throws IOException 
	 */
	public ApplicationDTO submitRejection(String userLogin, String userRole, ApplicationDTO data, Context context) throws ObjectNotFoundException, IOException {
		Import_permit model = boilerServ.loadApplication(data.getId());
		data = entityToDTOServ.ApplicationToDto(context, model, userLogin, userRole, data);
		data=validator.validatePIPChecklist(model, data);
		if(data.isValid()) {
			data=commonServ.doneJob(context, userLogin, userRole, "submitforreject" , "SGNT", data);
		}
		return data;
	}
	
	/**
	 * Send approved application to Secretary
	 * @param userLogin
	 * @param data
	 * @param context
	 * @return
	 * @throws ObjectNotFoundException 
	 * @throws IOException 
	 */
	public ApplicationDTO applicationApproval(String userLogin, String userRole, ApplicationDTO data, Context context) throws ObjectNotFoundException, IOException {
		Import_permit model = boilerServ.loadApplication(data.getId());
		data.setValid(true);
		data=entityToDTOServ.ApplicationToDto(context, model, userLogin, userRole, data);
		data=validator.validateDTO(data, true, true);
		if(data.isValid()) {
			data=validator.validatePIPChecklist(model, data);
			if(data.isValid()) {
				data=commonServ.doneJob(context, userLogin, userRole, "approval","ACCT" , data);
				
			}
		}
		return data;
	}
	/**
	 * Send returned application to Secretary
	 * @param userLogin
	 * @param data
	 * @param context
	 * @return
	 * @throws ObjectNotFoundException 
	 * @throws IOException 
	 */
	public ApplicationDTO applicationReturnl(String userLogin, String userRole, ApplicationDTO data, Context context) throws ObjectNotFoundException, IOException {
		Import_permit model = boilerServ.loadApplication(data.getId());
		data.setValid(true);
		data=entityToDTOServ.ApplicationToDto(context, model, userLogin, userRole, data);
		data=commonServ.doneJob(context, userLogin, userRole, "return","RJCT" , data);
		return data;
	}


	public DocTypesDTO documents(String userLogin, DocTypesDTO dto, Context context) throws ObjectNotFoundException, JsonParseException, JsonMappingException, IOException {
		String select = boilerServ.loadSelect("documentsType");
		if(dto.getFilterBtns().getSelectKey() == 2) {// active
			select += " and opt.Active = 1";
		}else if(dto.getFilterBtns().getSelectKey() == 3) {// not active
			select += " and opt.Active = 0";
		}
		CreateHeaders createHeaders = this::documentsHeaders;
		Headers headers = boilerServ.loadHeaders(context, "", dto.getTable(), createHeaders);
		dto.getTable().setHeaders(headers);
		List<TableRow> rows= jdbcRepo.qtbGroupReport(select, 
				"",
				"Discriminator like 'DocType'", headers);
		TableQtb.tablePage(rows, dto.getTable());
		dto.setTable(boilerServ.translateRows(dto.getTable()));
		dto.getTable().setSelectable(false);
		
		dto.getFilterBtns().getKeys().clear();
		dto.getFilterBtns().getNames().clear();
		
		dto.getFilterBtns().getKeys().add(1);
		dto.getFilterBtns().getNames().add(messages.get("all"));
		
		dto.getFilterBtns().getKeys().add(2);
		dto.getFilterBtns().getNames().add(messages.get("active"));
		
		dto.getFilterBtns().getKeys().add(3);
		dto.getFilterBtns().getNames().add(messages.get("not_active"));
		
		return dto;
	}
	
	private Headers documentsHeaders() {
		Headers ret = new Headers();
		ret.getHeaders().add(TableHeader.instanceOf(
				"Code",
				"sra_code",
				true,
				true,
				true,
				TableHeader.COLUMN_LINK,
				0));
		ret.getHeaders().add(TableHeader.instanceOf(
				"Description",
				"global_description",
				true,
				true,
				true,
				TableHeader.COLUMN_I18,
				0));
		ret.getHeaders().add(TableHeader.instanceOf(
				"appl",
				"applicant",
				true,
				true,
				true,
				TableHeader.COLUMN_BOOLEAN_CHECKBOX,
				0));
		ret.getHeaders().add(TableHeader.instanceOf(
				"proc",
				"procces",
				true,
				true,
				true,
				TableHeader.COLUMN_BOOLEAN_CHECKBOX,
				0));
		ret.getHeaders().add(TableHeader.instanceOf(
				"act",
				"active",
				true,
				true,
				true,
				TableHeader.COLUMN_BOOLEAN_CHECKBOX,
				0));
		
		return ret;
	}
	
	@Transactional
	public NavigatorDTO documentOpen(String fetchUserLogin, Context context, NavigatorDTO navigator) throws ObjectNotFoundException {
		navigator.setTab("administration");
		navigator.setComponent("document");
		navigator.setParams(navigator.getId() + "");

		return navigator;
	}
	
	@Transactional
	public DocTypesDTO documentOpenEdit(User currentUser, Context context, DocTypesDTO docDTO) throws ObjectNotFoundException, JsonParseException, JsonMappingException, IOException {
		DocType dtype = null;
		if(docDTO.getId() > 0)
			dtype = dictServ.loadDocTypeById(docDTO.getId());
		docDTO = entityToDTOServ.docTypeToDTO(dtype);
		return docDTO;
	}
	
	@Transactional
	public DocTypesDTO documentSave(String fetchUserLogin, Context context, DocTypesDTO docDTO) throws ObjectNotFoundException, JsonParseException, JsonMappingException, IOException {
		docDTO = validator.validateDTO(docDTO, true, true);
		if(docDTO.isValid()) {
			DocType doctype = new DocType();
			
			// set code to UpperCase
			String c = docDTO.getSra_code().getValue().toUpperCase();
			docDTO.getSra_code().setValue(c);
			
			// unique code DocType
			if(boilerServ.isUniqueDocTypeCode(docDTO.getSra_code().getValue())) {
				doctype = dtoToEntityServ.docTypeFromDTO(doctype, docDTO);
				
				doctype.setActive(entityToDTOServ.getYesNoValue(docDTO.getActive().getValue()));
				doctype.setAttachToApplicant(entityToDTOServ.getYesNoValue(docDTO.getApplicant().getValue()));
				doctype.setAttachToPIP(entityToDTOServ.getYesNoValue(docDTO.getProcess().getValue()));
				
				// create field Description
				String desc = "DocType_" + docDTO.getSra_code().getValue();
				doctype.setDescription(desc);
				
				boilerServ.updateResourceMessage(desc, docDTO.getName_us().getValue(), docDTO.getName_port().getValue());

				doctype = docTypeRepo.save(doctype);
				
				doctype = dictServ.loadDocTypeById(doctype.getId());
				docDTO = entityToDTOServ.docTypeToDTO(doctype);
				
				// reload all Messages from DB
				messages.getMessages().clear();
				messages.reload(messages.getWorkspace().getDefaultLocale());
			}else {
				docDTO.setValid(false);
				docDTO.setValidError(messages.get("error_dublicateCode"));
			}
		}
		return docDTO;
	}
	
	@Transactional
	public DocTypesDTO documentUpdate(String fetchUserLogin, Context context, DocTypesDTO docDTO) throws ObjectNotFoundException, JsonParseException, JsonMappingException, IOException {
		docDTO = validator.validateDTO(docDTO, true, true);
		if(docDTO.isValid()) {
			DocType doctype = dictServ.loadDocTypeById(docDTO.getId());
			
			doctype.setActive(entityToDTOServ.getYesNoValue(docDTO.getActive().getValue()));
			doctype.setAttachToApplicant(entityToDTOServ.getYesNoValue(docDTO.getApplicant().getValue()));
			doctype.setAttachToPIP(entityToDTOServ.getYesNoValue(docDTO.getProcess().getValue()));
			
			doctype = dtoToEntityServ.docTypeFromDTO(doctype, docDTO);
			boilerServ.updateResourceMessage(doctype.getDescription(), docDTO.getName_us().getValue(), docDTO.getName_port().getValue());
			
			doctype = docTypeRepo.save(doctype);
			
			doctype = dictServ.loadDocTypeById(doctype.getId());
			docDTO = entityToDTOServ.docTypeToDTO(doctype);
			
			// reload all Messages from DB
			messages.getMessages().clear();
			messages.reload(messages.getWorkspace().getDefaultLocale());
		}
		return docDTO;
	}
	
	@Transactional
	public ApplicationDTO applicationOpen(String userLogin, String userRole, Context context, ApplicationDTO appDTO) throws ObjectNotFoundException, IOException {
		Import_permit model = boilerServ.loadApplication(appDTO.getId());
		appDTO=entityToDTOServ.ApplicationToDto(context, model, userLogin,userRole,appDTO);
		/*if(model.getPipStatus().getCode().equalsIgnoreCase("VALD")) {
			long maxId=0;
			String statString = "";
			for(PIPTrackDone track: model.getJobsDone()) {
				if(track.getId()>maxId) {
					maxId=track.getId();
					statString = track.getJobCode();
				}
			}
			if(statString.length()>0) {
				appDTO.setAlertMessage(messages.get(statString));
			}
		}
		appDTO.setValid(true);*/
		return appDTO;
	}
}

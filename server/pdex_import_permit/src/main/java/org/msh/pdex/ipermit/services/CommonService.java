package org.msh.pdex.ipermit.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.msh.pdex.dto.ApplicantDTO;
import org.msh.pdex.dto.AttachmentDTO;
import org.msh.pdex.dto.AttachmentsDTO;
import org.msh.pdex.dto.ManufacturerDTO;
import org.msh.pdex.dto.NavigatorDTO;
import org.msh.pdex.dto.form.FormFieldDTO;
import org.msh.pdex.dto.form.OptionDTO;
import org.msh.pdex.dto.tables.Headers;
import org.msh.pdex.dto.tables.TableHeader;
import org.msh.pdex.dto.tables.TableQtb;
import org.msh.pdex.dto.tables.TableRow;
import org.msh.pdex.exceptions.ObjectNotFoundException;
import org.msh.pdex.i18N.Messages;
import org.msh.pdex.ipermit.dto.ApplicationDTO;
import org.msh.pdex.ipermit.dto.ApplicationEventsDTO;
import org.msh.pdex.ipermit.dto.FileResource;
import org.msh.pdex.ipermit.dto.ProductDTO;
import org.msh.pdex.model.Applicant;
import org.msh.pdex.model.Company;
import org.msh.pdex.model.Excipient;
import org.msh.pdex.model.Inn;
import org.msh.pdex.model.Product;
import org.msh.pdex.model.User;
import org.msh.pdex.model.pip.Import_permit;
import org.msh.pdex.model.pip.PIPDoc;
import org.msh.pdex.model.pip.PIPTrackDone;
import org.msh.pdex.model.pip.PipStatus;
import org.msh.pdex.model.rsecond.ApplicantDoc;
import org.msh.pdex.model.rsecond.Context;
import org.msh.pdex.model.rsecond.DocType;
import org.msh.pdex.model.rsecond.Document;
import org.msh.pdex.repository.ApplicantRepository;
import org.msh.pdex.repository.CompanyRepo;
import org.msh.pdex.repository.DosageUnitRepo;
import org.msh.pdex.repository.ExcipientRepository;
import org.msh.pdex.repository.InnRepository;
import org.msh.pdex.repository.JdbcRepository;
import org.msh.pdex.repository.ProductRepo;
import org.msh.pdex.repository.pip.Import_permitRepo;
import org.msh.pdex.repository.pip.PipDocRepo;
import org.msh.pdex.repository.pip.PipStatusRepo;
import org.msh.pdex.repository.rsecond.ApplicantDocRepo;
import org.msh.pdex.repository.rsecond.DocTypeRepo;
import org.msh.pdex.repository.rsecond.DocumentRepo;
import org.msh.pdex.services.DictionaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * Support common API, i.e. API available to all authorized users
 * @author alexk
 *
 */
@Service
public class CommonService {
	private static final Logger logger = LoggerFactory.getLogger(CommonService.class);
	@Autowired
	BoilerplateServices boilerServ;
	@Autowired
	JdbcRepository jdbcRepo;
	@Autowired
	Import_permitRepo appRepo;
	@Autowired
	CheckListService checkListService;
	@Autowired
	PipStatusRepo statusRepo;
	@Autowired
	DictionaryService dict;
	@Autowired
	Messages messages;
	@Autowired
	Import_permitRepo permitRepo;
	@Autowired
	EntityToDtoService entityToDTOServ;
	@Autowired
	ValidationService validator;
	@Autowired
	DocumentRepo docRepo;
	@Autowired
	DocTypeRepo docTypeRepo;
	@Autowired
	PipDocRepo pipDocRepo;
	@Autowired
	ApplicantDocRepo applicantDocRepo;
	@Autowired
	ApplicantRepository applicantRepo;
	@Autowired
	ProductRepo prodRepo;
	@Autowired
	DosageUnitRepo dosageUnitRepo;
	@Autowired
	InnRepository innRepository;
	@Autowired
	ExcipientRepository excipientRepository;
	@Autowired
	DtoToEntityService dtoToEntityServ;
	@Autowired
	ProductRepo productRepo;
	@Autowired
	CompanyRepo companyRepo;

	/**
	 * Get all possible suppliers from:
	 * <ul>
	 * <li> applicants
	 * <li> companies
	 * <li> options with discriminators "Supplier", "Consignee", "Payer"
	 * </ul>
	 * @param fetchUserLogin
	 * @param context
	 * @param option
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	public OptionDTO suppliers(OptionDTO option) throws ObjectNotFoundException {
		String select=boilerServ.loadSelect("suppliers");
		return possiblePlayers(option, select);
	}

	public OptionDTO loadInns(OptionDTO option) throws ObjectNotFoundException {
		if(option != null && option.getCode() != null) {
			List<Inn> list = innRepository.findByNameStartingWithOrderByName(option.getCode());

			option.getOptions().clear();
			if(list != null) {
				for(Inn inn:list) {
					OptionDTO dto = entityToDTOServ.innToOptionDTO(inn);
					if(dto.getId() > 0)
						option.getOptions().add(dto);
				}
			}
		}

		return option;
	}
	
	public OptionDTO loadManufacturers(OptionDTO option) throws ObjectNotFoundException {
		if(option != null && option.getCode() != null) {
			Iterable<Company> list = companyRepo.findByCompanyNameStartingWithOrderByCompanyName(option.getCode());

			option.getOptions().clear();
			option.getOptions().add(entityToDTOServ.createOtherOptionDTO());
			if(list != null) {
				for(Company c:list) {
					OptionDTO dto = entityToDTOServ.companyToOptionDTO(c);
					if(dto.getId() > 0)
						option.getOptions().add(dto);
				}
			}
		}

		return option;
	}
	
	public ManufacturerDTO loadCompany(ManufacturerDTO dto) throws ObjectNotFoundException {
		long id = dto.getManuf_name().getValue().getId();
		if(id > 0) {
			Optional<Company> opt = companyRepo.findById(id);
			if(opt.isPresent()) {
				Company c = opt.get();
				dto.setApplicant_country(entityToDTOServ.countryToDTO(c.getAddress()));
			}
		}
		
		return dto;
	}
	
	public OptionDTO loadExcs(OptionDTO option) throws ObjectNotFoundException {
		if(option != null && option.getCode() != null) {
			List<Excipient> list = excipientRepository.findByNameStartingWithOrderByName(option.getCode());

			option.getOptions().clear();
			if(list != null) {
				for(Excipient exc:list) {
					OptionDTO dto = entityToDTOServ.excToOptionDTO(exc);
					if(dto.getId() > 0)
						option.getOptions().add(dto);
				}
			}
		}

		return option;
	}


	/**
	 * Select all possible Suppliers, Payers, Consignee (all together are players) 
	 * @param option
	 * @param select
	 */
	private OptionDTO possiblePlayers(OptionDTO option, String select) {
		Headers headers = new Headers();
		headers.getHeaders().add(TableHeader.instanceOf("Name", TableHeader.COLUMN_STRING ));
		headers.getHeaders().add(TableHeader.instanceOf("Description", TableHeader.COLUMN_STRING ));
		headers.getHeaders().add(TableHeader.instanceOf("OptionID", TableHeader.COLUMN_LONG ));
		headers.getHeaders().add(TableHeader.instanceOf("ApplicantID", TableHeader.COLUMN_LONG ));
		headers.getHeaders().add(TableHeader.instanceOf("CompanyID", TableHeader.COLUMN_LONG ));
		String where = "p.Name like '"+option.getCode() +"%'";
		headers.getHeaders().get(0).setSort(true);
		headers.getHeaders().get(0).setSortValue(TableHeader.SORT_ASC);
		List<TableRow> rows = jdbcRepo.qtbGroupReport(select, "",where, headers);
		List<OptionDTO> newOptions = new ArrayList<OptionDTO>();
		for(TableRow row :rows) {
			OptionDTO opt = OptionDTO.of(row);
			newOptions.add(opt);
		}
		option.getOptions().clear();
		option.getOptions().addAll(newOptions);
		return option;
	}
	/**
	 * get all possible payers
	 * @param option
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	public OptionDTO payers(OptionDTO option) throws ObjectNotFoundException {
		String select=boilerServ.loadSelect("payers");
		return possiblePlayers(option, select);
	}
	/**
	 * Get all possible consignees
	 * @param option
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	public OptionDTO consignees(OptionDTO option) throws ObjectNotFoundException {
		String select=boilerServ.loadSelect("consignees");
		return possiblePlayers(option, select);
	}
	/**
	 * Open an application
	 * @param userLogin
	 * @param context
	 * @param navigator
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	@Transactional
	public NavigatorDTO applicationOpen(String userRole, Context context, NavigatorDTO navigator) throws ObjectNotFoundException {
		Optional<Import_permit> appo = appRepo.findById(navigator.getId());
		if(appo.isPresent()) {
			PipStatus status = appo.get().getPipStatus();
			if(status != null) {
				//an applicant
				if(userRole.equalsIgnoreCase( "applicant")) {
					if(status.getCode().equalsIgnoreCase("DRFT")) {
						navigator.setTab("applications");
						navigator.setComponent("applicationForm");
						navigator.setParams(navigator.getId()+"");
						return navigator;
					}
					if(status.getCode().equalsIgnoreCase("FACT")) {
						navigator.setTab("invoiced");
						navigator.setComponent("ApplicantInvoiced");
						navigator.setParams(navigator.getId()+"");
						return navigator;
					}
					//default
					navigator.setTab("applications");
					navigator.setComponent("applicationTrack");
					navigator.setParams(navigator.getId()+"");
					return navigator;
				}
				//DNF secretary
				if(userRole.equalsIgnoreCase("secretary")) {
					if(status.getCode().equalsIgnoreCase("DRFT")) {
						navigator.setTab("notifications");
						navigator.setComponent("ApplicationTrack");
						navigator.setParams(navigator.getId()+"");
						return navigator;
					}
					if(status.getCode().equalsIgnoreCase("RQST")) {
						navigator.setTab("applications");
						navigator.setComponent("SecretaryApplicationReceipt");
						navigator.setParams(navigator.getId()+"");
						return navigator;
					}
					if(status.getCode().equalsIgnoreCase("RJCT") || status.getCode().equalsIgnoreCase("ACCT") || status.getCode().equalsIgnoreCase("CNCL") ) {
						navigator.setComponent("ArchiveApplication");
						navigator.setParams(navigator.getId()+"");
						return navigator;
					}
					//default
					navigator.setTab("applications");
					navigator.setComponent("applicationTrack");
					navigator.setParams(navigator.getId()+"");
					return navigator;
				}
				if(userRole.equalsIgnoreCase("review")) {
					if(status.getCode().equalsIgnoreCase("VRFY")) {
						navigator.setTab("applications");
						navigator.setComponent("ReviewApplication");
						navigator.setParams(navigator.getId()+"");
						return navigator;
					}
					navigator.setTab("applications");
					navigator.setComponent("applicationTrack");
					navigator.setParams(navigator.getId()+"");
					return navigator;
				}
				if(userRole.equalsIgnoreCase( "moderator")) {
					if(status.getCode().equalsIgnoreCase("VALD")) {
						navigator.setTab("validation");
						navigator.setComponent("ValidateApplication");
						navigator.setParams(navigator.getId()+"");
						return navigator;
					}
					if(status.getCode().equalsIgnoreCase("FACT")) {
						navigator.setComponent("InvoicingApplication");
						navigator.setParams(navigator.getId()+"");
						return navigator;
					}
					if(status.getCode().equalsIgnoreCase("SGNT")) {
						navigator.setComponent("FinalizeApplication");
						navigator.setParams(navigator.getId()+"");
						return navigator;
					}
					navigator.setComponent("ApplicationTrack");
					navigator.setParams(navigator.getId()+"");
					return navigator;
				}
				navigator.setTab("applications");
				navigator.setComponent("applicationTrack");
				navigator.setParams(navigator.getId()+"");
				return navigator;
			}else {
				return navigator;
			}
		}else {
			return navigator;
		}
	}

	/**
	 * Indicate that some job is done.
	 * The verification of the job assumed as passed before
	 * @param context 
	 * @param userLogin
	 * @param jobCode
	 * @param newStateName 
	 * @param string 
	 * @param appDTO
	 * @return
	 * @throws ObjectNotFoundException 
	 * @throws IOException 
	 */
	@Transactional
	public ApplicationDTO doneJob(Context context, String userLogin, String userRole, String jobCode, String newStateName, ApplicationDTO appDTO) throws ObjectNotFoundException, IOException {
		Import_permit model = boilerServ.loadApplication(appDTO.getId());
		Optional<PipStatus> statuso = statusRepo.findByCode(newStateName);
		if(statuso.isPresent()) {
			PIPTrackDone jobDone = new PIPTrackDone();
			jobDone.setCompleted(new Date());
			jobDone.setPrevStatus(model.getPipStatus());
			jobDone.setNewStatus(statuso.get());
			jobDone.setJobCode(jobCode);
			jobDone.setUserExecutor(boilerServ.userByUserLogin(userLogin));
			jobDone.setOrder(0);
			model.getJobsDone().add(jobDone);
			model.setPipStatus(statuso.get());
			model.setPipNumber(statuso.get().getCode()+"/"+model.getId());
			model = appRepo.save(model);
			appDTO=entityToDTOServ.ApplicationToDto(context, model, userLogin, userRole, appDTO);
		}
		return appDTO;
	}


	/**
	 * Create an application events table
	 * @param userLogin
	 * @param context
	 * @param eventsDTO
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	public ApplicationEventsDTO applicationEvents(String userLogin, Context context,
			ApplicationEventsDTO eventsDTO) throws ObjectNotFoundException {
		if(eventsDTO.getEvents().getHeaders().getHeaders().size()==0) {
			eventsDTO.getEvents().setHeaders(eventsHeaders());
		}
		Import_permit model = boilerServ.loadApplication(eventsDTO.getId());
		if(entityToDTOServ.checkUserAccess(model, userLogin)) {
			String select = boilerServ.loadSelect("trackapplication");
			List<TableRow> rows = jdbcRepo.qtbGroupReport(select, "", "tr.doneIpId="+eventsDTO.getId(), eventsDTO.getEvents().getHeaders());
			for(TableRow row : rows) {
				String keyValue = row.getRow().get(1).getValue();
				row.getRow().get(1).setValue(messages.get(keyValue));
				row.getRow().get(1).setOriginalValue(messages.get(keyValue));
			}
			TableQtb.tablePage(rows, eventsDTO.getEvents());
			boilerServ.translateHeaders(eventsDTO.getEvents().getHeaders());
			eventsDTO.getEvents().setSelectable(false);
		}
		return eventsDTO;
	}
	/**
	 * Headers for application events table
	 * @return
	 */
	private Headers eventsHeaders() {
		Headers ret = new Headers();
		ret.getHeaders().add(TableHeader.instanceOf(
				"tr.Completed",
				"completedat",
				false,
				true,
				false,
				TableHeader.COLUMN_LOCALDATE,
				0));
		ret.getHeaders().add(TableHeader.instanceOf(
				"tr.JobCode",
				"label_actions",
				false,
				true,
				false,
				TableHeader.COLUMN_LINK,
				0));
		ret.getHeaders().add(TableHeader.instanceOf(
				"u.name",
				"completedby",
				false,
				true,
				false,
				TableHeader.COLUMN_STRING,
				0));
		ret.getHeaders().get(0).setSort(true);
		ret.getHeaders().get(0).setSortValue(TableHeader.SORT_ASC);
		return ret;
	}
	/**
	 * Load or create selected attachment
	 * @param userLogin
	 * @param context
	 * @param attDTO
	 * @return
	 * @throws ObjectNotFoundException 
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	@Transactional
	public AttachmentsDTO applicationAttachments(String userLogin, Context context, AttachmentsDTO attDTO) throws ObjectNotFoundException, JsonParseException, JsonMappingException, IOException {
		Optional<Import_permit> modelo= permitRepo.findById(attDTO.getAppId());
		if(modelo.isPresent()) {
			attDTO.setApplicantId(modelo.get().getApplicant().getApplcntId());
			Import_permit model = modelo.get();
			if(entityToDTOServ.checkUserAccess(model, userLogin)) {
				String select=boilerServ.loadSelect("documents");
				CreateHeaders createHeaders=this::attachmentHeaders;
				if(!attDTO.isEditor() && attDTO.getTable().getHeaders().getHeaders().size()==1) {
					attDTO.getTable().getHeaders().getHeaders().clear();
				}
				Headers headers = boilerServ.loadHeaders(context,"ATTACHMENTS_HEADERS", attDTO.getTable(),createHeaders);
				attDTO.getTable().setHeaders(headers);
				select = select.replace("ip.id = doc.import_permitId", " ip.id = doc.import_permitId and ip.id="+attDTO.getAppId());
				select = select.replace("appl.applcntId = doc.applicantapplcntId", "appl.applcntId = doc.applicantapplcntId and appl.applcntId = "+model.getApplicant().getApplcntId());
				List<TableRow> rows= jdbcRepo.qtbGroupReport(select, 
						"",
						"", headers);
				TableQtb.tablePage(rows, attDTO.getTable());
				attDTO.setTable(boilerServ.translateRows(attDTO.getTable()));
				if(attDTO.isEditor()) {
					//short up the table and load selected if we can find it
					attDTO.setTable(boilerServ.subTable(attDTO.getTable(), 0));
				}
				attDTO.getTable().setSelectable(false);
			}
			return attDTO;
		}else {
			throw new ObjectNotFoundException("Application not found. Id is "+attDTO.getAppId());
		}
	}

	private Headers attachmentHeaders() {
		Headers ret = new Headers();
		ret.getHeaders().add(TableHeader.instanceOf(
				"SignNo",
				"SignNo",
				true,
				true,
				true,
				TableHeader.COLUMN_LINK,
				0));
		ret.getHeaders().add(TableHeader.instanceOf(
				"SignDate",
				"SignDate",
				true,
				true,
				true,
				TableHeader.COLUMN_LOCALDATE,
				0));
		ret.getHeaders().add(TableHeader.instanceOf(
				"author.name",
				"attachedby",
				true,
				true,
				true,
				TableHeader.COLUMN_STRING,
				0));
		ret.getHeaders().add(TableHeader.instanceOf(
				"TypeKey",
				"docType",
				true,
				true,
				true,
				TableHeader.COLUMN_I18,
				0));
		return ret;
	}
	/**
	 * Open or initialize an attachment
	 * @param userLogin
	 * @param context
	 * @param attDTO
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	public AttachmentDTO attachmentOpen(String userLogin, Context context, AttachmentDTO att) throws ObjectNotFoundException {
		if(att.getId()==0) {
			//new one
			att.setDocType(new FormFieldDTO<OptionDTO>(OptionDTO.of(new DocType(), dict.allActiveDocTypes(),messages)));
		}else {
			att = entityToDTOServ.documentToDto(att);
		}
		att.setDocType(boilerServ.translateOption(att.getDocType()));
		att.setDisplayOnly(!isAttachemntEditable(userLogin, att));
		return att;
	}

	/**
	 * Set of rules to determine possibility to edit an attachment
	 * @param userLogin
	 * @param att
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	@Transactional
	private boolean isAttachemntEditable(String userLogin, AttachmentDTO att) throws ObjectNotFoundException {
		if(att.getId()==0) {
			return true;
		}
		User user = boilerServ.userByUserLogin(userLogin);
		Document doc = boilerServ.document(att.getId());
		Import_permit application=null;
		if(doc instanceof PIPDoc) {
			application=((PIPDoc)doc).getImport_permit();
		}
		if(application==null) {
			return  doc.getAuthor()!=null && (doc.getAuthor().getUserId()==user.getUserId());	//author only
		}
		//may edit only created by himself
		if(application!=null && application.getPipStatus().getCode().equalsIgnoreCase("DRFT")) {
			return doc.getAuthor()!=null && (doc.getAuthor().getUserId()==user.getUserId());
		}else {
			//restrict applicant to edit them after submit
			return doc.getAuthor()!=null
					&& (doc.getAuthor().getUserId()==user.getUserId()
					&& user.getApplicant()==null
							);
		}
	}

	/**
	 * Strict verify upload data
	 * @param userLogin
	 * @param context
	 * @param att
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	public AttachmentDTO uploadVerify(String userLogin, Context context, AttachmentDTO att) throws ObjectNotFoundException {
		if(isAttachemntEditable(userLogin, att)) {
			att= validator.validateDTO(att, true, true);
			if(att.getId()==0) {
				att.getFileName().setError(false);
				att.getFileName().setStrict(false);
				att.getFileName().setSuggest("");
				if(att.getFileName().getValue().length()==0) {
					att.getFileName().setError(true);
					att.getFileName().setStrict(true);
					att.getFileName().setSuggest(messages.get("newdocument"));
					att.setValid(false);
				}
			}
			return att;
		}else {
			throw new ObjectNotFoundException("User "+ userLogin +" cannot save document id="+att.getId(),logger);
		}
	}
	/**
	 * Save a file as document
	 * @param userName 
	 * @param att
	 * @param fileBytes
	 * @throws ObjectNotFoundException 
	 */
	@Transactional
	public AttachmentDTO saveFile(String userLogin, AttachmentDTO att, byte[] fileBytes) throws ObjectNotFoundException {
		User user = boilerServ.userByUserLogin(userLogin);
		Optional<DocType> docTypeo = docTypeRepo.findById(att.getDocType().getValue().getId());
		if(docTypeo.isPresent()) {
			Document doc = null;
			if(att.getId()>0) {
				doc = boilerServ.document(att.getId());
			}
			if(docTypeo.get().getAttachToApplicant()) {
				att= saveApplicantDoc(att, user, fileBytes, doc);
			}
			if(docTypeo.get().getAttachToPIP()) {
				att= savePipDoc(att,user, fileBytes, doc);
			}
			att=entityToDTOServ.documentToDto(att);
			return att;
		}else {
			throw new ObjectNotFoundException("Document type not found. ID is "+att.getDocType().getValue().getId());
		}

	}

	/**
	 * Save an attachment to PIP
	 * @param att
	 * @param user 
	 * @param fileBytes
	 * @param user2 
	 * @param fileName
	 * @param fileSize
	 * @param fileContentType
	 * @param docType 
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	private AttachmentDTO savePipDoc(AttachmentDTO att, User user, byte[] fileBytes, Document doc) throws ObjectNotFoundException {
		PIPDoc pdoc =new PIPDoc();
		if(doc != null) {
			if(doc instanceof PIPDoc) {
				pdoc = (PIPDoc) doc;
			}else { //doc class should be changed
				fileBytes = doc.getAttachment();
				docRepo.delete(doc);
			}
		}
		Import_permit application = boilerServ.loadApplication(att.getPipId());
		pdoc.setImport_permit(application);
		pdoc=(PIPDoc) dtoToEntityServ.commonDocFields(att,user,fileBytes,pdoc);
		pdoc = pipDocRepo.save(pdoc);
		att.setId(pdoc.getId());
		return att;
	}

	/**
	 * 
	 * @param att 
	 * @param user
	 * @param fileBytes
	 * @param doc maybe null for new documents
	 * @return
	 * @throws ObjectNotFoundException
	 */
	private AttachmentDTO saveApplicantDoc(AttachmentDTO att, User user, byte[] fileBytes,Document doc) throws ObjectNotFoundException {
		ApplicantDoc adoc =new ApplicantDoc();
		if(doc != null) {
			if(doc instanceof ApplicantDoc) {
				adoc = (ApplicantDoc) doc;
			}
			if(doc instanceof PIPDoc) {
				att.setApplicantId(((PIPDoc) doc).getImport_permit().getApplicant().getApplcntId());
				fileBytes = doc.getAttachment();
				docRepo.delete(doc);
			}
		}
		Applicant applicant = boilerServ.applicant(att.getApplicantId());
		adoc.setApplicant(applicant);
		adoc=(ApplicantDoc) dtoToEntityServ.commonDocFields(att,user,fileBytes,adoc);
		doc = applicantDocRepo.save(adoc);
		att.setId(doc.getId());
		return att;
	}
	/**
	 * Download a document from Document table
	 * @param docId
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	@Transactional
	public FileResource downloadFile(Long docId) throws ObjectNotFoundException {
		Optional<Document> doco = docRepo.findById(docId);
		if(doco.isPresent()) {
			Resource res = new ByteArrayResource(doco.get().getAttachment());
			FileResource ret = new FileResource();
			ret.setResource(res);
			ret.setFileName(doco.get().getFileName());
			ret.setContentType(doco.get().getContentEncoding());
			if(ret.getContentType()==null || ret.getContentType().length()==0) {
				ret.setContentType("application/octet-stream");
			}
			return ret;
		}else {
			String mess = "Document (file) not found. Id is "+docId;
			logger.error(mess);
			throw new ObjectNotFoundException(mess);
		}
	}

	/**
	 * delete an attachment
	 * Only applicant can remove own attachments
	 * @param userLogin
	 * @param att
	 * @param context
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	@Transactional
	public AttachmentDTO attachmentDelete(String userLogin, AttachmentDTO att, Context context) throws ObjectNotFoundException {
		if(isAttachemntEditable(userLogin, att)) {
			Optional<DocType> docTypeo = docTypeRepo.findById(att.getDocType().getValue().getId());
			User user = boilerServ.userByUserLogin(userLogin);
			if(user.getApplicant() != null) {
				if(docTypeo.isPresent()) {
					if(docTypeo.get().getAttachToApplicant()) {
						return deleteApplicantDoc(att, user);
					}
					if(docTypeo.get().getAttachToPIP()) {
						return deletePipDoc(att, user);
					}
					return att;
				}else {
					throw new ObjectNotFoundException("Document type not found. ID is "+user.getUserId(),logger);
				}
			}else {
				throw new ObjectNotFoundException("Applicant is null  for the user. User ID is "+user.getUserId(),logger);
			}
		}else {
			throw new ObjectNotFoundException("User "+ userLogin +" cannot save document id="+att.getId(),logger);
		}
	}

	/**
	 * Delete application's document, but only by authorized user
	 * @param att
	 * @param user
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	private AttachmentDTO deletePipDoc(AttachmentDTO att, User user) throws ObjectNotFoundException {
		Optional<PIPDoc> doco = pipDocRepo.findById(att.getId());
		AttachmentDTO ret=new AttachmentDTO();
		if(doco.isPresent()) {
			if(doco.get().getImport_permit().getApplicant().getApplcntId()==user.getApplicant().getApplcntId()) {
				pipDocRepo.delete(doco.get());
				ret.setPipId(att.getPipId());
			}else {
				throw new ObjectNotFoundException("Attachment is not allowed to delete by the user. User Id is "+user.getUserId(),logger);
			}
			return ret;
		}else {
			throw new ObjectNotFoundException("Application's attachment not found. Id is "+att.getId(),logger);
		}
	}

	/**
	 * Delete applicants document, but only by authorized user?
	 * @param att
	 * @param user
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	private AttachmentDTO deleteApplicantDoc(AttachmentDTO att, User user) throws ObjectNotFoundException {
		Optional<ApplicantDoc> doco = applicantDocRepo.findById(att.getId());
		AttachmentDTO ret=new AttachmentDTO();
		if(doco.isPresent()) {
			if(doco.get().getApplicant().getApplcntId()==user.getApplicant().getApplcntId()) {
				applicantDocRepo.delete(doco.get());
				ret.setApplicantId(att.getApplicantId());
			}else {
				throw new ObjectNotFoundException("Attachment is not allowed to delete by the user. User Id is "+user.getUserId(),logger);
			}
			return ret;
		}else {
			throw new ObjectNotFoundException("Applicant's attachment not found. Id is "+att.getId(),logger);
		}
	}
	/**
	 * Load product data to the DTO
	 * @param fetchUserLogin
	 * @param context
	 * @param data
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	@Transactional
	public ProductDTO productLoad(String fetchUserLogin, Context context, ProductDTO data) throws ObjectNotFoundException {
		Optional<Product> modelo = prodRepo.findById(data.getId());
		if(modelo.isPresent()) {
			data = entityToDTOServ.productToDto(modelo.get(), data);
			return data;
		}else {
			throw new ObjectNotFoundException("Product not found. Id is "+data.getId(),logger);
		}
	}
	/**
	 * Load applicant data
	 * @param context
	 * @param fetchGroup
	 * @param fetchUserLogin
	 * @param data
	 * @return
	 * @throws ObjectNotFoundException 
	 */
	@Transactional
	public ApplicantDTO applicant(Context context, String fetchGroup, String fetchUserLogin, ApplicantDTO data) throws ObjectNotFoundException {
		Import_permit model = boilerServ.loadApplication(data.getAppId());
		data = entityToDTOServ.applicantDTOFromModel(model, data);
		return data;
	}
	/**
	 * Cancel an application
	 * @param context
	 * @param fetchGroup
	 * @param fetchUserLogin
	 * @param data
	 * @return
	 * @throws ObjectNotFoundException 
	 * @throws IOException 
	 */
	@Transactional
	public ApplicationDTO applicationCancel(Context context, String userRole, String userLogin,
			ApplicationDTO data) throws ObjectNotFoundException, IOException {
		Import_permit model = boilerServ.loadApplication(data.getId());
		if(entityToDTOServ.applicationCancelCondition(model, userLogin,userRole)){
			data = doneJob(context, userLogin, userRole, "cancelapplication", "CNCL", data);
		}
		return data;
	}

	@Transactional
	public ProductDTO productSave(String userLogin, String userRole, ProductDTO prodDTO, Context context) throws ObjectNotFoundException, IOException {
		prodDTO = save(prodDTO, false, context, userLogin, userRole);
		prodDTO = validator.validateDTO(prodDTO, true, false);
		// validateProdInns
		if(prodDTO.isValid()) {
			prodDTO = validateProdInns(prodDTO);
			prodDTO = validateManufs(prodDTO);
		}
		return prodDTO;
	}
	
	private ProductDTO validateProdInns(ProductDTO prodDTO) throws ObjectNotFoundException {
		/*List<ProdInnDTO> inns = prodDTO.getInns();
		for(ProdInnDTO dto:inns) {
			dto = validator.validateDTO(dto, true, false);
		}*/
		
		String suggest = "";
		if(prodDTO.getInns() == null || prodDTO.getInns().size() == 0) {
			suggest = messages.get("selectprodinns");
		}
		
		prodDTO.setValidError(suggest);
		prodDTO.setValid(prodDTO.isValid() && suggest.length() == 0);
		return prodDTO;
	}
	
	private ProductDTO validateManufs(ProductDTO prodDTO) throws ObjectNotFoundException {
		String suggest = "";
		if(prodDTO.getManufacturers() == null || prodDTO.getManufacturers().size() == 0) {
			suggest = messages.get("no_manufacturer");
		}
		
		prodDTO.setValidError(suggest);
		prodDTO.setValid(prodDTO.isValid() && suggest.length() == 0);
		return prodDTO;
	}
	/**
	 * Save product to the database
	 * @param prodDTO
	 * @param strict - do not save in case of validation errors, otherwise save anyway
	 * @return
	 * @throws ObjectNotFoundException 
	 * @throws IOException 
	 */
	private ProductDTO save(ProductDTO prodDTO, boolean strict, Context context, String userLogin, String userRole) throws IOException, ObjectNotFoundException {
		if(strict) {
			if(prodDTO.isValid()) {
				prodDTO.setLastSaved(null);
				return prodDTO;
			}
		}
		Product product = new Product();
		long id = prodDTO.getId();
		Optional<Product> optional = prodRepo.findById(id);
		if(optional.isPresent()) {
			product = optional.get();
		} 
		product = dtoToEntityServ.productFromDTO(product, prodDTO);
		product = productRepo.save(product);
		
		product = prodRepo.findById(product.getId()).get();
		//prodDTO.setId(product.getId());
		prodDTO = entityToDTOServ.productToDto(product, prodDTO);

		return prodDTO;
	}
	/**
	 * Load an application and represent it as DTO
	 * @param context 
	 * @param fetchUserLogin
	 * @param fetchGroup
	 * @param data
	 * @return
	 * @throws ObjectNotFoundException 
	 * @throws IOException 
	 */
	public ApplicationDTO applicationTrack(Context context, String userLogin, String userRole, ApplicationDTO data) throws ObjectNotFoundException, IOException {
		Import_permit model = boilerServ.loadApplication(data.getId());
		data = entityToDTOServ.ApplicationToDto(context, model, userLogin, userRole, data);
		return data;
	}
}
package org.msh.pdex.ipermit.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.msh.pdex.dto.ApplicantDTO;
import org.msh.pdex.dto.AttachmentDTO;
import org.msh.pdex.dto.AttachmentsDTO;
import org.msh.pdex.dto.ManufacturerDTO;
import org.msh.pdex.dto.NavigatorDTO;
import org.msh.pdex.dto.ProdExcipientDTO;
import org.msh.pdex.dto.ProdInnDTO;
import org.msh.pdex.dto.QuestionDTO;
import org.msh.pdex.dto.form.OptionDTO;
import org.msh.pdex.exceptions.ObjectNotFoundException;
import org.msh.pdex.i18N.Messages;
import org.msh.pdex.ipermit.WebSecurity;
import org.msh.pdex.ipermit.dto.ApplicationDTO;
import org.msh.pdex.ipermit.dto.ApplicationDetailDTO;
import org.msh.pdex.ipermit.dto.ApplicationEventsDTO;
import org.msh.pdex.ipermit.dto.ApplicationsDTO;
import org.msh.pdex.ipermit.dto.FileResource;
import org.msh.pdex.ipermit.dto.ProductDTO;
import org.msh.pdex.ipermit.dto.UserDetailsDTO;
import org.msh.pdex.ipermit.dto.UserRoleDto;
import org.msh.pdex.ipermit.exceptions.DataNotFoundException;
import org.msh.pdex.ipermit.services.ApplicantService;
import org.msh.pdex.ipermit.services.BoilerplateServices;
import org.msh.pdex.ipermit.services.CheckListService;
import org.msh.pdex.ipermit.services.CommonService;
import org.msh.pdex.ipermit.services.DocumentService;
import org.msh.pdex.ipermit.services.EntityToDtoService;
import org.msh.pdex.ipermit.services.ModeratorService;
import org.msh.pdex.ipermit.services.NotificationService;
import org.msh.pdex.ipermit.services.ResponsibleService;
import org.msh.pdex.ipermit.services.SecretaryService;
import org.msh.pdex.ipermit.services.UserService;
import org.msh.pdex.model.enums.TemplateType;
import org.msh.pdex.model.rsecond.Context;
import org.msh.pdex.repository.UserRepository;
import org.msh.pdex.services.ContextServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Common api for any authorized user
 * @author alexk
 *
 */
@RestController
public class CommonAPI {

	private static final Logger logger = LoggerFactory.getLogger(ApplicantAPI.class);

	@Autowired
	private ContextServices contextServices;

	@Autowired
	private CommonService commonService;

	@Autowired
	private CheckListService checkListService;

	@Autowired
	ApplicantService applicantService;

	@Autowired
	SecretaryService secretaryService;

	@Autowired
	ResponsibleService responsibleService;

	@Autowired
	ModeratorService moderatorService;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private Messages messages;

	@Autowired
	private ModeratorService moderServ;

	@Autowired
	NotificationService notifServ;
	@Autowired
	BoilerplateServices boilerServ;

	@Autowired
	UserRepository userRepo;

	@Autowired
	UserService userService;

	@Autowired
	EntityToDtoService entityToDTOServ;

	@Autowired
	private DocumentService documentService;

	/**
	 * the list of applictins for an applicant
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param appDTO
	 * @return
	 */
	@RequestMapping(value= { "/api/common/applications"}, method = RequestMethod.POST)
	public ApplicationsDTO applications(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody ApplicationsDTO appsDTO) {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		userService.updateCurrentRole(auth, context);
		try {
			appsDTO = loadApplications(auth, appsDTO, context);

		} catch (ObjectNotFoundException | IOException e) {
			new DataNotFoundException(e);
			logger.error(e.getMessage());
		}
		return appsDTO;
	}
	/**
	 * Load a list of applications depends on current user role
	 * @param auth
	 * @param appsDTO
	 * @param context
	 * @return
	 * @throws ObjectNotFoundException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public ApplicationsDTO loadApplications(Authentication auth, ApplicationsDTO appsDTO, Context context)
			throws ObjectNotFoundException, JsonParseException, JsonMappingException, IOException {
		if(WebSecurity.fetchGroup(auth).equalsIgnoreCase("applicant")) {
			appsDTO = applicantService.applications(WebSecurity.fetchUserLogin(auth), appsDTO, context);
			appsDTO.getButtons().setVisible(true);
		}
		if(WebSecurity.fetchGroup(auth).equalsIgnoreCase("secretary")) {
			appsDTO = secretaryService.applications(WebSecurity.fetchUserLogin(auth), appsDTO, context);
			appsDTO.getButtons().setVisible(false);
		}
		if(WebSecurity.fetchGroup(auth).equalsIgnoreCase("review")) {
			appsDTO = responsibleService.applications(WebSecurity.fetchUserLogin(auth), appsDTO, context);
			appsDTO.getButtons().setVisible(false);
		}
		if(WebSecurity.fetchGroup(auth).equalsIgnoreCase("moderator")) {
			appsDTO = moderatorService.applications(WebSecurity.fetchUserLogin(auth), appsDTO, context);
			appsDTO.getButtons().setVisible(false);
		}
		return appsDTO;
	}

	/**
	 * the list of applictins for an applicant - Excel version
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param appDTO
	 * @return
	 */
	@RequestMapping(value= { "/api/common/applications/excel"}, method = RequestMethod.POST)
	public ModelAndView applicationsExcel(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody ApplicationsDTO appsDTO) {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		userService.updateCurrentRole(auth, context);

		Map<String, Object> model = new HashMap<String, Object>();
		try {
			appsDTO = loadApplications(auth, appsDTO, context);
			appsDTO.getTable().getHeaders().setPageSize(Integer.MAX_VALUE);
			//Sheet Name
			String name = messages.get("applications");
			model.put(ExcelView.SHEETNAME, name);
			//Title
			model.put(ExcelView.TITLE, messages.get("applications"));
			//Headers List
			model.put(ExcelView.HEADERS, appsDTO.getTable().getHeaders().getHeaders());
			//Rows
			model.put(ExcelView.ROWS, appsDTO.getTable().getRows());
			response.setHeader( HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"applications.xlsx\"");
			response.setHeader("filename", "applications.xlsx");       
		} catch (ObjectNotFoundException | IOException e) {
			new DataNotFoundException(e);
			logger.error(e.getMessage());
		}
		return new ModelAndView(new ExcelView(), model);
	}

	/**
	 * Get list of all possible suppliers with the names starts from string in data.code
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param data
	 * @return result will be in data.options. For each of them - option.code - name, option.description - table name,
	 * id is record's id in this table
	 * @throws DataNotFoundException
	 */
	@RequestMapping(value= { "/api/common/suppliers"}, method = RequestMethod.POST)
	public OptionDTO suppliers(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody OptionDTO option) throws DataNotFoundException {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		try {
			option= commonService.suppliers(option);
		} catch (ObjectNotFoundException e) {
			logger.error(e.getMessage());
			throw new DataNotFoundException(e);
		}
		return option;
	}

	/**
	 * Get list of all possible payers with the names starts from string in data.code
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param data
	 * @return result will be in data.options. For each of them - option.code - name, option.description - table name,
	 * id is record's id in this table
	 * @throws DataNotFoundException
	 */
	@RequestMapping(value= { "/api/common/payers"}, method = RequestMethod.POST)
	public OptionDTO payers(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody OptionDTO option) throws DataNotFoundException {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		try {
			option= commonService.payers(option);
		} catch (ObjectNotFoundException e) {
			logger.error(e.getMessage());
			throw new DataNotFoundException(e);
		}
		return option;
	}

	/**
	 * Get list of all possible payers with the names starts from string in data.code
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param data
	 * @return result will be in data.options. For each of them - option.code - name, option.description - table name,
	 * id is record's id in this table
	 * @throws DataNotFoundException
	 */
	@RequestMapping(value= { "/api/common/consignees"}, method = RequestMethod.POST)
	public OptionDTO consignees(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody OptionDTO option) throws DataNotFoundException {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		try {
			option= commonService.consignees(option);
		} catch (ObjectNotFoundException e) {
			logger.error(e.getMessage());
			throw new DataNotFoundException(e);
		}
		return option;
	}

	/**
	 * Save an answer to a checklist question
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param data
	 * @return result will be in data.options. For each of them - option.code - name, option.description - table name,
	 * id is record's id in this table
	 * @throws DataNotFoundException
	 */
	@RequestMapping(value= { "/api/common/answer/save"}, method = RequestMethod.POST)
	public QuestionDTO answerSave(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody QuestionDTO question) throws DataNotFoundException {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		try {
			question= checkListService.answerPIPSave(WebSecurity.fetchUserLogin(auth),context,question);
		} catch (ObjectNotFoundException e) {
			logger.error(e.getMessage());
			throw new DataNotFoundException(e);
		}
		return question;
	}

	/**
	 * Navigate to ther right taset/tab/component to open an application in a form
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param navigator
	 * @return
	 * @throws DataNotFoundException
	 */
	@RequestMapping(value= { "/api/common/application/open"}, method = RequestMethod.POST)
	public NavigatorDTO applicationOpen(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody NavigatorDTO navigator) throws DataNotFoundException {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		try {
			navigator= commonService.applicationOpen(WebSecurity.fetchGroup(auth),context,navigator);
		} catch (ObjectNotFoundException e) {
			logger.error(e.getMessage());
			throw new DataNotFoundException(e);
		}
		return navigator;
	}

	/**
	 * Load or create new detail record, i.e. Import_permit_product
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param appDTO
	 * @return
	 */
	@RequestMapping(value= { "/api/common/application/detail"}, method = RequestMethod.POST)
	public ApplicationDetailDTO applicationDetail(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody ApplicationDetailDTO data) {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		try {
			data = applicantService.applicationDetail(WebSecurity.fetchUserLogin(auth), WebSecurity.fetchGroup(auth), data, context);
		} catch (ObjectNotFoundException e) {
			new DataNotFoundException(e);
			logger.error(e.getMessage());
		}
		return data;
	}

	/**
	 * Open a checklist in accordance with the current application state
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param appDTO
	 * @return
	 * @throws DataNotFoundException
	 */
	@RequestMapping(value= { "/api/common/checklist/open"}, method = RequestMethod.POST)
	public ApplicationDTO checkListOpen(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody ApplicationDTO appDTO) throws DataNotFoundException {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		try {
			appDTO = checkListService.questionsForPIP(appDTO,boilerServ.userByUserLogin(WebSecurity.fetchUserLogin(auth)));
		} catch (ObjectNotFoundException e) {
			logger.error(e.getMessage());
			throw new DataNotFoundException(e);
		}
		appDTO.setValid(true);
		appDTO.setAlertMessage("");
		return appDTO;
	}

	/**
	 * Open a checklist in accordance with the current application state
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param appDTO
	 * @return
	 * @throws DataNotFoundException
	 */
	@RequestMapping(value= { "/api/common/verification/result"}, method = RequestMethod.POST)
	public ApplicationDTO verificationResult(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody ApplicationDTO appDTO) throws DataNotFoundException {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		try {
			appDTO= checkListService.verificationResult(appDTO,boilerServ.userByUserLogin(WebSecurity.fetchUserLogin(auth)));
		} catch (ObjectNotFoundException e) {
			logger.error(e.getMessage());
			throw new DataNotFoundException(e);
		}
		appDTO.setValid(true);
		appDTO.setAlertMessage("");
		return appDTO;
	}




	/**
	 * Load events (jobs done) for an application
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param eventsDTO
	 * @return
	 * @throws DataNotFoundException
	 */
	@RequestMapping(value= { "/api/common/application/events"}, method = RequestMethod.POST)
	public ApplicationEventsDTO applicationEvents(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody ApplicationEventsDTO eventsDTO) throws DataNotFoundException {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		try {
			eventsDTO= commonService.applicationEvents(WebSecurity.fetchUserLogin(auth),context,eventsDTO);
		} catch (ObjectNotFoundException e) {
			logger.error(e.getMessage());
			throw new DataNotFoundException(e);
		}
		return eventsDTO;
	}

	/**
	 * 
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param attDTO
	 * @return
	 * @throws DataNotFoundException
	 */
	@RequestMapping(value= { "/api/common/application/attachments"}, method = RequestMethod.POST)
	public AttachmentsDTO applicationAttachments(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody AttachmentsDTO attDTO) throws DataNotFoundException {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		try {
			attDTO= commonService.applicationAttachments(WebSecurity.fetchUserLogin(auth),context,attDTO);
		} catch (ObjectNotFoundException | IOException e) {
			logger.error(e.getMessage());
			throw new DataNotFoundException(e);
		}
		return attDTO;
	}

	/**
	 * Open an attachment for edit or read or initialize a new one
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param attDTO
	 * @return
	 * @throws DataNotFoundException
	 */
	@RequestMapping(value= { "/api/common/attachment/open"}, method = RequestMethod.POST)
	public AttachmentDTO attachmentOpen(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody AttachmentDTO att) throws DataNotFoundException {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		try {
			att= commonService.attachmentOpen(WebSecurity.fetchUserLogin(auth),context,att);
		} catch (ObjectNotFoundException e) {
			logger.error(e.getMessage());
			throw new DataNotFoundException(e);
		}
		return att;
	}

	/**
	 * Upload a file from Documents table
	 * @param docId
	 * @return
	 * @throws org.msh.pdex.ipermit.exceptions.BadQueryParameterException
	 * @throws DataNotFoundException
	 */
	@RequestMapping(value = "/api/common/download/id={id}",  method = RequestMethod.GET)
	public ResponseEntity<Resource> download(@PathVariable(value = "id", required=true) Long docId ) throws DataNotFoundException{
		try {
			FileResource res = commonService.downloadFile(docId);
			return ResponseEntity.ok()
					.contentType(MediaType.parseMediaType(res.getContentType()))
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;  filename=\""+res.getFileName()+"\"")
					.header("filename",res.getFileName())
					.body(res.getResource());
		} catch (ObjectNotFoundException e) {
			throw new DataNotFoundException(e);
		}
	}

	/**
	 * Load product data to the DTO
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param data
	 * @return
	 * @throws DataNotFoundException
	 */
	@RequestMapping(value= { "/api/common/product/load"}, method = RequestMethod.POST)
	public ProductDTO productLoad(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody ProductDTO data) throws DataNotFoundException {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		try {
			data= commonService.productLoad(WebSecurity.fetchUserLogin(auth),context,data);
		} catch (ObjectNotFoundException e) {
			logger.error(e.getMessage());
			throw new DataNotFoundException(e);
		}
		return data;
	}

	@RequestMapping(value= { "/api/common/product/save"}, method = RequestMethod.POST)
	public ProductDTO productSave(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody ProductDTO prodDTO) {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		try {
			prodDTO = commonService.productSave(WebSecurity.fetchUserLogin(auth), WebSecurity.fetchGroup(auth), prodDTO, context);
		} catch (ObjectNotFoundException | IOException e) {
			new DataNotFoundException(e);
			logger.error(e.getMessage());
		}
		return prodDTO;	
	}

	@RequestMapping(value= { "/api/common/inns"}, method = RequestMethod.POST)
	public OptionDTO inns(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody OptionDTO option) throws DataNotFoundException {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));

		try {
			option = commonService.loadInns(option);
		} catch (ObjectNotFoundException e) {
			logger.error(e.getMessage());
			throw new DataNotFoundException(e);
		}
		return option;
	}

	@RequestMapping(value= {"/api/common/inns/add"}, method = RequestMethod.POST)
	public List<ProdInnDTO> innsAdd(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody List<ProdInnDTO> data) throws DataNotFoundException {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));

		if(data == null)
			data = new ArrayList<ProdInnDTO>();

		ProdInnDTO dto = new ProdInnDTO();
		dto.setId(-1);
		dto.setUniqueKey("newInn" + data.size());
		dto.setDos_unit(entityToDTOServ.dosUnitToDTO(null));
		dto.getProduct_innname().getValue().setCode("");
		dto.getDos_strength().setValue("");

		data.add(dto);
		return data;
	}

	@RequestMapping(value= { "/api/common/excipients"}, method = RequestMethod.POST)
	public OptionDTO excipients(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody OptionDTO option) throws DataNotFoundException {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));

		try {
			option = commonService.loadExcs(option);
		} catch (ObjectNotFoundException e) {
			logger.error(e.getMessage());
			throw new DataNotFoundException(e);
		}
		return option;
	}

	@RequestMapping(value= {"/api/common/excs/add"}, method = RequestMethod.POST)
	public List<ProdExcipientDTO> excsAdd(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody List<ProdExcipientDTO> data) throws DataNotFoundException {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));

		if(data == null)
			data = new ArrayList<ProdExcipientDTO>();

		ProdExcipientDTO dto = new ProdExcipientDTO();
		dto.setId(-1);
		dto.setUniqueKey("newExc" + data.size());
		dto.setDos_unit(entityToDTOServ.dosUnitToDTO(null));
		dto.getProduct_active().getValue().setCode("");
		dto.getDos_strength().setValue("");

		data.add(dto);
		return data;
	}

	@RequestMapping(value= { "/api/common/manufacturers"}, method = RequestMethod.POST)
	public OptionDTO manufacturers(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody OptionDTO option) throws DataNotFoundException {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));

		try {
			option = commonService.loadManufacturers(option);
		} catch (ObjectNotFoundException e) {
			logger.error(e.getMessage());
			throw new DataNotFoundException(e);
		}
		return option;
	}

	@RequestMapping(value= {"/api/common/manufs/add"}, method = RequestMethod.POST)
	public List<ManufacturerDTO> manufsAdd(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody List<ManufacturerDTO> data) throws DataNotFoundException {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));

		if(data == null)
			data = new ArrayList<ManufacturerDTO>();

		ManufacturerDTO dto = new ManufacturerDTO();
		dto.setId(-1);
		dto.setCompany_type(entityToDTOServ.companyTypeToOptionDTO(null));
		dto.setUniqueKey("newMan" + data.size());

		data.add(dto);
		return data;
	}

	@RequestMapping(value= { "/api/common/load/company"}, method = RequestMethod.POST)
	public ManufacturerDTO loadCompany(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody ManufacturerDTO data) throws DataNotFoundException {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));

		try {
			data = commonService.loadCompany(data);
		} catch (ObjectNotFoundException e) {
			logger.error(e.getMessage());
			throw new DataNotFoundException(e);
		}
		return data;
	}

	/**
	 * Save an application attachment
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param jsonDto
	 * @param file
	 * @return
	 * @throws DataNotFoundException
	 */
	@RequestMapping(value= { "/api/applicant/upload/save/file","/api/common/upload/save/file"}, method = RequestMethod.POST)
	public AttachmentDTO saveFile(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestParam("dto") String jsonDto,
			@RequestParam("file") Optional<MultipartFile> file) throws DataNotFoundException {

		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		try {
			AttachmentDTO att = objectMapper.readValue(jsonDto, AttachmentDTO.class);
			byte[] fileBytes = new byte[0];
			if(file.isPresent()){
				fileBytes=file.get().getBytes();
				att.getFileName().setValue(file.get().getOriginalFilename());
				att.getFileSize().setValue(file.get().getSize());
				att.getFileContentType().setValue(file.get().getContentType());
			}
			att= commonService.uploadVerify(WebSecurity.fetchUserLogin(auth),context,att);
			if(att.isValid()) {
				commonService.saveFile(WebSecurity.fetchUserLogin(auth),att, fileBytes);
			}
			return att;
		} catch (ObjectNotFoundException | IOException e) {
			logger.error(e.getMessage());
			throw new DataNotFoundException(e);
		}
	}

	/**
	 * Delete an attachment
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param appDTO
	 * @return
	 */
	@RequestMapping(value= { "/api/applicant/attachment/delete","/api/common/attachment/delete"}, method = RequestMethod.POST)
	public AttachmentDTO attachmentDelete(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody AttachmentDTO att) {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		try {
			att = commonService.attachmentDelete(WebSecurity.fetchUserLogin(auth), att, context);
		} catch (ObjectNotFoundException e) {
			new DataNotFoundException(e);
			logger.error(e.getMessage());
		}
		return att;
	}

	/**
	 * Validate file upload data (not the file itself!!!)
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param att
	 * @return
	 * @throws DataNotFoundException
	 */
	@RequestMapping(value= { "/api/common/upload/verify"}, method = RequestMethod.POST)
	public AttachmentDTO uploadVerify(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody AttachmentDTO att) throws DataNotFoundException {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		try {
			att= commonService.uploadVerify(WebSecurity.fetchUserLogin(auth),context,att);
		} catch (ObjectNotFoundException e) {
			logger.error(e.getMessage());
			throw new DataNotFoundException(e);
		}
		return att;
	}


	/**
	 * Create a list of questions asked from a checklist to the application given
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param data
	 * @return
	 * @throws DataNotFoundException
	 */
	@RequestMapping(value= { "/api/common/questions/answers"}, method = RequestMethod.POST)
	public ApplicationDTO questionsAnswer(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody ApplicationDTO data) throws DataNotFoundException
	{
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		userService.updateCurrentRole(auth, context);
		try {
			return notifServ.questionsAnswers(context, WebSecurity.fetchGroup(auth), WebSecurity.fetchUserLogin(auth), data);
		} catch (ObjectNotFoundException | IOException e) {
			logger.error(e.getMessage());
			throw new DataNotFoundException(e);
		}
	}
	/**
	 * Applicant data
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param data
	 * @return
	 * @throws DataNotFoundException
	 */
	@RequestMapping(value= { "/api/common/applicant"}, method = RequestMethod.POST)
	public ApplicantDTO applicant(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody ApplicantDTO data) throws DataNotFoundException
	{
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		userService.updateCurrentRole(auth, context);
		try {
			return commonService.applicant(context, WebSecurity.fetchGroup(auth), WebSecurity.fetchUserLogin(auth), data);
		} catch (ObjectNotFoundException e) {
			logger.error(e.getMessage());
			throw new DataNotFoundException(e);
		}
	}

	/**
	 * Cancel an application. Allowed only for Applicant - owner and moderator
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param data
	 * @return
	 * @throws DataNotFoundException
	 */
	@RequestMapping(value= { "/api/common/application/cancel"}, method = RequestMethod.POST)
	public ApplicationDTO applicationCancel(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody ApplicationDTO data) throws DataNotFoundException
	{
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		userService.updateCurrentRole(auth, context);
		try {
			return commonService.applicationCancel(context, WebSecurity.fetchGroup(auth), WebSecurity.fetchUserLogin(auth), data);
		} catch (ObjectNotFoundException | IOException e) {
			logger.error(e.getMessage());
			throw new DataNotFoundException(e);
		}
	}

	/**
	 * load an application and validate the markup
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param data
	 * @return
	 */
	@RequestMapping(value= { "/api/common/marking/open"}, method = RequestMethod.POST)
	public ApplicationDTO markingOpen(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody ApplicationDTO data) {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		try {
			data = moderServ.markingOpen(WebSecurity.fetchUserLogin(auth), WebSecurity.fetchGroup(auth), data, context);
		} catch (ObjectNotFoundException | IOException e) {
			new DataNotFoundException(e);
			logger.error(e.getMessage());
		}
		return data;
	}

	/**
	 * load an finalize markup of an application
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param data
	 * @return
	 */
	@RequestMapping(value= { "/api/common/finalize/open"}, method = RequestMethod.POST)
	public ApplicationDTO finalizeOpen(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody ApplicationDTO data) {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		try {
			data = moderServ.markingOpen(WebSecurity.fetchUserLogin(auth),  WebSecurity.fetchGroup(auth), data, context);
		} catch (ObjectNotFoundException | IOException e) {
			new DataNotFoundException(e);
			logger.error(e.getMessage());
		}
		return data;
	}

	/**
	 * Get user's details for just authentificated user
	 * @param user
	 * @return
	 */
	@RequestMapping(value= { "/api/common/changeuserrole"}, method = RequestMethod.POST)
	public UserDetailsDTO changeuserrole(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody Long data) {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		try {
			//save to context current role id
			contextServices.saveCurrentUserRole(context, data, ContextServices.CURRENTROLE_ID);

			UserDetailsDTO ret = userService.loadUserDetailsDTO(auth.getName());

			// find current role
			UserRoleDto roledto = userService.findCurrentRoleDto(ret.getAuthorities(), data);
			ret.updateCurrentRole(roledto, messages);

			return ret;
		} catch (IOException e) {
			new DataNotFoundException(e);
			logger.error(e.getMessage());
		}
		return null;
	}

	@RequestMapping(value= { "/api/common/upload/checklist"}, method = RequestMethod.POST)
	public ModelAndView uploadCheckList(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody  ApplicationDTO appDTO) throws DataNotFoundException {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		try {
			if(appDTO.getTypeTmplCheckList().length() > 0) {
				TemplateType type = TemplateType.valueOf(appDTO.getTypeTmplCheckList());
				Map<String, Object> model = documentService.checkListModel(type, appDTO, WebSecurity.fetchUserLogin(auth));
				response.setHeader( HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"checklist.docx\"");
				response.setHeader("filename", "checklist.docx");
				return new ModelAndView(new DocxView(documentService.checklistTemplate(type)), model);
			}
			return null;
		} catch (ObjectNotFoundException e) {
			logger.error(e.getMessage());
			throw new DataNotFoundException(e);
		}
	}

	/**
	 * Open an application for tracking
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param navigator
	 * @return
	 * @throws DataNotFoundException
	 */
	@RequestMapping(value= { "/api/common/application/track"}, method = RequestMethod.POST)
	public ApplicationDTO applicationTrack(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody ApplicationDTO data) throws DataNotFoundException {
		try {
			Context context = contextServices.loadContext(contextId);
			data =commonService.applicationTrack(context, WebSecurity.fetchUserLogin(auth), WebSecurity.fetchGroup(auth),data);
		} catch (ObjectNotFoundException | IOException e) {
			throw new DataNotFoundException(e);
		}
		return data;
	}

}

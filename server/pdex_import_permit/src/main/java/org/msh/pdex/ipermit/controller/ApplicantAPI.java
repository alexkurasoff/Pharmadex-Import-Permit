package org.msh.pdex.ipermit.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.msh.pdex.exceptions.ObjectNotFoundException;
import org.msh.pdex.i18N.Messages;
import org.msh.pdex.ipermit.WebSecurity;
import org.msh.pdex.ipermit.dto.ApplicationDTO;
import org.msh.pdex.ipermit.dto.ApplicationDetailDTO;
import org.msh.pdex.ipermit.dto.ApplicationsButtonsDTO;
import org.msh.pdex.ipermit.dto.QuotasDTO;
import org.msh.pdex.ipermit.dto.QuoteDTO;
import org.msh.pdex.ipermit.exceptions.DataNotFoundException;
import org.msh.pdex.ipermit.services.ApplicantService;
import org.msh.pdex.ipermit.services.CommonService;
import org.msh.pdex.ipermit.services.DocumentService;
import org.msh.pdex.model.rsecond.Context;
import org.msh.pdex.services.ContextServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 * All API needed for an applicant
 * @author alexk
 *
 */
@RestController
public class ApplicantAPI {
	private static final Logger logger = LoggerFactory.getLogger(ApplicantAPI.class);

	@Autowired
	private ContextServices contextServices;

	@Autowired
	private Messages mess;

	@Autowired
	private ApplicantService applicantService;
	@Autowired
	private DocumentService documentService;
	@Autowired
	private CommonService commonServ;

	/**
	 * Select product quotas for an applicant
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param data
	 * @return
	 * @throws DataNotFoundException
	 */
	@RequestMapping(value= { "/api/applicant/quotas"}, method = RequestMethod.POST)
	public QuotasDTO quotas(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody QuotasDTO data) throws DataNotFoundException {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		QuotasDTO ret;
		try {
			ret = applicantService.quotas(data, WebSecurity.fetchUserLogin(auth), context);
		} catch (ObjectNotFoundException | IOException e) {
			logger.error(e.getMessage());
			throw new DataNotFoundException(e);
		}
		return ret;
	}
	/**
	 * the list of applictins for an applicant - Excel version
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param appDTO
	 * @return
	 */
	@RequestMapping(value= { "/api/applicant/quotas/excel"}, method = RequestMethod.POST)
	public ModelAndView quotasExcel(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody  QuotasDTO data) {
			Context context = contextServices.loadContext(contextId);
			response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
			Map<String, Object> model = new HashMap<String, Object>();
			QuotasDTO ret;
			try {
				ret = applicantService.quotas(data, WebSecurity.fetchUserLogin(auth), context);
				ret.getTable().getHeaders().setPageSize(Integer.MAX_VALUE);
				String name = mess.get("quotas");
				model.put(ExcelView.SHEETNAME, name);
				//Title
				model.put(ExcelView.TITLE, mess.get("quotas"));
				//Headers List
				model.put(ExcelView.HEADERS, ret.getTable().getHeaders().getHeaders());
				//Rows
				model.put(ExcelView.ROWS, ret.getTable().getRows());
				response.setHeader( HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"quotas.xlsx\"");
				response.setHeader("filename", "quotas.xlsx");       
			} catch (ObjectNotFoundException | IOException e) {
				new DataNotFoundException(e);
				logger.error(e.getMessage());
			}
			return new ModelAndView(new ExcelView(), model);
	}
	

	/**
	 * Create and save new empty draft application in accordance with ApplicationsButtonsDTO
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param appDTO
	 * @return
	 * @throws DataNotFoundException
	 */
	@RequestMapping(value= { "/api/applicant/application/create"}, method = RequestMethod.POST)
	public ApplicationsButtonsDTO applicationInit(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody ApplicationsButtonsDTO data) throws DataNotFoundException {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		try {
			data= applicantService.applicationCreate(WebSecurity.fetchUserLogin(auth),WebSecurity.fetchGroup(auth),context,data);
		} catch (ObjectNotFoundException e) {
			logger.error(e.getMessage());
			throw new DataNotFoundException(e);
		}
		return data;
	}
	
	/**
	 * Open an application
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param appDTO
	 * @return
	 * @throws DataNotFoundException
	 */
	@RequestMapping(value= { "/api/applicant/application/open"}, method = RequestMethod.POST)
	public ApplicationDTO applicationOpen(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody ApplicationDTO appDTO) throws DataNotFoundException {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		try {
			//perform logic
			appDTO= applicantService.applicationOpen(WebSecurity.fetchUserLogin(auth),WebSecurity.fetchGroup(auth),context,appDTO);
			//where to navigate
			appDTO.getNavigator().setId(appDTO.getId());
			appDTO.setNavigator(commonServ.applicationOpen(WebSecurity.fetchGroup(auth), context, appDTO.getNavigator()));
		} catch (ObjectNotFoundException | IOException e) {
			logger.error(e.getMessage());
			throw new DataNotFoundException(e);
		}
		return appDTO;
	}

	
	/**
	 * verify and save the applicationS
	 * we allows to save an application that not fulfill all requirements. Only warnings will be displayed
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param appDTO
	 * @return
	 */
	@RequestMapping(value= { "/api/applicant/application/save"}, method = RequestMethod.POST)
	public ApplicationDTO applicationSave(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody ApplicationDTO appDTO) {
			Context context = contextServices.loadContext(contextId);
			response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
			try {
				appDTO = applicantService.applicationSave(WebSecurity.fetchUserLogin(auth), WebSecurity.fetchGroup(auth), appDTO, context);
			} catch (ObjectNotFoundException | IOException e) {
				new DataNotFoundException(e);
				logger.error(e.getMessage());
			}
		return appDTO;
	}
	
	/**
	 * Expand a product quota to quotas mentioned for each actual PIP
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param appDTO
	 * @return
	 */
	@RequestMapping(value= { "/api/applicant/quotas/expand"}, method = RequestMethod.POST)
	public QuoteDTO quotasExpand(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody QuoteDTO qDTO) {
			Context context = contextServices.loadContext(contextId);
			response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
			try {
				qDTO = applicantService.quotasExpand(WebSecurity.fetchUserLogin(auth), qDTO, context);
			} catch (ObjectNotFoundException | IOException e) {
				new DataNotFoundException(e);
				logger.error(e.getMessage());
			}
		return qDTO;
	}
	
	/**
	 * Strict verify an application
	 * Errors will be displayed
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param appDTO
	 * @return
	 */
	@RequestMapping(value= { "/api/applicant/application/verify"}, method = RequestMethod.POST)
	public ApplicationDTO applicationVerify(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody ApplicationDTO appDTO) {
			Context context = contextServices.loadContext(contextId);
			response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
			try {
				appDTO = applicantService.applicationVerify(WebSecurity.fetchUserLogin(auth), WebSecurity.fetchGroup(auth), appDTO, context);
			} catch (ObjectNotFoundException | IOException e) {
				new DataNotFoundException(e);
				logger.error(e.getMessage());
			}
		return appDTO;
		
	}
	
	/**
	 * Submit an application
	 * Errors will be displayed
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param appDTO
	 * @return
	 */
	@RequestMapping(value= { "/api/applicant/application/submit"}, method = RequestMethod.POST)
	public ApplicationDTO applicationSubmit(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody ApplicationDTO appDTO) {
			Context context = contextServices.loadContext(contextId);
			response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
			try {
				appDTO = applicantService.applicationSubmit(WebSecurity.fetchUserLogin(auth), WebSecurity.fetchGroup(auth), appDTO, context);
			} catch (ObjectNotFoundException | IOException e) {
				new DataNotFoundException(e);
				logger.error(e.getMessage());
			}
		return appDTO;
		
	}
	
	/**
	 * Upload a main document
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param appDTO
	 * @return
	 * @throws DataNotFoundException 
	 */
	@RequestMapping(value= { "/api/applicant/upload/bief"}, method = RequestMethod.POST)
	public ModelAndView uploadBief(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody  ApplicationDTO appDTO) throws DataNotFoundException {
			Context context = contextServices.loadContext(contextId);
			response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
			try {
				Map<String, Object> model =documentService.biefModel(appDTO.getId(), WebSecurity.fetchUserLogin(auth));
				response.setHeader( HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"bief.docx\"");
				response.setHeader("filename", "bief.docx");
				return new ModelAndView(new DocxView(documentService.biefTemplate()), model);
			} catch (ObjectNotFoundException e) {
				logger.error(e.getMessage());
				throw new DataNotFoundException(e);
			}
	}
	

	
	/**
	 * Save new detail record, i.e. Import_permit_product
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param appDTO
	 * @return
	 * @throws DataNotFoundException 
	 */
	@RequestMapping(value= { "/api/applicant/application/detail/save"}, method = RequestMethod.POST)
	public ApplicationDetailDTO applicationDetailSave(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody ApplicationDetailDTO data) throws DataNotFoundException {
			Context context = contextServices.loadContext(contextId);
			response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
			try {
				data = applicantService.applicationDetailSave(WebSecurity.fetchUserLogin(auth), WebSecurity.fetchGroup(auth), data, context);
			} catch (ObjectNotFoundException e) {
				throw new  DataNotFoundException(e);
			}
		return data;
	}
	
	/**
	 * Delete a detail record, i.e. Import_permit_product
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param appDTO
	 * @return
	 */
	@RequestMapping(value= { "/api/applicant/application/detail/delete"}, method = RequestMethod.POST)
	public ApplicationDetailDTO applicationDetailDelete(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody ApplicationDetailDTO data) {
			Context context = contextServices.loadContext(contextId);
			response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
			try {
				data = applicantService.applicationDetailDelete(WebSecurity.fetchUserLogin(auth), WebSecurity.fetchGroup(auth), data, context);
			} catch (ObjectNotFoundException e) {
				new DataNotFoundException(e);
				logger.error(e.getMessage());
			}
		return data;
	}
	
	/**
	 * Load or create new detail record, i.e. Import_permit_product
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param appDTO
	 * @return
	 */
	@RequestMapping(value= { "/api/applicant/application/detail"}, method = RequestMethod.POST)
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
	 * Delete application draft only if all questions are answered
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param data
	 * @return
	 */
	@RequestMapping(value= { "/api/applicant/application/delete"}, method = RequestMethod.POST)
	public ApplicationDTO applicationDelete(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody ApplicationDTO data) {
			Context context = contextServices.loadContext(contextId);
			response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
			try {
				data = applicantService.applicationDelete(WebSecurity.fetchUserLogin(auth), WebSecurity.fetchGroup(auth), data, context);
			} catch (ObjectNotFoundException | IOException e) {
				new DataNotFoundException(e);
				logger.error(e.getMessage());
			}
		return data;
	}
}

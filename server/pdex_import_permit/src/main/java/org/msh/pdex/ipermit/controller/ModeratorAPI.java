package org.msh.pdex.ipermit.controller;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.msh.pdex.dto.CheckListsDTO;
import org.msh.pdex.dto.DocTypesDTO;
import org.msh.pdex.dto.NavigatorDTO;
import org.msh.pdex.exceptions.ObjectNotFoundException;
import org.msh.pdex.ipermit.WebSecurity;
import org.msh.pdex.ipermit.dto.ApplicationDTO;
import org.msh.pdex.ipermit.exceptions.DataNotFoundException;
import org.msh.pdex.ipermit.services.BoilerplateServices;
import org.msh.pdex.ipermit.services.CheckListService;
import org.msh.pdex.ipermit.services.CommonService;
import org.msh.pdex.ipermit.services.ModeratorService;
import org.msh.pdex.model.rsecond.Context;
import org.msh.pdex.services.ContextServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

@RestController
public class ModeratorAPI {
	private static final Logger logger = LoggerFactory.getLogger(ApplicantAPI.class);
	@Autowired
	private ContextServices contextServices;
	
	@Autowired
	private ModeratorService moderServ;
	
	@Autowired
	private CheckListService checkListService;
	
	@Autowired
	BoilerplateServices boilerServ;
	
	@Autowired
	private CommonService commonServ;

	
	/**
	 * Validate and save marking data
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param data
	 * @return
	 */
	@RequestMapping(value= { "/api/moderator/marking/save"}, method = RequestMethod.POST)
	public ApplicationDTO markingSave(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody ApplicationDTO data) {
			Context context = contextServices.loadContext(contextId);
			response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
			try {
				data = moderServ.markupSave(WebSecurity.fetchUserLogin(auth),  WebSecurity.fetchGroup(auth),data, context);
			} catch (ObjectNotFoundException | IOException e) {
				new DataNotFoundException(e);
				logger.error(e.getMessage());
			}
		return data;
	}
	
	/**
	 * Validate and save finalize data
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param data
	 * @return
	 */
	@RequestMapping(value= { "/api/moderator/finalize/save"}, method = RequestMethod.POST)
	public ApplicationDTO finalizeSave(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody ApplicationDTO data) {
			Context context = contextServices.loadContext(contextId);
			response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
			try {
				data = moderServ.finalizeSave(WebSecurity.fetchUserLogin(auth), WebSecurity.fetchGroup(auth), data, context);
			} catch (ObjectNotFoundException | IOException e) {
				new DataNotFoundException(e);
				logger.error(e.getMessage());
			}
		return data;
	}
	
	/**
	 * Submit this application to invoicing
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param data
	 * @return
	 */
	@RequestMapping(value= { "/api/moderator/submit/invoicing"}, method = RequestMethod.POST)
	public ApplicationDTO submitInvoicing(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody ApplicationDTO data) {
			Context context = contextServices.loadContext(contextId);
			response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
			try {
				data = moderServ.submitInvoicing(WebSecurity.fetchUserLogin(auth), WebSecurity.fetchGroup(auth),data, context);
			} catch (ObjectNotFoundException | IOException e) {
				new DataNotFoundException(e);
				logger.error(e.getMessage());
			}
		return data;
	}
	
	/**
	 * Submit this application to approval
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param data
	 * @return
	 */
	@RequestMapping(value= { "/api/moderator/submit/approval"}, method = RequestMethod.POST)
	public ApplicationDTO submitApproval(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody ApplicationDTO data) {
			Context context = contextServices.loadContext(contextId);
			response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
			try {
				data = moderServ.submitApproval(WebSecurity.fetchUserLogin(auth),  WebSecurity.fetchGroup(auth),data, context);
			} catch (ObjectNotFoundException | IOException e) {
				new DataNotFoundException(e);
				logger.error(e.getMessage());
			}
		return data;
	}
	
	/**
	 * Submit this application to approval
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param data
	 * @return
	 */
	@RequestMapping(value= { "/api/moderator/submit/rejection"}, method = RequestMethod.POST)
	public ApplicationDTO submitRejection(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody ApplicationDTO data) {
			Context context = contextServices.loadContext(contextId);
			response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
			try {
				data = moderServ.submitRejection(WebSecurity.fetchUserLogin(auth),  WebSecurity.fetchGroup(auth),data, context);
			} catch (ObjectNotFoundException | IOException e) {
				new DataNotFoundException(e);
				logger.error(e.getMessage());
			}
		return data;
	}
	
	/**
	 * Approve and send to secretary
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param data
	 * @return
	 */
	@RequestMapping(value= { "/api/moderator/application/approval"}, method = RequestMethod.POST)
	public ApplicationDTO applicationApproval(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody ApplicationDTO data) {
			Context context = contextServices.loadContext(contextId);
			response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
			try {
				data = moderServ.applicationApproval(WebSecurity.fetchUserLogin(auth),  WebSecurity.fetchGroup(auth), data, context);
			} catch (ObjectNotFoundException | IOException e) {
				new DataNotFoundException(e);
				logger.error(e.getMessage());
			}
		return data;
	}
	
	/**
	 * Return an application to an applicant via secretary office 
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param data
	 * @return
	 */
	@RequestMapping(value= { "/api/moderator/application/return"}, method = RequestMethod.POST)
	public ApplicationDTO applicationReturn(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody ApplicationDTO data) {
			Context context = contextServices.loadContext(contextId);
			response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
			try {
				data = moderServ.applicationReturnl(WebSecurity.fetchUserLogin(auth),  WebSecurity.fetchGroup(auth), data, context);
			} catch (ObjectNotFoundException | IOException e) {
				new DataNotFoundException(e);
				logger.error(e.getMessage());
			}
		return data;
	}
	
	@RequestMapping(value= { "/api/moderator/checklists"}, method = RequestMethod.POST)
	public CheckListsDTO checklists(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody CheckListsDTO data) throws DataNotFoundException {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		try {
			data = checkListService.checklists(WebSecurity.fetchUserLogin(auth), data, context);
		} catch (ObjectNotFoundException | IOException e) {
			new DataNotFoundException(e);
			logger.error(e.getMessage());
		}
		return data;
	}
	
	@RequestMapping(value= { "/api/moderator/checklist/open"}, method = RequestMethod.POST)
	public NavigatorDTO checklistOpen(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody NavigatorDTO navigator) throws DataNotFoundException {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		try {
			navigator= checkListService.checklistOpen(WebSecurity.fetchUserLogin(auth), context, navigator);
		} catch (ObjectNotFoundException e) {
			logger.error(e.getMessage());
			throw new DataNotFoundException(e);
		}
		return navigator;
	}
	
	@RequestMapping(value= { "/api/moderator/checklist/open/edit"}, method = RequestMethod.POST)
	public CheckListsDTO checklistOpenEdit(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody CheckListsDTO data) throws DataNotFoundException {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		try {
			data = checkListService.checkListOpenEdit(boilerServ.userByUserLogin(WebSecurity.fetchUserLogin(auth)), context, data);
			return data;
		} catch (ObjectNotFoundException | IOException e) {
			logger.error(e.getMessage());
			throw new DataNotFoundException(e);
		}
	}
	
	@RequestMapping(value= { "/api/moderator/question/save"}, method = RequestMethod.POST)
	public CheckListsDTO questionSave(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody CheckListsDTO data) throws DataNotFoundException {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		try {
			data = checkListService.questionTemplateSave(WebSecurity.fetchUserLogin(auth), context, data);
		} catch (ObjectNotFoundException e) {
			logger.error(e.getMessage());
			throw new DataNotFoundException(e);
		}
		return data;
	}
	
	@RequestMapping(value= { "/api/moderator/documents"}, method = RequestMethod.POST)
	public DocTypesDTO checklists(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody DocTypesDTO data) throws DataNotFoundException {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		try {
			data = moderServ.documents(WebSecurity.fetchUserLogin(auth), data, context);
		} catch (ObjectNotFoundException | IOException e) {
			new DataNotFoundException(e);
			logger.error(e.getMessage());
		}
		return data;
	}
	
	@RequestMapping(value= { "/api/moderator/document/open"}, method = RequestMethod.POST)
	public NavigatorDTO documentOpen(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody NavigatorDTO navigator) throws DataNotFoundException {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		try {
			navigator= moderServ.documentOpen(WebSecurity.fetchUserLogin(auth), context, navigator);
		} catch (ObjectNotFoundException e) {
			logger.error(e.getMessage());
			throw new DataNotFoundException(e);
		}
		return navigator;
	}
	
	@RequestMapping(value= { "/api/moderator/document/open/edit"}, method = RequestMethod.POST)
	public DocTypesDTO documentOpenEdit(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody DocTypesDTO data) throws DataNotFoundException {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		try {
			data = moderServ.documentOpenEdit(boilerServ.userByUserLogin(WebSecurity.fetchUserLogin(auth)), context, data);
			return data;
		} catch (ObjectNotFoundException | IOException e) {
			logger.error(e.getMessage());
			throw new DataNotFoundException(e);
		}
	}
	
	@RequestMapping(value= { "/api/moderator/document/save"}, method = RequestMethod.POST)
	public DocTypesDTO documentSave(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody DocTypesDTO data) throws DataNotFoundException, JsonParseException, JsonMappingException, IOException {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		try {
			if(data.getId() > 0) {
				data = moderServ.documentUpdate(WebSecurity.fetchUserLogin(auth), context, data);
			}else {
				data = moderServ.documentSave(WebSecurity.fetchUserLogin(auth), context, data);
			}
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
	@RequestMapping(value= { "/api/moderator/application/open"}, method = RequestMethod.POST)
	public ApplicationDTO applicationOpen(
			Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody ApplicationDTO appDTO
			) throws  DataNotFoundException {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		try {
			appDTO = moderServ.applicationOpen(WebSecurity.fetchUserLogin(auth), WebSecurity.fetchGroup(auth),context,appDTO);
			//where to navigate
			appDTO.getNavigator().setId(appDTO.getId());
			appDTO.setNavigator(commonServ.applicationOpen(WebSecurity.fetchGroup(auth), context, appDTO.getNavigator()));
		} catch (IOException | ObjectNotFoundException e) {
			logger.error(e.getMessage());
			throw new DataNotFoundException(e);
		}
		return appDTO;
	}
}

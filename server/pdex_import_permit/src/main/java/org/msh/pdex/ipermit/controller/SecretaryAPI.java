package org.msh.pdex.ipermit.controller;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.msh.pdex.exceptions.ObjectNotFoundException;
import org.msh.pdex.ipermit.WebSecurity;
import org.msh.pdex.ipermit.dto.ApplicationDTO;
import org.msh.pdex.ipermit.exceptions.DataNotFoundException;
import org.msh.pdex.ipermit.services.ApplicantService;
import org.msh.pdex.ipermit.services.CommonService;
import org.msh.pdex.ipermit.services.NotificationService;
import org.msh.pdex.ipermit.services.SecretaryService;
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

/**
 * Controller for DNF Secretarie's API
 * @author alexk
 *
 */
@RestController
public class SecretaryAPI {
	
	private static final Logger logger = LoggerFactory.getLogger(SecretaryAPI.class);

	@Autowired
	private ContextServices contextServices;

	@Autowired
	ApplicantService applicantService;
	
	@Autowired
	SecretaryService secretaryService;
	
	@Autowired
	NotificationService notifServ;
	
	@Autowired
	private CommonService commonServ;

	
	/**
	 * Submit (pass) to verification
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param appDTO
	 * @return
	 * @throws DataNotFoundException
	 */
	@RequestMapping(value= { "/api/secretary/passtoverification"}, method = RequestMethod.POST)
	public ApplicationDTO passToVerification(
			Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody ApplicationDTO appDTO
			) throws  DataNotFoundException {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		try {
			appDTO = secretaryService.passToVerification(WebSecurity.fetchUserLogin(auth), WebSecurity.fetchGroup(auth),context,appDTO);
		} catch (IOException | ObjectNotFoundException e) {
			logger.error(e.getMessage());
			throw new DataNotFoundException(e);
		}
		return appDTO;
	}
	
	/**
	 * Submit (pass) to verification
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param appDTO
	 * @return
	 * @throws DataNotFoundException
	 */
	@RequestMapping(value= { "/api/secretary/application/archive"}, method = RequestMethod.POST)
	public ApplicationDTO applicationArchive(
			Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody ApplicationDTO appDTO
			) throws  DataNotFoundException {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		try {
			appDTO = secretaryService.applicationArchive(WebSecurity.fetchUserLogin(auth),  WebSecurity.fetchGroup(auth),context,appDTO);
		} catch (IOException | ObjectNotFoundException e) {
			logger.error(e.getMessage());
			throw new DataNotFoundException(e);
		}
		return appDTO;
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
	@RequestMapping(value= { "/api/secretary/application/open"}, method = RequestMethod.POST)
	public ApplicationDTO applicationOpen(
			Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody ApplicationDTO appDTO
			) throws  DataNotFoundException {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		try {
			appDTO = secretaryService.applicationOpen(WebSecurity.fetchUserLogin(auth), WebSecurity.fetchGroup(auth),context,appDTO);
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

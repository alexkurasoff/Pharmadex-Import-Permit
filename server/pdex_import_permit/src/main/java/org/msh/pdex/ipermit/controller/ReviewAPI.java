package org.msh.pdex.ipermit.controller;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.msh.pdex.exceptions.ObjectNotFoundException;
import org.msh.pdex.ipermit.WebSecurity;
import org.msh.pdex.ipermit.dto.ApplicationDTO;
import org.msh.pdex.ipermit.exceptions.DataNotFoundException;
import org.msh.pdex.ipermit.services.BoilerplateServices;
import org.msh.pdex.ipermit.services.CommonService;
import org.msh.pdex.ipermit.services.ResponsibleService;
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
 * Review by Import Responsible
 * @author alexk
 *
 */
@RestController
public class ReviewAPI {
	private static final Logger logger = LoggerFactory.getLogger(ReviewAPI.class);
	
	@Autowired
	ResponsibleService reviewService;
	@Autowired
	BoilerplateServices boilerServ;
	@Autowired
	ContextServices contextServices;
	@Autowired
	private CommonService commonServ;
	
	/**
	 * Submit application for approval
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param data
	 * @return
	 */
	@RequestMapping(value= { "/api/review/approve"}, method = RequestMethod.POST)
	public ApplicationDTO submitForApproval(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody ApplicationDTO data) {
			Context context = contextServices.loadContext(contextId);
			response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
			try {
				data= reviewService.approve(WebSecurity.fetchUserLogin(auth), WebSecurity.fetchGroup(auth), data, context);
				
			} catch (ObjectNotFoundException | IOException e) {
				new DataNotFoundException(e);
				logger.error(e.getMessage());
			}
		return data;
	}
	
	/**
	 * Submit for reject
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param data
	 * @return
	 */
	@RequestMapping(value= { "/api/review/reject"}, method = RequestMethod.POST)
	public ApplicationDTO submitForReject(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody ApplicationDTO data) {
			Context context = contextServices.loadContext(contextId);
			response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
			try {
				data= reviewService.reject(WebSecurity.fetchUserLogin(auth), WebSecurity.fetchGroup(auth), data, context);
				
			} catch (ObjectNotFoundException | IOException e) {
				new DataNotFoundException(e);
				logger.error(e.getMessage());
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
	@RequestMapping(value= { "/api/review/application/open"}, method = RequestMethod.POST)
	public ApplicationDTO applicationOpen(
			Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody ApplicationDTO appDTO
			) throws  DataNotFoundException {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		try {
			appDTO = reviewService.applicationOpen(WebSecurity.fetchUserLogin(auth), WebSecurity.fetchGroup(auth),context,appDTO);
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

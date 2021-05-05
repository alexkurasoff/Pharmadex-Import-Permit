package org.msh.pdex.ipermit.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.msh.pdex.dto.NavigatorDTO;
import org.msh.pdex.dto.NotificationsDTO;
import org.msh.pdex.dto.QuestionDTO;
import org.msh.pdex.exceptions.ObjectNotFoundException;
import org.msh.pdex.i18N.Messages;
import org.msh.pdex.ipermit.WebSecurity;
import org.msh.pdex.ipermit.exceptions.DataNotFoundException;
import org.msh.pdex.ipermit.services.CheckListService;
import org.msh.pdex.ipermit.services.NotificationService;
import org.msh.pdex.ipermit.services.UserService;
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
 * All API needed for review
 * @author alexk
 *
 */
@RestController
public class NotificationsAPI {
	private static final Logger logger = LoggerFactory.getLogger(NotificationsAPI.class);
	@Autowired
	private NotificationService notifService;
	
	@Autowired
	private CheckListService checkListService;

	@Autowired
	private ContextServices contextServices;
	
	@Autowired
	private Messages mess;
	
	@Autowired
	UserService userService;

	/**
	 * Get notifications for a person defined in auth and for a group as well
	 * @param auth
	 * @param data
	 * @return
	 * @throws DataNotFoundException 
	 */
	@RequestMapping(value= { "/api/notifications/list"}, method = RequestMethod.POST)
	public NotificationsDTO notificationsList(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody NotificationsDTO data) throws DataNotFoundException
	{
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		userService.updateCurrentRole(auth, context);
		
		try {
			return notifService.notifications(context, WebSecurity.fetchGroup(auth), WebSecurity.fetchUserLogin(auth), data);
		} catch (ObjectNotFoundException | IOException e) {
			logger.error(e.getMessage());
			throw new DataNotFoundException(e);
		}
	}


	@RequestMapping(value= { "/api/notifications/list/excel"},  method = {RequestMethod.POST, RequestMethod.GET})
	public ModelAndView notificationsListExcel(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			@RequestBody(required=false) NotificationsDTO data, 
			HttpServletResponse response) throws DataNotFoundException{
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		userService.updateCurrentRole(auth, context);
		
		try {
			data.getTable().getHeaders().setPageSize(Integer.MAX_VALUE);  //to ensure all data
			NotificationsDTO ret = notifService.notifications(context, WebSecurity.fetchGroup(auth), WebSecurity.fetchUserLogin(auth), data);
			Map<String, Object> model = new HashMap<String, Object>();
			//Sheet Name
			model.put(ExcelView.SHEETNAME, mess.get("notifications"));
			//Title
			model.put(ExcelView.TITLE, mess.get("notifications"));
			//Headers List
			model.put(ExcelView.HEADERS, ret.getTable().getHeaders().getHeaders());
			//Rows
			model.put(ExcelView.ROWS, ret.getTable().getRows());
			response.setHeader( HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"notifications.xlsx\"");
			response.setHeader("filename", "notifications.xlsx");       
			return new ModelAndView(new ExcelView(), model);
		} catch (ObjectNotFoundException | IOException  e) {
			logger.error(e.getMessage());
			throw new DataNotFoundException(e);
		}
	}
	
	/**
	 * Determine component or URL to navigate after the link has pressed
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param data
	 * @return
	 * @throws DataNotFoundException
	 */
	@RequestMapping(value= { "/api/notifications/link"}, method = RequestMethod.POST)
	public NavigatorDTO notificationsLink(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody NavigatorDTO data) throws DataNotFoundException
	{
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		userService.updateCurrentRole(auth, context);
		
		try {
			return notifService.notificationsLink(context, WebSecurity.fetchGroup(auth), WebSecurity.fetchUserLogin(auth), data);
		} catch (ObjectNotFoundException e) {
			logger.error(e.getMessage());
			throw new DataNotFoundException(e);
		}
	}
	

	
	/**
	 * This question has been answered. Document is attached
	 * @param auth
	 * @param contextId
	 * @param response
	 * @param data
	 * @return
	 * @throws DataNotFoundException
	 */
	@RequestMapping(value= { "/api/common/notification/question/answered"}, method = RequestMethod.POST)
	public QuestionDTO notificationsQuestionAnswer(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody QuestionDTO data) throws DataNotFoundException
	{
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		userService.updateCurrentRole(auth, context);
		
		try {
			return checkListService.questionAnswered(context, WebSecurity.fetchGroup(auth), WebSecurity.fetchUserLogin(auth), data);
		} catch (ObjectNotFoundException e) {
			logger.error(e.getMessage());
			throw new DataNotFoundException(e);
		}
	}
}

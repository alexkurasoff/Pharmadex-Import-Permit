package org.msh.pdex.ipermit.controller;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.msh.pdex.i18N.Messages;
import org.msh.pdex.ipermit.WebSecurity;
import org.msh.pdex.ipermit.services.UserService;
import org.msh.pdex.model.rsecond.Context;
import org.msh.pdex.services.ContextServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * This controller provides access to this multi page Web Application
 * It is responsible for all non AJAX queries:
 * <ul>
 * <li>queries for new pages, typically get queries - navigate by user
 * <li>not ajax FORM SUBMITS - post queries
 * </ul>
 * @author Alex Kurasoff
 *
 */
@RestController
public class WebApp{

	
	@Value("${server.servlet.context-path:}")
	private String contextPath;
	
	@Autowired
	Messages messages;

	@Autowired
	private ContextServices contextServices;
	
	@Autowired
	UserService userService;

	private static final Logger logger = LoggerFactory.getLogger(WebApp.class);
	
	/**
	 * redirect to a tabset depends on auth data
	 * @return
	 * @throws JsonProcessingException 
	 */
	@GetMapping({"/"})
	public RedirectView redirectHome(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response) throws JsonProcessingException {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		if(auth != null)
			userService.updateCurrentRole(auth, context);
		else// this is /logout - clear current role id in context
			contextServices.saveCurrentUserRole(context, new Long(0), ContextServices.CURRENTROLE_ID);
		return new RedirectView(WebSecurity.fetchGroup(auth));
	}

	
	@RequestMapping(value = {"/anonymous"},  method = RequestMethod.GET)
	public ModelAndView anonymous(){
		return createView("anonymous");
	}
	@RequestMapping(value = {"/applicant"},  method = RequestMethod.GET)
	public ModelAndView applicant(){
		return createView( "applicant");
	}
	@RequestMapping(value = {"/secretary"},  method = RequestMethod.GET)
	public ModelAndView secretary(){
		return createView("secretary");
	}
	@RequestMapping(value = {"/moderator"},  method = RequestMethod.GET)
	public ModelAndView moderator(){
		return createView("moderator");
	}
	@RequestMapping(value = {"/review"},  method = RequestMethod.GET)
	public ModelAndView review(){
		return createView("review");
	}
	
	/**
	 * Home view
	 * @param userModule 
	 * @return
	 */
	private ModelAndView createView(String userModule) {
		ModelAndView mv = createWithBundles(userModule);
		mv.addObject("title",messages.get("application.title"));
		mv.addObject("contextPath", contextPath);
		return mv;
	}
	/**
	 * Create ModelAndView and resolve js bundle names. Currently we use only Application template
	 * @param userModule 
	 * @return
	 */
	private ModelAndView createWithBundles(String userModule) {
		ModelAndView mv = new ModelAndView(userModule);
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		try {
			Resource[] bundles = resolver.getResources("classpath:static/js/*.js");
			if(bundles.length>0){
				for(Resource child : bundles){
					String varName="";
					String name = child.getFilename().toUpperCase();
					if(name.startsWith("ANONYMOUS")){
						varName = "anonymousBundle";
					}
					if(name.startsWith("APPLICANT")){
						varName = "applicantBundle";
					}
					if(name.startsWith("SECRETARY")){
						varName = "secretaryBundle";
					}
					if(name.startsWith("REVIEW")){
						varName = "reviewBundle";
					}
					if(name.startsWith("MODERATOR")){
						varName = "moderatorBundle";
					}
					if(name.startsWith("HEAD.")){
						varName = "headBundle";
					}
					if(varName.length()>0){
						if(!child.isFile()){
							mv.addObject(varName, "js/"+ child.getFilename());  //production mode from jar
						}else{
							mv.addObject(varName, "js/"+ varName+".js"); //development mode from Eclipse
						}
					}

				}
			}else{ //no scripts is sure Eclipse
				mv.addObject("anonymousBundle", "js/anonymousBundle.js");
				mv.addObject("applicantBundle", "js/applicantBundle.js");
				mv.addObject("secretaryBundle", "js/secretaryBundle.js");
				mv.addObject("reviewBundle", "js/reviewBundle.js");
				mv.addObject("moderatorBundle", "js/moderatorBundle.js");
				mv.addObject("headBundle", "js/headBundle.js");
			}
			
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return mv;
	}



}

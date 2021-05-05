package org.msh.pdex.ipermit.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.msh.pdex.dto.AtcDTO;
import org.msh.pdex.dto.Language;
import org.msh.pdex.i18N.Messages;
import org.msh.pdex.ipermit.dto.ApplicationsDTO;
import org.msh.pdex.ipermit.dto.FlagDTO;
import org.msh.pdex.ipermit.dto.HeaderDTO;
import org.msh.pdex.ipermit.dto.UserDetailsDTO;
import org.msh.pdex.ipermit.dto.UserRoleDto;
import org.msh.pdex.ipermit.services.EntityToDtoService;
import org.msh.pdex.ipermit.services.UserService;
import org.msh.pdex.model.rsecond.Context;
import org.msh.pdex.services.ContextServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;


/**
 * This controller responsible for public domain  REST API
 * @author Alex Kurasoff
 *
 */
@RestController
public class PublicAPI{

	private static final Logger logger = LoggerFactory.getLogger(PublicAPI.class);

	@Autowired
	private Messages messages;

	@Autowired
	UserService userService;

	@Autowired
	private ContextServices contextServices;
	
	@Autowired
	EntityToDtoService entityToDTOServ;
	
	/**
	 * Crerate a context cookie
	 * @param contextId
	 */
	public static Cookie createContextCookie(String contextId){
		Cookie ret = new Cookie(ContextServices.PDEX_CONTEXT, contextId);
		ret.setPath("/");
		return ret;
	}

	/**
	 * Living or died?
	 * @return
	 */
	@RequestMapping(value = "/api/public/ping",  method = {RequestMethod.POST,RequestMethod.GET})
	public String ping(){
		return "OK";
	}


	/**
	 * Provide labels for keys defined in the parameter
	 * For label "locale" always returns a name of the current locale
	 * @param keys
	 * @return
	 */
	@RequestMapping(value="/api/public/provideLabels", method = RequestMethod.POST)
	public Map<String,String> localeProvideLabels(@RequestBody List<String> keys){
		Map<String,String> ret = new HashMap<String, String>();
		for(String key :keys) {
			ret.put(key,messages.get(key));
		}
		ret.put("locale", messages.getLocaleStr());
		return ret;
	}

	/**
	 * Load the emblem (national or logo or...)
	 * @return
	 */
	@RequestMapping(value="api/public/emblem.svg", method = RequestMethod.GET)
	public ResponseEntity<Resource> emblem() {
		Resource res = new ByteArrayResource(messages.loadEmblem().getBytes());
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType("image/svg+xml"))
				.header("filename","emblem.svg")
				.body(res);
	}
	/**
	 * Data for page header - emblem, ministerio, etc
	 * @return
	 */
	@RequestMapping(value="/api/public/header")
	public HeaderDTO header() {
		HeaderDTO ret = new HeaderDTO();
		ret.setWorkspace(messages.getWorkspace());
		for(Language lang :messages.getLanguages().getLangs()) {
			FlagDTO flag = new FlagDTO();
			flag.setDisplayName(lang.getDisplayName());
			flag.setLocaleStr(lang.getLocaleAsString());
			if(flag.getLocaleStr().equalsIgnoreCase(LocaleContextHolder.getLocale().toString())) {
				flag.setSelected(true);
			}
			ret.getFlags().add(flag);
		}
		return ret;
	}

	/**
	 * Get an SVG flag to switch the language
	 * @return
	 */
	@RequestMapping(value="/api/public/flag", method = RequestMethod.GET)
	public ResponseEntity<Resource> flag(@RequestParam String localeStr	){
		Resource res = new ByteArrayResource("".getBytes());
		for(Language lang : messages.getLanguages().getLangs()) {
			if(lang.getLocaleAsString().equalsIgnoreCase(localeStr)) {
				res=new ByteArrayResource(lang.getFlagSVG().getBytes());
			}
		}
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType("image/svg+xml"))
				.header("filename","emblem.svg")
				.body(res);
	}

	/**
	 * try to login/password authentificate
	 * @param user - dto
	 * @return
	 */
	/*@RequestMapping(value="/api/public/checkauth",  method = RequestMethod.POST)
	public UserDetailsDTO authentificate(@RequestBody UserDetailsDTO user) {
		user = userService.authUser(user);
		return user;
	}*/
	/**
	 * Get user's details for just authentificated user
	 * @param user
	 * @return
	 */	
	@RequestMapping(value= {"/api/public/userData"}, method = RequestMethod.POST)
	public UserDetailsDTO userData(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response) {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));

		UserDetailsDTO ret = new UserDetailsDTO();
		if(auth != null && auth.getAuthorities().size()>0) {
			ret = userService.loadUserDetailsDTO(auth.getName());
			ret.setPassword("*****************");

			try {
				Long curRoleID = contextServices.loadCurrentUserRole(context, ContextServices.CURRENTROLE_ID);
				List<GrantedAuthority> allroles = new ArrayList<GrantedAuthority>();
				allroles.addAll(ret.getAuthorities());
				if(!(curRoleID != null && curRoleID > 0)) {
					curRoleID = ((UserRoleDto)allroles.get(0)).getId();
				}
				contextServices.saveCurrentUserRole(context, curRoleID, ContextServices.CURRENTROLE_ID);
				UserRoleDto roleDto = userService.findCurrentRoleDto(allroles, curRoleID);
				if(roleDto != null)
					ret.updateCurrentRole(roleDto, messages);	
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}
	
	/**
	 * Get user's details for just authentificated user
	 * @param user
	 * @return
	 */	
	@RequestMapping(value= {"/api/public/atcs"}, method = RequestMethod.POST)
	public AtcDTO atcs(Authentication auth,
			@CookieValue(ContextServices.PDEX_CONTEXT) Optional<String> contextId,
			HttpServletResponse response,
			@RequestBody AtcDTO data) {
		Context context = contextServices.loadContext(contextId);
		response.addCookie(PublicAPI.createContextCookie(Long.toString(context.getID())));
		
		// reload all list
		data = entityToDTOServ.reloadAtc(data);
		
		return data;
	}

}
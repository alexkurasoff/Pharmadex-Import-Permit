package org.msh.pdex.ipermit;

import org.msh.pdex.ipermit.dto.UserDetailsDTO;
import org.msh.pdex.ipermit.dto.UserRoleDto;
import org.msh.pdex.ipermit.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

@Configuration
@EnableWebSecurity
public class WebSecurity extends WebSecurityConfigurerAdapter {

	private static final String ROLE_IMPORT_RESPONSIBLE = "ROLE_IMPORT_RESPONSIBLE";
	private static final String ROLE_COMPANY = "ROLE_COMPANY";
	private static final String ROLE_SECRETARY = "ROLE_SECRETARY";
	private static final String ROLE_IMPORT_MODERATOR = "ROLE_IMPORT_MODERATOR";
	@Autowired
	UserService userService;

	/**
	 * Configure HTTP security
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
		.csrf().disable()
		.authorizeRequests()
		.antMatchers("/").permitAll()
		.antMatchers("/anonymous").permitAll()
		.antMatchers("/api/public/**").permitAll()
		.antMatchers("/moderator/**").hasAnyAuthority(ROLE_IMPORT_MODERATOR)
		.antMatchers("/api/moderator/**").hasAnyAuthority(ROLE_IMPORT_MODERATOR)
		.antMatchers("/secretary/**").hasAnyAuthority(ROLE_SECRETARY)
		.antMatchers("/api/secretary/**").hasAnyAuthority(ROLE_SECRETARY)
		.antMatchers("/review/**").hasAnyAuthority(ROLE_IMPORT_RESPONSIBLE)
		.antMatchers("/api/review/**").hasAnyAuthority(ROLE_IMPORT_RESPONSIBLE)
		.antMatchers("/applicant/**").hasAnyAuthority(ROLE_COMPANY)
		.antMatchers("/api/applicant/**").hasAnyAuthority(ROLE_COMPANY)
		.antMatchers("/api/common/**").authenticated()
		.antMatchers("/api/notifications/**").authenticated()
		.and()
		.formLogin()
		.loginPage("/anonymous#term,Login,void")
		.loginProcessingUrl("/login")
		.failureUrl("/anonymous/") 
		.and()
		.logout().deleteCookies("JSESSIONID")
		.logoutSuccessUrl("/")
		.and()
		.rememberMe().key("арозаупаланалапуазора")
		.userDetailsService(userService)
		.and()
	    .exceptionHandling().accessDeniedPage("/anonymous#term,Login,void");;
	}

	/**
	 * Fetch user's group (tabset) - applicant, secretary, moderator, etc
	 * @param auth
	 * @return group name as string
	 */
	public static String fetchGroup(Authentication auth) {
		if(auth != null) {
			UserDetailsDTO dto = (UserDetailsDTO)auth.getPrincipal();
			if(dto.getCurrentrole().getValue() != null) {
				String rolename = findCurrentAuth(dto, dto.getCurrentrole().getValue().getId());
				if(rolename == null)
					return "anonymous";
				
				if(rolename.toUpperCase().equals(WebSecurity.ROLE_COMPANY)) {
					return "applicant";
				}
				if(rolename.toUpperCase().equals(WebSecurity.ROLE_IMPORT_MODERATOR)) {
					return "moderator";
				}
				if(rolename.toUpperCase().equals(WebSecurity.ROLE_SECRETARY)) {
					return "secretary";
				}
				if(rolename.toUpperCase().equals(WebSecurity.ROLE_IMPORT_RESPONSIBLE)) {
					return "review";
				}
			}
		}
		return "anonymous";
	}
	/**
	 * Fetch user login name
	 * @param auth
	 * @return
	 */
	public static String fetchUserLogin(Authentication auth) {
		UserDetailsDTO userDetails = (UserDetailsDTO) auth.getPrincipal();
		return userDetails.getUsername();
	}

	public static String findCurrentAuth(UserDetailsDTO userDet, long id) {
		String rolename = null;
		if(userDet != null && userDet.getAuthorities() != null) {
			for(GrantedAuthority ga:userDet.getAuthorities()) {
				long gaID = ((UserRoleDto)ga).getId();
				if(id == gaID){
					rolename = ga.getAuthority();
					break;
				}
			}
		}
		if(rolename==null && userDet.getAuthorities().iterator().hasNext()) {
			rolename=userDet.getAuthorities().iterator().next().getAuthority();
		}
		return rolename;
	}
}

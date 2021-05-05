package org.msh.pdex.ipermit.services;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

import org.msh.pdex.i18N.Messages;
import org.msh.pdex.ipermit.dto.UserDetailsDTO;
import org.msh.pdex.ipermit.dto.UserRoleDto;
import org.msh.pdex.model.User;
import org.msh.pdex.model.rsecond.Context;
import org.msh.pdex.repository.UserRepository;
import org.msh.pdex.services.ContextServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/**
 * Get user details
 * @author Alex Kurasoff
 *
 */
@Service
public class UserService implements UserDetailsService {

	@Autowired
	UserRepository userRepo;

	@Autowired
	PasswordEncoder password;

	@Autowired
	private ContextServices contextServices;
	
	@Autowired
	private Messages messages;
	/**
	 * To fit the Spring Security Interface UserDetails
	 */
	@Override
	@Transactional
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return  loadUserDetailsDTO(username);
	}

	/**
	 * Extended UserDetails for pleasures any other then Spring Auth
	 * @param username
	 * @return
	 */
	@Transactional
	public UserDetailsDTO loadUserDetailsDTO(String username) {
		Optional<User> usero = userRepo.findByUsername(username);
		if (!usero.isPresent()) {
			throw new UsernameNotFoundException(username);
		}
		return UserDetailsDTO.of(usero.get(), messages);
	}
	/**
	 * Check user login and password by the REST API
	 * @param user
	 * @return
	 */
	public UserDetailsDTO authUser(UserDetailsDTO user) {
		Optional<User> usero = userRepo.findByUsername(user.getUsername());
		if(usero.isPresent()) {
			if(password.matches(user.getPassword(), usero.get().getPassword())) {
				user.setMessage("OK");
				user=UserDetailsDTO.of(usero.get(), messages);
			}else {
				user.setMessage("Invalid user name/password");
			}
		}else {
			user.setMessage("User not found : " + user.getUserName());
		}
		user.setPassword("");
		return user;
	}
	
	/**
	 * find UserRole in list userDet.getAuthorities()
	 * @param userDet
	 * @param id
	 * @return
	 */
	public UserRoleDto findCurrentRoleDto(Collection<? extends GrantedAuthority> collection, long id) {
		UserRoleDto role = null;
		if(collection != null) {
			for(GrantedAuthority ga:collection) {
				long gaID = ((UserRoleDto)ga).getId();
				if(id == gaID){
					role = (UserRoleDto)ga;
					break;
				}
			}
		}
		
		return role;
	}
	
	/**
	 * load current role id from Context and update in auth
	 * @param auth
	 * @param context
	 */
	public void updateCurrentRole(Authentication auth, Context context) {
		UserRoleDto current = null;
		if(auth == null)
			return ;
		try {
			Long curRoleID = contextServices.loadCurrentUserRole(context, ContextServices.CURRENTROLE_ID);
			current = findCurrentRoleDto(auth.getAuthorities(), curRoleID);
			if(current != null)
				((UserDetailsDTO)auth.getPrincipal()).updateCurrentRole(current, messages);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

package org.msh.pdex.ipermit.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.msh.pdex.dto.form.AllowValidation;
import org.msh.pdex.dto.form.FormFieldDTO;
import org.msh.pdex.dto.form.OptionDTO;
import org.msh.pdex.i18N.Messages;
import org.msh.pdex.model.Role;
import org.msh.pdex.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserDetailsDTO implements UserDetails{

	
	private static final long serialVersionUID = 473550447149786669L;
	/**
	 * login
	 */
	private String userName = "anonymous";
	private String password = "";
	private String message = "";
	public String fullName = "Anonymous A Anonymous";
	private List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
	private FormFieldDTO<OptionDTO> currentrole = new FormFieldDTO<OptionDTO>(new OptionDTO());
	private boolean multirole = false;
	// the same user
	private String applicantName = "";

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isMultirole() {
		return multirole;
	}

	public void setMultirole(boolean multirole) {
		this.multirole = multirole;
	}

	public void setAuthorities(List<GrantedAuthority> authorities) {
		this.authorities = authorities;
	}

	/**
	 * login
	 */
	@Override
	public String getUsername() {
		return this.userName;
	}

	/**
	 * Accounts cannot be expired
	 */
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getApplicantName() {
		return applicantName;
	}

	public void setApplicantName(String applicantName) {
		this.applicantName = applicantName;
	}

	public FormFieldDTO<OptionDTO> getCurrentrole() {
		return currentrole;
	}

	public void setCurrentrole(FormFieldDTO<OptionDTO> currentrole) {
		this.currentrole = currentrole;
	}

	public void updateCurrentRole(UserRoleDto roledto, Messages messages) {
		this.getCurrentrole().getValue().setId(roledto.getId());
		this.getCurrentrole().getValue().setCode(messages.get(roledto.getAuthority()));
	}
	
	/**
	 * Create a use from
	 * 
	 * @param user
	 * @return
	 */
	public static UserDetailsDTO of(User user, Messages messages) {
		UserDetailsDTO ret = new UserDetailsDTO();
		ret.setUserName(user.getUsername());
		ret.setFullName(user.getName());
		ret.password = user.getPassword();
		for (Role role : user.getRoles()) {
			if (role.isEpermit() || role.getRolename().equalsIgnoreCase("ROLE_COMPANY")) {
				ret.authorities.add(UserRoleDto.of(role));
				ret.getCurrentrole().getValue().getOptions().add(OptionDTO.of(role, messages));
			}
		}
		if(ret.authorities.size() > 1)
			ret.setMultirole(true);
		
		if (user.getApplicant() != null) {
			ret.setApplicantName(user.getApplicant().getAppName());
		}
		return ret;
	}
}

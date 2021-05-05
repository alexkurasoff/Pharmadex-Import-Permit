package org.msh.pdex.ipermit.dto;

import java.util.List;

import org.msh.pdex.dto.form.OptionDTO;
import org.msh.pdex.model.Role;
import org.springframework.security.core.GrantedAuthority;

public class UserRoleDto implements GrantedAuthority {
	private static final long serialVersionUID = -6990177762624983658L;

	public static final String DISCRIMINATOR = "1";
	private static final String DEFAULT_ROLE="ROLE_USER";
	private String authority = DEFAULT_ROLE;

	private long id = 0;
	private String displayname = "";
	
	@Override
	public String getAuthority() {
		return authority.toUpperCase();
	}

	private void setAuthority(String val) {
		this.authority=val;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getDisplayname() {
		return displayname;
	}

	public void setDisplayname(String displayname) {
		this.displayname = displayname;
	}

	public static GrantedAuthority of(Role role) {
		UserRoleDto ret = new UserRoleDto();
		String val = role.getRolename();
		if(val != null){
			val = val.toUpperCase();
			ret.setAuthority(val);
			ret.setId(role.getRoleId());
			ret.setDisplayname(role.getDisplayname());
		}
		return ret;
	}


	public static OptionDTO of(GrantedAuthority custom, List<GrantedAuthority> customs) {
		OptionDTO ret = new OptionDTO();
		if(custom != null) {
			ret.setId(((UserRoleDto)custom).getId());
			ret.setCode(custom.getAuthority());
			ret.setDescription(custom.getAuthority());
		}
		if(customs != null) {
			for(GrantedAuthority cus : customs) {
				OptionDTO dto = new OptionDTO();
				dto.setId(((UserRoleDto)cus).getId());
				dto.setCode(cus.getAuthority());
				dto.setDescription(cus.getAuthority());
				ret.getOptions().add(dto);
			}
		}
		
		return ret;
	}

}

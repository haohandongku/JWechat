package com.dcits.app.security;

import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;

import com.dcits.app.security.UserDetails;

@SuppressWarnings("serial")
public class User implements UserDetails {

	private Collection<GrantedAuthority> authorities;
	private String password;
	private String username;
	/** 账号是否过期 */
	private boolean accountNonExpired;
	/** 账号是否被锁定 */
	private boolean accountNonLocked;
	/** 证书是否过期 */
	private boolean credentialsNonExpired;
	/** 是否可用 */
	private boolean enabled;
	@SuppressWarnings("rawtypes")
	private Map other;

	@Override
	public Collection<GrantedAuthority> getAuthorities() {
		return authorities;
	}

	public void setAuthorities(Collection<GrantedAuthority> authorities) {
		this.authorities = authorities;
	}

	@Override
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return accountNonExpired;
	}

	public void setAccountNonExpired(boolean accountNonExpired) {
		this.accountNonExpired = accountNonExpired;
	}

	@Override
	public boolean isAccountNonLocked() {
		return accountNonLocked;
	}

	public void setAccountNonLocked(boolean accountNonLocked) {
		this.accountNonLocked = accountNonLocked;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return credentialsNonExpired;
	}

	public void setCredentialsNonExpired(boolean credentialsNonExpired) {
		this.credentialsNonExpired = credentialsNonExpired;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@SuppressWarnings("rawtypes")
	public Map getOther() {
		return other;
	}

	@SuppressWarnings("rawtypes")
	public void setOther(Map other) {
		this.other = other;
	}

	@Override
	public int hashCode() {
		return username.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		User user = (User) object;
		return this.username.equals(user.getUsername());
	}

}
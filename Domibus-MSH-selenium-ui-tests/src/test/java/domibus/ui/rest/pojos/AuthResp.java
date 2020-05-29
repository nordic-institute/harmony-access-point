package domibus.ui.rest.pojos;

import java.util.List;


public class AuthResp {

	String username;
	List<String> authorities;
	Boolean defaultPasswordUsed;
	Boolean externalAuthProvider;
	Integer daysTillExpiration;

	@Override
	public String toString() {
		return "AuthResp{" +
				"username='" + username + '\'' +
				", authorities=" + authorities +
				", defaultPasswordUsed=" + defaultPasswordUsed +
				", externalAuthProvider=" + externalAuthProvider +
				", daysTillExpiration=" + daysTillExpiration +
				'}';
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public List<String> getAuthorities() {
		return authorities;
	}

	public void setAuthorities(List<String> authorities) {
		this.authorities = authorities;
	}

	public Boolean getDefaultPasswordUsed() {
		return defaultPasswordUsed;
	}

	public void setDefaultPasswordUsed(Boolean defaultPasswordUsed) {
		this.defaultPasswordUsed = defaultPasswordUsed;
	}

	public Boolean getExternalAuthProvider() {
		return externalAuthProvider;
	}

	public void setExternalAuthProvider(Boolean externalAuthProvider) {
		this.externalAuthProvider = externalAuthProvider;
	}

	public Integer getDaysTillExpiration() {
		return daysTillExpiration;
	}

	public void setDaysTillExpiration(Integer daysTillExpiration) {
		this.daysTillExpiration = daysTillExpiration;
	}
}

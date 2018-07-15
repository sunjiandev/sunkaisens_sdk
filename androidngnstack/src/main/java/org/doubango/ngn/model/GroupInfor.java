package org.doubango.ngn.model;

import java.util.List;

public class GroupInfor {

	private String name;
	private String uri;
	private String displayName;
	private String serviceType;
	private String creator;
	private List<GroupMember> members;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public List<GroupMember> getMembers() {
		return members;
	}

	public void setMembers(List<GroupMember> members) {
		this.members = members;
	}

	public GroupInfor(String name, String uri, String displayName,
			String serviceType, String creator, List<GroupMember> members) {
		super();
		this.name = name;
		this.uri = uri;
		this.displayName = displayName;
		this.serviceType = serviceType;
		this.creator = creator;
		this.members = members;
	}

	public GroupInfor() {
		super();
		// TODO Auto-generated constructor stub
	}

	public static class GroupMember {
		private String uri;
		private String displayName;
		private String userType;
		private String deviceType;

		public String getUri() {
			return uri;
		}

		public void setUri(String uri) {
			this.uri = uri;
		}

		public String getDisplayName() {
			return displayName;
		}

		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}

		public String getUserType() {
			return userType;
		}

		public void setUserType(String userType) {
			this.userType = userType;
		}

		public String getDeviceType() {
			return deviceType;
		}

		public void setDeviceType(String deviceType) {
			this.deviceType = deviceType;
		}

		@Override
		public String toString() {
			return "GroupMember [uri=" + uri + ", displayName=" + displayName
					+ ", userType=" + userType + ", deviceType=" + deviceType
					+ "]";
		}

		public GroupMember(String uri, String displayName, String userType,
				String deviceType) {
			super();
			this.uri = uri;
			this.displayName = displayName;
			this.userType = userType;
			this.deviceType = deviceType;
		}

		public GroupMember() {
			super();
			// TODO Auto-generated constructor stub
		}

	}
}

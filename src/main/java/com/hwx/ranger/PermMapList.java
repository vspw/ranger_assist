package com.hwx.ranger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermMapList {

	private List<String> groupList;
	private List<String> permList;	
	
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();
	
	
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
		}


		public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
		}


		public List<String> getGroupList() {
			return groupList;
		}


		public void setGroupList(List<String> groupList) {
			this.groupList = groupList;
		}


		public List<String> getPermList() {
			return permList;
		}


		public void setPermList(List<String> permList) {
			this.permList = permList;
		}
		
		
}

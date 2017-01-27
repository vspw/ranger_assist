package com.hwx.ranger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class HDFSCheckList {





private String depth;
private String description;
private String path;
private String resourceName;
private String repositoryName;
private boolean isEnabled;
private List<PolicyItem> policyItems;
private String allowRangerPathDelete;


private Map<String, Object> additionalProperties = new HashMap<String, Object>();





public Map<String, Object> getAdditionalProperties() {
return this.additionalProperties;
}


public void setAdditionalProperty(String name, Object value) {
this.additionalProperties.put(name, value);
}


public String getCheckname() {
	return depth;
}


public void setCheckname(String checkname) {
	this.depth = checkname;
}


public String getDescription() {
	return description;
}


public void setDescription(String description) {
	this.description = description;
}


public String getPath() {
	return path;
}


public void setPath(String path) {
	this.path = path;
}


public String getResourceName() {
	return resourceName;
}


public void setResourceName(String resourceName) {
	this.resourceName = resourceName;
}


public List<PolicyItem> getPolicyItemList() {
	return policyItems;
}


public void setPolicyItemList(List<PolicyItem> policyItemList) {
	this.policyItems = policyItemList;
}


public String getRepositoryName() {
	return repositoryName;
}


public void setRepositoryName(String repositoryName) {
	this.repositoryName = repositoryName;
}


public String getDepth() {
	return depth;
}


public void setDepth(String depth) {
	this.depth = depth;
}


/* (non-Javadoc)
 * @see java.lang.Object#toString()
 */
@Override
public String toString() {
	return getClass().getName() + " {\n\tdepth: " + depth + "\n\tdescription: "
			+ description + "\n\tpath: " + path + "\n\tresourceName: "
			+ resourceName + "\n\trepositoryName: " + repositoryName
			+ "\n\tpolicyItemList: " + policyItems + "\n\tallowRangerPathDelete: " + allowRangerPathDelete
			+ "\n\tadditionalProperties: " + additionalProperties + "\n}";
}



public String getAllowRangerPathDelete() {
	return allowRangerPathDelete;
}


public void setAllowRangerPathDelete(String allowRangerPathDelete) {
	this.allowRangerPathDelete = allowRangerPathDelete;
}


public boolean isEnabled() {
	return isEnabled;
}


public void setEnabled(boolean isEnabled) {
	this.isEnabled = isEnabled;
}


}
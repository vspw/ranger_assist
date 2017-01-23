package com.hwx.ranger;

import java.util.List;

public class PolicyItem {

private List<Access> accesses = null;
private List<Object> users = null;
private List<String> groups = null;
private List<Object> conditions = null;
private boolean delegateAdmin;

public List<Access> getAccesses() {
return accesses;
}

public void setAccesses(List<Access> accesses) {
this.accesses = accesses;
}

public List<Object> getUsers() {
return users;
}

public void setUsers(List<Object> users) {
this.users = users;
}

public List<String> getGroups() {
return groups;
}

public void setGroups(List<String> groups) {
this.groups = groups;
}

public List<Object> getConditions() {
return conditions;
}

public void setConditions(List<Object> conditions) {
this.conditions = conditions;
}

public boolean isDelegateAdmin() {
return delegateAdmin;
}

public void setDelegateAdmin(boolean delegateAdmin) {
this.delegateAdmin = delegateAdmin;
}

}
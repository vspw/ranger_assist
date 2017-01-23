package com.hwx.ranger;


public class FileStatus {


private Integer accessTime;

private Integer blockSize;

private Integer childrenNum;

private Integer fileId;

private String group;

private Integer length;

private Long modificationTime;

private String owner;

private String pathSuffix;

private String permission;

private Integer replication;

private Integer storagePolicy;

private String type;

public Integer getAccessTime() {
return accessTime;
}

public void setAccessTime(Integer accessTime) {
this.accessTime = accessTime;
}

public Integer getBlockSize() {
return blockSize;
}

public void setBlockSize(Integer blockSize) {
this.blockSize = blockSize;
}

public Integer getChildrenNum() {
return childrenNum;
}

public void setChildrenNum(Integer childrenNum) {
this.childrenNum = childrenNum;
}

public Integer getFileId() {
return fileId;
}

public void setFileId(Integer fileId) {
this.fileId = fileId;
}

public String getGroup() {
return group;
}

public void setGroup(String group) {
this.group = group;
}

public Integer getLength() {
return length;
}

public void setLength(Integer length) {
this.length = length;
}

public Long getModificationTime() {
return modificationTime;
}

public void setModificationTime(Long modificationTime) {
this.modificationTime = modificationTime;
}

public String getOwner() {
return owner;
}

public void setOwner(String owner) {
this.owner = owner;
}

public String getPathSuffix() {
return pathSuffix;
}

public void setPathSuffix(String pathSuffix) {
this.pathSuffix = pathSuffix;
}

public String getPermission() {
return permission;
}

public void setPermission(String permission) {
this.permission = permission;
}

public Integer getReplication() {
return replication;
}

public void setReplication(Integer replication) {
this.replication = replication;
}

public Integer getStoragePolicy() {
return storagePolicy;
}

public void setStoragePolicy(Integer storagePolicy) {
this.storagePolicy = storagePolicy;
}

public String getType() {
return type;
}

public void setType(String type) {
this.type = type;
}

/* (non-Javadoc)
 * @see java.lang.Object#toString()
 */
@Override
public String toString() {
	return getClass().getName() + " {\n\taccessTime: " + accessTime
			+ "\n\tblockSize: " + blockSize + "\n\tchildrenNum: " + childrenNum
			+ "\n\tfileId: " + fileId + "\n\tgroup: " + group + "\n\tlength: "
			+ length + "\n\tmodificationTime: " + modificationTime
			+ "\n\towner: " + owner + "\n\tpathSuffix: " + pathSuffix
			+ "\n\tpermission: " + permission + "\n\treplication: "
			+ replication + "\n\tstoragePolicy: " + storagePolicy
			+ "\n\ttype: " + type + "\n}";
}

}
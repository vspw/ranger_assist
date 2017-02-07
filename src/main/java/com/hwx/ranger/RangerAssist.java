package com.hwx.ranger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;

import org.apache.hadoop.security.authentication.client.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

class RangerAssist extends TimerTask {
	WebHDFSConnection conn = null;
	RangerConnection rconn=null;
	Response objInput=null;
	protected static final Logger logger = LoggerFactory.getLogger(RangerAssist.class);

	public RangerAssist(Response objInput) {
		this.objInput=objInput;
	}

	@Override
	public void run() {
		try {
			logger.info("Establishing Connection to HDFS");
			this.conn = this.connectSecure(objInput);

			logger.info("Establishing Connection to Ranger");
			this.rconn=this.connectr(objInput);

			logger.info("Iterating the input HDFS policy checklist");
			Iterator<HDFSCheckList> iteratorHDFSCheckList= objInput.getHdfschecklist().iterator();
			logger.info("ForEach Input HDFS Path");

			while(iteratorHDFSCheckList.hasNext())
			{

				logger.info("#############################################" );
				HDFSCheckList objInputHDFSItem= iteratorHDFSCheckList.next();
				ArrayList<String> listHdfsDepthPaths= new ArrayList<String>();
				List<String> listInputPaths=objInputHDFSItem.getPaths();
				//String strInputPath=objInputHDFSItem.getPaths();
				Iterator<String> iteratorInputPaths = listInputPaths.iterator();

				while(iteratorInputPaths.hasNext())
				{
					String strInputPath=iteratorInputPaths.next();
					logger.info("###INPUT PATH:"+strInputPath+"###" );
					logger.info("---Get list of paths in HDFS based on the depth from input");

					//This does not matter if depth is 0, since its the provided directory only
					logger.info("---HDFS listStatus for the given Depth, retrieving list of hdfs-depth-paths");
					this.listStatusForDepth(strInputPath,Integer.parseInt(objInputHDFSItem.getDepth()),listHdfsDepthPaths);

				}

				//Get policy in Ranger using the resource_name from input
				//0. Get all policies and check for the input policy name.
				//1. Get Policy by service-name and policy-name
				//2. Update Policy by service-name and policy-name
				logger.info("---Get All Existing Ranger Polices");
				String strAllPolicies=this.getAllRepositoryPolicies();
				logger.debug(strAllPolicies);
				List<RangerPolicyResponse> objRangerPolicies=new JsonUtils().parseRangerPolicies(strAllPolicies);
				logger.debug(objRangerPolicies.iterator().next().getName());

				logger.info("---Check if we need a NEW ranger policy");
				Iterator<RangerPolicyResponse> iteratorObjRangerPolicies = objRangerPolicies.iterator();
				boolean policyFoundInRanger=false;
				// check if its a new Ranger Policy
				while(iteratorObjRangerPolicies.hasNext())
				{
					RangerPolicyResponse objRangerPolicy=iteratorObjRangerPolicies.next();
					if(objRangerPolicy.getName().equals(objInputHDFSItem.getResourceName()))
					{
						logger.info("-----Ranger Policy with InputResourceName: "+objInputHDFSItem.getResourceName()+" found");
						policyFoundInRanger=true;
						break; 
					}
				}// end of check on a new Ranger Policy

				//If policy is not found create a new policy and break
				if (policyFoundInRanger==false)
				{
					logger.info("---Current HDFS Checklist Policy: "+objInputHDFSItem.getResourceName()+" is NOT found in Ranger. CREATING a new Ranger Policy");
					//curl -iv -u admin:admin -d @createPolicy.json -H "Content-type:application/json" -X POST http://zulu.hdp.com:6080/service/public/v2/api/policy/
					RangerPolicyResponse objNewRangerPolicy= new RangerPolicyResponse();
					boolean boolNewIsAuditEnabled=true;
					objNewRangerPolicy.setName(objInputHDFSItem.getResourceName());
					objNewRangerPolicy.setService(objInputHDFSItem.getRepositoryName());
					objNewRangerPolicy.setIsAuditEnabled(boolNewIsAuditEnabled);
					objNewRangerPolicy.setIsEnabled(objInputHDFSItem.isEnabled());

					//PolicyItems are copied from the HDFS input list
					objNewRangerPolicy.setPolicyItems(objInputHDFSItem.getPolicyItemList());

					Resources objNewResources=new Resources();
					objNewRangerPolicy.setResources(objNewResources);

					Path objNewPath= new Path();
					objNewPath.setIsExcludes(false);
					objNewPath.setIsRecursive(objInputHDFSItem.isRecursive());
					objNewPath.setValues(listHdfsDepthPaths);
					objNewRangerPolicy.getResources().setPath(objNewPath);
					this.createPolicy(objNewRangerPolicy); 
					//created a policy for this inputCheckList item. Now go to the next element.
					continue;
					//delete unused paths is ignored while policy creation.. should take effect on existing policies.
				}

				logger.info("---Policy is Found in Ranger. Continue and edit this.");
				logger.info("---Get Ranger Policy by service-name and policy-name");
				String strRangerPolicyName=objInputHDFSItem.getResourceName();
				String strRangerPolicy =this.getRangerPolicyByName(strRangerPolicyName);
				RangerPolicyResponse objRangerPol=new JsonUtils().parseRangerPolicy(strRangerPolicy);
				ArrayList<String> listRangerPolPaths= new ArrayList<String>();
				listRangerPolPaths=objRangerPol.getResources().getPath().getValues();
				logger.info("---ForEach hdfs-depth-path");
				for (String strHdfsDepthPath:listHdfsDepthPaths)
				{
					logger.info("-----Search ranger-policy-paths for hdfs-depth-path");
					if(listRangerPolPaths.contains(strHdfsDepthPath))
					{
						//path found
						logger.info("*****DEPTH PATH:<"+strHdfsDepthPath+"> FOUND! in Ranger Policy*****");

					}
					else
					{
						//path not found
						logger.info("*****DEPTH PATH:<"+strHdfsDepthPath+"> NOT FOUND in Ranger Policy*****");
						logger.info("-----Add this depth path and Update Ranger Policy");
						this.addAndUpdatePolicy(objInputHDFSItem,strHdfsDepthPath,objRangerPol);
					}

				}
				//Delete path from Ranger if allowRangerPathDelete is true and 
				//Ranger policy has a path not present in HDFS (nothing to do with Depth paths).
				//TODO: Check to find a way to better organize delete. Maybe functionalize add and delete.
				logger.info("### TRY DELETING UNUSED PATHS IN POLICY: "+objInputHDFSItem.getResourceName()+" : "+ objInputHDFSItem.getAllowRangerPathDelete()+ "###" );
				if(objInputHDFSItem.getAllowRangerPathDelete().equalsIgnoreCase("true"))
				{
					//refresh the Policy as new paths may have got added
					objRangerPol=new JsonUtils().parseRangerPolicy(this.getRangerPolicyByName(strRangerPolicyName));
					listRangerPolPaths=objRangerPol.getResources().getPath().getValues();

					logger.info("---ForEach Ranger-Policy-Path");
					Iterator<String> iteratorListRangerPolPaths = listRangerPolPaths.iterator();
					while(iteratorListRangerPolPaths.hasNext())
					{
						if(listRangerPolPaths.size()==1)
						{
							logger.warn("-----Last Path in Policy: Unable to delete Path from Ranger Policy.");
							iteratorListRangerPolPaths.next();
						}
						else
						{
							String strRangerPolicyPath=iteratorListRangerPolPaths.next();
							logger.info("-----Get HDFS Content Summary to check if the Ranger-Policy-Path exists: "+strRangerPolicyPath);
							try
							{
								logger.info(this.conn.getContentSummary(strRangerPolicyPath.replaceFirst("/", "")));
							}
							catch(FileNotFoundException fe)
							{
								logger.info("-----HDFS path NOT FOUND!!:"+strRangerPolicyPath);
								logger.info("-----Removing the Path from the this Ranger Policy List");
								iteratorListRangerPolPaths.remove();
								logger.info("-----Updating Ranger Policy with the updated List");
								this.rconn.updatePolicyByName(objRangerPol.getName(), new Gson().toJson(objRangerPol));
								//fe.printStackTrace();

							}
						}
					}

				}//VERIFY HDFS PATH & DELETE

			}//FOR EACH INPUT HDFS PATH


		} catch(FileNotFoundException e)
		{
			logger.error(e.getMessage());
			logger.error("Verify your URL for HDFS, path is not present");
			e.printStackTrace();
		}
		catch (Exception e)
		{	
			e.printStackTrace();
		}


	}
	//recursiveList("/tenant",1)
	private void listStatusForDepth(String strPath,int intDepth, ArrayList<String> listPaths) throws Exception {

		String strHdfsLsContent=null;
		try
		{
			strHdfsLsContent=new JsonUtils().prettyPrint(conn.listStatus(strPath));
		}
		catch(FileNotFoundException fe)
		{
			logger.warn("HDFS path NOT FOUND!!:"+strPath+" Move on to the next input path");
			return;
			//fe.printStackTrace();
		}
		HDFSListStatusResponse objHdfsLs=new JsonUtils().parseHDFSList(strHdfsLsContent);
		Iterator<FileStatus> iteratorFileStatus=objHdfsLs.getFileStatuses().getFileStatus().iterator();

		//iterate for all the Files returned inside the list status (Might not be needed for depth 0)
		//For every FileStatus Returned- based on depth, iterate for paths
		while(iteratorFileStatus.hasNext())
		{
			FileStatus objFileStatus=iteratorFileStatus.next();
			//depth 0 is for root level of depth verification only. E.g: For input paths /source or /base/test etc.
			//the list should have only /source or /base/test respectively
			if (intDepth==0)
			{
				logger.debug(intDepth+"::"+conn.getFileStatus(strPath));
				String strHDFSRootPathStatus=new JsonUtils().prettyPrint(conn.getFileStatus(strPath));
				FileStatusResponse objRootStatus=new JsonUtils().parseHDFSFileStatus(strHDFSRootPathStatus);
				if(objRootStatus.getFileStatus().getType().equals("DIRECTORY"))
				{
					logger.debug("/"+strPath+objRootStatus.getFileStatus().getPathSuffix());
					listPaths.add("/"+strPath+objRootStatus.getFileStatus().getPathSuffix());
					//break from the loop. Don't want that dir to be added n times
					break;
				}
			}
			//depth 1 is for first level of verification only. E.g: For input paths /source or /base/test etc.
			//the list should have only /source/A, /source/B or /base/test/case1, //base/test/case2, /base/test/case3 respectively
			//The reason depth 0 and depth 1 are two different conditions is because of the way we get the directory contents
			else if (intDepth==1)
			{
				logger.debug(intDepth+"::"+conn.listStatus(strPath));
				if(objFileStatus.getType().equals("DIRECTORY"))
				{
					logger.debug("/"+strPath+"/"+objFileStatus.getPathSuffix());
					listPaths.add("/"+strPath+"/"+objFileStatus.getPathSuffix());
				}
				//return;
				//System.out.println(objHdfsLs.getFileStatuses().getFileStatus().iterator().next().getPathSuffix());
			}
			//This is for Nth level of verification only. We recursively call this function until we reach the required level
			else
			{
				//FileStatus item= iteratorFileStatus.next();
				//int adepth=intDepth-1;
				if(objFileStatus.getType().equals("DIRECTORY"))
				{
					listStatusForDepth(strPath+"/"+objFileStatus.getPathSuffix(), intDepth-1,listPaths);
				}
			}
		}

	}

	private WebHDFSConnection connect(Response objInput) throws Exception {

		conn = new PseudoWebHDFSConnection(objInput.getEnvDetails().getHdfsURI(), objInput.getEnvDetails().getOpUsername().split("@")[0], objInput.getEnvDetails().getOpPassword());
		return conn;

	}
	private RangerConnection connectr(Response objInput) throws Exception {

		rconn = new BasicAuthRangerConnection(objInput.getEnvDetails().getRangerURI(), objInput.getEnvDetails().getOpUsername().split("@")[0], objInput.getEnvDetails().getOpPassword(), objInput.getHdfschecklist().iterator().next().getRepositoryName());
		return rconn;

	}

	private WebHDFSConnection connectSecure(Response objInput) throws Exception {

		conn = new KerberosWebHDFSConnection(objInput.getEnvDetails().getHdfsURI(), objInput.getEnvDetails().getOpUsername(), objInput.getEnvDetails().getOpPassword());
		return conn;

	}


	private void listPolicyById(String id) throws MalformedURLException, IOException, AuthenticationException {
		String jsonResp = rconn.getPolicybyId(id);
		JsonElement jelement = new JsonParser().parse(jsonResp);
		logger.info("Result: "+ jsonResp);
	}

	private void getPolicyByName(String policyName) throws MalformedURLException, IOException, AuthenticationException {
		//String jsonResp = rconn.getPolicyByName(policyName);
		logger.info(new JsonUtils().prettyPrint(rconn.getPolicyByName(policyName)));
	}
	private void open(String path) throws MalformedURLException, IOException, AuthenticationException {
		FileOutputStream os = new  FileOutputStream(new File("/tmp/downloadfromhdfs.file"));
		String json = conn.open(path, os);
		logger.info(json);
	}


	private void create(String path) throws MalformedURLException, IOException, AuthenticationException {
		FileInputStream is = new FileInputStream(new File("/tmp/downloadfromhdfs.file"));
		String json = conn.create(path, is);
		logger.info(json);
	}


	private void delete(String path) throws MalformedURLException, IOException, AuthenticationException {
		String json = conn.delete(path);
		logger.info(json);
	}
	private void findPathInRepository(String path, String repo) throws MalformedURLException, IOException, AuthenticationException {
		logger.debug(new JsonUtils().prettyPrint(rconn.getAllRepositoryPolicies()));
		rconn.getAllRepositoryPolicies();

	}
	private  HashMap<String, List<String>> listAllInputPaths(Response objInput) throws MalformedURLException, IOException, AuthenticationException {
		HashMap<String, List<String>> map = new HashMap<String, List<String>>();

		while(objInput.getHdfschecklist().iterator().hasNext())
		{
			//	listStatus(objInput.getHdfschecklist().iterator().next().getPath();
		}
		return map;

	}
	private String getRangerPolicyByName(String strRangerPolicyName) throws MalformedURLException, IOException, AuthenticationException
	{
		String strRangerPolicyContent=new JsonUtils().prettyPrint(rconn.getPolicyByName(strRangerPolicyName));
		return strRangerPolicyContent;

	}
	private String getAllRepositoryPolicies() throws MalformedURLException, IOException, AuthenticationException
	{
		String strRangerPoliciesContent=new JsonUtils().prettyPrint(rconn.getAllRepositoryPolicies());
		return strRangerPoliciesContent;

	}
	private void createPolicy(RangerPolicyResponse objRangerPol) throws MalformedURLException, IOException, AuthenticationException
	{
		//print the current state of policy
		Gson gson = new Gson();
		rconn.createPolicy(gson.toJson(objRangerPol));
	}
	private void addAndUpdatePolicy(HDFSCheckList objInputHDFSItem,String path, RangerPolicyResponse objRangerPol) throws MalformedURLException, IOException, AuthenticationException
	{
		//print the current state of policy
		Gson gson = new Gson();
		logger.info("addAndUpdatePolicy: Adding current input path to a Ranger Policy");
		logger.debug("addAndUpdatePolicy: inputPath: "+path);
		objRangerPol.getResources().getPath().getValues().add(path);
		//iterate acl list in objRangerPol vs acl in objInputHDFSItem
		Iterator<PolicyItem> iteratorInputPolicyItemList = objInputHDFSItem.getPolicyItemList().iterator();
		Iterator<PolicyItem> iteratorRangerPolicyItemList =null;
		logger.info("addAndUpdatePolicy: Iterate over the Input Policy ACL list");
		while(iteratorInputPolicyItemList.hasNext())
		{
			PolicyItem objInputPolicyItem = iteratorInputPolicyItemList.next();
			boolean boolInputPolicyFound=false;
			//iterate over the Ranger Policy ACL list
			iteratorRangerPolicyItemList = objRangerPol.getPolicyItems().iterator();
			logger.info("---addAndUpdatePolicy: Iterate over the Input Policy ACL list");
			while(iteratorRangerPolicyItemList.hasNext())
			{
				logger.debug("---addAndUpdatePolicy: "+gson.toJson(objInputPolicyItem.toString()));
				PolicyItem objRangerPolicyItem= iteratorRangerPolicyItemList.next();
				// Compare InputPolicy ACL list with Ranger ACL list
				// if RangerPolicy Contains an ACL with ALL users/groups in InputPolicy.. then its ignores the InputPolicyItem
				//   even if the RangerPolicy ACL has additional groups/users
				//   w.r.t "access" in PolicyItem. Order is important.
				if(objRangerPolicyItem.equals(objInputPolicyItem))
				{
					logger.info("-----addAndUpdatePolicy: Ranger policy found");
					boolInputPolicyFound=true;
					break;
				}
				else
				{
					logger.info("-----addAndUpdatePolicy: Ranger policy NOT found");
					boolInputPolicyFound=false;
				}
			}
			//add inputPolicyItem if its not found in any current ranger policies
			if(boolInputPolicyFound==false)
			{
				logger.debug("---addAndUpdatePolicy: Add inputPolicyItem if its not found in any current ranger policies");
				objRangerPol.getPolicyItems().add(objInputPolicyItem);
			}
		}


		logger.info("addAndUpdatePolicy: Update Ranger Policy :"+objRangerPol.getName());
		logger.debug("addAndUpdatePolicy: "+gson.toJson(objRangerPol));
		rconn.updatePolicyByName(objRangerPol.getName(), gson.toJson(objRangerPol));

	}


}

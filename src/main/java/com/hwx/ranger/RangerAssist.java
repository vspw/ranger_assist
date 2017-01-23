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
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.hadoop.security.authentication.client.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;


public class RangerAssist extends TimerTask {
	WebHDFSConnection conn = null;
	RangerConnection rconn=null;
	protected static final Logger logger = LoggerFactory.getLogger(RangerAssist.class);


	public static void main(String[] args) {

		Timer timer = new Timer();
		timer.schedule(new RangerAssist(), 0, 10000);

	}
	//recursiveList("/tenant",1)
	private void listStatusForDepth(String strPath,int intDepth, ArrayList<String> listPaths) throws Exception {

		String strHdfsLsContent=new JsonUtils().prettyPrint(conn.listStatus(strPath));
		HDFSListStatusResponse objHdfsLs=new JsonUtils().parseHDFSList(strHdfsLsContent);
		Iterator<FileStatus> iteratorFileStatus=objHdfsLs.getFileStatuses().getFileStatus().iterator();

		/*		if (intDepth==0)
		{
			System.out.println(intDepth+"::"+conn.listStatus(strPath));
			//HDFSListStatusResponse objHdfsLs=new JsonUtils().parseHDFSList(new JsonUtils().prettyPrint(conn.listStatus(strPath)));
			//Iterator<FileStatus> iteratorFileStatus = objHdfsLs.getFileStatuses().getFileStatus().iterator();
			while(iteratorFileStatus.hasNext())
			{
				FileStatus objFileStatus=iteratorFileStatus.next();
				if(objFileStatus.getType().equals("DIRECTORY"))
				{
				System.out.println("/"+strPath+"/"+objFileStatus.getPathSuffix());
				listPaths.add("/"+strPath+"/"+objFileStatus.getPathSuffix());
				}
			}
			//System.out.println(objHdfsLs.getFileStatuses().getFileStatus().iterator().next().getPathSuffix());
		}*/

		while(iteratorFileStatus.hasNext())
		{
			FileStatus objFileStatus=iteratorFileStatus.next();
			if (intDepth==0)
			{
				System.out.println(intDepth+"::"+conn.listStatus(strPath));


				if(objFileStatus.getType().equals("DIRECTORY"))
				{
					System.out.println("/"+strPath+"/"+objFileStatus.getPathSuffix());
					listPaths.add("/"+strPath+"/"+objFileStatus.getPathSuffix());
				}

				//System.out.println(objHdfsLs.getFileStatuses().getFileStatus().iterator().next().getPathSuffix());
			}
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

		//return (n<2) ? 1 : n*factorial(n-1);
		//return res;

	}

	private WebHDFSConnection connect(Response objInput) throws Exception {

		conn = new PseudoWebHDFSConnection(objInput.getEnvDetails().getHdfsURI(), objInput.getEnvDetails().getOpUsername(), objInput.getEnvDetails().getOpPassword());
		return conn;

	}
	private RangerConnection connectr(Response objInput) throws Exception {

		rconn = new BasicAuthRangerConnection(objInput.getEnvDetails().getRangerURI(), objInput.getEnvDetails().getOpUsername(), objInput.getEnvDetails().getOpPassword(), objInput.getHdfschecklist().iterator().next().getRepositoryName());
		return rconn;

	}

	private WebHDFSConnection connectSecure(Response objInput) throws Exception {

		conn = new KerberosWebHDFSConnection(objInput.getEnvDetails().getHdfsURI(), objInput.getEnvDetails().getOpUsername(), objInput.getEnvDetails().getOpPassword());
		return conn;

	}


	private void listPolicyById(String id) throws MalformedURLException, IOException, AuthenticationException {
		String jsonResp = rconn.getPolicybyId(id);
		JsonElement jelement = new JsonParser().parse(jsonResp);
		System.out.println("Result: "+ jsonResp);
	}

	private void getPolicyByName(String policyName) throws MalformedURLException, IOException, AuthenticationException {
		//String jsonResp = rconn.getPolicyByName(policyName);
		System.out.println(new JsonUtils().prettyPrint(rconn.getPolicyByName(policyName)));
	}
	private void open(String path) throws MalformedURLException, IOException, AuthenticationException {
		FileOutputStream os = new  FileOutputStream(new File("/tmp/downloadfromhdfs.file"));
		String json = conn.open(path, os);
		System.out.println(json);
	}


	private void create(String path) throws MalformedURLException, IOException, AuthenticationException {
		FileInputStream is = new FileInputStream(new File("/tmp/downloadfromhdfs.file"));
		String json = conn.create(path, is);
		System.out.println(json);
	}


	private void delete(String path) throws MalformedURLException, IOException, AuthenticationException {
		String json = conn.delete(path);
		System.out.println(json);
	}
	private void findPathInRepository(String path, String repo) throws MalformedURLException, IOException, AuthenticationException {
		System.out.println(new JsonUtils().prettyPrint(rconn.getAllRepositoryPolicies(repo)));
		rconn.getAllRepositoryPolicies(repo);

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
	private void addAndUpdatePolicy(String path, RangerPolicyResponse objRangerPol) throws MalformedURLException, IOException, AuthenticationException
	{
		//print the current state of policy
		Gson gson = new Gson();
		objRangerPol.getResources().getPath().getValues().add(path);
		//System.out.println(gson.toJson(objRangerPol));
		rconn.updatePolicyByName(objRangerPol.getName(), gson.toJson(objRangerPol));

	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		RangerAssist objAssist=new RangerAssist();
		JsonUtils objJutils = new JsonUtils();
		try {
			logger.info("###############LETS BEGIN###############");
			logger.info("Make sure the input file nyl_ranger_policy_input.json is available with all necessary details");
			Scanner objScanner=new Scanner(new File("nyl_ranger_policy_input.json"));
			String strJsonInput = objScanner.useDelimiter("\\Z").next();
			objScanner.close();
			logger.info("Parsing the json input file");
			Response objInput = objJutils.parseJSON(strJsonInput);
			logger.info("fromInputJson->Environment: "+ objInput.getEnvDetails());

			logger.info("Establishing Connection to HDFS");
			objAssist.conn = objAssist.connect(objInput);

			logger.info("Establishing Connection to Ranger");
			objAssist.rconn=objAssist.connectr(objInput);

			logger.info("Iterating the input HDFS policy checklist");
			Iterator<HDFSCheckList> iteratorHDFSCheckList= objInput.getHdfschecklist().iterator();
			logger.info("ForEach Input HDFS Path");
			while(iteratorHDFSCheckList.hasNext())
			{

				logger.info("#############################################" );
				HDFSCheckList objInputHDFSItem= iteratorHDFSCheckList.next();
				String strInputPath=objInputHDFSItem.getPath();
				logger.info("###INPUT PATH:"+strInputPath+"###" );
				logger.info("---Get list of paths in HDFS based on the depth from input");

				ArrayList<String> listHdfsDepthPaths= new ArrayList<String>();
				logger.info("---HDFS listStatus for the given Depth, retrieving list of hdfs-depth-paths");
				objAssist.listStatusForDepth(strInputPath,Integer.parseInt(objInputHDFSItem.getDepth()),listHdfsDepthPaths);


				//Get policy in Ranger using the resource_name from input
				//1. Get Policy by service-name and policy-name
				//2. Update Policy by service-name and policy-name
				logger.info("---Get Ranger Policy by service-name and policy-name");
				String strRangerPolicyName=objInputHDFSItem.getResourceName();
				RangerPolicyResponse objRangerPol=new JsonUtils().parseRangerPolicy(objAssist.getRangerPolicyByName(strRangerPolicyName));
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
						objAssist.addAndUpdatePolicy(strHdfsDepthPath,objRangerPol);
					}

				}
				//Delete path from Ranger if allowRangerPathDelete is true and 
				//Ranger policy has a path not present in HDFS (nothing to do with Depth paths).
				//TODO: Check to find a way to better organize delete. Maybe functionalize add and delete.
				logger.info("### TRY DELETING UNUSED PATHS IN POLICY: "+objInputHDFSItem.getResourceName()+" : "+ objInputHDFSItem.getAllowRangerPathDelete()+ "###" );
				if(objInputHDFSItem.getAllowRangerPathDelete().equalsIgnoreCase("true"))
				{
					//refresh the Policy as new paths may have got added
					objRangerPol=new JsonUtils().parseRangerPolicy(objAssist.getRangerPolicyByName(strRangerPolicyName));
					listRangerPolPaths=objRangerPol.getResources().getPath().getValues();
					logger.info("---ForEach Ranger-Policy-Path");
					for(Iterator<String> iteratorListRangerPolPaths = listRangerPolPaths.iterator();iteratorListRangerPolPaths.hasNext(); )
					{
						if(listRangerPolPaths.size()==1)
						{
							logger.warn("-----Last Path in Policy: Unable to delete Path from Ranger Policy.");
						}
						else
						{
							String strRangerPolicyPath=iteratorListRangerPolPaths.next();
							logger.info("-----Get HDFS Content Summary to check if the Ranger-Policy-Path exists: "+strRangerPolicyPath);
							try
							{
								logger.info(objAssist.conn.getContentSummary(strRangerPolicyPath.replaceFirst("/", "")));
							}
							catch(FileNotFoundException fe)
							{
								logger.info("-----HDFS path NOT FOUND!!:"+strRangerPolicyPath);
								logger.info("-----Removing the Path from the this Ranger Policy List");
								iteratorListRangerPolPaths.remove();
								logger.info("-----Updating Ranger Policy with the updated List");
								objAssist.rconn.updatePolicyByName(objRangerPol.getName(), new Gson().toJson(objRangerPol));
								//fe.printStackTrace();

							}
						}
					}

				}//VERIFY HDFS PATH & DELETE

			}//FOR EACH INPUT HDFS PATH


		} catch(FileNotFoundException e)
		{
			System.out.println(e.getMessage());
			System.out.println("Verify your URL for HDFS, path is not present");
			e.printStackTrace();
		}
		catch (Exception e)
		{	
			e.printStackTrace();
		}


	}



}

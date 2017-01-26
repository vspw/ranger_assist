package com.hwx.ranger;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.hadoop.security.authentication.client.AuthenticationException;

/**
 * 
===== HTTP GET <br/>
<li>GETPOLICYBYNAME 
<li>GETPOLICYBYID 
<li>GETPOLICIESBYSEARCH 
<br/>
===== HTTP PUT <br/>
<li>UPDATEPOLICY
<br/>
===== HTTP POST <br/>
CREATEPOLICYBYID 
<br/>


 */
public interface RangerConnection {
	
	
	
/*
 * ========================================================================
 * GET	
 * ========================================================================
 */
	/**
	 * <b>GETHOMEDIRECTORY</b>
	 * 
	 * curl -i "http://<HOST>:<PORT>/webhdfs/v1/?op=GETHOMEDIRECTORY"
	 * 
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws AuthenticationException
	 */
	public String getPolicyByName(String policyName) throws MalformedURLException, IOException, AuthenticationException ;
	
	/**
	 * <b>OPEN</b>
	 * 
	 * curl -i -L "http://<HOST>:<PORT>/webhdfs/v1/<PATH>?op=OPEN
	                    [&offset=<LONG>][&length=<LONG>][&buffersize=<INT>]"
	 * @param path
	 * @param os
	 * @throws AuthenticationException 
	 * @throws IOException 
	 * @throws MalformedURLException 
	 */
	public  String getPolicybyId(String id) throws MalformedURLException, IOException, AuthenticationException ;
	
	/**
	 * <b>GETCONTENTSUMMARY</b>
	 * 
	 * curl -i "http://<HOST>:<PORT>/webhdfs/v1/<PATH>?op=GETCONTENTSUMMARY"
	 * 
	 * @param repo
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws AuthenticationException
	 */
	public  String getAllRepositoryPolicies() throws MalformedURLException, IOException, AuthenticationException ;
	
	
	
	/*
	 * ========================================================================
	 * POST
	 * ========================================================================	
	 */
	/**
	 * <b>CREATE</b>
	 * 
	 * curl -i -X PUT "http://<HOST>:<PORT>service/public/v2/api/policy"
	 * @param path
	 * @param is
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws AuthenticationException
	 */
	public String createPolicy(String jsonContent) throws MalformedURLException, IOException, AuthenticationException;

	/*
	 * ========================================================================
	 * PUT
	 * ========================================================================	
	 */

	/**
	 * <b>MKDIRS</b>
	 * 
	 * curl -i -X PUT "http://<HOST>:<PORT>/<PATH>?op=MKDIRS[&permission=<OCTAL>]"
	 * 
	 * @param path
	 * @return
	 * @throws AuthenticationException 
	 * @throws IOException 
	 * @throws MalformedURLException 
	 */
	public String updatePolicyByName(String policyName, String jsonContent) throws MalformedURLException, IOException, AuthenticationException ;

	
	
	
}

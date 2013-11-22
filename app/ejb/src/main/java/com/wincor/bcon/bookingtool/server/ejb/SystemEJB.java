package com.wincor.bcon.bookingtool.server.ejb;

import java.io.FileOutputStream;
import java.io.IOException;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;

@Stateless
public class SystemEJB implements SystemEJBLocal {

	//protected final static String EAR_FILE = "C:\\jboss-7.1.1\\standalone\\deployments\\bookingtool.ear";
	protected final static String EAR_FILE = "/opt/glassfish3/glassfish/domains/domain1/autodeploy/bookingtool.ear";
	
	@Override
	@RolesAllowed("superuser")
	public void deployEar(byte[] contents) throws IOException {
		FileOutputStream fos = new FileOutputStream(EAR_FILE);
		fos.write(contents);
		fos.close();
	}

}

package com.wincor.bcon.bookingtool.server.ejb;

import com.wincor.bcon.bookingtool.server.db.entity.SystemInfo;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.annotation.security.PermitAll;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class SystemEJB {

	//protected final static String EAR_FILE = "C:\\jboss-7.1.1\\standalone\\deployments\\bookingtool.ear";
	//protected final static String EAR_FILE = "/opt/glassfish3/glassfish/domains/domain1/autodeploy/bookingtool.ear";
	protected final static String EAR_FILE = "/usr/jboss/standalone/deployments/bookingtool.ear";
	
    @PersistenceContext(unitName = "EJBsPU")
    private EntityManager em;
    
	@RolesAllowed("superuser")
	public void deployEar(byte[] contents) throws IOException {
		FileOutputStream fos = new FileOutputStream(EAR_FILE);
		fos.write(contents);
		fos.close();
	}

        @PermitAll
        public String getSystemWarning() {
            SystemInfo info = em.find(SystemInfo.class, "syswarning");
            if (info != null) return info.getValue();
            return null;
        }

	@RolesAllowed("superuser")
        public void setSystemWarning(String warning) {
            SystemInfo info = em.find(SystemInfo.class, "syswarning");
            if (info == null) {
                info = new SystemInfo("syswarning", warning);
                em.persist(info);
            } else {
                info.setValue(warning);
            }
        }
}

package com.wincor.bcon.bookingtool.server.ejb;

import java.io.IOException;

import javax.ejb.Local;

@Local
public interface SystemEJBLocal {

	public void deployEar(byte[] contents) throws IOException;

        public String getSystemWarning();
        
        public void setSystemWarning(String warning);
}

package com.wincor.bcon.bookingtool.server.ejb.fi;

import java.util.List;
import java.util.Map;

import javax.ejb.Local;

import com.wincor.bcon.bookingtool.server.vo.AutoCreateInfoVo;

@Local
public interface AdminFIEJBLocal {

	public Map<Class<?>, List<?>> autoCreateBudgetsAndTemplates(AutoCreateInfoVo createVo);

}

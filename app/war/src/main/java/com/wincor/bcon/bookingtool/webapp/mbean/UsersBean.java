package com.wincor.bcon.bookingtool.webapp.mbean;

import java.io.Serializable;
import java.util.List;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import com.wincor.bcon.bookingtool.server.db.entity.User;
import com.wincor.bcon.bookingtool.server.ejb.UsersEJBLocal;
import com.wincor.bcon.bookingtool.server.vo.UserInfoVo;
import com.wincor.bcon.bookingtool.webapp.util.WebUtils;

@Named
@SessionScoped
public class UsersBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@EJB
	private UsersEJBLocal ejb;
	
	private UserInfoVo current = new UserInfoVo(new User());

	public void clear() {
		current = new UserInfoVo(new User());
	}

	public UserInfoVo getCurrent() {
		return current;
	}
	
	public List<UserInfoVo> getUsers() {
		return ejb.getUsers();
	}
	
	public void save() {
		ejb.saveUser(current);
                clear();
	}
	
	public void edit(UserInfoVo u) {
		current = u;
	}
	
	public void delete(UserInfoVo u) {
		try {
			ejb.deleteUser(u.getUser().getName());
		} catch (Exception ex) {
			WebUtils.addFacesMessage(ex);
		}
	}

        public void resetPassword(UserInfoVo u) {
            ejb.resetPassword(u.getUser().getName());
	}
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.wincor.bcon.bookingtool.server.vo;

import com.wincor.bcon.bookingtool.server.db.entity.User;

/**
 * Simple container holding a User entity and a comma-separated list of role names
 */
public class UserInfoVo {
    private User user;
    private String roleList;

    public UserInfoVo(User user) {
        this.user = user;
    }
    
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getRoleList() {
        return roleList;
    }

    public void setRoleList(String roleList) {
        this.roleList = roleList;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.wincor.bcon.bookingtool.server.ejb;

import com.wincor.bcon.bookingtool.server.db.entity.User;
import com.wincor.bcon.bookingtool.server.vo.UserInfoVo;
import java.util.List;
import javax.ejb.Local;

/**
 *
 */
@Local
public interface UsersEJBLocal {

    /**
     * Returns the list of users
     * @return list of users
     */
    public List<UserInfoVo> getUsers();

    /**
     * Get the current user
     * @return current user
     */
    public User getCurrentUser();
    
    /**
     * Saves the user to the database.
     * @param userVo a user value object
     */
    public void saveUser(UserInfoVo userVo);

    /**
     * Deletes the user
     * @param userName a user name
     */
    public void deleteUser(String userName);

    /**
     * Changes the password of the currently logged in user
     * @param oldPassword old password
     * @param newPassword new password
     */
    public void changePassword(String oldPassword, String newPassword);
}

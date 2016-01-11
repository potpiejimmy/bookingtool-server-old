/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.wincor.bcon.bookingtool.server.ejb;

import com.wincor.bcon.bookingtool.server.db.entity.User;
import com.wincor.bcon.bookingtool.server.db.entity.UserRole;
import com.wincor.bcon.bookingtool.server.vo.UserInfoVo;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class UsersEJB {

    @PersistenceContext(unitName = "EJBsPU")
    private EntityManager em;
    
    @Resource
    private SessionContext ctx;
	
    /**
     * Returns the list of users
     * @return list of users
     */
    @RolesAllowed({"superuser"})
    public List<UserInfoVo> getUsers() {
        List<User> users = em.createNamedQuery("User.findAll", User.class).getResultList();
        List<UserInfoVo> result = new ArrayList<UserInfoVo>(users.size());
        for (User user : users) {
            UserInfoVo vo = new UserInfoVo(user);
            vo.setRoleList(getUserRoleNames(user));
            result.add(vo);
        }
        return result;
    }
    
    protected String getUserRoleNames(User user) {
        List<UserRole> roles = em.createNamedQuery("UserRole.findByUserName", UserRole.class).setParameter("userName", user.getName()).getResultList();
        StringBuilder stb = new StringBuilder();
        for (UserRole role : roles) {
            if (stb.length() > 0) stb.append(',');
            stb.append(role.getRole());
        }
        return stb.toString();
    }

    /**
     * Saves the user to the database.
     * @param userVo a user value object
     */
    @RolesAllowed({"superuser"})
    public void saveUser(UserInfoVo userVo) {
        User user = userVo.getUser();
        if (em.find(User.class, user.getName())==null) {
            // new user, set a default password
            setDefaultPassword(user);
            em.persist(user);
        } else {
            // existing user, nothing to do (cannot alter name or password here)
            // remove old roles:
            em.createNamedQuery("UserRole.deleteByUserName").setParameter("userName", user.getName()).executeUpdate();
        }

        // now persist all new roles
        String[] roles = userVo.getRoleList().split(",");
        for (String rname : roles) {
            UserRole role = new UserRole();
            role.setUserName(user.getName());
            role.setRole(rname.trim());
            em.persist(role);
        }
    }

    /**
     * Deletes the user
     * @param userName a user name
     */
    @RolesAllowed({"superuser"})
    public void deleteUser(String userName) {
        em.createNamedQuery("UserRole.deleteByUserName").setParameter("userName", userName).executeUpdate();
        em.remove(em.find(User.class, userName));
    }

    /**
     * Resets the password for the given user to
     * a default value.
     * @param userName a user name
     */
    @RolesAllowed({"superuser"})
    public void resetPassword(String userName) {
        setDefaultPassword(em.find(User.class, userName));
    }

    /**
     * Get the current user
     * @return current user
     */
    @RolesAllowed({"user","admin","superuser"})
    public User getCurrentUser() {
        return em.find(User.class, ctx.getCallerPrincipal().getName());
    }

    /**
     * Changes the password of the currently logged in user
     * @param oldPassword old password
     * @param newPassword new password
     */
    @RolesAllowed({"user","admin","superuser"})
    public void changePassword(String oldPassword, String newPassword) {
        User user = getCurrentUser();
        if (!md5Hex(oldPassword).equals(user.getPassword()))
            throw new RuntimeException("Sorry, you entered the wrong password.");
        if (oldPassword.equals(newPassword))
            throw new RuntimeException("Sorry, the new password cannot be the same as the old password.");
        user.setPassword(md5Hex(newPassword));
        user.setPwStatus((byte)1);
    }
    
    protected static void setDefaultPassword(User user) {
        // set a default password and pwStatus = 0 (force change pw)
        user.setPwStatus((byte)0); // change password
        user.setPassword(md5Hex(user.getName())); // default password
    }
    
    protected static String md5Hex(String in) {
        StringBuilder md5 = new StringBuilder(new BigInteger(1, md5(in)).toString(16));
        while (md5.length()<32) md5.insert(0, '0');
        return md5.toString();
    }
    
    protected static byte[] md5(String in) {
        try {
            return MessageDigest.getInstance("MD5").digest(in.getBytes("ISO-8859-1"));
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }
}

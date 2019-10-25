package com.stylefeng.guns.user.vo;

import java.io.Serializable;

/**
 * @AUTHOR :yuankejia
 * @DESCRIPTION: 这个userModel不会用来封装全部的信息，而是仅仅用来登录使用的。
 * @DATE:CRETED: IN 12:18 2019/10/23
 * @MODIFY:
 */
public class UserModel implements Serializable {
    private String username;
    private String password;
    private String phone;
    private String address;
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "UserModel{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}

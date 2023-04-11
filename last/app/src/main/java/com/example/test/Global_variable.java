package com.example.test;

import android.app.Application;

public class Global_variable extends Application {
    private String code;
//    private String ip = "220.124.24.89";
    private String ip = "203.230.154.190";
    private int port = 8080;

    public String getCode(){
        return code;
    }

    public void setCode(String code){
        this.code = code;
    }

    public String getIp() { return ip; }

    public void setIP(String ip) {this.ip = ip;}

    public int getPort() { return port; }
}
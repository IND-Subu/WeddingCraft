package com.subu.Login;

import java.util.HashMap;
import java.util.Map;

public class UserToken {
    public String name;
    public String token;

    public UserToken(){} // needed for firestore

    public UserToken(String name, String token){
        this.name = name;
        this.token = token;
    }

    public Map<String, Object> toMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("token", token);
        return map;
    }
}

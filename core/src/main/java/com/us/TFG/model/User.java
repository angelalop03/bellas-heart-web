package com.us.TFG.model;

import com.us.TFG.annotations.Column;
import com.us.TFG.annotations.Table;

@Table(name ="users")
public class User{
    @Column(name = "id", type = "INTEGER", primaryKey = true,notNull = true)
    private int id;  // ID único para la base de datos

    @Column(name = "username", type = "TEXT", unique = true,notNull = true)
    private String username;

    @Column(name = "password", type = "TEXT",notNull = true)
    private String password;

    @Column(name = "authority", type = "INTEGER", notNull = true)
    private Authorities authority; // Rol del usuario (ADMIN o PLAYER)


    public User(String username, String password, Authorities  authority) {
        this.username = username;
        this.password = password;
        this.authority = authority;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public Authorities getAuthority() {
        return authority;
    }

    public void setAuthority(Authorities authority) {
        this.authority = authority;
    }
}

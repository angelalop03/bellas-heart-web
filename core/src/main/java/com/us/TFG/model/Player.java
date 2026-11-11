package com.us.TFG.model;


import com.us.TFG.annotations.Column;
import com.us.TFG.annotations.Table;

@Table(name="players")
public class Player {

    @Column(name = "id", type = "INTEGER", primaryKey = true, notNull = true)
    private int id;  // ID único para la base de datos

    @Column(name = "firstname", type = "TEXT", notNull = true)
    private String firstname;

    @Column(name = "lastname", type = "TEXT", notNull = true)
    private String lastname;

    @Column(name = "email", type = "TEXT", notNull = true)
    private String email;

    @Column(name = "image", type = "TEXT", notNull = true, defaultValue = "'default_profile.png'")
    private String image;

    @Column(name = "user_id", type = "INTEGER", notNull = true)  // Relación con el User (clave foránea)
    private int userId;

    public Player(String firstname, String lastname, String email, String image, int userId) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.image = image;
        this.userId = userId;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

}

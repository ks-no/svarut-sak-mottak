package no.ks.sakimport.fake;

public class AuthorizationUser {

    private String username;
    private String password;

    public AuthorizationUser(String username, String password){
        this.username = username;
        this.password = password;
    }

    public String getUsername(){
        return username;
    }

    public String getPassword(){
        return password;
    }
}
public class User {

    @EmailValidation
    private String email;

    public User(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
public class Main {

    public static void main(String[] args) {

        User user1 = new User("vikas@gmail.com");
        User user2 = new User("vikasgmail.com");

        Validator.validate(user1);
        Validator.validate(user2);
    }
}
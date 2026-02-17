import java.util.Scanner;

class LoginValidation {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        int attempts = 0;   // counter for login tries
        String username;
        String password;

        while (attempts < 3) {

            System.out.print("Enter Username: ");
            username = sc.nextLine();

            System.out.print("Enter Password: ");
            password = sc.nextLine();

            // check login
            if (username.equals("admin") && password.equals("1234")) {
                System.out.println("Login Successful");
                return;   // exit program
            } 
            else {
                attempts++;
                System.out.println("Invalid Username or Password");
                System.out.println("Remaining Attempts: " + (3 - attempts));
            }
        }

        // after 3 wrong attempts
        System.out.println("Account Blocked!");
    }
}

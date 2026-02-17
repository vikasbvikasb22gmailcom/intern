import java.util.Scanner;

class LoginValidation {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        int attempts = 0; 
        String username;
        String password;

        while (attempts < 3) {

            System.out.print("Enter Username: ");
            username = sc.nextLine();

            System.out.print("Enter Password: ");
            password = sc.nextLine();

           
            if (username.equals("admin") && password.equals("1234")) {
                System.out.println("Login Successful");
                return;   
            } 
            else {
                attempts++;
                System.out.println("Invalid Username or Password");
                System.out.println("Remaining Attempts: " + (3 - attempts));
            }
        }

      
        System.out.println("Account Blocked!");
    }
}


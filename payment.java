import java.util.Scanner;

class payment {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        double walletBalance = 10000;
        int choice;
        double amount;

        do {
            System.out.println("\nCurrent Wallet Balance = " + walletBalance);

            System.out.println("Choose Payment Method");
            System.out.println("1. UPI");
            System.out.println("2. Card");
            System.out.println("3. Cash");
            System.out.println("4. Exit");
            System.out.print("Enter choice: ");

            choice = sc.nextInt();

            if (choice == 4) {
                System.out.println("Thank you!");
                break;
            }

            System.out.print("Enter amount to pay: ");
            amount = sc.nextDouble();

            if (amount <= walletBalance) {
                walletBalance = walletBalance - amount;
                System.out.println("Payment Successful ✅");
                System.out.println("Updated Balance = " + walletBalance);
            } else {
                System.out.println("Insufficient Balance ❌");
            }

        } while (true);

        sc.close();
    }
}

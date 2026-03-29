import java.util.Scanner;

class ElectricityBill {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        int units;
        double bill = 0;

        System.out.print("Enter number of units consumed: ");
        units = sc.nextInt();

       /* if(units <= 100) {
            bill = units * 5;
        }
        else if(units <= 200) {
            bill = (100 * 5) + (units - 100) * 7;
        }
        else {
            bill = (100 * 5) + (100 * 7) + (units - 200) * 10;
        }
*/
 bill = (units <= 100) 
                ? units * 5
                : (units <= 200)
                    ? (100 * 5) + (units - 100) * 7
                    : (100 * 5) + (100 * 7) + (units - 200) * 10;
        System.out.println("Total Electricity Bill = Rs" + bill);
    }
}


public class Employee {

    private double salary = 100000;

    
    public double get_sal() {
        return salary;
    }

    
    public static void main(String[] args) {

        Employee e = new Employee();   // create object

        System.out.println("Salary = " + e.get_sal());  // print salary
    }
}

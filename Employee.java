
public class Employee {

    private double salary = 100000;

    
    public double get_sal() {
        return salary;
    }

    
    public static void main(String[] args) {

        Employee e = new Employee();   

        System.out.println("Salary = " + e.get_sal());  
    }
}


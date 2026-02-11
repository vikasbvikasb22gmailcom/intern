class Car {
    String color;      

    void start() {   
        System.out.println("Car started");
    }
}

public class Main {
    public static void main(String[] args) {

        Car c1 = new Car();   

        c1.color = "Red";     
        c1.start();          
    }
}

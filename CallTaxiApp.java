import java.util.*;

class Booking {
    int bookingId;
    int customerId;
    char pickupPoint;
    char dropPoint;
    int pickupTime;
    int dropTime;
    int amount;

    public Booking(int bookingId, int customerId, char pickupPoint, char dropPoint, int pickupTime, int dropTime, int amount) {
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.pickupPoint = pickupPoint;
        this.dropPoint = dropPoint;
        this.pickupTime = pickupTime;
        this.dropTime = dropTime;
        this.amount = amount;
    }

    @Override
    public String toString() {
        return bookingId + " " + customerId + " " + pickupPoint + " " + dropPoint + " " + pickupTime + " " + dropTime + " " + amount;
    }
}

class Taxi {
    int taxiId;
    char currentPoint;
    int freeTime;
    int totalEarnings;
    List<Booking> bookings;

    public Taxi(int taxiId) {
        this.taxiId = taxiId;
        this.currentPoint = 'A'; // all taxis start at A
        this.freeTime = 0;
        this.totalEarnings = 0;
        this.bookings = new ArrayList<>();
    }

    public void addBooking(Booking booking, char dropPoint, int dropTime, int amount) {
        bookings.add(booking);
        this.currentPoint = dropPoint;
        this.freeTime = dropTime;
        this.totalEarnings += amount;
    }

    public void displayDetails() {
        System.out.println("Taxi-" + taxiId + " Total Earnings: Rs. " + totalEarnings);
        for (Booking b : bookings) {
            System.out.println(b);
        }
    }
}

class TaxiBookingSystem {
    List<Taxi> taxis;
    int bookingCounter = 1;

    public TaxiBookingSystem(int n) {
        taxis = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            taxis.add(new Taxi(i));
        }
    }

    private int calculateFare(char pickup, char drop) {
        int distance = Math.abs(pickup - drop) * 15; // each segment = 15 km
        if (distance <= 5) return 100;
        return 100 + (distance - 5) * 10;
    }

    private int calculateDropTime(char pickup, char drop, int pickupTime) {
        int segments = Math.abs(pickup - drop);
        return pickupTime + segments; // each segment = 1 hour
    }

    public void bookTaxi(int customerId, char pickup, char drop, int pickupTime) {
        Taxi chosenTaxi = null;
        int minDistance = Integer.MAX_VALUE;

        for (Taxi taxi : taxis) {
            if (taxi.freeTime <= pickupTime) {
                int distance = Math.abs(taxi.currentPoint - pickup);
                if (distance < minDistance) {
                    minDistance = distance;
                    chosenTaxi = taxi;
                } else if (distance == minDistance && chosenTaxi != null) {
                    if (taxi.totalEarnings < chosenTaxi.totalEarnings) {
                        chosenTaxi = taxi;
                    }
                }
            }
        }

        if (chosenTaxi == null) {
            System.out.println("Booking rejected. No taxi available.");
            return;
        }

        int amount = calculateFare(pickup, drop);
        int dropTime = calculateDropTime(pickup, drop, pickupTime);

        Booking booking = new Booking(bookingCounter++, customerId, pickup, drop, pickupTime, dropTime, amount);
        chosenTaxi.addBooking(booking, drop, dropTime, amount);

        System.out.println("Taxi can be allotted.");
        System.out.println("Taxi-" + chosenTaxi.taxiId + " is allotted");
    }

    public void displayTaxiDetails() {
        for (Taxi taxi : taxis) {
            taxi.displayDetails();
            System.out.println();
        }
    }
}

public class CallTaxiApp {
    public static void main(String[] args) {
        TaxiBookingSystem system = new TaxiBookingSystem(4);

        system.bookTaxi(1, 'A', 'B', 9);
        system.bookTaxi(2, 'B', 'D', 9);
        system.bookTaxi(3, 'B', 'C', 12);

        System.out.println("\n--- Taxi Details ---");
        system.displayTaxiDetails();
    }
}

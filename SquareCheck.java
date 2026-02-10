import java.util.*;

class SquareCheck {

    
    static int distSq(int x1, int y1, int x2, int y2) {
        return (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
    }

    static boolean isSquare(int[] p1, int[] p2, int[] p3, int[] p4) {

        int[] d = new int[6];

       
        d[0] = distSq(p1[0], p1[1], p2[0], p2[1]);
        d[1] = distSq(p1[0], p1[1], p3[0], p3[1]);
        d[2] = distSq(p1[0], p1[1], p4[0], p4[1]);
        d[3] = distSq(p2[0], p2[1], p3[0], p3[1]);
        d[4] = distSq(p2[0], p2[1], p4[0], p4[1]);
        d[5] = distSq(p3[0], p3[1], p4[0], p4[1]);

        Arrays.sort(d);

       
        return d[0] > 0 &&
               d[0] == d[1] &&
               d[1] == d[2] &&
               d[2] == d[3] &&
               d[4] == d[5];
    }

    public static void main(String[] args) {

        int[] p1 = {20, 10};
        int[] p2 = {10, 20};
        int[] p3 = {20, 20};
        int[] p4 = {10, 10};

        if (isSquare(p1, p2, p3, p4))
            System.out.println("Yes");
        else
            System.out.println("No");
    }
}


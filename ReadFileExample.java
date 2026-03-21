import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ReadFileExample {

    public static void main(String[] args) {

        try {
      FileReader fr = new FileReader("C:\\Users\\vikas\\Desktop\\java\\JavaFileRead\\user.txt");
            BufferedReader br = new BufferedReader(fr);

            String line;

            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }

            br.close();

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }

    }
}
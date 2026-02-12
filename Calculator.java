import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Calculator extends JFrame implements ActionListener {

    JTextField tf;
    double n1, n2, result;
    char op;

    Calculator() {

        
        setTitle("Simple Calculator");
        setSize(300, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

       
        tf = new JTextField();
        tf.setFont(new Font("Arial", Font.BOLD, 150));
        tf.setHorizontalAlignment(JTextField.RIGHT);
        add(tf, BorderLayout.NORTH);

     
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(4, 4, 5, 5));

        
        String btns[] = {
                "7","8","9","/",
                "4","5","6","*",
                "1","2","3","-",
                "0","C","=","+"
        };

        
        for(String s : btns) {
            JButton b = new JButton(s);
            b.setFont(new Font("Arial", Font.BOLD, 20));
            b.addActionListener(this);
            p.add(b);
        }

        add(p);

        setVisible(true);
    }

    
    public void actionPerformed(ActionEvent e) {

        String s = e.getActionCommand();

     
        if(s.matches("[0-9]")) {
            tf.setText(tf.getText() + s);
        }

       
        else if(s.equals("C")) {
            tf.setText("");
        }

       
        else if(s.matches("[+\\-*/]")) {
            n1 = Double.parseDouble(tf.getText());
            op = s.charAt(0);
            tf.setText("");
        }

       
        else if(s.equals("=")) {

            n2 = Double.parseDouble(tf.getText());

            switch(op) {
                case '+': result = n1 + n2; break;
                case '-': result = n1 - n2; break;
                case '*': result = n1 * n2; break;
                case '/': result = n1 / n2; break;
            }

            tf.setText("" + result);
        }
    }

 
    public static void main(String[] args) {
        new Calculator();
    }
}


import java.lang.reflect.Field;

public class Validator {

    public static void validate(Object obj) {

        Field[] fields = obj.getClass().getDeclaredFields();

        for (Field field : fields) {

            if (field.isAnnotationPresent(EmailValidation.class)) {

                field.setAccessible(true);

                try {

                    String value = (String) field.get(obj);

                    if (value.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                        System.out.println("Valid Email: " + value);
                    } else {
                        System.out.println("Invalid Email: " + value);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
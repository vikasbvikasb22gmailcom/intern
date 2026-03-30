import java.util.*;

class Blog {
    int id;
    String title;
    String content;
    String author;
    String date;
    String category;

    Blog(int id, String title, String content, String author, String date, String category) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.author = author;
        this.date = date;
        this.category = category;
    }

    void display() {
        System.out.println("\n----------------------------");
        System.out.println("ID: " + id);
        System.out.println("Title: " + title);
        System.out.println("Author: " + author);
        System.out.println("Date: " + date);
        System.out.println("Category: " + category);
        System.out.println("Content: " + content);
        System.out.println("----------------------------");
    }
}

public class BlogApp {

    static Scanner sc = new Scanner(System.in);
    static ArrayList<Blog> blogs = new ArrayList<>();
    static int idCounter = 1;

    // ---------------- ADMIN LOGIN ----------------
    static boolean adminLogin() {
        System.out.print("Enter Admin Username: ");
        String username = sc.nextLine();

        System.out.print("Enter Admin Password: ");
        String password = sc.nextLine();

        return username.equals("admin") && password.equals("1234");
    }

    // ---------------- CREATE BLOG ----------------
    static void createBlog() {
        System.out.print("Enter Title: ");
        String title = sc.nextLine();

        System.out.print("Enter Content: ");
        String content = sc.nextLine();

        System.out.print("Enter Author Name: ");
        String author = sc.nextLine();

        System.out.print("Enter Date: ");
        String date = sc.nextLine();

        System.out.print("Enter Category: ");
        String category = sc.nextLine();

        blogs.add(new Blog(idCounter++, title, content, author, date, category));
        System.out.println("Blog Created Successfully!");
    }

    // ---------------- VIEW ALL BLOGS ----------------
    static void viewAllBlogs() {
        if (blogs.isEmpty()) {
            System.out.println("No Blogs Available.");
            return;
        }
        for (Blog b : blogs) {
            b.display();
        }
    }

    // ---------------- VIEW BLOG BY ID ----------------
    static void viewBlogById() {
        System.out.print("Enter Blog ID: ");
        int id = sc.nextInt();
        sc.nextLine();

        for (Blog b : blogs) {
            if (b.id == id) {
                b.display();
                return;
            }
        }
        System.out.println("Blog Not Found!");
    }

    // ---------------- SEARCH BY TITLE ----------------
    static void searchByTitle() {
        System.out.print("Enter Title Keyword: ");
        String keyword = sc.nextLine().toLowerCase();

        for (Blog b : blogs) {
            if (b.title.toLowerCase().contains(keyword)) {
                b.display();
            }
        }
    }

    // ---------------- FILTER BY CATEGORY ----------------
    static void filterByCategory() {
        System.out.print("Enter Category: ");
        String category = sc.nextLine().toLowerCase();

        for (Blog b : blogs) {
            if (b.category.toLowerCase().equals(category)) {
                b.display();
            }
        }
    }

    // ---------------- EDIT BLOG ----------------
    static void editBlog() {
        System.out.print("Enter Blog ID to Edit: ");
        int id = sc.nextInt();
        sc.nextLine();

        for (Blog b : blogs) {
            if (b.id == id) {
                System.out.print("New Title: ");
                b.title = sc.nextLine();

                System.out.print("New Content: ");
                b.content = sc.nextLine();

                System.out.print("New Category: ");
                b.category = sc.nextLine();

                System.out.println("Blog Updated Successfully!");
                return;
            }
        }
        System.out.println("Blog Not Found!");
    }

    // ---------------- DELETE BLOG ----------------
    static void deleteBlog() {
        System.out.print("Enter Blog ID to Delete: ");
        int id = sc.nextInt();
        sc.nextLine();

        Iterator<Blog> iterator = blogs.iterator();

        while (iterator.hasNext()) {
            Blog b = iterator.next();
            if (b.id == id) {
                iterator.remove();
                System.out.println("Blog Deleted Successfully!");
                return;
            }
        }
        System.out.println("Blog Not Found!");
    }

    // ---------------- MAIN METHOD ----------------
    public static void main(String[] args) {

        while (true) {
            System.out.println("\n===== BLOG SYSTEM =====");
            System.out.println("1. Admin Login");
            System.out.println("2. View All Blogs");
            System.out.println("3. View Blog by ID");
            System.out.println("4. Search Blog by Title");
            System.out.println("5. Filter by Category");
            System.out.println("6. Exit");
            System.out.print("Choose Option: ");

            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {

                case 1:
                    if (adminLogin()) {
                        System.out.println("Login Successful!");
                        adminMenu();
                    } else {
                        System.out.println("Invalid Credentials!");
                    }
                    break;

                case 2:
                    viewAllBlogs();
                    break;

                case 3:
                    viewBlogById();
                    break;

                case 4:
                    searchByTitle();
                    break;

                case 5:
                    filterByCategory();
                    break;

                case 6:
                    System.out.println("Exiting...");
                    return;

                default:
                    System.out.println("Invalid Option!");
            }
        }
    }

    // ---------------- ADMIN MENU ----------------
    static void adminMenu() {
        while (true) {
            System.out.println("\n--- ADMIN MENU ---");
            System.out.println("1. Create Blog");
            System.out.println("2. Edit Blog");
            System.out.println("3. Delete Blog");
            System.out.println("4. Back");
            System.out.print("Choose Option: ");

            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    createBlog();
                    break;
                case 2:
                    editBlog();
                    break;
                case 3:
                    deleteBlog();
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid Option!");
            }
        }
    }
}
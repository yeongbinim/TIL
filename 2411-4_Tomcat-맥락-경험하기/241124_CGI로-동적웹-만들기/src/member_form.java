import java.io.*;

public class member_form {
    public static void main(String[] args) throws IOException {
        System.out.println("Content-Type: text/html");
        System.out.println();

        System.out.println("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Create Member</title>
                </head>
                <body>
                    <form action="/cgi-bin/dispatcher/member_save" method="POST">
                        username: <input type="text" name="username" />
                        password: <input type="password" name="password" />
                        <button type="submit">Submit</button>
                    </form>
                </body>
                </html>
                """);
    }
}
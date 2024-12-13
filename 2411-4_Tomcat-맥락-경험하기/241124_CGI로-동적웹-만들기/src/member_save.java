import java.io.*;
import java.util.*;

public class member_save {
    public static void main(String[] args) throws IOException {
        MemberRepository memberRepository = MemberRepository.getInstance();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder body = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            body.append(line);
        }

        String[] params = body.toString().split("&");
        String username = params[0].split("=")[1];
        String password = params[1].split("=")[1];

        Member member = new Member(null, username, password);
        Member newMember = memberRepository.save(member);

        System.out.println("Content-Type: text/html");
        System.out.println();

        System.out.println("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Member Saved</title>
                </head>
                <body>
                    <h1>Member Saved Successfully</h1>
                    <ul>
                        <li>ID: """ + newMember.getId() + """
                        </li>
                        <li>Username: """ + newMember.getName() + """
                        </li>
                        <li>Password: """ + newMember.getPassword() + """
                        </li>
                    </ul>
                    <a href="/cgi-bin/dispatcher/member_list">View All Members</a>
                </body>
                </html>
                """);
    }
}

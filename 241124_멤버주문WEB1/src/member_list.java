import java.io.*;
import java.util.*;

public class member_list {
    public static void main(String[] args) throws IOException {
        MemberRepository memberRepository = MemberRepository.getInstance();
        List<Member> members = memberRepository.findAll();

        System.out.println("Content-Type: text/html");
        System.out.println();

        StringBuilder tableRows = new StringBuilder();
        for (Member member : members) {
            tableRows.append("""
                    <tr>
                        <td>""" + member.getId() + """
                        </td>
                        <td>""" + member.getName() + """
                        </td>
                        <td>""" + member.getPassword() + """
                        </td>
                    </tr>
                    """);
        }

        System.out.println("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                </head>
                <body>
                    <h1>Member List</h1>
                    <a href="/cgi-bin/dispatcher/member_form">Create New Member</a>
                    <table border="1">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Username</th>
                                <th>Password</th>
                            </tr>
                        </thead>
                        <tbody>
                            """ + tableRows.toString() + """
                        </tbody>
                    </table>
                </body>
                </html>
                """);
    }
}

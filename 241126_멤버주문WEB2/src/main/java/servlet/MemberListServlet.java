package servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

public class MemberListServlet extends HttpServlet {
    private final MemberRepository memberRepository = MemberRepository.getInstance();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<Member> members = memberRepository.findAll();
        resp.setContentType("text/html;charset=UTF-8");

        PrintWriter out = resp.getWriter();

        String formattedMembers = members.stream().map(member -> """
                    <tr>
                        <td>""" + member.getId() + """
                        </td>
                        <td>""" + member.getName() + """
                        </td>
                        <td>""" + member.getPassword() + """
                        </td>
                    </tr>
                """).collect(Collectors.joining());
        out.write("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                </head>
                <body>
                <h1>Member List</h1>
                <a href="/servlet/members/new-form">회원 생성</a>
                <table>
                    <thead>
                        <th>id</th>
                        <th>username</th>
                        <th>password</th>
                    </thead>
                    <tbody>""" + formattedMembers + """
                    </tbody>
                </table>
                
                </body>
                </html>
                """);
    }
}

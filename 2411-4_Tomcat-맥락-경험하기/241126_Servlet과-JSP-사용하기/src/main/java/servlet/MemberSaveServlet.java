package servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "memberSaveServlet", urlPatterns = "/servlet/members/save")
public class MemberSaveServlet extends HttpServlet {
    private final MemberRepository memberRepository = MemberRepository.getInstance();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        System.out.println("MemberSaveServlet.service");
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        Member member = new Member(null, username, password);
        memberRepository.save(member);
        resp.setContentType("text/html;charset=UTF-8");

        PrintWriter out = resp.getWriter();
        out.write("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                </head>
                <body>
                <h1>Member Saved Successfully</h1>
                <ul>
                    <li>id:""" + member.getId() + """
                    </li>
                    <li>name:""" + member.getName() + """
                    </li>
                    <li>password:""" + member.getPassword() + """
                    </li>
                </ul>
                <a href="/servlet/members">전체 보기</a>
                </body>
                </html>
                """);
    }
}
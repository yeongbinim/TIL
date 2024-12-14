package yeim.infrastructure;

import static yeim.infrastructure.connection.ConnectionConst.PASSWORD;
import static yeim.infrastructure.connection.ConnectionConst.URL;
import static yeim.infrastructure.connection.ConnectionConst.USERNANE;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import yeim.domain.Member;

/**
 * DriveManager로부터 직접 Connection얻어오는 버전
 */
@Slf4j
public class MemberRepositoryV0 {

	public void save(Member member) {
		String sql = "INSERT INTO member(member_id, money) values(?, ?)";
		Connection conn = null;
		PreparedStatement ps = null;

		try {
			conn = getConnection();

			ps = conn.prepareStatement(sql);
			ps.setString(1, member.getMemberId());
			ps.setInt(2, member.getMoney());
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(conn, ps, null);
		}
	}

	public Member findById(String memberId) throws SQLException {
		String sql = "select * from member where member_id = ?";
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			con = getConnection();
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, memberId);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				String name = rs.getString("member_id");
				Integer money = rs.getInt("money");
				return new Member(name, money);
			} else {
				throw new NoSuchElementException("member not found memberId=" +
					memberId);
			}
		} catch (SQLException e) {
			log.error("db error", e);
			throw e;
		} finally {
			close(con, pstmt, rs);
		}
	}


	public void close(Connection conn, PreparedStatement ps, ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				log.info("error", e);
			}
		}

		if (ps != null) {
			try {
				ps.close();
			} catch (SQLException e) {
				log.info("error", e);
			}
		}

		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				log.info("error", e);
			}
		}
	}

	private Connection getConnection() {
		try {
			Connection connection = DriverManager.getConnection(URL, USERNANE, PASSWORD);
			log.info("get connection={}, class={}", connection, connection.getClass());
			// get connection=conn0: url=jdbc:h2:mem:testdb user=SA, class=class org.h2.jdbc.JdbcConnection
			return connection;
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
	}
}

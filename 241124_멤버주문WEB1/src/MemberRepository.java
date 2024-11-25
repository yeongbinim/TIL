import java.io.*;
import java.util.*;

public class MemberRepository {
    private static final String DATA_FILE = "/opt/homebrew/var/www/cgi-bin/members.csv";
    private static final String DELIMITER = ",";

    private static MemberRepository instance;

    public static MemberRepository getInstance() {
        if (instance == null) {
            instance = new MemberRepository();
        }
        return instance;
    }

    private MemberRepository() {}

    public List<Member> findAll() throws IOException {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        List<Member> members = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(DATA_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(DELIMITER);
                if (fields.length >= 3) {
                    long id = Long.parseLong(fields[0]);
                    String name = fields[1];
                    String password = fields[2];
                    members.add(new Member(id, name, password));
                }
            }
        }
        return members;
    }

    // ID로 특정 멤버 찾기
    public Optional<Member> findById(Long id) throws IOException {
        List<Member> members = findAll();
        return members.stream()
                .filter(member -> member.getId().equals(id))
                .findFirst();
    }

    // 새로운 멤버 저장
    public Member save(Member member) throws IOException {
        List<Member> members = findAll();

        // ID 할당
        long maxId = members.stream()
                .mapToLong(Member::getId)
                .max()
                .orElse(0);
        member.setId(maxId + 1);

        // 멤버 추가 및 CSV 파일 업데이트
        members.add(member);
        writeMembersToFile(members);

        return member;
    }

    // CSV 파일에 멤버 리스트 저장
    private void writeMembersToFile(List<Member> members) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATA_FILE))) {
            for (Member member : members) {
                writer.write(String.join(DELIMITER,
                        String.valueOf(member.getId()),
                        member.getName(),
                        member.getPassword()));
                writer.newLine();
            }
        }
    }
}

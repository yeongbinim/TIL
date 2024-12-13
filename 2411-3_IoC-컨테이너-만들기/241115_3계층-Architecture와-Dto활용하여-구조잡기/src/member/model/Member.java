package member.model;

public class Member {
    private Long id;
    private String name;
    private String email;
    private MemberGrade grade;
    private String password;

    public Member(Long id, String name, String email, MemberGrade grade, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.grade = grade;
        this.password = password;
    }

    public static Member from(MemberCreate memberCreate) {
        return new Member(
                null,
                memberCreate.getName(),
                memberCreate.getEmail(),
                memberCreate.getGrade(),
                memberCreate.getPassword()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public MemberGrade getGrade() {
        return grade;
    }

    public void setGrade(MemberGrade grade) {
        this.grade = grade;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}


package mvc.domain;

public class Member {
    private Long id;
    private String name;
    private String password;

    public Member(Long id, String name, String password) {
        this.id = id;
        this.name = name;
        this.password = password;
    }

    public static Member from(MemberCreate memberCreate) {
        return new Member(
                null,
                memberCreate.getName(),
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

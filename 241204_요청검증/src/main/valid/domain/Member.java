package valid.domain;

public class Member {

    private Long id;
    private String name;
    private String password;
    private Integer money;

    public Member(Long id, String name, String password, Integer money) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.money = money;
    }

    public static Member from(MemberCreate memberCreate) {
        return new Member(
            null,
            memberCreate.getName(),
            memberCreate.getPassword(),
            memberCreate.getMoney()
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

    public Integer getMoney() {
        return money;
    }

    public void setMoney(Integer money) {
        this.money = money;
    }
}

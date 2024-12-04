package valid.domain;

public class MemberCreate {

    private String name;
    private String password;
    private Integer money;

    public MemberCreate(String name, String password, Integer money) {
        this.name = name;
        this.password = password;
        this.money = money;
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

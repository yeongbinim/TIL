package valid.domain;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class MemberCreate {

    @NotBlank
    @Pattern(
        regexp = "^[a-z][a-zA-Z0-9]{3,13}$",
        message = "영어와 숫자로만, 첫 글자는 소문자, 최소 4자, 최대 14자"
    )
    private String name;

    @NotBlank
    @Pattern(
        regexp = "^[A-Z][a-zA-Z0-9]{3,13}$",
        message = "영어와 숫자로만, 첫 글자는 대문자, 최소 4자, 최대 14자"
    )
    private String password;

    @NotNull
    @Min(1_000)
    @Max(1_000_000)
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

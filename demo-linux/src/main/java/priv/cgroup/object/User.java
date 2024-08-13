package priv.cgroup.object;

import jakarta.persistence.*;

@Entity(name="User")
@Table(name="user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private String username;
    @Column
    private String phoneNumber;
    //密码加密之后存储在数据库
    @Column
    private String password;

    public User(int id, String username, String phoneNumber, String password) {
        this.id = id;
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.password = password;
    }

    public User() {

    }

    // Getter 和 Setter 方法
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUserName(String name) {
        this.username = username;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    // 重写toString方法
    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}


package priv.cgroup.object;


import jakarta.persistence.*;

@Entity(name="Task")
@Table(name="task")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private String name;
    @Column
    private String pid;
    @Column
    private String path;
    @Column
    private String datestamp;
    @Column
    private String description;

    public Task(){
        this.pid = "";
        this.path = "";
        this.datestamp = "";
    }

    public Task(String name, String pid, String path, String datestamp, String description) {
        this.name = name;
        this.pid = pid;
        this.path = path;
        this.datestamp = datestamp;
        this.description = description;
    }


    public String getPid() {
        return pid;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDatestamp() {
        return datestamp;
    }

    public void setDatestamp(String datestamp) {
        this.datestamp = datestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

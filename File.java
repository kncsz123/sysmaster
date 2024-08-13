package priv.cgroup.object;

import jakarta.persistence.*;

@Entity(name="File")
@Table(name="file")
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private String path;
    @Column
    private String name;
    @Column
    private String datestamp;
    @Column
    private String lastmodified;
    @Column
    private long size;

    public File(){};

    public File(String name, String path, String datestamp, String lastmodified, long size) {
        this.name = name;
        this.path = path;
        this.datestamp = datestamp;
        this.lastmodified = lastmodified;
        this.size = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDatastamp() {
        return datestamp;
    }

    public void setDatastamp(String datastamp) {
        this.datestamp = datastamp;
    }

    public String getLastmodified() {
        return lastmodified;
    }

    public void setLastmodified(String lastmodified) {
        this.lastmodified = lastmodified;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}

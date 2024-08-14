package priv.cgroup.object;
import jakarta.persistence.*;

@Entity(name="Cgroup")
@Table(name="cgroup")
public class Cgroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private String name;
    @Column
    private String cgroupPath;
    @Column
    private String cgroupConfigDir;
    @Column
    private String datestamp;
    @Column
    private String description;
    @Column
    private int hierarchy;

    @Column
    private Byte isCpu_controller_status;

    @Column
    private Byte isCpuset_controller_status;

    @Column
    private Byte isIo_controller_status;

    @Column
    private Byte isMemory_controller_status;

    @Column
    private Byte isPids_controller_status;

    @Column
    private boolean cpu_controller_status;

    @Column
    private boolean memory_controller_status;

    @Column
    private boolean io_controller_status;

    @Column
    private boolean pids_controller_status;

    @Column
    private boolean cpuset_controller_status;

    @Column
    private boolean rdma_controller_status;

    @Column
    private boolean hugetlb_controller_status;

    @Column
    private boolean misc_controller_status;

    public Cgroup() {}

    public Cgroup(String name, int hierarchy, String datestamp, String description){
        this.name = name;
        this.hierarchy = hierarchy;
        this.datestamp = "default";
        this.description = "default";
    }

    public Cgroup(String cgroupPath, String cgroupConfigDir){
        this.cgroupPath = cgroupPath;
        this.cgroupConfigDir = cgroupConfigDir;
    }

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    public String getCgroupPath() {
        return cgroupPath;
    }

    public void setCgroupPath(String cgroupPath) {
        this.cgroupPath = cgroupPath;
    }

    public int getHierarchy() {
        return hierarchy;
    }

    public void setHierarchy(int hierarchy) {
        this.hierarchy = hierarchy;
    }

    public String getCgroupConfigDir() {
        return cgroupConfigDir;
    }

    public void setCgroupConfigDir(String cgroupConfigDir) {
        this.cgroupConfigDir = cgroupConfigDir;
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

    public boolean isCpu_controller_status() {
        return cpu_controller_status;
    }

    public void setCpu_controller_status(boolean cpu_controller_status) {
        this.cpu_controller_status = cpu_controller_status;
    }

    public boolean isMemory_controller_status() {
        return memory_controller_status;
    }

    public void setMemory_controller_status(boolean memory_controller_status) {
        this.memory_controller_status = memory_controller_status;
    }

    public boolean isIo_controller_status() {
        return io_controller_status;
    }

    public void setIo_controller_status(boolean io_controller_status) {
        this.io_controller_status = io_controller_status;
    }

    public boolean isPids_controller_status() {
        return pids_controller_status;
    }

    public void setPids_controller_status(boolean pids_controller_status) {
        this.pids_controller_status = pids_controller_status;
    }

    public boolean isCpuset_controller_status() {
        return cpuset_controller_status;
    }

    public void setCpuset_controller_status(boolean cpuset_controller_status) {
        this.cpuset_controller_status = cpuset_controller_status;
    }

    public boolean isRdma_controller_status() {
        return rdma_controller_status;
    }

    public void setRdma_controller_status(boolean rdma_controller_status) {
        this.rdma_controller_status = rdma_controller_status;
    }

    public boolean isHugetlb_controller_status() {
        return hugetlb_controller_status;
    }

    public void setHugetlb_controller_status(boolean hugetlb_controller_status) {
        this.hugetlb_controller_status = hugetlb_controller_status;
    }

    public boolean isMisc_controller_status() {
        return misc_controller_status;
    }

    public void setMisc_controller_status(boolean misc_controller_status) {
        this.misc_controller_status = misc_controller_status;
    }

    public Byte getIsCpu_controller_status() {
        return isCpu_controller_status;
    }

    public void setIsCpu_controller_status(Byte isCpu_controller_status) {
        this.isCpu_controller_status = isCpu_controller_status;
    }

    public Byte getIsCpuset_controller_status() {
        return isCpuset_controller_status;
    }

    public void setIsCpuset_controller_status(Byte isCpuset_controller_status) {
        this.isCpuset_controller_status = isCpuset_controller_status;
    }

    public Byte getIsIo_controller_status() {
        return isIo_controller_status;
    }

    public void setIsIo_controller_status(Byte isIo_controller_status) {
        this.isIo_controller_status = isIo_controller_status;
    }

    public Byte getIsMemory_controller_status() {
        return isMemory_controller_status;
    }

    public void setIsMemory_controller_status(Byte isMemory_controller_status) {
        this.isMemory_controller_status = isMemory_controller_status;
    }

    public Byte getIsPids_controller_status() {
        return isPids_controller_status;
    }

    public void setIsPids_controller_status(Byte isPids_controller_status) {
        this.isPids_controller_status = isPids_controller_status;
    }
}

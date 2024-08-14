package priv.cgroup.repository;

import jakarta.transaction.Transactional;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import priv.cgroup.object.Cgroup;

import java.util.List;

@Repository
public interface CgroupRepository extends JpaRepository<Cgroup, Integer> , JpaSpecificationExecutor<Cgroup> {

    @Query("SELECT new priv.cgroup.object.Cgroup(c.cgroupPath,c.cgroupConfigDir) FROM Cgroup c WHERE c.hierarchy = :hierarchy AND c.name = :name")
    //@Query(value = "SELECT * FROM cgroup WHERE name = ?1 and hierarchy = ?2", nativeQuery = true)
    public Cgroup selectCgroup(@Param("hierarchy") int hierarchy,
                               @Param("name") String name);
    List<Cgroup> findByName(String name);

    @Query("SELECT c FROM Cgroup c WHERE c.datestamp BETWEEN :startTime AND :endTime")
    List<Cgroup> findByDateRange(String startTime, String endTime);

    @Query("SELECT c FROM Cgroup c WHERE c.name = :name AND c.datestamp BETWEEN :startTime AND :endTime")
    List<Cgroup> findByNameAndDateRange(String name, String startTime, String endTime);

    @NotNull
    @Transactional
    @Override
    public Cgroup save(Cgroup cgroup);

    //在方法执行前开启一个事务，并在方法执行后根据情况提交或回滚事务。
    @Transactional
    @Modifying
    @Query("DELETE FROM Cgroup c WHERE c.cgroupPath LIKE CONCAT(:cgroupPath, '%')")
    public void deleteCgroupByPathPrefix(@Param("cgroupPath") String cgroupPath);
}

package priv.cgroup.repository;

import jakarta.transaction.Transactional;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import priv.cgroup.object.Task;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {

    @Query("SELECT t FROM Task t WHERE t.pid = :pid")
    public Task selectTask(@Param("pid") String pid);

    public List<Task> findByName(String name);

    @Query("SELECT t FROM Task t WHERE t.datestamp BETWEEN :startTime AND :endTime")
    public List<Task> findByDateRange(String startTime, String endTime);

    @Query("SELECT t FROM Task t WHERE t.name = :name AND t.datestamp BETWEEN :startTime AND :endTime")
    public List<Task> findByNameAndDateRange(String name, String startTime, String endTime);

    @NotNull
    @Transactional
    public Task save(Task task);

    // 新增update方法
    @Transactional
    @Modifying
    @Query("UPDATE Task t SET t.path = :path WHERE t.pid = :pid")
    public void updateTask(@Param("pid") String pid, @Param("path") String path);

    @Transactional
    @Modifying//告诉Spring Data JPA这是一个修改操作
    @Query("Delete FROM Task t WHERE t.pid = :pid")
    public void deleteTask(@Param("pid") String pid);
}

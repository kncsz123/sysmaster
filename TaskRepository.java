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
    Task selectTask(@Param("pid") String pid);

    List<Task> findByName(String name);

    @Query("SELECT t FROM Task t WHERE t.datestamp BETWEEN :startTime AND :endTime")
    List<Task> findByDateRange(String startTime, String endTime);

    @Query("SELECT t FROM Task t WHERE t.name = :name AND t.datestamp BETWEEN :startTime AND :endTime")
    List<Task> findByNameAndDateRange(String name, String startTime, String endTime);

    @NotNull
    @Transactional
    Task save(Task task);

    // 新增update方法
    @Transactional
    @Modifying
    @Query("UPDATE Task t SET t.path = :path WHERE t.pid = :pid")
    void updateTask(@Param("pid") String pid, @Param("path") String path);

    // 更新执行时间
    @Transactional
    @Modifying
    @Query(value = "UPDATE task SET total_time_of_recent_run = :total_time_of_recent_run, status = :open WHERE pid = :pid", nativeQuery = true)
//    @Query("UPDATE TASK t SET t.totalTimeOfRecentRun= :total_time_of_recent_run WHERE t.pid = :pid")
    void updateExecTimeAndStatus(@Param("total_time_of_recent_run") String total_time_of_recent_run, @Param("pid") String pid, @Param("open") boolean open);

    @Transactional
    @Modifying//告诉Spring Data JPA这是一个修改操作
    @Query("Delete FROM Task t WHERE t.pid = :pid")
    void deleteTask(@Param("pid") String pid);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM task WHERE path LIKE :prefix", nativeQuery = true)
//    @Query("DELETE FROM TASK t WHERE t.path LIKE CONCAT(:path, '%')")
    void deleteTaskByPathPrefix(@Param("prefix") String prefix);

    @Transactional
    @Modifying
    @Query(value = "UPDATE task SET status = :open WHERE pid = :pid", nativeQuery = true)
    void updateStatus(@Param("pid") String pid, @Param("open") boolean open);
}

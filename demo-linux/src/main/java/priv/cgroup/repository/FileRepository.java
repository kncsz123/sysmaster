package priv.cgroup.repository;

import jakarta.transaction.Transactional;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import priv.cgroup.object.File;

@Repository
public interface FileRepository extends JpaRepository<File, Integer> {
    @Query("SELECT f FROM File f WHERE f.path = :path")
    public File findByPath(@Param("path") String path);

    @NotNull
    @Transactional
    @Modifying
    @Override
    public File save(File file);

    /*
        该API暂未实现
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM File f WHERE f.path = :path")
    public void deleteFileByPath(@Param("path") String path);
}

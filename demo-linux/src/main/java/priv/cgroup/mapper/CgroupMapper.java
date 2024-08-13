package priv.cgroup.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import priv.cgroup.object.Cgroup;

import java.util.Map;

@Mapper
public interface CgroupMapper {
    public Cgroup selectCgroup(@Param("params") Map<String, Object> params);

    public Cgroup selectAllCgroup();

    public void insertCgroup(Cgroup cgroup);

    public void updateCgroup(Cgroup cgroup);

    public void deleteCgroup(int hierarchy, String name);

    @Delete("DELETE FROM cgroup WHERE cgroupPath LIKE CONCAT(#{cgroupPath}, '%')")
    void deleteCgroupByPathPrefix(String cgroupPath);

}

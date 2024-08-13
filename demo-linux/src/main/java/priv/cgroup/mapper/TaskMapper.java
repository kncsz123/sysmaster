package priv.cgroup.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import priv.cgroup.object.Task;

import java.util.List;
import java.util.Map;

@Mapper
public interface TaskMapper {
    public Task selectTask(@Param("params") Map<String, Object> params);
    public void insertTask(Task task);
    public void deleteTask(String pid);
    public List<Task> selectAllTask();
    public void update(Task selectedTask);
}

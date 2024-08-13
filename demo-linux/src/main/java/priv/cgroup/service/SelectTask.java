package priv.cgroup.service;

import cn.hutool.core.util.StrUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import priv.cgroup.mapper.TaskMapper;
import priv.cgroup.object.Task;
import priv.cgroup.repository.TaskRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SelectTask {
    @Autowired
    private final TaskRepository taskRepository;

    public SelectTask(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /*
        此方法无请求参数
     */
    public Map<String, Object> selectUserTask() {
        Map<String, Object> response = new HashMap<>();

        try{
            //在数据库中查询用户创建了哪些任务
            List<Task> tasks = taskRepository.findAll();
            if(!StrUtil.isEmptyIfStr(tasks)){
                response.put("status", 200);
                response.put("message", "success");
                response.put("tasks", tasks);
            }else{
                response.put("status", 400);
                response.put("message", "No tasks found.");
                response.put("tasks", null);
            }
        }catch (Exception e){
            response.put("status", 500);
            response.put("message", e.getMessage());
            response.put("tasks", null);
            return response;
        }
        return response;
    }
}

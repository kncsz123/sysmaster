package priv.cgroup.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import priv.cgroup.repository.TaskRepository;

import java.util.HashMap;
import java.util.Map;

@Service
public class DeleteTask {

    @Autowired
    private final TaskRepository taskRepository;

    public DeleteTask(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /*
        @param pid 进程的pid
     */
    public Map<String, Object> deleteTask(Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<>();

        String pid = requestBody.get("pid").toString();

        try {
            if(!isProcessRunning(pid)){
                response.put("status", 404);
                response.put("message", "the process is not running");
                return response;
            }
            // 正常终止进程
            ProcessBuilder termProcessBuilder = new ProcessBuilder("sudo", "kill", "-TERM", pid);
            Process termProcess = termProcessBuilder.start();
            termProcess.waitFor();

            // 检查进程是否仍在运行
            if (isProcessRunning(pid)) {
                // 如果进程仍在运行，强制终止进程
                ProcessBuilder killProcessBuilder = new ProcessBuilder("sudo", "kill", "-KILL", pid);
                Process killProcess = killProcessBuilder.start();
                killProcess.waitFor();

                // 再次检查进程是否仍在运行
                if (isProcessRunning(pid)) {
                    response.put("status", 400);
                    response.put("message", "Failed to kill process " + pid);
                    return response;
                }
            }

            // 从数据库中删除任务条目
            taskRepository.deleteTask(pid);

            response.put("status", 200);
            response.put("message", "Process " + pid + " killed and task deleted successfully.");
        } catch (Exception e) {
            response.put("status", 500);
            response.put("message", "An error occurred: " + e.getMessage());
            return response;
        }

        return response;
    }

    private boolean isProcessRunning(String pid) throws Exception {
        ProcessBuilder checkProcessBuilder = new ProcessBuilder("ps", "-p", pid);
        Process checkProcess = checkProcessBuilder.start();
        int exitCode = checkProcess.waitFor();
        return exitCode == 0;
    }
}

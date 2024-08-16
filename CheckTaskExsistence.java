package priv.cgroup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import priv.cgroup.object.Task;
import priv.cgroup.repository.TaskRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

@Service
public class CheckTaskExsistence {
    @Autowired
    private final TaskRepository taskRepository;

    public CheckTaskExsistence(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Scheduled(fixedRate = 30000)
    public void checkTaskExsistence() {
        List<Task> tasks = taskRepository.findAll();

        for (Task task : tasks) {
            String pid = task.getPid();
            if (!isPidRunning(pid)) {
                // 如果PID不存在，则删除该任务条目
                taskRepository.delete(task);
                System.out.println("Deleted task with PID: " + pid);
            }
        }
    }

    // 检查PID是否在运行
    private boolean isPidRunning(String pid) {
        try {
            String[] command = {"ps -p " + pid};
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(pid)) {
                    return true;  // 找到了PID，说明它正在运行
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;  // 如果没有找到PID，说明它没有在运行
    }
}

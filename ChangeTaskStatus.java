package priv.cgroup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import priv.cgroup.repository.TaskRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Service
public class ChangeTaskStatus {

    @Autowired
    private final TaskRepository taskRepository;

    public ChangeTaskStatus(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Map<String, Object> changeTaskStatus(Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<>();

        try{
            boolean open = requestBody.containsKey("open");
            String pid = requestBody.get("pid").toString();
            if(open){
                String[] command = {"sudo", "kill", "-CONT", pid};
                Runtime runtime = Runtime.getRuntime();
                runtime.exec(command);

                response.put("status", 200);
                response.put("message", "Task is opened");
            }else{
                // 获取上一次的执行时间
                String[] command1 = {"sudo", "ps", "-l", "-p", pid};

                // 执行命令
                ProcessBuilder processBuilder = new ProcessBuilder(command1);
                processBuilder.redirectErrorStream(true);  // 将错误流重定向到标准输出流
                Process process = processBuilder.start();

                // 读取命令输出
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                boolean isFirstLine = true;

                String execTime = "";
                while ((line = reader.readLine()) != null) {
                    if (isFirstLine) {
                        // 忽略第一行标题行
                        isFirstLine = false;
                        continue;
                    }

                    // 输出的具体信息
                    System.out.println("Command Output: " + line);

                    // 分割行内容，提取TIME字段
                    String[] fields = line.trim().split("\\s+");
                    execTime = fields[13];  // 根据ps输出的字段顺序获取TIME字段
                }

                // 更新上次执行的时长
                taskRepository.updateExecTime(execTime, pid);

                String[] command2 = {"sudo", "kill", "-STOP", pid};
                Runtime runtime2 = Runtime.getRuntime();
                runtime2.exec(command2);

                response.put("status", 200);
                response.put("message", "Task is stoped");
            }
        }catch(IOException e){
            response.put("status", 500);
            response.put("message", e.getMessage());
        }

        return response;
    }
}

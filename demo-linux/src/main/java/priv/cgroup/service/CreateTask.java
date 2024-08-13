package priv.cgroup.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import priv.cgroup.mapper.TaskMapper;
import priv.cgroup.object.Task;
import priv.cgroup.repository.TaskRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CreateTask {

    @Autowired
    private final TaskRepository taskRepository;

    public CreateTask(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /*
        @param commands 用户输入的命令
     */
    public Map<String, Object> createTask(Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<>();
        String pid;

        List<String> commands = (List<String>) requestBody.get("commands");

        //检查任务是否存在
        if (commands == null || commands.isEmpty()) {
            response.put("status", 400);
            response.put("message", "Command list is empty,please input a valid command");
            response.put("pid", null);
            return response;
        }

        // 检查最后一条命令是否是执行命令
        String finalCommand = commands.get(commands.size() - 1);
        if (!finalCommand.startsWith("./")) {
            response.put("status", 400);
            response.put("message", "The last command must be an execution command starting with './'");
            response.put("pid", null);
            return response;
        }

        //接收用户传入的命令并执行，但需要限制用户的输入，避免用户误操。
        try {
            //进入用户文件目录
            String USERDIR = "/home/kncsz/SysMaster/user/file";
            ProcessBuilder cdProcessBuilder = new ProcessBuilder("bash", "-c", "cd", USERDIR);
            Process cdProcess = cdProcessBuilder.start();
            cdProcess.waitFor();

            // 执行编译命令
            for (int i = 0; i < commands.size() - 1; i++) {
                String command = commands.get(i);
                ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
                Process process = processBuilder.start();
                process.waitFor();
            }

            // 执行最后一个命令，并获取其PID
            finalCommand = commands.get(commands.size() - 1);

            ProcessBuilder finalProcessBuilder = new ProcessBuilder("bash", "-c", finalCommand + "& echo $!");
            Process finalProcess = finalProcessBuilder.start();

            finalProcessBuilder.redirectErrorStream();

            System.out.println(">>>>>>>>>>>>>>>> pid:"+finalProcess.pid());
            // 提取参数作为name
            String name = extractNameFromCommand(finalCommand);

            // 获取PID
            BufferedReader reader = new BufferedReader(new InputStreamReader(finalProcess.getInputStream()));
            pid = reader.readLine().trim();
            finalProcess.waitFor();

            System.out.println(">---------->>>>> pid:"+pid);

            // 创建时间戳
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd E HH:mm:ss");
            String timestamp = now.format(formatter);

            //将任务信息加入数据库
            Task insertedTask = new Task(name, pid, "", timestamp, "default");
            taskRepository.save(insertedTask);

            // 成功结果
            response.put("status", 200);
            response.put("message", "success");
            response.put("pid", pid);

        } catch (Exception e) {
            response.put("status", 500);
            response.put("message", e.getMessage());
            response.put("pid", null);
            return response;
        }
        return response;
    }
    private static String extractNameFromCommand(String command) {
        if (command.startsWith("./")) {
            return command.substring(2); // 去掉"./"并返回后面的部分
        }
        return ""; // 如果不以"./"开头，返回空字符串或你想要的默认值
    }
}

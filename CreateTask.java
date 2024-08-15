package priv.cgroup.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import priv.cgroup.object.Task;
import priv.cgroup.repository.TaskRepository;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

        List<String> commands = (List<String>) requestBody.get("commands");

        // 检查用户是否输入命令
        if (commands == null || commands.isEmpty()) {
            response.put("status", 400);
            response.put("message", "Command list is empty,please input a valid command");
            response.put("pid", null);
            return response;
        }

        // 获取前端参数
        String taskName = Optional.ofNullable(requestBody.get("taskName")).map(Object::toString).orElse("defaultName");
        String cgroupName = Optional.ofNullable(requestBody.get("cgroupName")).map(Object::toString).orElse("defaultCgroupName");
        String cgroupPath = Optional.ofNullable(requestBody.get("cgroupPath")).map(Object::toString).orElse("/default/path");
        String cgroupConfigDir = Optional.ofNullable(requestBody.get("cgroupConfigDir")).map(Object::toString).orElse("/default/config/dir");
        String description = Optional.ofNullable(requestBody.get("description")).map(Object::toString).orElse("defaultDescription");
        String fileName = Optional.ofNullable(requestBody.get("fileName")).map(Object::toString).orElse("");
        String filePath = Optional.ofNullable(requestBody.get("filePath")).map(Object::toString).orElse("");

        // 在数据库中查找是否有同名的taskName，如果有则阻止用户创建
        List<Task> tasks = taskRepository.findByName(taskName);
        boolean isNameConflict = tasks.stream().anyMatch(task -> task.getName().equals(taskName));
        if(isNameConflict) {
            response.put("status", 409);
            response.put("message", "Name conflict");
            return response;
        }

        // 接收用户传入的命令并执行，但需要限制用户的输入，避免用户误操。
        // TODO

        try {
            // 进入用户文件目录
            String parentDir = filePath;

            // 获取命令
            String command = commands.getLast();

            /*
                组装命令，分几种情况
                1.若文件名fileName没有后缀，则执行filePath + File.seperator + fileName
                2.若后缀为.jar,则执行java -jar filePath + File.seperator + fileName
                3.若后缀为。py,则执行python filePath + File.seperator + fileName
                4.若后缀为.sh,则执行bash filePath + File.seperator + fileName
             */
            String fullCommand;
            if (fileName.endsWith(".jar")) {
                fullCommand = "java -jar " + filePath + File.separator + fileName;
            } else if (fileName.endsWith(".py")) {
                fullCommand = "python " + filePath + File.separator + fileName;
            } else if (fileName.endsWith(".sh")) {
                fullCommand = "bash " + filePath + File.separator + fileName;
            } else if (fileName.isEmpty()) {
                fullCommand = command;
            } else {
                fullCommand = filePath + File.separator + fileName;
            }

            // 执行命令
            Runtime runtime = Runtime.getRuntime();
            String[] cmdArray = {fullCommand};
            Process process = runtime.exec(cmdArray);

            // 获取进程pid
            ProcessHandle processHandler = process.toHandle();
            String pid = Long.toString(processHandler.pid());

            // 创建时间戳
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd E HH:mm:ss");
            String timestamp = now.format(formatter);

            // 分为用户指定了加入某个组和没有指定加入某个组
            if(!cgroupPath.equals("/default/path")) {

                ProcessBuilder addToCgroup = new ProcessBuilder("bash", "-c", "echo" + pid + " > " + cgroupPath
                                                                + "/" + "cgroup.procs");
                Process ps = addToCgroup.start();
                int exitCode = ps.waitFor();

                if(exitCode == 0) {
                    File configFile = new File(cgroupConfigDir + "/" + cgroupName + ".json");
                    // 读取现有的配置文件
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode rootNode = mapper.readTree(configFile);
                    ObjectNode tasksNode;

                    // 检查是否存在 tasks 节点，如果不存在则创建
                    if (rootNode.has("tasks")) {
                        tasksNode = (ObjectNode) rootNode;
                    } else {
                        tasksNode = mapper.createObjectNode();
                        ((ObjectNode) rootNode).set("tasks", tasksNode);
                    }

                    // 追加 pid 到 tasks 数组
                    ArrayNode tasksArray;
                    if (tasksNode.has("tasks")) {
                        tasksArray = (ArrayNode) tasksNode.get("tasks");
                    } else {
                        tasksArray = mapper.createArrayNode();
                        tasksNode.set("tasks", tasksArray);
                    }

                    tasksArray.add(pid);

                    // 将修改后的内容写回配置文件
                    mapper.writerWithDefaultPrettyPrinter().writeValue(configFile, rootNode);

                    // 将任务信息加入数据库
                    Task insertedTask = new Task(taskName, pid, cgroupPath, timestamp, description, true, "00:00:00");
                    taskRepository.save(insertedTask);
                }
                // 成功结果
                response.put("status", 200);
                response.put("message", "success");
                response.put("pid", pid);

            }else{
                // 将任务信息加入数据库
                Task insertedTask = new Task(taskName, pid, "", timestamp, description, true, "00:00:00");
                taskRepository.save(insertedTask);
                // 成功结果
                response.put("status", 200);
                response.put("message", "success");
                response.put("pid", pid);
            }

        } catch (Exception e) {
            response.put("status", 500);
            response.put("message", e.getMessage());
            response.put("pid", null);
            return response;
        }
        return response;
    }
}

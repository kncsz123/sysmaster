package priv.cgroup;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
    启动类，开启后初始化配置，然后启动服务
 */
@MapperScan("priv.cgroup.mapper")
@SpringBootApplication
public class App {

    private static final String ROOT_CGROUP_PATH = "/sys/fs/cgroup"; // 根 cgroup 路径
    private static final String USER_CGROUP_PATH = ROOT_CGROUP_PATH + "/user"; // user 子 cgroup 路径
    private static final String USER_FILEDIR_PATH = "/home/kncsz/SysMaster/user/file"; //用kncsz测试，SysMaster改为小写
    private static final String CONFIG_FILE_PATH = "/home/kncsz/SysMaster/conf/user/user.json";
    private static final String CONFIG_FILE_DIR = "/home/kncsz/SysMaster/conf/user";
    private static final String DB_URL = "jdbc:mysql://192.168.81.1:3306/sysmaster";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "lyq20040513?";

    public static void main(String[] args) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // 读取文件内容
            JsonNode rootNode = objectMapper.readTree(Paths.get(CONFIG_FILE_PATH).toFile());

            // 检查文件内容是否为一个空的 JSON 对象, 如果不为空，则说明环境中存在user组，则在环境中删除并删除数据库中的条目，重新配置
            if (rootNode != null && !rootNode.isEmpty()) {
                // 在环境中和数据库中删除user组
                deleteUserCgroup();
            }

            // 创建组
            createUserCgroup();

            // 将user组信息加入数据库
            addUserGroupToDatabase("user", 0);

            // 向配置文件中写入信息
            writeConfigToFile(CONFIG_FILE_PATH);

            // 默认的配置文件路径，运行时不会使用这个，而是linux上配置文件的路径
            String configPath = System.getProperty("config.path", CONFIG_FILE_PATH);

            // 文件操作对象
            File configFile = new File(configPath);

            // java对象序列化为json字符串
            Map<String, Object> config = objectMapper.readValue(configFile, Map.class);

            /*
                获取控制器权限
             */
            Map<String, Object> cgroupConfig = (Map<String, Object>) config.get("cgroup");
            List<String> controllers = (List<String>) cgroupConfig.get("controllers");

            // 将已有权限分配给子组
            openSubtreeControl(controllers);

            // 获取组配置
            Map<String, String> user_settings = (Map<String, String>) config.get("settings");

            // 应用组配置
            applySubgroupSettings(user_settings);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // 开启服务
        SpringApplication.run(App.class, args);
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private static void writeConfigToFile(String configFilePath) {
        // 创建ObjectMapper实例
        ObjectMapper objectMapper = new ObjectMapper();

        // 创建配置数据的Map结构
        Map<String, Object> config = new HashMap<>();
        config.put("name", "user");
        config.put("cgroupPath", "/sys/fs/cgroup/user");
        config.put("cgroupConfigDir", "/home/kncsz/SysMaster/conf/user");
        config.put("hierarchy", 0);

        // 创建cgroup配置
        Map<String, Object> cgroupConfig = new HashMap<>();
        cgroupConfig.put("controllers", List.of("memory", "pids", "io", "cpu", "cpuset", "hugetlb", "rdma", "misc"));
        config.put("cgroup", cgroupConfig);

        // 创建settings配置
        Map<String, String> settingsConfig = new HashMap<>();
        settingsConfig.put("cpu.max", "20000 100000");
        config.put("settings", settingsConfig);

        // 创建一个pretty printer
        ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();

        try {
            // 将JSON写入文件，并以漂亮的格式输出
            writer.writeValue(new File(configFilePath), config);
            System.out.println("Configuration successfully written to " + configFilePath);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error writing configuration to file.");
        }
    }

    // 开机自启
    private static void enableAutoStart() {
        String scriptPath = "/path/to/your/script.sh";
        String serviceName = "my-startup-script";

        String serviceFileContent = String.format(
                "[Unit]\n" +
                        "Description=Run my startup script\n" +
                        "After=network.target\n\n" +
                        "[Service]\n" +
                        "ExecStart=%s\n" +
                        "Type=simple\n" +
                        "User=root\n\n" +
                        "[Install]\n" +
                        "WantedBy=multi-user.target\n", scriptPath);

        try {
            String serviceFilePath = "/etc/systemd/system/" + serviceName + ".service";
            Files.write(Paths.get(serviceFilePath), serviceFileContent.getBytes());

            executeCommand("sudo systemctl enable " + serviceName);
            executeCommand("sudo systemctl start " + serviceName);

            System.out.println("Auto-start configured successfully.");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 设置root密码
    private static void setRootPassword() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("sudo", "passwd", "root");
            Process process = processBuilder.start();

            // 输入三次密码
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
                writer.write("lyq20040513\n");
                writer.flush();
                writer.write("lyq20040513\n");
                writer.flush();
                writer.write("lyq20040513\n");
                writer.flush();
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Root password set successfully.");
            } else {
                System.err.println("Failed to set root password.");
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 进入root
    private static void goIntoRoot() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("sudo", "su", "-");
            Process process = processBuilder.start();

            // 输入一次密码
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
                writer.write("lyq20040513\n");
                writer.flush();
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Switched to root user.");
            } else {
                System.err.println("Failed to switch to root user.");
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 为子组开启控制器权限
    private static void openSubtreeControl(List<String> controllers) {
        String cgroupPath = ROOT_CGROUP_PATH + "/cgroup.subtree_control";
        for (String controller : controllers) {
            String command = String.format("echo +%s > %s", controller, cgroupPath);
            try {
                ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
                Process process = processBuilder.start();
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    System.out.println("Controller " + controller + " written to " + cgroupPath);
                } else {
                    System.err.println("Failed to write controller " + controller + " to " + cgroupPath);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // 创建用户根组
    private static void createUserCgroup() {
        try {
            Process mkdirProcess = new ProcessBuilder("sudo", "mkdir", "-p", USER_CGROUP_PATH).start();
            mkdirProcess.waitFor();
            System.out.println("User cgroup created at " + USER_CGROUP_PATH);

            Process mkdirFileDirProcess = new ProcessBuilder("mkdir", "-p", USER_FILEDIR_PATH).start();
            mkdirFileDirProcess.waitFor();
            System.out.println("User file directory created at " + USER_FILEDIR_PATH);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 应用组配置
    private static void applySubgroupSettings(Map<String, String> settings) {
        try {
            for (Map.Entry<String, String> entry : settings.entrySet()) {
                String controllerFile = entry.getKey();
                String value = entry.getValue();
                String filePath = USER_CGROUP_PATH + "/" + controllerFile;

                String command = String.format("echo %s > %s", value, filePath);
                executeCommand(command);
            }
            System.out.println("Settings applied to " + USER_CGROUP_PATH);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 执行命令
    private static void executeCommand(String command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
        Process process = processBuilder.start();
        int exitCode = process.waitFor();
        if (exitCode == 0) {
            System.out.println("Command executed successfully");
        } else {
            System.err.println("Failed to execute command: " + command);
        }
    }

    // 删除user组
    private static void deleteUserCgroup() throws Exception {
        String sql = "DELETE FROM sysmaster.cgroup WHERE cgroup_path LIKE '/sys/fs/cgroup/user%'";

        // 环境中删除user组
        try {
            Process rmdirProcess = new ProcessBuilder("sudo", "rmdir", USER_CGROUP_PATH).start();
            rmdirProcess.waitFor();
            System.out.println("User cgroup deleted at " + USER_CGROUP_PATH);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        // 递归地删除user文件夹下除user.json以外的所有文件，删除配置文件user.json中的信息（不删除文件，只删除里面的内容）
        deleteDirectory(new File(CONFIG_FILE_DIR));
        String configPath = System.getProperty("config.path", CONFIG_FILE_PATH);
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(configPath))) {
            writer.write("{}"); // 写入空的JSON对象
            System.out.println("Cleared content of user.json");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to clear content of user.json");
        }

        // 数据库中删除组条目
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.executeUpdate();

            System.out.println("User group deleted from database");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 添加user组到数据库
    private static void addUserGroupToDatabase(String groupName, int hierarchy) {
        // 获取创建时间
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd E HH:mm:ss");
        String datestamp = now.format(formatter);

        String sql = "INSERT INTO sysmaster.cgroup (name, hierarchy, datestamp, description, cgroup_path, cgroup_config_dir, " +
                "cpu_controller_status, memory_controller_status, cpuset_controller_status, io_controller_status, pids_controller_status," +
                "hugetlb_controller_status, misc_controller_status, rdma_controller_status," +
                "is_cpu_controller_status, is_cpuset_controller_status, is_io_controller_status, is_pids_controller_status, is_memory_controller_status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            Byte bytevalue = 1;
            statement.setString(1, groupName);
            statement.setInt(2, hierarchy);
            statement.setString(3, datestamp);
            statement.setString(4, "default");
            statement.setString(5, USER_CGROUP_PATH);
            statement.setString(6, "/home/kncsz/SysMaster/conf/user");
            statement.setBoolean(7, true);
            statement.setBoolean(8, true);
            statement.setBoolean(9, true);
            statement.setBoolean(10, true);
            statement.setBoolean(11, true);
            statement.setBoolean(12, true);
            statement.setBoolean(13, true);
            statement.setBoolean(14, true);
            statement.setByte(15, bytevalue);
            statement.setByte(16, bytevalue);
            statement.setByte(17, bytevalue);
            statement.setByte(18, bytevalue);
            statement.setByte(19, bytevalue);
            statement.executeUpdate();

            System.out.println("User group added to database");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 服务停止时自动执行
    @PreDestroy
    public void deleteUserGroupFromDatabase() throws Exception {
        String sql = "DELETE FROM sysmaster.cgroup WHERE cgroup_path LIKE '/sys/fs/cgroup/user%'";

        // 环境中删除user组
        try {
            Process rmdirProcess = new ProcessBuilder("sudo", "rmdir", USER_CGROUP_PATH).start();
            rmdirProcess.waitFor();
            System.out.println("User cgroup deleted at " + USER_CGROUP_PATH);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        // 递归地删除user文件夹下除user.json以外的所有文件，删除配置文件user.json中的信息（不删除文件，只删除里面的内容）
        deleteDirectory(new File(CONFIG_FILE_DIR));
        String configPath = System.getProperty("config.path", CONFIG_FILE_PATH);
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(configPath))) {
            writer.write("{}"); // 写入空的JSON对象
            System.out.println("Cleared content of user.json");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to clear content of user.json");
        }

        // 数据库中删除组条目
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.executeUpdate();
            System.out.println("User group deleted from database");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void deleteDirectory(File directory) throws Exception {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    // 跳过删除名为 user.json 的文件
                    if (file.isFile() && file.getName().equals("user.json")) {
                        continue;
                    }
                    deleteDirectory(file); // 递归删除子文件或子目录
                }
            }
        }

        // 如果当前目录不是 user.json 的父目录或祖先目录，则删除目录
        if (!isParentOfUserJson(directory)) {
            if (!directory.delete()) {
                throw new Exception("Failed to delete directory: " + directory.getAbsolutePath());
            }
        }
    }

    // 检查是否是 user.json 的父目录或祖先目录
    private static boolean isParentOfUserJson(File directory) {
        File userJsonFile = new File(CONFIG_FILE_PATH);
        File parent = userJsonFile.getParentFile();

        while (parent != null) {
            if (parent.equals(directory)) {
                return true; // 当前目录是 user.json 的父目录或祖先目录
            }
            parent = parent.getParentFile();
        }
        return false; // 当前目录不是 user.json 的父目录或祖先目录
    }
}

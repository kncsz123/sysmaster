package priv.cgroup;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/*
    启动类，开启后初始化配置，然后启动服务
 */
//@springBootApplication的默认扫描范围只是在启动类（即被@springbootApplication注解的类）所在的包及其子包
@MapperScan("priv.cgroup.mapper")
@SpringBootApplication
public class App {

    //获取用户名
    //String username = userService.getUserName();
    private static final String ROOT_CGROUP_PATH = "/sys/fs/cgroup"; // 根 cgroup 路径
    private static final String USER_CGROUP_PATH = ROOT_CGROUP_PATH + "/user"; // user 子 cgroup 路径
    private static final String USER_FILEDIR_PATH = "/home/kncsz/SysMaster/user/file"; //用kncsz测试，SysMaster改为小写

    public static void main(String[] args) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            //将开发环境的配置文件路径映射到生产环境
            String configPath = System.getProperty("config.path", "src/main/resources/conf/user.json");
            File configFile = new File(configPath);
            /*
                配置文件里面不能有注释，否则无法解析
             */
            Map<String, Object> config = objectMapper.readValue(configFile, Map.class);

            /*
                如果检查到环境中有 user 组，那么删除组，读取配置，然后创建组。
             */
            if (Files.exists(Paths.get(USER_CGROUP_PATH))) {
                // 删除 user 组
                deleteUserCgroup();
            }

            // 读取根cgroup需要打开的控制器
            Map<String, Object> cgroupConfig = (Map<String, Object>) config.get("cgroup");
            List<String> controllers = (List<String>) cgroupConfig.get("controllers");
            writeToSubtreeControl(controllers);

            // 创建并配置 user 子 cgroup
            createUserCgroup();

            // 读取 user cgroup 配置
            Map<String, String> user_settings = (Map<String, String>) cgroupConfig.get("settings");
            applySubgroupSettings(user_settings);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SpringApplication.run(App.class, args);
    }

    // 设置开机自启,执行shell脚本
    // 设置开机自启,执行shell脚本
    private static void enableAutoStart() {
        String scriptPath = "/path/to/your/script.sh"; // 替换为你的Shell脚本路径
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
            // 将 service 文件内容写入 /etc/systemd/system/ 下的 .service 文件
            String serviceFilePath = "/etc/systemd/system/" + serviceName + ".service";
            Files.write(Paths.get(serviceFilePath), serviceFileContent.getBytes());

            // 设置服务开机自启并立即启动
            executeCommand("sudo systemctl enable " + serviceName);
            executeCommand("sudo systemctl start " + serviceName);

            System.out.println("Auto-start configured successfully.");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    /*
    我要在程序里面启动时设置 root 密码，但这只能在 root 权限下操作，由于开机启动时不在 root 下，
    root 密码每次开机又是随机生成的，需要用户手动设置 root 密码之后才能进入 root
     */

    // 设置 root 密码
    private static void setRoot() {
        // 设置 root 密码相关代码
    }

    // 进入 root
    private static void goIntoRoot() {
        // 进入 root 相关代码
    }

    // 初始化根 cgroup 配置，默认所有控制器向后代开放
    private static void writeToSubtreeControl(List<String> controllers) {
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

    // 为用户初始化一个 user cgroup，它是根 cgroup 的孩子，用户将在 user 下管理其创建的 cgroup
    private static void createUserCgroup() {
        try {
            // 创建 user 子 cgroup
            Process mkdirProcess = new ProcessBuilder("sudo", "mkdir", "-p", USER_CGROUP_PATH).start();
            mkdirProcess.waitFor();

            // 确认 user 子 cgroup 创建成功
            System.out.println("User cgroup created at " + USER_CGROUP_PATH);

            // 创建用户文件夹，若已创建，则会跳过此步骤
            Process mkdirFileDirProcess = new ProcessBuilder("mkdir", "-p", USER_FILEDIR_PATH).start();
            mkdirFileDirProcess.waitFor();

            // 确认用户文件夹创建成功
            System.out.println("User file directory created at " + USER_FILEDIR_PATH);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

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

    private static void executeCommand(String command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
        Process process = processBuilder.start();
        int exitCode = process.waitFor();
        if (exitCode == 0) {
            System.out.println("Command executed successfully");
        } else {
            System.err.println("Failed to execute command " + command + " to " + USER_CGROUP_PATH);
        }
    }

    private static void deleteUserCgroup() {
        try {
            // 删除 user cgroup 目录
            Process rmdirProcess = new ProcessBuilder("sudo", "rmdir", USER_CGROUP_PATH).start();
            rmdirProcess.waitFor();

            // 确认 user 子 cgroup 删除成功
            System.out.println("User cgroup deleted at " + USER_CGROUP_PATH);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

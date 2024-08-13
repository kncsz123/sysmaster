package priv.cgroup.mapper;


import org.apache.ibatis.annotations.Mapper;
import priv.cgroup.object.File;

import java.util.List;

@Mapper
public interface FileMapper {
    public File selectFile(String path);

    public void insertFile(File file);

    public void deleteFile(String path);

    public List<File> selectAllFile();
}

package priv.cgroup.service;

import cn.hutool.core.util.StrUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import priv.cgroup.mapper.CgroupMapper;
import priv.cgroup.object.Cgroup;
import priv.cgroup.repository.CgroupRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SelectCgroup {
    @Autowired
    private final CgroupRepository cgroupRepository;

    public SelectCgroup(CgroupRepository cgroupRepository) {
        this.cgroupRepository = cgroupRepository;
    }

    /*
        此方法无请求参数
     */
    public Map<String, Object> selectCgroup(){
        Map<String, Object> response = new HashMap<String, Object>();
        try{
            List<Cgroup> selectedCgroup = cgroupRepository.findAll();
            if(!StrUtil.isEmptyIfStr(selectedCgroup)){
                response.put("status", 200);
                response.put("message", "success");
                response.put("cgroup", selectedCgroup);
            }else{
                response.put("status", 400);
                response.put("message", "error");
                response.put("cgroup", null);
            }
        }catch (Exception e){
            response.put("status",500);
            response.put("message",e.getMessage());
            response.put("cgroup", null);
            return response;
        }

        return response;
    }

}

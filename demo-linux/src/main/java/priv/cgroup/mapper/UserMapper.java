package priv.cgroup.mapper;

import priv.cgroup.object.User;

public interface UserMapper {
    public User selectUser(String id);
}

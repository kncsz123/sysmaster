package priv.cgroup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import priv.cgroup.object.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
}

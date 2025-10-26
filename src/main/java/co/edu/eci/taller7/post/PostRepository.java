package co.edu.eci.taller7.post;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByStreamNameOrderByCreatedAtDesc(String streamName);
}
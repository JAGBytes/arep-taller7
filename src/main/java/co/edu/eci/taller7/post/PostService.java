package co.edu.eci.taller7.post;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import co.edu.eci.taller7.post.Post;
import co.edu.eci.taller7.stream.StreamService;
import co.edu.eci.taller7.user.UserService;
import co.edu.eci.taller7.post.PostRepository;
import co.edu.eci.taller7.user.User;
import co.edu.eci.taller7.stream.Stream;

@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private StreamService streamService;

    public List<Post> getStreamPosts(String streamName) {
        return postRepository.findByStreamNameOrderByCreatedAtDesc(streamName);
    }

    public Post createPost(String content, Long userId, String streamName) {
        if (content.length() > 140) {
            throw new IllegalArgumentException("Máximo 140 caracteres");
        }

        User user = userService.getUserByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no existe"));

        Stream stream = streamService.getOrCreateStream(streamName);

        Post post = new Post();
        post.setContent(content);
        post.setUser(user);
        post.setStream(stream);

        return postRepository.save(post);
    }
}

package co.edu.eci.taller7.post;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    @Autowired
    private PostService postService;

    @GetMapping
    public List<Post> getStream(@RequestParam(defaultValue = "global") String stream) {
        return postService.getStreamPosts(stream);
    }

    @PostMapping
    public Post createPost(@RequestParam String content,
            @RequestParam Long userId,
            @RequestParam(defaultValue = "global") String stream) {
        return postService.createPost(content, userId, stream);
    }
}

package co.edu.eci.taller7.post;

import co.edu.eci.taller7.stream.Stream;
import co.edu.eci.taller7.user.User;
import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "posts")
@Data
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 140)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stream_id", nullable = false)
    @JsonIgnore
    private Stream stream;

    @Column(name = "created_at")
    private Date createdAt = new Date();
}
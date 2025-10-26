package co.edu.eci.taller7.stream;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StreamRepository extends JpaRepository<Stream, Long> {
    Optional<Stream> findByName(String name);
}
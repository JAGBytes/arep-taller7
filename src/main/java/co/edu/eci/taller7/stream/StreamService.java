package co.edu.eci.taller7.stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class StreamService {

    @Autowired
    private StreamRepository streamRepository;

    public Stream getOrCreateStream(String name) {
        Optional<Stream> stream = streamRepository.findByName(name);
        if (stream.isPresent()) {
            return stream.get();
        } else {
            Stream newStream = new Stream();
            newStream.setName(name);
            return streamRepository.save(newStream);
        }
    }
}
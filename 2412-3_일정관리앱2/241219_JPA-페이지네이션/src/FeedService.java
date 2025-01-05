import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedService {

	private final FeedRepository feedRepository;

	public List<Feed> findAllFeed() {
		return feedRepository.findAll();
	}

	public Page<Feed> findAllFeed(Pageable pageable) {
		return feedRepository.findAll(pageable);
	}
}

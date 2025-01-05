import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/page")
public class FeedController {
	private final FeedService feedService;

	@GetMapping
	public ResponseEntity<List<Feed>> findAllFeed() {
		List<Feed> feedList = feedService.findAllFeed();

		return ResponseEntity.ok(feedList);
	}

	@GetMapping("/v1")
	public ResponseEntity<Page<Feed>> findAllFeed(
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size
	) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
		Page<Feed> feedPage = feedService.findAllFeed(pageable);
		return ResponseEntity.ok(feedPage);
	}


	@GetMapping("/v2")
	public ResponseEntity<Page<Feed>> findAllFeed(
		@PageableDefault(size = 10, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable
	) {
		Page<Feed> feedsPage = feedService.findAllFeed(pageable);

		return ResponseEntity.ok(feedsPage);
	}
}

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService {

	private static final String CACHE_NAME = "carts";
	private final CacheManager cacheManager;
	private final MenuRepository menuRepository;

	@Cacheable(value = CACHE_NAME, key = "#authUser.id")
	public Cart getCart(AuthUser authUser) {
		return new Cart();
	}

	@CachePut(value = CACHE_NAME, key = "#authUser.id")
	public Cart addItemToCart(AuthUser authUser, Long menuId) {
		Cart cart = getOrCreateCart(authUser.id());
		Menu menu = menuRepository.findById(menuId)
			.orElseThrow(() -> new InvalidRequestException(MENU_NOT_FOUND));
		cart.addItem(menuId, menu.getShop().getId());

		return cart;
	}

	@CachePut(value = CACHE_NAME, key = "#authUser.id")
	public Cart removeItemFromCart(AuthUser authUser, Long menuId) {
		Cart cart = getOrCreateCart(authUser.id());
		cart.removeItem(menuId);

		return cart;
	}

	// 캐시에서 삭제
	@CacheEvict(value = CACHE_NAME, key = "#authUser.id")
	public void clearCart(AuthUser authUser) {
	}

	private Cart getOrCreateCart(Long userId) {
		Cache cache = cacheManager.getCache(CACHE_NAME);
		if (cache == null) {
			throw new ServerException(CACHE_CONFIGURATION_ERROR);
		}

		return Optional.ofNullable(cache.get(userId, Cart.class)).orElseGet(Cart::new);
	}
}

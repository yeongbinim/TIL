import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/carts")
public class CartController {

	private final CartService cartService;

	@GetMapping("/items")
	public ResponseEntity<Cart> getCart(@Auth AuthUser authUser) {
		Cart cart = cartService.getCart(authUser);

		return ResponseEntity.ok(cart);
	}

	@PostMapping("/items/{menuId}")
	public ResponseEntity<Cart> addItemToCart(
		@Auth AuthUser authUser,
		@PathVariable Long menuId
	) {
		Cart cart = cartService.addItemToCart(authUser, menuId);

		return ResponseEntity.ok(cart);
	}

	@DeleteMapping("/items/{menuId}")
	public ResponseEntity<Cart> removeItemFromCart(
		@Auth AuthUser authUser,
		@PathVariable Long menuId
	) {
		Cart cart = cartService.removeItemFromCart(authUser, menuId);

		return ResponseEntity.ok(cart);
	}

	@DeleteMapping("/items")
	public ResponseEntity<Void> clearCart(
		@Auth AuthUser authUser
	) {
		cartService.clearCart(authUser);

		return ResponseEntity.noContent().build();
	}
}

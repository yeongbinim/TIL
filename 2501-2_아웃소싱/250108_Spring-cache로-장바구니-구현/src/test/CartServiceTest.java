import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

	@Mock
	private CacheManager cacheManager;

	@Mock
	private MenuRepository menuRepository;

	@Mock
	private Cache cache;

	@InjectMocks
	private CartService cartService;

	@BeforeEach
	void setUp() {
		when(cacheManager.getCache("carts")).thenReturn(cache);
	}

	@Nested
	@DisplayName("장바구니 아이템 추가 테스트")
	class AddItemToCartTest {

		@Test
		@DisplayName("정상적으로 아이템을 하나 추가한다")
		void addItemToCart_Success1() {
			//given
			Long userId = 1L;
			Long shopId = 1L;
			Long menuId = 1L;

			Shop shop = new Shop();
			ReflectionTestUtils.setField(shop, "id", shopId);
			Menu menu = new Menu();
			ReflectionTestUtils.setField(menu, "id", menuId);
			ReflectionTestUtils.setField(menu, "shop", shop);

			given(cache.get(userId, Cart.class)).willReturn(new Cart());
			given(menuRepository.findById(menuId)).willReturn(Optional.of(menu));

			//when
			Cart updatedCart = cartService.addItemToCart(new AuthUser(userId, null, null), menuId);

			//then
			assertThat(updatedCart).isNotNull();
			assertThat(updatedCart.getItems()).hasSize(1);
			assertThat(updatedCart.getItems().get(0).getMenuId()).isEqualTo(menuId);
			assertThat(updatedCart.getRecentShopId()).isEqualTo(shopId);
		}

		@Test
		@DisplayName("장바구니에 같은 아이템을 추가하면 수량이 늘어난다")
		void addItemToCart_Success2() {
			//given
			Long userId = 1L;
			Long shopId = 1L;
			Long menuId = 1L;

			Shop shop = new Shop();
			ReflectionTestUtils.setField(shop, "id", shopId);
			Menu menu = new Menu();
			ReflectionTestUtils.setField(menu, "id", menuId);
			ReflectionTestUtils.setField(menu, "shop", shop);

			Cart cart = new Cart();
			cart.addItem(menuId, shopId);
			given(cache.get(userId, Cart.class)).willReturn(cart);
			given(menuRepository.findById(menuId)).willReturn(Optional.of(menu));

			//when
			Cart updatedCart = cartService.addItemToCart(new AuthUser(userId, null, null), menuId);

			//then
			assertThat(updatedCart).isNotNull();
			assertThat(updatedCart.getItems()).hasSize(1);
			assertThat(updatedCart.getItems().get(0).getQuantity()).isEqualTo(2);
			assertThat(updatedCart.getRecentShopId()).isEqualTo(shopId);
		}

		@Test
		@DisplayName("중간에 다른 shop의 물건 추가되면 장바구니가 처음부터 쌓인다")
		void addItemToCart_Success3() {
			//given
			Long userId = 1L;
			Long shopId1 = 1L;
			Long shopId2 = 2L;
			Long menuId1 = 1L;
			Long menuId2 = 2L;

			Shop shop1 = new Shop();
			ReflectionTestUtils.setField(shop1, "id", shopId1);
			Menu menu1 = new Menu();
			ReflectionTestUtils.setField(menu1, "id", menuId1);
			ReflectionTestUtils.setField(menu1, "shop", shop1);

			Shop shop2 = new Shop();
			ReflectionTestUtils.setField(shop2, "id", shopId2);
			Menu menu2 = new Menu();
			ReflectionTestUtils.setField(menu2, "id", menuId1);
			ReflectionTestUtils.setField(menu2, "shop", shop2);

			Cart cart = new Cart();
			cart.addItem(menuId1, shopId1);
			given(cache.get(userId, Cart.class)).willReturn(cart);
			given(menuRepository.findById(menuId2)).willReturn(Optional.of(menu2));

			//when
			Cart updatedCart = cartService.addItemToCart(new AuthUser(1L, null, null), menuId2);

			//then
			assertThat(updatedCart).isNotNull();
			assertThat(updatedCart.getItems()).hasSize(1);
			assertThat(updatedCart.getItems().get(0).getMenuId()).isEqualTo(menuId2);
			assertThat(updatedCart.getItems().get(0).getQuantity()).isEqualTo(1);
			assertThat(updatedCart.getRecentShopId()).isEqualTo(shopId2);
		}

		@Test
		@DisplayName("예외: 존재하지 않는 menu를 추가하려 할 때 예외발생")
		void addItemToCart_Exception1() {
			//given
			Long userId = 1L;
			Long shopId = 1L;
			Long menuId = 1L;
			AuthUser authUser = new AuthUser(userId, null, null);

			Shop shop = new Shop();
			ReflectionTestUtils.setField(shop, "id", shopId);

			given(cache.get(userId, Cart.class)).willReturn(new Cart());
			given(menuRepository.findById(menuId)).willReturn(Optional.empty());

			//when & then
			assertThatThrownBy(() -> cartService.addItemToCart(authUser, menuId))
				.isInstanceOf(InvalidRequestException.class)
				.hasMessageContaining(ErrorCode.MENU_NOT_FOUND.getMessage());
		}
	}

	@Nested
	@DisplayName("장바구니 아이템 제거 테스트")
	class RemoveItemFromCartTest {

		@Test
		@DisplayName("정상적으로 아이템을 제거한다 (2개 -> 1개)")
		void removeItemFromCart_Success1() {
			//given
			Long menuId = 1L;
			Long shopId = 1L;
			AuthUser authUser = new AuthUser(1L, null, null);
			Cart cart = new Cart();

			cart.addItem(menuId, shopId);
			cart.addItem(menuId, shopId); // 같은 아이템을 두 번 추가

			given(cache.get(authUser.id(), Cart.class)).willReturn(cart);

			//when
			Cart updatedCart = cartService.removeItemFromCart(authUser, menuId);

			//then
			assertThat(updatedCart).isNotNull();
			assertThat(updatedCart.getItems()).hasSize(1);
			assertThat(updatedCart.getItems().get(0).getMenuId()).isEqualTo(menuId);
			assertThat(updatedCart.getItems().get(0).getQuantity()).isEqualTo(1);
			assertThat(updatedCart.getRecentShopId()).isEqualTo(shopId);
		}

		@Test
		@DisplayName("아이템을 제거할때 마지막 아이템이면 장바구니에서 제거한다.")
		void removeItemFromCart_Success2() {
			//given
			Long menuId = 1L;
			Long shopId = 1L;
			AuthUser authUser = new AuthUser(1L, null, null);
			Cart cart = new Cart();

			cart.addItem(menuId, shopId);

			given(cache.get(authUser.id(), Cart.class)).willReturn(cart);

			//when
			Cart updatedCart = cartService.removeItemFromCart(authUser, menuId);

			//then
			assertThat(updatedCart).isNotNull();
			assertThat(updatedCart.getItems()).isEmpty();
		}

		@Test
		@DisplayName("예외: 존재하지 않는 아이템을 제거하려 할 때 예외 발생")
		void removeItemFromCart_Exception() {
			//given
			Long menuId = 1L;
			AuthUser authUser = new AuthUser(1L, null, null);
			Cart cart = new Cart(); // 아무 아이템도 추가되지 않은 상태

			given(cache.get(authUser.id(), Cart.class)).willReturn(cart);

			//when & then
			assertThatThrownBy(() -> cartService.removeItemFromCart(authUser, menuId))
				.isInstanceOf(InvalidRequestException.class)
				.hasMessageContaining(CART_ITEM_NOT_FOUND.getMessage());
		}
	}
}

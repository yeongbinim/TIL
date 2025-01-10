# 스프링 캐시에서 내부 호출

장바구니에 물건을 추가하고 빼고 조회하고 비우는 기능을 개발했는데, 자꾸 장바구니에 하나의 물건밖에 들어가지 않는 것을 확인했다.

<br/>

```java
private static final String CACHE_NAME = "carts";

@Cacheable(value = CACHE_NAME, key = "#authUser.id")
public Cart getCart(AuthUser authUser) {
  return new Cart();
}

@CachePut(value = CACHE_NAME, key = "#authUser.id")
public Cart addItemToCart(AuthUser authUser, Long menuId) {
  Cart cart = getCart(authUser);
  Menu menu = menuRepository.findById(menuId)
      .orElseThrow(() -> new InvalidRequestException(MENU_NOT_FOUND));
  cart.addItem(menuId, menu.getShop().getId());

  return cart;
}

@CachePut(value = CACHE_NAME, key = "#authUser.id")
public Cart removeItemFromCart(AuthUser authUser, Long menuId) {
  Cart cart = getCart(authUser);
  cart.removeItem(menuId);

  return cart;
}

// 캐시에서 삭제
@CacheEvict(value = CACHE_NAME, key = "#authUser.id")
public void clearCart(AuthUser authUser) {
}
```

기존에 작성했던 기능인데, 

"`@Cacheable` 애노테이션은 메서드의 첫 호출 시에는 메서드 실행 후에 결과값을 캐시에 저장하고, 이후 같은 요청이 들어오면 캐시된 결과를 반환하여 메서드 호출 없이 처리하도록 한다" 

까지만 알고 있는 상태라서 다른 메서드에서도 이 메서드를 호출하면 해당 기능대로 캐시된 결과를 반환할 줄 알았던 것이다.

<img width="700" src="https://github.com/user-attachments/assets/a887030d-15de-44b0-9534-cf68f8d9cffb" />

하지만 위의 경고를 보면 '@Cachealbe 애너테이션은 런타임에 무시되는데 너는 어찌 내부호출을 사용하려하니' 하고 말해주고 있다.

그렇다. 저 스프링 캐시는 프록시 기반이었던 것이다.

아 더 정확히는 스프링 AOP를 통해서 프록시 객체를 생성한 것이다.

아마도 `@Around` 어드바이스를 통해서 아래처럼 구현되어 있지 않을까?

```java
private final CacheManager cachemanager;

@Around("Caheable 애노테이션 찾는 포인트컷")
public Object doTransaction(ProceedingJoinPoint joinPoint) throws Throwable {
  Cache cache = cachemanager에서 name에 해당하는 캐시 찾기
  Cart cart = cache에서 key에 해당하는 Cart찾기
  if (cache == null) {
    캐시에 값 추가하기
    return joinPoint.proceed();
  }
  else () {
    return cache.get(cacheKey);
  }
}
```

대충 내부적으로 이러지 않을까 의사코드 적어봤다.

여튼 이 문제를 해결하기 위해서

1. 자기 자신 주입
2. 서비스 클래스 분리

두 가지를 고려했지만 둘 다 별로 맘에 들지 않았다.

채영님한테 고민을 털어놨더니 저 캐시매니저에 직접 조회하면 되는거 아니냐고, 그게 가장 간단해보인다고 말씀해 주셔서

<br/>

아! 그러면 되는구나 하고 아래처럼 변경했다.

```java
private static final String CACHE_NAME = "carts";
private final CacheManager cacheManager;

@CachePut(value = CACHE_NAME, key = "#authUser.id")
public Cart addItemToCart(AuthUser authUser, Long menuId) {
  Cart cart = getOrCreateCart(authUser.id());
  // 기존 로직
  return cart;
}

private Cart getOrCreateCart(Long userId) {
  Cache cache = cacheManager.getCache(CACHE_NAME);
  if (cache == null) {
    throw new ServerException(CACHE_CONFIGURATION_ERROR);
  }

  return Optional.ofNullable(cache.get(userId, Cart.class)).orElseGet(Cart::new);
}
```

왜 애너테이션으로 모든 걸 해결하려 했을까... 애너테이션 너무 편리해서 가끔 생각이 짧아지는 것 같다.

여튼 이후 장바구니에 값이 정상적으로 추가되고 삭제되는 것을 확인할 수 있었다.

# Spring cache로 장바구니 구현

장바구니 관련 기능을 개발하고 있는데, 이 장바구니 테이블이 실제로 존재하는 것이 아니라 메모리에 저장해두고, 값을 조회하는 방식으로 개발하려 한다.

여러 서버를 두고 로드밸런싱 중이라면 Redis와 같은 Inmemory DB를 사용했겠지만(이를 글로벌캐시라고 한다), 현재 단일 서버에서 실습 중이기에 Spring Cache (로컬 캐시)를 사용하기로 했다.

(처음에는 세션 사용을 고려했지만, 그럴경우 사용자 ID에 해당하는 장바구니를 관리하기 어려웠다.)

### 목차

- [cache 사용을 위한 작업](#cache-사용을-위한-작업)
- [장바구니 조회하기](#장바구니-조회하기)
- [장바구니 업데이트하고, 비우기](#장바구니-업데이트하고-비우기)
- [캐시 만료 시간 설정하기](#캐시-만료-시간-설정하기)


<br/>

### cache 사용을 위한 작업

```
implementation 'org.springframework.boot:spring-boot-starter-cache
```

먼저 위의 의존성을 추가해주었고,

```java
@EnableCaching
@Configuration
public class CachingConfig {
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager();
    }
}
```

먼저 캐시 활성화를 위한 애너테이션인 `@EnableCaching`을 선언 해야한다. spring boot가 기본으로 `ConcurrentMapCacheManager`를 사용하기 때문에 위의 빈 등록은 하지 않아도 된다.

<br/>

### 장바구니 조회하기

```java
@Cacheable(value = CACHE_NAME, key = "#authUser.id")
public Cart getCart(AuthUser authUser) {
  return new Cart();
}
```

`@Cacheable` 애노테이션은 메서드의 첫 호출 시에는 메서드 실행 후에 결과값을 캐시에 저장하고, 이후 같은 요청이 들어오면 캐시된 결과를 반환하여 메서드 호출 없이 처리하도록 한다.

value에 어떤 이름의 캐시인지 지정하고(영역을 구분짓기 위함), key에 해당 영역에서 조회하고자 하는 key값을 전달한다.

key값을 전달할 때에는 SpEL(Spring Expression Language)을 사용하는데, authUser 파라미터로부터 id 속성을 추출하도록 설정 했다.

```java
private final CacheManager cacheManager;

private Cart getOrCreateCart(Long userId) {
  Cache cache = cacheManager.getCache(CACHE_NAME);
  if (cache == null) {
    throw new ServerException(CACHE_CONFIGURATION_ERROR);
  }

  return Optional.ofNullable(cache.get(userId, Cart.class)).orElseGet(Cart::new);
}
```

그리고 앞으로 같은 서비스 내의 다른 메서드에서 사용할 함수를 이렇게 만들어 두었는데, 프록시 객체가 생성되어도 내부적으로 getCart() 메서드를 호출하면 캐시된 데이터를 불러올 수 없기 때문이었다.

따라서 cacheManager로부터 캐시를 불러와 key로 꺼내는 함수를 만든 것이다.

<br/>

### 장바구니 업데이트하고, 비우기

위의 getOrCreateCart 함수를 사용하여 장바구니를 업데이트하는 함수를 만들었다.

```java
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
```

`@CachePut` 에너테이션도 마찬가지로 메서드가 반환하는 값을 지정된 캐시에 저장하게끔 처리하지만, 메서드가 호출될 때마다 반드시 실행되며, 캐시 업데이트 용도로 쓰인다.

이외는 `@Cacheable`과 동일했다.

이어서 장바구니를 비우는 함수를 보면

```java
@CacheEvict(value = CACHE_NAME, key = "#authUser.id")
public void clearCart(AuthUser authUser) {
}
```

`@CacheEvict`로 해당 캐시를 제거해준다.

<br/>

### 캐시 만료 시간 설정하기

SpringBoot가 기본으로 사용하는 `ConcurrentMapCacheManager`는 만료 정책을 설정하는 걸 지원하지 않아서 Caffeine 캐싱 라이브러리를 사용하여 `CaffeineCacheManager` 를 사용하도록 변경해주어야 한다.

```
implementation 'com.github.ben-manes.caffeine:caffeine'
```

위와같이 의존성을 추가해주고

```java
@Configuration
@EnableCaching
public class CacheConfig {

	@Bean
	public CacheManager cacheManager() {
		CaffeineCacheManager cacheManager = new CaffeineCacheManager();
		cacheManager.setCaffeine(Caffeine.newBuilder()
			.expireAfterWrite(1, TimeUnit.DAYS)
			.recordStats());
		return cacheManager;
	}
}

```

그리고 위와같이 설정해주면 끝이다! 아주 편리!!

아래는 참고 자료

[스프링 캐시 기본 사용법](https://wildeveloperetrain.tistory.com/119)
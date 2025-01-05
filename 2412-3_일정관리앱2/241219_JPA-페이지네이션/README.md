# JPA 페이지네이션 하기

아래와 같이 전체 피드를 조회하는 컨트롤러와 서비스를 페이지 기반으로 반환되도록 변경해보자. 이때 정렬은 updatedAt을 기준으로 내림차순 정렬 되어야 한다.



```java
//컨트롤러
@GetMapping
public ResponseEntity<List<Feed>> findAllFeed() {
  List<Feed> feedList = feedService.findAllFeed();
  return ResponseEntity.ok(feedList);
}

//서비스
public List<Feed> findAllFeed() {
  return feedRepository.findAll();
}
```

spring data 에서 제공해주는 Pageable, Page, PageRequest, Sort 를 사용할 것이다.

<br/>

### ver1: PageRequest를 통해 Pageable 객체 만들기

```java
@GetMapping
public ResponseEntity<Page<Feed>> findAllFeed(
  @RequestParam(defaultValue = "0") int page,
  @RequestParam(defaultValue = "10") int size
) {
  Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
  Page<Feed> feedPage = feedService.findAllFeed(pageable);
  return ResponseEntity.ok(feedPage);
}

//서비스
public Page<Feed> findAllFeed(Pageable pageable) {
  return feedRepository.findAll(pageable);
}
```

PageRequest.of()를 통해 Pageable 객체를 만들 수 있다. 그리고, spring data jpa는 이 pageable을 통해 조회하는 것 또한 제공하기 때문에 Repository는 별 다른 수정 없이 pageable을 넘김으로서 Page객체를 불러올 수 있었다.

<br/>

### ver2: @PageableDefault를 통해 Pageable 바인딩하기

```java
@GetMapping("/v2")
public ResponseEntity<Page<Feed>> findAllFeed(
  @PageableDefault(sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable
) {
  Page<Feed> feedsPage = feedService.findAllFeed(pageable);

  return ResponseEntity.ok(feedsPage);
}
```

더 세련되게 할 방법을 찾던 도중 `@PageableDefault`를 통해 Pageable을 바로 바인딩 하는 방법을 알아냈다.

size의 default는 10이기 때문에 별도로 지정하지는 않았다.
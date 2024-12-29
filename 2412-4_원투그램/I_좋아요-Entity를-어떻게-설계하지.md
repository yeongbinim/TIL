# 좋아요 Entity를 어떻게 설계하지

피드 CRUD, 피드의 댓글 CRUD 기능이 제공되는 서비스에 피드와 댓글 좋아요 기능을 추가하려고 한다.

그 과정에서 좋아요 Entity를 설계할 때 겪었던 고민들에 대해서 적어두려 한다.

<br/>

### ver1: 처음 상태

`Like` 테이블은 `Feed`를 위한 것이라 생각하고 `Comment` 테이블은 'like_count' 컬럼을 추가하여 해결하기로 했다.

<img width="600" src="https://github.com/user-attachments/assets/4a7b62b0-8cd5-441f-9e01-f8493460136c" />

단점: 사용자 입장에서 내가 어떤 댓글에 좋아요를 눌렀는지 확인할 방법이 없어진다.

<br/>

### ver2: 각 Entity에 좋아요 Entity 추가하기

`FeedLike`와 `CommentLike`를 각각 따로 만들어 각 Entity에 대한 좋아요를 관리하도록 설계하였다.


<img width="600" src="https://github.com/user-attachments/assets/bdafbade-283c-444c-84ce-19f500297cca" />

단점: 다른 테이블에도 좋아요 기능이 추가된다면 그때마다 "좋아요 테이블을 따로 추가해야 하나?" 하는 회의감이 들었다. 

따라서 두 좋아요 테이블을 합치기로 하였다.

<br/>

### ver3: 하나의 테이블에 외래키 여러개 관리

하나의 `Like` 테이블에 ’feed_id’ 컬럼과 ‘comment_id’ 컬럼을 둘 다 넣어서 두 엔티티를 관리할 수 있도록 했고, nullable로 설정하여 feed에 대한 좋아요라면 comment_id가 null이 되게 했다.

<img width="600" src="https://github.com/user-attachments/assets/2ebaa958-3757-4c5f-a17b-6b366c2feeeb" />

단점: 

1. 좋아요 기능이 다른 Entity에도 추가 될 때마다 외래키를 추가 해야한다.
2. 사용 되지 않는 다른 외래키 필드들이 null값이 들어가고 용량을 차지한다.

<br/>

### ver4: 외래키 제거하고 테이블을 타입으로 구분

Like 테이블에 entity_type을 추가하여 이것으로 엔티티를 구분하고, entity_id는 하나로만 관리되도록 했다.

<img width="600" src="https://github.com/user-attachments/assets/0990be87-c4b2-42da-9c5d-479255e50e2e" />

기존에는 외래키로 인해 참조 무결성이 보장 되었지만 외래키를 제거해 보장할 수 없게 되었다.

하지만 비즈니스 코드에서 좋아요 레코드를 추가,삭제 할때 Entity 테이블의 기본키를 체크하는 로직을 추가하고 `Feed`, `Comment`의 레코드를 삭제할 때 관련된 좋아요 레코드를 삭제하게 구현하면 문제가 해결 된다.
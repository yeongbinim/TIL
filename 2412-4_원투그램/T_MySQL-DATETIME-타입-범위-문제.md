# MySQL DATETIME 타입 범위 문제

feed를 페이징 조회하는 API에서 날짜 기간을 쿼리 파라미터로 전달시, 해당 기간의 조회가 되도록하고, 그렇지 않을 경우에는 전체 기간에서 조회가 되도록 구현을 했었다.

하지만, 쿼리파라미터가 입력되지 않았을 때 아래와 같이 계속 빈 값이 응답이 왔다.

<div align="center"><img width="250" src="https://github.com/user-attachments/assets/e6392941-0496-4c49-84c2-48fe90454fdb" /></div>

디버깅모드로 어떤 시점에서의 문제인지 확인해 보고자 했다.

<div align="center"><img width="1000" src="https://github.com/user-attachments/assets/4f7d185b-e222-494f-b649-bb355c58e950" /></div>

팀원이 전체 데이터를 조회하는 로직을 위와 같이 작성 했었는데, 그 이유는 null에 대해 다른 요청 쿼리를 사용하고 싶지 않아서between 문을 사용하는 쿼리를 null일때와 아닐때 동일하게 사용하고 싶어서 였다.

저 LocalDateTime.MIN과 LocalDateTime.MAX 부분이 문제였는데, Java에서의 LocalDateTime의 최소 최대는

`-999999999-01-01T00:00:00` ~ `+999999999-12-31T23:59:59` 이지만, MySQL에서의 DATETIME 최소 최대는 `1000-01-01 00:00:00` ~ `9999-12-31 23:59:59` 였던 것이다.

<div align="center"><img width="700" src="https://github.com/user-attachments/assets/98eb7b4e-6d4d-4fd4-a6e0-365c7dafc8ee" /></div>

위와 같이 코드를 바꿔주었더니 정상적으로 동작을 했다.

MySQL을 사용하고 있는 상황이라 저렇게 해주었지만, DB마다 그 최소최대가 다를 수 있기때문에 더 명확한 예외처리가 필요할거 같으며

Between문을 사용하면은 그만큼 쿼리 성능이 잘 나오지 않기 때문에, 각각 다른 요청 쿼리를 보낼 수 있도록 코드를 개선할 필요가 있기는 하다.
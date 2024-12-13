# MySQL 드라이버 로딩 실패

JdbcTemplate을 초기화 하는 과정에서 에러가 났다.

<img width="1000" src="https://github.com/user-attachments/assets/bf4fd335-900a-4c46-9fb5-ebbc6061eded">

<pre>
Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.UnsatisfiedDependencyException

: Error creating bean with name 'apiScheduleController' defined in file [~~/ApiScheduleController.class]: Unsatisfied dependency expressed through constructor parameter 0

: Error creating bean with name 'scheduleService' defined in file [~~/ScheduleService.class]: Unsatisfied dependency expressed through constructor parameter 0

<strong>: Error creating bean with name 'jdbcTemplateScheduleRepository' defined in file [~~/JdbcTemplateScheduleRepository.class]: Unsatisfied dependency expressed through constructor parameter 0</strong>

: Error creating bean with name 'dataSourceScriptDatabaseInitializer' defined in class path resource [org/springframework/boot/autoconfigure/sql/init/DataSourceInitializationConfiguration.class]: Unsatisfied dependency expressed through method 'dataSourceScriptDatabaseInitializer' parameter 0

: Error creating bean with name 'dataSource' defined in class path resource [org/springframework/boot/autoconfigure/jdbc/DataSourceConfiguration$Hikari.class]: Failed to instantiate [com.zaxxer.hikari.HikariDataSource]: Factory method 'dataSource' threw exception with message: Cannot load 

driver class: com.mysql.cj.jdbc.Driver
</pre>

위에서부터 아래로 읽다가 내가 작성하지 않은 클래스가 나오기 직전의 부분이 보통 문제다.

apiScheduleController(내가 작성) -> scheduleService(내가 작성) -> jdbcTemplateScheduleRepository(내가 작성) ->
dataSourceScriptDatabaseInitializer(내가 안 작성)

저 jdbcTemplateScheduleRepository 가 문제라고 한다. 확인해보자

<img width="500" src="https://github.com/user-attachments/assets/203f2fbe-3c34-4b5b-85ed-dc3ccf08d42d">

오 세상에.. 저 template에 `template = new JdbcTemplate(datasource)`로 초기화를 시켜주어야 하는데, 습관처럼 저렇게 JdbcTemplate
빈을 바로 찾으려 했다.

아래와 같이 바꿔주었다.

<img width="450" src="https://github.com/user-attachments/assets/265eac4c-52e4-4e98-82e8-f23efa988d03">

이제 되겠지? 했지만, 다시 에러가 발생했다.

<img width="1000" src="https://github.com/user-attachments/assets/54432c92-27e8-483e-a758-4ea64de74663">

여전히 jdbcTemplateScheduleRepository 빈을 생성하는데 문제가 생긴다고 한다.

자동주입을 하면서 어떤 문제가 생긴건가? 해서 수동주입으로 바꿔봤다.

<img width="400" src="https://github.com/user-attachments/assets/66c0dd82-a3d2-4ddd-a6db-0b3dcbd722aa">

<img width="1000" src="https://github.com/user-attachments/assets/f26e5a59-0e9d-44a7-9b51-3f2cf7746aad">

<pre>
Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.UnsatisfiedDependencyException

: Error creating bean with name 'jdbcTemplateConfig' defined in file [~~/JdbcTemplateConfig.class]: Unsatisfied dependency expressed through constructor parameter 0

: Error creating bean with name 'dataSource' defined in class path resource [org/springframework/boot/autoconfigure/jdbc/DataSourceConfiguration$Hikari.class]: Failed to instantiate [com.zaxxer.hikari.HikariDataSource]
<strong>: Factory method 'dataSource' threw exception with message: Cannot load driver class: com.mysql.cj.jdbc.Driver</strong>
</pre>

여전히 같은 에러가 발생한다. 이제 그 아래에 있는 메시지에 집중해야 할 때다.

"Cannot load driver class: com.mysql.cj.jdbc.Driver" 처음에 발견하지 못했던 메시지를 발견했다.

빈 등록하는 내 코드 자체에는 문제가 없지만, 저 드라이버를 로드하지 못한다고 한다.

저 드라이버 의존관계를 명시한 build.gradle을 확인해보자

<img width="500" src="https://github.com/user-attachments/assets/73b25d89-74c6-4bbb-a69f-0934e73c8a26">

저 드라이버에 버전을 명시하지 않았다.

의존성을 선언할 때에는 라이브러리의 정확한 버전을 명시하는게 기본인데, 스프링 부트는 자체적으로 많은 의존성들의 버전을 관리해서 버전을 명시하지 않아도 되는 것들이 많았다.

그래서 습관처럼 MySQL 드라이버도 버전을 명시하지 않았는데, Spring Boot의 의존성 관리 시스템이 이 드라이버의 적절한 버전을 자동으로 해결하지 못해서 발생한 거였다.

```java
runtimeOnly 'mysql:mysql-connector-java:8.0.28'
```

이렇게 버전까지 명시하니 정상적으로 스프링부트가 실행되는 것을 확인할 수 있었다.

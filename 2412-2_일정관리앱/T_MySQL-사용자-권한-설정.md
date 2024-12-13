# MySQL 사용자 권한 설정

MySQL 서버에서 scheduler 스키마를 생성하여 이 안에 Schedule 테이블을 생성해두고, 이제 인텔리제이에서 연동하려고 하니 내가 만든 스키마가 뜨지를 않았다.

<img width="200" src="https://github.com/user-attachments/assets/01f99b2d-1adc-45da-a31a-b1fa8aaa3fde">

root 사용자로 접근하는 게 아닌 userA라는 사용자를 만들어 이 사용자로 접근하게 하려고 했는데, 이 사용자에게는 scheduler 스키마의 테이블들에 접근이 제한되어 있던
것이다.

```sql
GRANT ALL PRIVILEGES ON scheduler.* TO 'userA'@'%';

FLUSH PRIVILEGES;
```

위의 명령을 통해 userA에게 scheduler 스키마의 모든 테이블에 권한을 열어두었다.

<img width="278" src="https://github.com/user-attachments/assets/0ed7d44c-f492-4f0b-a18d-54cbab5cb52d">

참고로 권한을 확인하거나 유저들 목록을 확인하는 명령어는 아래와 같다.

```sql
SHOW GRANTS FOR 'userA'@'localhost';
SELECT user, host
FROM mysql.user;
```

<img width="200" src="https://github.com/user-attachments/assets/9bf08d2b-d2af-4b32-9d9c-45094d872cfa">

권한을 열어두고 새로고침 해보니 위와같이 스키마와 연동할 수 있게 되었다.
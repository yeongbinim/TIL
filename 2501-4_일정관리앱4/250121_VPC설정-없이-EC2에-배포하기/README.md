# VPC설정-없이-EC2에-배포하기

별도로 VPC를 생성하지 않아도, 리전마다 default VPC와 해당 VPC에 연결된 4개의 서브넷이 할당되어 있다.

처음 AWS를 사용하는 사용자들이 쉽게 네트워킹을 설정하고 EC2인스턴스를 배포할 수 있도록 하기 위함인데, 이걸 적극 활용하여 간편하게 EC2에 스프링 서비스를 배포해볼 것이다.

<br/>

### 목차

- [MFA 추가 및 IAM 사용자 생성](#mfa-추가-및-iam-사용자-생성)
- [EC2 인스턴스 생성 및 ssh 접속](#ec2-인스턴스-생성-및-ssh-접속)
- [jar 파일 배포하기](#jar-파일-배포하기)
- [보안그룹 설정하기](#보안그룹-설정하기)


<br/>

### MFA 추가 및 IAM 사용자 생성

우선 회원가입을 진행한다. 결제수단을 추가하고, 이런저런 정보를 추가하는데 총 3분 정도 걸렸던 것 같다.

이제 이 이메일을 통해서 로그인이 가용한데, 이걸 Root Account라하고, 너무 중요한 계정이므로 MFA(Multifactor Authenticate)를 꼭 추가해주는 것이 좋다.

<div align="center"><img width="400" alt="Image" src="https://github.com/user-attachments/assets/a5c106c4-07f1-46f3-9fd2-0c63b48e48d7" /></div>

보안 자격 증명 페이지로 들어가서

<div align="center"><img width="600" alt="Image" src="https://github.com/user-attachments/assets/dc083727-9b2c-4b4e-b702-5ece43872e94" /></div>

MFA 디바이스 할당을 해준다. (이거 폰 옮기려고 하면, 제거를 꼭 해두자 나중에 전화해서 영어로 해야되는데 엄청 귀찮다고..)

MFA를 추가했다고 하더라도, 이 계정을 그대로 사용하는 것이 아니라 IAM 이라는 서비스를 이용해서 관리자(Admin) 계정이나 개발자 계정을 만들어서 사용하는 게 좋다.

어드민 자체는 root 와 거의 동일하지만, root 어카운트 사용하지 않고 어드민을 사용해야 하는 이유는 이 계정이 노출되었을때 루트 어카운트를 통해서 어드민을 제어할 수 있기 때문이다.

<div align="center"><img width="850" alt="Image" src="https://github.com/user-attachments/assets/6aaa5bb2-3b38-4446-a216-1d1e27db851d" /></div>

우선 IAM의 사용자그룹을 생성해준다. admin이라고 그룹이름을 지은 이후 `AdminstratorAccess` 권한을 연결해준다.

<div align="center"><img width="800" alt="Image" src="https://github.com/user-attachments/assets/3de3512e-1af0-430b-a69a-cb1b801ba8ca" /></div>

그리고 새로 사용자를 생성하여, 해당 그룹에 연결지어주는 것이다.

팀원들에게 공유할 Developer 그룹은 사진은 생략하지만, 절차는 위와 동일하다. 단, 권한 정책은 IAM 접근이 불가능하고, 몇몇 제약이 있는 PowerUserAccess로 해주자.

<div align="center"><img width="405" alt="Image" src="https://github.com/user-attachments/assets/5491a7d8-82fb-4027-bd31-24c725b835d5" /></div>

마지막으로 해당 아이디로 접속해 별칭 생성해주면, 좀 더 간편하다.

<br/>

### EC2 인스턴스 생성 및 ssh 접속

<div align="center"><img width="600" alt="Image" src="https://github.com/user-attachments/assets/0c3e328e-15b2-423e-a5ba-4c5c74fe6926" /></div>

여기 페이지로 이동해서 인스턴스를 생성한다.

<div align="center"><img width="400" alt="Image" src="https://github.com/user-attachments/assets/f5375b17-b3e3-4b97-9c08-fdb9249f55dc" /></div>

프리티어에서 사용이 가능한 ubuntu22.04LTS + t2.micro로 해준다.

이후에 별 다른 설정을 할 필요는 없고, 중요한 건 키페어를 생성하는 것인데, aws 키 잃어버리면 해당 인스턴스에 다시는 접속 못하니 주의해야 한다.

<div align="center"><img width="400" alt="Image" src="https://github.com/user-attachments/assets/d18072c0-ef4b-45d9-a459-ba1e836d7c7b" /></div>

확인해보면 인스턴스 생성이 1분도 채 걸리지 않고, 실행 중으로 바뀌는 것을 볼 수 있다. (Naver cloud 썼을때는 5분 이상..)

<div align="center"><img width="400" alt="Image" src="https://github.com/user-attachments/assets/af52a567-0767-469e-833a-dcbf544dfd65" /></div>

여기서 연결 버튼을 눌러 바로 연결이 되긴 하지만, 이렇게 매번 연결하기란.. 참 불편하니 ssh로 연결하는 것을 해보자

<div align="center"><img width="566" alt="Image" src="https://github.com/user-attachments/assets/28ee09ba-8983-4936-a18b-75bb4ecdcedc" /></div>

터미널 열고 우선 기존 키의 접근 권한을 소유주만 읽을 수 있도록 400으로 지정해주고, ssh의 `-i` 옵션을 통해서 접속한다.

그러면 연결이 완료된다.

추가로 서버 껐다 킬때마다 ip 바뀌는거 불편하면 탄력적 IP 설정해주자.

<br/>

### jar 파일 배포하기

우선 서버에 배포할 jar 파일을 빌드해야 한다.

<div align="center"><img width="250" alt="Image" src="https://github.com/user-attachments/assets/8a23dc40-10b5-42b1-be75-1b2b2aa8eb8b" /></div>

오른쪽의 코기리 탭에서 build를 더블클릭해도 되고, 또는

```shell
$ ./gradlew clean build
```

를 하면 `build/libs/` 폴더에 jar 파일이 만들어진다. `-plain.jar` 는 이 프로젝트를 라이브러리로써 활용하고 있는 경우에 사용되므로, 지금은 무시하자

이 파일을 방금 만든 `13.125.249.101` 주소의 인스턴스에 옮겨야 하는데, git으로 올렸다가 내려받는 방법도, ftp 를 사용하는 방법도 있겠지만 가장 간단한 방법인 scp를 사용한다.

가볍게 설명하면 ssh 프로토콜 기반의 파일전송 프로토콜이다.

```shell
$ scp -i /path/to/key.pem project-0.1.0.jar ubuntu@server-ip:/home/ubuntu
```

이 명령어로 전송을 할 것이다.

<div align="center"><img width="300" alt="Image" src="https://github.com/user-attachments/assets/52a5b584-f81c-46dc-a090-b971c204b9f2" /></div>

해당 명령어로 전송 후 정상적으로 jar파일이 이동한 것을 확인할 수 있다.

이제 이것을 실행하기 위해서 jdk를 설치할 건데, 

```shell
$ sudo apt update
$ sudo apt install openjdk-17-jdk
```

를 통해서 프로젝트와 동일한 버전의 jdk 17을 설치해주었다.

마지막으로 `java -jar` 명령어로 서비스를 실행시켜주면

<div align="center"><img width="600" alt="Image" src="https://github.com/user-attachments/assets/10d31c41-38b2-4734-82bb-b1f552bc2535" /></div>

정상적으로 돌아가는 것을 확인할 수 있다. 만약 백그라운드로 실행시키고 싶다면

```shell
$ nohup java -jar ./publish-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod &
```

로 실행시키고, 끄고 싶다면 `lsof -i :8080` 로 띄워져 있는거 확인한 후에 `kill {PID}` 하면 된다.

자 이제 브라우저에서 `13.125.249.101:8080`으로 접속해보자

<div align="center"><img width="600" alt="Image" src="https://github.com/user-attachments/assets/bae99fbb-bc39-4432-8a7b-a027ca05d2f1" /></div>

어림도 없다 왜일까?

<br/>

### 보안그룹 설정하기

보안그룹은 여러개의 인스턴스에 대한 트래픽을 제어하는 가상의 방화벽(외부에서 내부(Inbound) 혹은 내부에서 외부(Outbound)에서의 접근을 보호하는 역할) 역할을 한다.

기본적으로 보안그룹은 모든 Outbound 트래픽을 허용하고, 보안 그룹 규칙은 항상 허용적이며 액세스를 거부하는 규칙을 생성할 수 없다.

이 보안그룹을 설정해보자. 우선 해당 인스턴스에 연결했던 보안그룹을 찾아내서 해당 보안그룹의 인바운드 규칙을 편집한다.

<div align="center"><img width="1000" alt="Image" src="https://github.com/user-attachments/assets/52cfca04-f555-4aeb-8b18-a112958d1fc6" /></div>

8080 포트를 열어주면

<div align="center"><img width="460" alt="Image" src="https://github.com/user-attachments/assets/d23cd50a-d99c-458f-92e2-73e46952367f" /></div>

이제 해당 주소의 포트로 접속이 가능한 것을 볼 수 있다.

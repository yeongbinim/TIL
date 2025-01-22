# AWS 오브젝트 스토리지 S3

### S3란

**S3(Simple Storage Service)**는 AWS에서 제공하는 객체 스토리지 서비스이며, 아마존에서 수많은 상품의 이미지를 저장하기 위해서 만들어졌다고 한다.

<div align="center"><img width="600" alt="Image" src="https://github.com/user-attachments/assets/c5c7ad3c-abc1-4b17-8de5-ae20ac5f12b5" /></div>

S3는 사전에 용량을 지정하지 않고, 사용한 만큼만 돈을 지불하여 사용할 수 있다.

다른 스토리지 (ex. 파일 스토리지(EFS), 블록 스토리지(EBS))에 비해 확장성이 좋고, 다수의 사본을 분산시켜 저장해두기 때문에 안전성이 높다. (99.999999999% 내구도를 가진다)

이런 특성에 따라 아래의 사용 예시들이 있다.

- 클라우드 저장소 (개인 파일 보관, 구글 드라이브처럼 사용 가능)
- 서비스의 대용량 파일 저장소 - 이미지, 동영상, 빅데이터 (ex: 넷플릭스)
- 서비스 로그 저장 및 분석
- AWS 아데나를 이용한 빅데이터 업로드 및 분석
- 서비스 사용자의 데이터 업로드 서버 (이미지 서버, 동영상 서버)
- 정적 웹 사이트 호스팅

<br/>

### S3의 핵심 용어

S3의 주된 용어는 아래와 같이 크게 4개가 있다.

<div align="center"><img width="300" alt="Image" src="https://github.com/user-attachments/assets/3384db1b-ab3a-49b2-98ce-fa79658a1d2b" /></div>

- **Bucket**: 오브젝트를 담는 최상위 단위 (이름, 개수 제한이 있다)
- **Object**: Bucket에 담는 데이터의 단위 (파일)
- **MetaData**: Object에 대한 여러가지 정보를 담고있는 데이터
- **Policy**: Bucket, Object에 대한 접근을 통제하는 권한 정보

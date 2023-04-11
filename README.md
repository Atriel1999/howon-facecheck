##### howon-facecheck는 휴대폰 카메라에 비춰지는 화면에 존재하는 사람의 얼굴을 인식 하고 자동으로 출결 체크를 해주는 애플리케이션 입니다.
##### 애플리케이션 명은 호원대 출석앱 줄여서 '호출'입니다.
<p align="center">
<img src=https://user-images.githubusercontent.com/118334518/231158946-c37dc8fb-5eb1-4a99-81cf-1ecfc4b26474.png> </p>
</br>

# <span style="color:yellow">안드로이드 클라이언트</span>
##### + 안드로이드 클라이언트에서는 Cameara2 API, Google ML kit Face Detection, TTS등의 기술을 사용하였습니다.
##### + 교수 사용자는 본인의 강의를 새로 등록 or 로그인 한 후 카메라 출결 시작, 날짜별 출결확인이 가능합니다.
##### + 학생 사용자는 기본적인 정보와 본인의 사진을 입력한뒤 교수 사용자가 등록한 강의 번호를 받아서 본인의 출석정보를 확인할 수 있습니다.
##### + Face Detection을 이용해 카메라에서 얼굴이 인식될때 서버에 사진정보를 Crop해서 전송함
##### + 와이어 프레임(Wire frame) Link: https://miro.com/app/board/uXjVO6Z5cH8=/

![image](https://user-images.githubusercontent.com/118334518/231157362-7d22dabd-f46e-41fe-a339-681de6080729.png)
![image](https://user-images.githubusercontent.com/118334518/231157446-af79839f-f3a6-421a-8482-02d3afa2a07a.png)
![image](https://user-images.githubusercontent.com/118334518/231157566-8da3ac72-4217-40fd-9b00-e16abbf590c9.png)

</br></br></br></br>
# <span style="color:yellow">파이썬 서버</span>
##### 소켓 통신으로 안드로이드와 연결된 파이썬 서버에서는 Open-CV, KNN CLassifier, pymysql등의 기술이 사용되었습니다.
##### 학생 사용자의 정보를 서버에 등록하고 출석시에 KNN Classifier 알고리즘을 사용해 미리 등록된 사진과 얼굴을 비교해 유사율을 측정 (80% 이상시 동일)
##### 학생 사용자는 기본적인 정보와 본인의 사진을 입력한뒤 교수 사용자가 등록한 강의 번호를 받아서 본인의 출석정보를 확인할 수 있습니다.

![image](https://user-images.githubusercontent.com/118334518/231157268-ad188c2a-1d77-4df3-8992-c35a99636b45.png)












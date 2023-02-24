# use-Bluetooth-AndroidStudio
use bluetooth in Android studio mobile app -- get permission from user to use bluetooth

---
code reference: <a href="https://www.youtube.com/watch?v=iFtjox9_zAI&list=LL&index=1&t=32s">YouTube - Simple Bluetooth Example - Android Studio Tutorial</a>

EDIT part: checkSelfPermission추가 (실행중 사용자에게 권한허용받기)

종류(<a href="https://ddangeun.tistory.com/158">ref</a>):

*  BLUETOOTH_SCAN : 주변 블루투스 기기를 검색하는 경우
*  BLUETOOTH_ADVERTISE : 현재 기기를 다른 블루투스 기기에서 검색할 수 있도록 하는 경우
*  BLUETOOT_CONNECT : 이미 피어링된 기기와 통신해야 할 경우

---
# 실제 태블릿에서 run하기
ref: <a href="https://www.youtube.com/watch?v=Wp6KbJcnxGU">YouTube - How to run Android Studio app on real device</a>

1. usb로 태블릿과 pc 연결
2. 설정 >> 태블릿 정보 >> 소프트웨어 정보 >> 빌드 번호
3. 빌드 번호를 7번 클릭  “개발자 모드“ popup 확인
4. 설정 >> 개발자 옵션 >> USB 디버깅: enable  허용

![image](https://user-images.githubusercontent.com/61898376/221110795-215b92dd-7450-4260-9388-e8fc45aba52f.png)


# DNS Switcher

평소에는 Android 프라이빗 DNS를 지정한 호스트 이름(광고 차단 DNS 등)으로 두고,
선택한 앱이 화면에 떠 있는 동안만 `자동(권장)`으로 바꾼다.

## 동작

- 접근성 서비스가 앞에 뜬 앱(액티비티)의 패키지 이름만 확인한다. 화면 내용은 읽지 않는다.
- 선택한 앱이면 `private_dns_mode=opportunistic`, 아니면 `hostname`으로 바꾼다.
- 호스트 이름(`private_dns_specifier`)은 건드리지 않는다. 시스템 설정에서 지정한 값을 그대로 쓴다.
- 접근성 서비스가 꺼지면 `hostname`으로 되돌린다.

## 설치

1. APK를 설치한다.
2. 시스템 설정에서 프라이빗 DNS를 `프라이빗 DNS 공급자 호스트 이름`으로 두고 호스트를 입력한다.
   (갤럭시: 설정 > 연결 > 기타 연결 설정 > 프라이빗 DNS)
3. PC에서 권한을 한 번 준다.
   ```bash
   adb shell pm grant io.github.mhe1239.dnsswitcher android.permission.WRITE_SECURE_SETTINGS
   ```
4. 앱의 `접근성 설정 열기`에서 DNS Switcher를 켠다.
   Android 13 이상에서 직접 설치한 APK는 "제한된 설정"으로 막힐 수 있다.
   이때는 설정 > 애플리케이션 > DNS Switcher > 오른쪽 위 메뉴 > `제한된 설정 허용` 후 다시 켠다.
5. 앱 목록에서 `자동`으로 쓸 앱을 체크한다.

## 한계

- 설정은 기기 전체에 적용된다. 선택한 앱이 앞에 있는 동안에는 백그라운드 앱도 차단 DNS를 쓰지 않는다.
- 모드가 바뀔 때 DNS 연결이 다시 맺어지며 잠깐 조회가 실패할 수 있다.
- 배터리 최적화로 접근성 서비스가 꺼지지 않게 앱을 최적화 예외에 두는 것을 권장한다.

## 빌드

JDK 17 또는 21이 필요하다. (Android Studio 내장 JDK 25는 Gradle 8.11에서 실행되지 않음)

```bash
JAVA_HOME="/c/Program Files/Java/jdk-21" ./gradlew testDebugUnitTest assembleDebug
```

결과물: `app/build/outputs/apk/debug/app-debug.apk`

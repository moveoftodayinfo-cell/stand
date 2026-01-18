# Stand 앱 결제 시스템 설정 가이드

이 가이드는 Firebase와 Google Play Billing을 사용한 구독 결제 시스템 설정 방법을 안내합니다.

---

## 📋 목차

1. [Firebase 프로젝트 설정](#1-firebase-프로젝트-설정)
2. [Firebase Authentication 설정](#2-firebase-authentication-설정)
3. [Firestore 데이터베이스 설정](#3-firestore-데이터베이스-설정)
4. [google-services.json 추가](#4-google-servicesjson-추가)
5. [Google Play Console 설정](#5-google-play-console-설정)
6. [구독 상품 생성](#6-구독-상품-생성)
7. [Firebase Functions 배포](#7-firebase-functions-배포)
8. [테스트 방법](#8-테스트-방법)

---

## 1. Firebase 프로젝트 설정

### 1.1 Firebase 콘솔 접속
1. https://console.firebase.google.com/ 접속
2. "프로젝트 추가" 클릭
3. 프로젝트 이름 입력: `Stand` (또는 원하는 이름)
4. Google Analytics 사용 설정 (선택사항)
5. "프로젝트 만들기" 클릭

### 1.2 Android 앱 추가
1. Firebase 프로젝트 개요 페이지에서 "Android" 아이콘 클릭
2. **Android 패키지 이름**: `com.moveoftoday.walkorwait`
   - ⚠️ 중요: 정확히 입력해야 합니다!
3. 앱 닉네임(선택): `Stand`
4. 디버그 서명 인증서 SHA-1 (선택사항, 나중에 추가 가능)
5. "앱 등록" 클릭

---

## 2. Firebase Authentication 설정

### 2.1 Authentication 활성화
1. Firebase 콘솔 왼쪽 메뉴에서 "Authentication" 클릭
2. "시작하기" 버튼 클릭
3. "Sign-in method" 탭 선택

### 2.2 익명 로그인 활성화
1. "익명" 항목 클릭
2. "사용 설정" 토글을 ON으로 변경
3. "저장" 클릭

> **왜 익명 로그인?**
> 사용자가 이메일/비밀번호 없이도 앱을 사용할 수 있습니다.
> Firebase는 각 사용자에게 고유 UID를 자동으로 부여합니다.

### 2.3 (선택) 이메일/비밀번호 로그인 추가
나중에 계정 시스템을 추가하려면:
1. "이메일/비밀번호" 항목 클릭
2. "사용 설정" ON
3. "저장" 클릭

---

## 3. Firestore 데이터베이스 설정

### 3.1 Firestore 생성
1. Firebase 콘솔 왼쪽 메뉴에서 "Firestore Database" 클릭
2. "데이터베이스 만들기" 클릭
3. **모드 선택**: "프로덕션 모드에서 시작" 선택
   - 보안 규칙은 나중에 설정합니다
4. **위치 선택**: `asia-northeast3 (서울)` 권장
5. "사용 설정" 클릭

### 3.2 보안 규칙 설정
1. "규칙" 탭 클릭
2. 다음 규칙을 복사/붙여넣기:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // 사용자는 본인 데이터만 읽기/쓰기 가능
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;

      // 일일 기록은 사용자가 작성 가능
      match /dailyRecords/{dateId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }

      // 구독 데이터는 읽기만 가능 (서버에서만 쓰기)
      match /subscriptions/{monthId} {
        allow read: if request.auth != null && request.auth.uid == userId;
        allow write: if false; // Functions에서만 작성
      }
    }
  }
}
```

3. "게시" 클릭

### 3.3 색인 생성 (선택사항)
나중에 앱 실행 시 Firestore에서 색인 생성 링크가 나오면 클릭하여 생성하세요.

---

## 4. google-services.json 추가

### 4.1 파일 다운로드
1. Firebase 프로젝트 설정(⚙️) > 프로젝트 설정
2. "내 앱" 섹션에서 Android 앱 찾기
3. **"google-services.json 다운로드"** 클릭

### 4.2 파일 위치
다운로드한 `google-services.json` 파일을 다음 경로에 복사:

```
WalkorWait/
  app/
    google-services.json  ← 여기!
    build.gradle.kts
    src/
```

⚠️ **주의**: `app/` 폴더 안에 넣어야 합니다! (루트 폴더가 아님)

### 4.3 파일 확인
Android Studio에서 프로젝트를 "Android" 뷰로 보면:
```
app
  └─ google-services.json ✅
```

---

## 5. Google Play Console 설정

### 5.1 Play Console 접속
1. https://play.google.com/console 접속
2. 앱 선택 또는 새 앱 생성
3. ⚠️ **중요**: 앱을 Internal Testing 이상으로 배포해야 결제 테스트 가능

### 5.2 앱 업로드 (최초 1회)
1. Android Studio에서 `Build > Generate Signed Bundle / APK`
2. Android App Bundle (.aab) 선택
3. Key Store 생성 (처음인 경우)
4. Release 빌드 생성
5. Play Console에서 "프로덕션" 또는 "내부 테스트"에 업로드

---

## 6. 구독 상품 생성

### 6.1 구독 상품 설정 페이지
1. Play Console 왼쪽 메뉴에서 **"수익 창출 > 구독"** 클릭
2. "구독 만들기" 클릭

### 6.2 기본 정보 입력
- **제품 ID**: `monthly_deposit_subscription`
  - ⚠️ **중요**: 코드에서 사용하는 ID와 정확히 일치해야 합니다!
  - (BillingManager.kt:35 참고)
- **이름**: `월간 보증금 구독`
- **설명**: `걷기 목표 달성 시 다음달 면제`

### 6.3 가격 설정
구독은 하나의 가격만 설정할 수 있습니다. 여러 보증금 옵션(1,000원~1,000,000원)을 지원하려면:

#### 옵션 A: 단일 구독 + 서버 로직 (권장)
1. 기본 가격: **1,000원/월**로 설정
2. 앱에서 보증금 금액을 선택하면, 서버(Functions)에서 해당 금액만큼 청구
3. Google Play Billing API 사용하여 동적 가격 조정

#### 옵션 B: 여러 구독 상품 생성
각 보증금 금액마다 별도의 구독 상품 생성:
- `monthly_deposit_1000` (1,000원)
- `monthly_deposit_10000` (10,000원)
- `monthly_deposit_100000` (100,000원)
- 등...

> **권장**: 옵션 A를 사용하되, 일단 테스트용으로 10,000원 구독 1개만 생성하세요.

### 6.4 가격 입력
1. "기본 가격 추가" 클릭
2. **국가**: 대한민국
3. **가격**: 10,000원 (테스트용)
4. **갱신 기간**: 1개월
5. "추가" 클릭

### 6.5 무료 체험 (선택사항)
- 무료 체험 기간: 사용 안 함 (보증금 개념이므로)

### 6.6 유예 기간 (선택사항)
- 유예 기간: 사용 안 함

### 6.7 저장 및 활성화
1. "저장" 클릭
2. "활성화" 클릭

---

## 7. Firebase Functions 배포

### 7.1 Node.js 설치
1. https://nodejs.org/ 에서 LTS 버전 다운로드 및 설치
2. 터미널에서 확인:
   ```bash
   node --version
   npm --version
   ```

### 7.2 Firebase CLI 설치
```bash
npm install -g firebase-tools
```

### 7.3 Firebase 로그인
```bash
firebase login
```

### 7.4 Functions 초기화
프로젝트 루트 폴더에서:
```bash
cd C:\Users\ato91\AndroidStudioProjects\WalkorWait
firebase init functions
```

선택 사항:
- **사용할 Firebase 프로젝트**: 위에서 생성한 프로젝트 선택
- **언어**: TypeScript 권장
- **ESLint**: Yes
- **종속성 설치**: Yes

### 7.5 Functions 코드 작성
`functions/src/index.ts` 파일을 다음과 같이 작성:

```typescript
import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import {google} from "googleapis";

admin.initializeApp();

// Google Play Developer API 설정
const androidPublisher = google.androidpublisher("v3");

/**
 * 구매 영수증 검증
 */
export const verifyPurchase = functions.https.onCall(async (data, context) => {
  // 인증 확인
  if (!context.auth) {
    throw new functions.https.HttpsError(
      "unauthenticated",
      "User must be authenticated"
    );
  }

  const {packageName, productId, purchaseToken} = data;

  try {
    // TODO: Google Play Developer API 키 설정 필요
    // const response = await androidPublisher.purchases.subscriptions.get({
    //   packageName: packageName,
    //   subscriptionId: productId,
    //   token: purchaseToken,
    // });

    // 임시로 성공 반환 (테스트용)
    return {
      success: true,
      verified: true,
    };
  } catch (error) {
    console.error("Purchase verification failed:", error);
    throw new functions.https.HttpsError(
      "internal",
      "Failed to verify purchase"
    );
  }
});

/**
 * 매월 말일에 실행: 성공/실패 판정
 */
export const processMonthlyResults = functions.pubsub
  .schedule("0 23 * * *") // 매일 23:00 실행
  .timeZone("Asia/Seoul")
  .onRun(async (context) => {
    const today = new Date();
    const isLastDayOfMonth =
      today.getDate() ===
      new Date(today.getFullYear(), today.getMonth() + 1, 0).getDate();

    if (!isLastDayOfMonth) {
      console.log("Not the last day of the month. Skipping.");
      return;
    }

    console.log("Processing monthly results...");

    // 모든 사용자의 구독 정보 가져오기
    const usersSnapshot = await admin.firestore().collection("users").get();

    for (const userDoc of usersSnapshot.docs) {
      const userId = userDoc.id;
      const monthId = today.toISOString().slice(0, 7); // YYYY-MM

      // 해당 월 구독 정보 가져오기
      const subscriptionDoc = await admin
        .firestore()
        .collection("users")
        .doc(userId)
        .collection("subscriptions")
        .doc(monthId)
        .get();

      if (!subscriptionDoc.exists) {
        continue;
      }

      const subscription = subscriptionDoc.data();
      if (!subscription) continue;

      // 일일 기록에서 성공 일수 계산
      const dailyRecords = await admin
        .firestore()
        .collection("users")
        .doc(userId)
        .collection("dailyRecords")
        .where("dateId", ">=", `${monthId}-01`)
        .where("dateId", "<", getNextMonthId(monthId) + "-01")
        .get();

      let totalDays = 0;
      let successDays = 0;

      dailyRecords.forEach((doc) => {
        const record = doc.data();
        if (record.isControlDay) {
          totalDays++;
          if (record.isSuccess) {
            successDays++;
          }
        }
      });

      const achievementRate =
        totalDays > 0 ? (successDays / totalDays) * 100 : 0;
      const isSuccess = achievementRate >= 90;

      // 구독 업데이트
      await subscriptionDoc.ref.update({
        totalDays,
        successDays,
        achievementRate,
        isSuccess,
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      });

      // 다음달 구독 생성
      const nextMonthId = getNextMonthId(monthId);
      const nextSubscription = {
        monthId: nextMonthId,
        deposit: subscription.deposit,
        isPaid: false,
        isExempt: isSuccess,
        actualCharge: isSuccess ? 0 : subscription.deposit,
        refundAmount: !isSuccess ? subscription.deposit : 0,
        totalDays: 0,
        successDays: 0,
        achievementRate: 0,
        isSuccess: false,
        goal: subscription.goal,
        controlDays: subscription.controlDays,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      };

      await admin
        .firestore()
        .collection("users")
        .doc(userId)
        .collection("subscriptions")
        .doc(nextMonthId)
        .set(nextSubscription);

      console.log(
        `Processed ${userId}: ${isSuccess ? "SUCCESS" : "FAILED"} (${achievementRate.toFixed(1)}%)`
      );
    }

    return null;
  });

function getNextMonthId(monthId: string): string {
  const [year, month] = monthId.split("-").map(Number);
  const date = new Date(year, month, 1); // 다음달 1일
  return date.toISOString().slice(0, 7);
}
```

### 7.6 Functions 배포
```bash
firebase deploy --only functions
```

배포 완료 후 Firebase 콘솔의 "Functions" 메뉴에서 확인 가능합니다.

---

## 8. 테스트 방법

### 8.1 테스트 계정 추가
1. Play Console > "설정 > 라이선스 테스트"
2. "라이선스 테스터" 추가
3. 본인의 Gmail 계정 입력
4. "변경사항 저장"

### 8.2 Internal Testing 배포
1. Play Console > "출시 > 테스트 > 내부 테스트"
2. "새 출시 만들기"
3. AAB 파일 업로드
4. "출시 검토" > "내부 테스트 시작"

### 8.3 테스트 앱 설치
1. 테스터로 추가한 Gmail 계정으로 Play 스토어 접속
2. 테스트 링크를 통해 앱 다운로드
3. 앱 실행 > 보증금 설정 > 결제 진행

### 8.4 결제 테스트
- ✅ **테스트 계정으로 결제하면 실제 청구되지 않습니다!**
- Google Play는 테스트 구매를 자동으로 감지합니다.
- 테스트 결제는 몇 분 후 자동 취소됩니다.

### 8.5 Firestore 데이터 확인
1. Firebase 콘솔 > Firestore Database
2. `users > {userId} > subscriptions > {monthId}` 확인
3. 데이터가 정상적으로 저장되었는지 확인

---

## 🔧 문제 해결

### Q1. "google-services.json not found" 오류
- `app/google-services.json` 경로가 맞는지 확인
- Android Studio에서 "Sync Project with Gradle Files" 실행

### Q2. Billing 연결 실패
- Play Console에서 앱이 최소 Internal Testing으로 배포되었는지 확인
- 구독 상품이 "활성화" 상태인지 확인
- 패키지 이름이 정확한지 확인 (`com.moveoftoday.walkorwait`)

### Q3. Firebase Auth 오류
- Firebase 콘솔에서 Authentication이 활성화되었는지 확인
- 익명 로그인이 사용 설정되었는지 확인

### Q4. 테스트 결제가 실제 청구됨
- 테스트 계정(라이선스 테스터)으로 로그인했는지 확인
- Internal Testing 트랙으로 배포했는지 확인

---

## 📚 참고 자료

- [Firebase 문서](https://firebase.google.com/docs)
- [Google Play Billing 문서](https://developer.android.com/google/play/billing)
- [Play Console 도움말](https://support.google.com/googleplay/android-developer)

---

## ✅ 체크리스트

설정 완료 여부를 확인하세요:

- [ ] Firebase 프로젝트 생성
- [ ] Android 앱 추가
- [ ] Firebase Authentication 활성화 (익명 로그인)
- [ ] Firestore Database 생성
- [ ] Firestore 보안 규칙 설정
- [ ] google-services.json 다운로드 및 추가
- [ ] Google Play Console 앱 생성
- [ ] 앱을 Internal Testing으로 배포
- [ ] 구독 상품 생성 (`monthly_deposit_subscription`)
- [ ] 구독 상품 활성화
- [ ] 테스트 계정 추가
- [ ] Firebase Functions 배포
- [ ] 테스트 앱 설치 및 결제 테스트

모든 항목을 완료하면 결제 시스템이 작동합니다! 🎉

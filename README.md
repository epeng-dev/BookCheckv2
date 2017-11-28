BookCheckv2
===========
책첵 프로젝트 2차 앱
-----------

#### 동결되었음 (서버 개발 x)

<hr/>

## 구성 : 
## Activity:
## 1. MainActivity : 
#### 앱에서 메인이 되는 엑티비티
#### Fragment를 삽입하여 사용한다.
#### DrawerLayout과 NavigationView를 사용하였다.

## 2. RFIDActivity :
#### RFID를 인식하는 엑티비티
#### 책 반납이나 대출을 처리하는데 사용하였다.
#### RFID로 반납, 대출을 한다.

## 3. BarcodeActivity :
#### Barcode를 찍어서 인식하는 엑티비티
#### 책 반납이나 대출을 처리하는데 사용하였다.
#### Barcode로 반납, 대출을 한다.

## 4. LoginActivity : 
#### 로그인 처리를 담당하는 엑티비티

#

## Fragment:
## 1. MainFragment :
#### Web으로 띄워진 메인 페이지를 출력한다.
#### 안드로이드 WebView 사용


## 2. BorrowFragment :
#### 책을 빌리기 위한 프레그먼트
#### RecyclerView와 CardView를 이용해서 만들었다.

## 3. ReturnBookFragment :
#### 책을 반납하기 위한 프레그먼트
#### RecyclerView와 CardView를 이용해서 만들었다.

## 4. RegisterBookFragment :
#### 책을 등록하기 위한 프레그먼트
#### 관리자 기능으로 만들어졌다.
#### RecyclerView와 CardView를 이용해서 만들었다.

#

## Component
## 1. CardViewItem :
#### 카드뷰를 이루는 아이템

## 2. CardViewAdapter :
#### 카드뷰 생성, 관리
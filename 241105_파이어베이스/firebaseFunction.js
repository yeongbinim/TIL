import { db } from './firebaseConfig.js';
import { addDoc, collection, getDocs, deleteDoc, doc } from "https://www.gstatic.com/firebasejs/11.0.1/firebase-firestore.js";
import { formatCurDate } from './utils.js';

/* 조회 */
export async function renderComments() {
  let data;
  try {
    const querySnapshot = await getDocs(collection(db, "comments"));
    data = querySnapshot.docs.map((doc) => {
      return {...doc.data(), id: doc.id};
    });
  } catch (error) {
    console.error('댓글 조회 에러', error);
  }
  
  document.getElementById("comment-box").innerHTML = data.sort((a, b) => new Date(a.date) - new Date(b.date)).map((row) => `
    <div class="comment-box__container">
      <div class="comment-box__item comment-box__item--header">
        <span><strong>${row.author}</strong></span>
        <span>${row.date}</span>
        <div><button data-id="${row.id}" data-password="${row.password}" class="comment-box__button--delete">삭제</button></div>
      </div>
      <div class="comment-box__item comment-box__item--content">
        ${row.comment}
      </div>
    </div>
  `).join("");
}

/* 생성 */
export async function submitCommentForm(event) {
  event.preventDefault();  // 폼의 기본 제출 동작 방지
  const formData = new FormData(event.target);  // 폼 데이터 수집

  const dataObject = {};  // 데이터 객체 생성
  formData.forEach((value, key) => {
    dataObject[key] = value;  // 폼 데이터를 객체에 할당
  });

  dataObject.date = formatCurDate();  // 현재 날짜와 시간 추가

  try {
    await addDoc(collection(db, "comments"), dataObject);  // 데이터베이스에 문서 추가
  } catch (error) {
    console.error('댓글 전송 에러', error);
  }
  await renderComments();
  event.target.reset();  // 폼 필드 초기화
}

/* 삭제 */
export async function deleteComment(event) {
  const deleteButton = event.target.closest('.comment-box__button--delete');
  if (deleteButton) {
    const id = deleteButton.getAttribute('data-id'); // 데이터 ID 추출
    const password = deleteButton.getAttribute('data-password'); // 데이터 ID 추출
    const userInput = prompt("댓글을 삭제하려면 '비밀번호'를 입력하세요."); // 사용자 입력 요청
    if (userInput === password) {
        try {
            await deleteDoc(doc(db, "comments", id));
            await renderComments();
        } catch (error) {
          console.error('댓글 삭제 에러', error);
        }
    } else {
      alert("비밀번호가 일치하지 않습니다.");
    }
  }
}

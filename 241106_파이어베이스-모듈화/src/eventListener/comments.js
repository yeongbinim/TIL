import { formatDate } from '../utils/date.js';
import { commentRepository } from '../request/dependency.js';
import { renderComments } from '../render/comments.js';

const repsoitory = commentRepository();

export async function getAllComments() {
  const data = await repsoitory.findAll();
  const sortedData = data.sort((a, b) => new Date(a.date) - new Date(b.date));
  renderComments(sortedData);
}

export async function submitCommentForm(event) {
  event.preventDefault();  // 폼의 기본 제출 동작 방지
  const formData = new FormData(event.target);  // 폼 데이터 수집

  const dataObject = {};  // 데이터 객체 생성
  formData.forEach((value, key) => {
    dataObject[key] = value;  // 폼 데이터를 객체에 할당
  });

  dataObject.date = formatDate(new Date());  // 현재 날짜와 시간 추가

  await repsoitory.save(dataObject);
  await getAllComments();
  event.target.reset();  // 폼 필드 초기화
}

export async function deleteComment(event) {
  const deleteButton = event.target.closest('.comment-box__button--delete');
  if (deleteButton) {
    const id = deleteButton.getAttribute('data-id'); // 데이터 ID 추출
    const password = deleteButton.getAttribute('data-password'); // 데이터 ID 추출
    const userInput = prompt("댓글을 삭제하려면 '비밀번호'를 입력하세요."); // 사용자 입력 요청
    if (userInput === password) {
      await repsoitory.deleteById(id);
      await getAllComments();
    } else {
      alert("비밀번호가 일치하지 않습니다.");
    }
  }
}

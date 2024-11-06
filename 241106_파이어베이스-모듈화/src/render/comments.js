export function renderCommentSection() {
  const elementId = "comment";
  document.getElementById(elementId).innerHTML = `
    <div id="comment-box"></div>
    <form id="comment-form" class="comment-form">
      <div class="comment-form__container">
        <div class="comment-form__group">
          <input class="comment-form__input comment-form__input--author" type="text" id="author" name="author" maxlength="8" placeholder="작성자" required>
          <input class="comment-form__input comment-form__input--password" type="password" id="password" name="password" placeholder="비밀번호 4자리" maxlength="4" minlength="4" required>
        </div>
        <div class="comment-form__group">
          <input class="comment-form__input comment-form__input--comment" type="text" id="comment" name="comment" placeholder="댓글을 입력하세요" maxlength="40" required>
        </div>
      </div>
      <button type="submit" class="comment-form__button">전송</button>
    </form>  
  `;
}

export function renderComments(data) {
  const elementId = "comment-box";
  document.getElementById(elementId).innerHTML = data.map((row) => `
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
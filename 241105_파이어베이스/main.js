import { renderComments, deleteComment, submitCommentForm } from "./firebaseFunction.js";

document.getElementById('comment-form').addEventListener('submit', submitCommentForm);
document.getElementById('comment-box').addEventListener('click', deleteComment);

window.addEventListener('DOMContentLoaded', (event) => {
  renderComments();
});

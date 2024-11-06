import { deleteComment, submitCommentForm, getAllComments } from "./eventListener/comments.js";
import { renderCommentSection } from "./render/comments.js";

window.addEventListener('DOMContentLoaded', (event) => {
  renderCommentSection();
  getAllComments();

  document.getElementById('comment-form').addEventListener('submit', submitCommentForm);
  document.getElementById('comment-box').addEventListener('click', deleteComment);
});
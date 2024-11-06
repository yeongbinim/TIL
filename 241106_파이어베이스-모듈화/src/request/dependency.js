import FirebaseRepository from "./FirebaseRepository.js";

export function commentRepository() {
  return new FirebaseRepository("comments");
}
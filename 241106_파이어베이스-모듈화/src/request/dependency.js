import FirebaseRepository from "./FirebaseRepository.js";
import Request from "./RequestRepository.js";

export function commentRepository() {
  return new Request("comments");
}
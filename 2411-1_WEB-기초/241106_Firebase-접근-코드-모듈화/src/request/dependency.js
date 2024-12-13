import FirebaseRepository from "./FirebaseRepository.js";
import Request from "./Request.js";

export function commentRepository() {
  return new Request("comments");
}
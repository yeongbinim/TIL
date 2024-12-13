import RepositoryInterface from "./RepositoryInterface.js";
import { addDoc, collection, getDocs, deleteDoc, doc, query, where, updateDoc } from "https://www.gstatic.com/firebasejs/11.0.1/firebase-firestore.js";
import { initializeApp } from "https://www.gstatic.com/firebasejs/11.0.1/firebase-app.js";
import { getFirestore } from "https://www.gstatic.com/firebasejs/11.0.1/firebase-firestore.js";
import { firebaseConfig } from "../config.js";

const db = getFirestore(initializeApp(firebaseConfig));

export default class FirebaseRepository extends RepositoryInterface {
  #collectionName;
  constructor(collectionName) {
    super();
    this.#collectionName = collectionName;
  }

  async save(obj) {
    await addDoc(collection(db, this.#collectionName), obj);
  }

  async findAll() {
    const querySnapshot = await getDocs(collection(db, this.#collectionName));
    return querySnapshot.docs.map((doc) => ({...doc.data(), id: doc.id}));
  }

  async findBy(key, value) {
    const q = query(collection(db, this.collectionName), where(key, '==', value));
    const querySnapshot = await getDocs(q);
    return querySnapshot.docs.map((doc) => ({...doc.data(), id: doc.id}));
  }
  
  async updateById(id, obj) {
    await updateDoc(doc(db, this.#collectionName, id), obj);
  }

  async deleteById(id) {
    await deleteDoc(doc(db, this.#collectionName, id));
  }
}
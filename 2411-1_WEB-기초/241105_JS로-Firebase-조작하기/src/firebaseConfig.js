import { initializeApp } from "https://www.gstatic.com/firebasejs/11.0.1/firebase-app.js";
import { getFirestore } from "https://www.gstatic.com/firebasejs/11.0.1/firebase-firestore.js";

const firebaseConfig = {
  apiKey: "AIzaSyBPdAZwbx7hmnmzYdx6tK3syWlxBEPxXSA",
  authDomain: "joyworld-70e9f.firebaseapp.com",
  projectId: "joyworld-70e9f",
  storageBucket: "joyworld-70e9f.firebasestorage.app",
  messagingSenderId: "969858900322",
  appId: "1:969858900322:web:57a139f81b3f1807f48c3b"
};

const app = initializeApp(firebaseConfig);
export const db = getFirestore(app);

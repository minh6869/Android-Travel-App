// Import the functions you need from the SDKs you need
import { initializeApp } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-app.js";
import { getAuth } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-auth.js";
import { getFirestore } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-firestore.js";
import { getStorage } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-storage.js";

// Your web app's Firebase configuration
const firebaseConfig = {
    apiKey: "AIzaSyC2w0NaJqbeNrcGMKCz2j-VevAGpVuMys8",
    authDomain: "travelerapp-b3f83.firebaseapp.com",
    databaseURL: "https://travelerapp-b3f83-default-rtdb.asia-southeast1.firebasedatabase.app",
    projectId: "travelerapp-b3f83",
    storageBucket: "travelerapp-b3f83.firebasestorage.app",
    messagingSenderId: "1073465178344",
    appId: "1:1073465178344:web:7aaed3909cc93974ec315d",
    measurementId: "G-ZKF356MWZ3"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
const auth = getAuth(app);
const db = getFirestore(app);
const storage = getStorage(app);

export { auth, db, storage };
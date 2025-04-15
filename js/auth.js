import { auth, db } from './firebase-config.js';
import { createUserWithEmailAndPassword, signInWithEmailAndPassword, signOut, onAuthStateChanged } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-auth.js";
import { doc, setDoc, getDoc } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-firestore.js";

// Check if user is already logged in
onAuthStateChanged(auth, (user) => {
    const currentPage = window.location.pathname.split('/').pop();
    
    if (user) {
        // User is signed in
        if (currentPage === 'index.html' || currentPage === 'register.html' || currentPage === '') {
            window.location.href = 'dashboard.html';
        }
    } else {
        // User is signed out
        if (currentPage !== 'index.html' && currentPage !== 'register.html' && currentPage !== '') {
            window.location.href = 'index.html';
        }
    }
});

// Login Form
const loginForm = document.getElementById('loginForm');
if (loginForm) {
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;
        const loginError = document.getElementById('loginError');
        
        try {
            await signInWithEmailAndPassword(auth, email, password);
            window.location.href = 'dashboard.html';
        } catch (error) {
            console.error("Error signing in:", error);
            loginError.style.display = 'block';
            
            switch (error.code) {
                case 'auth/invalid-email':
                    loginError.textContent = 'Invalid email address format.';
                    break;
                case 'auth/user-not-found':
                    loginError.textContent = 'No user found with this email address.';
                    break;
                case 'auth/wrong-password':
                    loginError.textContent = 'Incorrect password.';
                    break;
                default:
                    loginError.textContent = 'Failed to sign in. Please try again.';
            }
        }
    });
}

// Register Form
const registerForm = document.getElementById('registerForm');
if (registerForm) {
    registerForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const fullName = document.getElementById('fullName').value;
        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;
        const confirmPassword = document.getElementById('confirmPassword').value;
        const registerError = document.getElementById('registerError');
        
        // Validate password match
        if (password !== confirmPassword) {
            document.getElementById('confirmPasswordError').textContent = 'Passwords do not match';
            document.getElementById('confirmPasswordError').style.display = 'block';
            return;
        }
        
        try {
            // Create user with email and password
            const userCredential = await createUserWithEmailAndPassword(auth, email, password);
            const user = userCredential.user;
            
            // Add admin user info to Firestore
            await setDoc(doc(db, "admins", user.uid), {
                fullName: fullName,
                email: email,
                role: 'admin',
                createdAt: new Date()
            });
            
            // Redirect to dashboard
            window.location.href = 'dashboard.html';
        } catch (error) {
            console.error("Error registering:", error);
            registerError.style.display = 'block';
            
            switch (error.code) {
                case 'auth/email-already-in-use':
                    registerError.textContent = 'Email is already in use by another account.';
                    break;
                case 'auth/invalid-email':
                    registerError.textContent = 'Invalid email address format.';
                    break;
                case 'auth/weak-password':
                    registerError.textContent = 'Password is too weak. Use at least 6 characters.';
                    break;
                default:
                    registerError.textContent = 'Failed to register. Please try again.';
            }
        }
    });
}

// Logout functionality
const logoutBtn = document.getElementById('logoutBtn');
if (logoutBtn) {
    logoutBtn.addEventListener('click', async () => {
        try {
            await signOut(auth);
            window.location.href = 'index.html';
        } catch (error) {
            console.error("Error signing out:", error);
            alert("Failed to sign out. Please try again.");
        }
    });
}

// Function to get current admin data
export async function getCurrentAdmin() {
    const user = auth.currentUser;
    if (user) {
        const adminDoc = await getDoc(doc(db, "admins", user.uid));
        if (adminDoc.exists()) {
            return { id: user.uid, ...adminDoc.data() };
        }
    }
    return null;
}
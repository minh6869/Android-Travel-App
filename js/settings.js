import { auth, db, storage } from './firebase-config.js';
import { onAuthStateChanged, updatePassword, EmailAuthProvider, reauthenticateWithCredential, signOut } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-auth.js";
import { doc, getDoc, updateDoc, collection, addDoc, Timestamp } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-firestore.js";
import { ref, uploadBytes, getDownloadURL } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-storage.js";
import { getCurrentAdmin } from './auth.js';

// Initialize settings page
document.addEventListener('DOMContentLoaded', () => {
    // Check authentication
    onAuthStateChanged(auth, async (user) => {
        if (user) {
            // User is signed in, load settings
            await loadAdminProfile();
            setupTabsNavigation();
            setupEventListeners();
        } else {
            // User is signed out, redirect to login
            window.location.href = 'index.html';
        }
    });
});

// Load admin profile data
async function loadAdminProfile() {
    try {
        const admin = await getCurrentAdmin();
        
        if (admin) {
            document.getElementById('adminName').value = admin.fullName || '';
            document.getElementById('adminEmail').value = admin.email || '';
            document.getElementById('adminPhone').value = admin.phoneNumber || '';
            
            if (admin.profileImageUrl) {
                document.getElementById('adminProfileImage').src = admin.profileImageUrl;
            }
        }
    } catch (error) {
        console.error("Error loading admin profile:", error);
        alert('Failed to load profile data. Please try again.');
    }
}

// Setup tabs navigation
function setupTabsNavigation() {
    const tabButtons = document.querySelectorAll('.tab-btn');
    const tabContents = document.querySelectorAll('.tab-content');
    
    tabButtons.forEach(button => {
        button.addEventListener('click', () => {
            const tabId = button.getAttribute('data-tab');
            
            // Remove active class from all buttons and contents
            tabButtons.forEach(btn => btn.classList.remove('active'));
            tabContents.forEach(content => content.classList.remove('active'));
            
            // Add active class to current button and content
            button.classList.add('active');
            document.getElementById(`${tabId}-tab`).classList.add('active');
        });
    });
}

// Setup event listeners
function setupEventListeners() {
    // Account form submission
    const accountForm = document.getElementById('accountForm');
    if (accountForm) {
        accountForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            await updateAdminProfile();
        });
    }
    
    // System settings form submission
    const systemForm = document.getElementById('systemForm');
    if (systemForm) {
        systemForm.addEventListener('submit', (e) => {
            e.preventDefault();
            saveSystemSettings();
        });
    }
    
    // Notifications form submission
    const notificationsForm = document.getElementById('notificationsForm');
    if (notificationsForm) {
        notificationsForm.addEventListener('submit', (e) => {
            e.preventDefault();
            saveNotificationSettings();
        });
    }
    
    // Password form submission
    const passwordForm = document.getElementById('passwordForm');
    if (passwordForm) {
        passwordForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            await changePassword();
        });
    }
    
    // 2FA checkbox
    const enable2fa = document.getElementById('enable2fa');
    if (enable2fa) {
        enable2fa.addEventListener('change', () => {
            document.getElementById('2faSetup').style.display = enable2fa.checked ? 'block' : 'none';
        });
    }
    
    // Verify 2FA button
    const verify2faBtn = document.getElementById('verify2faBtn');
    if (verify2faBtn) {
        verify2faBtn.addEventListener('click', () => {
            verify2FA();
        });
    }
    
    // Logout all devices button
    const logoutAllBtn = document.getElementById('logoutAllBtn');
    if (logoutAllBtn) {
        logoutAllBtn.addEventListener('click', () => {
            logoutAllDevices();
        });
    }
    
    // Profile image upload
    const profileImageUpload = document.getElementById('profileImageUpload');
    if (profileImageUpload) {
        profileImageUpload.addEventListener('change', (e) => {
            const file = e.target.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = (e) => {
                    document.getElementById('adminProfileImage').src = e.target.result;
                };
                reader.readAsDataURL(file);
            }
        });
    }
    
    // Backup related event listeners
    setupBackupEventListeners();
    
    // Logout button
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
}

// Update admin profile
async function updateAdminProfile() {
    try {
        const user = auth.currentUser;
        
        if (!user) {
            throw new Error("User not authenticated");
        }
        
        const fullName = document.getElementById('adminName').value;
        const phoneNumber = document.getElementById('adminPhone').value;
        const profileImage = document.getElementById('profileImageUpload').files[0];
        
        let profileData = {
            fullName,
            phoneNumber,
            updatedAt: Timestamp.now()
        };
        
        // Upload profile image if provided
        if (profileImage) {
            const storageRef = ref(storage, `admins/${user.uid}_${Date.now()}`);
            const uploadResult = await uploadBytes(storageRef, profileImage);
            const downloadURL = await getDownloadURL(uploadResult.ref);
            
            profileData.profileImageUrl = downloadURL;
        }
        
        // Update admin document
        await updateDoc(doc(db, "admins", user.uid), profileData);
        
        alert('Profile updated successfully!');
    } catch (error) {
        console.error("Error updating profile:", error);
        alert('Failed to update profile. Please try again.');
    }
}

// Save system settings
function saveSystemSettings() {
    try {
        const language = document.getElementById('language').value;
        const timezone = document.getElementById('timezone').value;
        const dateFormat = document.getElementById('dateFormat').value;
        const currency = document.getElementById('currency').value;
        const darkMode = document.getElementById('darkMode').checked;
         // Save settings to localStorage for demo purposes
        // In a real application, you might want to save these to Firestore
        const systemSettings = {
            language,
            timezone,
            dateFormat,
            currency,
            darkMode,
            updatedAt: new Date().toISOString()
        };
        
        localStorage.setItem('systemSettings', JSON.stringify(systemSettings));
        
        alert('System settings saved successfully!');
    } catch (error) {
        console.error("Error saving system settings:", error);
        alert('Failed to save system settings. Please try again.');
    }
}

// Save notification settings
function saveNotificationSettings() {
    try {
        const emailNewBooking = document.getElementById('emailNewBooking').checked;
        const emailNewUser = document.getElementById('emailNewUser').checked;
        const emailNewReview = document.getElementById('emailNewReview').checked;
        const emailSystemAlerts = document.getElementById('emailSystemAlerts').checked;
        const dashboardAlerts = document.getElementById('dashboardAlerts').checked;
        const notificationSound = document.getElementById('notificationSound').value;
        
        // Save settings to localStorage for demo purposes
        // In a real application, you might want to save these to Firestore
        const notificationSettings = {
            emailNotifications: {
                newBooking: emailNewBooking,
                newUser: emailNewUser,
                newReview: emailNewReview,
                systemAlerts: emailSystemAlerts
            },
            dashboardNotifications: {
                showAlerts: dashboardAlerts,
                sound: notificationSound
            },
            updatedAt: new Date().toISOString()
        };
        
        localStorage.setItem('notificationSettings', JSON.stringify(notificationSettings));
        
        alert('Notification settings saved successfully!');
    } catch (error) {
        console.error("Error saving notification settings:", error);
        alert('Failed to save notification settings. Please try again.');
    }
}

// Change password
async function changePassword() {
    try {
        const user = auth.currentUser;
        
        if (!user) {
            throw new Error("User not authenticated");
        }
        
        const currentPassword = document.getElementById('currentPassword').value;
        const newPassword = document.getElementById('newPassword').value;
        const confirmPassword = document.getElementById('confirmPassword').value;
        
        // Validate password match
        if (newPassword !== confirmPassword) {
            alert('New password and confirmation do not match.');
            return;
        }
        
        // Validate password strength
        if (newPassword.length < 6) {
            alert('New password must be at least 6 characters long.');
            return;
        }
        
        // Re-authenticate user before changing password
        const credential = EmailAuthProvider.credential(user.email, currentPassword);
        
        try {
            await reauthenticateWithCredential(user, credential);
        } catch (authError) {
            console.error("Authentication error:", authError);
            alert('Current password is incorrect. Please try again.');
            return;
        }
        
        // Update password
        await updatePassword(user, newPassword);
        
        // Clear form
        document.getElementById('passwordForm').reset();
        
        alert('Password updated successfully!');
    } catch (error) {
        console.error("Error changing password:", error);
        alert('Failed to change password. Please try again.');
    }
}

// Verify 2FA setup
function verify2FA() {
    try {
        const verificationCode = document.getElementById('verificationCode').value;
        
        // This is a demo implementation
        // In a real application, you would validate the code with a proper 2FA library
        
        if (verificationCode.length !== 6 || !/^\d+$/.test(verificationCode)) {
            alert('Please enter a valid 6-digit verification code.');
            return;
        }
        
        // Simulate successful verification
        alert('Two-factor authentication enabled successfully!');
        
        // Save 2FA status to localStorage for demo purposes
        localStorage.setItem('2faEnabled', 'true');
    } catch (error) {
        console.error("Error verifying 2FA:", error);
        alert('Failed to verify 2FA code. Please try again.');
    }
}

// Logout all devices
function logoutAllDevices() {
    try {
        // This is a demo implementation
        // In a real application, you would use Firebase Auth's revokeRefreshTokens method
        // or implement a custom solution with Firestore to track and invalidate sessions
        
        if (confirm('Are you sure you want to log out from all other devices?')) {
            // Simulate successful operation
            alert('You have been logged out from all other devices.');
            
            // Update sessions table for demo purposes
            const sessionsTable = document.getElementById('sessionsTable');
            if (sessionsTable) {
                sessionsTable.innerHTML = `
                    <tr>
                        <td>
                            <div class="d-flex align-center gap-2">
                                <i class="fas fa-laptop"></i>
                                <span>Chrome on Windows</span>
                                <span class="badge badge-primary">Current</span>
                            </div>
                        </td>
                        <td>Hanoi, Vietnam</td>
                        <td>Just now</td>
                        <td>-</td>
                    </tr>
                `;
            }
        }
    } catch (error) {
        console.error("Error logging out all devices:", error);
        alert('Failed to log out all devices. Please try again.');
    }
}

// Setup backup related event listeners
function setupBackupEventListeners() {
    // Create backup button
    const createBackupBtn = document.getElementById('createBackupBtn');
    if (createBackupBtn) {
        createBackupBtn.addEventListener('click', createBackup);
    }
    
    // Save backup schedule button
    const saveScheduleBtn = document.getElementById('saveScheduleBtn');
    if (saveScheduleBtn) {
        saveScheduleBtn.addEventListener('click', saveBackupSchedule);
    }
    
    // Confirm restore checkbox
    const confirmRestore = document.getElementById('confirmRestore');
    const restoreBtn = document.getElementById('restoreBtn');
    
    if (confirmRestore && restoreBtn) {
        confirmRestore.addEventListener('change', () => {
            restoreBtn.disabled = !confirmRestore.checked;
        });
    }
    
    // Restore button
    if (restoreBtn) {
        restoreBtn.addEventListener('click', restoreBackup);
    }
}

// Create backup
async function createBackup() {
    try {
        const backupType = document.getElementById('backupType').value;
        
        // This is a demo implementation
        // In a real application, you would fetch data from Firestore and generate a backup file
        
        // Simulate fetching data
        alert(`Creating ${backupType} backup... Please wait.`);
        
        // Simulate processing time
        await new Promise(resolve => setTimeout(resolve, 1500));
        
        // Generate a dummy backup file
        const backupData = {
            type: backupType,
            timestamp: new Date().toISOString(),
            data: {
                message: "This is a simulated backup file for demo purposes."
            }
        };
        
        // Convert to JSON string
        const backupJson = JSON.stringify(backupData, null, 2);
        
        // Create download link
        const blob = new Blob([backupJson], { type: 'application/json' });
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        
        link.href = url;
        link.download = `travel_admin_${backupType}_backup_${new Date().toISOString().split('T')[0]}.json`;
        document.body.appendChild(link);
        
        // Trigger download
        link.click();
        
        // Clean up
        document.body.removeChild(link);
        URL.revokeObjectURL(url);
        
        // Add to recent backups table
        const backupsTable = document.getElementById('backupsTable');
        if (backupsTable) {
            const newRow = document.createElement('tr');
            newRow.innerHTML = `
                <td>${new Date().toLocaleString()}</td>
                <td>${backupType.charAt(0).toUpperCase() + backupType.slice(1)} Backup</td>
                <td>0.1 MB</td>
                <td>Admin</td>
                <td>
                    <button class="btn btn-secondary btn-sm">
                        <i class="fas fa-download"></i> Download
                    </button>
                    <button class="btn btn-danger btn-sm">
                        <i class="fas fa-trash"></i> Delete
                    </button>
                </td>
            `;
            
            backupsTable.insertBefore(newRow, backupsTable.firstChild);
        }
    } catch (error) {
        console.error("Error creating backup:", error);
        alert('Failed to create backup. Please try again.');
    }
}

// Save backup schedule
function saveBackupSchedule() {
    try {
        const enableScheduledBackups = document.getElementById('enableScheduledBackups').checked;
        const backupFrequency = document.getElementById('backupFrequency').value;
        const backupTime = document.getElementById('backupTime').value;
        const retentionPeriod = document.getElementById('retentionPeriod').value;
        
        // Save settings to localStorage for demo purposes
        const backupSettings = {
            enabled: enableScheduledBackups,
            frequency: backupFrequency,
            time: backupTime,
            retention: retentionPeriod,
            updatedAt: new Date().toISOString()
        };
        
        localStorage.setItem('backupSettings', JSON.stringify(backupSettings));
        
        alert('Backup schedule saved successfully!');
    } catch (error) {
        console.error("Error saving backup schedule:", error);
        alert('Failed to save backup schedule. Please try again.');
    }
}

// Restore backup
function restoreBackup() {
    try {
        const restoreFile = document.getElementById('restoreFile').files[0];
        
        if (!restoreFile) {
            alert('Please select a backup file to restore.');
            return;
        }
        
        // Confirm restoration
        if (!confirm('WARNING: This will overwrite your existing data. Are you absolutely sure you want to continue?')) {
            return;
        }
        
        // This is a demo implementation
        // In a real application, you would parse the backup file and restore data to Firestore
        
        // Simulate restoration process
        alert('Restoring backup... Please wait.');
        
        // Simulate processing time
        setTimeout(() => {
            alert('Backup restored successfully! The page will now reload.');
            window.location.reload();
        }, 2000);
    } catch (error) {
        console.error("Error restoring backup:", error);
        alert('Failed to restore backup. Please try again.');
    }
}
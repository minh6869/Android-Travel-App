import { auth, db, storage } from './firebase-config.js';
import { onAuthStateChanged } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-auth.js";
import { collection, doc, getDocs, getDoc, addDoc, updateDoc, deleteDoc, query, where, Timestamp } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-firestore.js";
import { ref, uploadBytes, getDownloadURL, deleteObject } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-storage.js";

// Initialize users page
document.addEventListener('DOMContentLoaded', () => {
    // Check authentication
    onAuthStateChanged(auth, (user) => {
        if (user) {
            // User is signed in, load users
            loadUsers();
            
            // Setup event listeners
            setupEventListeners();
        } else {
            // User is signed out, redirect to login
            window.location.href = 'index.html';
        }
    });
});

// Load users from Firestore
async function loadUsers(searchQuery = '') {
    try {
        const usersTable = document.getElementById('usersTable');
        usersTable.innerHTML = '<tr><td colspan="7" class="text-center">Loading users...</td></tr>';
        
        const usersQuery = collection(db, "users");
        const querySnapshot = await getDocs(usersQuery);
        
        if (querySnapshot.empty) {
            usersTable.innerHTML = '<tr><td colspan="7" class="text-center">No users found</td></tr>';
            return;
        }
        
        let tableHTML = '';
        
        // Process each user
        querySnapshot.forEach((doc) => {
            const user = doc.data();
            const userId = doc.id;
            
            // Apply search filter if provided
            if (searchQuery) {
                const fullName = user.fullName || '';
                const userName = user.userName || '';
                const email = user.email || '';
                
                const searchLower = searchQuery.toLowerCase();
                const fullNameLower = fullName.toLowerCase();
                const userNameLower = userName.toLowerCase();
                const emailLower = email.toLowerCase();
                
                if (!fullNameLower.includes(searchLower) && 
                    !userNameLower.includes(searchLower) && 
                    !emailLower.includes(searchLower)) {
                    return;
                }
            }
            
            // Get favorite tours count
            const favoritesTours = user.favoriteTour || [];
            
            tableHTML += `
                <tr>
                    <td>
                        <img src="${user.userImageUrl || 'https://via.placeholder.com/40'}" alt="${user.fullName || 'User'}" style="width: 40px; height: 40px; border-radius: 50%; object-fit: cover;">
                    </td>
                    <td>${user.fullName || 'N/A'}</td>
                    <td>${user.userName || 'N/A'}</td>
                    <td>${user.email || 'N/A'}</td>
                    <td>${user.phoneNumber || 'N/A'}</td>
                    <td>
                        <button class="btn btn-secondary btn-sm view-favorites" data-id="${userId}" data-name="${user.fullName || 'User'}">
                            ${favoritesTours.length} Tours
                        </button>
                    </td>
                    <td>
                        <div class="d-flex gap-2">
                            <button class="btn btn-secondary btn-sm edit-user" data-id="${userId}">
                                <i class="fas fa-edit"></i>
                            </button>
                            <button class="btn btn-danger btn-sm delete-user" data-id="${userId}">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        });
        
        usersTable.innerHTML = tableHTML;
        
        // Add event listeners to action buttons
        document.querySelectorAll('.edit-user').forEach(button => {
            button.addEventListener('click', (e) => {
                const userId = e.currentTarget.getAttribute('data-id');
                openEditUserModal(userId);
            });
        });
        
        document.querySelectorAll('.delete-user').forEach(button => {
            button.addEventListener('click', (e) => {
                const userId = e.currentTarget.getAttribute('data-id');
                openDeleteModal(userId);
            });
        });
        
        document.querySelectorAll('.view-favorites').forEach(button => {
            button.addEventListener('click', (e) => {
                const userId = e.currentTarget.getAttribute('data-id');
                const userName = e.currentTarget.getAttribute('data-name');
                openFavoritesModal(userId, userName);
            });
        });
        
    } catch (error) {
        console.error("Error loading users:", error);
        document.getElementById('usersTable').innerHTML = 
            '<tr><td colspan="7" class="text-center">Error loading users</td></tr>';
    }
}

// Setup event listeners for the page
function setupEventListeners() {
    // Add User button
    const addUserBtn = document.getElementById('addUserBtn');
    if (addUserBtn) {
        addUserBtn.addEventListener('click', () => {
            openAddUserModal();
        });
    }
    
    // Search input
    const searchUser = document.getElementById('searchUser');
    if (searchUser) {
        searchUser.addEventListener('input', () => {
            loadUsers(searchUser.value);
        });
    }
    
    // Close modal buttons
    document.querySelectorAll('.close-modal').forEach(button => {
        button.addEventListener('click', () => {
            document.querySelectorAll('.modal').forEach(modal => {
                modal.style.display = 'none';
            });
        });
    });
    
    // Save User button
    const saveUserBtn = document.getElementById('saveUserBtn');
    if (saveUserBtn) {
        saveUserBtn.addEventListener('click', saveUser);
    }
    
    // User image preview
    const userImage = document.getElementById('userImage');
    if (userImage) {
        userImage.addEventListener('change', (e) => {
            const file = e.target.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = (e) => {
                    document.getElementById('previewImg').src = e.target.result;
                    document.getElementById('imagePreview').style.display = 'block';
                };
                reader.readAsDataURL(file);
            }
        });
    }
    
    // Confirm delete button
    const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');
    if (confirmDeleteBtn) {
        confirmDeleteBtn.addEventListener('click', deleteUser);
    }
}

// Open modal to add a new user
function openAddUserModal() {
    document.getElementById('modalTitle').textContent = 'Add New User';
    document.getElementById('userForm').reset();
    document.getElementById('userId').value = '';
    document.getElementById('imagePreview').style.display = 'none';
    document.getElementById('userModal').style.display = 'flex';
}

// Open modal to edit an existing user
async function openEditUserModal(userId) {
    try {
        document.getElementById('modalTitle').textContent = 'Edit User';
        document.getElementById('userId').value = userId;
        
        const userDoc = await getDoc(doc(db, "users", userId));
        
        if (userDoc.exists()) {
            const user = userDoc.data();
            
            document.getElementById('fullName').value = user.fullName || '';
            document.getElementById('userName').value = user.userName || '';
            document.getElementById('email').value = user.email || '';
            document.getElementById('phoneNumber').value = user.phoneNumber || '';
            
            if (user.userImageUrl) {
                document.getElementById('previewImg').src = user.userImageUrl;
                document.getElementById('imagePreview').style.display = 'block';
            } else {
                document.getElementById('imagePreview').style.display = 'none';
            }
            
            document.getElementById('userModal').style.display = 'flex';
        } else {
            alert('User not found!');
        }
    } catch (error) {
        console.error("Error opening edit modal:", error);
        alert('Failed to load user data. Please try again.');
    }
}

// Save user (create or update)
async function saveUser() {
    try {
        const userId = document.getElementById('userId').value;
        const fullName = document.getElementById('fullName').value;
        const userName = document.getElementById('userName').value;
        const email = document.getElementById('email').value;
        const phoneNumber = document.getElementById('phoneNumber').value;
        const imageFile = document.getElementById('userImage').files[0];
        
        // Validate required fields
        if (!fullName || !userName || !email) {
            alert('Please fill in all required fields.');
            return;
        }
        
        let userData = {
            fullName,
            userName,
            email,
            phoneNumber,
            updatedAt: Timestamp.now()
        };
        
        // Handle image upload if a new image is selected
        if (imageFile) {
            const storageRef = ref(storage, `users/${Date.now()}_${imageFile.name}`);
            const uploadResult = await uploadBytes(storageRef, imageFile);
            const downloadURL = await getDownloadURL(uploadResult.ref);
            
            userData.userImageUrl = downloadURL;
        }
        
        if (userId) {
            // Update existing user
            await updateDoc(doc(db, "users", userId), userData);
        } else {
            // Create new user
            userData.createdAt = Timestamp.now();
            userData.favoriteTour = [];
            await addDoc(collection(db, "users"), userData);
        }
        
        // Close modal and reload users
        document.getElementById('userModal').style.display = 'none';
        loadUsers(document.getElementById('searchUser').value);
        
    } catch (error) {
        console.error("Error saving user:", error);
        alert('Failed to save user. Please try again.');
    }
}

// Open modal to confirm user deletion
function openDeleteModal(userId) {
    document.getElementById('deleteId').value = userId;
    document.getElementById('deleteModal').style.display = 'flex';
}

// Delete a user
async function deleteUser() {
    try {
        const userId = document.getElementById('deleteId').value;
        
        // Get user data to delete image from storage if needed
        const userDoc = await getDoc(doc(db, "users", userId));
        
        if (userDoc.exists()) {
            const user = userDoc.data();
            
            // Delete user image from storage if it exists
            if (user.userImageUrl) {
                try {
                    const imageRef = ref(storage, user.userImageUrl);
                    await deleteObject(imageRef);
                } catch (imageError) {
                    console.error("Error deleting image:", imageError);
                    // Continue with user deletion even if image deletion fails
                }
            }
            
            // Delete the user document
            await deleteDoc(doc(db, "users", userId));
            
            // Close modal and reload users
            document.getElementById('deleteModal').style.display = 'none';
            loadUsers(document.getElementById('searchUser').value);
        } else {
            throw new Error("User not found");
        }
    } catch (error) {
        console.error("Error deleting user:", error);
        alert('Failed to delete user. Please try again.');
    }
}

// Open modal to view favorite tours
async function openFavoritesModal(userId, userName) {
    try {
        document.getElementById('userNameForFavorites').textContent = `${userName}'s Favorite Tours`;
        
        const userDoc = await getDoc(doc(db, "users", userId));
        
        if (userDoc.exists()) {
            const user = userDoc.data();
            const favoriteTours = user.favoriteTour || [];
            
            const favoritesToursTable = document.getElementById('favoritesToursTable');
            
            if (favoriteTours.length === 0) {
                favoritesToursTable.innerHTML = '<tr><td colspan="4" class="text-center">No favorite tours found</td></tr>';
            } else {
                favoritesToursTable.innerHTML = '<tr><td colspan="4" class="text-center">Loading favorite tours...</td></tr>';
                
                let tableHTML = '';
                
                // Fetch tour details for each favorite
                for (const tourId of favoriteTours) {
                    try {
                        const tourDoc = await getDoc(doc(db, "tours", tourId));
                        
                        if (tourDoc.exists()) {
                            const tour = tourDoc.data();
                            
                            // Format status badge
                            let statusBadge = '';
                            switch (tour.status) {
                                case 'active':
                                    statusBadge = '<span class="badge badge-success">Active</span>';
                                    break;
                                case 'inactive':
                                    statusBadge = '<span class="badge badge-danger">Inactive</span>';
                                    break;
                                case 'draft':
                                    statusBadge = '<span class="badge badge-primary">Draft</span>';
                                    break;
                                default:
                                    statusBadge = '<span class="badge badge-primary">Unknown</span>';
                            }
                            
                            tableHTML += `
                                <tr>
                                    <td>
                                        <img src="${tour.tourImageUrl || 'https://via.placeholder.com/40x40'}" alt="${tour.title}" style="width: 40px; height: 40px; object-fit: cover; border-radius: 4px;">
                                    </td>
                                    <td>${tour.title || 'Unknown Tour'}</td>
                                    <td>${tour.category || 'N/A'}</td>
                                    <td>${statusBadge}</td>
                                </tr>
                            `;
                        } else {
                            // Tour not found, but still show in the list
                            tableHTML += `
                                <tr>
                                    <td>
                                        <img src="https://via.placeholder.com/40x40" alt="Not Found" style="width: 40px; height: 40px; object-fit: cover; border-radius: 4px;">
                                    </td>
                                    <td>Tour Not Found (ID: ${tourId.substring(0, 8)})</td>
                                    <td>N/A</td>
                                    <td><span class="badge badge-danger">Not Found</span></td>
                                </tr>
                            `;
                        }
                    } catch (tourError) {
                        console.error(`Error fetching tour ${tourId}:`, tourError);
                        
                        // Add error row
                        tableHTML += `
                            <tr>
                                <td>
                                    <img src="https://via.placeholder.com/40x40" alt="Error" style="width: 40px; height: 40px; object-fit: cover; border-radius: 4px;">
                                </td>
                                <td>Error Loading Tour (ID: ${tourId.substring(0, 8)})</td>
                                <td>N/A</td>
                                <td><span class="badge badge-danger">Error</span></td>
                            </tr>
                        `;
                    }
                }
                
                favoritesToursTable.innerHTML = tableHTML;
            }
        } else {
            document.getElementById('favoritesToursTable').innerHTML = 
                '<tr><td colspan="4" class="text-center">User not found</td></tr>';
        }
        
        document.getElementById('favoritesModal').style.display = 'flex';
    } catch (error) {
        console.error("Error opening favorites modal:", error);
        alert('Failed to load favorite tours. Please try again.');
    }
}
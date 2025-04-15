import { auth, db, storage } from './firebase-config.js';
import { onAuthStateChanged } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-auth.js";
import { collection, doc, getDocs, getDoc, addDoc, updateDoc, deleteDoc, query, where, orderBy, Timestamp } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-firestore.js";
import { ref, uploadBytes, getDownloadURL, deleteObject } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-storage.js";

// Initialize tours page
document.addEventListener('DOMContentLoaded', () => {
    // Check authentication
    onAuthStateChanged(auth, (user) => {
        if (user) {
            // User is signed in, load tours
            loadTours();
            
            // Setup event listeners
            setupEventListeners();
        } else {
            // User is signed out, redirect to login
            window.location.href = 'index.html';
        }
    });
});

// Load tours from Firestore
async function loadTours(statusFilter = 'all', searchQuery = '') {
    try {
        const toursTable = document.getElementById('toursTable');
        toursTable.innerHTML = '<tr><td colspan="6" class="text-center">Loading tours...</td></tr>';
        
        let toursQuery = collection(db, "tours");
        
        // Apply status filter if not 'all'
        if (statusFilter !== 'all') {
            toursQuery = query(toursQuery, where("status", "==", statusFilter));
        }
        
        const querySnapshot = await getDocs(toursQuery);
        
        if (querySnapshot.empty) {
            toursTable.innerHTML = '<tr><td colspan="6" class="text-center">No tours found</td></tr>';
            return;
        }
        
        let tableHTML = '';
        
        // Process each tour
        querySnapshot.forEach((doc) => {
            const tour = doc.data();
            const tourId = doc.id;
            
            // Apply search filter if provided
            if (searchQuery && !tour.title?.toLowerCase().includes(searchQuery.toLowerCase())) {
                return;
            }
            
            // Format status badge
            let statusBadge = '';
            switch (tour.status) {
                case 'active':
                    statusBadge = '<span class="badge badge-success">Active</span>';
                    break;
                case 'inactive':
                    statusBadge = '<span class="badge badge-danger">Inactive</span>';
                    break;
                case 'archived': // Changed from 'draft' to 'archived'
                    statusBadge = '<span class="badge badge-primary">Archived</span>';
                    break;
                case 'completed': // Added completed status
                    statusBadge = '<span class="badge badge-success">Completed</span>';
                    break;
                case 'pending': // Added pending status
                    statusBadge = '<span class="badge badge-primary">Pending</span>';
                    break;
                default:
                    statusBadge = '<span class="badge badge-primary">Unknown</span>';
            }
            
            // Generate rating stars
            let stars = '';
            const ratingValue = parseFloat(tour.rating) || 0;
            for (let i = 1; i <= 5; i++) {
                if (i <= ratingValue) {
                    stars += '<i class="fas fa-star text-primary"></i>';
                } else if (i - 0.5 <= ratingValue) {
                    stars += '<i class="fas fa-star-half-alt text-primary"></i>';
                } else {
                    stars += '<i class="far fa-star text-primary"></i>';
                }
            }
            
            tableHTML += `
                <tr>
                    <td>
                        <img src="${tour.tourImageUrl || 'https://via.placeholder.com/50x50'}" alt="${tour.title || 'Tour'}" style="width: 50px; height: 50px; object-fit: cover; border-radius: 4px;">
                    </td>
                    <td>
                        <div style="font-weight: 500;">${tour.title || 'Unnamed Tour'}</div>
                        <div style="font-size: 0.8rem; color: var(--dark-gray);">ID: ${tourId.substring(0, 8)}</div>
                    </td>
                    <td>${formatCategory(tour.category) || 'N/A'}</td>
                    <td>${stars} <span style="margin-left: 5px;">${ratingValue.toFixed(1)}</span></td>
                    <td>${statusBadge}</td>
                    <td>
                        <div class="d-flex gap-2">
                            <button class="btn btn-secondary btn-sm edit-tour" data-id="${tourId}">
                                <i class="fas fa-edit"></i>
                            </button>
                            <button class="btn btn-primary btn-sm manage-dates" data-id="${tourId}" data-title="${tour.title || 'Tour'}">
                                <i class="fas fa-calendar"></i>
                            </button>
                            <button class="btn btn-danger btn-sm delete-tour" data-id="${tourId}">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        });
        
        toursTable.innerHTML = tableHTML;
        
        // Add event listeners to action buttons
        document.querySelectorAll('.edit-tour').forEach(button => {
            button.addEventListener('click', (e) => {
                const tourId = e.currentTarget.getAttribute('data-id');
                openEditTourModal(tourId);
            });
        });
        
        document.querySelectorAll('.manage-dates').forEach(button => {
            button.addEventListener('click', (e) => {
                const tourId = e.currentTarget.getAttribute('data-id');
                const tourTitle = e.currentTarget.getAttribute('data-title');
                openDatesModal(tourId, tourTitle);
            });
        });
        
        document.querySelectorAll('.delete-tour').forEach(button => {
            button.addEventListener('click', (e) => {
                const tourId = e.currentTarget.getAttribute('data-id');
                openDeleteModal(tourId);
            });
        });
        
    } catch (error) {
        console.error("Error loading tours:", error);
        document.getElementById('toursTable').innerHTML = 
            '<tr><td colspan="6" class="text-center">Error loading tours</td></tr>';
    }
}

// Format category name for display
function formatCategory(category) {
    if (!category) return 'N/A';
    
    // Capitalize first letter
    return category.charAt(0).toUpperCase() + category.slice(1);
}

// Setup event listeners for the page
function setupEventListeners() {
    // Add Tour button
    const addTourBtn = document.getElementById('addTourBtn');
    if (addTourBtn) {
        addTourBtn.addEventListener('click', () => {
            openAddTourModal();
        });
    }
    
    // Status filter
    const statusFilter = document.getElementById('statusFilter');
    if (statusFilter) {
        statusFilter.addEventListener('change', () => {
            const searchQuery = document.getElementById('searchTour').value;
            loadTours(statusFilter.value, searchQuery);
        });
    }
    
    // Search input
    const searchTour = document.getElementById('searchTour');
    if (searchTour) {
        searchTour.addEventListener('input', () => {
            const statusFilter = document.getElementById('statusFilter').value;
            loadTours(statusFilter, searchTour.value);
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
    
    // Close modal when clicking outside the content
    document.querySelectorAll('.modal').forEach(modal => {
        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                modal.style.display = 'none';
            }
        });
    });
    
    // Prevent click events from bubbling up from modal content
    document.querySelectorAll('.modal-content').forEach(content => {
        content.addEventListener('click', (e) => {
            e.stopPropagation();
        });
    });
    
    // Save Tour button
    const saveTourBtn = document.getElementById('saveTourBtn');
    if (saveTourBtn) {
        saveTourBtn.addEventListener('click', saveTour);
    }
    
    // Tour image preview
    const tourImage = document.getElementById('tourImage');
    if (tourImage) {
        tourImage.addEventListener('change', (e) => {
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
        confirmDeleteBtn.addEventListener('click', deleteTour);
    }
    
    // Add Date button
    const addDateBtn = document.getElementById('addDateBtn');
    if (addDateBtn) {
        addDateBtn.addEventListener('click', addOrUpdateDate);
    }
    
    // Handle keyboard events for modals
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            document.querySelectorAll('.modal').forEach(modal => {
                if (modal.style.display === 'flex') {
                    modal.style.display = 'none';
                }
            });
        }
    });
}

// Open modal to add a new tour
function openAddTourModal() {
    document.getElementById('modalTitle').textContent = 'Add New Tour';
    document.getElementById('tourForm').reset();
    document.getElementById('tourId').value = '';
    document.getElementById('imagePreview').style.display = 'none';
    document.getElementById('tourModal').style.display = 'flex';
    
    // Set default status to active
    document.getElementById('status').value = 'active';
    
    // Scroll modal to top
    setTimeout(() => {
        const modalBody = document.querySelector('#tourModal .modal-body');
        if (modalBody) modalBody.scrollTop = 0;
    }, 100);
}

// Open modal to edit an existing tour
async function openEditTourModal(tourId) {
    try {
        document.getElementById('modalTitle').textContent = 'Edit Tour';
        document.getElementById('tourId').value = tourId;
        
        const tourDoc = await getDoc(doc(db, "tours", tourId));
        
        if (tourDoc.exists()) {
            const tour = tourDoc.data();
            
            document.getElementById('title').value = tour.title || '';
            document.getElementById('description').value = tour.description || '';
            document.getElementById('category').value = tour.category || '';
            document.getElementById('status').value = tour.status || 'active';
            document.getElementById('providerPhone').value = tour.providerPhone || '';
            document.getElementById('pickupLoc').value = tour.pickupLoc || '';
            document.getElementById('address').value = tour.address || '';
            
            if (tour.tourImageUrl) {
                document.getElementById('previewImg').src = tour.tourImageUrl;
                document.getElementById('imagePreview').style.display = 'block';
            } else {
                document.getElementById('imagePreview').style.display = 'none';
            }
            
            document.getElementById('tourModal').style.display = 'flex';
            
            // Scroll modal to top
            setTimeout(() => {
                const modalBody = document.querySelector('#tourModal .modal-body');
                if (modalBody) modalBody.scrollTop = 0;
            }, 100);
        } else {
            alert('Tour not found!');
        }
    } catch (error) {
        console.error("Error opening edit modal:", error);
        alert('Failed to load tour data. Please try again.');
    }
}

// Save tour (create or update)
async function saveTour() {
    try {
        const tourId = document.getElementById('tourId').value;
        const title = document.getElementById('title').value;
        const description = document.getElementById('description').value;
        const category = document.getElementById('category').value;
        const status = document.getElementById('status').value;
        const providerPhone = document.getElementById('providerPhone').value;
        const pickupLoc = document.getElementById('pickupLoc').value;
        const address = document.getElementById('address').value;
        const imageFile = document.getElementById('tourImage').files[0];
        
        // Validate required fields
        if (!title || !description || !category || !status) {
            alert('Please fill in all required fields.');
            return;
        }
        
        let tourData = {
            title,
            description,
            category,
            status,
            providerPhone,
            pickupLoc,
            address,
            updatedAt: Timestamp.now()
        };
        
        // Handle image upload if a new image is selected
        if (imageFile) {
            const storageRef = ref(storage, `tours/${Date.now()}_${imageFile.name}`);
            const uploadResult = await uploadBytes(storageRef, imageFile);
            const downloadURL = await getDownloadURL(uploadResult.ref);
            
            tourData.tourImageUrl = downloadURL;
        }
        
        if (tourId) {
            // Update existing tour
            await updateDoc(doc(db, "tours", tourId), tourData);
        } else {
            // Create new tour
            tourData.createdAt = Timestamp.now();
            tourData.rating = 0;
            await addDoc(collection(db, "tours"), tourData);
        }
        
        // Close modal and reload tours
        document.getElementById('tourModal').style.display = 'none';
        loadTours(document.getElementById('statusFilter').value, document.getElementById('searchTour').value);
        
    } catch (error) {
        console.error("Error saving tour:", error);
        alert('Failed to save tour. Please try again.');
    }
}

// Open modal to confirm tour deletion
function openDeleteModal(tourId) {
    document.getElementById('deleteId').value = tourId;
    document.getElementById('deleteModal').style.display = 'flex';
}

// Delete a tour
async function deleteTour() {
    try {
        const tourId = document.getElementById('deleteId').value;
        
        // Get tour data to delete image from storage if needed
        const tourDoc = await getDoc(doc(db, "tours", tourId));
        
        if (tourDoc.exists()) {
            const tour = tourDoc.data();
            
            // Delete tour image from storage if it exists
            if (tour.tourImageUrl) {
                try {
                    const imageRef = ref(storage, tour.tourImageUrl);
                    await deleteObject(imageRef);
                } catch (imageError) {
                    console.error("Error deleting image:", imageError);
                    // Continue with tour deletion even if image deletion fails
                }
            }
            
            // Delete available dates subcollection
            const datesSnapshot = await getDocs(collection(db, "tours", tourId, "availableDates"));
            const deleteDatePromises = datesSnapshot.docs.map(dateDoc => 
                deleteDoc(doc(db, "tours", tourId, "availableDates", dateDoc.id))
            );
            
            await Promise.all(deleteDatePromises);
            
            // Delete the tour document
            await deleteDoc(doc(db, "tours", tourId));
            
            // Close modal and reload tours
            document.getElementById('deleteModal').style.display = 'none';
            loadTours(document.getElementById('statusFilter').value, document.getElementById('searchTour').value);
        } else {
            throw new Error("Tour not found");
        }
    } catch (error) {
        console.error("Error deleting tour:", error);
        alert('Failed to delete tour. Please try again.');
    }
}

// Open modal to manage available dates
async function openDatesModal(tourId, tourTitle) {
    try {
        document.getElementById('dateTourId').value = tourId;
        document.getElementById('dateId').value = '';
        document.getElementById('tourTitleForDates').textContent = tourTitle;
        document.getElementById('dateForm').reset();
        
        // Load available dates
        await loadAvailableDates(tourId);
        
        document.getElementById('datesModal').style.display = 'flex';
        
        // Scroll modal to top
        setTimeout(() => {
            const modalBody = document.querySelector('#datesModal .modal-body');
            if (modalBody) modalBody.scrollTop = 0;
        }, 100);
    } catch (error) {
        console.error("Error opening dates modal:", error);
        alert('Failed to load available dates. Please try again.');
    }
}

// Load available dates for a tour
async function loadAvailableDates(tourId) {
    try {
        const datesTable = document.getElementById('datesTable');
        datesTable.innerHTML = '<tr><td colspan="5" class="text-center">Loading dates...</td></tr>';
        
        const datesQuery = query(
            collection(db, "tours", tourId, "availableDates"),
            orderBy("date", "asc")
        );
        
        const querySnapshot = await getDocs(datesQuery);
        
        if (querySnapshot.empty) {
            datesTable.innerHTML = '<tr><td colspan="5" class="text-center">No dates available</td></tr>';
            return;
        }
        
        let tableHTML = '';
        
        querySnapshot.forEach((doc) => {
            const dateData = doc.data();
            const dateId = doc.id;
            
            // Format date
            const date = dateData.date ? new Date(dateData.date.seconds * 1000) : new Date();
            const formattedDate = date.toLocaleDateString();
            
            // Get day of week
            const dayOfWeek = date.toLocaleDateString('en-US', { weekday: 'long' });
            
            tableHTML += `
                <tr>
                    <td>${formattedDate}</td>
                    <td>${dayOfWeek}</td>
                    <td>$${(dateData.price || 0).toFixed(2)}</td>
                    <td>${dateData.isHoliday ? '<span class="badge badge-primary">Yes</span>' : 'No'}</td>
                    <td>
                        <div class="d-flex gap-2">
                            <button class="btn btn-secondary btn-sm edit-date" data-id="${dateId}">
                                <i class="fas fa-edit"></i>
                            </button>
                            <button class="btn btn-danger btn-sm delete-date" data-id="${dateId}">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        });
        
        datesTable.innerHTML = tableHTML;
        
        // Add event listeners to date action buttons
        document.querySelectorAll('.edit-date').forEach(button => {
            button.addEventListener('click', async (e) => {
                const dateId = e.currentTarget.getAttribute('data-id');
                await editDate(tourId, dateId);
            });
        });
        
        document.querySelectorAll('.delete-date').forEach(button => {
            button.addEventListener('click', async (e) => {
                const dateId = e.currentTarget.getAttribute('data-id');
                if (confirm('Are you sure you want to delete this date?')) {
                    await deleteDate(tourId, dateId);
                }
            });
        });
        
    } catch (error) {
        console.error("Error loading available dates:", error);
        document.getElementById('datesTable').innerHTML = 
            '<tr><td colspan="5" class="text-center">Error loading dates</td></tr>';
    }
}

// Load date data for editing
async function editDate(tourId, dateId) {
    try {
        const dateDoc = await getDoc(doc(db, "tours", tourId, "availableDates", dateId));
        
        if (dateDoc.exists()) {
            const dateData = dateDoc.data();
            
            document.getElementById('dateId').value = dateId;
            
            // Format date for input field (YYYY-MM-DD)
            const date = dateData.date ? new Date(dateData.date.seconds * 1000) : new Date();
            const formattedDate = date.toISOString().split('T')[0];
            
            document.getElementById('dateValue').value = formattedDate;
            document.getElementById('price').value = dateData.price || 0;
            document.getElementById('isHoliday').checked = dateData.isHoliday || false;
            
            // Scroll to the top of the form
            const dateForm = document.getElementById('dateForm');
            if (dateForm) {
                dateForm.scrollIntoView({ behavior: 'smooth', block: 'start' });
            }
        } else {
            throw new Error("Date not found");
        }
    } catch (error) {
        console.error("Error loading date for edit:", error);
        alert('Failed to load date data. Please try again.');
    }
}

// Add or update an available date
async function addOrUpdateDate() {
    try {
        const tourId = document.getElementById('dateTourId').value;
        const dateId = document.getElementById('dateId').value;
        const dateValue = document.getElementById('dateValue').value;
        const price = parseFloat(document.getElementById('price').value);
        const isHoliday = document.getElementById('isHoliday').checked;
        
        // Validate required fields
        if (!dateValue || isNaN(price)) {
            alert('Please fill in all required fields with valid values.');
            return;
        }
        
        // Parse date string to Date object
        const date = new Date(dateValue);
        
        // Get day of week (0 = Sunday, 1 = Monday, etc.)
        const dayOfWeek = date.getDay();
        
        const dateData = {
            date: Timestamp.fromDate(date),
            dayOfWeek,
            price,
            isHoliday
        };
        
        if (dateId) {
            // Update existing date
            await updateDoc(doc(db, "tours", tourId, "availableDates", dateId), dateData);
        } else {
            // Add new date
            await addDoc(collection(db, "tours", tourId, "availableDates"), dateData);
        }
        
        // Reset form and reload dates
        document.getElementById('dateForm').reset();
        document.getElementById('dateId').value = '';
        await loadAvailableDates(tourId);
        
    } catch (error) {
        console.error("Error saving date:", error);
        alert('Failed to save date. Please try again.');
    }
}

// Delete an available date
async function deleteDate(tourId, dateId) {
    try {
        await deleteDoc(doc(db, "tours", tourId, "availableDates", dateId));
        await loadAvailableDates(tourId);
    } catch (error) {
        console.error("Error deleting date:", error);
        alert('Failed to delete date. Please try again.');
    }
}
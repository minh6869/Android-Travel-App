import { auth, db } from './firebase-config.js';
import { onAuthStateChanged, signOut } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-auth.js";
import { collection, getDocs, query, orderBy, limit, where, getCountFromServer } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-firestore.js";
import { getCurrentAdmin } from './auth.js';

// Initialize dashboard page
document.addEventListener('DOMContentLoaded', async () => {
    // Check authentication
    onAuthStateChanged(auth, async (user) => {
        if (user) {
            // User is signed in
            const admin = await getCurrentAdmin();
            if (admin) {
                document.getElementById('adminName').textContent = admin.fullName;
            }
            
            // Load dashboard data
            loadDashboardStats();
            loadRecentBookings();
            loadRevenueChart();
            loadRecentReviews();
        } else {
            // User is signed out, redirect to login
            window.location.href = 'index.html';
        }
    });
    
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
});

// Load dashboard statistics
async function loadDashboardStats() {
    try {
        // Get total customers (users)
        const usersSnapshot = await getCountFromServer(collection(db, "users"));
        const totalCustomers = usersSnapshot.data().count;
        document.getElementById('totalCustomers').textContent = totalCustomers;
        
        // Get total bookings
        const bookingsSnapshot = await getCountFromServer(collection(db, "bookings"));
        const totalBookings = bookingsSnapshot.data().count;
        document.getElementById('toursSold').textContent = totalBookings;
        
        // Calculate total revenue from COMPLETED bookings only
        const completedBookingsQuery = query(
            collection(db, "bookings"),
            where("status", "==", "completed") // Only count completed bookings
        );
        
        const completedBookingsSnapshot = await getDocs(completedBookingsQuery);
        let totalRevenue = 0;
        
        completedBookingsSnapshot.forEach((doc) => {
            const booking = doc.data();
            totalRevenue += booking.totalPrice || 0;
        });
        
        // If no completed bookings found, try to check for paymentStatus="paid" as fallback
        if (completedBookingsSnapshot.empty) {
            console.log("No bookings with status 'completed' found. Checking for paymentStatus='paid' instead.");
            
            const paidBookingsQuery = query(
                collection(db, "bookings"),
                where("paymentStatus", "==", "paid")
            );
            
            const paidBookingsSnapshot = await getDocs(paidBookingsQuery);
            
            paidBookingsSnapshot.forEach((doc) => {
                const booking = doc.data();
                totalRevenue += booking.totalPrice || 0;
            });
        }
        
        // Format VND currency
        document.getElementById('totalRevenue').textContent = `₫${totalRevenue.toLocaleString('vi-VN')}`;
        
        // Calculate average rating
        const reviewsQuery = await getDocs(collection(db, "reviews"));
        let totalRating = 0;
        let reviewCount = 0;
        reviewsQuery.forEach((doc) => {
            const review = doc.data();
            totalRating += review.rating || 0;
            reviewCount++;
        });
        const avgRating = reviewCount > 0 ? (totalRating / reviewCount).toFixed(1) : '0.0';
        document.getElementById('averageRating').textContent = avgRating;
        
        // If no data is found, load sample data
        if (totalCustomers === 0 && totalBookings === 0 && reviewCount === 0) {
            loadSampleDashboardStats();
        }
        
    } catch (error) {
        console.error("Error loading dashboard stats:", error);
        loadSampleDashboardStats();
    }
}

// Load sample dashboard statistics when real data is not available
function loadSampleDashboardStats() {
    document.getElementById('totalCustomers').textContent = '156';
    document.getElementById('toursSold').textContent = '87';
    document.getElementById('totalRevenue').textContent = '₫12,450,000';
    document.getElementById('averageRating').textContent = '4.7';
}

// Load recent bookings
async function loadRecentBookings() {
    try {
        const bookingsQuery = query(
            collection(db, "bookings"),
            orderBy("tourDateStart", "desc"),
            limit(5)
        );
        
        const bookingsSnapshot = await getDocs(bookingsQuery);
        const tableBody = document.getElementById('recentBookingsTable');
        
        if (bookingsSnapshot.empty) {
            loadSampleRecentBookings();
            return;
        }
        
        let tableHTML = '';
        
        for (const doc of bookingsSnapshot.docs) {
            const booking = doc.data();
            const bookingId = doc.id;
            
            // Get user data
            let userName = booking.participantName || 'Unknown';
            
            // Get tour data
            let tourName = 'Unknown Tour';
            try {
                const tourSnapshot = await getDocs(query(
                    collection(db, "tours"),
                    where("__name__", "==", booking.tourId)
                ));
                
                if (!tourSnapshot.empty) {
                    tourName = tourSnapshot.docs[0].data().title;
                }
            } catch (error) {
                console.error("Error fetching tour:", error);
            }
            
            // Format date
            const date = booking.tourDateStart ? new Date(booking.tourDateStart.seconds * 1000) : new Date();
            const formattedDate = date.toLocaleDateString();
            
            // Format status badge
            let statusBadge = '';
            const status = booking.status || booking.paymentStatus; // Check both status fields
            
            switch (status) {
                case 'completed':
                    statusBadge = '<span class="badge badge-success">Completed</span>';
                    break;
                case 'paid':
                    statusBadge = '<span class="badge badge-success">Paid</span>';
                    break;
                case 'pending':
                    statusBadge = '<span class="badge badge-primary">Pending</span>';
                    break;
                case 'failed':
                    statusBadge = '<span class="badge badge-danger">Failed</span>';
                    break;
                default:
                    statusBadge = '<span class="badge badge-primary">Pending</span>';
            }
            
            // Format VND currency
            const totalPrice = booking.totalPrice || 0;
            
            tableHTML += `
                <tr>
                    <td>${bookingId.substring(0, 8)}</td>
                    <td>${userName}</td>
                    <td>${tourName}</td>
                    <td>${formattedDate}</td>
                    <td>${booking.numberOfPerson || 1}</td>
                    <td>₫${totalPrice.toLocaleString('vi-VN')}</td>
                    <td>${statusBadge}</td>
                </tr>
            `;
        }
        
        tableBody.innerHTML = tableHTML;
        
    } catch (error) {
        console.error("Error loading recent bookings:", error);
        loadSampleRecentBookings();
    }
}

// Load sample recent bookings
function loadSampleRecentBookings() {
    const tableBody = document.getElementById('recentBookingsTable');
    
    tableBody.innerHTML = `
        <tr>
            <td>BK12345A</td>
            <td>Nguyen Van A</td>
            <td>Ha Long Bay Cruise</td>
            <td>14/04/2025</td>
            <td>2</td>
            <td>₫4,600,000</td>
            <td><span class="badge badge-success">Completed</span></td>
        </tr>
        <tr>
            <td>BK12346B</td>
            <td>Tran Thi B</td>
            <td>Sapa Trekking Adventure</td>
            <td>12/04/2025</td>
            <td>4</td>
            <td>₫8,280,000</td>
            <td><span class="badge badge-success">Completed</span></td>
        </tr>
        <tr>
            <td>BK12347C</td>
            <td>Le Van C</td>
            <td>Hoi An Cultural Tour</td>
            <td>10/04/2025</td>
            <td>3</td>
            <td>₫5,520,000</td>
            <td><span class="badge badge-primary">Pending</span></td>
        </tr>
        <tr>
            <td>BK12348D</td>
            <td>Pham Thi D</td>
            <td>Mekong Delta Experience</td>
            <td>08/04/2025</td>
            <td>2</td>
            <td>₫3,450,000</td>
            <td><span class="badge badge-success">Completed</span></td>
        </tr>
        <tr>
            <td>BK12349E</td>
            <td>Hoang Van E</td>
            <td>Phu Quoc Island Getaway</td>
            <td>05/04/2025</td>
            <td>2</td>
            <td>₫9,200,000</td>
            <td><span class="badge badge-danger">Failed</span></td>
        </tr>
    `;
}

// Load revenue chart
async function loadRevenueChart() {
    try {
        // Get completed bookings only for revenue calculation
        const bookingsQuery = query(
            collection(db, "bookings"),
            where("status", "==", "completed") // Only count completed bookings
        );
        
        let bookingsSnapshot = await getDocs(bookingsQuery);
        
        // If no completed bookings found, try to check for paymentStatus="paid" as fallback
        if (bookingsSnapshot.empty) {
            console.log("No bookings with status 'completed' found for chart. Checking for paymentStatus='paid' instead.");
            
            const paidBookingsQuery = query(
                collection(db, "bookings"),
                where("paymentStatus", "==", "paid")
            );
            
            bookingsSnapshot = await getDocs(paidBookingsQuery);
        }
        
        // Group revenue by month
        const revenueByMonth = {};
        
        bookingsSnapshot.forEach((doc) => {
            const booking = doc.data();
            if (booking.tourDateStart && booking.totalPrice) {
                const date = new Date(booking.tourDateStart.seconds * 1000);
                const month = date.toLocaleString('default', { month: 'short' });
                
                if (!revenueByMonth[month]) {
                    revenueByMonth[month] = 0;
                }
                
                // Add revenue (already in VND)
                revenueByMonth[month] += booking.totalPrice;
            }
        });
        
        // Prepare chart data
        const months = Object.keys(revenueByMonth);
        const revenues = Object.values(revenueByMonth);
        
        // If no data is found, use sample data
        if (months.length === 0) {
            loadSampleRevenueChart();
            return;
        }
        
        // Create chart
        const ctx = document.getElementById('revenueChart').getContext('2d');
        new Chart(ctx, {
            type: 'bar',
            data: {
                labels: months,
                datasets: [{
                    label: 'Monthly Revenue (₫)',
                    data: revenues,
                    backgroundColor: 'rgba(37, 99, 235, 0.2)',
                    borderColor: '#2563EB',
                    borderWidth: 1
                }]
            },
            options: {
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            callback: function(value) {
                                return '₫' + value.toLocaleString('vi-VN');
                            }
                        }
                    }
                },
                plugins: {
                    legend: {
                        display: false
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                return '₫' + context.raw.toLocaleString('vi-VN');
                            }
                        }
                    }
                }
            }
        });
    } catch (error) {
        console.error("Error loading revenue chart:", error);
        loadSampleRevenueChart();
    }
}

// Load sample revenue chart
function loadSampleRevenueChart() {
    const ctx = document.getElementById('revenueChart').getContext('2d');
    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
            datasets: [{
                label: 'Monthly Revenue (₫)',
                data: [69000000, 92000000, 115000000, 138000000, 126500000, 110400000],
                backgroundColor: 'rgba(37, 99, 235, 0.2)',
                borderColor: '#2563EB',
                borderWidth: 1
            }]
        },
        options: {
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) {
                            return '₫' + value.toLocaleString('vi-VN');
                        }
                    }
                }
            },
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return '₫' + context.raw.toLocaleString('vi-VN');
                        }
                    }
                }
            }
        }
    });
}

// Load recent reviews
async function loadRecentReviews() {
    try {
        const reviewsQuery = query(
            collection(db, "reviews"),
            orderBy("createdAt", "desc"),
            limit(3)
        );
        
        const reviewsSnapshot = await getDocs(reviewsQuery);
        const reviewsContainer = document.getElementById('recentReviews');
        
        if (reviewsSnapshot.empty) {
            loadSampleRecentReviews();
            return;
        }
        
        let reviewsHTML = '';
        
        for (const doc of reviewsSnapshot.docs) {
            const review = doc.data();
            
            // Get user data
            let userName = 'Anonymous User';
            let userImage = 'https://via.placeholder.com/40';
            
            try {
                if (review.userId) {
                    const userSnapshot = await getDocs(query(
                        collection(db, "users"),
                        where("__name__", "==", review.userId)
                    ));
                    
                    if (!userSnapshot.empty) {
                        const userData = userSnapshot.docs[0].data();
                        userName = userData.fullName || userData.userName || 'Anonymous';
                        userImage = userData.userImageUrl || userImage;
                    }
                }
            } catch (error) {
                console.error("Error fetching user:", error);
            }
            
            // Get tour data
            let tourName = 'Unknown Tour';
            
            try {
                if (review.tourId) {
                    const tourSnapshot = await getDocs(query(
                        collection(db, "tours"),
                        where("__name__", "==", review.tourId)
                    ));
                    
                    if (!tourSnapshot.empty) {
                        tourName = tourSnapshot.docs[0].data().title;
                    }
                }
            } catch (error) {
                console.error("Error fetching tour:", error);
            }
            
            // Generate stars
            let stars = '';
            for (let i = 1; i <= 5; i++) {
                if (i <= review.rating) {
                    stars += '<i class="fas fa-star text-primary"></i>';
                } else {
                    stars += '<i class="far fa-star text-primary"></i>';
                }
            }
            
            // Format date
            const date = review.createdAt ? new Date(review.createdAt.seconds * 1000) : new Date();
            const formattedDate = date.toLocaleDateString();
            
            reviewsHTML += `
                <div class="review-item" style="padding: 16px 0; border-bottom: 1px solid var(--light-gray);">
                    <div class="d-flex gap-2">
                        <img src="${userImage}" alt="${userName}" style="width: 40px; height: 40px; border-radius: 50%; object-fit: cover;">
                        <div>
                            <h4>${userName}</h4>
                            <p style="font-size: 0.8rem; color: var(--dark-gray);">${formattedDate} • ${tourName}</p>
                            <div class="stars" style="margin: 8px 0;">
                                ${stars}
                            </div>
                            <p>${review.comment || 'No comment provided.'}</p>
                        </div>
                    </div>
                </div>
            `;
        }
        
        reviewsContainer.innerHTML = reviewsHTML;
        
    } catch (error) {
        console.error("Error loading recent reviews:", error);
        loadSampleRecentReviews();
    }
}

// Load sample recent reviews
function loadSampleRecentReviews() {
    const reviewsContainer = document.getElementById('recentReviews');
    
    reviewsContainer.innerHTML = `
        <div class="review-item" style="padding: 16px 0; border-bottom: 1px solid var(--light-gray);">
            <div class="d-flex gap-2">
                <img src="https://via.placeholder.com/40" alt="Nguyen Van A" style="width: 40px; height: 40px; border-radius: 50%; object-fit: cover;">
                <div>
                    <h4>Nguyen Van A</h4>
                    <p style="font-size: 0.8rem; color: var(--dark-gray);">14/04/2025 • Ha Long Bay Cruise</p>
                    <div class="stars" style="margin: 8px 0;">
                        <i class="fas fa-star text-primary"></i>
                        <i class="fas fa-star text-primary"></i>
                        <i class="fas fa-star text-primary"></i>
                        <i class="fas fa-star text-primary"></i>
                        <i class="fas fa-star text-primary"></i>
                    </div>
                    <p>Amazing experience! The cruise was luxurious and the scenery was breathtaking. The staff were very attentive and professional. Highly recommend this tour!</p>
                </div>
            </div>
        </div>
        
        <div class="review-item" style="padding: 16px 0; border-bottom: 1px solid var(--light-gray);">
            <div class="d-flex gap-2">
                <img src="https://via.placeholder.com/40" alt="Tran Thi B" style="width: 40px; height: 40px; border-radius: 50%; object-fit: cover;">
                <div>
                    <h4>Tran Thi B</h4>
                    <p style="font-size: 0.8rem; color: var(--dark-gray);">12/04/2025 • Sapa Trekking Adventure</p>
                    <div class="stars" style="margin: 8px 0;">
                        <i class="fas fa-star text-primary"></i>
                        <i class="fas fa-star text-primary"></i>
                        <i class="fas fa-star text-primary"></i>
                        <i class="fas fa-star text-primary"></i>
                        <i class="far fa-star text-primary"></i>
                    </div>
                    <p>The trek was challenging but so rewarding! Our guide was knowledgeable about the local culture and very helpful. The homestay experience was authentic and memorable.</p>
                </div>
            </div>
        </div>
        
        <div class="review-item" style="padding: 16px 0; border-bottom: 1px solid var(--light-gray);">
            <div class="d-flex gap-2">
                <img src="https://via.placeholder.com/40" alt="Le Van C" style="width: 40px; height: 40px; border-radius: 50%; object-fit: cover;">
                <div>
                    <h4>Le Van C</h4>
                    <p style="font-size: 0.8rem; color: var(--dark-gray);">10/04/2025 • Hoi An Cultural Tour</p>
                    <div class="stars" style="margin: 8px 0;">
                        <i class="fas fa-star text-primary"></i>
                        <i class="fas fa-star text-primary"></i>
                        <i class="fas fa-star text-primary"></i>
                        <i class="fas fa-star text-primary"></i>
                        <i class="fas fa-star-half-alt text-primary"></i>
                    </div>
                    <p>Hoi An is such a beautiful city! The lanterns at night are magical. Our tour guide showed us all the hidden gems and best local food spots. The tailor shops were also amazing.</p>
                </div>
            </div>
        </div>
    `;
}
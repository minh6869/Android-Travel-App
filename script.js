// Main JavaScript for Tour Management System
import { auth } from './firebase-config.js';
import { signOut } from "https://www.gstatic.com/firebasejs/10.8.0/firebase-auth.js";

document.addEventListener('DOMContentLoaded', function() {
    // Mobile sidebar toggle
    const toggleSidebarBtn = document.querySelector('.toggle-sidebar');
    const sidebar = document.querySelector('.sidebar');
    const content = document.querySelector('.content');
    
    if (toggleSidebarBtn) {
        toggleSidebarBtn.addEventListener('click', function() {
            sidebar.classList.toggle('collapsed');
            content.classList.toggle('expanded');
        });
    }
    
    // Thêm nút đăng xuất vào sidebar nếu chưa có
    const mainNav = document.querySelector('.main-nav ul');
    if (mainNav && !document.querySelector('.logout-btn')) {
        const logoutItem = document.createElement('li');
        logoutItem.innerHTML = '<a href="#" class="logout-btn"><i class="fas fa-sign-out-alt"></i> Đăng xuất</a>';
        mainNav.appendChild(logoutItem);
        
        // Xử lý sự kiện đăng xuất
        document.querySelector('.logout-btn').addEventListener('click', async function(e) {
            e.preventDefault();
            
            if (confirm('Bạn có chắc chắn muốn đăng xuất?')) {
                try {
                    await signOut(auth);
                    // Chuyển hướng đến trang đăng nhập
                    window.location.href = 'login.html';
                } catch (error) {
                    console.error("Lỗi khi đăng xuất:", error);
                    alert('Đã xảy ra lỗi khi đăng xuất!');
                }
            }
        });
    }
    
    // Tour management - Delete confirmation
    const deleteButtons = document.querySelectorAll('.btn-delete');
    
    deleteButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            if (!confirm('Bạn có chắc chắn muốn xóa mục này không?')) {
                e.preventDefault();
            }
        });
    });
    
    // File upload button functionality
    const fileInputs = document.querySelectorAll('input[type="file"]');
    const uploadButtons = document.querySelectorAll('.btn-upload');
    
    if (fileInputs.length > 0 && uploadButtons.length > 0) {
        uploadButtons.forEach((button, index) => {
            button.addEventListener('click', function() {
                fileInputs[index].click();
            });
        });
        
        fileInputs.forEach(input => {
            input.addEventListener('change', function() {
                const fileName = this.value.split('\\').pop();
                if (fileName) {
                    const nextElement = this.nextElementSibling.nextElementSibling;
                    if (nextElement && nextElement.tagName !== 'BUTTON') {
                        nextElement.textContent = fileName;
                    }
                    
                    // Enable restore button if it exists and this is a backup file
                    if (this.id === 'restore-file') {
                        const restoreBtn = document.querySelector('.backup-actions .btn-primary[disabled]');
                        if (restoreBtn) {
                            restoreBtn.disabled = false;
                        }
                    }
                }
            });
        });
    }
    
    // Settings tab navigation
    const settingsLinks = document.querySelectorAll('.settings-nav a');
    const settingsSections = document.querySelectorAll('.settings-section');
    
    if (settingsLinks.length > 0 && settingsSections.length > 0) {
        settingsLinks.forEach(link => {
            link.addEventListener('click', function(e) {
                e.preventDefault();
                
                // Remove active class from all links and sections
                settingsLinks.forEach(link => {
                    link.parentElement.classList.remove('active');
                });
                settingsSections.forEach(section => {
                    section.classList.remove('active');
                });
                
                // Add active class to clicked link
                this.parentElement.classList.add('active');
                
                // Show corresponding section
                const targetId = this.getAttribute('href').substring(1);
                document.getElementById(targetId).classList.add('active');
            });
        });
    }
});
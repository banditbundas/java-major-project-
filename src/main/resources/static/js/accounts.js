// Accounts JavaScript - Complete Rewrite
(function() {
    'use strict';
    
    const API_URL = 'http://localhost:8080/api/accounts';
    
    // Get token helper
    function getAuthToken() {
        return localStorage.getItem('token');
    }
    
    // Load accounts - SIMPLE DIRECT APPROACH
    window.loadAccounts = async function() {
        const container = document.getElementById('accountsList');
        if (!container) {
            console.error('Container not found');
            return;
        }
        
        container.innerHTML = '<div class="empty-state"><p>Loading accounts...</p></div>';
        
        const token = getAuthToken();
        if (!token) {
            container.innerHTML = '<div class="empty-state"><p>Please <a href="/login">login</a> to view accounts.</p></div>';
            return;
        }
        
        try {
            const response = await fetch(API_URL, {
                method: 'GET',
                headers: {
                    'Authorization': 'Bearer ' + token,
                    'Content-Type': 'application/json'
                }
            });
            
            if (!response.ok) {
                if (response.status === 401) {
                    localStorage.removeItem('token');
                    window.location.href = '/login';
                    return;
                }
                throw new Error('HTTP ' + response.status);
            }
            
            const accounts = await response.json();
            
            if (!Array.isArray(accounts)) {
                console.error('Invalid response format:', accounts);
                container.innerHTML = '<div class="empty-state"><p>Error: Invalid response from server</p></div>';
                return;
            }
            
            displayAccounts(accounts);
            
        } catch (error) {
            console.error('Error loading accounts:', error);
            container.innerHTML = `
                <div class="empty-state">
                    <p style="color: red;">Error: ${error.message}</p>
                    <button class="btn btn-primary" onclick="loadAccounts()" style="margin-top: 10px;">Retry</button>
                </div>
            `;
        }
    };
    
    // Display accounts - SIMPLE DIRECT APPROACH
    window.displayAccounts = function(accounts) {
        const container = document.getElementById('accountsList');
        if (!container) return;
        
        if (!accounts || accounts.length === 0) {
            container.innerHTML = `
                <div class="empty-state" style="grid-column: 1 / -1;">
                    <h3>💳 No Accounts</h3>
                    <p>Create your first account to get started!</p>
                    <button class="btn btn-primary" onclick="showCreateAccountModal()" style="margin-top: 15px;">Create Account</button>
                </div>
            `;
            return;
        }
        
        const html = accounts.map(acc => {
            const icon = acc.accountType === 'SAVINGS' ? '💰' : 
                        acc.accountType === 'CURRENT' ? '💼' : 
                        acc.accountType === 'FIXED_DEPOSIT' ? '📈' : '🔄';
            const type = (acc.accountType || '').replace('_', ' ');
            const balance = acc.balance ? parseFloat(acc.balance).toFixed(2) : '0.00';
            const accountNum = acc.accountNumber || 'N/A';
            const accountName = acc.accountName || `${type} Account`;
            const ifsc = acc.ifscCode || 'N/A';
            
            return `
                <div class="account-card">
                    <h3>${icon} ${accountName}</h3>
                    <p style="color: hsl(215.4 16.3% 46.9%); font-size: 0.875rem; margin-bottom: 8px;">${type}</p>
                    <p><strong>Account Number:</strong> <span style="font-family: monospace; font-weight: 600;">${accountNum}</span></p>
                    <p><strong>IFSC Code:</strong> ${ifsc}</p>
                    <div class="balance">₹${balance}</div>
                    <div style="display: flex; gap: 8px; margin-top: 15px; flex-wrap: wrap;">
                        <button class="btn btn-primary" onclick="if(typeof showDepositModal === 'function') { showDepositModal('${accountNum}'); } else { alert('Deposit function not loaded. Please refresh the page.'); }" style="flex: 1; min-width: 100px;">💰 Deposit</button>
                        <button class="btn btn-secondary" onclick="viewAccountDetails('${accountNum}')" style="flex: 1; min-width: 100px;">View Details</button>
                    </div>
                </div>
            `;
        }).join('');
        
        container.innerHTML = html;
    };
    
    // Show modal
    window.showCreateAccountModal = function() {
        const modal = document.getElementById('createAccountModal');
        if (modal) {
            modal.style.display = 'block';
            document.getElementById('createAccountForm').reset();
        }
    };
    
    // Close modal
    window.closeCreateAccountModal = function() {
        const modal = document.getElementById('createAccountModal');
        if (modal) {
            modal.style.display = 'none';
            document.getElementById('createAccountForm').reset();
        }
    };
    
    // View details
    window.viewAccountDetails = function(accountNumber) {
        if (accountNumber && accountNumber !== 'N/A') {
            window.location.href = '/transactions?account=' + encodeURIComponent(accountNumber);
        }
    };
    
    // Handle form submission - SIMPLE DIRECT APPROACH
    function setupForm() {
        const form = document.getElementById('createAccountForm');
        if (!form) return;
        
        form.addEventListener('submit', async function(e) {
            e.preventDefault();
            
            const token = getAuthToken();
            if (!token) {
                alert('Please login first');
                window.location.href = '/login';
                return;
            }
            
            const select = document.getElementById('accountType');
            const accountType = select ? select.value : '';
            
            if (!accountType) {
                alert('Please select an account type');
                return;
            }
            
            const submitBtn = form.querySelector('button[type="submit"]');
            const originalText = submitBtn ? submitBtn.textContent : 'Create';
            
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.textContent = 'Creating...';
            }
            
            try {
                const accountNameInput = document.getElementById('accountName');
                const accountName = accountNameInput ? accountNameInput.value.trim() : '';
                
                let url = API_URL + '?accountType=' + encodeURIComponent(accountType);
                if (accountName) {
                    url += '&accountName=' + encodeURIComponent(accountName);
                }
                
                const response = await fetch(url, {
                    method: 'POST',
                    headers: {
                        'Authorization': 'Bearer ' + token,
                        'Content-Type': 'application/json'
                    }
                });
                
                const data = await response.json();
                
                if (response.ok) {
                    alert('Account created! Number: ' + (data.accountNumber || 'N/A'));
                    closeCreateAccountModal();
                    loadAccounts();
                } else {
                    alert('Error: ' + (data.error || 'Failed to create account'));
                }
            } catch (error) {
                console.error('Error:', error);
                alert('Error: ' + error.message);
            } finally {
                if (submitBtn) {
                    submitBtn.disabled = false;
                    submitBtn.textContent = originalText;
                }
            }
        });
    }
    
    // Deposit Modal Functions - Make sure they're globally available
    window.showDepositModal = function(accountNumber) {
        console.log('showDepositModal called with:', accountNumber);
        
        // Helper function to try opening the modal
        function tryOpenModal(retries) {
            retries = retries || 0;
            
            try {
                const modal = document.getElementById('depositModal');
                const accountInput = document.getElementById('depositAccountNumber');
                
                if (!modal) {
                    if (retries < 5) {
                        // Retry after a short delay
                        console.log('Modal not found, retrying... (' + (retries + 1) + '/5)');
                        setTimeout(() => tryOpenModal(retries + 1), 100);
                        return;
                    }
                    console.error('Deposit modal not found after retries');
                    alert('Error: Deposit modal not found. Please refresh the page.');
                    return;
                }
                
                if (!accountInput) {
                    console.error('Deposit account input not found');
                    alert('Error: Deposit form not found. Please refresh the page.');
                    return;
                }
                
                // Set account number and show modal
                accountInput.value = accountNumber;
                modal.style.display = 'block';
                
                // Reset form but keep account number
                const form = document.getElementById('depositForm');
                if (form) {
                    form.reset();
                    accountInput.value = accountNumber; // Set again after reset
                }
                
                // Focus on amount input
                const amountInput = document.getElementById('depositAmount');
                if (amountInput) {
                    setTimeout(() => amountInput.focus(), 100);
                }
            } catch (error) {
                console.error('Error in showDepositModal:', error);
                alert('Error opening deposit modal: ' + error.message);
            }
        }
        
        // Try to open immediately, with retries if needed
        tryOpenModal(0);
    };
    
    window.closeDepositModal = function() {
        try {
            const modal = document.getElementById('depositModal');
            if (modal) {
                modal.style.display = 'none';
                const form = document.getElementById('depositForm');
                if (form) {
                    form.reset();
                }
            }
        } catch (error) {
            console.error('Error in closeDepositModal:', error);
        }
    };
    
    // Handle deposit form submission
    function setupDepositForm() {
        const form = document.getElementById('depositForm');
        if (!form) return;
        
        form.addEventListener('submit', async function(e) {
            e.preventDefault();
            
            const token = getAuthToken();
            if (!token) {
                alert('Please login first');
                window.location.href = '/login';
                return;
            }
            
            const accountNumber = document.getElementById('depositAccountNumber').value;
            const amount = document.getElementById('depositAmount').value;
            const description = document.getElementById('depositDescription').value;
            
            if (!accountNumber || !amount || parseFloat(amount) <= 0) {
                alert('Please enter a valid amount');
                return;
            }
            
            const submitBtn = form.querySelector('button[type="submit"]');
            const originalText = submitBtn ? submitBtn.textContent : 'Deposit';
            
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.textContent = 'Processing...';
            }
            
            try {
                const response = await fetch('http://localhost:8080/api/transactions/deposit', {
                    method: 'POST',
                    headers: {
                        'Authorization': 'Bearer ' + token,
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        accountNumber: accountNumber,
                        amount: parseFloat(amount),
                        description: description || null
                    })
                });
                
                const data = await response.json();
                
                if (response.ok) {
                    alert('Deposit successful! Transaction ID: ' + (data.transactionId || data.id || 'N/A'));
                    closeDepositModal();
                    loadAccounts(); // Reload to show updated balance
                } else {
                    alert('Error: ' + (data.error || 'Failed to deposit'));
                }
            } catch (error) {
                console.error('Error:', error);
                alert('Error: ' + error.message);
            } finally {
                if (submitBtn) {
                    submitBtn.disabled = false;
                    submitBtn.textContent = originalText;
                }
            }
        });
    }
    
    // Close modal on outside click
    window.onclick = function(event) {
        const createModal = document.getElementById('createAccountModal');
        const depositModal = document.getElementById('depositModal');
        if (event.target === createModal) {
            closeCreateAccountModal();
        }
        if (event.target === depositModal) {
            closeDepositModal();
        }
    };
    
    // Initialize when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', function() {
            setupForm();
            setupDepositForm();
            setTimeout(loadAccounts, 300);
        });
    } else {
        setupForm();
        setupDepositForm();
        setTimeout(loadAccounts, 300);
    }
    
})();

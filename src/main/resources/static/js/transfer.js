// Transfer Funds JavaScript - Complete Rewrite
(function() {
    'use strict';
    
    const API_URL = 'http://localhost:8080/api';
    const ACCOUNTS_URL = API_URL + '/accounts';
    const TRANSFER_URL = API_URL + '/transactions/transfer';
    
    let allAccounts = []; // Store accounts globally
    
    // Get token helper
    function getToken() {
        return localStorage.getItem('token');
    }
    
    // Load accounts for both dropdowns
    async function loadAccounts() {
        const fromSelect = document.getElementById('fromAccount');
        const toSelect = document.getElementById('toAccount');
        
        if (!fromSelect || !toSelect) return;
        
        const token = getToken();
        if (!token) {
            fromSelect.innerHTML = '<option value="">Please login first</option>';
            toSelect.innerHTML = '<option value="">Please login first</option>';
            return;
        }
        
        try {
            const response = await fetch(ACCOUNTS_URL, {
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
                throw new Error('Failed to load accounts');
            }
            
            const accounts = await response.json();
            
            if (!Array.isArray(accounts)) {
                console.error('Invalid accounts response:', accounts);
                fromSelect.innerHTML = '<option value="">Error loading accounts</option>';
                toSelect.innerHTML = '<option value="">Error loading accounts</option>';
                return;
            }
            
            // Store accounts globally
            allAccounts = accounts;
            
            // Populate From Account dropdown
            populateAccountDropdown(fromSelect, accounts, 'Select From Account');
            
            // Populate To Account dropdown
            populateAccountDropdown(toSelect, accounts, 'Select To Account');
            
            // Update To Account dropdown when From Account changes
            fromSelect.addEventListener('change', function() {
                updateToAccountDropdown();
            });
            
        } catch (error) {
            console.error('Error loading accounts:', error);
            fromSelect.innerHTML = '<option value="">Error loading accounts</option>';
            toSelect.innerHTML = '<option value="">Error loading accounts</option>';
        }
    }
    
    // Populate account dropdown
    function populateAccountDropdown(select, accounts, defaultText) {
        select.innerHTML = `<option value="">${defaultText}</option>`;
        
        accounts.forEach(account => {
            const option = document.createElement('option');
            option.value = account.accountNumber || '';
            const type = (account.accountType || '').replace('_', ' ');
            const balance = account.balance ? parseFloat(account.balance).toFixed(2) : '0.00';
            option.textContent = `${type} - ${account.accountNumber} (₹${balance})`;
            select.appendChild(option);
        });
    }
    
    // Update To Account dropdown to exclude selected From Account
    function updateToAccountDropdown() {
        const fromSelect = document.getElementById('fromAccount');
        const toSelect = document.getElementById('toAccount');
        const selectedFromAccount = fromSelect.value;
        
        if (!selectedFromAccount) {
            // If no from account selected, show all accounts
            populateAccountDropdown(toSelect, allAccounts, 'Select To Account');
            return;
        }
        
        // Filter out the selected from account
        const filteredAccounts = allAccounts.filter(account => 
            account.accountNumber !== selectedFromAccount
        );
        
        populateAccountDropdown(toSelect, filteredAccounts, 'Select To Account');
    }
    
    // Handle form submission
    function setupForm() {
        const form = document.getElementById('transferForm');
        if (!form) return;
        
        form.addEventListener('submit', async function(e) {
            e.preventDefault();
            
            const errorDiv = document.getElementById('errorMessage');
            const successDiv = document.getElementById('successMessage');
            
            if (errorDiv) {
                errorDiv.style.display = 'none';
                errorDiv.textContent = '';
            }
            if (successDiv) {
                successDiv.style.display = 'none';
                successDiv.textContent = '';
            }
            
            const token = getToken();
            if (!token) {
                showError('Please login first');
                window.location.href = '/login';
                return;
            }
            
            const fromAccount = document.getElementById('fromAccount').value.trim();
            const toAccount = document.getElementById('toAccount').value.trim();
            const externalAccount = document.getElementById('externalAccount').value.trim();
            const amount = document.getElementById('amount').value.trim();
            const description = document.getElementById('description').value.trim();
            const ifscCode = document.getElementById('ifscCode').value.trim();
            
            // Determine which account to use (dropdown or external)
            const finalToAccount = toAccount || externalAccount;
            
            // Validation
            if (!fromAccount) {
                showError('Please select a from account');
                return;
            }
            
            if (!finalToAccount) {
                showError('Please select a to account from the dropdown or enter an external account number');
                return;
            }
            
            if (fromAccount === finalToAccount) {
                showError('Cannot transfer to the same account');
                return;
            }
            
            if (!amount || parseFloat(amount) <= 0) {
                showError('Please enter a valid amount');
                return;
            }
            
            // If external account is used, IFSC code is required
            if (externalAccount && !ifscCode) {
                showError('IFSC code is required for external transfers');
                return;
            }
            
            // If using dropdown account, clear external account field
            if (toAccount && externalAccount) {
                document.getElementById('externalAccount').value = '';
            }
            
            const submitBtn = form.querySelector('button[type="submit"]');
            const originalText = submitBtn ? submitBtn.textContent : 'Transfer';
            
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.textContent = 'Processing...';
            }
            
            try {
                const requestData = {
                    fromAccountNumber: fromAccount,
                    toAccountNumber: finalToAccount,
                    amount: parseFloat(amount),
                    description: description || null,
                    ifscCode: ifscCode || null
                };
                
                console.log('Transfer request:', requestData);
                
                const response = await fetch(TRANSFER_URL, {
                    method: 'POST',
                    headers: {
                        'Authorization': 'Bearer ' + token,
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(requestData)
                });
                
                console.log('Transfer response status:', response.status);
                
                let data;
                const contentType = response.headers.get('content-type');
                if (contentType && contentType.includes('application/json')) {
                    data = await response.json();
                } else {
                    const text = await response.text();
                    throw new Error(text || 'Server error');
                }
                
                if (response.ok) {
                    const transactionId = data.id || data.transactionId || 'N/A';
                    showSuccess(`Transfer successful! Transaction ID: ${transactionId}`);
                    form.reset();
                    // Reload accounts to update balances
                    setTimeout(loadAccounts, 500);
                } else {
                    const errorMsg = data.error || data.message || 'Transfer failed';
                    showError(errorMsg);
                }
            } catch (error) {
                console.error('Transfer error:', error);
                showError('Error: ' + (error.message || 'Failed to process transfer'));
            } finally {
                if (submitBtn) {
                    submitBtn.disabled = false;
                    submitBtn.textContent = originalText;
                }
            }
        });
    }
    
    // Show error message
    function showError(message) {
        const errorDiv = document.getElementById('errorMessage');
        if (errorDiv) {
            errorDiv.textContent = message;
            errorDiv.style.display = 'block';
        } else {
            alert(message);
        }
    }
    
    // Show success message
    function showSuccess(message) {
        const successDiv = document.getElementById('successMessage');
        if (successDiv) {
            successDiv.textContent = message;
            successDiv.style.display = 'block';
        } else {
            alert(message);
        }
    }
    
    // Initialize when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', function() {
            setupForm();
            setTimeout(loadAccounts, 300);
        });
    } else {
        setupForm();
        setTimeout(loadAccounts, 300);
    }
    
})();

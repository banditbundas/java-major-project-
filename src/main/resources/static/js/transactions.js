// Transactions JavaScript - Complete Rewrite
(function() {
    'use strict';
    
    const API_URL = 'http://localhost:8080/api';
    const ACCOUNTS_URL = API_URL + '/accounts';
    
    // Get token helper
    function getToken() {
        return localStorage.getItem('token');
    }
    
    // Load accounts for dropdown
    async function loadAccounts() {
        const select = document.getElementById('accountSelect');
        if (!select) return;
        
        const token = getToken();
        if (!token) {
            select.innerHTML = '<option value="">Please login first</option>';
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
                select.innerHTML = '<option value="">Error loading accounts</option>';
                return;
            }
            
            select.innerHTML = '<option value="">Select Account</option>';
            
            accounts.forEach(account => {
                const option = document.createElement('option');
                option.value = account.accountNumber || '';
                const type = (account.accountType || '').replace('_', ' ');
                option.textContent = `${type} - ${account.accountNumber}`;
                select.appendChild(option);
            });
            
        } catch (error) {
            console.error('Error loading accounts:', error);
            select.innerHTML = '<option value="">Error loading accounts</option>';
        }
    }
    
    // Load transactions for selected account
    async function loadTransactions() {
        const accountNumber = document.getElementById('accountSelect').value;
        const transactionsList = document.getElementById('transactionsList');
        
        if (!accountNumber) {
            if (transactionsList) {
                transactionsList.innerHTML = '<div class="empty-state"><p>Please select an account</p></div>';
            }
            return;
        }
        
        if (!transactionsList) return;
        
        transactionsList.innerHTML = '<div class="empty-state"><p>Loading transactions...</p></div>';
        
        const token = getToken();
        if (!token) {
            transactionsList.innerHTML = '<div class="empty-state"><p>Please login first</p></div>';
            return;
        }
        
        try {
            const response = await fetch(`${API_URL}/transactions/${accountNumber}`, {
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
                throw new Error('Failed to load transactions');
            }
            
            const transactions = await response.json();
            
            if (!Array.isArray(transactions)) {
                console.error('Invalid transactions response:', transactions);
                transactionsList.innerHTML = '<div class="empty-state"><p>Error loading transactions</p></div>';
                return;
            }
            
            displayTransactions(transactions, accountNumber);
            
        } catch (error) {
            console.error('Error loading transactions:', error);
            transactionsList.innerHTML = `<div class="empty-state"><p>Error: ${error.message}</p></div>`;
        }
    }
    
    // Display transactions
    function displayTransactions(transactions, currentAccountNumber) {
        const transactionsList = document.getElementById('transactionsList');
        if (!transactionsList) return;
        
        if (!transactions || transactions.length === 0) {
            transactionsList.innerHTML = '<div class="empty-state"><p>No transactions found</p></div>';
            return;
        }
        
        const html = `
            <table style="width: 100%; border-collapse: collapse; margin-top: 20px;">
                <thead>
                    <tr style="background-color: #667eea; color: white;">
                        <th style="padding: 12px; text-align: left;">Date</th>
                        <th style="padding: 12px; text-align: left;">Type</th>
                        <th style="padding: 12px; text-align: left;">From Account</th>
                        <th style="padding: 12px; text-align: left;">To Account</th>
                        <th style="padding: 12px; text-align: right;">Amount</th>
                        <th style="padding: 12px; text-align: left;">Status</th>
                        <th style="padding: 12px; text-align: left;">Description</th>
                    </tr>
                </thead>
                <tbody>
                    ${transactions.map(transaction => {
                        const fromAccountNum = transaction.fromAccountNumber || transaction.fromAccount?.accountNumber || '-';
                        const toAccountNum = transaction.toAccountNumber || transaction.toAccount?.accountNumber || transaction.externalAccountNumber || '-';
                        
                        // Determine if this transaction is debit (outgoing) or credit (incoming) for the current account
                        // If current account is the fromAccount -> it's a debit (money going out)
                        // If current account is the toAccount -> it's a credit (money coming in)
                        const isDebit = (fromAccountNum === currentAccountNumber);
                        const isCredit = (toAccountNum === currentAccountNumber);
                        
                        // For deposits, there's no fromAccount, so it's always credit
                        // For withdrawals, there's no toAccount, so it's always debit
                        let transactionDirection = '';
                        let amountColor = '#666';
                        
                        if (transaction.transactionType === 'DEPOSIT') {
                            transactionDirection = 'Received';
                            amountColor = '#3c3';
                        } else if (transaction.transactionType === 'WITHDRAWAL') {
                            transactionDirection = 'Withdrawn';
                            amountColor = '#c33';
                        } else if (isDebit) {
                            transactionDirection = 'Transferred';
                            amountColor = '#c33';
                        } else if (isCredit) {
                            transactionDirection = 'Received';
                            amountColor = '#3c3';
                        }
                        
                        const amount = transaction.amount ? parseFloat(transaction.amount) : 0;
                        const date = transaction.transactionDate ? new Date(transaction.transactionDate).toLocaleString() : 'N/A';
                        const status = transaction.status || 'UNKNOWN';
                        const statusColor = status === 'COMPLETED' ? '#3c3' : status === 'FAILED' ? '#c33' : '#fc3';
                        
                        return `
                            <tr style="border-bottom: 1px solid #e0e0e0;">
                                <td style="padding: 12px;">${date}</td>
                                <td style="padding: 12px;">${transaction.transactionType || 'N/A'}</td>
                                <td style="padding: 12px;">${fromAccountNum}</td>
                                <td style="padding: 12px;">${toAccountNum}</td>
                                <td style="padding: 12px; text-align: right; font-weight: bold; color: ${amountColor};">
                                    <span style="font-size: 0.9em; color: #666; margin-right: 5px;">${transactionDirection}</span>
                                    ${isDebit || transaction.transactionType === 'WITHDRAWAL' ? '-' : '+'}₹${amount.toFixed(2)}
                                </td>
                                <td style="padding: 12px;">
                                    <span style="color: ${statusColor}; font-weight: bold;">${status}</span>
                                </td>
                                <td style="padding: 12px;">${transaction.description || '-'}</td>
                            </tr>
                        `;
                    }).join('')}
                </tbody>
            </table>
        `;
        
        transactionsList.innerHTML = html;
    }
    
    // Filter transactions by date range
    async function filterTransactions() {
        const accountNumber = document.getElementById('accountSelect').value;
        const startDate = document.getElementById('startDate').value;
        const endDate = document.getElementById('endDate').value;
        
        if (!accountNumber) {
            alert('Please select an account');
            return;
        }
        
        if (!startDate || !endDate) {
            alert('Please select both start and end dates');
            return;
        }
        
        const token = getToken();
        if (!token) {
            alert('Please login first');
            return;
        }
        
        try {
            const startDateTime = new Date(startDate).toISOString();
            const endDateTime = new Date(endDate).toISOString();
            
            const response = await fetch(
                `${API_URL}/transactions/${accountNumber}/history?startDate=${startDateTime}&endDate=${endDateTime}`,
                {
                    method: 'GET',
                    headers: {
                        'Authorization': 'Bearer ' + token,
                        'Content-Type': 'application/json'
                    }
                }
            );
            
            if (!response.ok) {
                throw new Error('Failed to filter transactions');
            }
            
            const transactions = await response.json();
            
            if (!Array.isArray(transactions)) {
                console.error('Invalid transactions response:', transactions);
                alert('Error filtering transactions');
                return;
            }
            
            displayTransactions(transactions, accountNumber);
        } catch (error) {
            console.error('Error filtering transactions:', error);
            alert('Error: ' + error.message);
        }
    }
    
    // Reset filter
    function resetFilter() {
        document.getElementById('startDate').value = '';
        document.getElementById('endDate').value = '';
        loadTransactions();
    }
    
    // Make functions globally available
    window.loadTransactions = loadTransactions;
    window.filterTransactions = filterTransactions;
    window.resetFilter = resetFilter;
    
    // Initialize when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', function() {
            loadAccounts();
        });
    } else {
        loadAccounts();
    }
    
})();

// Dashboard JavaScript - Enhanced Version
(function() {
    'use strict';
    
    const API_URL = 'http://localhost:8080/api';
    const ACCOUNTS_URL = API_URL + '/accounts';
    
    // Get token helper
    function getToken() {
        return localStorage.getItem('token');
    }
    
    // Load user information
    async function loadUserInfo() {
        const token = getToken();
        if (!token) return;
        
        try {
            const response = await fetch(`${API_URL}/auth/me`, {
                method: 'GET',
                headers: {
                    'Authorization': 'Bearer ' + token,
                    'Content-Type': 'application/json'
                }
            });
            
            if (response.ok) {
                const user = await response.json();
                const userNameElement = document.getElementById('userName');
                const userEmailElement = document.getElementById('userEmail');
                
                if (userNameElement) {
                    userNameElement.textContent = user.firstName || user.username || 'User';
                }
                if (userEmailElement) {
                    userEmailElement.textContent = user.email || '';
                }
            }
        } catch (error) {
            console.error('Error loading user info:', error);
        }
    }
    
    // Load account summary
    async function loadAccountSummary() {
        const token = getToken();
        if (!token) return;
        
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
                return;
            }
            
            const accounts = await response.json();
            
            if (!Array.isArray(accounts)) {
                return;
            }
            
            // Calculate total balance
            let totalBalance = 0;
            accounts.forEach(account => {
                if (account.balance) {
                    totalBalance += parseFloat(account.balance);
                }
            });
            
            // Update summary cards
            const totalBalanceEl = document.getElementById('totalBalance');
            const totalAccountsEl = document.getElementById('totalAccounts');
            
            if (totalBalanceEl) {
                totalBalanceEl.textContent = '₹' + totalBalance.toFixed(2);
            }
            if (totalAccountsEl) {
                totalAccountsEl.textContent = accounts.length;
            }
            
            // Remove loading class
            document.querySelectorAll('.summary-card').forEach(card => {
                card.classList.remove('loading');
            });
            
            // Load accounts overview
            displayAccountsOverview(accounts);
            
            // Load recent transactions from all accounts
            loadRecentTransactionsFromAllAccounts(accounts);
            
        } catch (error) {
            console.error('Error loading account summary:', error);
        }
    }
    
    // Display accounts overview
    function displayAccountsOverview(accounts) {
        const container = document.getElementById('accountsOverview');
        if (!container) return;
        
            if (!accounts || accounts.length === 0) {
                container.innerHTML = `
                <div class="empty-state" style="grid-column: 1 / -1;">
                    <h3 style="margin-bottom: 12px;">💳 No Accounts Yet</h3>
                    <p style="margin-bottom: 20px;">Create your first account to get started!</p>
                    <a href="/accounts" class="btn btn-primary" style="width: auto; padding: 10px 20px; text-decoration: none;">➕ Create Account</a>
                </div>
            `;
                return;
            }
        
        // Show first 3 accounts
        const displayAccounts = accounts.slice(0, 3);
        
        container.innerHTML = displayAccounts.map(account => {
            const accountTypeIcon = {
                'SAVINGS': '💰',
                'CURRENT': '💼',
                'FIXED_DEPOSIT': '📈',
                'RECURRING_DEPOSIT': '🔄'
            }[account.accountType] || '💳';
            
            const balance = account.balance ? parseFloat(account.balance) : 0;
            const accountType = account.accountType ? account.accountType.replace('_', ' ') : 'Unknown';
            const accountName = account.accountName || `${accountType} Account`;
            
            return `
                <div class="account-card" onclick="window.location.href='/transactions?account=${account.accountNumber}'" style="cursor: pointer;">
                    <div style="display: flex; justify-content: space-between; align-items: start; margin-bottom: 12px;">
                        <h3>${accountTypeIcon} ${accountName}</h3>
                        <span style="font-size: 0.875rem; color: hsl(215.4 16.3% 46.9%);">${account.accountNumber}</span>
                    </div>
                    <p style="color: hsl(215.4 16.3% 46.9%); font-size: 0.875rem; margin-bottom: 8px;">${accountType}</p>
                    <div class="balance" style="font-size: 1.75rem; font-weight: 700; color: hsl(142 76% 36%); margin: 16px 0;">
                        ₹${balance.toFixed(2)}
                    </div>
                    <p style="color: hsl(215.4 16.3% 46.9%); font-size: 0.875rem; margin-top: 10px;">IFSC: ${account.ifscCode || 'N/A'}</p>
                </div>
            `;
        }).join('');
        
        if (accounts.length > 3) {
            container.innerHTML += `
                <div class="account-card" onclick="window.location.href='/accounts'" style="cursor: pointer; display: flex; align-items: center; justify-content: center; flex-direction: column; border: 2px dashed hsl(221.2 83.2% 53.3%);">
                    <div style="font-size: 3em; margin-bottom: 10px;">➕</div>
                    <p style="color: hsl(221.2 83.2% 53.3%); font-weight: 600;">View All Accounts</p>
                    <p style="color: hsl(215.4 16.3% 46.9%); font-size: 0.875rem; margin-top: 5px;">+${accounts.length - 3} more</p>
                </div>
            `;
        }
    }
    
    // Load recent transactions from all accounts
    async function loadRecentTransactionsFromAllAccounts(accounts) {
        const transactionsDiv = document.getElementById('recentTransactions');
        if (!transactionsDiv || !accounts || accounts.length === 0) {
            transactionsDiv.innerHTML = '<div class="empty-state"><p>No accounts found. Create an account to get started!</p></div>';
            return;
        }
        
        const token = getToken();
        if (!token) return;
        
        try {
            // Fetch transactions from all accounts
            const allTransactionsPromises = accounts.map(account => 
                fetch(`${API_URL}/transactions/${account.accountNumber}`, {
                    method: 'GET',
                    headers: {
                        'Authorization': 'Bearer ' + token,
                        'Content-Type': 'application/json'
                    }
                }).then(response => {
                    if (response.ok) {
                        return response.json().then(transactions => {
                            // Add account number to each transaction for reference
                            if (Array.isArray(transactions)) {
                                return transactions.map(tx => ({
                                    ...tx,
                                    _accountNumber: account.accountNumber
                                }));
                            }
                            return [];
                        });
                    }
                    return [];
                }).catch(() => [])
            );
            
            // Wait for all requests to complete
            const allTransactionsArrays = await Promise.all(allTransactionsPromises);
            
            // Flatten and combine all transactions
            let allTransactions = [];
            allTransactionsArrays.forEach(transactions => {
                allTransactions = allTransactions.concat(transactions);
            });
            
            if (allTransactions.length === 0) {
                transactionsDiv.innerHTML = '<div class="empty-state"><p>No recent transactions found. Make your first transaction!</p></div>';
                return;
            }
            
            // Sort by date (most recent first)
            allTransactions.sort((a, b) => {
                const dateA = a.transactionDate ? new Date(a.transactionDate) : new Date(0);
                const dateB = b.transactionDate ? new Date(b.transactionDate) : new Date(0);
                return dateB - dateA;
            });
            
            // Display top 10 most recent transactions
            displayRecentTransactions(allTransactions.slice(0, 10));
            
        } catch (error) {
            console.error('Error loading transactions:', error);
            transactionsDiv.innerHTML = '<div class="empty-state"><p>Error loading transactions</p></div>';
        }
    }
    
    // Display recent transactions
    function displayRecentTransactions(transactions) {
        const transactionsDiv = document.getElementById('recentTransactions');
        if (!transactionsDiv) return;
        
        const typeIcons = {
            'DEPOSIT': '💰',
            'WITHDRAWAL': '💸',
            'TRANSFER': '🔄',
            'BILL_PAYMENT': '📄',
            'RECHARGE': '📱',
            'INTEREST': '💹'
        };
        
            const html = transactions.map(transaction => {
            const fromAccountNum = transaction.fromAccountNumber || transaction.fromAccount?.accountNumber || '-';
            const toAccountNum = transaction.toAccountNumber || transaction.toAccount?.accountNumber || transaction.externalAccountNumber || '-';
            const currentAccountNum = transaction._accountNumber || '';
            
            // Determine if this transaction is debit or credit for the account it belongs to
            const isDebit = (fromAccountNum === currentAccountNum);
            const isCredit = (toAccountNum === currentAccountNum);
            
            let transactionDirection = '';
            let amountColor = 'hsl(215.4 16.3% 46.9%)';
            let amountPrefix = '';
            let borderColor = 'hsl(214.3 31.8% 91.4%)';
            
            if (transaction.transactionType === 'DEPOSIT') {
                transactionDirection = 'Received';
                amountColor = 'hsl(142 76% 36%)';
                amountPrefix = '+';
                borderColor = 'hsl(142 76% 36%)';
            } else if (transaction.transactionType === 'WITHDRAWAL') {
                transactionDirection = 'Withdrawn';
                amountColor = 'hsl(0 84.2% 60.2%)';
                amountPrefix = '-';
                borderColor = 'hsl(0 84.2% 60.2%)';
            } else if (isDebit) {
                transactionDirection = 'Transferred';
                amountColor = 'hsl(0 84.2% 60.2%)';
                amountPrefix = '-';
                borderColor = 'hsl(0 84.2% 60.2%)';
            } else if (isCredit) {
                transactionDirection = 'Received';
                amountColor = 'hsl(142 76% 36%)';
                amountPrefix = '+';
                borderColor = 'hsl(142 76% 36%)';
            }
            
            const amount = transaction.amount ? parseFloat(transaction.amount) : 0;
            const transactionDate = transaction.transactionDate ? new Date(transaction.transactionDate) : new Date();
            const typeIcon = typeIcons[transaction.transactionType] || '💳';
            const status = transaction.status || 'UNKNOWN';
            const statusColor = status === 'COMPLETED' ? 'hsl(142 76% 36%)' : status === 'FAILED' ? 'hsl(0 84.2% 60.2%)' : 'hsl(38 92% 50%)';
            const statusBg = status === 'COMPLETED' ? 'hsl(142 76% 36% / 0.1)' : status === 'FAILED' ? 'hsl(0 84.2% 60.2% / 0.1)' : 'hsl(38 92% 50% / 0.1)';
            const statusIcon = status === 'COMPLETED' ? '✓' : status === 'FAILED' ? '✗' : '⏳';
            
            // Format date nicely
            const dateStr = transactionDate.toLocaleDateString('en-US', { 
                month: 'short', 
                day: 'numeric', 
                year: 'numeric',
                hour: '2-digit',
                minute: '2-digit'
            });
            
            return `
                <div class="transaction-item" onclick="window.location.href='/transactions?account=${currentAccountNum}'" style="display: flex; justify-content: space-between; align-items: center; padding: 20px; border-left: 3px solid ${borderColor};">
                    <div style="flex: 1;">
                        <div style="display: flex; align-items: center; gap: 12px; margin-bottom: 8px; flex-wrap: wrap;">
                            <span style="font-size: 1.5em;">${typeIcon}</span>
                            <div style="flex: 1;">
                                <strong style="font-size: 1rem; font-weight: 600;">${transaction.transactionType || 'N/A'}</strong>
                                <span style="margin-left: 10px; padding: 4px 10px; background: ${statusBg}; color: ${statusColor}; border-radius: 12px; font-size: 0.75rem; font-weight: 600;">
                                    ${statusIcon} ${status}
                                </span>
                            </div>
                        </div>
                        <p style="color: hsl(215.4 16.3% 46.9%); margin: 5px 0; font-size: 0.875rem;">${transaction.description || 'No description'}</p>
                        <div style="display: flex; gap: 15px; margin-top: 8px; flex-wrap: wrap;">
                            <p style="color: hsl(215.4 16.3% 46.9%); font-size: 0.875rem; margin: 0;">🕒 ${dateStr}</p>
                            <p style="color: hsl(215.4 16.3% 46.9%); font-size: 0.875rem; margin: 0;">💳 Account: ${currentAccountNum}</p>
                        </div>
                    </div>
                    <div style="text-align: right; margin-left: 15px;">
                        <div style="font-size: 1.5rem; font-weight: 700; color: ${amountColor};">
                            ${amountPrefix}₹${amount.toFixed(2)}
                        </div>
                        <div style="font-size: 0.875rem; color: hsl(215.4 16.3% 46.9%); margin-top: 5px;">${transactionDirection}</div>
                    </div>
                </div>
            `;
        }).join('');
        
        transactionsDiv.innerHTML = html;
    }
    
    // Initialize when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', function() {
            loadUserInfo();
            loadAccountSummary();
        });
    } else {
        loadUserInfo();
        loadAccountSummary();
    }
    
})();

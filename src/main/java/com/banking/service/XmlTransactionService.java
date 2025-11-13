package com.banking.service;

import com.banking.model.Transaction;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class XmlTransactionService {

    @Value("${banking.transactions.xml.path}")
    private String xmlFilePath;

    public void saveTransactionToXml(Transaction transaction) {
        try {
            Path path = Paths.get(xmlFilePath);
            Path parentDir = path.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }

            File xmlFile = new File(xmlFilePath);
            TransactionsWrapper wrapper;

            if (xmlFile.exists() && xmlFile.length() > 0) {
                wrapper = loadTransactionsWrapperFromXml();
            } else {
                wrapper = new TransactionsWrapper();
                wrapper.setTransactions(new ArrayList<>());
            }

            TransactionXml transactionXml = convertToXml(transaction);
            wrapper.getTransactions().add(transactionXml);

            JAXBContext jaxbContext = JAXBContext.newInstance(TransactionsWrapper.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(wrapper, xmlFile);

        } catch (JAXBException | IOException e) {
            throw new RuntimeException("Failed to save transaction to XML: " + e.getMessage(), e);
        }
    }

    private TransactionsWrapper loadTransactionsWrapperFromXml() {
        try {
            File xmlFile = new File(xmlFilePath);
            if (!xmlFile.exists() || xmlFile.length() == 0) {
                TransactionsWrapper wrapper = new TransactionsWrapper();
                wrapper.setTransactions(new ArrayList<>());
                return wrapper;
            }

            JAXBContext jaxbContext = JAXBContext.newInstance(TransactionsWrapper.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            TransactionsWrapper wrapper = (TransactionsWrapper) unmarshaller.unmarshal(xmlFile);

            if (wrapper.getTransactions() == null) {
                wrapper.setTransactions(new ArrayList<>());
            }
            return wrapper;
        } catch (JAXBException e) {
            throw new RuntimeException("Failed to load transactions from XML: " + e.getMessage(), e);
        }
    }

    public List<TransactionXml> loadTransactionsFromXml() {
        TransactionsWrapper wrapper = loadTransactionsWrapperFromXml();
        return wrapper.getTransactions() != null ? wrapper.getTransactions() : new ArrayList<>();
    }

    public List<TransactionXml> getTransactionsByAccount(String accountNumber) {
        List<TransactionXml> allTransactions = loadTransactionsFromXml();
        return allTransactions.stream()
                .filter(t -> accountNumber.equals(t.getFromAccountNumber()) ||
                           accountNumber.equals(t.getToAccountNumber()))
                .collect(Collectors.toList());
    }

    public List<TransactionXml> getTransactionsByDateRange(String accountNumber,
                                                           String startDate, String endDate) {
        List<TransactionXml> accountTransactions = getTransactionsByAccount(accountNumber);
        return accountTransactions.stream()
                .filter(t -> {
                    String txDate = t.getTransactionDate();
                    return txDate.compareTo(startDate) >= 0 && txDate.compareTo(endDate) <= 0;
                })
                .collect(Collectors.toList());
    }

    private TransactionXml convertToXml(Transaction transaction) {
        TransactionXml txXml = new TransactionXml();
        txXml.setTransactionId(transaction.getTransactionId());
        txXml.setFromAccountNumber(transaction.getFromAccount() != null ?
                transaction.getFromAccount().getAccountNumber() : null);
        txXml.setToAccountNumber(transaction.getToAccount() != null ?
                transaction.getToAccount().getAccountNumber() : transaction.getExternalAccountNumber());
        txXml.setAmount(transaction.getAmount().toString());
        txXml.setTransactionType(transaction.getTransactionType().name());
        txXml.setStatus(transaction.getStatus().name());
        txXml.setDescription(transaction.getDescription());
        txXml.setTransactionDate(transaction.getTransactionDate()
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        txXml.setReferenceNumber(transaction.getReferenceNumber());
        return txXml;
    }

    // XML wrapper class
    @jakarta.xml.bind.annotation.XmlRootElement(name = "transactions")
    @jakarta.xml.bind.annotation.XmlAccessorType(jakarta.xml.bind.annotation.XmlAccessType.FIELD)
    public static class TransactionsWrapper {
        @jakarta.xml.bind.annotation.XmlElement(name = "transaction")
        private List<TransactionXml> transactions;

        public List<TransactionXml> getTransactions() {
            return transactions;
        }

        public void setTransactions(List<TransactionXml> transactions) {
            this.transactions = transactions;
        }
    }

    // Transaction XML representation
    @jakarta.xml.bind.annotation.XmlRootElement(name = "transaction")
    @jakarta.xml.bind.annotation.XmlAccessorType(jakarta.xml.bind.annotation.XmlAccessType.FIELD)
    public static class TransactionXml {
        private String transactionId;
        private String fromAccountNumber;
        private String toAccountNumber;
        private String amount;
        private String transactionType;
        private String status;
        private String description;
        private String transactionDate;
        private String referenceNumber;

        // Getters and setters
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

        public String getFromAccountNumber() { return fromAccountNumber; }
        public void setFromAccountNumber(String fromAccountNumber) { this.fromAccountNumber = fromAccountNumber; }

        public String getToAccountNumber() { return toAccountNumber; }
        public void setToAccountNumber(String toAccountNumber) { this.toAccountNumber = toAccountNumber; }

        public String getAmount() { return amount; }
        public void setAmount(String amount) { this.amount = amount; }

        public String getTransactionType() { return transactionType; }
        public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getTransactionDate() { return transactionDate; }
        public void setTransactionDate(String transactionDate) { this.transactionDate = transactionDate; }

        public String getReferenceNumber() { return referenceNumber; }
        public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
    }
}


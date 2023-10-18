package demo;

import account.Account;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.web3j.abi.TypeEncoder;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.generated.contracts.Bank;

import java.math.BigInteger;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

public class StaffView implements Initializable {

    private Application mainApplication;

    private Bank bank;

    private StringBuilder stringBuilder;

    @FXML
    private Text addressText;
    @FXML
    private ComboBox accountSelection;

    @FXML
    private TextArea consoleOutput;

    @FXML
    private VBox loanRequests;

    @FXML
    private TableColumn<Bank.account, String> memberBalance;

    @FXML
    private TableColumn<Bank.account, String> memberLoanAmount;

    @FXML
    private TableColumn<Bank.account, String> memberName;

    @FXML
    private TableView memberTable;

    @FXML
    private TableView staffTransactionTable;

    @FXML
    private TableColumn<Bank.transaction, String> transactionAmount;

    @FXML
    private TableColumn<Bank.transaction, String> transactionDate;

    @FXML
    private TableColumn<Bank.transaction, String> transactionFrom;

    @FXML
    private TableColumn<Bank.transaction, String> transactionId;

    @FXML
    private TableColumn<Bank.transaction, String> transactionTo;

    @FXML
    private ListView loanRequestList;

    public void setMainApplication(Application mainApplication) {
        this.mainApplication = mainApplication;
        this.setAddressText("Address: " + this.mainApplication.getAccount("member1").getEnode());
        this.stringBuilder = new StringBuilder();

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        this.accountSelection.valueProperty().addListener((observable, old, newValue) -> {
            Account newCurrentAccount = this.mainApplication.getAccount(newValue.toString());
            //System.out.println(newCurrentAccount);
            this.setAddressText(newCurrentAccount.getEnode());
            this.mainApplication.setCurrentAccount(newValue.toString());
            if(newCurrentAccount.getAccountType().equals("Admin")) {
                this.renderView();
                this.mainApplication.setIsAccountAdmin(true);
            } else {
                this.mainApplication.setIsAccountAdmin(false);
            }
        });

    }

    public void setBank(Bank bank) {
        this.bank = bank;
    }


    public List getAccounts() {

        List accounts = null;

        try{
            accounts =  this.bank.getAccounts().send();
        }catch(Exception e) {
            e.printStackTrace();
        }
        return accounts;
    }

    public void setMemberTable() {

        ObservableList observableMembers = FXCollections.observableArrayList(this.getAccounts());

        this.memberTable.setItems(observableMembers);

        this.memberName.setCellValueFactory(data -> {
            return new SimpleStringProperty(data.getValue().name);
        });

        this.memberBalance.setCellValueFactory(data -> {
            return new SimpleStringProperty(data.getValue().balance.toString());
        });

        this.memberLoanAmount.setCellValueFactory(data -> {
            return new SimpleStringProperty(data.getValue().loanBalance.toString());
        });

    }

    public List getTransactions() {

        try{
            return this.bank.getTransactionArray().send();
        }catch(Exception e) {
            e.printStackTrace();
        }

        return new ArrayList();
    }

    public void setStaffTransactionTable() {

        ObservableList observableTransactions = FXCollections.observableArrayList(this.getTransactions());

        this.staffTransactionTable.setItems(observableTransactions);

        this.transactionId.setCellValueFactory(data -> {
            return new SimpleStringProperty((TypeEncoder.encode(new Bytes32(data.getValue().transactionId)).toString()));
        });


        this.transactionFrom.setCellValueFactory(data -> {
            return new SimpleStringProperty(data.getValue().fromAccount);
        });

        this.transactionTo.setCellValueFactory(data -> {
            return new SimpleStringProperty(data.getValue().toAccount);
        });

        ZoneId zoneId = ZoneId.of("Europe/London");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        this.transactionDate.setCellValueFactory(data -> {

            //System.out.println(data.getValue().date.longValue());
            Instant instant = Instant.ofEpochSecond(data.getValue().date.longValue()/ 1000000000);
            ZonedDateTime zonedDateTime = instant.atZone(zoneId);

            return new SimpleStringProperty(formatter.format(zonedDateTime));
        });

        this.transactionAmount.setCellValueFactory(data -> {
            return new SimpleStringProperty("\u00a3" + data.getValue().amount.toString());
        });

    }

    public List getLoanRequests() {

        try {
            return this.bank.getLoanRequests().send();
        }catch(Exception e) {
            e.printStackTrace(
            );
        }
        return new ArrayList();
    }

    public void setLoanRequests() {

        ObservableList observableLoanRequests = FXCollections
                .observableArrayList(this.getLoanRequests());

        this.loanRequestList.setItems(observableLoanRequests);

        this.loanRequestList.setCellFactory(param -> new ListCell<Bank.loanRequest>() {
            @Override
            protected void updateItem(Bank.loanRequest loanRequest, boolean empty) {

                super.updateItem(loanRequest, empty);

                if(empty || loanRequest == null) {
                  setText(null);
                  setGraphic(null);
                } else {
                    HBox hBox = new HBox(10);
                    Label memberName = new Label(loanRequest.member.name);
                    Label amount = new Label("\u00a3" +loanRequest.amount.toString());
                    Button acceptButton = new Button("Accept");
                    Button declineButton = new Button("Decline");

                    int index = getIndex();

                    acceptButton.setOnAction(event -> {

                        long startTime = System.nanoTime();

                        try {
                            bank.loanResponse(BigInteger.valueOf(index), true).send();
                            setConsoleOutput("Successfully accepted the loan.");
                            renderView();
                        }catch (Exception e) {
                            e.printStackTrace();
                        }

                        long finishTime = System.nanoTime();

                        System.out.println((finishTime - startTime) / 1000000000 + " seconds");
                    });

                    declineButton.setOnAction(event -> {
                        try {
                            bank.loanResponse(BigInteger.valueOf(index), false).send();
                            setConsoleOutput("Loan was successfully declined.");
                            renderView();
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

                    hBox.getChildren().addAll(memberName, amount, acceptButton, declineButton);
                    setGraphic(hBox);

                }
            }

        });

    }

    public void renderView() {

        this.setMemberTable();
        this.setStaffTransactionTable();
        this.setLoanRequests();

    }

    public void setConsoleOutput(String message) {

        this.stringBuilder.append(message + "\n");

    }

    public void setMemberChoice(Set<String> nodes) {

        nodes.forEach(key -> this.accountSelection.getItems().addAll(key));

    }

    public void setAccountSelectionValue() {
        this.accountSelection.setValue(this.mainApplication.getCurrentAccount().getName());
    }

    public void setAddressText(String address) {
        this.addressText.setText("Address: " + address);
    }
}
